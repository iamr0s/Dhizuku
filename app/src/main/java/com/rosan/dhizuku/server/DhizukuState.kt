package com.rosan.dhizuku.server

import android.app.admin.DevicePolicyManager
import android.content.Context
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

    fun sync(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val packageName = context.packageName
        state = State(
            device = devicePolicyManager.isDeviceOwnerApp(packageName),
            profile = devicePolicyManager.isProfileOwnerApp(packageName)
        )
    }
}