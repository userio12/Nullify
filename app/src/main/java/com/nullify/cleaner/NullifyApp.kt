package com.nullify.cleaner

import android.app.Application
import com.nullify.cleaner.di.appModule
import com.nullify.cleaner.di.dataModule
import com.nullify.cleaner.di.domainModule
import com.nullify.cleaner.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import rikka.shizuku.ShizukuProvider
import rikka.sui.Sui

class NullifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Sui.init(this.packageName)
        startKoin {
            androidContext(this@NullifyApp)
            modules(appModule, dataModule, domainModule, uiModule)
        }
    }
}
