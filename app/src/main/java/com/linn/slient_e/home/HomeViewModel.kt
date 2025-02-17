package com.linn.slient_e.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.linn.slient_e.data.AudioRecord
import com.linn.slient_e.data.RecordRepository


class HomeViewModel(private val recordRepository: RecordRepository) : ViewModel() {

    var itemUiState by mutableStateOf(ItemUiState(audioDetails = AudioDetails()))
        private set

    suspend fun saveRecord() {
        recordRepository.insertRecord(
            AudioRecord(
                fileName = itemUiState.audioDetails.fileName,
                filePath = itemUiState.audioDetails.filePath
            )
        )
    }

    fun updateItemUiState(audioDetails: AudioDetails) {
        itemUiState = ItemUiState(audioDetails = audioDetails)
    }
}

data class ItemUiState(
    val audioDetails: AudioDetails = AudioDetails(),
)

data class AudioDetails(
    val fileName: String = "",
    val filePath: String = ""
)