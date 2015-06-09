package eu.zkkn.android.barcamp.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.zkkn.android.barcamp.receiver.AlarmReceiver;
import eu.zkkn.android.barcamp.Config;
import eu.zkkn.android.barcamp.Data;
import eu.zkkn.android.barcamp.database.DbHelper;
import eu.zkkn.android.barcamp.ErrorCode;
import eu.zkkn.android.barcamp.database.SessionTable;
import eu.zkkn.android.barcamp.VolleySingleton;
import eu.zkkn.android.barcamp.model.Alarm;

/**
 *
 */
public class ApiRequestService extends IntentService {

    public static final String API_SERVICE_BROADCAST_ACTION =
            "eu.zkkn.android.barcamp.service.ApiRequestService.BROADCAST";
    public static final String EXTENDED_ERROR_CODE =
            "eu.zkkn.android.barcamp.service.ApiRequestService.STATUS";


    private static final String VOLLEY_TAG = "ApiRequestServiceVolleyTag";
    private static final String ACTION_GET_SESSIONS =
            "eu.zkkn.android.barcamp.service.ApiRequestService.action.GET_SESSIONS";


    /**
     * Starts this service to perform action Get Sessions.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetSessions(Context context) {
        Intent intent = new Intent(context, ApiRequestService.class);
        intent.setAction(ACTION_GET_SESSIONS);
        context.startService(intent);
    }


    public ApiRequestService() {
        super("ApiRequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_SESSIONS.equals(action)) {
                handleActionGetSessions();
            }
        }
    }

    /**
     * Handle action Get Sessions in the provided background thread
     */
    private void handleActionGetSessions() {
        JsonObjectRequest request = new JsonObjectRequest(Config.API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int errorCode = ErrorCode.NO_ERROR;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Data data = new Data(ApiRequestService.this);
                        SQLiteDatabase db = new DbHelper(ApiRequestService.this).getWritableDatabase();
                        //TODO: maybe use transaction; db.beginTransaction();

                        try {
                            db.delete(SessionTable.TABLE_NAME, null, null); //truncate table
                            JSONArray jsonSessions = response.getJSONArray("sessions");
                            for (int i = 0; i < jsonSessions.length(); ++i) {
                                JSONObject jsonSession = jsonSessions.getJSONObject(i);

                                int id = jsonSession.getInt("id");
                                String name = jsonSession.getString("name");
                                String room = jsonSession.getString("room");
                                Date start = dateFormat.parse(Config.DATE + " " + jsonSession.getString("start"));
                                Date end = dateFormat.parse(Config.DATE + " " + jsonSession.getString("end"));

                                String speaker = jsonSession.isNull("speaker") ?
                                        null : jsonSession.getString("speaker");
                                String description = jsonSession.isNull("description") ?
                                        null : jsonSession.getString("description");
                                String cover = jsonSession.isNull("cover") ?
                                        null : jsonSession.getString("cover");


                                ContentValues values = new ContentValues();
                                values.put(SessionTable.COLUMN_ID, id);
                                values.put(SessionTable.COLUMN_ROOM, room);
                                values.put(SessionTable.COLUMN_START, start.getTime());
                                values.put(SessionTable.COLUMN_END, end.getTime());
                                values.put(SessionTable.COLUMN_NAME, name);
                                values.put(SessionTable.COLUMN_SPEAKER, speaker);
                                values.put(SessionTable.COLUMN_DESCRIPTION, description);
                                values.put(SessionTable.COLUMN_COVER, cover);

                                db.insert(SessionTable.TABLE_NAME, null, values);

                                // reschedule alarm if is set and time of session has been changed
                                Alarm alarm = data.getAlarm(id);
                                if (alarm != null && !alarm.time.equals(start)) {
                                    AlarmReceiver.cancelAlarm(ApiRequestService.this, id);
                                    AlarmReceiver.setAlarm(ApiRequestService.this, id, start);
                                }

                            }

                        } catch (JSONException | ParseException e) {
                            if (Config.DEBUG) Log.d(Config.TAG, "Error: " + e.toString());
                            errorCode = ErrorCode.JSON_ERROR;
                        }
                        db.close();

                        Intent localIntent = new Intent(API_SERVICE_BROADCAST_ACTION)
                                .putExtra(EXTENDED_ERROR_CODE, errorCode);
                        LocalBroadcastManager.getInstance(ApiRequestService.this)
                                .sendBroadcast(localIntent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (Config.DEBUG) Log.d(Config.TAG, "Error: " + error.toString());
                        Intent localIntent = new Intent(API_SERVICE_BROADCAST_ACTION)
                                .putExtra(EXTENDED_ERROR_CODE, ErrorCode.NETWORK_ERROR);
                        LocalBroadcastManager.getInstance(ApiRequestService.this)
                                .sendBroadcast(localIntent);
                    }
                }
        );
        request.setTag(VOLLEY_TAG);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

}
