package ee.ttu.schedule.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ScheduleProvider extends ContentProvider {
    private static final String AUTHORITY = EventContract.CONTENT_AUTHORITY;

    private static final int ROUTE_EVENTS = 1;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "events", ROUTE_EVENTS);
    }

    private ScheduleDb dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ScheduleDb(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)){
            case ROUTE_EVENTS:
                builder.setTables(EventContract.Event.TABLE_NAME);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)){
            case ROUTE_EVENTS:
                long id = db.insertOrThrow(EventContract.Event.TABLE_NAME, null, values);
                uri = Uri.parse(EventContract.Event.CONTENT_URI + "/" + id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        Context context = getContext();
        assert context != null;
        getContext().getContentResolver().notifyChange(uri, null, false);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
    static class ScheduleDb extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "schedule.db";

        public ScheduleDb(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String[] args = new String[]{
                    EventContract.Event.TABLE_NAME, EventContract.EventColumns._ID, EventContract.EventColumns.KEY_DT_START,
                    EventContract.EventColumns.KEY_DT_END, EventContract.EventColumns.KEY_DESCRIPTION, EventContract.EventColumns.KEY_LOCATION,
                    EventContract.EventColumns.KEY_SUMMARY
            };
            String sql = String.format("CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY, %3$s INTEGER, %4$s INTEGER, %5$s TEXT, %6$s TEXT, %7$s TEXT)", args);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion < newVersion)
                db.execSQL("DROP TABLE IF EXISTS " + EventContract.Event.TABLE_NAME);
            onCreate(db);
        }
    }
}
