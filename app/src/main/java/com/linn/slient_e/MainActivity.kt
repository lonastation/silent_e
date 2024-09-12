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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.linn.slient_e.ui.theme.Slient_eTheme
import java.io.File
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Slient_eTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isExtractionStarted by remember { mutableStateOf(false) }
    var extractionStatus by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 34.dp, start = 20.dp, end = 20.dp)
    ) {
        // Video Picker Button at the top
        VideoPicker { uri ->
            selectedVideoUri = uri
            isExtractionStarted = false // Reset extraction status when a new video is picked
        }

        // Spacer to add some space between the VideoPicker and the other content
        Spacer(modifier = Modifier.height(16.dp))

        // Start Extraction Button
        Button(
            onClick = {
                selectedVideoUri?.let { uri ->
                    extractAudioAndConvertToMp3(uri, context) { status ->
                        extractionStatus = status
                    }
                    isExtractionStarted = true
                }
            },
            enabled = selectedVideoUri != null
        ) {
            Text("Start Audio Extraction")
        }

        // Display selected video URI and extraction status
        Spacer(modifier = Modifier.height(16.dp)) // Add some space for better layout
        Column {
            if (selectedVideoUri != null) {
                Text("Selected Video: ${selectedVideoUri.toString()}")
            }
            if (isExtractionStarted) {
                Text(extractionStatus)
            }
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

fun extractAudioAndConvertToMp3(videoUri: Uri, context: Context, onStatusUpdate: (String) -> Unit) {
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
        val outputFile =
            File(context.getExternalFilesDir(null), "${videoUri.lastPathSegment}_audio.mp3")
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        val muxerAudioTrackIndex = muxer.addTrack(audioFormat)
        muxer.start()

        val buffer = ByteBuffer.allocate(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) {
                break
            }

            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.flags = if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                MediaCodec.BUFFER_FLAG_KEY_FRAME
            } else {
                0
            }

            muxer.writeSampleData(muxerAudioTrackIndex, buffer, bufferInfo)
            extractor.advance()
        }

        muxer.stop()
        muxer.release()
        extractor.release()

        Log.d("AudioExtractor", "Audio extracted to: ${outputFile.absolutePath}")
        onStatusUpdate("Audio extracted to: ${outputFile.absolutePath}")
    } else {
        Log.e("AudioExtractor", "No audio track found in the video.")
        extractor.release()
        onStatusUpdate("No audio track found in video.")
    }
}