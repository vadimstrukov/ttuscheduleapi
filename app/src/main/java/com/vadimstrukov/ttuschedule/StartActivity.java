package com.vadimstrukov.ttuschedule;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;
import com.vadimstrukov.ttuschedule.service.DatabaseHandler;
import com.vadimstrukov.ttuschedule.utils.ParseICSUtil;

import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.text.ParseException;


public class StartActivity extends BaseScheduleActivity {

    private static final String Url = "http://10.2.105.129:8080/schedule?group=";
    private SimpleTextRequest txtRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String group = getIntent().getStringExtra("group");
        txtRequest = new SimpleTextRequest(Url + group);

    }
    @Override
    protected void onStart() {
        super.onStart();
        getSpiceManager().execute(txtRequest, "txt", DurationInMillis.ONE_MINUTE,
                new TextRequestListener());
    }

    public final class TextRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(StartActivity.this, "failure", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final String result) {
            Toast.makeText(StartActivity.this, "success", Toast.LENGTH_SHORT).show();
            DatabaseHandler handler = new DatabaseHandler(StartActivity.this);
            try {
                if(handler.getAllSubjects().isEmpty()) {
                    ParseICSUtil parseICSUtil = new ParseICSUtil();
                    try {
                        parseICSUtil.getData(result, StartActivity.this);
                    } catch (IOException | ParseException | ParserException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(StartActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
