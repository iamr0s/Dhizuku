package com.rosan.dhizuku.ui.page.settings.home

import android.app.admin.DevicePolicyManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Adb
import androidx.compose.material.icons.twotone.AttachMoney
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Code
import androidx.compose.material.icons.twotone.DoNotDisturbOn
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material.icons.twotone.RoomPreferences
import androidx.compose.material.icons.twotone.SentimentVeryDissatisfied
import androidx.compose.material.icons.twotone.SentimentVerySatisfied
import androidx.compose.material.icons.twotone.SwapHorizontalCircle
import androidx.compose.material.icons.twotone.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.openUrlInBrowser
import com.rosan.dhizuku.server.DhizukuDAReceiver
import com.rosan.dhizuku.server.DhizukuState
import com.rosan.dhizuku.ui.page.settings.SettingsRoute
import com.rosan.dhizuku.ui.theme.exclude
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    windowInsets: WindowInsets,
    navController: NavController
) {
    val dhizukuState = DhizukuState.state
    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(windowInsets.exclude(WindowInsetsSides.Bottom))
            .fillMaxSize(),
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    OverflowMenu()
                }
            )
        }) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item("dhizuku_state") {
                DhizukuStateWidget()
            }
            if (dhizukuState.owner) item("app_management") {
                AppManagementWidget(navController)
            }
            item("dhizuku") {
                DhizukuWidget(navController)
            }
            item("shizuku") {
                ShizukuWidget(navController)
            }
            item("adb") {
                AdbWidget()
            }
            if (dhizukuState.owner) item("home_deactivate_title") {
                DeactivateWidget()
            }
        }
    }
}

@Composable
private fun OverflowMenu() {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var shutdownDialogShow by remember { mutableStateOf(false) }

    IconButton(onClick = { menuExpanded = true }) {
        Icon(Icons.TwoTone.MoreVert, null)
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }) {
        DropdownMenuItem(text = {
            Text(stringResource(R.string.home_shutdown_title))
        }, leadingIcon = {
            Icon(Icons.TwoTone.Cancel, null)
        }, onClick = {
            menuExpanded = false
            shutdownDialogShow = true
        })

        R.string.wechat
        R.string.alipay
        R.string.binance
        DropdownMenuItem(text = {
            Text(stringResource(R.string.donate))
        }, leadingIcon = {
            Icon(Icons.TwoTone.AttachMoney, null)
        }, onClick = {
            menuExpanded = false
            context.openUrlInBrowser("https://github.com/iamr0s/Dhizuku")
        })
    }

    if (!shutdownDialogShow) return
    AlertDialog(onDismissRequest = {
        shutdownDialogShow = false
    }, confirmButton = {
        TextButton(onClick = {
            shutdownDialogShow = false
        }) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(onClick = {
            exitProcess(0)
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, title = {
        Text(stringResource(R.string.home_shutdown_title))
    }, text = {
        Text(stringResource(R.string.home_shutdown_dsp))
    })
}

@Composable
private fun LazyItemScope.DhizukuStateWidget() {
    val isOwner = DhizukuState.state.owner

    @Suppress("AnimateAsStateLabel")
    val iconContainerColor by animateColorAsState(
        if (isOwner) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.errorContainer
    )

    @Suppress("AnimateAsStateLabel")
    val iconColor by animateColorAsState(
        if (isOwner) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onErrorContainer
    )
    CardWidget(colors = CardDefaults.cardColors(
        containerColor = iconContainerColor
    ), icon = {
        Icon(
            imageVector = Icons.TwoTone.run {
                if (isOwner) SentimentVerySatisfied
                else SentimentVeryDissatisfied
            },
            contentDescription = null
        )
    }, iconColors = IconButtonDefaults.iconButtonColors(
        containerColor = iconContainerColor,
        contentColor = iconColor
    ), title = {
        @Suppress("AnimatedContentLabel")
        AnimatedContent(targetState = isOwner) {
            Text(stringResource(if (it) R.string.home_status_owner_granted else R.string.home_status_owner_denied))
        }
    })
}

@Composable
private fun LazyItemScope.AppManagementWidget(navController: NavController) {
    CardWidget(onClick = {
        navController.navigate(SettingsRoute.AppManagement.route)
    }, icon = {
        Icon(imageVector = Icons.TwoTone.RoomPreferences, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.home_app_management_title))
    }, text = {
        Text(stringResource(R.string.home_app_management_dsp))
    })
}

@Composable
private fun LazyItemScope.DhizukuWidget(navController: NavController) {
    CardWidget(onClick = {
        navController.navigate(SettingsRoute.Activate.route(SettingsRoute.Activate.Mode.Dhizuku))
    }, icon = {
        Icon(imageVector = Icons.TwoTone.SwapHorizontalCircle, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.home_dhizuku_title))
    }, text = {
        HtmlText(stringResource(R.string.home_dhizuku_dsp, "https://github.com/iamr0s/Dhizuku"))
    })
}

