package com.rosan.dhizuku.di

import com.rosan.dhizuku.aidl.IDhizuku
import com.rosan.dhizuku.server.impl.IDhizukuImpl
import org.koin.dsl.module

private var iDhizuku: IDhizukuImpl? = null

val dhizukuModule = module {
    factory<IDhizuku> {
        if (iDhizuku?.pingBinder() == true) return@factory iDhizuku!!
        IDhizukuImpl().apply {
            iDhizuku = this
        }
    }
}