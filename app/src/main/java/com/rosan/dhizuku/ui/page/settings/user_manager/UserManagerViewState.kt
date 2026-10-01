package com.rosan.dhizuku.ui.page.settings.user_manager

import com.rosan.dhizuku.data.account.entity.UserEntity

data class UserManagerViewState(
    val users: List<UserEntity> = emptyList(),
    val cause: Throwable? = null,
    val loading: Boolean = false
)