package eu.zkkn.android.barcamp.activity;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.text.DateFormat;

import eu.zkkn.android.barcamp.receiver.AlarmReceiver;
import eu.zkkn.android.barcamp.DataObject;
import eu.zkkn.android.barcamp.Helper;
import eu.zkkn.android.barcamp.R;
import eu.zkkn.android.barcamp.loader.DataApiLoader;
import eu.zkkn.android.barcamp.model.Session;


public class SessionDetailActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<DataObject<Session>>, SwipeRefreshLayout.OnRefreshListener {

    public static final String SESSION_ID = "sessionId";

    private static final int LOADER_SESSION_ID = 0;
    private static final String ARGS_SESSION_ID_KEY = "sessionIdKey";

    /**
     * Format for output of time
     */
    private DateFormat mTimeFormat;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_session);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        int sessionId = getIntent().getIntExtra(SESSION_ID, 0);
        if (sessionId > 0) {
            getSupportLoaderManager().initLoader(LOADER_SESSION_ID,
                    Helper.intBundle(ARGS_SESSION_ID_KEY, sessionId), this);
        }

    }

    @Override
    protected void onRefresh(boolean forceApiReload) {
        mSwipeRefreshLayout.setRefreshing(true);
        Loader loader = getSupportLoaderManager().getLoader(LOADER_SESSION_ID);
        if (forceApiReload) {
            ((DataApiLoader) loader).loadFromApi(true);
        } else {
            loader.forceLoad();
        }
    }

    @Override
    public void onRefresh() {
        onRefresh(true);
    }

    @Override
    public Loader<DataObject<Session>> onCreateLoader(int id, Bundle args) {
        final int sessionId = args.getInt(ARGS_SESSION_ID_KEY, 0);
        return new DataApiLoader<Session>(this) {
            @Override
            public DataObject<Session> loadInBackground() {
                return new DataObject<>(getDatabase().getSession(sessionId));
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<DataObject<Session>> loader, DataObject<Session> data) {
        if (!data.hasError() && data.getData() != null) setWidgets(data.getData());
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<DataObject<Session>> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private void setWidgets(final Session session) {
        ((TextView) findViewById(R.id.tv_name)).setText(session.name);

        TextView speaker = (TextView) findViewById(R.id.tv_speaker);
        speaker.setText(session.speaker);
        if (session.speaker == null) speaker.setVisibility(View.GONE);

        ((TextView) findViewById(R.id.tv_description)).setText(session.description);

        CheckBox notification = (CheckBox) findViewById(R.id.cb_notification);
        notification.setText(getString(R.string.from_to_room, mTimeFormat.format(session.start),
                mTimeFormat.format(session.end), session.room));
        notification.setChecked(session.alarm != null);
        notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isAlarmSet;
                if (isChecked) {
                    isAlarmSet = AlarmReceiver.setAlarm(SessionDetailActivity.this, session.id, session.start);
                } else {
                    isAlarmSet = !AlarmReceiver.cancelAlarm(SessionDetailActivity.this, session.id);
                }
                // correct checkbox value if there was some problem with alarm setting/canceling
                if (isAlarmSet != isChecked) buttonView.setChecked(isAlarmSet);
            }
        });

        findViewById(R.id.progressbar).setVisibility(View.GONE);
    }

}
