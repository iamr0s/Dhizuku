package com.rosan.dhizuku.data.common.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.rosan.dhizuku.server.DhizukuDAReceiver

fun Context.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(this, text, duration).show()
    }
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    toast(getString(resId), duration)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.clearDelegatedScopes(uid: Int) {
    val packageNames = packageManager.getPackagesForUid(uid) ?: emptyArray()
    val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val component = ComponentName(this, DhizukuDAReceiver::class.java)
    packageNames.forEach {
        devicePolicyManager.setDelegatedScopes(
            component,
            it,
            emptyList()
        )
    }
}
