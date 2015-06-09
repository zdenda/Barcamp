package eu.zkkn.android.barcamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * App settings
 */
public class Preferences {

    /**
     * Boolean preference that indicates whether notifications for selected sessions should be showed to the user.
     */
    private static final String PREF_KEY_SESSIONS_NOTIFICATIONS_ENABLED = "pref_sessions_notifications_enabled";

    private static SharedPreferences sPreferences;

    private static SharedPreferences getPref(Context context) {
        if (sPreferences == null) {
            sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sPreferences;
    }

    public static void setSessionsNotificationsEnabled(Context context, boolean enabled) {
        getPref(context).edit().putBoolean(PREF_KEY_SESSIONS_NOTIFICATIONS_ENABLED, enabled).commit();
    }

    public static boolean isSessionsNotificationsEnabled(Context context) {
        return getPref(context).getBoolean(PREF_KEY_SESSIONS_NOTIFICATIONS_ENABLED, true);
    }

}
