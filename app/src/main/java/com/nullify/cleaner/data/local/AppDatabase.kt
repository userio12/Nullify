package com.nullify.cleaner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nullify.cleaner.data.local.dao.CleanupLogDao
import com.nullify.cleaner.data.local.dao.DeletedFileDao
import com.nullify.cleaner.data.local.dao.ExclusionRuleDao
import com.nullify.cleaner.data.local.dao.ScheduleDao
import com.nullify.cleaner.data.local.entity.CleanupLogEntity
import com.nullify.cleaner.data.local.entity.DeletedFileEntity
import com.nullify.cleaner.data.local.entity.ExclusionRuleEntity
import com.nullify.cleaner.data.local.entity.ScheduleEntity

@Database(
    entities = [
        CleanupLogEntity::class,
        ScheduleEntity::class,
        ExclusionRuleEntity::class,
        DeletedFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cleanupLogDao(): CleanupLogDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun exclusionRuleDao(): ExclusionRuleDao
    abstract fun deletedFileDao(): DeletedFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nullify_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
