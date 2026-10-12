package android.accounts;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAccountManagerResponse extends IInterface {
    void onResult(Bundle value) throws RemoteException;
    void onError(int errorCode, String errorMessage) throws RemoteException;

    abstract class Stub extends Binder implements IAccountManagerResponse {
        private static final String DESCRIPTOR = "android.accounts.IAccountManagerResponse";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            switch (code) {
                case 1: { // onResult
                    data.enforceInterface(DESCRIPTOR);
                    Bundle value = null;
                    if (0 != data.readInt()) {
                        value = Bundle.CREATOR.createFromParcel(data);
                    }
                    onResult(value);
                    reply.writeNoException();
                    return true;
                }
                case 2: { // onError
                    data.enforceInterface(DESCRIPTOR);
                    int errorCode = data.readInt();
                    String errorMessage = data.readString();
                    onError(errorCode, errorMessage);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}
