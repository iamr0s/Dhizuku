package com.rosan.dhizuku.ui.page.settings.home

import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.Context

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Adb
import androidx.compose.material.icons.twotone.ArrowDropDown
import androidx.compose.material.icons.twotone.AttachMoney
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Code
import androidx.compose.material.icons.twotone.DoNotDisturbOn
import androidx.compose.material.icons.twotone.RoomPreferences
import androidx.compose.material.icons.twotone.SentimentVeryDissatisfied
import androidx.compose.material.icons.twotone.SentimentVerySatisfied
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.SwapHorizontalCircle
import androidx.compose.material.icons.twotone.Terminal
import androidx.compose.material.icons.twotone.Warning
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
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
import com.rosan.dhizuku.data.common.util.checkShizukuWorked
import com.rosan.dhizuku.data.common.util.openUrlInBrowser
import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import com.rosan.dhizuku.server.DhizukuState
import com.rosan.dhizuku.ui.page.settings.SettingsRoute
import com.rosan.dhizuku.ui.theme.exclude

import kotlin.system.exitProcess

import kotlinx.coroutines.launch

import org.koin.compose.koinInject

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
                    TopBarActions()
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
            if (dhizukuState.isOwner) item("settings") {
                SettingsWidget(navController)
            }
            if (dhizukuState.isOwner) item("app_management") {
                AppManagementWidget(navController)
            }
            if (dhizukuState.isOwner) item("dhizuku") {
                DhizukuWidget(navController)
            }
            if (!dhizukuState.isOwner) item("shizuku") {
                ShizukuWidget(navController)
            }
            if (!dhizukuState.isOwner) item("adb") {
                AdbWidget()
            }
            if (dhizukuState.isOwner) item("home_deactivate_title") {
                DeactivateWidget()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarActions() {
    val context = LocalContext.current
    val settingsRepo = koinInject<SettingsRepo>()

    var shutdownDialogShow by remember { mutableStateOf(false) }
    var donateMenuExpanded by remember { mutableStateOf(false) }
    var donateHideConfirmShow by remember { mutableStateOf(false) }

    val donateButtonHidden by settingsRepo.flowDonateButtonHidden()
        .collectAsState(initial = settingsRepo.isDonateButtonHidden)

    if (!donateButtonHidden) {
        Box {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
                tooltip = { PlainTooltip { Text(stringResource(R.string.donate)) } },
                state = rememberTooltipState()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = {
                                context.openUrlInBrowser("https://github.com/iamr0s/Dhizuku/blob/main/docs/DONATE.md")
                            },
                            onLongClick = { donateMenuExpanded = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.TwoTone.AttachMoney, contentDescription = stringResource(R.string.donate))
                }
            }
            DropdownMenu(
                expanded = donateMenuExpanded,
                onDismissRequest = { donateMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.donate_hide_action)) },
                    onClick = {
                        donateMenuExpanded = false
                        donateHideConfirmShow = true
                    }
                )
            }
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(stringResource(R.string.home_shutdown_title)) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = { shutdownDialogShow = true }) {
            Icon(Icons.TwoTone.Cancel, contentDescription = stringResource(R.string.home_shutdown_title))
        }
    }

    if (shutdownDialogShow) {
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

    if (donateHideConfirmShow) {
        AlertDialog(onDismissRequest = {
            donateHideConfirmShow = false
        }, confirmButton = {
            TextButton(onClick = {
                donateHideConfirmShow = false
            }) {
                Text(stringResource(R.string.cancel))
            }
            TextButton(onClick = {
                settingsRepo.isDonateButtonHidden = true
                donateHideConfirmShow = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        }, title = {
            Text(stringResource(R.string.donate_hide_confirm_title))
        }, text = {
            Text(stringResource(R.string.donate_hide_confirm_dsp))
        })
    }
}

@Composable
private fun LazyItemScope.DhizukuStateWidget() {
    val isOwner = DhizukuState.state.isOwner

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
    val context = LocalContext.current
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
            Text(
                if (it) stringResource(
                    R.string.home_status_owner_granted,
                    stringResource(
                        if (DhizukuState.state.isProfileOwner) R.string.confirm_profile_owner
                        else R.string.confirm_device_owner
                    )
                )
                else stringResource(R.string.home_status_owner_denied)
            )
        }
    }, onClick = { DhizukuState.sync(context )})
}

