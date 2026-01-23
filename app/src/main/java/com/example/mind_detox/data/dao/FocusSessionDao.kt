package com.example.mind_detox.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mind_detox.data.entity.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Insert
    suspend fun insert(session: FocusSession)

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions")
    fun getTotalFocusTime(): Flow<Int?>
}
