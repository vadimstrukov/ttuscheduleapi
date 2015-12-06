package ee.ttu.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import com.vadimstrukov.ttuschedule.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.ttu.schedule.provider.GroupContract;
import ee.ttu.schedule.utils.Constants;
import ee.ttu.schedule.utils.SyncUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher, FilterQueryProvider, SimpleCursorAdapter.CursorToStringConverter {

    private Button getScheduleButton;
    private AutoCompleteTextView groupField;
    private TextInputLayout inputLayoutGroup;
    private View loading_panel;

    private SimpleCursorAdapter cursorAdapter;

    private SyncUtils syncUtils;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(Constants.SYNC_STATUS, -1)){
                case Constants.SYNC_STATUS_OK:
                    intent = new Intent(MainActivity.this, DrawerActivity.class);
                    startActivity(intent);
                    finish();
                case Constants.SYNC_STATUS_FAILED:
                    if(loading_panel != null)
                        loading_panel.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };
    private BroadcastReceiver networkBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
                if(groupField != null && groupValidate(groupField.getText().toString()))
                    inputLayoutGroup.setErrorEnabled(false);
            }
            else
                inputLayoutGroup.setError(getString(R.string.err_network));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        syncUtils = new SyncUtils(getApplicationContext());
        String group = PreferenceManager.getDefaultSharedPreferences(this).getString("group", null);
        if(group == null){
            syncUtils.syncGroups();
            setContentView(R.layout.start_activity);
            loading_panel = findViewById(R.id.loadingPanel);
            groupField = (AutoCompleteTextView) findViewById(R.id.input_group);
            cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                    new String[]{GroupContract.GroupColumns.KEY_NAME},new int[]{android.R.id.text1}, 0);
            cursorAdapter.setFilterQueryProvider(this);
            cursorAdapter.setCursorToStringConverter(this);
            groupField.setAdapter(cursorAdapter);
            groupField.addTextChangedListener(this);
            inputLayoutGroup = (TextInputLayout) findViewById(R.id.input_layout_group);
            getScheduleButton = (Button) findViewById(R.id.btn_get);
            getScheduleButton.setOnClickListener(this);
            loading_panel.setVisibility(View.INVISIBLE);
        }
        else {
            Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.SYNCHRONIZATION_ACTION));
        registerReceiver(networkBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(networkBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        loading_panel.setVisibility(View.VISIBLE);
        syncUtils.syncEvents(groupField.getText().toString());
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(groupField.getWindowToken(), 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(groupValidate(groupField.getText().toString())){
            inputLayoutGroup.setErrorEnabled(false);
        }
        else {
            if (!inputLayoutGroup.isErrorEnabled())
                inputLayoutGroup.setError(getString(R.string.err_msg_group));
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private Boolean groupValidate(String string){
        Matcher matcher = Pattern.compile("^[a-z][a-z][a-z][a-z][0-9][0-9]").matcher(string);
        return matcher.matches();
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        String sql = String.format("%1$s like ?", GroupContract.GroupColumns.KEY_NAME);
        String[] sqlArgs = new String[]{"%" + String.valueOf(constraint).toUpperCase() + "%"};
        return getContentResolver().query(GroupContract.Group.CONTENT_URI, null, sql, sqlArgs, null);
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(1);
    }
}