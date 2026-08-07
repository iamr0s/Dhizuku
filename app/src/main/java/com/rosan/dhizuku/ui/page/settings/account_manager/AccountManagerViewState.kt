package com.rosan.dhizuku.ui.page.settings.account_manager

import com.rosan.dhizuku.data.account.entity.AccountAuthenticatorEntity
import com.rosan.dhizuku.data.account.entity.AccountEntity

data class AccountManagerViewState(
    val authenticators: List<Authenticator> = emptyList(),
    val loading: Boolean = false
) {
    data class Authenticator(
        val auth: AccountAuthenticatorEntity,
        val accounts: List<AccountEntity>,
        val isFrozen: Boolean = false
    )
}