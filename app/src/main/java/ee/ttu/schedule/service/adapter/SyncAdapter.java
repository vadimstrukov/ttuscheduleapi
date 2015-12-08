package ee.ttu.schedule.service.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ee.ttu.schedule.model.Event;
import ee.ttu.schedule.provider.EventContract;
import ee.ttu.schedule.provider.GroupContract;
import ee.ttu.schedule.utils.Constants;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final String TAG = this.getClass().getSimpleName();
    private static final String URL = "http://test-patla4.rhcloud.com/api/v1";

    private static final int TIMEOUT = 15000;

    public static final String SYNC_TYPE = "sync_type";
    public static final int SYNC_GROUPS = 0;
    public static final int SYNC_EVENTS = 1;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    @Override
    public void onPerformSync(Account account, final Bundle extras, String authority, final ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Syncing started");
        final int sync_type = extras.getInt(SYNC_TYPE, -1);
        ArrayList<ContentProviderOperation> operations;
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        try {
            switch (sync_type) {
                case SYNC_GROUPS:
                    JsonObjectRequest groupsRequest = new JsonObjectRequest(Request.Method.GET, String.format("%1$s/groups", URL), future, future);
                    groupsRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(groupsRequest);
                    operations = getGroups(future.get(), provider, syncResult);

                    break;
                case SYNC_EVENTS:
                    JsonObjectRequest eventRequest = new JsonObjectRequest(Request.Method.GET, String.format("%1$s/schedule?group=%2$s", URL, extras.get("group")), future, future);
                    eventRequest.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(eventRequest);
                    operations = getEvents(future.get(), provider, syncResult);
                    break;
                default:
                    return;
            }
            provider.applyBatch(operations);
            broadcastIntent(Constants.SYNC_STATUS_OK);
        }
        catch (RemoteException | JSONException | InterruptedException | ExecutionException | OperationApplicationException e) {
            broadcastIntent(Constants.SYNC_STATUS_FAILED);
        }
    }

    private ArrayList<ContentProviderOperation> getEvents(JSONObject jsonObject, ContentProviderClient providerClient, SyncResult syncResult) throws RemoteException, JSONException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        Gson gson = new Gson();
        Event[] events = gson.fromJson(jsonObject.getJSONArray("events").toString(), Event[].class);
        providerClient.delete(EventContract.Event.CONTENT_URI, null, null);
        for (Event event : events) {
            operations.add(ContentProviderOperation.newInsert(EventContract.Event.CONTENT_URI)
                    .withValue(EventContract.EventColumns.KEY_DT_START, event.getDateStart())
                    .withValue(EventContract.EventColumns.KEY_DT_END, event.getDateEnd())
                    .withValue(EventContract.EventColumns.KEY_DESCRIPTION, event.getDescription())
                    .withValue(EventContract.EventColumns.KEY_LOCATION, event.getLocation())
                    .withValue(EventContract.EventColumns.KEY_SUMMARY, event.getSummary()).build());
            syncResult.stats.numInserts++;
        }
        return operations;
    }

    private ArrayList<ContentProviderOperation> getGroups(JSONObject jsonObject, ContentProviderClient providerClient, SyncResult syncResult) throws RemoteException, JSONException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        Gson gson = new Gson();
        String[] groups = gson.fromJson(jsonObject.getJSONArray("groups").toString(), String[].class);
        providerClient.delete(GroupContract.Group.CONTENT_URI, null, null);
        for (String group_name : groups) {
            operations.add(ContentProviderOperation.newInsert(GroupContract.Group.CONTENT_URI)
                    .withValue(GroupContract.Group.KEY_NAME, group_name).build());
            syncResult.stats.numInserts++;
        }
        return operations;
    }

    private void broadcastIntent(int status) {
        Intent intent = new Intent();
        intent.setAction(Constants.SYNCHRONIZATION_ACTION);
        intent.putExtra(Constants.SYNC_STATUS, status);
        getContext().sendBroadcast(intent);
    }
}