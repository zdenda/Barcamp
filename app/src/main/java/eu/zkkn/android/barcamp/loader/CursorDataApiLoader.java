package eu.zkkn.android.barcamp.loader;

import android.content.Context;
import android.database.Cursor;

import eu.zkkn.android.barcamp.DataObject;

/**
 * A loader that queries the SQLite database and returns a Cursor.
 * It can request refresh of data in database with data from API
 * It does Cursor closing when the cursor is no longer needed.
 * Inspired by CursorLoader
 */
//TODO: sometimes some Cursor isn't closed, so have a look at it
public abstract class CursorDataApiLoader extends DataApiLoader<Cursor> {

    public CursorDataApiLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(DataObject<Cursor> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (data.getData() != null) {
                data.getData().close();
            }
            return;
        }
        Cursor oldCursor = (mData != null) ? mData.getData() : null;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldCursor != null && oldCursor != data.getData() && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    public void onCanceled(DataObject<Cursor> data) {
        if (data.getData() != null && !data.getData().isClosed()) {
            data.getData().close();
        }
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        // close cursor and then call parent onReset() which will set it to null
        if (mData.getData() != null && !mData.getData().isClosed()) {
            mData.getData().close();
        }
        super.onReset();
    }

}
