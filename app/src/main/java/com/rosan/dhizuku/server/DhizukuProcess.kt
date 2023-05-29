package com.rosan.dhizuku.server

import com.rosan.app_process.AppProcess
import com.rosan.dhizuku.BuildConfig

object DhizukuProcess : AppProcess.Default() {
    init {
        init(BuildConfig.APPLICATION_ID)
    }
}