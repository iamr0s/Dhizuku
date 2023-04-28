package com.rosan.dhizuku.api;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import com.rosan.dhizuku.aidl.IDhizukuRemoteProcess;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DhizukuRemoteProcess extends Process {
    private static final String TAG = "DhizukuRemoteProcess";

    private IDhizukuRemoteProcess remote;

    private OutputStream outputStream;

    private InputStream inputStream;

    private InputStream errorStream;

    DhizukuRemoteProcess(IDhizukuRemoteProcess remote) {
        this.remote = Objects.requireNonNull(remote);
        try {
            remote.asBinder().linkToDeath(() -> {
                DhizukuRemoteProcess.this.remote = null;
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public OutputStream getOutputStream() {
        if (outputStream != null) return outputStream;
        try {
            outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(remote.getOutputStream());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return outputStream;
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream != null) return inputStream;
        try {
            inputStream = new ParcelFileDescriptor.AutoCloseInputStream(remote.getInputStream());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return inputStream;
    }

    @Override
    public InputStream getErrorStream() {
        if (errorStream != null) return errorStream;
        try {
            errorStream = new ParcelFileDescriptor.AutoCloseInputStream(remote.getErrorStream());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        return errorStream;
    }

    @Override
    public int exitValue() {
        try {
            return remote.exitValue();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            remote.destroy();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAlive() {
        try {
            return remote.alive();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int waitFor() {
        try {
            return remote.waitFor();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean waitFor(long timeout, TimeUnit unit) {
        try {
            return remote.waitForTimeout(timeout, unit.toString());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
