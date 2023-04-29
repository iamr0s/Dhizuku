package com.rosan.dhizuku.di

import com.rosan.dhizuku.aidl.IDhizuku
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.server.impl.IDhizukuImpl
import org.koin.dsl.module

val dhizukuModule = module {
    factory<IDhizuku> { (client: IDhizukuClient?) ->
        IDhizukuImpl(client)
    }
}