package com.example.mind_detox.data.dao

import androidx.room.*
import com.example.mind_detox.data.entity.BlockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps")
    fun getAllBlockedApps(): Flow<List<BlockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: BlockedApp)

    @Delete
    suspend fun delete(app: BlockedApp)

    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): BlockedApp?
}
