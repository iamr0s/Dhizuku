package com.rosan.dhizuku.ui.page.settings.home

import android.app.admin.DevicePolicyManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.rosan.dhizuku.App
import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.console.model.entity.ConsoleError
import com.rosan.dhizuku.data.console.repo.ConsoleRepo
import com.rosan.dhizuku.data.console.util.ConsoleRepoUtil
import com.rosan.dhizuku.data.console.util.ConsoleUtil
import com.rosan.dhizuku.server.DhizukuDAReceiver
import com.rosan.dhizuku.ui.activity.RequestPermissionActivity
import com.rosan.dhizuku.ui.widget.dialog.PositionDialog
import com.rosan.dhizuku.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.home))
            },
        )
    }) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                StatusWidget()
            }
            item {
                ActiveWidget()
            }
            item {
                DonateWidget()
            }
        }
    }
}

@Composable
fun StatusWidget() {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val isDeviceAdminer = app.isDeviceAdminer
    val isDeviceOwner = app.isDeviceOwner

    val containerColor = if (isDeviceOwner) MaterialTheme.colorScheme.primaryContainer
    else if (isDeviceAdminer) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.errorContainer

    val onContainerColor = if (isDeviceOwner) MaterialTheme.colorScheme.onPrimaryContainer
    else if (isDeviceAdminer) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

    val icon = if (isDeviceOwner) Icons.TwoTone.SentimentVerySatisfied
    else if (isDeviceAdminer) Icons.TwoTone.SentimentNeutral
    else Icons.TwoTone.SentimentVeryDissatisfied

    val text = stringResource(
        id = if (isDeviceOwner) R.string.device_owner_granted
        else if (isDeviceAdminer) R.string.device_admin_granted
        else R.string.device_admin_denied
    )

    CardWidget(colors = CardDefaults.elevatedCardColors(
        containerColor = containerColor, contentColor = onContainerColor
    ), content = {
        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
            Icon(
                modifier = Modifier.align(Alignment.CenterVertically),
                imageVector = icon,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(24.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }, onClick = {
        context.startActivity(
            Intent(context, RequestPermissionActivity::class.java).putExtra(
                "bundle",
                Bundle().apply {
                    putInt("uid", 10330)
                }).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    })
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShizukuButton() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var console: ConsoleRepo? = null
    var showDialog by remember {
        mutableStateOf(false)
    }
    var inProgress by remember {
        mutableStateOf(false)
    }
    var exception: Throwable? by remember {
        mutableStateOf(null)
    }

    Button(onClick = {
        scope.launch(Dispatchers.IO) {
            inProgress = true
            exception = null
            showDialog = true
            exception = kotlin.runCatching {
                val letConsole = ConsoleRepoUtil.shizuku {
                    command(
                        "dpm",
                        "set-device-owner",
                        "${context.packageName}/${DhizukuDAReceiver::class.qualifiedName}"
                    )
                }
                console = letConsole
                val util = ConsoleUtil(letConsole)
                val inputJob = async { util.inputBytes() }
                val errorJob = async { util.errorBytes() }
                val input = inputJob.await().decodeToString()
                val error = errorJob.await().decodeToString()
                val code = letConsole.exitValue()
                if (code != 0) throw ConsoleError(code, input, error)
            }.exceptionOrNull()
            inProgress = false
        }
    }) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.TwoTone.Key,
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(id = R.string.active_by_shizuku))
    }

    if (!showDialog) return
    PositionDialog(onDismissRequest = {}, centerTitle = {
        Text(stringResource(R.string.active_by_shizuku))
    }, leftText = {
        AnimatedContent(targetState = inProgress) {
            if (it) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else if (exception == null) Text(stringResource(R.string.execution_end))
            else {
                val text = when {
                    exception is IllegalStateException && exception?.message == "binder haven't been received" -> stringResource(
                        R.string.shizuku_binder_not_received
                    )
                    else -> ByteArrayOutputStream().also {
                        exception?.printStackTrace(PrintStream(it))
                    }.toByteArray().decodeToString()
                }
                SelectionContainer {
                    LazyColumn {
                        item {
                            Text(text)
                        }
                    }
                }
            }
        }
    }, rightButton = {
        TextButton(onClick = {
            console?.closeForcibly()
            showDialog = false
        }) {
            Text(
                stringResource(
                    if (inProgress) R.string.cancel
                    else R.string.finish
                )
            )
        }
    })
}

@Composable
fun ADBButton() {
    val context = LocalContext.current
    var showDialog by remember {
        mutableStateOf(false)
    }
    val command =
        "adb shell dpm set-device-owner ${context.packageName}/${DhizukuDAReceiver::class.qualifiedName}"

    Button(onClick = {
        showDialog = true
    }) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.TwoTone.Adb,
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(id = R.string.active_by_adb))
    }
    if (showDialog) {
        AlertDialog(onDismissRequest = {
            showDialog = false
        }, title = {
            Text(text = stringResource(id = R.string.active_by_adb))
        }, text = {
            val textColor = AlertDialogDefaults.textContentColor.toArgb()
            AndroidView(factory = {
                val view = TextView(it)
                view.movementMethod = LinkMovementMethod.getInstance()
                view.setTextColor(textColor)
                view.text = HtmlCompat.fromHtml(
                    it.getString(R.string.active_by_adb_dsp, command),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                return@AndroidView view
            })
        }, confirmButton = {
            TextButton(onClick = {
                val manager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                manager.setPrimaryClip(ClipData.newPlainText("Label", command))
                context.toast(R.string.copy_success)
                showDialog = false
            }) {
                Text(text = stringResource(id = R.string.copy))
            }
        }, dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        })
    }
}

