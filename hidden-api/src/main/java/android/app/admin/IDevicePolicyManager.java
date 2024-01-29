package android.app.admin;

import android.content.ComponentName;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.RequiresApi;

public interface IDevicePolicyManager extends IInterface {
    void setActiveAdmin(ComponentName policyReceiver, boolean refreshing, int userHandle);

    void removeActiveAdmin(ComponentName policyReceiver, int userHandle);

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    boolean setDeviceOwner(ComponentName who, int userId, boolean setProfileOwnerOnCurrentUserIfNecessary);

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    boolean setDeviceOwner(ComponentName who, String ownerName, int userId, boolean setProfileOwnerOnCurrentUserIfNecessary);

    @RequiresApi(Build.VERSION_CODES.N)
    boolean setDeviceOwner(ComponentName who, String ownerName, int userId);

    boolean setDeviceOwner(String packageName, String ownerName);

    abstract class Stub extends Binder implements IDevicePolicyManager {
        public static IDevicePolicyManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}