package com.rosan.dhizuku

import android.app.Application

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
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
            modules(module { single { this@App } })
        }
        DhizukuState.sync(this)
        Sui.init(packageName)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}