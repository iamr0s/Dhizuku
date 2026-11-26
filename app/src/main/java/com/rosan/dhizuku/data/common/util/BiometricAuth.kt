package com.rosan.dhizuku.data.common.util

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.util.Log

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuth {
    private const val TAG = "BiometricAuth"

    const val RESULT_AUTHENTICATED = 0

    const val RESULT_DENIED = 1

    const val RESULT_UNAVAILABLE = 2

    fun hasCredential(context: Context): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguard.isDeviceSecure
    }

    fun hasBiometric(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun canProtect(context: Context): Boolean = hasCredential(context) || hasBiometric(context)

    fun prompt(
        activity: FragmentActivity,
        onResult: (Int) -> Unit
    ) {
        if (!canProtect(activity)) {
            onResult(RESULT_UNAVAILABLE)
            return
        }
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(RESULT_AUTHENTICATED)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.w(TAG, "authentication error $errorCode: $errString")
                    onResult(classify(errorCode))
                }

                override fun onAuthenticationFailed() {
                }
            })
        runCatching { prompt.authenticate(buildInfo(activity)) }
            .onFailure {
                Log.e(TAG, "failed to start authentication", it)
                onResult(RESULT_UNAVAILABLE)
            }
    }

    private fun buildInfo(activity: FragmentActivity): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(com.rosan.dhizuku.R.string.biometric_title))
            .setSubtitle(activity.getString(com.rosan.dhizuku.R.string.biometric_subtitle))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        } else {
            builder.setDeviceCredentialAllowed(true)
        }
        return builder.build()
    }

    private fun classify(errorCode: Int): Int {
        return when (errorCode) {
            BiometricPrompt.ERROR_LOCKOUT -> RESULT_DENIED
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_TIMEOUT -> RESULT_DENIED
            else -> RESULT_UNAVAILABLE
        }
    }
}
