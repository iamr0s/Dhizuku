package com.rosan.dhizuku.api;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.rosan.dhizuku.aidl.IDhizuku;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.io.File;

public class Dhizuku {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;

    private static IDhizuku remote = null;

    public static boolean init(Context context) {
        assert context != null;
        if (remote != null && remote.asBinder().pingBinder()) return true;
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(DhizukuVariables.PROVIDER_AUTHORITY)
                .build();
        Bundle bundle;
        try {
            bundle = context.getContentResolver().call(
                    uri,
                    DhizukuVariables.PROVIDER_METHOD_CLIENT,
                    null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (bundle == null) return false;
        IBinder iBinder = bundle.getBinder(DhizukuVariables.PARAM_DHIZUKU_BINDER);
        if (iBinder == null) return false;
        remote = IDhizuku.Stub.asInterface(iBinder);
        mContext = context;
        return true;
    }

    private static IDhizuku requireServer() {
        if (remote != null && remote.asBinder().pingBinder()) return remote;
        if (mContext != null && init(mContext)) return remote;
        throw new IllegalStateException("binder haven't been received");
    }

    public static int getVersionCode() {
        try {
            return requireServer().getVersionCode();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getVersionName() {
        try {
            return requireServer().getVersionName();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPermissionGranted() {
        try {
            return requireServer().isPermissionGranted();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static void requestPermission(DhizukuRequestPermissionListener listener) {
        requestPermission(mContext, listener);
    }

    public static void requestPermission(Context context, DhizukuRequestPermissionListener listener) {
        Bundle bundle = new Bundle();
        bundle.putInt(DhizukuVariables.PARAM_CLIENT_UID, context.getApplicationInfo().uid);
        bundle.putBinder(DhizukuVariables.PARAM_CLIENT_REQUEST_PERMISSION_BINDER, listener.asBinder());
        Intent intent = new Intent(DhizukuVariables.ACTION_REQUEST_PERMISSION)
                .putExtra("bundle", bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean remoteTransact(IBinder iBinder, int code, Parcel data, Parcel reply, int flags) {
        boolean result = false;
        Parcel remoteData = Parcel.obtain();
        try {
            remoteData.writeInterfaceToken(DhizukuVariables.BINDER_DESCRIPTOR);
            remoteData.writeStrongBinder(iBinder);
            remoteData.writeInt(code);
            remoteData.writeInt(flags);
            remoteData.appendFrom(data, 0, data.dataSize());
            result = requireServer().asBinder().transact(DhizukuVariables.TRANSACT_CODE_REMOTE_BINDER, remoteData, reply, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } finally {
            remoteData.recycle();
        }
        return result;
    }

    public static IBinder binderWrapper(IBinder iBinder) {
        return new DhizukuBinderWrapper(iBinder);
    }

    public static DhizukuRemoteProcess newProcess(String[] cmd, String[] env, File dir) {
        try {
            return new DhizukuRemoteProcess(requireServer().remoteProcess(cmd, env, dir != null ? dir.getPath() : null));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
