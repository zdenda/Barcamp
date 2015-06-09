package eu.zkkn.android.barcamp.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import eu.zkkn.android.barcamp.Config;
import eu.zkkn.android.barcamp.Preferences;
import eu.zkkn.android.barcamp.R;

/**
 *
 */
public abstract class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_sessions_notifications)
                .setChecked(Preferences.isSessionsNotificationsEnabled(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_refresh:
                onRefresh(true);
                return true;

            case R.id.action_sessions_notifications:
                Preferences.setSessionsNotificationsEnabled(this, !item.isChecked());
                onSettingsChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // when some settings is changed in the Menu, we might need to update state of some controls
    // in current activity to reflect that change
    protected void onSettingsChanged() {
        invalidateOptionsMenu();
    }

    protected void showError(int errorCode) {
        // log error code
        if (Config.DEBUG) Log.d(Config.TAG, "ERROR (code: "+ errorCode +")");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_error_title))
                .setMessage(getString(R.string.alert_error_message, errorCode))
                .setPositiveButton(getString(R.string.alert_error_button_try_again),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRefresh(true);
                    }
                })
                .setNegativeButton(getString(R.string.alert_error_button_cancel),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    protected abstract void onRefresh(boolean forceApiReload);

}
