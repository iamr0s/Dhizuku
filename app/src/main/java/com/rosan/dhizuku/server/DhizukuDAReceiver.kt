package com.rosan.dhizuku.server

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import android.widget.Toast
import com.rosan.dhizuku.R
import com.rosan.dhizuku.shared.DhizukuVariables

import org.koin.core.component.KoinComponent

class DhizukuDAReceiver : DeviceAdminReceiver(), KoinComponent {

    companion object {
        val requirePermissions: Array<String> = arrayOf(
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            DhizukuVariables.PERMISSION_API
        )
        fun grantPermissions(context: Context) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            val admin: ComponentName = ComponentName(context, DhizukuDAReceiver::class.java)
            if (dpm!!.isDeviceOwnerApp(DhizukuVariables.OFFICIAL_PACKAGE_NAME)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requirePermissions.forEach { permission ->
                        dpm.setPermissionGrantState(
                            admin,
                            DhizukuVariables.OFFICIAL_PACKAGE_NAME,
                            permission,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                        )

                    }
                }
                dpm.clearUserRestriction(
                    admin,
                    UserManager.DISALLOW_ADD_MANAGED_PROFILE
                )
                Toast.makeText(
                    context,
                    context.getString(R.string.home_status_owner_granted),
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        DhizukuState.sync(context)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        grantPermissions(context)
    }
}
