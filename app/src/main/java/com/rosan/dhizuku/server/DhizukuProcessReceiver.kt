package com.rosan.dhizuku.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rosan.dhizuku.aidl.IDhizukuUserServiceManager

object DhizukuProcessReceiver : BroadcastReceiver() {
    const val ACTION_USER_SERVICE = "com.rosan.dhizuku.process.user_service"

    const val PARAM_MANAGER = "manager"

    const val PARAM_USER_SERVICE = "service"

    const val PARAM_TOKEN = "token"

    private var listeners = listOf<OnUserServiceReceiverListener>()

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        when (intent.action) {
            ACTION_USER_SERVICE -> onReceiveUserService(intent)
        }
    }

    private fun onReceiveUserService(intent: Intent) {
        val extras = intent.extras ?: return
        val manager = IDhizukuUserServiceManager.Stub.asInterface(extras.getBinder(PARAM_MANAGER))
        val service = extras.getBinder(PARAM_USER_SERVICE) ?: return
        val args = DhizukuUserServiceArgs(extras)

        listeners.forEach {
            it.onReceive(args, DhizukuUserServiceConnections.UserService(manager, service))
        }
    }

    fun addUserServiceListener(listener: OnUserServiceReceiverListener) {
        listeners = ArrayList(listeners).apply {
            add(listener)
        }
    }

    fun removeUserServiceListener(listener: OnUserServiceReceiverListener) {
        listeners = ArrayList(listeners).apply {
            remove(listener)
        }
    }

    fun clearUserServiceListeners() {
        listeners = listOf()
    }

    interface OnUserServiceReceiverListener {
        fun onReceive(
            args: DhizukuUserServiceArgs,
            service: DhizukuUserServiceConnections.UserService
        )
    }
}