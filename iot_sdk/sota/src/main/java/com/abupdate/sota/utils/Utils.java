package com.abupdate.sota.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class Utils {

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
                path = context.getExternalCacheDir() + "/sota_log.txt";
            } else {
                //小于6.0 存放于内置存储卡根目录
                path = Environment.getExternalStorageDirectory() + "/sota_log.txt";
            }
        } else {
            //sd卡不可用
            path = context.getCacheDir().getAbsolutePath() + File.separator + "sota_log.txt";
        }

        return path;
    }

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    public static String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

}
