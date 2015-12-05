package ee.ttu.schedule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ee.ttu.schedule.service.authenticator.AccountAuthenticator;

public class AuthenticatorService extends Service {
    private final String TAG = this.getClass().getSimpleName();

    private AccountAuthenticator accountAuthenticator;

    public AuthenticatorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        accountAuthenticator = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }
}
