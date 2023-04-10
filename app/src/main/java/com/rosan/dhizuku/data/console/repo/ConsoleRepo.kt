package com.rosan.dhizuku.data.console.repo

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

interface ConsoleRepo : Closeable {
    val inputStream: InputStream

    val errorStream: InputStream

    val outputStream: OutputStream

    suspend fun waitIt()

    suspend fun waitIt(timeout: Long, unit: TimeUnit): Boolean

    fun exitValue(): Int

    fun isAlive(): Boolean

    override fun close()

    fun closeForcibly()
}
