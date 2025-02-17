package com.linn.silent_e

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.linn.silent_e.home.HomeViewModel
import com.linn.silent_e.record.RecordViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(SilentApplication().container.recordRepository)
        }

        initializer {
            RecordViewModel(SilentApplication().container.recordRepository)
        }
    }
}