package com.abupdate.iot_libs.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类
 */
public class SPFTool {

    private static final String SP_NAME_IPORT = "iport_sp";
    public static final String KEY_SHOULD_REPORT = "key_should_report";

    /**
     * 保存版本，供开机后做版本对比，若不同，则认为是ota升级
     */
    private static SharedPreferences.Editor editor;

    /**
     * 注册信息
     */
    public static final String KEY_DEVICE_SECRET = "deviceSecret";
    public static String KEY_DEVICE_ID = "key_device_id";

    /**
     * 项目信息
     */
    public static final String KEY_PRODUCT_SECRET = "key_product_secret";
    public static String KEY_PRODUCT_ID = "key_product_id";

    /**
     * 保存版本，供开机后做版本对比，若不同，则认为是ota升级
     */
    public static final String KEY_UPDATE_FILE_PATH = "key_update_file_path";

    /**
     * 保存版本，供开机后做版本对比，若不同，则认为是ota升级
     */
    public static final String KEY_VERSION_NAME = "key_version_name";

    /**
     * 升级包的deltaId,用于升级上报
     */
    public static final String KEY_DELTAID = "KEY_DELTAID";

    /**
     * 保存版本信息，用于开机启动后进行升级等需求
     */
    public static final String KEY_VERSION_INFO = "key_version_info";

    /**
     * 保存上次升级时间
     */
    public static final String KEY_LAST_RECOVERY_TIME = "key_last_recovery_time";

    private static Context sCx;

    public static void initContext(Context context) {
        sCx = context;
    }

    public static void putInt(String key,
                              int value) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(String key, int defaultValue) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static void putBoolean(String key,
                                  boolean value) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void putLong(String key,
                               long value) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(String key, long defaultValue) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defaultValue);
    }


    public static void putString(String key,
                                 String value) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(String key,
                                   String defValue) {
        SharedPreferences sharedPreferences = sCx.getSharedPreferences(
                SP_NAME_IPORT, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defValue);
    }
}
