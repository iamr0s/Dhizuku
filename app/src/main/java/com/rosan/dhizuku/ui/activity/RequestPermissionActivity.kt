package com.rosan.dhizuku.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rosan.dhizuku.R
import com.rosan.dhizuku.aidl.IDhizukuRequestPermissionListener
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.shared.DhizukuVariables
import com.rosan.dhizuku.ui.theme.InstallerTheme
import com.rosan.dhizuku.ui.widget.dialog.DialogButton
import com.rosan.dhizuku.ui.widget.dialog.DialogButtons
import com.rosan.dhizuku.ui.widget.dialog.PositionDialog
import com.rosan.dhizuku.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RequestPermissionActivity : ComponentActivity(), KoinComponent {
    private val appRepo by inject<AppRepo>()

    private var grantResult: Int = PackageManager.PERMISSION_DENIED

    private var listener: IDhizukuRequestPermissionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.getBundleExtra("bundle")
        val clientUID = bundle?.getInt(DhizukuVariables.PARAM_CLIENT_UID)
        if (clientUID == null) {
            toast("please send client UID")
            finish()
            return
        }
        val packageName = packageManager.getPackagesForUid(clientUID)?.first()
        if (packageName == null) {
            toast("can not get package name for $clientUID")
            finish()
            return
        }
        val applicationInfo = packageManager.getPackageInfo(packageName, 0)?.applicationInfo
        if (applicationInfo == null) {
            toast("can not get application for $clientUID")
            finish()
            return
        }
        val label = applicationInfo.loadLabel(packageManager)
        val icon = applicationInfo.loadIcon(packageManager)
        bundle.getBinder(DhizukuVariables.PARAM_CLIENT_REQUEST_PERMISSION_BINDER)?.let {
            listener = IDhizukuRequestPermissionListener.Stub.asInterface(it)
        }
        fun updateAppEntity() {
            val allowApi = grantResult == PackageManager.PERMISSION_GRANTED
            CoroutineScope(Dispatchers.IO).launch {
                val entity = appRepo.findByUID(clientUID)
                if (entity == null)
                    appRepo.insert(AppEntity(uid = clientUID, allowApi = allowApi))
                else appRepo.update(entity.copy(allowApi = allowApi))
            }
        }
        setContent {
            // A surface based on material design theme.
            InstallerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    PositionDialog(onDismissRequest = {
                        finish()
                    }, centerIcon = {
                        Image(
                            painter = rememberDrawablePainter(drawable = icon),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }, centerTitle = {
                        val textSize = MaterialTheme.typography.titleLarge.fontSize.value
                        val textColor = AlertDialogDefaults.textContentColor.toArgb()
                        AndroidView(factory = {
                            val view = TextView(it)
                            view.movementMethod = LinkMovementMethod.getInstance()
                            view.setTextColor(textColor)
                            view.textSize = textSize
                            view.gravity = Gravity.CENTER
                            view.text = HtmlCompat.fromHtml(
                                it.getString(R.string.request_permission_text, label),
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                            return@AndroidView view
                        })
                    }, centerButton = {
                        DialogButtons(
                            listOf(
                                DialogButton(stringResource(R.string.agree)) {
                                    grantResult = PackageManager.PERMISSION_GRANTED
                                    updateAppEntity()
                                    finish()
                                },
                                DialogButton(stringResource(R.string.refuse)) {
                                    grantResult = PackageManager.PERMISSION_DENIED
                                    updateAppEntity()
                                    finish()
                                }
                            )
                        )
                    })
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
        listener?.onRequestPermission(grantResult)
    }
}