package com.rosan.dhizuku.ui.page.settings.account_manager.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rosan.dhizuku.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import com.rosan.dhizuku.ui.page.settings.account_manager.AccountManagerViewState
import com.rosan.dhizuku.ui.theme.AppIconCache

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    authenticator: AccountManagerViewState.Authenticator,
    isToggling: Boolean = false,
    onFreezeToggle: (packageName: String, isFrozen: Boolean) -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (authenticator.isFrozen) 0.6f else 1f,
        label = "alpha"
    )
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageBitmap = AppIconCache.rememberImageBitmapState(
                packageName = authenticator.auth.packageName,
                drawable = authenticator.auth.icon
            )
            Image(
                bitmap = imageBitmap.value,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = authenticator.auth.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    AnimatedVisibility(
                        visible = authenticator.isFrozen,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = authenticator.auth.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            TextButton(
                onClick = { onFreezeToggle(authenticator.auth.packageName, authenticator.isFrozen) },
                enabled = !isToggling,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = if (authenticator.isFrozen) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            ) {
                if (isToggling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = if (authenticator.isFrozen) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (authenticator.isFrozen) R.string.action_unfreeze
                            else R.string.action_freeze
                        )
                    )
                }
            }
        }
    }
}
