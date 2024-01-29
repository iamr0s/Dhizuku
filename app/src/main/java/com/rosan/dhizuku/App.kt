package com.rosan.dhizuku

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rosan.dhizuku.data.common.util.clearDelegatedScopes
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.di.init.appModules
import com.rosan.dhizuku.server.DhizukuService
import com.rosan.dhizuku.shared.DhizukuVariables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module
import rikka.sui.Sui

class App : Application(), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val repo by inject<AppRepo>()

    private val devicePolicyManager by lazy {
        getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    var isDeviceAdminer by mutableStateOf(false)
        private set

    var isDeviceOwner by mutableStateOf(false)
        private set

    var isProfileOwner by mutableStateOf(false)
        private set

    var isOwner by mutableStateOf(false)
        private set

    override fun onCreate() {
        super.onCreate()
        syncOwnerStatus()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
            modules(module { single { this@App } })
        }
        Sui.init(packageName)
        syncAppRepo()
        DhizukuService.start(this)
    }

    fun syncOwnerStatus() {
        isDeviceAdminer = devicePolicyManager.isAdminActive(DhizukuVariables.COMPONENT_NAME)
        isDeviceOwner = devicePolicyManager.isDeviceOwnerApp(packageName)
        isProfileOwner = devicePolicyManager.isProfileOwnerApp(packageName)
        isOwner = isDeviceOwner || isProfileOwner
    }

    fun syncAppRepo() = scope.launch {
        repo.all().forEach {
            val packageInfo = packageManager.getPackageInfoForUid(it.uid)
            if (it.allowApi &&
                packageInfo != null &&
                it.signature == packageInfo.signature
            ) return@forEach
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) clearDelegatedScopes(it.uid)
            repo.delete(it)
        }
    }
}
