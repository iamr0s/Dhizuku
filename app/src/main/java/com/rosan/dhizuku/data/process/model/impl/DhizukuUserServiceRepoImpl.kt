package com.rosan.dhizuku.data.process.model.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.os.Parcel
import com.rosan.dhizuku.aidl.IDhizukuUserServiceManager
import com.rosan.dhizuku.data.process.repo.ProcessRepo
import com.rosan.dhizuku.data.reflect.repo.ReflectRepo
import com.rosan.dhizuku.server.DhizukuProcessReceiver
import com.rosan.dhizuku.shared.DhizukuVariables
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.reflect.Constructor
import kotlin.system.exitProcess

class DhizukuUserServiceRepoImpl {
    companion object : ProcessRepo(), KoinComponent {
        private val context by inject<Context>()

        private val reflect by inject<ReflectRepo>()

        private val options = Options().addOption(
            Option.builder("c").argName("component name (pName/cName)").required().hasArg()
                .type(String::class.java).build()
        )

        @JvmStatic
        override fun main(args: Array<String>) = super.main(args)

        override fun onCreate(args: Array<String>) {
            super.onCreate(args)
            val cmdLine = DefaultParser().parse(options, args)
            val componentName = ComponentName.unflattenFromString(cmdLine.getOptionValue("c"))
                ?: throw Exception("need component name")
            val service = createService(componentName).asBinder()
            transactService(service, 1)
            val manager = createManager(service)
            val bundle = Bundle()
            bundle.putBinder(DhizukuProcessReceiver.PARAM_MANAGER, manager.asBinder())
            bundle.putBinder(DhizukuProcessReceiver.PARAM_USER_SERVICE, service)
            bundle.putParcelable(DhizukuVariables.PARAM_COMPONENT, componentName)
            val intent = Intent(DhizukuProcessReceiver.ACTION_USER_SERVICE).putExtras(bundle)
            context.sendBroadcast(intent)
            Looper.loop()
        }

        private fun createManager(service: IBinder): IDhizukuUserServiceManager {
            return object : IDhizukuUserServiceManager.Stub() {
                override fun destroy() {
                    transactService(service, 2)
                    exitProcess(0)
                }
            }
        }

        private fun createService(componentName: ComponentName): IInterface {
//            // compat for multi-user
//            val userHandle = UserHandleCompat.getUserHandleForUid(uid)
//            val packageContext = reflect.getDeclaredMethod(
//                Context::class.java,
//                "createPackageContextAsUser",
//                String::class.java,
//                Int::class.java,
//                UserHandle::class.java
//            )?.let {
//                it.isAccessible = true
//                it.invoke(
//                    context,
//                    componentName.packageName,
//                    Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY,
//                    userHandle
//                )
//            } as Context
            val packageContext = context.createPackageContext(
                componentName.packageName,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
            val serviceClazz = packageContext.classLoader.loadClass(componentName.className)
            var constructorWithContext: Constructor<*>? = null
            try {
                constructorWithContext = serviceClazz.getConstructor(Context::class.java)
            } catch (_: NoSuchMethodException) {
            } catch (_: SecurityException) {
            }
            return (if (constructorWithContext != null) constructorWithContext.newInstance(context)
            else serviceClazz.newInstance()).apply {
            } as IInterface
        }

        private fun transactService(service: IBinder, code: Int) {
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
}
