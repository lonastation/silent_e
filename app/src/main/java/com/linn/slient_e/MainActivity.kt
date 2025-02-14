package com.linn.slient_e

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linn.slient_e.ui.theme.Slient_eTheme
import java.io.File
import java.nio.ByteBuffer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.linn.slient_e.data.AppDatabase
import com.linn.slient_e.data.ExtractRecord
import com.linn.slient_e.ui.RecordListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Slient_eTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") {
                            val scope = rememberCoroutineScope()
                            Box(modifier = Modifier.fillMaxSize()) {
                                MainScreen(
                                    modifier = Modifier,
                                    onSaveRecord = { fileName, filePath ->
                                        val db = AppDatabase.getDatabase(applicationContext)
                                        scope.launch {
                                            db.extractRecordDao().insertRecord(
                                                ExtractRecord(
                                                    fileName = fileName,
                                                    filePath = filePath
                                                )
                                            )
                                        }
                                    }
                                )
                                FloatingActionButton(
                                    onClick = { navController.navigate("records") },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.List, "View Records")
                                }
                            }
                        }
                        composable("records") {
                            val db = AppDatabase.getDatabase(applicationContext)
                            val records by db.extractRecordDao()
                                .getAllRecords()
                                .collectAsState(initial = emptyList())

                            RecordListScreen(records = records)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier,
    onSaveRecord: (fileName: String, filePath: String) -> Unit
) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractionProgress by remember { mutableFloatStateOf(0f) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var mp3FileName by remember { mutableStateOf("") }
    var extractedFile by remember { mutableStateOf<File?>(null) }
    var savedFilePath by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 34.dp, start = 20.dp, end = 20.dp)
    ) {
        VideoPicker { uri ->
            selectedVideoUri = uri
            isExtracting = false
            extractionProgress = 0f
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedVideoUri?.let { uri ->
                    isExtracting = true
                    scope.launch {
                        extractedFile = extractAudioAndConvertToMp3(
                            uri,
                            context,
                            onProgressUpdate = { progress ->
                                extractionProgress = progress
                            }
                        )
                        isExtracting = false
                        showSaveDialog = true
                    }
                }
            },
            enabled = selectedVideoUri != null && !isExtracting
        ) {
            Text("Start Audio Extraction")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isExtracting) {
            LinearProgressIndicator(
                progress = { extractionProgress },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Extracting: ${(extractionProgress * 100).toInt()}%")
        }

        if (selectedVideoUri != null) {
            Text("Selected Video: ${selectedVideoUri.toString()}")
        }

        savedFilePath?.let { path ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Saved MP3 file: $path",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save MP3 File") },
            text = {
                OutlinedTextField(
                    value = mp3FileName,
                    onValueChange = { mp3FileName = it },
                    label = { Text("Enter file name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        extractedFile?.let { file ->
                            val newFile = File(
                                context.getExternalFilesDir(null),
                                "$mp3FileName.mp3"
                            )
                            file.renameTo(newFile)
                            savedFilePath = newFile.absolutePath
                            onSaveRecord(mp3FileName, newFile.absolutePath)
                        }
                        showSaveDialog = false
                        mp3FileName = ""
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    Slient_eTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            MainScreen(Modifier.padding(innerPadding), onSaveRecord = { _: String, _: String -> })
        }
    }
}

@Composable
fun VideoPicker(onVideoSelected: (Uri) -> Unit) {
    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                onVideoSelected(uri)
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    Button(
        onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Select Video")
    }
}

suspend fun extractAudioAndConvertToMp3(
    videoUri: Uri,
    context: Context,
    onProgressUpdate: (Float) -> Unit
): File? = withContext(Dispatchers.IO) {
    try {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, videoUri, null)
        Log.d("AudioExtractor", "start")

        var audioTrackIndex = -1
        var audioFormat: MediaFormat? = null

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("audio/")) {
                audioTrackIndex = i
                audioFormat = format
                extractor.selectTrack(audioTrackIndex)
                break
            }
        }

        if (audioTrackIndex >= 0 && audioFormat != null) {
            val outputFile = File(context.getExternalFilesDir(null), "temp_audio.mp3")
            val muxer =
                MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val muxerAudioTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            var totalBytesRead = 0L
            val duration = audioFormat.getLong(MediaFormat.KEY_DURATION)

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.flags =
                    if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                        MediaCodec.BUFFER_FLAG_KEY_FRAME
                    } else {
                        0
                    }

                muxer.writeSampleData(muxerAudioTrackIndex, buffer, bufferInfo)
                totalBytesRead += sampleSize

                val progress = extractor.sampleTime.toFloat() / duration.toFloat()
                withContext(Dispatchers.Main) {
                    onProgressUpdate(progress)
                }

                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            extractor.release()

            Log.d("AudioExtractor", "Audio extracted to: ${outputFile.absolutePath}")
            return@withContext outputFile
        } else {
            Log.e("AudioExtractor", "No audio track found in the video.")
            extractor.release()
            return@withContext null
        }
    } catch (e: Exception) {
        Log.e("AudioExtractor", "Error extracting audio", e)
        return@withContext null
    }
}