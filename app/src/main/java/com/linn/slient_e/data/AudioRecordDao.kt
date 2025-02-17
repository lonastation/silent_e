package com.linn.slient_e.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {
    @Query("SELECT * FROM extract_record ORDER BY extractedDate DESC")
    fun getAllRecords(): Flow<List<AudioRecord>>

    @Insert
    suspend fun insertRecord(record: AudioRecord)
} 