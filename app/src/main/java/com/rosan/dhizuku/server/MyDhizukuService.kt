package com.rosan.dhizuku.server

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.os.Process
import android.util.Log
import android.widget.Toast

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.rosan.dhizuku.R
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import com.rosan.dhizuku.server_api.DhizukuService
import com.rosan.dhizuku.ui.activity.SettingsActivity

import kotlinx.coroutines.runBlocking

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@SuppressLint("PrivateApi")
class MyDhizukuService(context: Context, admin: ComponentName?, client: IDhizukuClient?) :
    DhizukuService(context, admin, client), KoinComponent {
    companion object {
        private const val TAG = "MyDhizukuService"
        private const val OWNERSHIP_BLOCKED_NOTIFICATION_ID = 1002
        private const val OWNERSHIP_BLOCKED_CHANNEL_ID = "ownership_blocked"
    }

    private val context by inject<Context>()
    private val appRepo by inject<AppRepo>()
    private val settingsRepo by inject<SettingsRepo>()

    private val ownershipTransactions: Map<Int, Boolean> by lazy {
        runCatching {
            val stub = Class.forName("android.app.admin.IDevicePolicyManager\$Stub")
            listOf(
                "TRANSACTION_transferOwnership" to false,
                "TRANSACTION_clearDeviceOwner" to false,
                "TRANSACTION_clearDeviceOwnerApp" to false,
                "TRANSACTION_clearProfileOwner" to false,
                "TRANSACTION_setDeviceOwner" to true,
                "TRANSACTION_setProfileOwner" to true
            ).mapNotNull { (name, returnsBoolean) ->
                runCatching {
                    stub.getDeclaredField(name).apply { isAccessible = true }.getInt(null)
                }.getOrNull()?.let { it to returnsBoolean }
            }.toMap()
        }.onFailure {
            Log.w(TAG, "Unable to resolve ownership transaction codes", it)
        }.getOrDefault(emptyMap())
    }

    override fun onRemoteTransact(
        binder: IBinder?,
        code: Int,
        data: Parcel?,
        reply: Parcel?,
        flags: Int
    ): Boolean {
        if (!settingsRepo.isAllowOwnershipChanges && isOwnershipChange(binder, code)) {
            enforceCallingPermission("remote_transact")
            Log.i(TAG, "Blocked ownership change from uid=${Binder.getCallingUid()}")
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, R.string.ownership_change_blocked, Toast.LENGTH_LONG).show()
                notifyOwnershipBlocked()
            }
            reply?.apply {
                setDataPosition(0)
                setDataSize(0)
                if (ownershipTransactions[code] == true) writeInt(0)
            }
            return true
        }
        return super.onRemoteTransact(binder, code, data, reply, flags)
    }

    @SuppressLint("MissingPermission")
    private fun notifyOwnershipBlocked() {
        val manager = NotificationManagerCompat.from(context)
        val channel = NotificationChannelCompat.Builder(
            OWNERSHIP_BLOCKED_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        ).setName(context.getString(R.string.allow_ownership_changes)).build()
        manager.createNotificationChannel(channel)

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, OWNERSHIP_BLOCKED_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.ownership_change_blocked))
            .setSmallIcon(R.drawable.round_hourglass_empty_black_24)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()
        runCatching { manager.notify(OWNERSHIP_BLOCKED_NOTIFICATION_ID, notification) }
    }

    private fun isOwnershipChange(binder: IBinder?, code: Int): Boolean {
        if (binder == null) return false
        // The Dhizuku UI itself runs with the server UID; keep its activation flow working.
        if (getCallingUid() == Process.myUid()) return false
        val descriptor = runCatching { binder.interfaceDescriptor }.getOrNull() ?: return false
        if (descriptor != "android.app.admin.IDevicePolicyManager") return false
        return code in ownershipTransactions
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
