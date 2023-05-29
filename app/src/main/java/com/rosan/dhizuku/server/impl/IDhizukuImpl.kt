package com.rosan.dhizuku.server.impl

import android.content.Context
import android.content.SharedPreferences
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import com.rosan.dhizuku.R
import com.rosan.dhizuku.aidl.IDhizuku
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.aidl.IDhizukuRemoteProcess
import com.rosan.dhizuku.aidl.IDhizukuUserServiceConnection
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.server.DHIZUKU_SERVER_VERSION_NAME
import com.rosan.dhizuku.server.DHIZUKU_SERVRE_VERSION_CODE
import com.rosan.dhizuku.server.DhizukuProcess
import com.rosan.dhizuku.server.DhizukuUserServiceArgs
import com.rosan.dhizuku.server.DhizukuUserServiceConnections
import com.rosan.dhizuku.shared.DhizukuVariables
import com.rosan.dhizuku.util.toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class IDhizukuImpl(private val client: IDhizukuClient? = null) : IDhizuku.Stub(), KoinComponent {
    private var toastedWhenUsingDhizuku = false

    private val sharedPreferences by inject<SharedPreferences> {
        parametersOf("preferred")
    }

    private val context by inject<Context>()

    private val appRepo by inject<AppRepo>()

    override fun getVersionCode(): Int = DHIZUKU_SERVRE_VERSION_CODE

    override fun getVersionName(): String = DHIZUKU_SERVER_VERSION_NAME

    override fun isPermissionGranted(): Boolean {
        return appRepo.findByUID(Binder.getCallingUid())?.allowApi ?: false
    }

    private fun requireCallingPermission(name: String) {
        if (!isPermissionGranted) throw IllegalStateException(SecurityException("uid '${Binder.getCallingUid()}' not allow use dhizuku api"))
        if (toastedWhenUsingDhizuku ||
            !sharedPreferences.getBoolean("toast_when_using_dhizuku", false)
        ) return
        val packageManager = context.packageManager
        val packageName = (packageManager.getPackagesForUid(Binder.getCallingUid())
            ?: emptyArray<String>()).first()
        val label =
            packageManager.getPackageInfo(packageName, 0).applicationInfo.loadLabel(packageManager)
        context.toast(context.getString(R.string.toast_when_using_dhizuku_content, label))
        toastedWhenUsingDhizuku = true
    }

    private fun targetTransact(
        iBinder: IBinder, code: Int, data: Parcel, reply: Parcel?, flags: Int
    ): Boolean {
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