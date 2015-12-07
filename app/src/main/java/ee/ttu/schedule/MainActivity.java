package ee.ttu.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.vadimstrukov.ttuschedule.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.ttu.schedule.provider.GroupContract;
import ee.ttu.schedule.utils.Constants;
import ee.ttu.schedule.utils.SyncUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FilterQueryProvider, SimpleCursorAdapter.CursorToStringConverter {

    private Button getScheduleButton;
    private AutoCompleteTextView groupField;
    private TextInputLayout inputLayoutGroup;
    private View loading_panel;
    private SimpleCursorAdapter cursorAdapter;
    private SyncUtils syncUtils;
    private ProgressBar progBar;
    private Handler mHandler = new Handler();
    private int mProgressStatus = 0;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(Constants.SYNC_STATUS, -1)) {
                case Constants.SYNC_STATUS_OK:
                    intent = new Intent(MainActivity.this, DrawerActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case Constants.SYNC_STATUS_FAILED:
                    if (loading_panel != null) {
                        loading_panel.setVisibility(View.INVISIBLE);
                        getScheduleButton.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        syncUtils = new SyncUtils(getApplicationContext());
        String group = PreferenceManager.getDefaultSharedPreferences(this).getString("group", null);
        if (group == null) {
            syncUtils.syncGroups();
            setContentView(R.layout.start_activity);
            loading_panel = findViewById(R.id.loadingPanel);
            groupField = (AutoCompleteTextView) findViewById(R.id.input_group);
            cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                    new String[]{GroupContract.GroupColumns.KEY_NAME}, new int[]{android.R.id.text1}, 0);
            cursorAdapter.setFilterQueryProvider(this);
            cursorAdapter.setCursorToStringConverter(this);
            groupField.setAdapter(cursorAdapter);
            inputLayoutGroup = (TextInputLayout) findViewById(R.id.input_layout_group);
            getScheduleButton = (Button) findViewById(R.id.btn_get);
            progBar = (ProgressBar) findViewById(R.id.progressBar);
            getScheduleButton.setOnClickListener(this);
            loading_panel.setVisibility(View.INVISIBLE);
        } else {
            Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.SYNCHRONIZATION_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        progBar.setProgress(0);
        if (groupValidate(groupField.getText().toString())) {
            inputLayoutGroup.setErrorEnabled(false);
            getScheduleButton.setVisibility(View.INVISIBLE);
            loading_panel.setVisibility(View.VISIBLE);
            syncUtils.syncEvents(groupField.getText().toString());
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(groupField.getWindowToken(), 0);
            updateProgressBar();
        }
    }

    public void updateProgressBar() {
        new Thread(new Runnable() {
            public void run() {
                while (mProgressStatus < 100) {
                    mProgressStatus += 10;
                    mHandler.post(new Runnable() {
                        public void run() {
                            progBar.setProgress(mProgressStatus);
                        }
                    });
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean groupValidate(String string) {
        NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        Matcher matcher = Pattern.compile("^[A-Za-z][A-Za-z][A-Za-z][A-Za-z][0-9][0-9]").matcher(string);
        if (!matcher.matches()) {
            inputLayoutGroup.setError(getString(R.string.err_msg_group));
            return false;
        } else if (networkInfo == null) {
            inputLayoutGroup.setError(getString(R.string.err_network));
            return false;
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return true;
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        String sql = String.format("%1$s like ?", GroupContract.GroupColumns.KEY_NAME);
        String[] sqlArgs = new String[]{"%" + String.valueOf(constraint).toUpperCase() + "%"};
        groupField.performValidation();
        return getContentResolver().query(GroupContract.Group.CONTENT_URI, null, sql, sqlArgs, null);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(1);
    }
}