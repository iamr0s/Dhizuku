package com.rosan.dhizuku.data.settings.model.preferences.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.rosan.dhizuku.data.common.util.asFlow
import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsRepoImpl : SettingsRepo, KoinComponent {
    private val context by inject<Context>()

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("dhizuku_settings", Context.MODE_PRIVATE)
    }

    override fun flowWhitelistMode(): Flow<Boolean> =
        prefs.asFlow("whitelist_mode", false)

    override fun flowDhizukuEnabled(): Flow<Boolean> =
        prefs.asFlow("dhizuku_enabled", true)

    override var isWhitelistMode: Boolean
        get() = prefs.getBoolean("whitelist_mode", false)
        set(value) = prefs.edit(true) {
            putBoolean("whitelist_mode", value)
        }

    override var isDhizukuEnabled: Boolean
        get() = prefs.getBoolean("dhizuku_enabled", true)
        set(value) = prefs.edit(true) {
            putBoolean("dhizuku_enabled", value)
        }
}