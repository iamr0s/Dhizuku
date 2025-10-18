package com.rosan.dhizuku.data.common.util

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

inline fun <reified T> SharedPreferences.asFlow(
    key: String,
    defaultValue: T,
    crossinline getter: () -> T = {
        val value = all.getOrElse(key) { defaultValue }
        if (value is T) value
        else defaultValue
    }
) = callbackFlow {
    // 初始值发射
    trySend(getter())

    // 监听数据变化
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey == key) {
            trySend(getter())
        }
    }
    registerOnSharedPreferenceChangeListener(listener)

    // 取消监听
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}

fun SharedPreferences.stringFlow(key: String, defaultValue: String?) = asFlow(key, defaultValue)
