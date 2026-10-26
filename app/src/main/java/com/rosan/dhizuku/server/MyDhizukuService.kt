package com.rosan.dhizuku.server

import android.content.ComponentName
import android.content.Context
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.os.Process
import android.util.Log
import android.widget.Toast

import com.rosan.dhizuku.R
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
    companion object {
        private const val TAG = "MyDhizukuService"
    }

    private val context by inject<Context>()
    private val appRepo by inject<AppRepo>()
    private val settingsRepo by inject<SettingsRepo>()

    private val ownershipChangeTransactions: Set<Int> by lazy {
        runCatching {
            val stub = Class.forName("android.app.admin.IDevicePolicyManager\$Stub")
            listOf(
                "TRANSACTION_transferOwnership",
                "TRANSACTION_clearDeviceOwner",
                "TRANSACTION_clearDeviceOwnerApp",
                "TRANSACTION_clearProfileOwner"
            ).mapNotNull { name ->
                runCatching {
                    stub.getDeclaredField(name).apply { isAccessible = true }.getInt(null)
                }.getOrNull()
            }.toSet()
        }.onFailure {
            Log.w(TAG, "Unable to resolve ownership transaction codes", it)
        }.getOrDefault(emptySet())
    }

    override fun onRemoteTransact(
        binder: IBinder?,
        code: Int,
        data: Parcel?,
        reply: Parcel?,
        flags: Int
    ): Boolean {
        enforceCallingPermission("remote_transact")
        if (!settingsRepo.isAllowOwnershipChanges && isOwnershipChange(binder, code)) {
            Log.i(TAG, "Blocked ownership change from uid=${Binder.getCallingUid()}")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, R.string.ownership_change_blocked, Toast.LENGTH_LONG).show()
            }
            reply?.apply {
                setDataPosition(0)
                setDataSize(0)
            }
            return true
        }
        return super.onRemoteTransact(binder, code, data, reply, flags)
    }

    private fun isOwnershipChange(binder: IBinder?, code: Int): Boolean {
        if (binder == null) return false
        // The Dhizuku UI itself runs with the server UID; keep its activation flow working.
        if (Binder.getCallingUid() == Process.myUid()) return false
        val descriptor = runCatching { binder.interfaceDescriptor }.getOrNull() ?: return false
        if (descriptor != "android.app.admin.IDevicePolicyManager") return false
        return code in ownershipChangeTransactions
    }

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
