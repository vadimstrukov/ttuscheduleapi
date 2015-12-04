package ee.ttu.schedule.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import ee.ttu.schedule.model.Subject;
import ee.ttu.schedule.service.DatabaseHandler;

public class ScheduleFragment extends Fragment implements WeekView.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener {

    public static Set<Subject> subjects;
    private String[] colorArray;
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getAllSubjects();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_main, container, false);
        mWeekView = (WeekView) rootView.findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        mWeekView.goToHour(8);
        setupDateTimeInterpreter(false);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.action_three_day_view);
        switch (id) {
            case R.id.action_today:
                mWeekView.goToToday();
                mWeekView.goToHour(8);
                return true;
            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);
                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.goToHour(8);
                }
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);
                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.goToHour(8);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        DecimalFormat mFormat= new DecimalFormat("00");
        mFormat.setRoundingMode(RoundingMode.DOWN);
        alertDialog.setTitle(event.getName());
        String dateStart = mFormat.format((double)event.getStartTime().get(Calendar.HOUR_OF_DAY)) + ":" +mFormat.format((double)event.getStartTime().get(Calendar.MINUTE));
        String dateEnd = mFormat.format((double)event.getEndTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double)event.getEndTime().get(Calendar.MINUTE));
        String descr = event.getDescr();
        String location = event.getLocation();
        alertDialog.setMessage(dateStart + "--" + dateEnd + "\n" + descr + "\n" + location);
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
        List<WeekViewEvent> events = new LinkedList<>();
        long subjectIndex = 0;
        if (subjects != null) {
            for (Subject subject : subjects) {

                subjectIndex++;

                Calendar startTime = GregorianCalendar.getInstance();
                Calendar endTime = GregorianCalendar.getInstance();

                startTime.setTime(new Date(subject.getDateStart()));
                endTime.setTime(new Date(subject.getDateEnd()));


                WeekViewEvent event = new WeekViewEvent(subjectIndex, subject.getSummary(), subject.getDescription(), subject.getLocation(), startTime, endTime);
                event.setColor(Color.parseColor(colorArray[new Random().nextInt(colorArray.length)]));
                events.add(event);
            }
        }
        return events;
    }

    private void getAllSubjects(){
        DatabaseHandler handler = new DatabaseHandler(getActivity());
        try {
            subjects = handler.getAllSubjects();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        colorArray = getActivity().getApplicationContext().getResources().getStringArray(R.array.colors);
    }

    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
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
        });
    }
}
