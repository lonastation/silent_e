package com.linn.slient_e.data

import kotlinx.coroutines.flow.Flow

interface RecordRepository {
    fun getAllRecords(): Flow<List<AudioRecord>>

    suspend fun insertRecord(record: AudioRecord)
}