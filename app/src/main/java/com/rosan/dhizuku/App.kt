package com.rosan.dhizuku

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.di.init.appModules
import com.rosan.dhizuku.server.DhizukuDAReceiver
import com.rosan.dhizuku.server.DhizukuService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import rikka.sui.Sui

class App : Application(), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val appRepo by inject<AppRepo>()

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
        syncAppRepo()
        DhizukuService.start(this)
    }

    fun syncDeviceOwnerStatus() {
        val manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        isDeviceAdminer = manager.isAdminActive(ComponentName(this, DhizukuDAReceiver::class.java))
        isDeviceOwner = manager.isDeviceOwnerApp(this.packageName)
    }

    fun syncAppRepo() {
        scope.launch {
            appRepo.all().forEach {
                if (packageManager.getPackagesForUid(it.uid).isNullOrEmpty())
                    appRepo.delete(it)
            }
        }
    }
}
