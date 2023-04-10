package com.rosan.dhizuku.data.console.repo

abstract class ConsoleBuilderRepo {
    companion object {
        fun strings2environment(envp: List<String>): Map<String, String> {
            val environment = mutableMapOf<String, String>()
            for (envstring in envp) {
                var envstring = envstring
                if (envstring.indexOf('\u0000'.code.toChar()) != -1)
                    envstring = envstring.replaceFirst("\u0000.*".toRegex(), "")

                val eqlsign = envstring.indexOf('=', 0)
                if (eqlsign != -1)
                    environment[envstring.substring(0, eqlsign)] = envstring.substring(eqlsign + 1)
            }
            return environment
        }
    }

    var command: List<String> = emptyList()

    var directory: String? = null

    var environment: List<String>? = null

    fun command(vararg command: String) {
        this.command = command.toList()
    }

    fun environment(vararg environment: String) {
        this.environment = environment.toList()
    }

    abstract suspend fun open(): ConsoleRepo
}
