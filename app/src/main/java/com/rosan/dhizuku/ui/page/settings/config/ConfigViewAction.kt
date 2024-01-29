package com.rosan.dhizuku.ui.page.settings.config

sealed class ConfigViewAction {
    data object Init : ConfigViewAction()

    data class UpdateConfigByUID(val uid: Int, val allowApi: Boolean) : ConfigViewAction()
}