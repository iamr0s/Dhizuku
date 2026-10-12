package android.accounts;

import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.DeprecatedSinceApi;
import androidx.annotation.RequiresApi;

public interface IAccountManager extends IInterface {
    AuthenticatorDescription[] getAuthenticatorTypes(int userId);

    @RequiresApi(Build.VERSION_CODES.M)
    Account[] getAccountsAsUser(String accountType, int userId, String opPackageName);

    @DeprecatedSinceApi(api = Build.VERSION_CODES.M)
    Account[] getAccountsAsUser(String accountType, int userId);

    void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId);

    abstract class Stub extends Binder implements IAccountManager {
        public static IAccountManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
