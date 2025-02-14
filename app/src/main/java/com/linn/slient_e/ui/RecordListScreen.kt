package com.linn.slient_e.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linn.slient_e.data.ExtractRecord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordListScreen(
    records: List<ExtractRecord>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(records) { record ->
            RecordItem(record = record)
            HorizontalDivider()
        }
    }
}

@Composable
fun RecordItem(record: ExtractRecord) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = record.fileName,
                style = MaterialTheme.typography.titleMedium
            )
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
        }
    }
} 