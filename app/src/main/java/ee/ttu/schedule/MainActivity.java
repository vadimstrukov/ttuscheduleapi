package ee.ttu.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vadimstrukov.ttuschedule.R;

import ee.ttu.schedule.service.DatabaseHandler;

import java.text.ParseException;

/**
 * Created by vadimstrukov on 11/18/15.
 */
public class MainActivity extends AppCompatActivity {
    private EditText edittext;
    private Button getschedule;
    private TextInputLayout inputLayoutGroup;
    private String group;

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
                startActivity(ScheduleActivity.class, "Welcome!");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        Toast.makeText(getApplicationContext(), "Thank You! Schedule loading...", Toast.LENGTH_SHORT).show();
        group = edittext.getText().toString().toUpperCase();
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        intent.putExtra("group", group);
        startActivity(intent);
        finish();
    }

    private boolean validateName() {
        if (edittext.getText().toString().trim().isEmpty()) {
            inputLayoutGroup.setError(getString(R.string.err_msg_group));
            requestFocus(edittext);
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
