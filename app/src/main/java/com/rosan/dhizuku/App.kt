package com.rosan.dhizuku

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rosan.dhizuku.di.init.appModules
import com.rosan.dhizuku.server.DhizukuDAReceiver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import rikka.sui.Sui

class App : Application() {
    var isDeviceAdminer by mutableStateOf(false)
        private set

    var isDeviceOwner by mutableStateOf(false)
        private set

    override fun onCreate() {
        super.onCreate()
        syncDeviceOwnerStatus()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
        }
        Sui.init(packageName)
    }

    fun syncDeviceOwnerStatus() {
        val manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        isDeviceAdminer = manager.isAdminActive(ComponentName(this, DhizukuDAReceiver::class.java))
        isDeviceOwner = manager.isDeviceOwnerApp(this.packageName)
    }
}
