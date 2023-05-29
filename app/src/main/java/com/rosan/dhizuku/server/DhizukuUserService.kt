package com.rosan.dhizuku.server

import android.content.ComponentName
import android.content.Context
import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import androidx.annotation.Keep
import com.rosan.dhizuku.aidl.IDhizukuUserService
import java.lang.reflect.Constructor
import kotlin.system.exitProcess

class DhizukuUserService @Keep constructor(private val context: Context) :
    IDhizukuUserService.Stub() {
    private val map = mutableMapOf<String, IBinder>()

    override fun quit() {
        for (service in map.values) {
            transact(service, 2)
        }
        exitProcess(0)
    }

    override fun startService(component: ComponentName): IBinder {
        return map.getOrPut(component.flattenToString()) {
            val packageContext = context.createPackageContext(
                component.packageName,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
            val serviceClazz = packageContext.classLoader.loadClass(component.className)
            var constructorWithContext: Constructor<*>? = null
            try {
                constructorWithContext = serviceClazz.getConstructor(Context::class.java)
            } catch (_: NoSuchMethodException) {
            } catch (_: SecurityException) {
            }
            val service =
                (if (constructorWithContext != null) constructorWithContext.newInstance(context)
                else serviceClazz.newInstance()).apply {
                } as IBinder
            transact(service, 1)
            return@getOrPut service
        }
    }

    private fun transact(service: IBinder, code: Int) {
        var dataParcel: Parcel? = null
        var replyParcel: Parcel? = null
        try {
            val data = Parcel.obtain()
            dataParcel = data
            val reply = Parcel.obtain()
            replyParcel = reply
            data.writeInterfaceToken(service.interfaceDescriptor!!)
            service.transact(
                IBinder.FIRST_CALL_TRANSACTION + code, data, reply, Binder.FLAG_ONEWAY
            )
        } finally {
            dataParcel?.recycle()
            replyParcel?.recycle()
        }
    }
}