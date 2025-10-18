package com.rosan.dhizuku.di.init

import com.rosan.dhizuku.di.dataModule
import com.rosan.dhizuku.di.reflectModule
import com.rosan.dhizuku.di.viewModelModule

val appModules = listOf(
    dataModule,
    viewModelModule,
    reflectModule
)