package android.content.pm;
import android.content.Intent;

oneway interface IPackageDeleteObserver2 {
    void onUserActionRequired(in Intent intent);
    void onPackageDeleted(String packageName, int returnCode, String msg);
}