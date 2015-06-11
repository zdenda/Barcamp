package eu.zkkn.android.barcamp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import eu.zkkn.android.barcamp.Config;
import eu.zkkn.android.barcamp.DataObject;
import eu.zkkn.android.barcamp.adapter.GroupsCursorAdapter;
import eu.zkkn.android.barcamp.R;
import eu.zkkn.android.barcamp.loader.ApiLoadInterface;
import eu.zkkn.android.barcamp.loader.CursorDataApiLoader;


public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<DataObject<Cursor>> {

    static final int TOGGLE_ALARM_REQUEST = 0;

    private static final String PREF_LAST_API_SYNC = "lastApiSyncTimeMs";
    private static final int LOADER_SESSIONS_ID = 0;

    private GroupsCursorAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_sessions);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        getSupportLoaderManager().initLoader(LOADER_SESSIONS_ID, null, this);

        // check whether we should synchronize our data with API
        SharedPreferences pref = getDefaultSharedPreferences();
        long lastSync = pref.getLong(PREF_LAST_API_SYNC, 0);
        long now = System.currentTimeMillis();
        if (now > (lastSync + Config.API_SYNC_INTERVAL_MS)) {
            pref.edit().putLong(PREF_LAST_API_SYNC, now).commit();
            onRefresh(true);
        }

        ListView sessions = (ListView) findViewById(R.id.lv_sessions);
        sessions.setEmptyView(findViewById(R.id.progressbar));
        sessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int sessionId = (int) view.getTag();
                if (sessionId > 0) {
                    Intent intent = new Intent(MainActivity.this, SessionDetailActivity.class);
                    intent.putExtra(SessionDetailActivity.SESSION_ID, sessionId);
                    startActivityForResult(intent, TOGGLE_ALARM_REQUEST);
                }
            }
        });

        mAdapter = new GroupsCursorAdapter(this, R.layout.row_session, null);
        sessions.setAdapter(mAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if alarm was changed, refresh actual data from database
        if (requestCode == TOGGLE_ALARM_REQUEST && resultCode == RESULT_OK) {
            onRefresh(false);
        }
    }

    @Override
    protected void onRefresh(boolean forceApiReload) {
        if (Config.DEBUG) Log.d(Config.TAG, "MainActivity.onRefresh()" + (forceApiReload ? "FORCE" : ""));
        mSwipeRefreshLayout.setRefreshing(true);
        Loader loader = getSupportLoaderManager().getLoader(LOADER_SESSIONS_ID);
        if (forceApiReload) {
            ((ApiLoadInterface) loader).loadFromApi(true);
        } else {
            loader.forceLoad();
        }
    }

    @Override
    public void onRefresh() {
        onRefresh(true);
    }

    @Override
    public Loader<DataObject<Cursor>> onCreateLoader(int id, Bundle args) {
        return new CursorDataApiLoader(this) {
            @Override
            public DataObject<Cursor> loadInBackground() {
                Cursor sessions = getDatabase().getSessions();
                //if there are no sessions in the database, load them from API
                if (sessions.getCount() == 0) {
                    if (Config.DEBUG) Log.d(Config.TAG, "No sessions in DB, try load from API");
                    loadFromApi(false);
                }
                return new DataObject<>(sessions);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<DataObject<Cursor>> loader, DataObject<Cursor> data) {
        if (data.hasError()) {
            showError(data.getAndResetErrorCode());
        }
        mAdapter.swapCursor(data.getData());
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<DataObject<Cursor>> loader) {
        mAdapter.swapCursor(null);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
}
