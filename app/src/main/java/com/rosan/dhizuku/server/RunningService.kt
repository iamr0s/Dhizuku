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

import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RunningService : Service(), KoinComponent {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RunningService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(intent)
            else context.startService(intent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val repo by inject<AppRepo>()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val uid = intent.getIntExtra(Intent.EXTRA_UID, -1)
            if (uid < 0) return
            scope.launch {
                val entity = repo.findByUID(uid)
                    ?: return@launch
                if (verify(entity)) return@launch
                repo.delete(entity)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        runForeground()
        registerPackageReceiver()
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerPackageReceiver() {
        scope.launch {
            repo.all()
                .filter { !verify(it) }
                .forEach { repo.delete(it) }
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addDataScheme("package")
        registerReceiver(packageReceiver, filter)
    }

    private fun verify(entity: AppEntity): Boolean {
        val packageInfo = packageManager.getPackageInfoForUid(entity.uid)
            ?: return false

        if (!entity.allowApi) return false

        if (entity.signature != packageInfo.signature)
            return false

        return true
    }

    private fun runForeground() {
        val manager = NotificationManagerCompat.from(this)
        val channelName = "running_service_channel"
        val channel: NotificationChannelCompat =
            NotificationChannelCompat.Builder(channelName, NotificationManagerCompat.IMPORTANCE_MAX)
                .setName(getString(R.string.service_channel_name))
                .setVibrationEnabled(false)
                .setSound(null, null)
                .setShowBadge(false)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            manager.createNotificationChannel(channel)
        val notificationId = 1
        val notification = NotificationCompat.Builder(this, channel.id)
            .setSmallIcon(R.drawable.round_hourglass_empty_black_24)
            .setContentTitle(getString(R.string.service_running))
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            startForeground(notificationId, notification)
        else startForeground(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
        )
    }
}