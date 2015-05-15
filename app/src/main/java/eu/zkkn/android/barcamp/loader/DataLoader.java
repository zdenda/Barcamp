package eu.zkkn.android.barcamp.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import eu.zkkn.android.barcamp.Data;

/**
 * Abstract Loader for retrieving data from SQLite database in background (AsyncTask)
 */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {

    protected D mData;
    private Data database;


    public DataLoader(Context context) {
        super(context);
        database = new Data(context);
    }

    protected Data getDatabase() {
        return database;
    }

    @Override
    public void deliverResult(D data) {
        if (isReset()) return; // An async query came in while the loader is stopped
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

}
