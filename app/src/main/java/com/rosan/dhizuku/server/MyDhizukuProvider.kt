package com.rosan.dhizuku.server

import android.content.ComponentName
import android.content.Context
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.server_api.DhizukuProvider
import com.rosan.dhizuku.server_api.DhizukuService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MyDhizukuProvider : DhizukuProvider(), KoinComponent {
    private val context by inject<Context>()

    override fun onCreateService(client: IDhizukuClient): DhizukuService {
        val component = ComponentName(context, DhizukuDAReceiver::class.java)
        return MyDhizukuService(context, component, client)
    }
}