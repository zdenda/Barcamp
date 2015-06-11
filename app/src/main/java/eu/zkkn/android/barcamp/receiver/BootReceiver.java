package eu.zkkn.android.barcamp.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.zkkn.android.barcamp.Config;
import eu.zkkn.android.barcamp.Data;
import eu.zkkn.android.barcamp.database.AlarmTable;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            if (Config.DEBUG) Log.d(Config.TAG, "Reschedule alarms after reboot");
            Date max = null;
            try {
                max = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(Config.DATE + " 23:59:59");
            } catch (ParseException e) {}
            // when Barcamp has ended, we can disable this broadcast receiver
            if (max == null || System.currentTimeMillis() > max.getTime()) {
                disableBootReceiver(context);
                return;
            }

            Data data = new Data(context);
            Cursor alarms = data.getAlarms();
            while (alarms.moveToNext()) {
                int sessionId = alarms.getInt(alarms.getColumnIndexOrThrow(AlarmTable.COLUMN_SESSION_ID));
                Date time = new Date(alarms.getLong(alarms.getColumnIndexOrThrow(AlarmTable.COLUMN_TIME)));
                AlarmReceiver.setAlarm(context, sessionId, time);
            }
            alarms.close();
            data.close();
        }
    }

    private void disableBootReceiver(Context context) {
        if (Config.DEBUG) Log.d(Config.TAG, "Disable Boot BroadcastReceiver");
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
