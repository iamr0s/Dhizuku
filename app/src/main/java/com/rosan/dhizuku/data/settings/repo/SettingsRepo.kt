package com.rosan.dhizuku.data.settings.repo

import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    fun flowWhitelistMode(): Flow<Boolean>
    fun flowDhizukuEnabled(): Flow<Boolean>
    suspend fun setWhitelistMode(enabled: Boolean)
    suspend fun setDhizukuEnabled(enabled: Boolean)
    suspend fun getWhitelistMode(): Boolean
    suspend fun getDhizukuEnabled(): Boolean
}