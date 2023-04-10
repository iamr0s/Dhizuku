package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.InstantAppInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.IOnChecksumsReadyListener;
import android.content.pm.IPackageInstaller;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.ComponentEnabledSetting;
import android.content.pm.ProviderInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.pm.VersionedPackage;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.PersistableBundle;
import android.content.IntentSender;

interface IPackageManager {
    void checkPackageStartable(String packageName, int userId);
    boolean isPackageAvailable(String packageName, int userId);
    PackageInfo getPackageInfo(String packageName, long flags, int userId);
    PackageInfo getPackageInfoVersioned(in VersionedPackage versionedPackage,
            long flags, int userId);
    int getPackageUid(String packageName, long flags, int userId);
    int[] getPackageGids(String packageName, long flags, int userId);
    String[] currentToCanonicalPackageNames(in String[] names);
    String[] canonicalToCurrentPackageNames(in String[] names);
    ApplicationInfo getApplicationInfo(String packageName, long flags, int userId);
    int getTargetSdkVersion(String packageName);
    ActivityInfo getActivityInfo(in ComponentName className, long flags, int userId);
    boolean activitySupportsIntent(in ComponentName className, in Intent intent,
            String resolvedType);
    ActivityInfo getReceiverInfo(in ComponentName className, long flags, int userId);
    ServiceInfo getServiceInfo(in ComponentName className, long flags, int userId);
    ProviderInfo getProviderInfo(in ComponentName className, long flags, int userId);
    boolean isProtectedBroadcast(String actionName);
    int checkSignatures(String pkg1, String pkg2);
    int checkUidSignatures(int uid1, int uid2);
    List<String> getAllPackages();
    String[] getPackagesForUid(int uid);
    String getNameForUid(int uid);
    String[] getNamesForUids(in int[] uids);
    int getUidForSharedUser(String sharedUserName);
    int getFlagsForUid(int uid);
    int getPrivateFlagsForUid(int uid);
    boolean isUidPrivileged(int uid);
    ResolveInfo resolveIntent(in Intent intent, String resolvedType, long flags, int userId);
    ResolveInfo findPersistentPreferredActivity(in Intent intent, int userId);
    boolean canForwardTo(in Intent intent, String resolvedType, int sourceUserId, int targetUserId);
    ResolveInfo resolveService(in Intent intent,
            String resolvedType, long flags, int userId);
    ProviderInfo resolveContentProvider(String name, long flags, int userId);
    void querySyncProviders(inout List<String> outNames,
            inout List<ProviderInfo> outInfo);
    InstrumentationInfo getInstrumentationInfo(
            in ComponentName className, int flags);
    void finishPackageInstall(int token, boolean didLaunch);
    void setInstallerPackageName(in String targetPackage, in String installerPackageName);
    void setApplicationCategoryHint(String packageName, int categoryHint, String callerPackageName);
    String getInstallerPackageName(in String packageName);
    InstallSourceInfo getInstallSourceInfo(in String packageName);
    void resetApplicationPreferences(int userId);
    ResolveInfo getLastChosenActivity(in Intent intent,
            String resolvedType, int flags);
    void setLastChosenActivity(in Intent intent, String resolvedType, int flags,
            in IntentFilter filter, int match, in ComponentName activity);
    void addPreferredActivity(in IntentFilter filter, int match,
            in ComponentName[] set, in ComponentName activity, int userId, boolean removeExisting);
    void replacePreferredActivity(in IntentFilter filter, int match,
            in ComponentName[] set, in ComponentName activity, int userId);
    void clearPackagePreferredActivities(String packageName);
    int getPreferredActivities(out List<IntentFilter> outFilters,
            out List<ComponentName> outActivities, String packageName);
    void addPersistentPreferredActivity(in IntentFilter filter, in ComponentName activity, int userId);
    void clearPackagePersistentPreferredActivities(String packageName, int userId);
    void addCrossProfileIntentFilter(in IntentFilter intentFilter, String ownerPackage,
            int sourceUserId, int targetUserId, int flags);
    void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage);
    String[] setDistractingPackageRestrictionsAsUser(in String[] packageNames, int restrictionFlags,
            int userId);
    String[] getUnsuspendablePackagesForUser(in String[] packageNames, int userId);
    boolean isPackageSuspendedForUser(String packageName, int userId);
    Bundle getSuspendedPackageAppExtras(String packageName, int userId);
    byte[] getPreferredActivityBackup(int userId);
    void restorePreferredActivities(in byte[] backup, int userId);
    byte[] getDefaultAppsBackup(int userId);
    void restoreDefaultApps(in byte[] backup, int userId);
    byte[] getDomainVerificationBackup(int userId);
    void restoreDomainVerification(in byte[] backup, int userId);
     
     ComponentName getHomeActivities(out List<ResolveInfo> outHomeCandidates);
    void setHomeActivity(in ComponentName className, int userId);
    void overrideLabelAndIcon(in ComponentName componentName, String nonLocalizedLabel,
            int icon, int userId);
    void restoreLabelAndIcon(in ComponentName componentName, int userId);
    
    void setComponentEnabledSetting(in ComponentName componentName,
            in int newState, in int flags, int userId);
    void setComponentEnabledSettings(in List<ComponentEnabledSetting> settings, int userId);
    
    int getComponentEnabledSetting(in ComponentName componentName, int userId);
    
    void setApplicationEnabledSetting(in String packageName, in int newState, int flags,
            int userId, String callingPackage);
    
    int getApplicationEnabledSetting(in String packageName, int userId);
    void logAppProcessStartIfNeeded(String packageName, String processName, int uid, String seinfo, String apkFile, int pid);
    void flushPackageRestrictionsAsUser(in int userId);
    
    void setPackageStoppedState(String packageName, boolean stopped, int userId);
     void freeStorage(in String volumeUuid, in long freeStorageSize,
             int storageFlags, in IntentSender pi);
    void clearApplicationProfileData(in String packageName);

    String[] getSystemSharedLibraryNames();
    boolean hasSystemFeature(String name, int version);
    void enterSafeMode();
    boolean isSafeMode();
    boolean hasSystemUidErrors();
    oneway void notifyPackageUse(String packageName, int reason);
    oneway void notifyDexLoad(String loadingPackageName,
            in Map<String, String> classLoaderContextMap, String loaderIsa);
    boolean performDexOptMode(String packageName, boolean checkProfiles,
            String targetCompilerFilter, boolean force, boolean bootComplete, String splitName);
    boolean performDexOptSecondary(String packageName,
            String targetCompilerFilter, boolean force);
    void dumpProfiles(String packageName, boolean dumpClassesAndMethods);
    void forceDexOpt(String packageName);
    void reconcileSecondaryDexFiles(String packageName);
    int getMoveStatus(int moveId);
    int movePackage(in String packageName, in String volumeUuid);
    int movePrimaryStorage(in String volumeUuid);
    boolean setInstallLocation(int loc);
    int getInstallLocation();
    int installExistingPackageAsUser(String packageName, int userId, int installFlags,
            int installReason, in List<String> whiteListedPermissions);
    void verifyPendingInstall(int id, int verificationCode);
    void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay);
    void verifyIntentFilter(int id, int verificationCode, in List<String> failedDomains);
    int getIntentVerificationStatus(String packageName, int userId);
    boolean updateIntentVerificationStatus(String packageName, int status, int userId);
    boolean isFirstBoot();
    boolean isOnlyCoreApps();
    boolean isDeviceUpgrading();
    
    boolean isStorageLow();
    boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId);
    boolean getApplicationHiddenSettingAsUser(String packageName, int userId);
    void setSystemAppHiddenUntilInstalled(String packageName, boolean hidden);
    boolean setSystemAppInstallState(String packageName, boolean installed, int userId);
    IPackageInstaller getPackageInstaller();
    boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId);
    boolean getBlockUninstallForUser(String packageName, int userId);
    String getPermissionControllerPackageName();
    String getSdkSandboxPackageName();
    byte[] getInstantAppCookie(String packageName, int userId);
    boolean setInstantAppCookie(String packageName, in byte[] cookie, int userId);
    Bitmap getInstantAppIcon(String packageName, int userId);
    boolean isInstantApp(String packageName, int userId);
    boolean setRequiredForSystemUser(String packageName, boolean systemUserApp);
    void setUpdateAvailable(String packageName, boolean updateAvaialble);
    String getServicesSystemSharedLibraryPackageName();
    String getSharedSystemSharedLibraryPackageName();
    ChangedPackages getChangedPackages(int sequenceNumber, int userId);
    boolean isPackageDeviceAdminOnAnyUser(String packageName);
    int getInstallReason(String packageName, int userId);
    boolean canRequestPackageInstalls(String packageName, int userId);
    void deletePreloadsFileCache();
    ComponentName getInstantAppResolverComponent();
    ComponentName getInstantAppResolverSettingsComponent();
    ComponentName getInstantAppInstallerComponent();
    String getInstantAppAndroidId(String packageName, int userId);
    void setHarmfulAppWarning(String packageName, CharSequence warning, int userId);
    CharSequence getHarmfulAppWarning(String packageName, int userId);
    boolean hasSigningCertificate(String packageName, in byte[] signingCertificate, int flags);
    boolean hasUidSigningCertificate(int uid, in byte[] signingCertificate, int flags);
    String getDefaultTextClassifierPackageName();
    String getSystemTextClassifierPackageName();
    String getAttentionServicePackageName();
    String getRotationResolverPackageName();
    String getWellbeingPackageName();
    String getAppPredictionServicePackageName();
    String getSystemCaptionsServicePackageName();
    String getSetupWizardPackageName();
    String getIncidentReportApproverPackageName();
    String getContentCaptureServicePackageName();
    boolean isPackageStateProtected(String packageName, int userId);
    void sendDeviceCustomizationReadyBroadcast();
    List<ModuleInfo> getInstalledModules(int flags);
    ModuleInfo getModuleInfo(String packageName, int flags);
    int getRuntimePermissionsVersion(int userId);
    void setRuntimePermissionsVersion(int version, int userId);
    void notifyPackagesReplacedReceived(in String[] packages);
    void requestPackageChecksums(in String packageName, boolean includeSplits, int optional, int required, in List trustedInstallers, in IOnChecksumsReadyListener onChecksumsReadyListener, int userId);
    IntentSender getLaunchIntentSenderForPackage(String packageName, String callingPackage,
                String featureId, int userId);
    String[] getAppOpPermissionPackages(String permissionName);
    PermissionGroupInfo getPermissionGroupInfo(String name, int flags);
    boolean addPermission(in PermissionInfo info);
    boolean addPermissionAsync(in PermissionInfo info);
    void removePermission(String name);
    int checkPermission(String permName, String pkgName, int userId);
    void grantRuntimePermission(String packageName, String permissionName, int userId);
    int checkUidPermission(String permName, int uid);
    void setMimeGroup(String packageName, String group, in List<String> mimeTypes);
    String getSplashScreenTheme(String packageName, int userId);
    void setSplashScreenTheme(String packageName, String themeName, int userId);
    List<String> getMimeGroup(String packageName, String group);
    boolean isAutoRevokeWhitelisted(String packageName);
    void makeProviderVisible(int recipientAppId, String visibleAuthority);
    void makeUidVisible(int recipientAppId, int visibleUid);
    IBinder getHoldLockToken();
    void holdLock(in IBinder token, in int durationMs);
    void setKeepUninstalledPackages(in List<String> packageList);
    boolean canPackageQuery(String sourcePackageName, String targetPackageName, int userId);
}