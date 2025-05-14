package com.rosan.dhizuku.data.common.util

import android.content.pm.PackageManager

import com.rosan.dhizuku.data.common.model.exception.ShizukuNotWorkException
import com.rosan.dhizuku.shared.DhizukuVariables

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

import rikka.shizuku.Shizuku
import rikka.sui.Sui

private suspend fun blockingRequestShizukuPermission() = callbackFlow {
    val requestCode = (Int.MIN_VALUE..Int.MAX_VALUE).random()
    val listener =
        Shizuku.OnRequestPermissionResultListener { _requestCode, grantResult ->
            if (_requestCode != requestCode) return@OnRequestPermissionResultListener
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                trySend(Unit)
            else close(ShizukuNotWorkException("sui/shizuku permission denied"))
        }
    Shizuku.addRequestPermissionResultListener(listener)
    Shizuku.requestPermission(requestCode)
    awaitClose { Shizuku.removeRequestPermissionResultListener(listener) }
}.catch {
    throw it as? ShizukuNotWorkException ?: ShizukuNotWorkException(cause = it)
}.first()

suspend fun <T> requireShizukuPermissionGranted(action: suspend () -> T): T {
    Sui.init(DhizukuVariables.OFFICIAL_PACKAGE_NAME)
    val binder = Shizuku.getBinder()
    if (binder == null || !binder.pingBinder())
        throw ShizukuNotWorkException("sui/shizuku isn't activated")
    if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
        blockingRequestShizukuPermission()
    return action()
}

fun checkShizukuWorked(): Boolean {
    val sui: Boolean = Sui.init(DhizukuVariables.OFFICIAL_PACKAGE_NAME)
    val binder = Shizuku.getBinder()
    return (sui || binder != null)
}