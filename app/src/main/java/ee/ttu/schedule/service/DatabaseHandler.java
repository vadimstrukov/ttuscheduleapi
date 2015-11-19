package ee.ttu.schedule.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import ee.ttu.schedule.model.Subject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;


/**
 * Created by vadimstrukov on 10/13/15.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "scheduleManager.db";
    private static final String TABLE_SCHEDULE = "schedule";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DT_START = "date_start";
    private static final String KEY_DT_END = "date_end";
    private static final String KEY_DESCR = "descr";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_SUMMARY = "summary";
    public static String databasePath = "";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        databasePath = context.getDatabasePath("scheduleManager.db").getPath();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCHEDULE_TABLE = "CREATE TABLE "
                + TABLE_SCHEDULE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_DT_START + " TEXT, "
                + KEY_DT_END + " TEXT, "
                + KEY_DESCR + " TEXT, "
                + KEY_LOCATION + " TEXT, "
                + KEY_SUMMARY + " TEXT);";
        db.execSQL(CREATE_SCHEDULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
    }

    public void addContact(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DT_START, String.valueOf(subject.getDateStart()));
        values.put(KEY_DT_END, String.valueOf(subject.getDateEnd()));
        values.put(KEY_DESCR, subject.getDescr());
        values.put(KEY_LOCATION, subject.getLocation());
        values.put(KEY_SUMMARY, subject.getSummary());


        db.insert(TABLE_SCHEDULE, null, values);
        db.close();
    }

    public Set<Subject> getAllSubjects() throws ParseException {

        Set<Subject> subjectList = new LinkedHashSet<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SCHEDULE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        if (cursor.moveToFirst()) {
            do {
                Subject subject = new Subject();
                subject.setID(Integer.parseInt(cursor.getString(0)));
                subject.setDateStart(sdf.parse(cursor.getString(1)));
                subject.setDateEnd(sdf.parse(cursor.getString(2)));
                subject.setDescr(cursor.getString(3));
                subject.setLocation(cursor.getString(4));
                subject.setSummary(cursor.getString(5));

                subjectList.add(subject);
            } while (cursor.moveToNext());
        }

        return subjectList;
    }


}
