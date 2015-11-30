package ee.ttu.schedule;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;
import com.vadimstrukov.ttuschedule.R;

import ee.ttu.schedule.service.DatabaseHandler;
import ee.ttu.schedule.utils.Constans;
import ee.ttu.schedule.utils.ParseICSUtil;
import net.fortuna.ical4j.data.ParserException;
import java.io.IOException;
import java.text.ParseException;


public class StartActivity extends BaseScheduleActivity {

    private SimpleTextRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        String group = getIntent().getStringExtra("group");
        request = new SimpleTextRequest(Constans.URL + group);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSpiceManager().execute(request, "txt", DurationInMillis.ONE_MINUTE,
                new TextRequestListener());
    }

    private final class TextRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {

            Toast.makeText(StartActivity.this, "failure", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

        @Override
        public void onRequestSuccess(final String result) {

            Toast.makeText(StartActivity.this, "success", Toast.LENGTH_SHORT).show();
            DatabaseHandler handler = new DatabaseHandler(StartActivity.this);
            try {
                if(handler.getAllSubjects().isEmpty()) {
                    ParseICSUtil parseICSUtil = new ParseICSUtil();
                    parseICSUtil.getData(result, StartActivity.this);
                    Intent intent = new Intent(StartActivity.this, ScheduleActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (IOException | ParseException | ParserException e) {
                e.printStackTrace();
            }
        }
    }
}
