package android.app.admin;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IDevicePolicyManager extends IInterface {
    void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle);

    void removeActiveAdmin(ComponentName policyReceiver, int userHandle);

    boolean setDeviceOwner(ComponentName who, String ownerName, int userId, boolean setProfileOwnerOnCurrentUserIfNecessary);

    boolean setDeviceOwner(ComponentName who, String ownerName, int userId);

    boolean setDeviceOwner(String packageName, String ownerName);

    abstract class Stub extends Binder implements IDevicePolicyManager {
        public static IDevicePolicyManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}