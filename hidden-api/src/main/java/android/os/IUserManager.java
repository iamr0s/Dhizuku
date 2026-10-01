package android.os;

import android.content.pm.UserInfo;

import java.util.List;

public interface IUserManager extends IInterface {
    void setUserRestriction(String key, boolean value, int userHandle);

    List<UserInfo> getUsers(boolean excludePartial, boolean excludeDying, boolean excludePreCreated);

    List<UserInfo> getUsers(boolean excludeDying);

    boolean removeUser(int userHandle);

    abstract class Stub extends Binder implements IUserManager {
        public static IUserManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
