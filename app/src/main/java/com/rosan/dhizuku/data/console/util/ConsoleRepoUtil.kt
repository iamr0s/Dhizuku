package com.rosan.dhizuku.data.console.util

import com.rosan.dhizuku.data.console.model.impl.ShizukuConsoleBuilderRepoImpl
import com.rosan.dhizuku.data.console.repo.ConsoleRepo

class ConsoleRepoUtil {
    companion object {
        suspend fun shizuku(action: ShizukuConsoleBuilderRepoImpl.() -> Unit): ConsoleRepo {
            val repo = ShizukuConsoleBuilderRepoImpl()
            repo.command("sh")
            repo.action()
            return repo.open()
        }
    }
}