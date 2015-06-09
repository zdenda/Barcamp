package eu.zkkn.android.barcamp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import eu.zkkn.android.barcamp.loader.ApiLoadInterface;
import eu.zkkn.android.barcamp.loader.CursorDataApiLoader;


public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<DataObject<Cursor>> {

    private static final String PREF_APP_VERSION = "appVersion";
    private static final String PREF_REG_ID = "gcmRegistrationId";
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

        if (checkPlayServices()) {
            if (getGcmRegistrationId().length() == 0) {
                registerGcmInBackground();
            }
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
                    startActivity(intent);
                }
            }
        });

        mAdapter = new GroupsCursorAdapter(this, R.layout.row_session, null);
        sessions.setAdapter(mAdapter);

    }

    @Override
    protected void onRefresh(boolean forceApiReload) {
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
    protected void showError(int errorCode) {
        //TODO: display proper error message
        Toast.makeText(this, "Error: " + errorCode, Toast.LENGTH_LONG).show();
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


    /**
     * Check the device to make sure it has the Google Play Services APK
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (Config.DEBUG) Log.d(Config.TAG, "Google Play Services APK is missing");
            return false;
        }
        return true;
    }

    private void registerGcmInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = null;
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    String regId = gcm.register(Config.GCM_SENDER_ID);
                    if (Config.DEBUG) Log.d(Config.TAG, "Device registered, ID=" + regId);

                    if (regId != null && regId.length() > 0) {
                        result = regId;
                        // Persist the regID - no need to register again.
                        storeGcmRegistrationId(regId);
                        // You should send the registration ID to your server over HTTP, so it
                        // can use GCM/HTTP or CCS to send messages to your app.
                        sendGcmRegistrationIdToBackend();
                    }

                } catch (IOException ex) {
                    if (Config.DEBUG) Log.d(Config.TAG, "GCM Registration Error:" + ex.getMessage());
                }
                return result;
            }

            @Override
            protected void onPostExecute(String regId) {
                // share your ID instead sending it to the server
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, regId == null ?
                        "Chyba - Registrace neproběhla" : regId);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        }.execute();
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeGcmRegistrationId(String regId) {
        final SharedPreferences prefs = getDefaultSharedPreferences();
        int appVersion = getAppVersion();
        if (Config.DEBUG) Log.d(Config.TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_REG_ID, regId);
        editor.putInt(PREF_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing registration ID.
     */
    private String getGcmRegistrationId() {
        final SharedPreferences prefs = getDefaultSharedPreferences();
        String registrationId = prefs.getString(PREF_REG_ID, "");
        if (registrationId != null && registrationId.length() == 0) {
            if (Config.DEBUG) Log.d(Config.TAG, "GCM Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID since
        // the existing registration ID is not guaranteed to work with the new app version.
        int registeredVersion = prefs.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            if (Config.DEBUG) Log.d(Config.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void sendGcmRegistrationIdToBackend() {
        //TODO: send GCM Registration ID to our server
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            Context context = getApplicationContext();
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }
}
