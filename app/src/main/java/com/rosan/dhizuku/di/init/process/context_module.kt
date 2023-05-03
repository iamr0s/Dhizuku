package com.rosan.dhizuku.di.init.process

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.content.Context
import android.os.Build
import android.os.Looper
import android.os.Process
import com.rosan.dhizuku.data.reflect.repo.ReflectRepo
import org.koin.dsl.module

@SuppressLint("PrivateApi")
val contextModule = module {
    single<Context?> {
        // Normal app will have this.
        var thread = ActivityThread.currentActivityThread()
        if (thread == null) {
            // If not normal app (launch by app_process or other).
            if (Looper.getMainLooper() == null) Looper.prepareMainLooper()
            thread = ActivityThread.systemMain()
        }
        // so get normal application or system context.
        val context: Context = thread.application ?: thread.systemContext
        val uid = Process.myUid()
        val packageNames =
            context.packageManager.getPackagesForUid(uid)
        if (packageNames.isNullOrEmpty()) return@single context
        // if current context isn't system context, return it.
        if (
            context.packageName in packageNames &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    context.opPackageName in packageNames)
        ) return@single context
        // create a context for packageName.
        val packageName = packageNames.first()
        val packageContext = context.createPackageContext(
            packageName,
            Context.CONTEXT_IGNORE_SECURITY or Context.CONTEXT_INCLUDE_CODE
        )
        val reflect: ReflectRepo = get()
        val apk = reflect.getDeclaredField(packageContext::class.java, "mPackageInfo")?.let {
            it.isAccessible = true
            return@let it.get(packageContext)
        }!!
        val appContext = reflect.getDeclaredMethod(
            packageContext::class.java,
            "createAppContext",
            ActivityThread::class.java,
            apk::class.java
        )?.let {
            it.isAccessible = true
            return@let it.invoke(null, thread, apk)
        } as Context
        appContext
    }
}