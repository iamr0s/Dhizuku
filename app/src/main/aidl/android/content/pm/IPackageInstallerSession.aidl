package android.content.pm;

import android.content.pm.Checksum;
import android.content.pm.IOnChecksumsReadyListener;
import android.content.pm.IPackageInstallObserver2;
import android.content.IntentSender;
import android.os.ParcelFileDescriptor;

interface IPackageInstallerSession {
    void setClientProgress(float progress);
    void addClientProgress(float progress);
    String[] getNames();
    ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes);
    ParcelFileDescriptor openRead(String name);
    void write(String name, long offsetBytes, long lengthBytes, in ParcelFileDescriptor fd);
    void stageViaHardLink(String target);
    void setChecksums(String name, in Checksum[] checksums, in byte[] signature);
    void requestChecksums(in String name, int optional, int required, in List trustedInstallers, in IOnChecksumsReadyListener onChecksumsReadyListener);
    void removeSplit(String splitName);
    void close();
    void commit(in IntentSender statusReceiver, boolean forTransferred);
    void transfer(in String packageName);
    void abandon();
    void addFile(int location, String name, long lengthBytes, in byte[] metadata, in byte[] signature);
    void removeFile(int location, String name);
    boolean isMultiPackage();
    int[] getChildSessionIds();
    void addChildSessionId(in int sessionId);
    void removeChildSessionId(in int sessionId);
    int getParentSessionId();
    boolean isStaged();
    int getInstallFlags();
}