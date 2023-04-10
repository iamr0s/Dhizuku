package android.content.pm;

import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.PackageInstaller;
import android.content.pm.VersionedPackage;
import android.content.IntentSender;
import android.graphics.Bitmap;

interface IPackageInstaller {
    int createSession(in PackageInstaller.SessionParams params, String installerPackageName, String installerAttributionTag, int userId);
//     @RequiresApi(Build.VERSION_CODES.S)
//     int createSession(in PackageInstaller.SessionParams params, String installerPackageName, int userId);
    void updateSessionAppIcon(int sessionId, in Bitmap appIcon);
    void updateSessionAppLabel(int sessionId, String appLabel);
    void abandonSession(int sessionId);
    IPackageInstallerSession openSession(int sessionId);
    PackageInstaller.SessionInfo getSessionInfo(int sessionId);
    void registerCallback(IPackageInstallerCallback callback, int userId);
    void unregisterCallback(IPackageInstallerCallback callback);
    void uninstall(in VersionedPackage versionedPackage, String callerPackageName, int flags,
            in IntentSender statusReceiver, int userId);
    void uninstallExistingPackage(in VersionedPackage versionedPackage, String callerPackageName,
            in IntentSender statusReceiver, int userId);
    void installExistingPackage(String packageName, int installFlags, int installReason,
            in IntentSender statusReceiver, int userId, in List<String> whiteListedPermissions);
    void setPermissionsResult(int sessionId, boolean accepted);
    void bypassNextStagedInstallerCheck(boolean value);
    void bypassNextAllowedApexUpdateCheck(boolean value);
    void setAllowUnlimitedSilentUpdates(String installerPackageName);
    void setSilentUpdatesThrottleTime(long throttleTimeInSeconds);
}