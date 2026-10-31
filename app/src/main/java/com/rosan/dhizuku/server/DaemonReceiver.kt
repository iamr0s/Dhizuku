package com.rosan.dhizuku.server

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

import com.rosan.dhizuku.ui.activity.SettingsActivity

class DaemonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val action = intent?.action ?: return
        if (action == Telephony.Sms.Intents.SECRET_CODE_ACTION) {
            context.startActivity(Intent(context, SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        }
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED &&
            action != DeviceAdminReceiver.ACTION_DEVICE_ADMIN_DISABLE_REQUESTED &&
            action != DeviceAdminReceiver.ACTION_DEVICE_ADMIN_DISABLED &&
            action != DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED &&
            action != DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED &&
            action != DeviceAdminReceiver.ACTION_PROFILE_PROVISIONING_COMPLETE
        ) return
        DhizukuState.sync(context)
    }
}