package com.rosan.dhizuku.data.console.model.entity

data class ConsoleError(
    val code: Int,
    val read: String,
    val error: String
) : Exception("code: $code, read: $read, error: $error")