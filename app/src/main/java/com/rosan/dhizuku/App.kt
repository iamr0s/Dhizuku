package com.rosan.dhizuku

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import com.rosan.dhizuku.di.init.appModules
import com.rosan.dhizuku.server.DhizukuState
import com.rosan.dhizuku.server.RunningService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import rikka.sui.Sui

class App : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        DhizukuState.sync(getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModules)
            modules(module { single { this@App } })
        }
        Sui.init(packageName)
        RunningService.start(this)
    }

//    fun syncAppRepo() = scope.launch {
//        repo.all().forEach {
//            val packageInfo = packageManager.getPackageInfoForUid(it.uid)
//            if (it.allowApi &&
//                packageInfo != null &&
//                it.signature == packageInfo.signature
//            ) return@forEach
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) clearDelegatedScopes(it.uid)
//            repo.delete(it)
//        }
//    }
}
