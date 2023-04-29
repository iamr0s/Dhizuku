package com.rosan.dhizuku.server.impl

import android.content.Context
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import com.rosan.dhizuku.aidl.IDhizuku
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.aidl.IDhizukuRemoteProcess
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.server.DHIZUKU_SERVER_VERSION_NAME
import com.rosan.dhizuku.server.DHIZUKU_SERVRE_VERSION_CODE
import com.rosan.dhizuku.shared.DhizukuVariables
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class IDhizukuImpl(private val client: IDhizukuClient? = null) : IDhizuku.Stub(), KoinComponent {
    private val context by inject<Context>()

    private val appRepo by inject<AppRepo>()

    override fun getVersionCode(): Int = DHIZUKU_SERVRE_VERSION_CODE

    override fun getVersionName(): String = DHIZUKU_SERVER_VERSION_NAME

    override fun isPermissionGranted(): Boolean {
        return appRepo.findByUID(Binder.getCallingUid())?.allowApi ?: false
    }

    private fun requireCallingPermission(name: String) {
        if (isPermissionGranted) return
        throw IllegalStateException(SecurityException("uid '${Binder.getCallingUid()}' not allow use dhizuku api"))
    }

    private fun targetTransact(
        iBinder: IBinder, code: Int, data: Parcel, reply: Parcel?, flags: Int
    ): Boolean {
        requireCallingPermission("target_transact")
        val id = clearCallingIdentity()
        val result = iBinder.transact(code, data, reply, flags)
        restoreCallingIdentity(id)
        return result
    }

    override fun remoteProcess(
        cmd: Array<out String>?, env: Array<out String>?, dir: String?
    ): IDhizukuRemoteProcess {
        requireCallingPermission("remote_process")
        val file = if (dir != null) File(dir) else null
        val process = Runtime.getRuntime().exec(cmd, env, file)
        return IDhizukuRemoteProcessImpl(process, client?.asBinder() ?: this)
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