package eu.zkkn.android.barcamp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;

import eu.zkkn.android.barcamp.model.Session;


public class SessionDetailActivity extends BaseActivity {

    public static final String SESSION_ID = "sessionId";

    /**
     * Format for output of time
     */
    private DateFormat mTimeFormat;
    private int mSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        mSessionId = getIntent().getIntExtra(SESSION_ID, -1);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        onRefresh(false);
    }

    @Override
    protected void onRefresh(boolean forceReload) {
        mData.getSession(mSessionId, new Data.Listener<Session>() {
            @Override
            public void onData(Session session) {
                setWidgets(session);
            }

            @Override
            public void onError(String errorMsg) {
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
