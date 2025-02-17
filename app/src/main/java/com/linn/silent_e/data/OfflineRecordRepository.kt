package com.linn.silent_e.data

import kotlinx.coroutines.flow.Flow

class OfflineRecordRepository(private val audioRecordDao: AudioRecordDao) : RecordRepository {
    override fun getAllRecords(): Flow<List<AudioRecord>> {
        return audioRecordDao.getAllRecords()
    }

    override suspend fun insertRecord(record: AudioRecord) {
        audioRecordDao.insertRecord(record)
    }
}