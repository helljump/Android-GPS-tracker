package ru.zipta.authtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;


/**
 * Created by User on 10.08.2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "data.db";
    public static final int VERSION = 1;
    private static DBHelper instance;

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS locations ( " +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "lat REAL, " +
            "lng REAL, " +
            "alt REAL, " +
            "time INTEGER, " +
            "pub INTEGER DEFAULT 0" +
            ");";

    private DBHelper(Context c) {
        super(c, DB_NAME, null, VERSION);
    }

    public static synchronized DBHelper getInstance(Context c) {
        if (instance == null) {
            instance = new DBHelper(c);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    public synchronized long insertLocation(double lat, double lng, double alt, Date time) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        initialValues.put("lat", lat);
        initialValues.put("lng", lng);
        initialValues.put("alt", alt);
        initialValues.put("time", time.getTime());
        long _id = db.insert("locations", null, initialValues);
        db.close();
        return _id;
    }

    public synchronized Cursor getUnpublished(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM locations WHERE pub=0 ORDER BY time LIMIT 10;", null);
        if(c != null) c.moveToFirst();
        return c;
    }

    public synchronized Cursor getAllLocations(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM locations ORDER BY time DESC;", null);
        if(c != null) c.moveToFirst();
        return c;
    }

    public synchronized void setPublished(long _id){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE locations SET pub=1 WHERE _id=?", new String[]{String.valueOf(_id)});
        db.close();
    }

    public synchronized void vacuum(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM locations WHERE pub=1");
        db.execSQL("VACUUM");
        db.close();
    }

    public synchronized long getRowsCount() {
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, "locations");
    }
}