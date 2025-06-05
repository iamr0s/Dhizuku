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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.content.edit
import androidx.core.text.HtmlCompat

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView

import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.page.settings.SettingsPage
import com.rosan.dhizuku.ui.theme.DhizukuTheme

import org.koin.core.component.KoinComponent

class SettingsActivity : ComponentActivity(), KoinComponent {
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
                ) != PackageManager.PERMISSION_GRANTED) {
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
        val preferences = LocalContext.current.getSharedPreferences("app", MODE_PRIVATE)
        var agreed by remember {
            mutableStateOf(preferences.getBoolean("agreement", false))
        }
        preferences.edit {
            putBoolean("agreement", agreed)
            commit()
        }
        if (agreed) return

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
                agreed = true
            }) {
                Text(text = stringResource(id = R.string.agree_text))
            }
        })
    }
}