@Composable
private fun LazyItemScope.ShizukuWidget(navController: NavController) {
    CardWidget(onClick = {
        navController.navigate(SettingsRoute.Activate.route(SettingsRoute.Activate.Mode.Shizuku))
    }, icon = {
        Icon(imageVector = Icons.TwoTone.Terminal, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.home_shizuku_title))
    }, text = {
        HtmlText(stringResource(R.string.home_shizuku_dsp, "https://shizuku.rikka.app/"))
    })
}

@Composable
private fun LazyItemScope.AdbWidget() {
    val command = "adb shell dpm set-device-owner ${DhizukuDAReceiver.name.flattenToShortString()}"
    var state by remember {
        mutableStateOf(false)
    }
    CardWidget(icon = {
        Icon(imageVector = Icons.TwoTone.Adb, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.home_adb_title))
    }, content = {
        HtmlText(
            stringResource(
                R.string.home_adb_dsp,
                "https://developer.android.com/tools/adb"
            )
        )
        TextButton(onClick = { state = true }) {
            Icon(imageVector = Icons.TwoTone.Code, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.home_adb_btn_view_command))
        }
    })
    if (!state) return
    AlertDialog(onDismissRequest = {
        state = false
    }, confirmButton = {
        TextButton(onClick = {
            state = false
        }) {
            Text(stringResource(R.string.cancel))
        }
        val manager = LocalClipboardManager.current
        TextButton(onClick = {
            manager.setText(AnnotatedString(command))
            state = false
        }) {
            Text(stringResource(R.string.copy))
        }
    }, title = {
        Text(stringResource(R.string.home_adb_btn_view_command))
    }, text = {
        Text(command)
    })
}

@Composable
private fun LazyItemScope.DeactivateWidget() {
    val context = LocalContext.current
    val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    var state by remember {
        mutableStateOf(false)
    }
    CardWidget(onClick = {
        state = true
    }, icon = {
        Icon(imageVector = Icons.TwoTone.DoNotDisturbOn, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.home_deactivate_title))
    })
    if (!state) return
    AlertDialog(onDismissRequest = {
        state = false
    }, confirmButton = {
        TextButton(onClick = {
            state = false
        }) {
            Text(stringResource(R.string.cancel))
        }
        TextButton(onClick = {
            @Suppress("DEPRECATION")
            manager.clearDeviceOwnerApp(context.packageName)
            state = false
        }) {
            Text(stringResource(R.string.confirm))
        }
    }, title = {
        Text(stringResource(R.string.home_deactivate_title))
    }, text = {
        Text(stringResource(R.string.home_deactivate_dsp))
    })
}

@Composable
private fun HtmlText(text: String) {
    Text(
        AnnotatedString.fromHtml(
            text,
            TextLinkStyles(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )
        )
    )
}

@Composable
private fun LazyItemScope.CardWidget(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit,
    iconColors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    title: @Composable () -> Unit,
    text: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    ElevatedCard(
        modifier = modifier.animateItem(),
        colors = colors, onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(40.dp),
                    color = iconColors.containerColor,
                    contentColor = iconColors.contentColor
                ) {
                    Box(Modifier.padding(8.dp)) {
                        icon.invoke()
                    }
                }
                Column {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        title.invoke()
                    }
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        @Suppress("AnimatedContentLabel")
                        AnimatedContent(targetState = text) {
                            it?.invoke()
                        }
                    }
                }
            }
            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                content?.invoke()
            }
        }
    }
}
