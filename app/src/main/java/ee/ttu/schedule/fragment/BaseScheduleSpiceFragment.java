package ee.ttu.schedule.fragment;





import android.app.Fragment;

import com.octo.android.robospice.SpiceManager;
import ee.ttu.schedule.service.ScheduleService;

/**
 * Created by vadimstrukov on 11/17/15.
 */
public abstract class BaseScheduleSpiceFragment extends Fragment {
    private SpiceManager spiceManager = new SpiceManager(ScheduleService.class);


    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getActivity());
    }

    @Override
    public void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    protected SpiceManager getSpiceManager() {
        return spiceManager;
    }

}
