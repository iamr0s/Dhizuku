package com.rosan.dhizuku.ui.page.settings.account_manager

sealed class AccountManagerViewAction {
    object Load : AccountManagerViewAction()
    data class FreezeToggle(val packageName: String, val currentFrozen: Boolean) : AccountManagerViewAction()
    object FreezeAll : AccountManagerViewAction()
    object UnfreezeAll : AccountManagerViewAction()
}