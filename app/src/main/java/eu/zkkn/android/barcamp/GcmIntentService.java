package eu.zkkn.android.barcamp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.Date;

import eu.zkkn.android.barcamp.model.GcmNotification;

/**
 *
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && !extras.isEmpty()) { // GCM message should have extras
            // message should have the right version and type
            if (Config.BARCAMP_GCM_VERSION.equals(extras.getString("version"))
                    && "BarcampNotification".equals(extras.getString("type"))) {
                String text = extras.getString("text");
                saveNotification(text);
                if (Preferences.isGcmNotificationsEnabled(this)) {
                    showNotification(text);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmReceiver.completeWakefulIntent(intent);
    }

    private void saveNotification(String text) {
        GcmNotification n = new GcmNotification();
        n.text = text;
        n.received = new Date();

        Data data = new Data(this);
        data.saveGcmNotification(n);
    }

    private void showNotification(String text) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Barcamp")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .setAutoCancel(true);

        Intent intent = new Intent(this, GcmNotificationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, mBuilder.build());
    }
}
