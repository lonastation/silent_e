package com.linn.slient_e

import android.app.Application
import com.linn.slient_e.data.AppContainer
import com.linn.slient_e.data.AppDataContainer

class SlientApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}