package com.rosan.dhizuku.server

import android.app.admin.DevicePolicyManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rosan.dhizuku.BuildConfig

data object DhizukuState {
    data class State(
        val device: Boolean = false,
        val profile: Boolean = false
    ) {
        val owner = device || profile
    }

    var state by mutableStateOf(State())
        private set

    fun sync(devicePolicyManager: DevicePolicyManager) {
        val packageName = BuildConfig.APPLICATION_ID
        state = State(
            device = devicePolicyManager.isDeviceOwnerApp(packageName),
            profile = devicePolicyManager.isProfileOwnerApp(packageName)
        )
    }
}