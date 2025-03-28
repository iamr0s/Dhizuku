package com.rosan.dhizuku.ui.theme

import android.app.admin.DeviceAdminInfo
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap

import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import kotlin.coroutines.CoroutineContext

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AppIconCache : CoroutineScope, KoinComponent {
    private val context by inject<Context>()

    private val defaultImageBitMap by lazy {
        ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!
            .toBitmap()
            .asImageBitmap()
    }

    private val lruCache by lazy {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024
        val availableCacheSize = (maxMemory / 4).toInt()
        object : LruCache<String, Bitmap>(availableCacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount / 4
            }
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private fun id(applicationInfo: ApplicationInfo) =
        Triple(applicationInfo.packageName, "", applicationInfo.uid)

    private fun id(activityInfo: ActivityInfo) =
        Triple(activityInfo.packageName, activityInfo.name, activityInfo.applicationInfo.uid)

    @Composable
    private fun rememberImageBitmapState(key: String, action: () -> Bitmap?) =
        remember(key) {
            mutableStateOf(defaultImageBitMap).also {
                launch {
                    val bitmap = lruCache[key]
                        ?: action.invoke()
                        ?: return@launch
//                ?: applicationInfo.loadIcon(context.packageManager)?.toBitmap()
//                ?: return@launch
                    lruCache.put(key, bitmap)
                    it.value = bitmap.asImageBitmap()
                }
            }
        }

    @Composable
    fun rememberImageBitmapState(applicationInfo: ApplicationInfo): State<ImageBitmap> =
        rememberImageBitmapState("${applicationInfo.packageName}_${applicationInfo.uid}") {
            applicationInfo.loadIcon(context.packageManager)?.toBitmap()
        }

    @Composable
    fun rememberImageBitmapState(admin: DeviceAdminInfo): State<ImageBitmap> =
        rememberImageBitmapState("${admin.component.flattenToShortString()}_${admin.activityInfo.applicationInfo.uid}") {
            admin.loadIcon(context.packageManager)?.toBitmap()
        }
}