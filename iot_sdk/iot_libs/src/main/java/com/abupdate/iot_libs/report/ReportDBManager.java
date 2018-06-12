package com.abupdate.iot_libs.report;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.DownParamInfo;
import com.abupdate.iot_libs.info.ErrorFileParamInfo;
import com.abupdate.iot_libs.info.PushMessageInfo;
import com.abupdate.iot_libs.info.UpgradeParamInfo;

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

    public void add(DownParamInfo info) {
        db.beginTransaction();  //开始事务
        try {
            db.execSQL("INSERT INTO " + DBHelper.TABLE_NAME_REPORT_DOWN + " VALUES(null, ?, ?, ?, ?,?,?)",
                    new Object[]{info.deltaID, info.downloadStatus, info.downStart, info.downEnd, info.downSize, info.downIp});
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void add(UpgradeParamInfo info) {
        db.beginTransaction();  //开始事务
        try {
            db.execSQL("INSERT INTO " + DBHelper.TABLE_NAME_REPORT_UPGRADE + " VALUES(null, ?, ?)",
                    new Object[]{info.deltaID, info.updateStatus});
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void delete(DownParamInfo info) {
        db.delete(DBHelper.TABLE_NAME_REPORT_DOWN, "down_start_time = ?", new String[]{String.valueOf(info.downStart)});

    }

    public void delete(UpgradeParamInfo info) {
        db.delete(DBHelper.TABLE_NAME_REPORT_UPGRADE, "_id = ?", new String[]{String.valueOf(info._id)});
    }

    public void delete(PushMessageInfo info) {
        db.delete(DBHelper.TABLE_NAME_PUSH_RESPONSE, "_id = ?", new String[]{String.valueOf(info._id)});
    }

    public void delete(ErrorFileParamInfo info) {
        db.delete(DBHelper.TABLE_NAME_REPORT_ERROR_LOG, "_id = ?", new String[]{String.valueOf(info._id)});
    }

    //查询所有的未上传的下载报告
    public List<DownParamInfo> query_down() {
        ArrayList<DownParamInfo> infos = new ArrayList<DownParamInfo>();
        Cursor c = queryByTableName(DBHelper.TABLE_NAME_REPORT_DOWN);
        while (c != null && c.moveToNext()) {
            DownParamInfo info = new DownParamInfo();
            info._id = c.getInt(c.getColumnIndex("_id"));
            info.deltaID = c.getString(c.getColumnIndex("delta_id"));
            info.downloadStatus = c.getString(c.getColumnIndex("download_status"));
            info.downStart = c.getString(c.getColumnIndex("down_start_time"));
            info.downEnd = c.getString(c.getColumnIndex("down_end_time"));
            info.downSize = c.getInt(c.getColumnIndex("down_size"));
            info.downIp = c.getString(c.getColumnIndex("down_ip"));
            infos.add(info);
        }
        c.close();
        return infos;
    }

    //查询所有的未上传的升级报告
    public List<UpgradeParamInfo> query_upgrade() {
        ArrayList<UpgradeParamInfo> infos = new ArrayList<UpgradeParamInfo>();
        Cursor c = queryByTableName(DBHelper.TABLE_NAME_REPORT_UPGRADE);
        while (c != null && c.moveToNext()) {
            UpgradeParamInfo info = new UpgradeParamInfo();
            info._id = c.getInt(c.getColumnIndex("_id"));
            info.deltaID = c.getString(c.getColumnIndex("delta_id"));
            info.updateStatus = c.getString(c.getColumnIndex("updateStatus"));
            infos.add(info);
        }
        c.close();
        return infos;
    }

    public List<PushMessageInfo> query_push_data() {
        List<PushMessageInfo> infos = new ArrayList<>();
        Cursor c = queryByTableName(DBHelper.TABLE_NAME_PUSH_RESPONSE);
        while (c != null && c.moveToNext()) {
            PushMessageInfo info = new PushMessageInfo();
            info._id = c.getInt(c.getColumnIndex("_id"));
            info.msgId = c.getString(c.getColumnIndex("msgId"));
            infos.add(info);
        }
        c.close();
        return infos;
    }

    public List<ErrorFileParamInfo> query_error_log_data() {
        List<ErrorFileParamInfo> infos = new ArrayList<>();
        Cursor c = queryByTableName(DBHelper.TABLE_NAME_REPORT_ERROR_LOG);
        while (c != null && c.moveToNext()) {
            int id = c.getInt(c.getColumnIndex("_id"));
            String deltaID = c.getString(c.getColumnIndex("delta_id"));
            String errorType = c.getString(c.getColumnIndex("error_type"));
            String uploadFile = c.getString(c.getColumnIndex("upload_file"));
            ErrorFileParamInfo info = new ErrorFileParamInfo(DeviceInfo.getInstance().mid, deltaID, errorType, uploadFile);
            info._id = id;
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

    public void addPushData(PushMessageInfo info) {
        db.beginTransaction();  //开始事务
        try {
            db.execSQL("INSERT INTO " + DBHelper.TABLE_NAME_PUSH_RESPONSE + " VALUES(null, ?)",
                    new Object[]{info.msgId});
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void addErrorFileData(ErrorFileParamInfo info) {
        try {
            db.beginTransaction();
            db.execSQL("INSERT INTO " + DBHelper.TABLE_NAME_REPORT_ERROR_LOG + " VALUES(null,?,?,?)",
                    new Object[]{(TextUtils.isEmpty(info.deltaID) ? "" : info.deltaID), info.errorType, info.uploadFile});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
