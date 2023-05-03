package com.rosan.dhizuku.data.process.util

import android.content.Context
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.PrintWriter
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

object ProcessUtil : KoinComponent {
    fun <T : Any> start(clazz: KClass<T>, vararg params: Any?): Process {
        val process = Runtime.getRuntime().exec("sh")
        val writer = PrintWriter(process.outputStream, true)
        val cmd = ArrayList<String>()
        cmd.add("/system/bin/app_process")
        cmd.add(String.format("-Djava.class.path='%s'", get<Context>().packageCodePath))
        cmd.add("/system/bin")
        cmd.add(String.format("'%s'", clazz.qualifiedName))
        cmd.addAll(params.map { it.toString() })
        writer.println(cmd.joinToString(" "))
        writer.println("exit $?")
        return process
    }

    fun waitFor(process: Process, timeout: Long, unit: TimeUnit): Boolean {
        return kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) process.waitFor(timeout, unit)
            else {
                val startTime = System.nanoTime()
                var rem = unit.toNanos(timeout)

                do {
                    try {
                        process.exitValue()
                        return@runCatching true
                    } catch (ex: IllegalThreadStateException) {
                        if (rem > 0) Thread.sleep(
                            (TimeUnit.NANOSECONDS.toMillis(rem) + 1).coerceAtMost(100)
                        )
                    }
                    rem = unit.toNanos(timeout) - (System.nanoTime() - startTime)
                } while (rem > 0)
                return@runCatching false
            }
        }.getOrDefault(false)
    }
}
