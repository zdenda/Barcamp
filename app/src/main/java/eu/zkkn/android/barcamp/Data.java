package eu.zkkn.android.barcamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import eu.zkkn.android.barcamp.database.AlarmTable;
import eu.zkkn.android.barcamp.database.DbHelper;
import eu.zkkn.android.barcamp.database.GcmNotificationTable;
import eu.zkkn.android.barcamp.database.SessionTable;
import eu.zkkn.android.barcamp.model.Alarm;
import eu.zkkn.android.barcamp.model.GcmNotification;
import eu.zkkn.android.barcamp.model.Session;

/**
 *
 */
public class Data {

    private DbHelper mDb;


    public Data(Context context) {
        mDb = new DbHelper(context);
    }

    /**
     * Close any open database connection
     */
    public void close() {
        mDb.close();
    }

    public Cursor getGcmNotifications() {
        String[] projection = {GcmNotificationTable.COLUMN_ID, GcmNotificationTable.COLUMN_RECEIVED,
                GcmNotificationTable.COLUMN_TEXT};
        return mDb.getReadableDatabase()
                .query(GcmNotificationTable.TABLE_NAME, projection, null, null, null, null,
                        GcmNotificationTable.COLUMN_RECEIVED + " DESC");
    }

    public void saveGcmNotification(GcmNotification notification) {
        ContentValues values = new ContentValues();
        values.put(GcmNotificationTable.COLUMN_RECEIVED, notification.received.getTime());
        values.put(GcmNotificationTable.COLUMN_TEXT, notification.text);

        SQLiteDatabase db = mDb.getWritableDatabase();
        db.insert(GcmNotificationTable.TABLE_NAME, null, values);
        db.close();
    }

    /**
     * Add an alarm to database
     * @param sessionId ID of session for which the alarm is set
     * @param time Time when the alarm will go off
     * @return Return true if alarm was successfully added, otherwise return false
     */
    public boolean setAlarm(int sessionId, Date time) {
        ContentValues values = new ContentValues();
        values.put(AlarmTable.COLUMN_SESSION_ID, sessionId);
        values.put(AlarmTable.COLUMN_TIME, time.getTime());

        SQLiteDatabase db = mDb.getWritableDatabase();
        long id = db.insert(AlarmTable.TABLE_NAME, null, values);
        db.close();
        return id != -1; //error occurred during insert if ID is -1
    }

    /**
     * Remove alarm for session from database
     * @param sessionId ID of session for which the alarm should be removed
     * @return Return true if alarm was successfully removed, otherwise return false
     */
    public boolean deleteAlarm(int sessionId) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        int rows = db.delete(AlarmTable.TABLE_NAME, AlarmTable.COLUMN_SESSION_ID + "=?",
                new String[]{String.valueOf(sessionId)});
        db.close();
        return rows > 0;
    }


    public Cursor getSessions() {
        String[] projection = {SessionTable.COLUMN_ID, SessionTable.COLUMN_NAME,
                SessionTable.COLUMN_SPEAKER, SessionTable.COLUMN_START, SessionTable.COLUMN_END,
                SessionTable.COLUMN_ROOM, SessionTable.COLUMN_COVER};
        //TODO: what is better to return Cursor or list of Sessions?
        return mDb.getReadableDatabase()
                .query(SessionTable.TABLE_NAME, projection, null, null, null, null,
                        SessionTable.COLUMN_START);
    }

    public Session getSession(int sessionId) {
        String[] projection = {SessionTable.COLUMN_ID, SessionTable.COLUMN_NAME,
                SessionTable.COLUMN_SPEAKER, SessionTable.COLUMN_START, SessionTable.COLUMN_END,
                SessionTable.COLUMN_ROOM, SessionTable.COLUMN_DESCRIPTION, SessionTable.COLUMN_COVER};
        SQLiteDatabase database = mDb.getReadableDatabase();
        Cursor cursor = database.query(SessionTable.TABLE_NAME, projection,
                SessionTable.COLUMN_ID + "=?", new String[]{String.valueOf(sessionId)},
                null, null, null);

        if (cursor == null || !cursor.moveToFirst()) return null;

        Session session = new Session();
        session.id = sessionId;
        session.name = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_NAME));
        session.speaker = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_SPEAKER));
        session.room = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_ROOM));
        session.start = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_START)));
        session.end = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_END)));
        session.description = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_DESCRIPTION));
        session.cover = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_COVER));

        session.alarm = getAlarm(sessionId);

        cursor.close();
        database.close();

        return session;
    }

    public Alarm getAlarm(int sessionId) {
        String[] projection = {AlarmTable.COLUMN_ID, AlarmTable.COLUMN_SESSION_ID,
                AlarmTable.COLUMN_TIME};
        SQLiteDatabase database = mDb.getReadableDatabase();
        Cursor cursor = database.query(AlarmTable.TABLE_NAME, projection,
                AlarmTable.COLUMN_SESSION_ID + "=?", new String[]{String.valueOf(sessionId)},
                null, null, null, "1");

        if (cursor == null || !cursor.moveToFirst()) return null;

        Alarm alarm = new Alarm();
        alarm.id = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmTable.COLUMN_ID));
        alarm.sessionId = cursor.getInt(cursor.getColumnIndexOrThrow(AlarmTable.COLUMN_SESSION_ID));
        alarm.time = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(AlarmTable.COLUMN_TIME)));

        cursor.close();

        return alarm;
    }

}
