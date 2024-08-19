package com.rosan.dhizuku.server

import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.server_api.DhizukuProvider
import com.rosan.dhizuku.server_api.DhizukuService
import org.koin.core.component.KoinComponent

class MyDhizukuProvider : DhizukuProvider(), KoinComponent {
    override fun onCreateService(client: IDhizukuClient): DhizukuService {
        return MyDhizukuService(context, DhizukuState.component, client)
    }
}