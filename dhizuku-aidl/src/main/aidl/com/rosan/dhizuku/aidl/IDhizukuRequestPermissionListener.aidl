package com.rosan.dhizuku.aidl;

interface IDhizukuRequestPermissionListener {
    oneway void onRequestPermission(int grantResult);
}