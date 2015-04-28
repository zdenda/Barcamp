package eu.zkkn.android.barcamp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;


public class GcmNotificationsActivity extends ActionBarActivity {

    private Data mData;
    private ListView mNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcm_notifications);

        mData = new Data(this);
        mNotifications = (ListView) findViewById(R.id.lv_notifications);
        mNotifications.setEmptyView(findViewById(R.id.progressbar));

    }

    @Override
    protected void onStart() {
        super.onStart();
        Cursor notifications = mData.getGcmNotifications();
        GcmNotificationsAdapter adapter = new GcmNotificationsAdapter(this,
                R.layout.row_notification, notifications,
                new String[] {GcmNotificationTable.COLUMN_TEXT, GcmNotificationTable.COLUMN_RECEIVED},
                new int[] {R.id.tv_text, R.id.tv_received});
        mNotifications.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mData.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gcm_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
