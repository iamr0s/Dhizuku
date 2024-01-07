package com.rosan.dhizuku.server

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rosan.dhizuku.App
import com.rosan.dhizuku.R
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DhizukuService : Service(), KoinComponent {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DhizukuService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(
                intent
            )
            else context.startService(intent)
        }
    }

    private val app by inject<App>()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val packageName = intent.dataString ?: return
            app.syncAppRepo()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            run()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        super.onDestroy()
    }

    private suspend fun run() {
        runForeground()
        registerPackageReceiver()
    }

    private fun registerPackageReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addDataScheme("package")
        registerReceiver(packageReceiver, filter)
    }

    private suspend fun runForeground() {
        val manager = NotificationManagerCompat.from(this)
        val channelName = "service_channel"
        val channel: NotificationChannelCompat =
            NotificationChannelCompat.Builder(channelName, NotificationManagerCompat.IMPORTANCE_MAX)
                .setName(getString(R.string.service_channel_name)).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(
            channel
        )
        val notificationId = 1
        val notification = NotificationCompat.Builder(this, channel.id)
            .setSmallIcon(R.drawable.round_hourglass_empty_black_24)
            .setContentTitle(getString(R.string.service_running)).setAutoCancel(false)
            .setOngoing(true).build()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) startForeground(
            notificationId,
            notification
        ) else startForeground(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
        )
    }
}