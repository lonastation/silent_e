package com.linn.silent_e

import android.app.Application
import com.linn.silent_e.data.AppContainer
import com.linn.silent_e.data.AppDataContainer

class SilentApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}