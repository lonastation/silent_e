package com.linn.silent_e.ui.screens

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
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.linn.silent_e.AppViewModelProvider
import com.linn.silent_e.data.AudioRecord
import com.linn.silent_e.ui.theme.Silent_eTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordScreen(
    viewModel: RecordViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by viewModel.listUiState.collectAsState()

    Silent_eTheme {
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
    var isRandomPlay by remember { mutableStateOf(true) }
    var isPlayingAll by remember { mutableStateOf(false) }
    var currentPlayingIndex by remember { mutableIntStateOf(0) }

    fun playRecord(record: AudioRecord, onComplete: () -> Unit = {}) {
        // Release current player if any
        mediaPlayer?.release()
        mediaPlayer = null

        // Start new playback
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(record.filePath)
                prepare()
                start()
                setOnCompletionListener {
                    if (isPlayingAll) {
                        onComplete()
                    } else {
                        currentPlayingId = null
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }
            }
            currentPlayingId = record.id
        } catch (e: Exception) {
            currentPlayingId = null
            isPlayingAll = false
        }
    }

    fun playNextRecord(records: List<AudioRecord>, isRandom: Boolean) {
        if (records.isEmpty()) return

        val nextIndex = if (isRandom) {
            records.indices.random()
        } else {
            (currentPlayingIndex + 1) % records.size
        }
        currentPlayingIndex = nextIndex

        playRecord(
            records[nextIndex],
            onComplete = { playNextRecord(records, isRandom) }
        )
    }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { isRandomPlay = !isRandomPlay }
            ) {
                Icon(
                    imageVector = if (isRandomPlay) Icons.Default.CatchingPokemon else Icons.AutoMirrored.Outlined.QueueMusic,
                    contentDescription = if (isRandomPlay) "Random Play" else "Sequential Play"
                )
            }

            IconButton(
                onClick = {
                    if (isPlayingAll) {
                        // Stop playing
                        mediaPlayer?.apply {
                            stop()
                            release()
                        }
                        mediaPlayer = null
                        currentPlayingId = null
                        isPlayingAll = false
                        currentPlayingIndex = 0
                    } else {
                        // Check if records is not empty before starting playback
                        if (records.isNotEmpty()) {
                            // Start playing all
                            isPlayingAll = true
                            val startIndex = if (isRandomPlay) {
                                records.indices.random()
                            } else {
                                0
                            }
                            playRecord(
                                records[startIndex],
                                onComplete = { playNextRecord(records, isRandomPlay) }
                            )
                        }
                    }
                },
                enabled = isPlayingAll || records.isNotEmpty()
            ) {
                Icon(
                    imageVector = if (isPlayingAll) Icons.Outlined.StopCircle else Icons.AutoMirrored.Outlined.PlaylistPlay,
                    contentDescription = if (isPlayingAll) "Stop" else "Play All",
                    tint = if (isPlayingAll || records.isNotEmpty()) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }

        // Record list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(records) { record ->
                RecordItem(
                    record = record,
                    isPlaying = currentPlayingId == record.id,
                    onPlayPauseClick = { isPlaying ->
                        if (isPlaying) {
                            mediaPlayer?.apply {
                                stop()
                                release()
                            }
                            mediaPlayer = null
                            currentPlayingId = null
                            isPlayingAll = false
                        } else {
                            playRecord(record)
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