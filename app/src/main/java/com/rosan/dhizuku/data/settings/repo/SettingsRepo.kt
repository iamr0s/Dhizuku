package com.rosan.dhizuku.data.settings.repo

import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    fun flowWhitelistMode(): Flow<Boolean>
    fun flowDhizukuEnabled(): Flow<Boolean>
    var isWhitelistMode: Boolean
    var isDhizukuEnabled: Boolean
}