package android.app.admin;

import android.content.ComponentName;

/**
 * @noinspection unused
 */
public class DevicePolicyManager {
    public boolean setDeviceOwner(String packageName) {
        return false;
    }
    public boolean setDeviceOwner(String packageName, String ownerName) {
        return false;
    }
    public boolean setDeviceOwner(ComponentName who) {
        return false;
    }
    public boolean setDeviceOwner(ComponentName who, int userId) {
        return false;
    }
    public boolean setDeviceOwner(ComponentName who, String ownerName) {
        return false;
    }
    public boolean setDeviceOwner(ComponentName who, String ownerName, int userId) {
        return false;
    }
}
