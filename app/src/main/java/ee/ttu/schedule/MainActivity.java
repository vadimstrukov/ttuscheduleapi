package ee.ttu.schedule;

import android.app.ProgressDialog;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FilterQueryProvider, SimpleCursorAdapter.CursorToStringConverter {

    private Button getScheduleButton;
    private AutoCompleteTextView groupField;
    private TextInputLayout inputLayoutGroup;
    private ProgressDialog progressDialog;

    private SimpleCursorAdapter cursorAdapter;

    private SyncUtils syncUtils;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(Constants.SYNC_STATUS, -1)) {
                case Constants.SYNC_STATUS_OK:
                    intent = new Intent(MainActivity.this, DrawerActivity.class);
                    startActivity(intent);
                    finish();
            }
            if(getScheduleButton != null){
                getScheduleButton.setEnabled(true);
                progressDialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        syncUtils = new SyncUtils(getApplicationContext());
        String group = PreferenceManager.getDefaultSharedPreferences(this).getString("group", null);
        if (group == null) {
            syncUtils.syncGroups();
            setContentView(R.layout.start_activity);
            groupField = (AutoCompleteTextView) findViewById(R.id.input_group);
            cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                    new String[]{GroupContract.GroupColumns.KEY_NAME}, new int[]{android.R.id.text1}, 0);
            cursorAdapter.setFilterQueryProvider(this);
            cursorAdapter.setCursorToStringConverter(this);
            groupField.setAdapter(cursorAdapter);
            inputLayoutGroup = (TextInputLayout) findViewById(R.id.input_layout_group);
            getScheduleButton = (Button) findViewById(R.id.btn_get);
            getScheduleButton.setOnClickListener(this);
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
        if (groupValidate(groupField.getText().toString())) {
            inputLayoutGroup.setErrorEnabled(false);
            getScheduleButton.setEnabled(false);
            progressDialog = ProgressDialog.show(this, getString(R.string.loading_title), getString(R.string.loading_message), true);
            syncUtils.syncEvents(groupField.getText().toString());
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(groupField.getWindowToken(), 0);
        }
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