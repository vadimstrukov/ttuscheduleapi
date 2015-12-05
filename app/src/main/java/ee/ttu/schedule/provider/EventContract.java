package ee.ttu.schedule.provider;


import android.net.Uri;
import android.provider.BaseColumns;

public class EventContract {
    public static final String CONTENT_AUTHORITY = "ee.ttu.schedule";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_EVENTS = "events";

    public interface EventColumns extends BaseColumns {
        String KEY_DT_START = "date_start";
        String KEY_DT_END = "date_end";
        String KEY_DESCRIPTION = "description";
        String KEY_LOCATION = "location";
        String KEY_SUMMARY = "summary";
    }

    public static class Event implements BaseColumns, EventColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        public static final String TABLE_NAME = "event";
    }
}
