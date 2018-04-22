package com.example.c.dao.code.code;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.c.been.code.Column;
import com.example.c.been.code.Table;
import com.example.c.dao.code.code.code.Dao;
import com.example.c.utils.SqliteUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

public abstract class BaseDao<V> {
    protected static SQLiteDatabase db =  SQLiteDatabase.openOrCreateDatabase("appDemo.db",null);
    protected String table;
    protected List<String> columnName;
    public BaseDao(){

    }
    public void setTableInfo(Class<V> vclass){
        if(!vclass.isAnnotationPresent(Table.class)){
            String name = vclass.getSimpleName();
            table = name;
        }else {
            Annotation[] classAnnotation = vclass.getAnnotations();
            Table annotation = vclass.getAnnotation(Table.class);
            table = annotation.table();
        }
        Field[] fields = vclass.getDeclaredFields();
        for (Field f : fields) {
            if(!f.isAnnotationPresent(Column.class)){
                columnName.add(f.getName());
                continue;
            }
            Column fieldAnno = f.getAnnotation(Column.class);
            columnName.add(fieldAnno.cname());
        }

    }
    public V getBeen(String whereColumnName,String zh,Class<V> vclass) throws IllegalAccessException, InstantiationException {
        V v = vclass.newInstance();
        Cursor cursor = db.query(table,null,whereColumnName, new String[]{zh},null,null,null);

        Field[] fields = vclass.getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);
//            if(f.getGenericType())
//            Column fieldAnno = f.getAnnotation(Column.class);
//            columnName.add(fieldAnno.cname());
        }
       return v;
    }
    public V getBeen(Cursor cursor,Class<V> vclass){
        V v = null;
        try {
            v = vclass.newInstance();
            Field[] fields = vclass.getDeclaredFields();

            cursor.getExtras();
            for (Field f : fields) {
                f.setAccessible(true);

            }



        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return v;
    }


}
