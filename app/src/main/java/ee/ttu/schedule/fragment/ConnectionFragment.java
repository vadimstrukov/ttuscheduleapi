package ee.ttu.schedule.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;
import com.vadimstrukov.ttuschedule.R;

import net.fortuna.ical4j.data.ParserException;

import java.io.IOException;
import java.text.ParseException;

import ee.ttu.schedule.DrawerActivity;
import ee.ttu.schedule.service.DatabaseHandler;
import ee.ttu.schedule.utils.Constants;
import ee.ttu.schedule.utils.ParseICSUtil;

/**
 * Created by vadimstrukov on 12/2/15.
 */
public class ConnectionFragment extends BaseScheduleSpiceFragment {
    private SimpleTextRequest request;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        String group = bundle.getString("group");
        request = new SimpleTextRequest(Constants.URL + group);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.loading_activity, container, false);
        rootView.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getSpiceManager().execute(request, "txt", DurationInMillis.ONE_MINUTE,
                new TextRequestListener());
    }


    private final class TextRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getActivity(), "Failure!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccess(final String result) {
            DatabaseHandler handler = new DatabaseHandler(getActivity());
            try {
                if (handler.getAllSubjects().isEmpty()) {
                    ParseICSUtil parseICSUtil = new ParseICSUtil();
                    parseICSUtil.getData(result, getActivity());
                    Intent intent = new Intent(getActivity(), DrawerActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            } catch (IOException | ParseException | ParserException e) {
                e.printStackTrace();
            }
        }
    }
}