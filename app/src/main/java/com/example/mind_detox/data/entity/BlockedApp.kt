package com.example.mind_detox.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val dailyLimitMinutes: Int = 0
)
