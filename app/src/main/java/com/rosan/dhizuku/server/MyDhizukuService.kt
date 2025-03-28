package com.rosan.dhizuku.server

import android.content.ComponentName
import android.content.Context

import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.server_api.DhizukuService

import kotlinx.coroutines.runBlocking

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyDhizukuService(context: Context?, admin: ComponentName?, client: IDhizukuClient?) :
    DhizukuService(context, admin, client), KoinComponent {
    private val context by inject<Context>()

    private val repo by inject<AppRepo>()

    override fun getVersionName(): String = "$versionCode"

    private var signature: String? = null

    override fun checkCallingPermission(func: String?, callingUid: Int, callingPid: Int): Boolean {
        val entity = runBlocking { repo.findByUID(callingUid) } ?: return false
        if (!entity.allowApi) return false

        val signature = this.signature
            ?: context.packageManager.getPackageInfoForUid(callingUid)?.signature
            ?: return false
        this.signature = signature

        return signature == entity.signature
    }
}