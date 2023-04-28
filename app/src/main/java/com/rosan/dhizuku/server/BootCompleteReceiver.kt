package com.rosan.dhizuku.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        val action = intent.action ?: return
        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {}
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {}
            else -> return
        }
        DhizukuService.start(context)
    }
}