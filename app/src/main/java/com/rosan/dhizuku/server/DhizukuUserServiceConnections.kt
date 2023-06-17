package com.rosan.dhizuku.server

import android.content.ComponentName
import android.os.IBinder
import com.rosan.dhizuku.BuildConfig
import com.rosan.dhizuku.aidl.IDhizukuUserService
import com.rosan.dhizuku.aidl.IDhizukuUserServiceConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

object DhizukuUserServiceConnections {
    private fun onServiceConnected(args: DhizukuUserServiceArgs, service: UserService) {
        connectionMap.values.forEach {
            it.connected(args, service)
        }
        services[args.componentName.flattenToString()] = service
        kotlin.runCatching {
            service.service.linkToDeath({
                onServiceDisconnected(args)
            }, 0)
        }
    }

    private fun onServiceDisconnected(args: DhizukuUserServiceArgs) {
        connectionMap.values.forEach {
            it.died(args)
        }
        services.remove(args.componentName.flattenToString())
    }

    private val connectionMap = mutableMapOf<String, DhizukuUserServiceConnection>()

    private val services = mutableMapOf<String, UserService>()

    fun start(args: DhizukuUserServiceArgs) {
        val name = args.componentName
        val token = name.flattenToString()
        val service = services[token]
        if (service != null) return
        CoroutineScope(Dispatchers.IO).launch {
            startInner(args)
        }
    }

    private suspend fun startInner(args: DhizukuUserServiceArgs): UserService? {
        val scope = CoroutineScope(Dispatchers.IO)
        var process: Process? = null
        val manager = IDhizukuUserService.Stub.asInterface(
            DhizukuProcess.startProcess(
                ComponentName(
                    BuildConfig.APPLICATION_ID, DhizukuUserService::class.java.name
                ), false
            ) ?: return null
        )
        val service = UserService(manager, manager.startService(args.componentName))
        onServiceConnected(args, service)
        return service
    }

    fun stop(args: DhizukuUserServiceArgs) {
        val name = args.componentName
        val token = name.flattenToString()
        stop(token)
    }

    private fun stop(token: String) {
        val service = services[token] ?: return
        kotlin.runCatching { service.manager.quit() }
    }

    fun bind(
        uid: Int, pid: Int, args: DhizukuUserServiceArgs, iConnection: IDhizukuUserServiceConnection
    ) {
        val owner = "$uid:$pid"
        val name = args.componentName
        val token = name.flattenToString()
        val connection =
            connectionMap[owner] ?: DhizukuUserServiceConnection(uid, pid, iConnection).apply {
                connectionMap[owner] = this
            }
        connection.connection = iConnection
        connection.register(args)
        val service = services[token]
        if (service == null) {
            start(args)
            return
        }
        connection.connected(args, service)
    }

    fun unbind(uid: Int, pid: Int, args: DhizukuUserServiceArgs) {
        val owner = "$uid:$pid"
        val name = args.componentName
        val token = name.flattenToString()
        val connection = connectionMap[owner] ?: return
        connection.unregister(args)
        afterUnbind()
    }

    fun unbind(uid: Int, pid: Int) {
        val owner = "$uid:$pid"
        connectionMap.remove(owner)
        afterUnbind()
    }

    private fun afterUnbind() {
        val tokens = mutableListOf<String>()
        tokens.addAll(services.keys)
        for (token in services.keys) {
            for (connection in connectionMap.values) {
                if (connection.isRegister(token)) {
                    tokens.remove(token)
                    break
                }
            }
        }
        for (token in tokens) {
            stop(token)
        }
        Shizuku.checkSelfPermission()
    }

    data class UserService(val manager: IDhizukuUserService, val service: IBinder)
}
