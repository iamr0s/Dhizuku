package com.rosan.dhizuku.ui.page.settings.config

import android.graphics.drawable.Drawable

data class ConfigViewState(val data: List<Data>) {
    data class Data(
        val uid: Int,
        val packageName: String,
        val label: String,
        val icon: Drawable,
        val allowApi: Boolean
    )
}
