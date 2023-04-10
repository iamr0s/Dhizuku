package com.rosan.dhizuku.ui.widget.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BaseWidget(
    icon: ImageVector? = null,
    title: String,
    description: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    foreContent: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically),
            imageVector = icon ?: materialIcon("") { materialPath {} },
            contentDescription = null,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
        ) {
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                description?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            foreContent()
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            content()
        }
    }
}