@Composable
fun DeactivateButton() {
    val context = LocalContext.current
    Button(onClick = {
        val e = runCatching {
            val manager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            manager.clearDeviceOwnerApp(context.packageName)
        }.exceptionOrNull()
        context.toast(if (e == null) R.string.deactivate_success else R.string.deactivate_failed)
    }) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.TwoTone.Outlet,
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(id = R.string.deactivate))
    }
}

@Composable
fun ForceStopButton() {
    val context = LocalContext.current
    var showDialog by remember {
        mutableStateOf(false)
    }

    Button(onClick = {
        showDialog = true
    }) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.TwoTone.Close,
            contentDescription = null
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(text = stringResource(id = R.string.force_stop))
    }
    if (!showDialog) return
    AlertDialog(onDismissRequest = {
        showDialog = false
    }, title = {
        Text(text = stringResource(id = R.string.force_stop))
    }, text = {
        Text(stringResource(R.string.force_stop_dsp))
    }, confirmButton = {
        TextButton(onClick = {
            exitProcess(0)
        }) {
            Text(text = stringResource(id = R.string.confirm))
        }
    }, dismissButton = {
        TextButton(onClick = { showDialog = false }) {
            Text(text = stringResource(id = R.string.cancel))
        }
    })
}

@Composable
fun ActiveWidget() {
    CardWidget(title = {
        Text(text = stringResource(id = R.string.active))
    }, text = {
        Text(text = stringResource(id = R.string.active_func_dsp))
    }, buttons = {
        ShizukuButton()
        ADBButton()
        DeactivateButton()
        ForceStopButton()
    })
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun DonateWidget() {
    val context = LocalContext.current
    ItemsCardWidget(
        title = {
            Text(text = stringResource(id = R.string.donate))
        }, items = listOf(
            HomeCardItem(label = stringResource(id = R.string.alipay), onClick = {
                openUrl(context, "https://qr.alipay.com/fkx18580lfpydiop04dze47")
            }),
            HomeCardItem(label = stringResource(id = R.string.wechat), onClick = {
                openUrl(context, "https://missuo.ru/file/fee5df1381671c996b127.png")
            }),
            HomeCardItem(label = stringResource(id = R.string.binance), onClick = {
                openUrl(context, "https://missuo.ru/file/28368c28d4ff28d59ed4b.jpg")
            }),
        )
    )
}

@Composable
fun ItemsCardWidget(
    colors: CardColors = CardDefaults.elevatedCardColors(),
    onClick: (() -> Unit)? = null,
    showItemIcon: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    items: List<HomeCardItem>,
    buttons: (@Composable () -> Unit)? = null
) {
    CardWidget(
        colors = colors, onClick = onClick, icon = icon, title = title, content = {
            @Composable
            fun ItemWidget(item: HomeCardItem) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = item.onClick != null, onClick = item.onClick ?: {})
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (showItemIcon) {
                        if (item.icon != null) {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                        } else {
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = item.label, style = MaterialTheme.typography.bodyLarge)
                        if (item.content != null) {
                            Text(text = item.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Column {
                items.forEach {
                    ItemWidget(it)
                }
            }
        }, buttons = buttons
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardWidget(
    colors: CardColors = CardDefaults.elevatedCardColors(),
    onClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    buttons: (@Composable () -> Unit)? = null
) {
    ElevatedCard(
        colors = colors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onClick != null, onClick = onClick ?: {})
                .padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null || title != null) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    if (icon != null) {
                        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                            icon()
                        }
                    }
                    if (icon != null && title != null) {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                    if (title != null) {
                        ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                title()
                            }
                        }
                    }
                }
            }
            if (content != null || text != null) {
                if (content != null) Box {
                    content()
                }
                else Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                        text?.invoke()
                    }
                }
            }
            if (buttons != null) {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttons()
                }
            }
        }
    }
}