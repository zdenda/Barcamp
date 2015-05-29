package eu.zkkn.android.barcamp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 *
 */
public abstract class BaseActivity extends ActionBarActivity {

    /** @deprecated */
    protected Data mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mData = new Data(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mData.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_gcm_notifications)
                .setChecked(Preferences.isGcmNotificationsEnabled(this));
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

            case R.id.action_gcm_notifications:
                Preferences.setGcmNotificationsEnabled(this, !item.isChecked());
                onSettingsChanged();
                return true;

            case R.id.action_gcm_notifications_activity:
                Intent intent = new Intent(this, GcmNotificationsActivity.class);
                startActivity(intent);
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
        // if nothing else, at least log that error
        if (Config.DEBUG) Log.d(Config.TAG, "ERROR (code: "+ errorCode +")");
    }

    protected abstract void onRefresh(boolean forceApiReload);

}
