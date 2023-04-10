package com.rosan.dhizuku.server

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.rosan.dhizuku.App
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DhizukuDAReceiver : DeviceAdminReceiver(), KoinComponent {
    private val app = get<Context>().applicationContext as App

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        app.syncDeviceOwnerStatus()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        return super.onDisableRequested(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        app.syncDeviceOwnerStatus()
    }
}