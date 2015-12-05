package ee.ttu.schedule;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.vadimstrukov.ttuschedule.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ee.ttu.schedule.service.adapter.SyncAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private Button getScheduleButton;
    private AutoCompleteTextView groupField;
    private TextInputLayout inputLayoutGroup;
    private View loading_panel;

    private AccountManager accountManager;
    private Account account;
    private Bundle syncBundle;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("status", 0)){
                case 200:
                    intent = new Intent(MainActivity.this, DrawerActivity.class);
                    startActivity(intent);
                    finish();
                default:
                    loading_panel.setVisibility(View.INVISIBLE);
            }
        }
    };
    private BroadcastReceiver networkBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
                if(groupValidate(groupField.getText().toString()))
                    inputLayoutGroup.setErrorEnabled(false);
            }
            else
                inputLayoutGroup.setError(getString(R.string.err_network));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account = new Account(getString(R.string.app_name), "ee.ttu.schedule");
        accountManager = (AccountManager) getApplicationContext().getSystemService(ACCOUNT_SERVICE);
        syncBundle = new Bundle();
        if (accountManager.addAccountExplicitly(account, null, null)) {
            ContentResolver.setIsSyncable(account, "ee.ttu.schedule", 1);
        }
        syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        syncBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        setContentView(R.layout.start_activity);
        groupField = (AutoCompleteTextView) findViewById(R.id.input_group);
//        groupField.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groupList));
        groupField.addTextChangedListener(this);
        inputLayoutGroup = (TextInputLayout) findViewById(R.id.input_layout_group);
        getScheduleButton = (Button) findViewById(R.id.btn_get);
        getScheduleButton.setOnClickListener(this);
        loading_panel = findViewById(R.id.loadingPanel);
        loading_panel.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("ee.ttu.schedule.SYNC_FINISHED"));
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
        syncBundle.putInt(SyncAdapter.SYNC_TYPE, SyncAdapter.SYNC_EVENTS);
        syncBundle.putString("group", groupField.getText().toString());
        loading_panel.setVisibility(View.VISIBLE);
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(groupField.getWindowToken(), 0);
        ContentResolver.requestSync(account, "ee.ttu.schedule", syncBundle);
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
}