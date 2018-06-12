package com.abupdate.iot_libs.utils;

import android.content.Context;
import android.text.TextUtils;

import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.trace.Trace;

import java.io.File;

/**
 * Created by fighter_lee on 2017/5/16.
 */

public class DownloadUtils {

    public static String getCachePath(Context context) {
        String path = "";
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            //sd卡可用
            path = context.getExternalCacheDir().getAbsolutePath();
        }else{
            //sd卡不可用
            path = context.getCacheDir().getAbsolutePath();
        }
        return path;
    }

    public static File getCacheFile(Context context) {
        String deltaID = VersionInfo.getInstance().deltaID;
        if (TextUtils.isEmpty(deltaID)){
            Trace.d("DownloadUtils", "deleteCacheFile() deltaId is null,please do check version!");
            return null;
        }
        String s = DownloadUtils.getCachePath(context) + File.separator + deltaID + ".txt";
        return new File(s);
    }

    public static void deleteCacheFile(Context context) {

        String deltaID = VersionInfo.getInstance().deltaID;
        if (TextUtils.isEmpty(deltaID)){
            Trace.d("DownloadUtils", "deleteCacheFile() deltaId is null,please do check version!");
            return;
        }
        File file = new File(getCachePath(context) + File.separator + deltaID + ".txt");
        if (file.exists()){
            boolean delete = file.delete();
            Trace.d("DownloadUtils", "deleteCacheFile(): "+delete);
        }else{
            Trace.d("DownloadUtils", "deleteCacheFile() file is not exist!");
        }

    }

}
