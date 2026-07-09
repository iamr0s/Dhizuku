package com.rosan.dhizuku.ui.page.settings.user_manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.dhizuku.data.common.util.replace
import com.rosan.dhizuku.data.account.entity.UserEntity
import com.rosan.dhizuku.data.account.repo.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserManagerViewModel : ViewModel(), KoinComponent {
    private val jobs = mutableMapOf<String, Job>()

    private val userService by inject<UserService>()

    var state by mutableStateOf(UserManagerViewState())
        private set

    fun dispatch(action: UserManagerViewAction) {
        when (action) {
            UserManagerViewAction.Load -> load()
            is UserManagerViewAction.Remove -> remove(action.user)
        }
    }

    private fun load() {
        jobs.replace("load") {
            it?.cancel()

            viewModelScope.launch(Dispatchers.IO) {
                var first = true
                while (true) {
                    if (first) {
                        state = state.copy(loading = true)
                    }
                    kotlin.runCatching {
                        userService.getUsers().sortedBy { it.id }
                    }.onFailure {
                        it.printStackTrace()
                        state = state.copy(cause = it, loading = false)
                    }.onSuccess {
                        state = state.copy(users = it, cause = null, loading = false)
                    }
                    first = false
                    delay(1500)
                }
            }
        }
    }

    private fun remove(user: UserEntity) {
        viewModelScope.launch {
            val success = kotlin.runCatching {
                userService.removeUser(user)
            }.getOrDefault(false)
            if (!success) {
                state = state.copy(cause = RuntimeException("Failed to remove user ${user.name}"))
            }
        }
    }
}