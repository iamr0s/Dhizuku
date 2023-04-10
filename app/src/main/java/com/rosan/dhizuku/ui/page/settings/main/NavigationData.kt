package com.rosan.dhizuku.ui.page.settings.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationData(
    val icon: ImageVector,
    val label: String,
    val content: @Composable () -> Unit
)
