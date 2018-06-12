package com.example.administrator.apk_up_receiver.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StrictMode;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.ProductInfo;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.trace.Trace;
import com.example.administrator.apk_up_receiver.BuildConfig;

import java.io.File;

public class App extends Application {
    private static final String TAG = "App";
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        Trace.d(TAG,"onCreate()");
        initFota();
    }
    //初始化参数
    private void initFota(){
        context = getApplicationContext();
        String version = getTargetPackageVersion();
        Trace.d(TAG,version);
        Trace.d(TAG,getExternalCacheDir() + "");
        try {
            OtaAgentPolicy.init(context)
                    .setMid(BuildConfig.mid)
                    .commit();

            if(VERSION.SDK_INT >= VERSION_CODES.M){
                OtaAgentPolicy.config.setUpdatePath(getExternalCacheDir().getAbsolutePath() + File.separator + "update.zip");
            }

            DeviceInfo.getInstance().version = version;
            DeviceInfo.getInstance().deviceType = BuildConfig.deviceType;
            DeviceInfo.getInstance().mid = BuildConfig.mid;
            DeviceInfo.getInstance().models = BuildConfig.model;
            DeviceInfo.getInstance().oem = BuildConfig.oem;
            DeviceInfo.getInstance().platform = BuildConfig.platform;
            ProductInfo.getInstance().productId = BuildConfig.productId;
            ProductInfo.getInstance().productSecret = BuildConfig.productSecret;

        } catch (FotaException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取目标apk版本号
     * @return
     */
    public static String getTargetPackageVersion(){
        PackageInfo appInfo = null;
        PackageManager packageManager = context.getPackageManager();
        try {
            appInfo = packageManager.getPackageInfo(BuildConfig.targetPackageName,0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo != null) {
            return appInfo.versionName;
        }
        return "";
    }
}
