package ee.ttu.schedule.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class GroupContract {
    public static final String CONTENT_AUTHORITY = "ee.ttu.schedule";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_GROUPS = "groups";

    public interface GroupColumns extends BaseColumns {
        String KEY_NAME = "name";
    }

    public static class Group implements BaseColumns, GroupColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUPS).build();

        public static final String TABLE_NAME = "_group";
    }
}
