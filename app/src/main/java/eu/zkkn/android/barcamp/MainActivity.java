package eu.zkkn.android.barcamp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


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
}
