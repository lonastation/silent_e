package com.linn.slient_e

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.linn.slient_e.home.HomeViewModel
import com.linn.slient_e.record.RecordViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(SlientApplication().container.recordRepository)
        }

        initializer {
            RecordViewModel(SlientApplication().container.recordRepository)
        }
    }
}