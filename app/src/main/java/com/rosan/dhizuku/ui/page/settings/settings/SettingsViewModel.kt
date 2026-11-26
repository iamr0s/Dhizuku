package com.rosan.dhizuku.ui.page.settings.settings

import android.content.Context

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import com.rosan.dhizuku.data.common.util.BiometricAuth
import com.rosan.dhizuku.server.DaemonService

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel : ViewModel(), KoinComponent {
    private val settingsRepo by inject<SettingsRepo>()

    private val context by inject<Context>()

    var state by mutableStateOf(SettingsViewState())
        private set

    fun collect() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.flowWhitelistMode().collect { whitelistMode ->
                state = state.copy(whitelistMode = whitelistMode)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.flowDhizukuEnabled().collect { dhizukuEnabled ->
                state = state.copy(dhizukuEnabled = dhizukuEnabled)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.flowAllowOwnershipChanges().collect { allowOwnershipChanges ->
                state = state.copy(allowOwnershipChanges = allowOwnershipChanges)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.flowDaemonNotificationHidden().collect { daemonNotificationHidden ->
                state = state.copy(daemonNotificationHidden = daemonNotificationHidden)
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.flowBiometricEnabled().collect { biometricEnabled ->
                state = state.copy(biometricEnabled = biometricEnabled)
            }
        }
    }

    fun setWhitelistMode(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.isWhitelistMode = enabled
        }
    }

    fun setDhizukuEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.isDhizukuEnabled = enabled
        }
    }

    fun setAllowOwnershipChanges(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.isAllowOwnershipChanges = enabled
        }
    }

    fun setDaemonNotificationHidden(hidden: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepo.isDaemonNotificationHidden = hidden
            DaemonService.refresh(context)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (enabled && !BiometricAuth.canProtect(context)) {
                state = state.copy(biometricUnavailable = true)
                return@launch
            }
            settingsRepo.isBiometricEnabled = enabled
        }
    }

    fun clearBiometricUnavailable() {
        state = state.copy(biometricUnavailable = false)
    }
}