package com.example.c.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.c.been.FileInfo;

public class DbHelper extends SQLiteOpenHelper{
    public static String table = "file";

    public DbHelper(Context context){
        super(context,"download.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//文件名，下载地址，下载文件的总长度，当前下载完成长度
        sqLiteDatabase.execSQL("create table if not exists file(fileName varchar,url varchar,length integer,finished integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    /**
     * 插入一条下载信息
     */
    public void insertData(SQLiteDatabase db,FileInfo info){
        ContentValues values = new ContentValues();
        values.put("fileName", info.getFileName());
        values.put("url", info.getUrl());
        values.put("length", info.getLength());
        values.put("finished", info.getFinished());
        db.insert(table, null, values);
    }
    /**
     * 是否已经插入这条数据
     */
    public boolean isExist(SQLiteDatabase db, FileInfo info) {
        Cursor cursor = db.query(table, null, "url = ?", new String[]{info.getUrl()}, null, null, null, null);
        boolean exist = cursor.moveToNext();
        cursor.close();
        return exist;
    }

    /**
     * 查询已经存在的一条信息
     */
    public FileInfo queryData(SQLiteDatabase db, String url) {
        Cursor cursor = db.query(table, null, "url = ?", new String[]{url}, null, null, null, null);
        FileInfo info = new FileInfo();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex("fileName"));
                int length = cursor.getInt(cursor.getColumnIndex("length"));
                int finished = cursor.getInt(cursor.getColumnIndex("finished"));
                info.setStop(false);
                info.setFileName(fileName);
                info.setUrl(url);
                info.setLength(length);
                info.setFinished(finished);
            }
            cursor.close();
        }
        return info;
    }


    /**
     * 恢复一条下载信息
     */
    public void resetData(SQLiteDatabase db, String url) {
        ContentValues values = new ContentValues();
        values.put("finished", 0);
        values.put("length", 0);
        db.update(table, values, "url = ?", new String[]{url});
    }

    /**
     *修改下载信息
     */
    public void updateData(SQLiteDatabase db,FileInfo info){
        ContentValues values = new ContentValues();
        values.put("finished",info.getFinished());
        db.update(table,values,"fileName = ?",new String[]{info.getFileName()});
    }
}
