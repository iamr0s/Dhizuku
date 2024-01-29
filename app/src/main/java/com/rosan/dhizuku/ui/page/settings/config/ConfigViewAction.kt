package com.rosan.dhizuku.ui.page.settings.config

sealed class ConfigViewAction {
    data object Init : ConfigViewAction()

    data class UpdateConfigByUID(
        val uid: Int,
        val signature: String,
        val allowApi: Boolean
    ) : ConfigViewAction()
}