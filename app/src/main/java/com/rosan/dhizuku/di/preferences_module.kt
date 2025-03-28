package com.rosan.dhizuku.di

import android.content.Context
import android.content.SharedPreferences

import org.koin.dsl.module

val preferencesModule = module {
    val map = mapOf<String, SharedPreferences>()

    factory { (name: String) ->
        val preferences = map[name]
        if (preferences != null) return@factory preferences
        val context: Context = get()
        context.getSharedPreferences(name, Context.MODE_PRIVATE).apply {
            map[name]
        }
    }
}