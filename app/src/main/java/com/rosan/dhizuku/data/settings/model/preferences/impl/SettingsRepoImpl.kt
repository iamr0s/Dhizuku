package com.rosan.dhizuku.data.settings.model.preferences.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsRepoImpl : SettingsRepo, KoinComponent {
    private val context by inject<Context>()
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("dhizuku_settings", Context.MODE_PRIVATE)
    }

    private val _whitelistMode = MutableStateFlow(prefs.getBoolean("whitelist_mode", false))
    private val _dhizukuEnabled = MutableStateFlow(prefs.getBoolean("dhizuku_enabled", true))

    override fun flowWhitelistMode(): Flow<Boolean> = _whitelistMode.asStateFlow()
    override fun flowDhizukuEnabled(): Flow<Boolean> = _dhizukuEnabled.asStateFlow()

    override suspend fun setWhitelistMode(enabled: Boolean) {
        prefs.edit { putBoolean("whitelist_mode", enabled) }
        _whitelistMode.value = enabled
    }

    override suspend fun setDhizukuEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("dhizuku_enabled", enabled) }
        _dhizukuEnabled.value = enabled
    }

    override suspend fun getWhitelistMode(): Boolean = prefs.getBoolean("whitelist_mode", false)
    override suspend fun getDhizukuEnabled(): Boolean = prefs.getBoolean("dhizuku_enabled", true)
}