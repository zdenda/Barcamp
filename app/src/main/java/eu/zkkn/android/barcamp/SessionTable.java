package eu.zkkn.android.barcamp;

import android.database.sqlite.SQLiteDatabase;

/**
 *
 */
public class SessionTable {

    // If you change table, you must increment the table version.
    public static final int TABLE_VERSION = 1;

    public static final String TABLE_NAME = "session";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ROOM = "room";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SPEAKER = "speaker";
    public static final String COLUMN_DESCRIPTION = "description";

    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_ROOM + " INTEGER,"
            + COLUMN_START + " INTEGER,"
            + COLUMN_END + " INTEGER,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_SPEAKER + " TEXT,"
            + COLUMN_DESCRIPTION + " TEXT"
            + ");";

    private static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db) {
        // This table is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(TABLE_DELETE);
        onCreate(db);
    }

    public static void onDowngrade(SQLiteDatabase db) {
        onUpgrade(db);
    }
}
