package com.rosan.dhizuku.data.common.util

import android.os.ParcelFileDescriptor
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

private fun transferThread(input: InputStream, output: OutputStream) {
    thread(
        isDaemon = true
    ) {
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        var len = 0;
        try {
            while (run {
                    len = input.read(buf)
                    return@run len
                } > 0) {
                output.write(buf, 0, len)
                output.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                input.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                output.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun InputStream.parcelable(): ParcelFileDescriptor {
    val pipe = ParcelFileDescriptor.createPipe()
    val read = pipe[0]
    val write = pipe[1]
    transferThread(this, ParcelFileDescriptor.AutoCloseOutputStream(write))
    return read
}

fun OutputStream.parcelable(): ParcelFileDescriptor {
    val pipe = ParcelFileDescriptor.createPipe()
    val read = pipe[0]
    val write = pipe[1]
    transferThread(ParcelFileDescriptor.AutoCloseInputStream(read), this)
    return write
}