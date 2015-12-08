package ee.ttu.schedule.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.vadimstrukov.ttuschedule.R;

import ee.ttu.schedule.utils.Constants;

public class SyncStatusReceiver extends BroadcastReceiver {
    public SyncStatusReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getIntExtra(Constants.SYNC_STATUS, Constants.SYNC_STATUS_FAILED)){
            case Constants.SYNC_STATUS_OK:
                Toast.makeText(context, context.getString(R.string.loading_successful), Toast.LENGTH_SHORT).show();
                break;
            case  Constants.SYNC_STATUS_FAILED:
                Toast.makeText(context, context.getString(R.string.loading_failed), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
