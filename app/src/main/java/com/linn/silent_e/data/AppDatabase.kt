package com.linn.silent_e.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AudioRecord::class], version = 1, exportSchema = false)
abstract class SilentDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao

    companion object {
        @Volatile
        private var Instance: SilentDatabase? = null

        fun getDatabase(context: Context): SilentDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, SilentDatabase::class.java, "silent_database")
                    .build().also { Instance = it }
            }
        }
    }
}