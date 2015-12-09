package ee.ttu.schedule.fragment;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.vadimstrukov.ttuschedule.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ee.ttu.schedule.calendar.CalendarController;
import ee.ttu.schedule.calendar.DayView;
import ee.ttu.schedule.calendar.EventLoader;
import ee.ttu.schedule.drawable.DayOfMonthDrawable;
import ee.ttu.schedule.provider.BaseContract;
import ee.ttu.schedule.utils.Constants;
import ee.ttu.schedule.utils.SyncUtils;

public class ScheduleFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SyncStatusObserver, ViewSwitcher.ViewFactory {

    private static final String ARG_TYPE = "arg_type";
    public static final int TYPE_DAY_VIEW = 1;
    public static final int TYPE_THREE_DAY_VIEW = 2;
    private int days;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Object syncObserverHandle;

    private ViewSwitcher viewSwitcher;
    private EventLoader eventLoader;

    private SyncUtils syncUtils;

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
        eventLoader = new EventLoader(getActivity());
        syncUtils = new SyncUtils(getActivity());
        if (getArguments() != null) {
            switch (getArguments().getInt(ARG_TYPE, TYPE_THREE_DAY_VIEW)) {
                case TYPE_DAY_VIEW:
                    days = 1;
                    break;
                case TYPE_THREE_DAY_VIEW:
                    days = 3;
                    break;
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);
        viewSwitcher = (ViewSwitcher) rootView.findViewById(R.id.viewSwitcher);
        viewSwitcher.setFactory(this);
        viewSwitcher.getCurrentView().requestFocus();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        syncObserverHandle = ContentResolver.addStatusChangeListener(mask, this);
        eventLoader.startBackgroundThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(syncObserverHandle !=null){
            ContentResolver.removeStatusChangeListener(syncObserverHandle);
            syncObserverHandle = null;
        }
        eventLoader.stopBackgroundThread();
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
                return true;
            case R.id.action_update:
                syncUtils.syncEvents(PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).getString(Constants.GROUP, null));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    public void onEventClick(WeekViewEvent event, RectF eventRect) {
//        String description = event.getDescription();
//        String location = event.getLocation();
//        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
//        DecimalFormat mFormat = new DecimalFormat("00");
//        mFormat.setRoundingMode(RoundingMode.DOWN);
//        alertDialog.setTitle(event.getName());
//        String dateStart = mFormat.format((double) event.getStartTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double) event.getStartTime().get(Calendar.MINUTE));
//        String dateEnd = mFormat.format((double) event.getEndTime().get(Calendar.HOUR_OF_DAY)) + ":" + mFormat.format((double) event.getEndTime().get(Calendar.MINUTE));
//        alertDialog.setMessage(dateStart + "--" + dateEnd + "\n" + description + "\n" + location);
//        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//        alertDialog.show();
//    }


//
//
//    private DateTimeInterpreter getDateTimeInterpreter(final boolean shortDate) {
//        return new DateTimeInterpreter() {
//            @Override
//            public String interpretDate(Calendar date) {
//                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
//                String weekday = weekdayNameFormat.format(date.getTime());
//                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());
//
//                if (shortDate)
//                    weekday = String.valueOf(weekday.charAt(0));
//                return weekday.toUpperCase() + format.format(date.getTime());
//            }
//
//            @Override
//            public String interpretTime(int hour) {
//                return hour + ":00";
//            }
//        };
//    }


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
    public void onRefresh() {

    }

    @Override
    public void onStatusChanged(int which) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NetworkInfo networkInfo = ((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if(networkInfo == null){
                    ContentResolver.cancelSync(syncUtils.getAccount(), BaseContract.CONTENT_AUTHORITY);
                    Toast.makeText(getActivity(), getActivity().getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                }
                boolean syncActive = ContentResolver.isSyncActive(syncUtils.getAccount(), BaseContract.CONTENT_AUTHORITY);
                boolean syncPending = ContentResolver.isSyncPending(syncUtils.getAccount(), BaseContract.CONTENT_AUTHORITY);
                swipeRefreshLayout.setRefreshing(syncActive || syncPending);
            }
        });
    }

    /**
     * Creates a new {@link View} to be added in a
     * {@link ViewSwitcher}.
     *
     * @return a {@link View}
     */
    @Override
    public View makeView() {
        DayView view = new DayView(getActivity(), CalendarController.getInstance(getActivity()), viewSwitcher, eventLoader, days);
        view.setLayoutParams(new ViewSwitcher.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setSelected(GregorianCalendar.getInstance(), false, false);
        return view;
    }
}
