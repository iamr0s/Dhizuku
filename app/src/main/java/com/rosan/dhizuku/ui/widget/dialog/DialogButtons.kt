package com.rosan.dhizuku.ui.widget.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DialogButtons(
    buttons: List<DialogButton>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        buttons.forEachIndexed { index, button ->
            val specialCornerSize = CornerSize(12.dp)
            val shape = RoundedCornerShape(4.dp).let {
                if (index != 0) it
                else it.copy(topStart = specialCornerSize, topEnd = specialCornerSize)
            }.let {
                if (index + 1 != buttons.size) it
                else it.copy(bottomStart = specialCornerSize, bottomEnd = specialCornerSize)
            }
            TextButton(
                button.onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(button.text)
            }
        }
    }
}