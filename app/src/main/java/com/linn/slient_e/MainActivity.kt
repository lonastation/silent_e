package com.linn.slient_e

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Slient_eTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
    val context = LocalContext.current

    Column (
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
                    extractAudioFromVideo(uri, context)
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

fun extractAudioFromVideo(videoUri: Uri, context: Context) {
    val mediaExtractor = MediaExtractor()
    mediaExtractor.setDataSource(context, videoUri, null)

    for (i in 0 until mediaExtractor.trackCount) {
        val format = mediaExtractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME)

        if (mime != null) {
            if (mime.startsWith("audio/")) {
                mediaExtractor.selectTrack(i)

                // Here you can set up your MediaMuxer to write the audio to a file
                // This is a simplified example; you would need to handle buffering and writing properly
                break
            }
        }
    }

    mediaExtractor.release()
}