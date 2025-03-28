package com.rosan.dhizuku.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.google.accompanist.drawablepainter.rememberDrawablePainter

import com.rosan.dhizuku.R
import com.rosan.dhizuku.aidl.IDhizukuRequestPermissionListener
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.shared.DhizukuVariables
import com.rosan.dhizuku.ui.theme.DhizukuTheme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RequestPermissionActivity : ComponentActivity(), KoinComponent {
    companion object {
        const val UID_ERR = -1
    }

    data class ViewState(
        val uid: Int = UID_ERR,
        val allowApi: Boolean = false,
        val listener: IDhizukuRequestPermissionListener? = null
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val repo by inject<AppRepo>()

    private var state by mutableStateOf(ViewState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!registerAppEntity(intent)) {
            finish()
            return
        }
        setContent {
            DhizukuTheme {
                if (!showDialog()) finish()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (registerAppEntity(intent)) return
        finish()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun finish() {
        super.finish()

        coroutineScope.launch {
            val uid = state.uid
            val allowApi = state.allowApi
            val packageInfo = packageManager.getPackageInfoForUid(uid) ?: return@launch
            val signature = packageInfo.signature ?: return@launch

            val entity = repo.findByUID(uid)
            if (entity == null)
                repo.insert(AppEntity(uid = uid, signature = signature, allowApi = allowApi))
            else repo.update(entity.copy(uid = uid, signature = signature, allowApi = allowApi))
        }.invokeOnCompletion {
            val result = if (state.allowApi) PackageManager.PERMISSION_GRANTED
            else PackageManager.PERMISSION_DENIED
            state.listener?.onRequestPermission(result)
        }
    }

    private fun registerAppEntity(intent: Intent?): Boolean {
        if (intent == null) return false
        val bundle = listOfNotNull(
            intent.extras,
            intent.getBundleExtra("bundle")
        ).find { it.containsKey(DhizukuVariables.PARAM_CLIENT_UID) }
            ?: return false

        // In the future, it will be replaced by the following method
//        val bundle = intent.extras ?: return false

        val uid = bundle.getInt(DhizukuVariables.PARAM_CLIENT_UID, -1)
        if (uid == -1) return false

        val binder = bundle.getBinder(DhizukuVariables.PARAM_CLIENT_REQUEST_PERMISSION_BINDER)
            ?: return false

        val listener = kotlin.runCatching {
            IDhizukuRequestPermissionListener.Stub.asInterface(binder)
        }.getOrElse {
            it.printStackTrace()
            return false
        }

        state = state.copy(
            uid = uid,
            listener = listener
        )
        return true
    }

    @Composable
    private fun showDialog(): Boolean {
        val uid = state.uid
        val packageInfo = packageManager.getPackageInfoForUid(uid) ?: return false
        val packageName = packageInfo.packageName
        val applicationInfo = packageInfo.applicationInfo
        val icon = applicationInfo?.loadIcon(packageManager)
            ?: packageManager.defaultActivityIcon
        val label = applicationInfo?.loadLabel(packageManager)
            ?: packageName

        AlertDialog(onDismissRequest = {
            finish()
        }, icon = {
            Image(
                painter = rememberDrawablePainter(drawable = icon),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }, title = {
            Text(
                AnnotatedString.fromHtml(stringResource(R.string.request_permission_text, label)),
                textAlign = TextAlign.Center
            )
        }, confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                @Composable
                fun MyTextButton(onClick: () -> Unit, @StringRes textResId: Int) {
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(stringResource(textResId))
                    }
                }
                MyTextButton(onClick = {
                    state = state.copy(allowApi = true)
                    finish()
                }, textResId = R.string.agree)
                MyTextButton(onClick = {
                    state = state.copy(allowApi = false)
                    finish()
                }, textResId = R.string.refuse)
            }
        })
        return true
    }
}