package com.example.chineseculture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_DISPLAY_NAME = "display_name";

    public UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_DISPLAY_NAME + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " +
                    COL_DISPLAY_NAME + " TEXT NOT NULL DEFAULT ''");
        }
    }

    public boolean registerUser(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String displayName = "用户" + java.util.UUID.randomUUID().toString().replace("-", "");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_DISPLAY_NAME, displayName);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean validateLogin(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_USERNAME + "=? AND " + COL_PASSWORD + "=?";
        String[] args = {username, password};
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, selection, args,
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String username, String newPassword) {
        if (username == null || newPassword == null) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newPassword);
        int rows = db.update(TABLE_USERS, values, COL_USERNAME + "=?",
                new String[]{username});
        return rows > 0;
    }

    public boolean updateDisplayName(String username, String displayName) {
        if (username == null || displayName == null) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DISPLAY_NAME, displayName);
        int rows = db.update(TABLE_USERS, values, COL_USERNAME + "=?",
                new String[]{username});
        return rows > 0;
    }

    public String getDisplayName(String username) {
        if (username == null) {
            return "";
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_DISPLAY_NAME},
                COL_USERNAME + "=?", new String[]{username},
                null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name == null ? "" : name;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID},
                COL_USERNAME + "=?", new String[]{username},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
}
