package eu.zkkn.android.barcamp;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import eu.zkkn.android.barcamp.model.Session;

/**
 *
 */
public class Data {

    private static final String VOLLEY_TAG = "DataVolleyTag";

    private Context mCtx;

    public Data(Context context) {
        mCtx = context;
    }


    public void getSessions(Listener<List<Session>> listener, boolean forceReload) {
        loadSessions(listener);
    }


    private void loadSessions(final Listener<List<Session>> listener) {
        JsonObjectRequest request = new JsonObjectRequest(Config.API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ArrayList<Session> sessions = new ArrayList<>();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                            JSONArray jsonSessions = response.getJSONArray("sessions");
                            for (int i = 0; i < jsonSessions.length(); ++i) {
                                JSONObject jsonSession = jsonSessions.getJSONObject(i);
                                Session session = new Session();
                                session.id = jsonSession.getInt("id");
                                session.room = jsonSession.getInt("room");
                                session.start = dateFormat.parse(jsonSession.getString("start"));
                                session.end = dateFormat.parse(jsonSession.getString("end"));
                                session.name = jsonSession.getString("name");
                                session.speaker = jsonSession.getString("speaker");
                                session.description = jsonSession.getString("description");

                                sessions.add(session);
                            }

                            listener.onData(sessions);
                        } catch (JSONException | ParseException e) {
                            listener.onError(e.getMessage());
                        }
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
