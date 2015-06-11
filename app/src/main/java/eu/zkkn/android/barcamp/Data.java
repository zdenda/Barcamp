package eu.zkkn.android.barcamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.Date;

import eu.zkkn.android.barcamp.database.AlarmTable;
import eu.zkkn.android.barcamp.database.DbHelper;
import eu.zkkn.android.barcamp.database.SessionTable;
import eu.zkkn.android.barcamp.model.Alarm;
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
        //TODO: what is better to return Cursor or list of Sessions?

        // Left Join Session and Alarm tables
        String tables = SessionTable.TABLE_NAME + " LEFT JOIN " + AlarmTable.TABLE_NAME
                + " ON " + SessionTable.TABLE_NAME + "." + SessionTable.COLUMN_ID + " = "
                + AlarmTable.TABLE_NAME + "." + AlarmTable.COLUMN_SESSION_ID;

        String[] columns = {SessionTable.TABLE_NAME + "." + SessionTable.COLUMN_ID, // column _id is in both tables
                SessionTable.COLUMN_NAME, SessionTable.COLUMN_SPEAKER, SessionTable.COLUMN_START,
                SessionTable.COLUMN_END, SessionTable.COLUMN_ROOM, SessionTable.COLUMN_COVER,
                AlarmTable.COLUMN_TIME};

        String query = SQLiteQueryBuilder.buildQueryString(false, tables, columns, null, null, null,
                SessionTable.COLUMN_START, null);

        return mDb.getReadableDatabase().rawQuery(query, null);
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

    public Cursor getAlarms() {
        String[] projection = {AlarmTable.COLUMN_SESSION_ID, AlarmTable.COLUMN_TIME};
        return mDb.getReadableDatabase().query(AlarmTable.TABLE_NAME, projection, null, null, null, null, null);
    }

}
