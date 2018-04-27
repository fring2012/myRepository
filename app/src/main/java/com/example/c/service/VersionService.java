package com.example.c.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.c.been.FileInfo;
import com.example.c.ui.activity.activity.VersionManagerActivity;

public class VersionService extends IntentService{
    private static Context a;


    public static final String DEVICE_REGISTER_TASK = "deviceRegister"; //设备注册
    public static final String CHECK_LATEST_VERSION_TASK = "checkLatestVersion";//检测最新版本
    public static final String DOWNLOAD_LATEST_VERSION_TASK = "downloadLatestVersion";//下载最新版本

    public VersionService() {
        super("OkHttpService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String task = intent.getStringExtra("task");
        if(DEVICE_REGISTER_TASK.equals(task)){
            //访问服务器注册设备
            ((VersionManagerActivity)a).getVersionPresenter().deviceRegister();
        }else if(CHECK_LATEST_VERSION_TASK.equals(task)){
            //检测版本号
            ((VersionManagerActivity)a).getVersionPresenter().checkLatestVersion();
        }else if(DOWNLOAD_LATEST_VERSION_TASK.equals(task)){
            //下载
            ((VersionManagerActivity)a).getVersionPresenter().downloadLatestVersionFile();
        }else {

        }
    }



    public static void initContext(Context context){
        a = context;
    }
}
