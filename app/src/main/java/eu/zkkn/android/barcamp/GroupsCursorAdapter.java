package eu.zkkn.android.barcamp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Cursor Adapter with items in groups with title
 */
public class GroupsCursorAdapter extends ResourceCursorAdapter {

    //TODO: check if this is really useful or Cursor.getColumnIndexOrThrow() would be better
    protected Map<String, Integer> mColumnIndexes;

    /**
     * Format for output of time
     */
    private DateFormat mTimeFormat;



    public GroupsCursorAdapter(Context context, int layout, Cursor c) {
        //TODO: this ResourceCursorAdapter constructor is deprecated
        super(context, layout, c);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        if (c != null) findColumns(c.getColumnNames());

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Decide whether show group title (time of session start)
        boolean showTitle = false;
        long start;
        int position = cursor.getPosition();

        if (position == 0) {
            showTitle = true;
            start = cursor.getLong(mColumnIndexes.get(SessionTable.COLUMN_START));
        } else {
            int columnIndex = mColumnIndexes.get(SessionTable.COLUMN_START);
            start = cursor.getLong(columnIndex);
            cursor.moveToPrevious();
            long prevStart = cursor.getLong(columnIndex);
            cursor.moveToPosition(position);
            if (prevStart != start) {
                showTitle = true;
            }
        }

        TextView title = (TextView) view.findViewById(R.id.tv_title);
        if (showTitle) {
            title.setVisibility(View.VISIBLE);
            title.setText(mTimeFormat.format(new Date(start)));
        } else {
            title.setVisibility(View.GONE);
        }

        TextView name = (TextView) view.findViewById(R.id.tv_name);
        name.setText(cursor.getString(mColumnIndexes.get(SessionTable.COLUMN_NAME)));

        TextView speaker = (TextView) view.findViewById(R.id.tv_speaker);
        speaker.setText(cursor.getString(mColumnIndexes.get(SessionTable.COLUMN_SPEAKER)));

        view.setTag(cursor.getInt(mColumnIndexes.get(SessionTable.COLUMN_ID)));

    }

    @Override
    public Cursor swapCursor(Cursor c) {
        Cursor res = super.swapCursor(c);
        // rescan columns in case cursor layout is different
        if (c != null) findColumns(c.getColumnNames());
        return res;
    }

    /**
     * Create a map from an array of strings to column-index integers in mCursor.
     * If mCursor is null, the map will be discarded.
     *
     * @param from the Strings naming the columns of interest
     */
    private void findColumns(String[] from) {
        if (mCursor != null) {
            int i;
            int count = from.length;
            if (mColumnIndexes == null || mColumnIndexes.size() != count) {
                mColumnIndexes = new HashMap<>(count);
            }
            for (i = 0; i < count; i++) {
                int columnIndex = mCursor.getColumnIndexOrThrow(from[i]);
                mColumnIndexes.put(from[i], columnIndex);
            }
        } else {
            mColumnIndexes = null;
        }
    }
}
