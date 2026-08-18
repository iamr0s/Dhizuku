package com.rosan.dhizuku.data.common.util

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor

fun IBinder.transactDump(fd: FileDescriptor, args: Array<String>? = null) {
    val data = Parcel.obtain()
    val reply = Parcel.obtain()
    try {
        data.writeFileDescriptor(fd)
        data.writeStringArray(args)
        transact(Binder.DUMP_TRANSACTION, data, reply, 0)
        reply.readException()
    } finally {
        data.recycle()
        reply.recycle()
    }
}

fun IBinder.dumpBytes(args: Array<String>? = null): ByteArray {
    val pipe = ParcelFileDescriptor.createPipe()
    val readFD = pipe[0]
    val writeFD = pipe[1]
    writeFD.use {
        transactDump(it.fileDescriptor, args)
    }
    return readFD.use {
        ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
    }
}

fun IBinder.dumpText(args: Array<String>? = null): String =
    dumpBytes(args).decodeToString()
