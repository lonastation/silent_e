package com.linn.slient_e.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linn.slient_e.data.ExtractRecord
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.navigation.NavController
import android.media.MediaPlayer

@Composable
fun RecordListScreen(
    records: List<ExtractRecord>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var currentPlayingId by remember { mutableStateOf<Int?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navController.navigate("main") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(records) { record ->
                RecordItem(
                    record = record,
                    isPlaying = currentPlayingId == record.id,
                    onPlayPauseClick = { isPlaying ->
                        if (isPlaying) {
                            mediaPlayer?.apply {
                                if (isPlaying) {
                                    stop()
                                }
                                release()
                            }
                            mediaPlayer = null
                            currentPlayingId = null
                        } else {
                            // Stop current playback if any
                            mediaPlayer?.apply {
                                if (isPlaying) {
                                    stop()
                                }
                                release()
                            }
                            
                            // Start new playback
                            try {
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(record.filePath)
                                    prepare()
                                    start()
                                    setOnCompletionListener {
                                        currentPlayingId = null
                                        mediaPlayer?.release()
                                        mediaPlayer = null
                                    }
                                }
                                currentPlayingId = record.id
                            } catch (e: Exception) {
                                // Handle error (you might want to show a toast or error message)
                                currentPlayingId = null
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun RecordItem(
    record: ExtractRecord,
    isPlaying: Boolean,
    onPlayPauseClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.fileName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "ID: ${record.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Path: ${record.filePath}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Extracted: ${dateFormat.format(Date(record.extractedDate))}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTonalButton(
                onClick = { onPlayPauseClick(isPlaying) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }
} 