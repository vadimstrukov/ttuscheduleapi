package ee.ttu.schedule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ee.ttu.schedule.authenticator.AccountAuthenticator;

public class AuthenticatorService extends Service {
    private AccountAuthenticator accountAuthenticator;

    public AuthenticatorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        accountAuthenticator = new AccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }
}
