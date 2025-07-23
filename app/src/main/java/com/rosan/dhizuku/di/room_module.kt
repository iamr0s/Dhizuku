package com.rosan.dhizuku.di

import com.rosan.dhizuku.data.settings.model.preferences.impl.SettingsRepoImpl
import com.rosan.dhizuku.data.settings.model.room.DhizukuRoom
import com.rosan.dhizuku.data.settings.model.room.impl.AppRepoImpl
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.data.settings.repo.SettingsRepo

import org.koin.dsl.module

val roomModule = module {
    single {
        DhizukuRoom.createInstance()
    }

    single<AppRepo> {
        val room = get<DhizukuRoom>()
        AppRepoImpl(room.appDao)
    }

    single<SettingsRepo> {
        SettingsRepoImpl()
    }
}