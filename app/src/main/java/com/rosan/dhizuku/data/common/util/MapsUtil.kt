package com.rosan.dhizuku.data.common.util

inline fun <K, V> MutableMap<K, V>.replace(key: K, action: (value: V?) -> V): V {
    val newValue = this[key].let(action)
    this[key] = newValue
    return newValue
}
