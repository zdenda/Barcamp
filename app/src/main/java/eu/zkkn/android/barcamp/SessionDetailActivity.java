package eu.zkkn.android.barcamp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;

import eu.zkkn.android.barcamp.model.Session;


public class SessionDetailActivity extends ActionBarActivity {

    public static final String SESSION_ID = "sessionId";

    /**
     * Format for output of time
     */
    private DateFormat mTimeFormat;
    private int mSessionId;
    private Data mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        mData = new Data(this);
        mSessionId = getIntent().getIntExtra(SESSION_ID, -1);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        onRefresh(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_session_detail, menu);
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

    private void onRefresh(boolean forceReload) {
        mData.getSession(mSessionId, new Data.Listener<Session>() {
            @Override
            public void onData(Session session) {
                setWidgets(session);
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                if (Config.DEBUG) Log.d(Config.TAG, errorMsg);
            }
        }, forceReload);
    }

    private void setWidgets(Session session) {
        ((TextView) findViewById(R.id.tv_name)).setText(session.name);
        ((TextView) findViewById(R.id.tv_speaker)).setText(session.speaker);
        ((TextView) findViewById(R.id.tv_room)).setText(String.valueOf(session.room));
        ((TextView) findViewById(R.id.tv_start)).setText(mTimeFormat.format(session.start));
        ((TextView) findViewById(R.id.tv_end)).setText(mTimeFormat.format(session.end));
        ((TextView) findViewById(R.id.tv_description)).setText(session.description);
        findViewById(R.id.progressbar).setVisibility(View.GONE);
    }

}
