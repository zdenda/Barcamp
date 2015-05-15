package eu.zkkn.android.barcamp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.ListView;

import eu.zkkn.android.barcamp.loader.CursorDataLoader;


public class GcmNotificationsActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_NOTIFICATIONS_ID = 0;

    private GcmNotificationsAdapter mAdapter;
    private SwitchCompat mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcm_notifications);

        getSupportLoaderManager().initLoader(LOADER_NOTIFICATIONS_ID, null, this);

        ListView notifications = (ListView) findViewById(R.id.lv_notifications);
        notifications.setEmptyView(findViewById(R.id.progressbar));

        mAdapter = new GcmNotificationsAdapter(this, R.layout.row_notification, null,
                new String[] {GcmNotificationTable.COLUMN_TEXT, GcmNotificationTable.COLUMN_RECEIVED},
                new int[] {R.id.tv_text, R.id.tv_received});
        notifications.setAdapter(mAdapter);

        mSwitch = (SwitchCompat) findViewById(R.id.sw_notifications);
        mSwitch.setChecked(Preferences.isGcmNotificationsEnabled(this));
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.setGcmNotificationsEnabled(GcmNotificationsActivity.this, isChecked);
                onSettingsChanged();
            }
        });

    }

    @Override
    protected void onRefresh(boolean forceApiReload) {
        getSupportLoaderManager().getLoader(LOADER_NOTIFICATIONS_ID).forceLoad();
    }

    @Override
    protected void onSettingsChanged() {
        super.onSettingsChanged();
        mSwitch.setChecked(Preferences.isGcmNotificationsEnabled(this));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorDataLoader(this) {
            @Override
            public Cursor loadInBackground() {
                return getDatabase().getGcmNotifications();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
