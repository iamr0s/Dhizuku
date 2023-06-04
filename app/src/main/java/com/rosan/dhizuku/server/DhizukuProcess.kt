package com.rosan.dhizuku.server

import android.annotation.SuppressLint
import com.rosan.app_process.AppProcess

@SuppressLint("StaticFieldLeak")
object DhizukuProcess : AppProcess.Default() {
    init {
        init()
    }
}