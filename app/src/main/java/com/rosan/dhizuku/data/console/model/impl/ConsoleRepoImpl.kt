package com.rosan.dhizuku.data.console.model.impl

import android.os.Build
import com.rosan.dhizuku.data.console.repo.ConsoleRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class ConsoleRepoImpl(
    private val process: Process
) : ConsoleRepo {
    override val inputStream: InputStream = process.inputStream

    override val errorStream: InputStream = process.errorStream

    override val outputStream: OutputStream = process.outputStream

    override suspend fun waitIt() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                process.waitFor()
            }
        }
    }

    override suspend fun waitIt(timeout: Long, unit: TimeUnit): Boolean {
        return withContext(Dispatchers.IO) {
            val startTime = System.nanoTime()
            var rem = unit.toNanos(timeout)
            do {
                kotlin.runCatching {
                    exitValue()
                    return@withContext true
                }.getOrElse {
                    if (rem > 0) Thread.sleep(
                        Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100)
                    )
                }
                rem = unit.toNanos(timeout) - (System.nanoTime() - startTime)
            } while (rem > 0)
            return@withContext false
        }
    }

    override fun exitValue(): Int {
        return process.exitValue()
    }

    override fun isAlive(): Boolean {
        return kotlin.runCatching {
            exitValue()
            false
        }.getOrElse { true }
    }

    override fun close() {
        process.destroy()
    }

    override fun closeForcibly() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            process.destroyForcibly()
        else close()
    }
}