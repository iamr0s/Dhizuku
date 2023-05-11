package com.rosan.dhizuku.ui.page.settings.preferred

sealed class PreferredViewAction {
    object Init : PreferredViewAction()

    data class ChangeToastWhenUsingDhizuku(val value: Boolean) : PreferredViewAction()
}