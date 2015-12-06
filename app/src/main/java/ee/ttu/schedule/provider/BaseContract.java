package ee.ttu.schedule.provider;

import android.net.Uri;

public abstract class BaseContract {
    public static final String CONTENT_AUTHORITY = "ee.ttu.schedule";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
