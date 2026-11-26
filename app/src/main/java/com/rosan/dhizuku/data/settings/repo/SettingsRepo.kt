package com.rosan.dhizuku.data.settings.repo

import kotlinx.coroutines.flow.Flow

interface SettingsRepo {
    fun flowWhitelistMode(): Flow<Boolean>
    fun flowDhizukuEnabled(): Flow<Boolean>
    fun flowDonateButtonHidden(): Flow<Boolean>
    fun flowAllowOwnershipChanges(): Flow<Boolean>
    fun flowDaemonNotificationHidden(): Flow<Boolean>
    fun flowBiometricEnabled(): Flow<Boolean>
    var isWhitelistMode: Boolean
    var isDhizukuEnabled: Boolean
    var isDonateButtonHidden: Boolean
    var isAllowOwnershipChanges: Boolean
    var isDaemonNotificationHidden: Boolean
    var isBiometricEnabled: Boolean
}