package com.example.c.dao.code;

import android.database.sqlite.SQLiteDatabase;

import com.orhanobut.logger.Logger;

import java.io.File;

public class BaseDao implements Dao {
    private String dbPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/" + "appDemo.db";
    protected SQLiteDatabase database;

    public BaseDao(){
        getConnection();
    }
    @Override
    public void getConnection() {
        if(database == null)
            database = SQLiteDatabase.openOrCreateDatabase(dbPath,null);
    }

    @Override
    public void close() {
        database.close();
        database = null;
    }

    public void execSQL(String sql){
        database.execSQL(sql);
    }

}
