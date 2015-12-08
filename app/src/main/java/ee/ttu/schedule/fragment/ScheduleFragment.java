package ee.ttu.schedule.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.vadimstrukov.ttuschedule.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import ee.ttu.schedule.drawable.DayOfMonthDrawable;
import ee.ttu.schedule.provider.EventContract;

public class ScheduleFragment extends Fragment implements WeekView.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TYPE_DAY_VIEW = 1;
    public static final int TYPE_THREE_DAY_VIEW = 2;
    private static final String ARG_TYPE = "arg_type";
    private int WEEK_TYPE;

    private Map<Integer, List<WeekViewEvent>> map;

    private WeekView mWeekView;
    private String[] colorArray;

    public ScheduleFragment() {

    }

    public static ScheduleFragment newInstance(int type) {
        Bundle args = new Bundle();
        ScheduleFragment scheduleFragment = new ScheduleFragment();
        args.putInt(ARG_TYPE, type);
        scheduleFragment.setArguments(args);
        return scheduleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map = new HashMap<>();
        syncUtils = new SyncUtils(getActivity());
        colorArray = getResources().getStringArray(R.array.colors);
        if (getArguments() != null)
            WEEK_TYPE = getArguments().getInt(ARG_TYPE, TYPE_THREE_DAY_VIEW);
        setHasOptionsMenu(true);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        mWeekView = (WeekView) rootView.findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setDateTimeInterpreter(getDateTimeInterpreter(WEEK_TYPE == TYPE_THREE_DAY_VIEW));
        mWeekView.goToHour(8);
        switch (WEEK_TYPE) {
            case TYPE_DAY_VIEW:
                mWeekView.setNumberOfVisibleDays(1);
                break;
            case TYPE_THREE_DAY_VIEW:
                mWeekView.setNumberOfVisibleDays(3);
                break;
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_today);
        setTodayIcon((LayerDrawable) menuItem.getIcon(), getActivity(), "EET");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                mWeekView.goToToday();
                mWeekView.goToHour(8);
                return true;
            case R.id.action_update:
                syncUtils.syncEvents(PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).getString(Constants.GROUP, null));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        String description = event.getDescription();
        String location = event.getLocation();
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        DecimalFormat mFormat = new DecimalFormat("00");
        mFormat.setRoundingMode(RoundingMode.DOWN);
        alertDialog.setTitle(event.getName());
        String dateStart = mFormat.format((double) event.getStartTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double) event.getStartTime().get(Calendar.MINUTE));
        String dateEnd = mFormat.format((double) event.getEndTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double) event.getEndTime().get(Calendar.MINUTE));
        alertDialog.setMessage(dateStart + "--" + dateEnd + "\n" + description + "\n" + location);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        if (map.size() > 0 && map.containsKey(newMonth)) {
            return map.get(newMonth);
        }
        return new ArrayList<>();
    }


    private DateTimeInterpreter getDateTimeInterpreter(final boolean shortDate) {
        return new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour + ":00";
            }
        };
    }


    private void setTodayIcon(LayerDrawable icon, Context context, String timezone) {
        DayOfMonthDrawable today;
        // Reuse current drawable if possible
        Drawable currentDrawable = icon.findDrawableByLayerId(R.id.today_icon_day);
        if (currentDrawable != null && currentDrawable instanceof DayOfMonthDrawable) {
            today = (DayOfMonthDrawable) currentDrawable;
        } else {
            today = new DayOfMonthDrawable(context);
        }
        // Set the day and update the icon
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getDefault());
        today.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        icon.mutate();
        icon.setDrawableByLayerId(R.id.today_icon_day, today);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String orderBy = EventContract.EventColumns.KEY_DT_START + " ASC";
        return new CursorLoader(getActivity(), EventContract.Event.CONTENT_URI, null, null, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        map = new HashMap<>();
        if (data.moveToFirst()) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTimeInMillis(data.getLong(1));
            int temp = calendar.get(Calendar.MONTH)+1;
            List<WeekViewEvent> events = new ArrayList<>();
            do {
                calendar.setTimeInMillis(data.getLong(1));
                if ((calendar.get(Calendar.MONTH) +1) != temp || data.isAfterLast()) {
                    map.put(temp, events);
                    events = new ArrayList<>();
                }
                Calendar startTime = GregorianCalendar.getInstance();
                Calendar endTime = GregorianCalendar.getInstance();
                startTime.setTime(new Date(data.getLong(1)));
                endTime.setTime(new Date(data.getLong(2)));
                WeekViewEvent event = new WeekViewEvent(data.getInt(0), data.getString(5), data.getString(3), data.getString(4), startTime, endTime);
                event.setColor(Color.parseColor(colorArray[new Random().nextInt(colorArray.length)]));
                temp = calendar.get(Calendar.MONTH)+1;
                events.add(event);
                if(data.isLast())
                    map.put(temp, events);
            }
            while (data.moveToNext());
        }
        mWeekView.notifyDatasetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
