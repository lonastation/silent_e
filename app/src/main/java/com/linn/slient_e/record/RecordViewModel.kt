package com.linn.slient_e.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linn.slient_e.data.AudioRecord
import com.linn.slient_e.data.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecordViewModel(private val recordRepository: RecordRepository) : ViewModel() {
    private var _listUiState = MutableStateFlow(ListUiState.Success(listOf()))
    val listUiState: StateFlow<ListUiState> = _listUiState

    init {
        viewModelScope.launch {
            recordRepository.getAllRecords()
                .collect { items ->
                    _listUiState.value = ListUiState.Success(items)
                }
        }
    }
}

sealed class ListUiState(val itemList: List<AudioRecord>) {
    data class Success(val records: List<AudioRecord>) : ListUiState(records)
}