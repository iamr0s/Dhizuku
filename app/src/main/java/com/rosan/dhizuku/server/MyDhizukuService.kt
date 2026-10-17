package com.rosan.dhizuku.server

import android.content.ComponentName
import android.content.Context

import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import com.rosan.dhizuku.server_api.DhizukuService

import kotlinx.coroutines.runBlocking

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyDhizukuService(context: Context, admin: ComponentName?, client: IDhizukuClient?) :
    DhizukuService(context, admin, client), KoinComponent {
    private val context by inject<Context>()
    private val appRepo by inject<AppRepo>()
    private val settingsRepo by inject<SettingsRepo>()

    override fun checkCallingPermission(func: String?, callingUid: Int, callingPid: Int): Boolean {
        if (!settingsRepo.isDhizukuEnabled) {
            return false
        }

        val entity = runBlocking { appRepo.findByUID(callingUid) }
        if (entity == null) {
            return false
        }

        if (!entity.allowApi || entity.blocked) {
            return false
        }

        val signature = context.packageManager.getPackageInfoForUid(callingUid)?.signature ?: return false
        if (signature != entity.signature) {
            return false
        }

        return true
    }
}
