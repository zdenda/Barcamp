package eu.zkkn.android.barcamp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class DbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1 + SessionTable.TABLE_VERSION
            + AlarmTable.TABLE_VERSION;

    public static final String DATABASE_NAME = "Barcamp.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SessionTable.onCreate(db);
        AlarmTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SessionTable.onUpgrade(db);
        AlarmTable.onUpgrade(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SessionTable.onDowngrade(db);
        AlarmTable.onDowngrade(db);
    }
}
