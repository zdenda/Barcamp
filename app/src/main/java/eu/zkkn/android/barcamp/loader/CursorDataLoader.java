package eu.zkkn.android.barcamp.loader;

import android.content.Context;
import android.database.Cursor;

/**
 * A loader that queries the SQLite database and returns a Cursor.
 * It does Cursor closing when the cursor is no longer needed.
 * Inspired by CursorLoader
 */
public abstract class CursorDataLoader extends DataLoader<Cursor> {

    public CursorDataLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mData;
        mData = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        // close cursor and then call parent onReset() which will set it to null
        if (mData != null && !mData.isClosed()) {
            mData.close();
        }
        super.onReset();
    }
}
