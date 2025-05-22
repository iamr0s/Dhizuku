package com.rosan.dhizuku.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import kotlin.system.exitProcess

class ShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        exitProcess(0)
    }
}