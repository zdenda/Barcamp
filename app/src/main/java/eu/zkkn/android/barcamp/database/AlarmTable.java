package eu.zkkn.android.barcamp.database;

import android.database.sqlite.SQLiteDatabase;

/**
 *
 */
public class AlarmTable {

    // If you change table, you must increment the table version.
    public static final int TABLE_VERSION = 1;

    public static final String TABLE_NAME = "alarm";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SESSION_ID = "session_id";
    public static final String COLUMN_TIME = "time";

    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_SESSION_ID + " INTEGER UNIQUE,"
            + COLUMN_TIME + " INTEGER"
            + ");";

    private static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db) {
        onCreate(db);
    }

    public static void onDowngrade(SQLiteDatabase db) {
        // in case of downgrade simply discard all data and start over
        db.execSQL(TABLE_DELETE);
        onCreate(db);
    }
}
