package com.rosan.dhizuku.server

import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.rosan.dhizuku.BuildConfig
import com.rosan.dhizuku.R
import org.koin.core.component.KoinComponent

class DhizukuDAReceiver : DeviceAdminReceiver(), KoinComponent {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        DaemonReceiver().onReceive(context, intent)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        super.onEnabled(context, intent)
        if (dpm!!.isDeviceOwnerApp(BuildConfig.APPLICATION_ID)) {
            Toast.makeText(
                context,
                context.getString(R.string.home_status_owner_granted),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}