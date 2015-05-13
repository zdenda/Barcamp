package eu.zkkn.android.barcamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 *
 */
//TODO add Cursor.close(), something like in CursorLoader
public abstract class DataLoader<D> extends AsyncTaskLoader<DataObject<D>> {

    private DataObject<D> mData;
    private Data database;
    private int mLastErrorCode = ErrorCode.NO_ERROR;

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
     */
    protected void loadFromApi() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(Config.TAG, "BroadcastReceiver.onReceive()");
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this);
                mLastErrorCode = intent.getIntExtra(ApiRequestService.EXTENDED_ERROR_CODE,
                        ErrorCode.NO_ERROR);
                onContentChanged();
            }
        }, new IntentFilter(ApiRequestService.API_SERVICE_BROADCAST_ACTION));
        ApiRequestService.startActionGetSessions(getContext());
    }

}
