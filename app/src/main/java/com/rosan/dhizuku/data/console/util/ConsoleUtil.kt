package com.rosan.dhizuku.data.console.util

import com.rosan.dhizuku.data.console.repo.ConsoleRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ConsoleUtil(
    private val repo: ConsoleRepo
) {
    private suspend fun readBytes(input: InputStream): ByteArray = withContext(Dispatchers.IO) {
        var bytes = ByteArray(0)
        while (repo.isAlive())
            if (input.available() > 0)
                bytes += ByteArray(input.available()).let {
                    it.sliceArray(0 until input.read(it))
                }
        return@withContext bytes
    }

    suspend fun inputBytes(): ByteArray = readBytes(repo.inputStream)

    suspend fun errorBytes(): ByteArray = readBytes(repo.errorStream)

    suspend fun appendLine(any: Any?) = withContext(Dispatchers.IO) {
        val writer = repo.outputStream.writer()
        writer.appendLine(any.toString())
        writer.flush()
    }
}