package com.rosan.dhizuku.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.rosan.dhizuku.data.settings.repo.SettingsRepo
import com.rosan.dhizuku.shared.DhizukuVariables
import com.rosan.dhizuku.ui.theme.DhizukuTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.cos
import kotlin.math.sin

class RequestPermissionActivity : ComponentActivity(), KoinComponent {
    companion object {
        const val UID_ERR = -1
        const val AUTO_DENY_SECONDS = 15
    }

    data class ViewState(
        val uid: Int = UID_ERR,
        val allowApi: Boolean = false,
        val listener: IDhizukuRequestPermissionListener? = null,
        val timeLeft: Int = AUTO_DENY_SECONDS,
        val timedOut: Boolean = false,
        val shouldShowDialog: Boolean = true
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val appRepo by inject<AppRepo>()
    private val settingsRepo by inject<SettingsRepo>()
    private var state by mutableStateOf(ViewState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!registerAppEntity(intent)) {
            finish()
            return
        }

        // Check if dhizuku is enabled and app is not blocked
        coroutineScope.launch {
            if (!settingsRepo.isDhizukuEnabled) {
                state = state.copy(allowApi = false, timedOut = true, shouldShowDialog = false)
                finish()
                return@launch
            }

            val entity = appRepo.findByUID(state.uid)

            if (settingsRepo.isWhitelistMode && (entity == null || !entity.allowApi)) {
                state = state.copy(allowApi = false, timedOut = true, shouldShowDialog = false)
                finish()
                return@launch
            }

            if (entity?.blocked == true) {
                state = state.copy(allowApi = false, timedOut = true, shouldShowDialog = false)
                finish()
                return@launch
            }
        }

        setContent {
            DhizukuTheme {
                if (state.shouldShowDialog) {
                    LaunchedEffect(Unit) {
                        repeat(AUTO_DENY_SECONDS) { second ->
                            delay(1000)
                            state = state.copy(timeLeft = AUTO_DENY_SECONDS - second - 1)
                        }
                        if (!state.timedOut) {
                            state = state.copy(allowApi = false, timedOut = true)
                            finish()
                        }
                    }
                }

                if (state.shouldShowDialog && !showDialog()) finish()
                if (!state.shouldShowDialog) finish()
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

            val entity = appRepo.findByUID(uid)
            if (entity == null)
                appRepo.insert(AppEntity(uid = uid, signature = signature, allowApi = allowApi))
            else
                appRepo.update(entity.copy(uid = uid, signature = signature, allowApi = allowApi))
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
    private fun CountdownPieChart(
        progress: Float,
        modifier: Modifier = Modifier
    ) {
        val color = MaterialTheme.colorScheme.outline

        Canvas(modifier = modifier) {
            val radius = size.minDimension / 2f
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 2.dp.toPx() // Made thicker
            val lineRadius = radius - strokeWidth / 2f

            // Draw the remaining arc (hollow circle outline that gets eaten away counter-clockwise)
            if (progress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f, // Start from top (12 o'clock)
                    sweepAngle = 360f * progress, // Sweep clockwise for remaining time
                    useCenter = false, // This makes it hollow!
                    topLeft = androidx.compose.ui.geometry.Offset(
                        center.x - lineRadius,
                        center.y - lineRadius
                    ),
                    size = androidx.compose.ui.geometry.Size(lineRadius * 2, lineRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Always draw a line from center to top (12 o'clock position)
            drawLine(
                color = color,
                start = center,
                end = androidx.compose.ui.geometry.Offset(center.x, center.y - lineRadius),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Draw current time position line (moves clockwise as arc gets eaten counter-clockwise)
            if (progress > 0f && progress < 1f) {
                val angle = -90f + (360f * progress)
                val angleRad = Math.toRadians(angle.toDouble())
                val endX = center.x + lineRadius * cos(angleRad).toFloat()
                val endY = center.y + lineRadius * sin(angleRad).toFloat()

                drawLine(
                    color = color,
                    start = center,
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
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

        val progress by animateFloatAsState(
            targetValue = state.timeLeft.toFloat() / AUTO_DENY_SECONDS,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
            label = "countdown_progress"
        )

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
        }, text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CountdownPieChart(
                        progress = progress,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (state.timeLeft > 0) {
                            stringResource(R.string.auto_deny_in_seconds, state.timeLeft)
                        } else {
                            stringResource(R.string.denying_access)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }, confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                @Composable
                fun MyTextButton(
                    onClick: () -> Unit,
                    @StringRes textResId: Int,
                    isPrimary: Boolean = false
                ) {
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(16.dp),
                        colors = if (isPrimary) ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            stringResource(textResId),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                MyTextButton(onClick = {
                    state = state.copy(allowApi = true, timedOut = true)
                    finish()
                }, textResId = R.string.agree, isPrimary = true)
                MyTextButton(onClick = {
                    state = state.copy(allowApi = false, timedOut = true)
                    finish()
                }, textResId = R.string.refuse)
            }
        })
        return true
    }
}