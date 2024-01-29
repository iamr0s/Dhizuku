package com.rosan.dhizuku.data.common.util

import android.app.admin.IDevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.system.Os
import com.rosan.dhizuku.R
import kotlinx.coroutines.delay
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

suspend fun setDeviceOwner(context: Context, who: ComponentName) {
    requireShizukuPermissionGranted(context) {
        // wait for the account cache be refreshed
        delay(1500)

        val binder =
            ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.DEVICE_POLICY_SERVICE))
        val manager = IDevicePolicyManager.Stub.asInterface(binder)
        val ownerName = context.getString(R.string.app_name)
        val userId = Os.getuid() / 100000
        val profileOwner = false
        manager.setActiveAdmin(who, true, userId)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                manager.setDeviceOwner(who, userId, profileOwner)
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                manager.setDeviceOwner(who, ownerName, userId, false)
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                manager.setDeviceOwner(who, ownerName, userId)
            else manager.setDeviceOwner(who.packageName, ownerName)
        } catch (e: Exception) {
            manager.removeActiveAdmin(who, userId)
            throw e
        }
    }
}