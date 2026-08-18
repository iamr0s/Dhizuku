package com.rosan.dhizuku.ui.page.settings.user_manager

import com.rosan.dhizuku.data.account.entity.UserEntity

sealed class UserManagerViewAction {
    object Load : UserManagerViewAction()

    data class Remove(val user: UserEntity) : UserManagerViewAction()
}