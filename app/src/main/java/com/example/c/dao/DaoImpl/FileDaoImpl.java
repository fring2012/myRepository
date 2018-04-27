package com.example.c.dao.DaoImpl;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.c.been.FileInfo;
import com.example.c.dao.IFileInfoDao;
import com.example.c.dao.code.BaseDao;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class FileDaoImpl extends BaseDao implements IFileInfoDao {

    public FileDaoImpl(){
        createFileInfoTable();
    }
    private void createFileInfoTable(){
        //如果file_name表不存在，创建表
        database.execSQL("create table if not exists file_info(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name VARCHAR NOT NULL UNIQUE," +
                "url TEXT NOT NULL," +
                "length TEXT DEFAULT 0 NOT NULL ," +
                "progress TEXT DEFAULT 0 NOT NULL," +
                "is_stop INTEGER DEFAULT 0 NOT NULL," +
                "is_downloading INTEGER DEFAULT 1 NOT NULL," +
                "md5sum TEXT) ");
    }
    @Override
    public FileInfo getFileInfo(String fileName){
        FileInfo dbFileInfo = null;
        Cursor cursor = getCursor(fileName);
        if(cursor.moveToNext())
            dbFileInfo =  getFileInfo(cursor);
        cursor.close();
        return dbFileInfo;
    }


    private Cursor getCursor(String fileName){
        Cursor cursor = database.query("file_info", null,"file_name = ?", new String[]{fileName}, null, null,null);
        return cursor;
    }

    @Override
    public int upDateFileInfoProgress(FileInfo fileInfo) {
        ContentValues cv = new ContentValues();
        cv.put("progress",fileInfo.getProgress());
        if(!fileInfo.isDownLoading())
            cv.put("is_downloading", 0);
        return database.update("file_info",cv,"file_name=?", new String[]{fileInfo.getFileName()});
    }

    @Override
    public long insertFileInfo(FileInfo fileInfo) {
        //清理数据库和文件
        deleteFileInfo(fileInfo);
        ContentValues cv = new ContentValues();
        cv.put("file_name",fileInfo.getFileName());
        cv.put("url",fileInfo.getUrl());
        cv.put("length",fileInfo.getLength());
        cv.put("md5sum",fileInfo.getMd5sum());

        return database.insert("file_info",null,cv);
    }

    @Override
    public int deleteFileInfo(FileInfo fileInfo) {
        return   database.delete("file_info","file_name=?", new String[]{fileInfo.getFileName()});
    }


    private FileInfo getFileInfo(Cursor cursor){
        FileInfo dbFileInfo = new FileInfo();
        dbFileInfo.setFileName(cursor.getString(1));
        dbFileInfo.setUrl(cursor.getString(2));
        dbFileInfo.setLength(cursor.getInt(3));
        dbFileInfo.setProgress(cursor.getInt(4));
        if (cursor.getInt(5) == 1)
            dbFileInfo.setStop(true);
        else
            dbFileInfo.setStop(false);
        if (cursor.getInt(6) == 1)
            dbFileInfo.setDownLoading(true);
        else
            dbFileInfo.setDownLoading(false);
        dbFileInfo.setMd5sum(cursor.getString(7));

        return dbFileInfo;
    }


}
