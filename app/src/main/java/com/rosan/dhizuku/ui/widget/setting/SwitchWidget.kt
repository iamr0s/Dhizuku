package com.rosan.dhizuku.ui.widget.setting

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SwitchWidget(
    icon: ImageVector? = null,
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BaseWidget(
        icon = icon,
        title = title,
        description = description,
        enabled = enabled,
        onClick = {
            onCheckedChange(!checked)
        }
    ) {
        Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = { onCheckedChange(!checked) }
        )
    }
}