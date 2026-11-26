package com.rosan.dhizuku.ui.page.settings.settings

data class SettingsViewState(
    val whitelistMode: Boolean = false,
    val dhizukuEnabled: Boolean = true,
    val allowOwnershipChanges: Boolean = false,
    val daemonNotificationHidden: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricUnavailable: Boolean = false
)
