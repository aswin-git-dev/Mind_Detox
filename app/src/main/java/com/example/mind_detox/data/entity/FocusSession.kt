package com.example.mind_detox.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val appsBlockedCount: Int
)
