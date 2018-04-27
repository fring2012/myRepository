package com.example.c.dao.code;

import android.database.sqlite.SQLiteDatabase;

public interface Dao {
    void getConnection();
    void  close();
    void execSQL(String sql);
}
