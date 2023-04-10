package com.rosan.dhizuku.aidl;

import android.os.IBinder;
import android.os.Parcel;
import com.rosan.dhizuku.aidl.IDhizukuRemoteProcess;
import com.rosan.dhizuku.aidl.IDhizukuRequestPermissionListener;

interface IDhizuku {
    int getVersionCode() = 0;

    String getVersionName() = 1;

    boolean isPermissionGranted() = 2;

    // remote binder transact: 10

    IDhizukuRemoteProcess remoteProcess(in String[] cmd, in String[] env, in String dir) = 11;
}