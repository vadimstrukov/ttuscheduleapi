package ee.ttu.schedule.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;
import com.vadimstrukov.ttuschedule.R;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ee.ttu.schedule.DrawerActivity;
import ee.ttu.schedule.model.Subject;
import ee.ttu.schedule.service.DatabaseHandler;
import ee.ttu.schedule.utils.Constants;
import ee.ttu.schedule.utils.ParseICSUtil;

public class ConnectionFragment extends BaseScheduleSpiceFragment {
    private SimpleTextRequest request;
    private View loadingPanel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = new SimpleTextRequest(String.format("%1$s/schedule?groups=%2$s", Constants.URL, getArguments().getString("group")));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.loading_activity, container, false);
        loadingPanel = rootView.findViewById(R.id.loadingPanel);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getSpiceManager().execute(request, "txt", DurationInMillis.ONE_MINUTE,
                new TextRequestListener());
        loadingPanel.setVisibility(View.VISIBLE);
    }


    private final class TextRequestListener implements RequestListener<String> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Toast.makeText(getActivity(), "Failure!", Toast.LENGTH_SHORT).show();
            loadingPanel.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onRequestSuccess(final String result) {
            Map<String, List<Subject>> subjectMap = new GsonBuilder().create().fromJson(result, new TypeToken<Map<String, List<Subject>>>(){}.getType());
            DatabaseHandler handler = new DatabaseHandler(getActivity());
            try {
                if (handler.getAllSubjects().isEmpty()) {
                    ParseICSUtil parseICSUtil = new ParseICSUtil();
                    parseICSUtil.getData(subjectMap.get(getArguments().getString("group")), getActivity());
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