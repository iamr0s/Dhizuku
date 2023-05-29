package com.rosan.dhizuku.aidl;

import android.content.ComponentName;

interface IDhizukuUserService {
    void quit();

    IBinder startService(in ComponentName component);
}