package com.rosan.dhizuku.ui.page.settings.config

import android.graphics.drawable.Drawable

data class ConfigViewState(
    val initialized: Boolean = false,
    val data: List<Data> = emptyList()
) {
    data class Data(
        val uid: Int,
        val packageName: String,
        val label: String,
        val icon: Drawable,
        val allowApi: Boolean
    )
}
