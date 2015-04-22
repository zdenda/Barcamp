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
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    private static final String PREF_APP_VERSION = "appVersion";
    private static final String PREF_REG_ID = "gcmRegistrationId";

    private Data mData;
    private ListView mSessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPlayServices()) {
            if (getGcmRegistrationId().length() == 0) {
                registerGcmInBackground();
            }
        }


        mData = new Data(this);
        mSessions = (ListView) findViewById(R.id.lv_sessions);
        mSessions.setEmptyView(findViewById(R.id.progressbar));
        mSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    }

    @Override
    protected void onStart() {
        super.onStart();
        onRefresh(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mData.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            onRefresh(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRefresh(boolean forceReload) {
        mData.getSessions(new Data.Listener<Cursor>() {
            @Override
            public void onData(Cursor data) {
                GroupsCursorAdapter adapter = new GroupsCursorAdapter(MainActivity.this,
                        R.layout.row_session, data);
                mSessions.setAdapter(adapter);
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                if (Config.DEBUG) Log.d(Config.TAG, errorMsg);
            }
        }, forceReload);
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
        if (registrationId.length() == 0) {
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
