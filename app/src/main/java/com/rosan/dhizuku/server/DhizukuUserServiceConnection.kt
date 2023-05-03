package com.rosan.dhizuku.server

import android.os.IBinder
import com.rosan.dhizuku.aidl.IDhizukuUserServiceConnection

class DhizukuUserServiceConnection(
    val uid: Int,
    val pid: Int,
    connection: IDhizukuUserServiceConnection
) {
    private var _connection = connection

    var connection: IDhizukuUserServiceConnection
        get() = _connection
        set(value) {
            kotlin.runCatching { _connection.asBinder().unlinkToDeath(recipient, 0) }
            _connection = value
            kotlin.runCatching {
                _connection.asBinder().linkToDeath(recipient, 0)
            }
        }

    private val recipient = IBinder.DeathRecipient {
        DhizukuUserServiceConnections.unbind(uid, pid)
    }

    private val tokens = mutableListOf<String>()

    init {
        kotlin.runCatching {
            _connection.asBinder().linkToDeath(recipient, 0)
        }
    }

    fun register(args: DhizukuUserServiceArgs) {
        val name = args.componentName
        val token = name.flattenToString()
        tokens.remove(token)
        tokens.add(token)
    }

    fun unregister(args: DhizukuUserServiceArgs) {
        val name = args.componentName
        val token = name.flattenToString()
        tokens.remove(token)
    }

    fun isRegister(token: String) = tokens.contains(token)

    fun connected(
        args: DhizukuUserServiceArgs,
        service: DhizukuUserServiceConnections.UserService
    ) {
        val name = args.componentName
        val token = name.flattenToString()
        if (token in tokens) kotlin.runCatching {
            connection.connected(
                args.build(),
                service.service
            )
        }
    }

    fun died(args: DhizukuUserServiceArgs) {
        val name = args.componentName
        val token = name.flattenToString()
        if (token in tokens) kotlin.runCatching { connection.died(args.build()) }
    }
}