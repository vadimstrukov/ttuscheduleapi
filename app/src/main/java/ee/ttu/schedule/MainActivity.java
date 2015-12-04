package ee.ttu.schedule;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vadimstrukov.ttuschedule.R;

import ee.ttu.schedule.fragment.ConnectionFragment;
import ee.ttu.schedule.service.DatabaseHandler;

import java.text.ParseException;

/**
 * Created by vadimstrukov on 11/18/15.
 */
public class MainActivity extends AppCompatActivity {

    public static Button getschedule;
    private EditText edittext;
    private TextInputLayout inputLayoutGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHandler handler = new DatabaseHandler(this);

        try {
            if (handler.getAllSubjects().isEmpty()) {
                setContentView(R.layout.start_activity);
                edittext = (EditText) findViewById(R.id.input_group);
                inputLayoutGroup = (TextInputLayout) findViewById(R.id.input_layout_group);
                getschedule = (Button) findViewById(R.id.btn_get);
                edittext.addTextChangedListener(new MyTextWatcher(edittext));
                getschedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submitForm();
                    }
                });

            } else {
                startActivity(DrawerActivity.class, "Welcome!");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void startActivity(Class<?> cls, String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
        finish();
    }

    private void submitForm() {
        if (!validateName()) {
            return;
        }
        InputMethodManager imm=
                (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);

        Toast.makeText(getApplicationContext(), "Schedule loading...", Toast.LENGTH_SHORT).show();
        String group = edittext.getText().toString().toUpperCase();
        Bundle data = new Bundle();
        data.putString("group", group);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        ConnectionFragment fragment = new ConnectionFragment();
        fragment.setArguments(data);
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }


    private boolean validateName() {
        if (edittext.getText().toString().trim().isEmpty()) {
            inputLayoutGroup.setError(getString(R.string.err_msg_group));
            requestFocus(edittext);
            return false;
        } else if(!isOnline()){
            inputLayoutGroup.setError(getString(R.string.err_network));
            requestFocus(edittext);
            return false;
        }
        else {
            inputLayoutGroup.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_group:
                    validateName();
                    break;
            }
        }
    }
}
