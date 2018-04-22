package com.example.c.dao.code.DaoImpl;

import com.example.c.been.FileInfo;
import com.example.c.dao.code.IDao.IFileInfoDao;
import com.example.c.dao.code.code.BaseDao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FileInfoDao extends BaseDao implements IFileInfoDao {

    public FileInfoDao(){
        setTable();
    }

    private void setTable() {

    }

    @Override
    public FileInfo getBeen(int id) {
        return null;
    }

    @Override
    public int insertBeen(FileInfo fileInfo) {
        return 0;
    }

    @Override
    public int updateBeenForId(FileInfo fileInfo) {
        return 0;
    }

    @Override
    public int deleteBeen(FileInfo fileInfo) {
        return 0;
    }


}
