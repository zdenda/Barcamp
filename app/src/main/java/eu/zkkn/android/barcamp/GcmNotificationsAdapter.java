package eu.zkkn.android.barcamp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

/**
 *
 */
public class GcmNotificationsAdapter extends SimpleCursorAdapter {

    private final DateFormat mDateFormat;
    private final DateFormat mTimeFormat;

    public GcmNotificationsAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
    }

    @Override
    public void setViewText(TextView v, String text) {
        // if text should be displayed as a date
        if (v.getId() == R.id.tv_received) {
            Date date = new Date(Long.parseLong(text));
            text = DateUtils.isToday(date.getTime()) ?
                    mTimeFormat.format(date) :  mDateFormat.format(date) + ", " + mTimeFormat.format(date);
        }
        super.setViewText(v, text);
    }
}
