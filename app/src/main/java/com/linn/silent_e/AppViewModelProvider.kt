package com.linn.silent_e

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.linn.silent_e.ui.screens.HomeViewModel
import com.linn.silent_e.ui.screens.RecordViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(silentApplication().container.recordRepository)
        }

        initializer {
            RecordViewModel(silentApplication().container.recordRepository)
        }
    }
}

fun CreationExtras.silentApplication(): SilentApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SilentApplication)