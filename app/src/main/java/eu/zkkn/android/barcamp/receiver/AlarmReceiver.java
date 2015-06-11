package eu.zkkn.android.barcamp.receiver;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;

import eu.zkkn.android.barcamp.Config;
import eu.zkkn.android.barcamp.Data;
import eu.zkkn.android.barcamp.Preferences;
import eu.zkkn.android.barcamp.R;
import eu.zkkn.android.barcamp.activity.SessionDetailActivity;
import eu.zkkn.android.barcamp.model.Session;

/**
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String INTENT_SESSION_ID = "intentExtraSessionId";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        int sessionId = intent.getIntExtra(INTENT_SESSION_ID, 0);
        if (Config.DEBUG) Log.d(Config.TAG, "AlarmReceiver Session ID: " + sessionId);

        Session session = new Data(context).getSession(sessionId);
        if (session != null) {
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            showNotification(context, session.id, session.name, timeFormat.format(session.start) + ", " + session.room);
        }
    }

    public static boolean setAlarm(Context context, int sessionId, Date time) {
        long triggerAtMillis = time.getTime();
        if (triggerAtMillis < System.currentTimeMillis()) return false;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        setExact(alarmManager, triggerAtMillis, getAlarmIntent(context, sessionId));
        Data data = new Data(context);
        return data.setAlarm(sessionId, time);
    }

    public static boolean cancelAlarm(Context context, int sessionId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getAlarmIntent(context, sessionId));
        Data data = new Data(context);
        return data.deleteAlarm(sessionId);
    }

    private static void setExact(AlarmManager alarmManager, long triggerAtMillis, PendingIntent operation) {
        // since KIT-KAT we need to use setExact() so the alarm would go off at precise time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
            //TODO: maybe setWindow(), it might save some battery
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
        }
    }

    private static PendingIntent getAlarmIntent(Context context, int sessionId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(INTENT_SESSION_ID, sessionId);
        return PendingIntent.getBroadcast(context, sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void showNotification(Context context, int sessionId, String title, String text) {
        // Don't show notification if they're disabled in settings
        if (!Preferences.isSessionsNotificationsEnabled(context)) return;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setAutoCancel(true);

        Intent intent = new Intent(context, SessionDetailActivity.class);
        intent.putExtra(SessionDetailActivity.SESSION_ID, sessionId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, mBuilder.build());
    }

}
