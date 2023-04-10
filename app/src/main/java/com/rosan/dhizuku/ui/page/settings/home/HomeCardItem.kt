package com.rosan.dhizuku.ui.page.settings.home

import androidx.compose.ui.graphics.vector.ImageVector

data class HomeCardItem(
    val icon: ImageVector? = null,
    val label: String,
    val content: String? = null,
    val onClick: (() -> Unit)? = null
) {
}