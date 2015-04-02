package eu.zkkn.android.barcamp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import eu.zkkn.android.barcamp.model.Session;


public class MainActivity extends ActionBarActivity {

    private Data mData;
    private ListView mSessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mData = new Data(this);
        mSessions = (ListView) findViewById(R.id.lv_sessions);
        mSessions.setEmptyView(findViewById(R.id.progressbar));

    }

    @Override
    protected void onStart() {
        super.onStart();
        onRefresh(false);
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
        mData.getSessions(new Data.Listener<List<Session>>() {
            @Override
            public void onData(List<Session> data) {
                SessionsAdapter adapter = new SessionsAdapter(MainActivity.this, data);
                mSessions.setAdapter(adapter);
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                if (Config.DEBUG) Log.d(Config.TAG, errorMsg);
            }
        }, forceReload);
    }
}
