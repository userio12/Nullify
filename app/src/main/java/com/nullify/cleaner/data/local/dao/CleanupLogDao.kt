package com.nullify.cleaner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nullify.cleaner.data.local.entity.CleanupLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CleanupLogDao {
    @Insert
    suspend fun insert(log: CleanupLogEntity)

    @Query("SELECT * FROM cleanup_logs ORDER BY timestamp DESC LIMIT 50")
    fun observeRecent(): Flow<List<CleanupLogEntity>>

    @Query("SELECT SUM(bytesFreed) FROM cleanup_logs")
    fun observeTotalBytesFreed(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM cleanup_logs")
    fun observeTotalCleanups(): Flow<Int?>
}
