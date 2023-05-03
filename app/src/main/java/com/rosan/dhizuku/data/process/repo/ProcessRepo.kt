package com.rosan.dhizuku.data.process.repo

import com.rosan.dhizuku.di.init.processModules
import org.koin.core.context.startKoin
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

abstract class ProcessRepo {
    /*
    * use @JvmStatic
    * */
    open fun main(args: Array<String>) {
        setIgnoreWarning(true)
        val exception = kotlin.runCatching {
            startKoin {
                modules(processModules)
            }
            onCreate(args)
            destroy()
        }.exceptionOrNull()
        setIgnoreWarning(false)
        if (exception == null) return
        exception.printStackTrace()
        throw exception
    }

    protected open fun onCreate(args: Array<String>) {
    }

    fun destroy(code: Int = 0) {
        onDestroy()
        exitProcess(code)
    }

    protected open fun onDestroy() {
    }

    private fun setIgnoreWarning(state: Boolean) {
        System.setErr(PrintStream(if (state) NullOutputStream else FileOutputStream(FileDescriptor.err)))
    }

    object NullOutputStream : OutputStream() {
        override fun write(b: Int) {
        }
    }
}