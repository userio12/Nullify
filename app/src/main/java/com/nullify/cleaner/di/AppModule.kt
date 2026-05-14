package com.nullify.cleaner.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.storage.StorageManager
import com.nullify.cleaner.data.local.AppDatabase
import com.nullify.cleaner.data.preferences.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().cleanupLogDao() }
    single { get<AppDatabase>().scheduleDao() }
    single { get<AppDatabase>().exclusionRuleDao() }
    single { get<AppDatabase>().deletedFileDao() }

    single { AppPreferences(androidContext()) }

    single<StorageManager> { androidContext().getSystemService(Context.STORAGE_SERVICE) as StorageManager }
    single<PackageManager> { androidContext().packageManager }
}
