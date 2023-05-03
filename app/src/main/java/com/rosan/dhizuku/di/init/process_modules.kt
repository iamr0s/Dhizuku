package com.rosan.dhizuku.di.init

import com.rosan.dhizuku.di.init.process.contextModule
import com.rosan.dhizuku.di.reflectModule

val processModules = listOf(
    contextModule,
    reflectModule
)


