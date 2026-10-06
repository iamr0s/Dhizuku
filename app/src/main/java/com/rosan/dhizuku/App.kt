package com.rosan.dhizuku

import android.app.Application
import android.content.Context

import com.google.android.material.color.DynamicColors

import com.rosan.dhizuku.di.init.appModules
import com.rosan.dhizuku.server.DhizukuState

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module

import rikka.sui.Sui

class App : Application(), KoinComponent {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // MyDhizukuProvider is published before Application.onCreate() runs, so binder
        // calls can arrive while the process is still starting up. Start Koin as early
        // as possible, otherwise any call into MyDhizukuService fails (or crashes) with
        // "KoinApplication has not been started" (#205).
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
            modules(module { single { this@App } })
        }
    }

    override fun onCreate() {
        super.onCreate()
        DhizukuState.sync(this)
        Sui.init(packageName)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}