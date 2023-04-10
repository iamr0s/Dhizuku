package android.content.pm;
import android.content.Intent;
import android.os.Bundle;

oneway interface IPackageInstallObserver2 {
    void onUserActionRequired(in Intent intent);
    void onPackageInstalled(String basePackageName, int returnCode, String msg, in Bundle extras);
}