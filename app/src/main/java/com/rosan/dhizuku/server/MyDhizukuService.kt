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

    override fun getVersionName(): String = "$versionCode"

    private var signature: String? = null

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

        if (settingsRepo.isWhitelistMode && !entity.allowApi) {
            return false
        }

        val signature = this.signature
            ?: context.packageManager.getPackageInfoForUid(callingUid)?.signature
        if (signature == null) {
            return false
        }
        this.signature = signature

        if (signature != entity.signature) {
            return false
        }

        return true
    }
}
