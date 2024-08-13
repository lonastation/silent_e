package com.linn.slient_e

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.linn.slient_e.ui.theme.Slient_eTheme
import java.io.File
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Slient_eTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isExtractionStarted by remember { mutableStateOf(false) }
    var extractionStatus by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
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
                Text("Audio extraction in progress...")
            }
        }
    }
}

@Composable
fun VideoPicker(onVideoSelected: (Uri) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { onVideoSelected(it) }
        }

    Button(
        onClick = { launcher.launch("video/*") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Select Video")
    }
}

fun extractAudioAndConvertToMp3(videoUri: Uri, context: Context, onStatusUpdate: (String) -> Unit) {
    val mediaExtractor = MediaExtractor()
    mediaExtractor.setDataSource(context, videoUri, null)

    var audioTrackIndex = -1
    for (i in 0 until mediaExtractor.trackCount) {
        val format = mediaExtractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)
        if (mime != null) {
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i
                mediaExtractor.selectTrack(audioTrackIndex)
                break
            }
        }
    }

    if (audioTrackIndex >= 0) {
        // Prepare to save the audio
        val extractedAudioFile = File(context.getExternalFilesDir(null), "extracted_audio.aac")
        val mediaMuxer =
            MediaMuxer(extractedAudioFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // Create a new track in the muxer
        mediaMuxer.addTrack(mediaExtractor.getTrackFormat(audioTrackIndex))
        mediaMuxer.start()

        // Buffer for reading audio data
        val buffer = ByteBuffer.allocate(1024 * 1024) // 1 MB buffer
        val bufferInfo = MediaCodec.BufferInfo()
        var readSize: Int
        while (true) {
            readSize = mediaExtractor.readSampleData(buffer, 0)
            if (readSize < 0) {
                break // End of stream
            }
            bufferInfo.offset = 0
            bufferInfo.size = readSize
            bufferInfo.presentationTimeUs = mediaExtractor.sampleTime
            bufferInfo.flags = mediaExtractor.sampleFlags

            mediaMuxer.writeSampleData(audioTrackIndex, buffer, bufferInfo)
            mediaExtractor.advance()
        }

        mediaMuxer.stop()
        mediaMuxer.release()
        mediaExtractor.release()

        // Convert the extracted audio to MP3
        val outputMp3File = File(context.getExternalFilesDir(null), "output_audio.mp3")
        convertToMp3(extractedAudioFile, outputMp3File) { status ->
            onStatusUpdate(status)
        }
    } else {
        onStatusUpdate("No audio track found in video.")
    }
}

fun convertToMp3(inputFile: File, outputFile: File, onStatusUpdate: (String) -> Unit) {
    // Define the FFmpeg command to convert to MP3
    val command =
        "-i ${inputFile.absolutePath} -vn -ar 44100 -ac 2 -b:a 192k ${outputFile.absolutePath}"

    // Execute the FFmpeg command
    FFmpegKit.executeAsync(command) { session ->
        val returnCode = session.returnCode
        if (ReturnCode.isSuccess(returnCode)) {
            // Conversion successful
            onStatusUpdate("Conversion to MP3 successful: ${outputFile.absolutePath}")
        } else {
            // Conversion failed
            onStatusUpdate("Conversion failed with return code: $returnCode")
        }
    }
}