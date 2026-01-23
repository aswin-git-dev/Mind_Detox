package com.example.mind_detox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mind_detox.data.dao.BlockedAppDao
import com.example.mind_detox.data.dao.FocusSessionDao
import com.example.mind_detox.data.entity.BlockedApp
import com.example.mind_detox.data.entity.FocusSession

@Database(entities = [BlockedApp::class, FocusSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mind_detox_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
