package com.rosan.dhizuku.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

import com.rosan.dhizuku.ui.page.settings.SettingsPage
import com.rosan.dhizuku.ui.theme.DhizukuTheme

import org.koin.core.component.KoinComponent
import androidx.core.net.toUri

class SettingsActivity : ComponentActivity(), KoinComponent {
    private lateinit var stringLauncher: ActivityResultLauncher<String>
    private lateinit var intentLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DhizukuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
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
}