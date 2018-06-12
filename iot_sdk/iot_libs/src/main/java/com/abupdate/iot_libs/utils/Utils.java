package com.abupdate.iot_libs.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.abupdate.trace.Trace;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by fighter_lee on 2017/5/16.
 */

public class Utils {

    /**
     * 获取app的版本
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 * * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    /**
     * 设置Log日志存放路径
     *
     * @param context
     * @return
     */
    public static String setFotaLog(Context context) {

        String path;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //sd卡挂载
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //大于6.0 存放于Android/data/data/包名/cache 此路径不需要申请权限
                path = context.getExternalCacheDir() + "/iport_log.txt";
            } else {
                //小于6.0 存放于内置存储卡根目录
                path = Environment.getExternalStorageDirectory() + "/iport_log.txt";
            }
        } else {
            //sd卡不可用
            path = context.getCacheDir().getAbsolutePath() + File.separator + "iport_log.txt";
        }

        return path;
    }

    /**
     * 获取精确到秒的时间戳
     * @return
     */
    public static long getSecondTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 传入时间是否在区间内
     * 传入的时间格式为 10:10
     *
     * @param currentTime
     * @param from
     * @param to
     * @return
     */
    public static boolean timeCompare(String currentTime, Date from, Date to) {
        Trace.d("utils", "timeCompare() cur:" + currentTime + ",form:" + from.toString() + ",to:" + to.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date current = sdf.parse(currentTime);
            Date twenty_four = sdf.parse("24:00");
            Date zero = sdf.parse("00:00");
            if (from.after(to)) {
                //隔夜
                if (current.after(from) && current.before(twenty_four) ||
                        current.after(zero) && current.before(to)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (current.after(from) && current.before(to)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String map2String(Map map) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Object key : map.keySet()) {
            builder.append(key)
                    .append(":")
                    .append(map.get(key))
                    .append(",");
        }
        if (builder.length()>1){
            builder.delete(builder.length()-1,builder.length());
        }
        builder.append("]");
        return builder.toString();
    }

}
