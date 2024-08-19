package com.rosan.dhizuku.server

import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.rosan.dhizuku.BuildConfig
import org.koin.core.component.KoinComponent

class DhizukuDAReceiver : DeviceAdminReceiver(), KoinComponent {
    companion object {
        val name = ComponentName(BuildConfig.APPLICATION_ID, DhizukuDAReceiver::class.java.name)
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        DhizukuState.sync(context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
    }
}