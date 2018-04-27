package com.example.c.dao;

import android.database.Cursor;

import com.example.c.been.FileInfo;
import com.example.c.dao.code.Dao;

import java.util.List;

public interface IFileInfoDao extends Dao{


    /**
     * 用文件名获取文件信息
     * @param fileName
     * @return
     */
    FileInfo getFileInfo(String fileName);



    /**
     * 更新数据库的下载进度
     * @param fileInfo
     * @return
     */
    int upDateFileInfoProgress(FileInfo fileInfo);

    /**
     * 插入下载信息
     * @param fileInfo
     * @return
     */
    long insertFileInfo(FileInfo fileInfo);

    /**
     * 删除数据库信息
     * @param fileInfo
     * @return
     */
    int deleteFileInfo(FileInfo fileInfo);

}
