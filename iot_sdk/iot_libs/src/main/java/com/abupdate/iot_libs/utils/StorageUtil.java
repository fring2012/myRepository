package com.abupdate.iot_libs.utils;


import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.abupdate.trace.Trace;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StorageUtil {

    public static final String ota_log_folder_name = "/otaLogs";
    private static final String TAG = "StorageUtil";

    public static String getOtaLogPath(Context context) {
        String path = "";
        String removableStorage = getStoragePath(context, true);
        if (!TextUtils.isEmpty(removableStorage)) {
            //存在外置T卡
            File file = new File(removableStorage + ota_log_folder_name);
            if (file.exists()) {
                path = file.getAbsolutePath();
            }
        } else {
            //使用内置T卡
            String internalStorage = getStoragePath(context, false);
            if (!TextUtils.isEmpty(internalStorage)) {
                File file = new File(internalStorage + ota_log_folder_name);
                if (file.exists()) {
                    path = file.getAbsolutePath();
                }
            }
        }

        if (TextUtils.isEmpty(path)) {
            File file = new File(Environment.getExternalStorageDirectory() + ota_log_folder_name);
            if (file.exists()) {
                path = file.getAbsolutePath();
            }
        }
        Trace.d(TAG,"getOtaLogPath() path = "+path);
        return path;
    }


    public static String getStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
