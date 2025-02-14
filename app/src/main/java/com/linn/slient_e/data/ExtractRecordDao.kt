package com.linn.slient_e.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtractRecordDao {
    @Query("SELECT * FROM extract_record ORDER BY extractedDate DESC")
    fun getAllRecords(): Flow<List<ExtractRecord>>

    @Insert
    suspend fun insertRecord(record: ExtractRecord)
} 