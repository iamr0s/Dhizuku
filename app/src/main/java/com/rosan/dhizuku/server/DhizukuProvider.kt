package com.rosan.dhizuku.server

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.server.impl.IDhizukuImpl
import com.rosan.dhizuku.shared.DhizukuVariables
import org.koin.core.component.KoinComponent

class DhizukuProvider : ContentProvider(), KoinComponent {
    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return when (method) {
            DhizukuVariables.PROVIDER_METHOD_CLIENT -> Bundle().apply {
                val clientIBinder = extras?.getBinder(DhizukuVariables.EXTRA_CLIENT)
                val client =
                    if (clientIBinder != null) IDhizukuClient.Stub.asInterface(clientIBinder)
                    else null
                putBinder(DhizukuVariables.PARAM_DHIZUKU_BINDER, IDhizukuImpl(client).asBinder())
            }

            else -> null;
        }
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
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}