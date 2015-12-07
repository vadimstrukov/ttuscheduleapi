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
    private static final int ROUTE_GROUPS = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "events", ROUTE_EVENTS);
        uriMatcher.addURI(AUTHORITY, "groups", ROUTE_GROUPS);
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
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)){
            case ROUTE_EVENTS:
                builder.setTables(EventContract.Event.TABLE_NAME);
                break;
            case ROUTE_GROUPS:
                builder.setTables(GroupContract.Group.TABLE_NAME);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        Context context = getContext();
        assert context != null;
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
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
        long id;
        switch (uriMatcher.match(uri)){
            case ROUTE_EVENTS:
                id = db.insertOrThrow(EventContract.Event.TABLE_NAME, null, values);
                break;
            case ROUTE_GROUPS:
                id = db.insertOrThrow(GroupContract.Group.TABLE_NAME, null, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        Context context = getContext();
        assert context != null;
        getContext().getContentResolver().notifyChange(uri, null, false);
        return Uri.parse(EventContract.Event.CONTENT_URI + "/" + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result;
        switch (uriMatcher.match(uri)){
            case ROUTE_EVENTS:
                result = db.delete(EventContract.Event.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_GROUPS:
                result = db.delete(GroupContract.Group.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        Context context = getContext();
        assert context != null;
        getContext().getContentResolver().notifyChange(uri, null, false);
        return result;
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
            String[] args_event = new String[]{
                    EventContract.Event.TABLE_NAME, EventContract.EventColumns._ID, EventContract.EventColumns.KEY_DT_START,
                    EventContract.EventColumns.KEY_DT_END, EventContract.EventColumns.KEY_DESCRIPTION, EventContract.EventColumns.KEY_LOCATION,
                    EventContract.EventColumns.KEY_SUMMARY
            };
            String[] args_group = new String[]{GroupContract.Group.TABLE_NAME, GroupContract.GroupColumns._ID, GroupContract.GroupColumns.KEY_NAME};
            String sql_event = String.format("CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY, %3$s INTEGER, %4$s INTEGER, %5$s TEXT, %6$s TEXT, %7$s TEXT)", args_event);
            String sql_group = String.format("CREATE TABLE %1$s (%2$s INTEGER PRIMARY KEY, %3$s TEXT)", args_group);
            db.execSQL(sql_event);
            db.execSQL(sql_group);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion < newVersion){
                db.execSQL("DROP TABLE IF EXISTS " + EventContract.Event.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + GroupContract.Group.TABLE_NAME);
            }
            onCreate(db);
        }
    }
}
