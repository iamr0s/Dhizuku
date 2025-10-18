package com.rosan.dhizuku.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.asFlow
import com.rosan.dhizuku.ui.page.settings.SettingsPage
import com.rosan.dhizuku.ui.theme.DhizukuTheme
import org.koin.core.component.KoinComponent

class SettingsActivity : ComponentActivity(), KoinComponent {
    private val appPrefs by lazy {
        getSharedPreferences("app", MODE_PRIVATE)
    }

    private val agreementFlow by lazy {
        appPrefs.asFlow("agreement", false)
    }

    private lateinit var stringLauncher: ActivityResultLauncher<String>
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DhizukuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AgreementDialog()
                    SettingsPage(windowInsets = WindowInsets.safeDrawing)
                }
            }
        }

        stringLauncher = registerForActivityResult(
            RequestPermission()
        ) { _: Boolean -> }
        requestPostNotificationPermission()

        intentLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { _: ActivityResult -> }
        requestManageExternalStoragePermission()
    }

    fun requestPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                stringLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = "package:$packageName".toUri()
                    intentLauncher.launch(intent)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Composable
    private fun AgreementDialog() {
        val agreement by agreementFlow.collectAsState(false)
        if (agreement) return

        AlertDialog(onDismissRequest = { }, title = {
            Text(text = stringResource(id = R.string.agreement_title))
        }, text = {
            val textColor = AlertDialogDefaults.textContentColor.toArgb()
            AndroidView(factory = {
                TextView(it).apply {
                    setTextColor(textColor)
                    movementMethod = LinkMovementMethod.getInstance()
                    text = HtmlCompat.fromHtml(
                        context.getString(R.string.agreement_text),
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                }
            })
        }, dismissButton = {
            TextButton(onClick = {
                this@SettingsActivity.finish()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }, confirmButton = {
            TextButton(onClick = {
                appPrefs.edit(true) {
                    putBoolean("agreement", true)
                }
            }) {
                Text(text = stringResource(id = R.string.agree_text))
            }
        })
    }
}