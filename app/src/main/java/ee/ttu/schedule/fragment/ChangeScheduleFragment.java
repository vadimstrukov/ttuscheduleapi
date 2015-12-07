package ee.ttu.schedule.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.vadimstrukov.ttuschedule.R;

import java.util.HashMap;
import java.util.Map;

import ee.ttu.schedule.provider.GroupContract;
import ee.ttu.schedule.utils.SyncUtils;

public class ChangeScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener, TextWatcher {
    private ListView groupListView;
    private EditText groupEditText;
    private CursorAdapter groupCursorAdapter;

    private Map<Long, String> groupMap;

    private SyncUtils syncUtils;

    private static final String GROUP_FRAGMENT = "group_fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, new Bundle(), this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        syncUtils = new SyncUtils(getActivity());
        groupCursorAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, null,
                new String[]{GroupContract.GroupColumns.KEY_NAME}, new int[]{android.R.id.text1}, 0);
        groupListView.setAdapter(groupCursorAdapter);
        groupListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        groupListView.setMultiChoiceModeListener(this);
        groupListView.setOnItemClickListener(this);
        groupEditText.addTextChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_schedule, container, false);
        groupListView = (ListView) view.findViewById(R.id.groupListView);
        groupEditText = (EditText) view.findViewById(R.id.groupEditText);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = String.format("%1$s like ?", GroupContract.GroupColumns.KEY_NAME);
        String[] selectionArgs = new String[]{"%" + args.getString(GROUP_FRAGMENT, "") + "%"};
        return new CursorLoader(getActivity(), GroupContract.Group.CONTENT_URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        groupCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        groupCursorAdapter.changeCursor(null);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked)
            groupMap.put(id, groupCursorAdapter.getCursor().getString(1));
        else
            groupMap.remove(id);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        groupMap = new HashMap<>();
        mode.getMenuInflater().inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        for(Map.Entry<Long, String> entry : groupMap.entrySet()){
            syncUtils.syncEvents(entry.getValue());
        }
        mode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Bundle bundle = new Bundle();
        bundle.putString(GROUP_FRAGMENT, s.toString());
        getLoaderManager().restartLoader(0, bundle, this);
    }
}
