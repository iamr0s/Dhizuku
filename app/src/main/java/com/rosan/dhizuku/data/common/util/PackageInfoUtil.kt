package com.rosan.dhizuku.data.common.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

fun PackageManager.getPackageNameForUid(uid: Int): String? =
    getPackagesForUid(uid)?.first()

fun PackageManager.getPackageInfoForUid(
    uid: Int,
    flags: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES
    else PackageManager.GET_SIGNATURES
): PackageInfo? = kotlin.runCatching {
    getPackageInfo(getPackageNameForUid(uid) ?: return null, flags)
}.getOrNull()

@OptIn(ExperimentalStdlibApi::class)
val PackageInfo.signature: String?
    get() = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) signingInfo?.apkContentsSigners
    else signatures)?.firstOrNull()?.toByteArray()?.digest("sha256")
        ?.toHexString(HexFormat.UpperCase)
