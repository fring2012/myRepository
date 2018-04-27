package com.example.c.dao;

import android.database.Cursor;

import com.example.c.been.FileInfo;
import com.example.c.dao.code.Dao;

import java.util.List;

public interface IFileInfoDao extends Dao{
    /**
     * 创建表
     */
    void createFileInfoTable();

    /**
     * 用文件名获取文件信息
     * @param fileName
     * @return
     */
    FileInfo getFileInfo(String fileName);

    /**
     * 获取文件信息列表
     * @param fileName
     * @return
     */
    List<FileInfo> getFileInfoList(String fileName);

    /**
     * 获取结果集
     * @param fileName
     * @return
     */
    Cursor getCursor(String fileName);

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

}
