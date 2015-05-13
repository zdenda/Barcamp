package eu.zkkn.android.barcamp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.ListView;


public class GcmNotificationsActivity extends BaseActivity {

    private ListView mNotifications;
    private SwitchCompat mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcm_notifications);

        mNotifications = (ListView) findViewById(R.id.lv_notifications);
        mNotifications.setEmptyView(findViewById(R.id.progressbar));

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
    protected void onStart() {
        super.onStart();
        onRefresh(false);
    }

    @Override
    protected void onRefresh(boolean forceApiReload) {
        //TODO: use Loader
        Cursor notifications = mData.getGcmNotifications();
        GcmNotificationsAdapter adapter = new GcmNotificationsAdapter(this,
                R.layout.row_notification, notifications,
                new String[] {GcmNotificationTable.COLUMN_TEXT, GcmNotificationTable.COLUMN_RECEIVED},
                new int[] {R.id.tv_text, R.id.tv_received});
        mNotifications.setAdapter(adapter);
    }

    @Override
    protected void onSettingsChanged() {
        super.onSettingsChanged();
        mSwitch.setChecked(Preferences.isGcmNotificationsEnabled(this));
    }
}
