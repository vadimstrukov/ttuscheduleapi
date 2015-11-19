package com.vadimstrukov.ttuschedule;

import android.support.v7.app.AppCompatActivity;

import com.octo.android.robospice.SpiceManager;
import com.vadimstrukov.ttuschedule.service.ScheduleService;

/**
 * Created by vadimstrukov on 11/17/15.
 */
public abstract class BaseScheduleActivity extends AppCompatActivity {
    private SpiceManager spiceManager = new SpiceManager(ScheduleService.class);

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

}
