package com.nullify.cleaner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nullify.cleaner.data.local.entity.ExclusionRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExclusionRuleDao {
    @Query("SELECT * FROM exclusion_rules ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ExclusionRuleEntity>>

    @Query("SELECT * FROM exclusion_rules WHERE toolType = :toolType")
    fun observeByTool(toolType: String): Flow<List<ExclusionRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ExclusionRuleEntity): Long

    @Update
    suspend fun update(rule: ExclusionRuleEntity)

    @Delete
    suspend fun delete(rule: ExclusionRuleEntity)
}
