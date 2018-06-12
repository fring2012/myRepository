package com.abupdate.sota.info.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建 2 张数据库表：上报下载信息，上报升级信息<br/>
 * 如果表有更新，请将DATABASE_VERSION版本号+1<br/>
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "sota_sdk.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_REPORT = "report";

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_REPORT +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "appName VARCHAR, " +
                "packageName VARCHAR, " +
                "versionCode INTEGER, " +
                "versionName VARCHAR," +
                "reportType VARCHAR," +
                "status INTEGER)");

    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table IF EXISTS " + TABLE_NAME_REPORT);
        onCreate(db);
    }
}
