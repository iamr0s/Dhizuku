package com.rosan.dhizuku.ui.page.settings.activate

import android.app.admin.DeviceAdminInfo

data class ActivateViewData(
    val admin: DeviceAdminInfo,
    val enabled: Boolean
)