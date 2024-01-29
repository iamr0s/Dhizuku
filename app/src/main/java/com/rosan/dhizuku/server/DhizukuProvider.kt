package com.rosan.dhizuku.server

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.rosan.dhizuku.App
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.server.impl.IDhizukuImpl
import com.rosan.dhizuku.shared.DhizukuVariables
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DhizukuProvider : ContentProvider(), KoinComponent {
    private val app by inject<App>()

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        app.syncOwnerStatus()
        if (!app.isOwner) return null
        if (method != DhizukuVariables.PROVIDER_METHOD_CLIENT) return null
        val client = extras?.getBinder(DhizukuVariables.EXTRA_CLIENT)?.let {
            IDhizukuClient.Stub.asInterface(it)
        } ?: return null
        val bundle = Bundle()
        bundle.putBinder(DhizukuVariables.PARAM_DHIZUKU_BINDER, IDhizukuImpl(client).asBinder())
        return bundle
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int = 0
}