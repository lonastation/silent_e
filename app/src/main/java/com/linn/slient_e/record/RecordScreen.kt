package com.linn.slient_e.record

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.linn.slient_e.AppViewModelProvider
import com.linn.slient_e.data.AudioRecord
import com.linn.slient_e.ui.theme.Slient_eTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordScreen(
    viewModel: RecordViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by viewModel.listUiState.collectAsState()

    Slient_eTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            RecordListBody(
                records = listUiState.itemList,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun RecordListBody(
    records: List<AudioRecord>,
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
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

@Composable
fun RecordItem(
    record: AudioRecord,
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