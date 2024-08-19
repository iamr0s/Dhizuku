package com.rosan.dhizuku.ui.page.settings.activate

import android.annotation.SuppressLint
import android.app.admin.DeviceAdminInfo
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.admin.IDevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.system.Os
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.data.common.util.has
import com.rosan.dhizuku.data.common.util.requireDhizukuPermissionGranted
import com.rosan.dhizuku.data.common.util.requireShizukuPermissionGranted
import com.rosan.dhizuku.server.DhizukuState
import com.rosan.dhizuku.ui.page.settings.SettingsRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.xmlpull.v1.XmlPullParserException
import rikka.shizuku.ShizukuBinderWrapper
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException


class ActivateViewModel : ViewModel(), KoinComponent {
    private val context by inject<Context>()

    private val packageManager by lazy {
        context.packageManager
    }

    private val devicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    var state by mutableStateOf(ActivateViewState())
        private set

    fun collect() {
        collectData()
    }

    private var collectDataJob: Job? = null

    fun collectData() {
        state = state.copy(loading = true)
        collectDataJob?.cancel()
        collectDataJob = viewModelScope.launch(Dispatchers.IO) {
            // http://aospxref.com/android-14.0.0_r2/xref/packages/apps/Settings/src/com/android/settings/applications/specialaccess/deviceadmin/DeviceAdminListPreferenceController.java#271

            var flags = PackageManager.GET_META_DATA
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                flags = flags or PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS

            val data = packageManager.queryBroadcastReceivers(
                Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                flags
            ).mapNotNull {
                if (it == null) return@mapNotNull null
                try {
                    return@mapNotNull DeviceAdminInfo(context, it)
                } catch (ignored: XmlPullParserException) {
                } catch (ignored: IOException) {
                }
                return@mapNotNull null
            }.filter {
                if (!it.isVisible) return@filter false
                !it.activityInfo.applicationInfo.flags.has(ApplicationInfo.FLAG_SYSTEM)
            }.map {
                val component = it.component
                ActivateViewData(
                    admin = it,
                    enabled = devicePolicyManager.isAdminActive(component)
                            && devicePolicyManager.isDeviceOwnerApp(component.packageName)
                )
            }.sortedBy {
                it.admin.packageName
            }
            state = state.copy(data = data, loading = false)
        }
    }

    private var activateJob: Job? = null

    fun cancel() {
        activateJob?.cancel()
        state = state.copy(status = ActivateViewState.Status.Waiting)
    }

    fun activate(
        mode: SettingsRoute.Activate.Mode,
        comp: ComponentName
    ) {
        state = state.copy(status = ActivateViewState.Status.Running)
        activateJob?.cancel()
        activateJob = viewModelScope.launch(Dispatchers.IO) {
            val exceptionOrNull = kotlin.runCatching {
                when (mode) {
                    SettingsRoute.Activate.Mode.Dhizuku -> activateByDhizuku(comp)
                    SettingsRoute.Activate.Mode.Shizuku -> activateByShizuku(comp)
                }
            }.exceptionOrNull().let {
                if (it is InvocationTargetException) it.targetException
                else it
            }
            if (exceptionOrNull is CancellationException) return@launch
            state = state.copy(status = ActivateViewState.Status.End(error = exceptionOrNull))
        }
    }

    @SuppressLint("NewApi")
    private suspend fun activateByDhizuku(comp: ComponentName) =
        requireDhizukuPermissionGranted {
            requireBinderWrapperDevicePolicyManager(wrapper = {
                Dhizuku.binderWrapper(it)
            }) {
                it.transferOwnership(Dhizuku.getOwnerComponent(), comp, null)
            }
            DhizukuState.sync(devicePolicyManager)
        }

    @SuppressLint("PrivateApi")
    private suspend fun activateByShizuku(who: ComponentName) =
        requireShizukuPermissionGranted(context) {
            // wait for the account cache be refreshed
            delay(1500)
            requireBinderWrapperDevicePolicyManager(wrapper = {
                ShizukuBinderWrapper(it)
            }) {
                val userId = Os.getuid() / 100000
                it.setActiveAdmin(who, true, userId)
                it.setDeviceOwner(who, null, userId)
            }
        }

    private fun DevicePolicyManager.setActiveAdmin(
        policyReceiver: ComponentName,
        refreshing: Boolean,
        userHandle: Int
    ) = this::class.java
        .getDeclaredMethod(
            "setActiveAdmin",
            ComponentName::class.java,
            Boolean::class.java,
            Int::class.java
        )
        .also { it.isAccessible = true }
        .invoke(this, policyReceiver, refreshing, userHandle)

    private fun DevicePolicyManager.setDeviceOwner(
        who: ComponentName,
        ownerName: String?,
        userId: Int
    ) {
        val clazz = this::class.java
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            clazz.getDeclaredMethod("setDeviceOwner", ComponentName::class.java, Int::class.java)
                .also { it.isAccessible = true }.invoke(this, who, userId)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            clazz.getDeclaredMethod(
                "setDeviceOwner",
                ComponentName::class.java,
                String::class.java,
                Int::class.java
            ).also { it.isAccessible = true }.invoke(this, who, ownerName, userId)
            return
        }
        clazz.getDeclaredMethod("setDeviceOwner", ComponentName::class.java, String::class.java)
            .also { it.isAccessible = true }.invoke(this, who, ownerName)
    }


    @SuppressLint("PrivateApi")
    private fun requireBinderWrapperDevicePolicyManager(
        wrapper: (IBinder) -> IBinder,
        action: (DevicePolicyManager) -> Unit
    ) {
        val manager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        var field: Field? = null
        var iInterface: IInterface? = null
        try {
            field = manager.javaClass.getDeclaredField("mService")
            field.isAccessible = true
            iInterface = field.get(manager) as IInterface
            val binder = wrapper.invoke(iInterface.asBinder()!!)
            field.set(manager, IDevicePolicyManager.Stub.asInterface(binder))
            action.invoke(manager)
        } finally {
            try {
                if (field != null && iInterface != null)
                    field.set(manager, iInterface)
            } catch (ignored: Throwable) {
            }
        }
    }
}