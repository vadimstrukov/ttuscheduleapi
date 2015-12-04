package ee.ttu.schedule.service;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

/**
 * Created by vadimstrukov on 11/17/15.
 */
public class ScheduleService extends SpiceService {
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        InFileStringObjectPersister inFileStringObjectPersister =
                new InFileStringObjectPersister(application);

        cacheManager.addPersister(inFileStringObjectPersister);
        return cacheManager;
    }

    @Override
    public int getThreadCount() {
        return 3;
    }
}
