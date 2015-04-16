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

/**
 *
 */
public class Data {

    private static final String VOLLEY_TAG = "DataVolleyTag";

    private Context mCtx;
    private DbHelper mDb;


    public Data(Context context) {
        mCtx = context;
        mDb = new DbHelper(context);
    }


    public void getSessions(Listener<Cursor> listener, boolean forceReload) {
        Cursor cursor = getSessions();
        if (forceReload || cursor.getCount() == 0) {
            loadSessions(listener);
            return;
        }
        listener.onData(cursor);
    }

    private Cursor getSessions() {
        String[] projection = {SessionTable.COLUMN_ID, SessionTable.COLUMN_NAME,
                SessionTable.COLUMN_SPEAKER, SessionTable.COLUMN_START};
        return mDb.getReadableDatabase()
                .query(SessionTable.TABLE_NAME, projection, null, null, null, null,
                        SessionTable.COLUMN_START);
    }

    private void loadSessions(final Listener<Cursor> listener) {
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

                                ContentValues values = new ContentValues();
                                values.put(SessionTable.COLUMN_ID,
                                        jsonSession.getInt("id"));
                                values.put(SessionTable.COLUMN_ROOM,
                                        jsonSession.getInt("room"));
                                values.put(SessionTable.COLUMN_START,
                                        dateFormat.parse(jsonSession.getString("start")).getTime());
                                values.put(SessionTable.COLUMN_END,
                                        dateFormat.parse(jsonSession.getString("end")).getTime());
                                values.put(SessionTable.COLUMN_NAME,
                                        jsonSession.getString("name"));
                                values.put(SessionTable.COLUMN_SPEAKER,
                                        jsonSession.getString("speaker"));
                                values.put(SessionTable.COLUMN_DESCRIPTION,
                                        jsonSession.getString("description"));

                                db.insert(SessionTable.TABLE_NAME, null, values);
                            }

                        } catch (JSONException | ParseException e) {
                            listener.onError(e.getMessage());
                        } finally {
                            db.close();
                        }
                        listener.onData(getSessions());
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
