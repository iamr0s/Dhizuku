package com.rosan.dhizuku.data.console.model.impl

import android.content.pm.PackageManager
import com.rosan.dhizuku.data.console.repo.ConsoleBuilderRepo
import com.rosan.dhizuku.data.console.repo.ConsoleRepo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import rikka.shizuku.Shizuku

class ShizukuConsoleBuilderRepoImpl : ConsoleBuilderRepo() {
    private fun _open(): ConsoleRepoImpl {
        return ConsoleRepoImpl(
            Shizuku.newProcess(
                command.toTypedArray(),
                environment?.toTypedArray(),
                directory
            )
        )
    }

    override suspend fun open(): ConsoleRepo {
        return callbackFlow {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                send(_open())
                awaitClose { }
            } else {
                val listener =
                    Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
                        if (requestCode != 0) return@OnRequestPermissionResultListener
                        kotlin.runCatching { _open() }
                            .onSuccess {
                                trySend(it)
                            }
                            .onFailure {
                                close(it)
                            }
                    }
                Shizuku.addRequestPermissionResultListener(listener)
                Shizuku.requestPermission(0)
                awaitClose {
                    Shizuku.removeRequestPermissionResultListener(listener)
                }
            }
        }.first()
    }
}