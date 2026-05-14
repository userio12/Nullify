package com.nullify.cleaner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nullify.cleaner.data.local.entity.DeletedFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedFileDao {
    @Query("SELECT * FROM deleted_files ORDER BY deletedAt DESC")
    fun observeAll(): Flow<List<DeletedFileEntity>>

    @Insert
    suspend fun insert(file: DeletedFileEntity)

    @Query("DELETE FROM deleted_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM deleted_files WHERE expiresAt < :now")
    suspend fun deleteExpired(now: Long)
}
