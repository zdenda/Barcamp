package eu.zkkn.android.barcamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 *
 */
//TODO add Cursor.close(), something like in CursorLoader
public abstract class DataLoader<D> extends AsyncTaskLoader<DataObject<D>> {

    /** Minimal interval in milliseconds between two consecutive access to the API  */
    private static final long MIN_API_LOAD_INTERVAL_MS = 5 * 60 * 1000; // 5 min in milliseconds

    private DataObject<D> mData;
    private Data database;
    private int mLastErrorCode = ErrorCode.NO_ERROR;
    private long mLastApiLoad; // in milliseconds

    public DataLoader(Context context) {
        super(context);
        database = new Data(context);
    }

    protected Data getDatabase() {
        return database;
    }

    @Override
    public void deliverResult(DataObject<D> data) {
        if (isReset()) return; // An async query came in while the loader is stopped

        data.setErrorCode(mLastErrorCode);
        mLastErrorCode = ErrorCode.NO_ERROR;

        mData = data;
        super.deliverResult(data);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }
        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        mData = null;
    }

    /**
     * Start a service which will load sessions from API
     * @param forceApiReload ignore minimal interval between API access
     */
    protected void loadFromApi(boolean forceApiReload) {
        if (Config.DEBUG) Log.d(Config.TAG, "DataLoader.loadFromApi()" + (forceApiReload ? " forced" : ""));
        if (forceApiReload || checkApiReloadInterval()) {
            mLastApiLoad = SystemClock.elapsedRealtime();
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
                    mLastErrorCode = intent.getIntExtra(ApiRequestService.EXTENDED_ERROR_CODE,
                            ErrorCode.NO_ERROR);
                    onContentChanged();
                }
            }, new IntentFilter(ApiRequestService.API_SERVICE_BROADCAST_ACTION));
            ApiRequestService.startActionGetSessions(getContext());
        }
    }

    /**
     * Check if the minimal delay between two consecutive API accesses is over
     */
    private synchronized boolean checkApiReloadInterval() {
        return SystemClock.elapsedRealtime() > (mLastApiLoad + MIN_API_LOAD_INTERVAL_MS);
    }

}
