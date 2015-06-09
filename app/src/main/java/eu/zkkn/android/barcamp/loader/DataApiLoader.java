package eu.zkkn.android.barcamp.loader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import eu.zkkn.android.barcamp.service.ApiRequestService;
import eu.zkkn.android.barcamp.Config;
import eu.zkkn.android.barcamp.DataObject;
import eu.zkkn.android.barcamp.ErrorCode;

/**
 *
 */
public abstract class DataApiLoader<D> extends DataLoader<DataObject<D>> implements ApiLoadInterface {

    /** Minimal interval in milliseconds between two consecutive access to the API  */
    private static final long MIN_API_LOAD_INTERVAL_MS = 5 * 60 * 1000; // 5 min in milliseconds

    private long mLastApiLoad; // in milliseconds
    private int mLastErrorCode = ErrorCode.NO_ERROR;


    public DataApiLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(DataObject<D> data) {
        data.setErrorCode(mLastErrorCode);
        mLastErrorCode = ErrorCode.NO_ERROR;
        super.deliverResult(data);
    }

    /**
     * Start a service which will load sessions from API
     * @param forceReload If true, ignore minimal interval between API access
     */
    @Override
    public void loadFromApi(boolean forceReload) {
        if (Config.DEBUG) Log.d(Config.TAG, "DataLoader.loadFromApi()" + (forceReload ? " forced" : ""));
        if (forceReload || checkApiReloadInterval()) {
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
    private boolean checkApiReloadInterval() {
        return SystemClock.elapsedRealtime() > (mLastApiLoad + MIN_API_LOAD_INTERVAL_MS);
    }

}
