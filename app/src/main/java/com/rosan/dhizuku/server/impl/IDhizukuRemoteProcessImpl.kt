package com.rosan.dhizuku.server.impl

import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.rosan.dhizuku.aidl.IDhizukuRemoteProcess
import com.rosan.dhizuku.data.common.util.parcelable
import java.io.IOException
import java.util.concurrent.TimeUnit

class IDhizukuRemoteProcessImpl(private val process: Process, private val iBinder: IBinder) :
    IDhizukuRemoteProcess.Stub() {
    init {
        iBinder.linkToDeath({
            kotlin.runCatching { if (alive()) destroy() }
        }, 0)
    }

    private var output: ParcelFileDescriptor? = null

    private var input: ParcelFileDescriptor? = null

    private var error: ParcelFileDescriptor? = null

    override fun getOutputStream(): ParcelFileDescriptor {
        if (output != null) return output!!
        try {
            output = process.outputStream.parcelable()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        return output!!
    }

    override fun getInputStream(): ParcelFileDescriptor {
        if (input != null) return input!!
        try {
            input = process.inputStream.parcelable()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        return input!!
    }

    override fun getErrorStream(): ParcelFileDescriptor {
        if (error != null) return error!!
        try {
            error = process.errorStream.parcelable()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
        return error!!
    }

    override fun exitValue(): Int = process.exitValue()

    override fun destroy() = process.destroy()

    override fun alive(): Boolean {
        return try {
            exitValue()
            false
        } catch (e: IllegalThreadStateException) {
            true
        }
    }

    override fun waitFor(): Int {
        try {
            return process.waitFor()
        } catch (e: InterruptedException) {
            throw IllegalStateException(e)
        }
    }

    override fun waitForTimeout(timeout: Long, unitName: String): Boolean {
        val unit = TimeUnit.valueOf(unitName)
        val start = System.nanoTime()
        var rem = unit.toNanos(timeout)
        do {
            try {
                exitValue()
                return true
            } catch (e: IllegalThreadStateException) {
                if (rem > 0) {
                    try {
                        Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100))
                    } catch (e: InterruptedException) {
                        throw IllegalStateException(e)
                    }
                }
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - start)
        } while (rem > 0)
        return false
    }
}