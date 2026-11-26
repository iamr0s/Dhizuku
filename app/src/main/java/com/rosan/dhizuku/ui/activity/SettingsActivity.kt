package com.rosan.dhizuku.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.widget.TextView

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity

import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.asFlow
import com.rosan.dhizuku.ui.page.settings.SettingsPage
import com.rosan.dhizuku.data.common.util.linkText
import com.rosan.dhizuku.ui.theme.DhizukuTheme
import com.rosan.dhizuku.data.common.util.BiometricAuth
import com.rosan.dhizuku.data.settings.repo.SettingsRepo

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsActivity : FragmentActivity(), KoinComponent {
    private val appPrefs by lazy {
        getSharedPreferences("app", MODE_PRIVATE)
    }

    private val agreementFlow by lazy {
        appPrefs.asFlow("agreement", false)
    }

    private val settingsRepo by inject<SettingsRepo>()

    private lateinit var stringLauncher: ActivityResultLauncher<String>

    private var unlocked by mutableStateOf(false)

    private var authenticating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DhizukuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (unlocked) {
                        AgreementDialog()
                        SettingsPage(windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout))
                    }
                }
            }
        }

        stringLauncher = registerForActivityResult(
            RequestPermission()
        ) { _: Boolean -> }
        requestUnlock()
        requestPostNotificationPermission()
        requestIgnoreBatteryOptimization()
    }

    private fun requestUnlock() {
        if (!settingsRepo.isBiometricEnabled) {
            unlocked = true
            return
        }
        if (!BiometricAuth.canProtect(this)) {
            unlocked = true
            return
        }
        authenticating = true
        BiometricAuth.prompt(this) { result ->
            authenticating = false
            if (result == BiometricAuth.RESULT_AUTHENTICATED) unlocked = true
            else finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (authenticating) return
        if (!unlocked) finish()
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

    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            try {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        ("package:$packageName").toUri()
                    )
                )
            } catch (_: ActivityNotFoundException) {
                // Some TV and embedded system images omit this optional settings activity.
                return
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
            val termsLabel = getString(R.string.agreement_terms)
            val privacyLabel = getString(R.string.agreement_privacy)
            var linked: CharSequence = getString(
                R.string.agreement_text, termsLabel, privacyLabel
            )
            linked = linkText(
                this@SettingsActivity, linked, termsLabel,
                "https://iamr0s.github.io/Dhizuku/TERMS.html"
            )
            linked = linkText(
                this@SettingsActivity, linked, privacyLabel,
                "https://iamr0s.github.io/Dhizuku/PRIVACY.html"
            )
            AndroidView(factory = {
                TextView(it).apply {
                    setTextColor(textColor)
                    movementMethod = LinkMovementMethod.getInstance()
                    text = linked
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