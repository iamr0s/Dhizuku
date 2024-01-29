package com.rosan.dhizuku.ui.page.settings.config

import android.graphics.drawable.Drawable
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity

data class ConfigViewState(
    val initialized: Boolean = false,
    val data: List<Data> = emptyList()
) {
    data class Data(
        val packageName: String,
        val label: String,
        val icon: Drawable,
        val appEntity: AppEntity
    )
}
