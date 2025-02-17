package com.linn.slient_e.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extract_record")
data class AudioRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val extractedDate: Long = System.currentTimeMillis()
) 