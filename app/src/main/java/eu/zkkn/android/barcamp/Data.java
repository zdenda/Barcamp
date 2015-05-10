package eu.zkkn.android.barcamp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.zkkn.android.barcamp.model.Alarm;
import eu.zkkn.android.barcamp.model.GcmNotification;
import eu.zkkn.android.barcamp.model.Session;

/**
 *
 */
//TODO: This is becoming bigger and bigger ugliness, so you should really rewrite it!!! soon!!!
// maybe https://dl.google.com/googleio/2010/android-developing-RESTful-android-apps.pdf
public class Data {

    private static final String VOLLEY_TAG = "DataVolleyTag";

    private Context mCtx;
    private DbHelper mDb;


    public Data(Context context) {
        mCtx = context;
        mDb = new DbHelper(context);
    }

    /**
     * Close any open database connection
     */
    public void close() {
        mDb.close();
    }

    public void getSessions(Listener<Cursor> listener, boolean forceReload) {
        Cursor cursor = getSessions();
        if (forceReload || cursor.getCount() == 0) {
            loadAllSessionsFromApi(listener);
            return;
        }
        listener.onData(cursor);
    }

    public void getSession(int sessionId, Listener<Session> listener, boolean forceReload) {
        Session session = getSession(sessionId);
        if (forceReload || session == null) {
            loadAllSessionsFromApi(listener, sessionId);
            return;
        }
        listener.onData(session);
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

    public void setAlarm(int sessionId, Date time) {
        ContentValues values = new ContentValues();
        values.put(AlarmTable.COLUMN_SESSION_ID, sessionId);
        values.put(AlarmTable.COLUMN_TIME, time.getTime());

        SQLiteDatabase db = mDb.getWritableDatabase();
        db.insert(AlarmTable.TABLE_NAME, null, values);
        db.close();
    }

    public void deleteAlarm(int sessionId) {
        SQLiteDatabase db = mDb.getWritableDatabase();
        db.delete(AlarmTable.TABLE_NAME, AlarmTable.COLUMN_SESSION_ID + "=?",
                new String[]{String.valueOf(sessionId)});
        db.close();
    }


    private Cursor getSessions() {
        String[] projection = {SessionTable.COLUMN_ID, SessionTable.COLUMN_NAME,
                SessionTable.COLUMN_SPEAKER, SessionTable.COLUMN_START};
        //TODO: what is better to return Cursor or list of Sessions?
        return mDb.getReadableDatabase()
                .query(SessionTable.TABLE_NAME, projection, null, null, null, null,
                        SessionTable.COLUMN_START);
    }

    public Session getSession(int sessionId) {
        String[] projection = {SessionTable.COLUMN_ID, SessionTable.COLUMN_NAME,
                SessionTable.COLUMN_SPEAKER, SessionTable.COLUMN_START, SessionTable.COLUMN_END,
                SessionTable.COLUMN_ROOM, SessionTable.COLUMN_DESCRIPTION};
        SQLiteDatabase database = mDb.getReadableDatabase();
        Cursor cursor = database.query(SessionTable.TABLE_NAME, projection,
                SessionTable.COLUMN_ID + "=?", new String[]{String.valueOf(sessionId)},
                null, null, null);

        if (cursor == null || !cursor.moveToFirst()) return null;

        Session session = new Session();
        session.id = sessionId;
        session.name = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_NAME));
        session.speaker = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_SPEAKER));
        session.room = cursor.getInt(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_ROOM));
        session.start = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_START)));
        session.end = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_END)));
        session.description = cursor.getString(cursor.getColumnIndexOrThrow(SessionTable.COLUMN_DESCRIPTION));

        session.alarm = getAlarm(sessionId);

        cursor.close();
        database.close();

        return session;
    }

    private Alarm getAlarm(int sessionId) {
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


    private void loadAllSessionsFromApi(final Listener listener) {
        loadAllSessionsFromApi(listener, 0);
    }

    private void loadAllSessionsFromApi(final Listener listener, final int sessionId) {
        JsonObjectRequest request = new JsonObjectRequest(Config.API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SQLiteDatabase db = mDb.getWritableDatabase();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                        db.delete(SessionTable.TABLE_NAME, null, null); //truncate table
                        try {
                            JSONArray jsonSessions = response.getJSONArray("sessions");
                            for (int i = 0; i < jsonSessions.length(); ++i) {
                                JSONObject jsonSession = jsonSessions.getJSONObject(i);


                                int id = jsonSession.getInt("id");
                                int room = jsonSession.getInt("room");
                                Date start = dateFormat.parse(jsonSession.getString("start"));
                                Date end = dateFormat.parse(jsonSession.getString("end"));
                                String name = jsonSession.getString("name");
                                String speaker = jsonSession.getString("speaker");
                                String description = jsonSession.getString("description");

                                ContentValues values = new ContentValues();
                                values.put(SessionTable.COLUMN_ID, id);
                                values.put(SessionTable.COLUMN_ROOM, room);
                                values.put(SessionTable.COLUMN_START, start.getTime());
                                values.put(SessionTable.COLUMN_END, end.getTime());
                                values.put(SessionTable.COLUMN_NAME, name);
                                values.put(SessionTable.COLUMN_SPEAKER, speaker);
                                values.put(SessionTable.COLUMN_DESCRIPTION, description);

                                db.insert(SessionTable.TABLE_NAME, null, values);

                                // reschedule alarm if is set and time of session has been changed
                                Alarm alarm = getAlarm(id);
                                if (alarm != null && !alarm.time.equals(start)) {
                                    AlarmReceiver.cancelAlarm(mCtx, id);
                                    AlarmReceiver.setAlarm(mCtx, id, start);
                                }
                            }

                        } catch (JSONException | ParseException e) {
                            listener.onError(e.getMessage());
                        } finally {
                            db.close();
                        }
                        //TODO: do it better
                        listener.onData(sessionId > 0 ? getSession(sessionId) : getSessions());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error.getMessage());
                    }
                }
        );
        request.setTag(VOLLEY_TAG);
        VolleySingleton.getInstance(mCtx).addToRequestQueue(request);
    }


    /**
     * Callback interface for delivering data or error message.
     */
    public interface Listener<T> {
        /**
         * Called when the requested data are loaded.
         */
        public void onData(T data);

        /**
         * Called when an error has been occurred during data loading.
         */
        public void onError(String errorMsg);
    }

}
