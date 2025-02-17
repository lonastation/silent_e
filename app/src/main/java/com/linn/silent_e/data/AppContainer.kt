package com.linn.silent_e.data

import android.content.Context

interface AppContainer {
    val recordRepository: RecordRepository
}

class AppDataContainer(private val context: Context):AppContainer {
    override val recordRepository: RecordRepository by lazy {
        OfflineRecordRepository(AppDatabase.getDatabase(context).audioRecordDao())
    }
}