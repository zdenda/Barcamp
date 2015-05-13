package eu.zkkn.android.barcamp;

import android.os.Bundle;

/**
 *
 */
public class Helper {

    /**
     * Make a Bundle for a single key/int value pair.
     */
    public static Bundle intBundle(String key, int value) {
        Bundle b = new Bundle(1);
        b.putInt(key, value);
        return b;
    }

    /**
     * Sleep some time
     * used for debugging, for example to simulate longer network delay
     * @param seconds The time to sleep in seconds
     */
    public static void sleep(int seconds) {
        if (!Config.DEBUG) return;
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
