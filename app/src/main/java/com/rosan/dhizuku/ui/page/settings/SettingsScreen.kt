package com.rosan.dhizuku.ui.page.settings

sealed class SettingsScreen(val route: String) {
    object Main : SettingsScreen("main")
}
