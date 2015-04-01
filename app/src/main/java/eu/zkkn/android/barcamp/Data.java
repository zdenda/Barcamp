package eu.zkkn.android.barcamp;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class Data {

    private static final String PREFS_NAME = "DataPrefsFile";
    private static final String PREFS_KEY_NAME = "prefsKeyName";

    private static final String VOLLEY_TAG = "DataVolleyTag";

    private Context mCtx;

    public Data(Context context) {
        mCtx = context;
    }

    public void getName(Listener<String> listener, boolean forceReload) {
        SharedPreferences preferences = mCtx.getSharedPreferences(PREFS_NAME, 0);
        String name = preferences.getString(PREFS_KEY_NAME, null);
        if (name == null || forceReload) {
            loadName(listener, preferences);
            return;
        }
        listener.onData(name);
    }

    private void loadName(final Listener<String> listener, final SharedPreferences preferences) {
        JsonObjectRequest request = new JsonObjectRequest(Config.API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String name = response.getString("name");
                            preferences.edit().putString(PREFS_KEY_NAME, name).commit();
                            listener.onData(name);
                        } catch (JSONException e) {
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
