package com.rosan.dhizuku.ui.activity

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle

import androidx.activity.ComponentActivity

import org.koin.core.component.KoinComponent

class ProvisioningActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent()
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}