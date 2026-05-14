package com.nullify.cleaner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nullify_prefs")

class AppPreferences(private val context: Context) {

    private object Keys {
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val MODE_OVERRIDE = stringPreferencesKey("mode_override")
        val RECYCLE_BIN_ENABLED = booleanPreferencesKey("recycle_bin_enabled")
        val AUTO_DELETE_INTERVAL = intPreferencesKey("auto_delete_interval_days")
        val LAST_CLEANUP_TIME = longPreferencesKey("last_cleanup_time")
        val TOTAL_BYTES_FREED = longPreferencesKey("total_bytes_freed")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val EXCLUDED_PACKAGES = stringPreferencesKey("excluded_packages")
    }

    val firstLaunch: Flow<Boolean> = context.dataStore.data.map { it[Keys.FIRST_LAUNCH] ?: true }
    val modeOverride: Flow<String?> = context.dataStore.data.map { it[Keys.MODE_OVERRIDE] }
    val recycleBinEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.RECYCLE_BIN_ENABLED] ?: true }
    val lastCleanupTime: Flow<Long?> = context.dataStore.data.map { it[Keys.LAST_CLEANUP_TIME] }
    val totalBytesFreed: Flow<Long> = context.dataStore.data.map { it[Keys.TOTAL_BYTES_FREED] ?: 0L }
    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_THEME] ?: false }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { it[Keys.FIRST_LAUNCH] = false }
    }

    suspend fun setModeOverride(mode: String?) {
        context.dataStore.edit { if (mode != null) it[Keys.MODE_OVERRIDE] = mode else it.remove(Keys.MODE_OVERRIDE) }
    }

    suspend fun setRecycleBinEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RECYCLE_BIN_ENABLED] = enabled }
    }

    suspend fun updateLastCleanupTime() {
        context.dataStore.edit { it[Keys.LAST_CLEANUP_TIME] = System.currentTimeMillis() }
    }

    suspend fun addBytesFreed(bytes: Long) {
        context.dataStore.edit { it[Keys.TOTAL_BYTES_FREED] = (it[Keys.TOTAL_BYTES_FREED] ?: 0L) + bytes }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = dark }
    }
}
