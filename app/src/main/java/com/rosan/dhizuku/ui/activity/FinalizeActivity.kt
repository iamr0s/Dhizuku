package com.rosan.dhizuku.ui.activity

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.rosan.dhizuku.shared.DhizukuVariables
import org.koin.core.component.KoinComponent

class FinalizeActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        do {} while(!devicePolicyManager.isDeviceOwnerApp(packageName))
        devicePolicyManager.setProfileEnabled(DhizukuVariables.COMPONENT_NAME)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
