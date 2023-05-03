package com.rosan.dhizuku.di

import android.os.Build
import com.rosan.dhizuku.data.reflect.model.impl.ReflectRepoImpl
import com.rosan.dhizuku.data.reflect.repo.ReflectRepo
import org.koin.dsl.module
import org.lsposed.hiddenapibypass.HiddenApiBypass

val reflectModule = module {
    single<ReflectRepo> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.setHiddenApiExemptions("")
        }
        ReflectRepoImpl()
    }
}