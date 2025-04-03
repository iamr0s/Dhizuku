package com.rosan.dhizuku.ui.activity

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity

import com.rosan.dhizuku.shared.DhizukuVariables
import com.rosan.dhizuku.server.DhizukuDAReceiver
import com.rosan.dhizuku.R

import org.koin.core.component.KoinComponent

import rikka.shizuku.ShizukuProvider

class FinalizeActivity : ComponentActivity(), KoinComponent {
    val REQUIRE_PERMISSIONS: Array<String> = arrayOf(
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.MANAGE_EXTERNAL_STORAGE,
          DhizukuVariables.PERMISSION_API,
          //ShizukuProvider.PERMISSION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            REQUIRE_PERMISSIONS.forEach { permission ->
                (getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager).setPermissionGrantState(ComponentName(this, DhizukuDAReceiver::class.java), DhizukuVariables.OFFICIAL_PACKAGE_NAME, permission, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)
            }
        }
        setResult(Activity.RESULT_OK)
        Toast.makeText(this, getString(R.string.home_status_owner_granted), Toast.LENGTH_LONG).show()
        finish()
    }
}