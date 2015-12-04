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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.vadimstrukov.ttuschedule.R;

import org.json.JSONArray;
import org.json.JSONException;

import ee.ttu.schedule.fragment.ConnectionFragment;
import ee.ttu.schedule.service.DatabaseHandler;
import ee.ttu.schedule.utils.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vadimstrukov on 11/18/15.
 */
public class MainActivity extends AppCompatActivity {

    public static Button getScheduleButton;
    private AutoCompleteTextView groupField;
    private TextInputLayout inputLayoutGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final List<String> groupList = new ArrayList<>();
        DatabaseHandler handler = new DatabaseHandler(this);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsObjRequest = new JsonArrayRequest(Request.Method.GET, Constants.URL + "/groups",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                groupList.add(response.get(i).toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsObjRequest);
        try {
            if (handler.getAllSubjects().isEmpty()) {
                setContentView(R.layout.start_activity);
                groupField = (AutoCompleteTextView) findViewById(R.id.input_group);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groupList);
                groupField.setAdapter(adapter);
                inputLayoutGroup = (TextInputLayout) findViewById(R.id.input_layout_group);
                getScheduleButton = (Button) findViewById(R.id.btn_get);
                groupField.addTextChangedListener(new MyTextWatcher(groupField));
                getScheduleButton.setOnClickListener(new View.OnClickListener() {
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
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(groupField.getWindowToken(), 0);

        Toast.makeText(getApplicationContext(), "Schedule loading...", Toast.LENGTH_SHORT).show();
        String group = groupField.getText().toString().toUpperCase();
        Bundle data = new Bundle();
        data.putString("group", group);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        ConnectionFragment fragment = new ConnectionFragment();
        fragment.setArguments(data);
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }


    private boolean validateName() {
        if (groupField.getText().toString().trim().isEmpty()) {
            inputLayoutGroup.setError(getString(R.string.err_msg_group));
            requestFocus(groupField);
            return false;
        } else if (!isOnline()) {
            inputLayoutGroup.setError(getString(R.string.err_network));
            requestFocus(groupField);
            return false;
        } else {
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
