package com.rosan.dhizuku.server

import android.os.IBinder
import com.rosan.dhizuku.aidl.IDhizukuUserServiceConnection
import com.rosan.dhizuku.aidl.IDhizukuUserServiceManager
import com.rosan.dhizuku.data.process.model.impl.DhizukuUserServiceRepoImpl
import com.rosan.dhizuku.data.process.util.ProcessUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import rikka.shizuku.Shizuku
import rikka.sui.Sui

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
        val serviceResult = scope.async {
            callbackFlow<UserService> {
                val listener = object : DhizukuProcessReceiver.OnUserServiceReceiverListener {
                    override fun onReceive(_args: DhizukuUserServiceArgs, service: UserService) {
                        if (args.componentName.flattenToString() != _args.componentName.flattenToString()) return
                        trySendBlocking(service)
                        close()
                    }
                }
                DhizukuProcessReceiver.addUserServiceListener(listener)
                process = ProcessUtil.start(
                    DhizukuUserServiceRepoImpl::class,
                    "-c='${args.componentName.flattenToString()}'"
                )
                awaitClose {
                    DhizukuProcessReceiver.removeUserServiceListener(listener)
                }
            }.first()
        }
        val timeoutResult = scope.async {
            delay(15 * 1000)
            null
        }
        val service = select<UserService?> {
            serviceResult.onAwait { it }
            timeoutResult.onAwait { it }
        }
        scope.cancel()
        if (service == null) {
            kotlin.runCatching { process?.destroy() }
        } else {
            onServiceConnected(args, service)
        }
        return service
    }

    fun stop(args: DhizukuUserServiceArgs) {
        val name = args.componentName
        val token = name.flattenToString()
        stop(token)
    }

    private fun stop(token: String) {
        val service = services[token] ?: return
        kotlin.runCatching { service.manager.destroy() }
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

    data class UserService(val manager: IDhizukuUserServiceManager, val service: IBinder)
}
