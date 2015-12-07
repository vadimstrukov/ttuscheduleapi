package ee.ttu.schedule.service.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ee.ttu.schedule.model.Event;
import ee.ttu.schedule.provider.EventContract;
import ee.ttu.schedule.provider.GroupContract;
import ee.ttu.schedule.utils.Constants;

public class SyncAdapter extends AbstractThreadedSyncAdapter implements Response.Listener<JSONObject>, Response.ErrorListener {
    private final String TAG = this.getClass().getSimpleName();
    private static final String URL = "http://test-patla4.rhcloud.com/api/v1";

    private ContentProviderClient providerClient;

    public static final String SYNC_TYPE = "sync_type";
    public static final int SYNC_GROUPS = 0;
    public static final int SYNC_EVENTS = 1;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, final Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Syncing started");
        this.providerClient = provider;
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        switch (extras.getInt(SYNC_TYPE, 0)){
            case SYNC_GROUPS:
                JsonObjectRequest groupsRequest = new JsonObjectRequest(Request.Method.GET, String.format("%1$s/groups", URL), this, this);
                groupsRequest.setRetryPolicy(new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(groupsRequest);
                break;
            case SYNC_EVENTS:
                JsonObjectRequest eventRequest = new JsonObjectRequest(Request.Method.GET, String.format("%1$s/schedule?group=%2$s", URL, extras.get("group")), this, this);
                requestQueue.add(eventRequest);
                break;
        }
    }

    @Override
    public void onResponse(JSONObject response) {
        Gson gson = new Gson();
        scheduleAsyncQueryHandler asyncQueryHandler = new scheduleAsyncQueryHandler(getContext().getContentResolver());
        try {
            if(response.has("events")){
                Event[] events = gson.fromJson(response.getJSONArray("events").toString(), Event[].class);
                providerClient.delete(EventContract.Event.CONTENT_URI, null, null);
                for(Event event : events) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(EventContract.EventColumns.KEY_DT_START, event.getDateStart());
                    contentValues.put(EventContract.EventColumns.KEY_DT_END, event.getDateEnd());
                    contentValues.put(EventContract.EventColumns.KEY_DESCRIPTION, event.getDescription());
                    contentValues.put(EventContract.EventColumns.KEY_LOCATION, event.getLocation());
                    contentValues.put(EventContract.EventColumns.KEY_SUMMARY, event.getSummary());
                    asyncQueryHandler.startInsert(-1, null, EventContract.Event.CONTENT_URI, contentValues);
                }
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("group", response.getString("group")).commit();
            }
            else if(response.has("groups")){
                String[] groups = gson.fromJson(response.getJSONArray("groups").toString(), String[].class);
                providerClient.delete(GroupContract.Group.CONTENT_URI, null, null);
                for(String group_name : groups){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GroupContract.Group.KEY_NAME, group_name);
                    asyncQueryHandler.startInsert(-1, null, GroupContract.Group.CONTENT_URI, contentValues);
                }
            }
        }
        catch (JSONException | RemoteException  e) {
            e.printStackTrace();
        }
        finally {
            broadcastIntent(Constants.SYNC_STATUS_OK);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        broadcastIntent(Constants.SYNC_STATUS_FAILED);
    }

    private void broadcastIntent(int status){
        Intent intent = new Intent();
        intent.setAction("ee.ttu.schedule.SYNC_FINISHED");
        intent.putExtra(Constants.SYNC_STATUS, status);
        getContext().sendBroadcast(intent);
    }
    private static class scheduleAsyncQueryHandler extends AsyncQueryHandler {

        public scheduleAsyncQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }
    }
}