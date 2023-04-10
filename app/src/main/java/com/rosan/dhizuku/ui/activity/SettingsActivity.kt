package com.rosan.dhizuku.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.rosan.dhizuku.ui.page.settings.SettingsPage
import com.rosan.dhizuku.ui.theme.InstallerTheme
import org.koin.core.component.KoinComponent

class SettingsActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // A surface based on material design theme.
            InstallerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    SettingsPage()
                }
            }
        }
        if (!XXPermissions.isGranted(
                this,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        )
            XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request { permissions, allGranted -> }
    }
}