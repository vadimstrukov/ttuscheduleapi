package ee.ttu.schedule.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import ee.ttu.schedule.service.adapter.SyncAdapter;

public class SyncService extends Service {
    private final String TAG = this.getClass().getSimpleName();

    private static final Object syncAdapterLock = new Object();
    private SyncAdapter syncAdapter = null;

    public SyncService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        synchronized (syncAdapterLock){
            if(syncAdapter == null)
                syncAdapter = new SyncAdapter(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
