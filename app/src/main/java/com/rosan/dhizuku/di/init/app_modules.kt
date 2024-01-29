package com.rosan.dhizuku.di.init

import com.rosan.dhizuku.di.preferencesModule
import com.rosan.dhizuku.di.reflectModule
import com.rosan.dhizuku.di.roomModule
import com.rosan.dhizuku.di.viewModelModule

val appModules = listOf(
    roomModule,
    viewModelModule,
    reflectModule,
    preferencesModule
)