@Composable
private fun LazyItemScope.SettingsWidget(navController: NavController) {
    CardWidget(onClick = {
        navController.navigate(SettingsRoute.Settings.route)
    }, icon = {
        Icon(imageVector = Icons.TwoTone.Settings, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.settings))
    }, text = {
        Text(stringResource(R.string.settings_desc))
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
        if (checkShizukuWorked()) {
            navController.navigate(SettingsRoute.Activate.route(SettingsRoute.Activate.Mode.Shizuku))
        } else {
            return@CardWidget
        }
    }, icon = {
        Icon(imageVector = Icons.TwoTone.Terminal, contentDescription = null)
    }, title = {
        Text(stringResource(R.string.home_shizuku_title))
    }, text = {
        HtmlText(stringResource(R.string.home_shizuku_dsp, "https://shizuku.rikka.app/"))
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LazyItemScope.AdbWidget() {
    val deviceOwnerCommand =
        "adb shell dpm set-device-owner ${DhizukuState.admin.flattenToShortString()}"
    val profileOwnerCommand =
        "adb shell dpm set-profile-owner ${DhizukuState.admin.flattenToShortString()}"

    var commandDialogShow by remember { mutableStateOf(false) }
    var useProfileOwner by remember { mutableStateOf(false) }
    var ownerMenuExpanded by remember { mutableStateOf(false) }
    var profileOwnerWarningShow by remember { mutableStateOf(false) }

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
        TextButton(onClick = { commandDialogShow = true }) {
            Icon(imageVector = Icons.TwoTone.Code, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.home_adb_btn_view_command))
        }
    })

    if (profileOwnerWarningShow) {
        AlertDialog(onDismissRequest = {
            profileOwnerWarningShow = false
        }, icon = {
            Icon(imageVector = Icons.TwoTone.Warning, contentDescription = null)
        }, confirmButton = {
            TextButton(onClick = {
                useProfileOwner = true
                profileOwnerWarningShow = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        }, dismissButton = {
            TextButton(onClick = { profileOwnerWarningShow = false }) {
                Text(stringResource(R.string.cancel))
            }
        }, title = {
            Text(stringResource(R.string.profile_owner_warning_title))
        }, text = {
            Text(stringResource(R.string.profile_owner_warning_dsp))
        })
    }

    if (!commandDialogShow) return
    val command = if (useProfileOwner) profileOwnerCommand else deviceOwnerCommand
    AlertDialog(onDismissRequest = {
        commandDialogShow = false
    }, confirmButton = {
        TextButton(onClick = {
            commandDialogShow = false
        }) {
            Text(stringResource(R.string.cancel))
        }
        val manager = LocalClipboard.current
        val scope = rememberCoroutineScope()
        TextButton(onClick = {
            scope.launch {
                manager.setClipEntry(ClipEntry(ClipData.newPlainText("command", command)))
                commandDialogShow = false
            }
        }) {
            Text(stringResource(R.string.copy))
        }
    }, title = {
        Text(stringResource(R.string.home_adb_btn_view_command))
    }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                OutlinedButton(onClick = { ownerMenuExpanded = true }) {
                    Text(
                        stringResource(
                            if (useProfileOwner) R.string.confirm_profile_owner
                            else R.string.confirm_device_owner
                        )
                    )
                    Icon(imageVector = Icons.TwoTone.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = ownerMenuExpanded,
                    onDismissRequest = { ownerMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.confirm_device_owner)) },
                        onClick = {
                            useProfileOwner = false
                            ownerMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.confirm_profile_owner)) },
                        onClick = {
                            ownerMenuExpanded = false
                            profileOwnerWarningShow = true
                        }
                    )
                }
            }
            SelectionContainer {
                Text(command)
            }
        }
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
            try {
                @Suppress("DEPRECATION")
                manager.clearProfileOwner(DhizukuState.admin)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            try {
                @Suppress("DEPRECATION")
                manager.clearDeviceOwnerApp(context.packageName)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
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