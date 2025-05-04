package com.rosan.dhizuku.ui.activity

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import com.rosan.dhizuku.ui.page.settings.SettingsPage
import com.rosan.dhizuku.ui.theme.DhizukuTheme

import org.koin.core.component.KoinComponent

class SettingsActivity : ComponentActivity(), KoinComponent {
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

        requestNotificationPermissions()
    }

    private fun requestNotificationPermissions() {
    }
}