package com.example.c.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class SqliteUtil {
    private static SqliteUtil sqliteUtil;
    private static SQLiteDatabase db;




    private SqliteUtil(String path){
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        db = SQLiteDatabase.openOrCreateDatabase(path+"appDemo.db",null);
    }
    public static SQLiteDatabase geSQLiteDatabase(String path){
        if (sqliteUtil == null)
            sqliteUtil = new SqliteUtil(path);
        return db;
    }
}
