package edu.wust.lynn.classcontacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lynn on 14-8-21.
 */
public class CollegeHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "College.db";

    public CollegeHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(College.Room.SQL_CREATE_ENTRIES);
        db.execSQL(College.Student.SQL_CREATE_ENTRIES);
        db.execSQL(College.StudentFTS3.SQL_CREATE_ENTRIES);
        db.execSQL(College.Course.SQL_CREATE_ENTRIES);
        db.execSQL(College.Optional.SQL_CREATE_ENTRIES);
        db.execSQL(College.Record.SQL_CREATE_ENTRIES);
        db.execSQL(College.State.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TEABLE IF EXISTS " + College.StudentFTS3.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + College.State.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + College.Record.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + College.Optional.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXITSTS " + College.Student.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXITSTS " + College.Room.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXITSTS " + College.Course.TABLE_NAME);
            onCreate(db);
        }
    }
}
