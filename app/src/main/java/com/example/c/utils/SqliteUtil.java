package com.example.c.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqliteUtil {
    private static SqliteUtil sqliteUtil;
    private SQLiteDatabase db;



    private SqliteUtil(){
        db = SQLiteDatabase.openOrCreateDatabase("appDemo.db",null);
    }
    public static SqliteUtil getSqliteUtil(){
        if (sqliteUtil == null)
            sqliteUtil = new SqliteUtil();
        return sqliteUtil;
    }
    public void execSQL(String sql){
        db.execSQL(sql);
    }
    public void insert(String tableName,ContentValues cValues){
        db.insert(tableName,null,cValues);
    }
    public void delete(String tableName,String whereClause,String[] whereArgs){
        db.delete(tableName,whereClause,whereArgs);
    }
    public void update(String tableName,ContentValues values,String whereClause,String[] whereArgs){
        db.update(tableName,values,whereClause,whereArgs);
    }
    public Cursor query(String table,String[] columns,String selection,String[]  selectionArgs,String groupBy,String having,String orderBy,String limit){
        return  db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }
    public void dropTable(String table){
        String sql = "DROP TABLE "+table;
        execSQL(sql);
    }
}
