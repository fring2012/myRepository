package com.abupdate.sota.engine.report;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.abupdate.sota.info.db.DBHelper;
import com.abupdate.sota.info.remote.ReportInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * 上报数据的本地数据库管理类<br><br/>
 * 功能：对两张上报表的增加，删除，查找<br/>
 */
public class ReportDBManager {

    private DBHelper helper;
    private SQLiteDatabase db;

    public ReportDBManager(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    public void add(String appName,String packageName,String versionName,int versionCode,String reportType,int status ) {
        db.beginTransaction();  //开始事务
        try {
            db.execSQL("INSERT INTO " + DBHelper.TABLE_NAME_REPORT + " VALUES(null, ?, ?, ?, ?,?,?)",
                    new Object[]{appName, packageName, versionCode, versionName, reportType, status});
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }


    public void delete(ReportInfo info) {
        db.delete(DBHelper.TABLE_NAME_REPORT, "_id = ?", new String[]{String.valueOf(info._id)});

    }

    public List<ReportInfo> query() {
        ArrayList<ReportInfo> infos = new ArrayList<>();
        Cursor c = queryByTableName(DBHelper.TABLE_NAME_REPORT);
        while (c != null && c.moveToNext()) {
            int _id = c.getInt(c.getColumnIndex("_id"));
            String appName = c.getString(c.getColumnIndex("appName"));
            String packageName = c.getString(c.getColumnIndex("packageName"));
            int versionCode = c.getInt(c.getColumnIndex("versionCode"));
            String versionName = c.getString(c.getColumnIndex("versionName"));
            String reportType = c.getString(c.getColumnIndex("reportType"));
            int status = c.getInt(c.getColumnIndex("status"));
            ReportInfo info = new ReportInfo(appName, packageName, versionCode, versionName, reportType, status);
            info.setId(_id);
            infos.add(info);
        }
        c.close();
        return infos;
    }


    private Cursor queryByTableName(String table_name) {
        Cursor c = db.rawQuery("SELECT * FROM " + table_name, null);
        return c;
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }
}
