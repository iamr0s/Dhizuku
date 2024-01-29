package com.rosan.dhizuku.server.impl

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import androidx.annotation.RequiresApi
import com.rosan.dhizuku.R
import com.rosan.dhizuku.aidl.IDhizuku
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.aidl.IDhizukuRemoteProcess
import com.rosan.dhizuku.aidl.IDhizukuUserServiceConnection
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.common.util.toast
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.server.DHIZUKU_SERVER_VERSION_NAME
import com.rosan.dhizuku.server.DHIZUKU_SERVRE_VERSION_CODE
import com.rosan.dhizuku.server.DhizukuProcess
import com.rosan.dhizuku.server.DhizukuUserServiceArgs
import com.rosan.dhizuku.server.DhizukuUserServiceConnections
import com.rosan.dhizuku.shared.DhizukuVariables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.collections.set

class IDhizukuImpl(private val client: IDhizukuClient) : IDhizuku.Stub(), KoinComponent {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val context by inject<Context>()

    private val devicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    private val repo by inject<AppRepo>()

    private var enableToast = true

    private var toasted = false

    private lateinit var appEntity: AppEntity

    private var callingCheckUid = -1

    private var callingCheckJob: Job? = null

    private lateinit var callingCheckPackageInfo: PackageInfo

    init {
        initSharedPreference()
    }

    private fun initSharedPreference() {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                if (key != "toast_when_using_dhizuku") return@OnSharedPreferenceChangeListener
                enableToast = sharedPreferences.getBoolean("toast_when_using_dhizuku", false)
            }
        val preferences = get<SharedPreferences> { parametersOf("preferred") }
        enableToast = preferences.getBoolean("toast_when_using_dhizuku", false)
        preferences.registerOnSharedPreferenceChangeListener(listener)
        client.asBinder().linkToDeath({
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }, 0)
    }

    private fun initCallingCheckJob() = synchronized(this) {
        val uid = Binder.getCallingUid()
        if (callingCheckUid == uid && callingCheckJob != null) return@synchronized
        callingCheckUid = uid
        callingCheckJob?.cancel()
        appEntity = repo.findByUID(uid) ?: AppEntity.DENY
        callingCheckJob = coroutineScope.launch {
            repo.flowFindByUID(uid).collect {
                appEntity = repo.findByUID(uid) ?: AppEntity.DENY
            }
        }
        callingCheckPackageInfo = context.packageManager.getPackageInfoForUid(uid)!!
        client.asBinder()?.linkToDeath({
            callingCheckJob?.cancel()
            callingCheckJob = null
        }, 0)
    }

    override fun getVersionCode(): Int = DHIZUKU_SERVRE_VERSION_CODE

    override fun getVersionName(): String = DHIZUKU_SERVER_VERSION_NAME

    override fun isPermissionGranted(): Boolean {
        return runCatching { requireCallingPermission("is_granted", true) }.isSuccess
    }

    private fun requireCallingPermission(name: String, ignoreToast: Boolean = false) {
        initCallingCheckJob()
        if (!appEntity.allowApi || appEntity.signature != callingCheckPackageInfo.signature)
            throw IllegalStateException(SecurityException("uid '${Binder.getCallingUid()}' not allow use dhizuku api ($name)"))
        if (ignoreToast || toasted || !enableToast) return
        val label =
            callingCheckPackageInfo.applicationInfo.loadLabel(context.packageManager)
        context.toast(context.getString(R.string.toast_when_using_dhizuku_content, label))
        toasted = true
    }

    private fun targetTransact(
        iBinder: IBinder, code: Int, data: Parcel, reply: Parcel?, flags: Int
    ): Boolean {
        requireCallingPermission("remote_transact")
        return DhizukuProcess.binderWrapper(iBinder).transact(code, data, reply, flags)
    }

    override fun remoteProcess(
        cmd: Array<out String>, env: Array<out String>?, dir: String?
    ): IDhizukuRemoteProcess {
        requireCallingPermission("remote_process")
        val environment = mutableMapOf<String, String>()
        if (env != null) for (envString in env) {
            val index: Int = envString.indexOf('=')
            if (index != -1) environment[envString.substring(0, index)] =
                envString.substring(index + 1)
        }
        val process = DhizukuProcess.remoteProcess(cmd.toMutableList(), environment, dir)
        return IDhizukuRemoteProcessImpl(process, client?.asBinder() ?: this)
    }

    override fun bindUserService(
        connection: IDhizukuUserServiceConnection?, bundle: Bundle?
    ) {
        requireCallingPermission("bind_user_service")
        bundle ?: return
        connection ?: return
        val uid = Binder.getCallingUid()
        val pid = Binder.getCallingPid()
        val args = DhizukuUserServiceArgs(bundle)
        DhizukuUserServiceConnections.bind(uid, pid, args, connection)
    }

    override fun unbindUserService(bundle: Bundle?) {
        requireCallingPermission("unbind_user_service")
        bundle ?: return
        val uid = Binder.getCallingUid()
        val pid = Binder.getCallingPid()
        val args = DhizukuUserServiceArgs(bundle)
        DhizukuUserServiceConnections.unbind(uid, pid, args)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getDelegatedScopes(packageName: String): Array<String> {
        requireCallingPermission("get_delegated_scopes")
        return devicePolicyManager.getDelegatedScopes(DhizukuVariables.COMPONENT_NAME, packageName)
            .toTypedArray()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setDelegatedScopes(packageName: String, scopes: Array<out String>) {
        requireCallingPermission("set_delegated_scopes")
        return devicePolicyManager.setDelegatedScopes(
            DhizukuVariables.COMPONENT_NAME,
            packageName,
            scopes.toList()
        )
    }

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code == DhizukuVariables.TRANSACT_CODE_REMOTE_BINDER) {
            val targetData = Parcel.obtain()
            try {
                data.enforceInterface(DhizukuVariables.BINDER_DESCRIPTOR)
                val iBinder = data.readStrongBinder()
                val targtCode = data.readInt()
                val targetFlags = data.readInt()
                targetData.appendFrom(data, data.dataPosition(), data.dataAvail())
                return targetTransact(iBinder, targtCode, targetData, reply, targetFlags)
            } finally {
                targetData.recycle()
            }
        }
        return super.onTransact(code, data, reply, flags)
    }
}