package com.example.c.presenter.PresenterImpl;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;


import com.example.c.been.FileInfo;
import com.example.c.been.ResultData;
import com.example.c.dao.DaoImpl.FileDaoImpl;
import com.example.c.dao.IFileInfoDao;
import com.example.c.data.DownloadFileInfo;
import com.example.c.data.ParamsConfig;
import com.example.c.presenter.Presenter.IVersionPresenter;
import com.example.c.presenter.common.BasePresenter;
import com.example.c.service.VersionService;
import com.example.c.ui.activity.activity.VersionManagerActivity;
import com.example.c.utils.Codec2;
import com.example.c.utils.DownloadManagerUtil;
import com.example.c.utils.OkHttpManager;
import com.example.c.utils.PropertiesUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Version;

public class VersionPresenter extends BasePresenter<VersionManagerActivity> implements IVersionPresenter {



    private static IFileInfoDao fileInfoDao;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public ParamsConfig paramsConfig;



    public static final int RESULT_LATEST_VERSION_INFO_ERROR = 0;
    public static final int RESULT_LATEST_VERSION_INFO_SUCCESS = 1;
    public static final int DOWNLOADING_PROGRESS = 2;
    public static final int DOWNLOADING_ERROR = 3;
    public static final int SHUNT_PROGRESS_DIALOG = 4;
    public static final int INIT_PROGRESS = 5;
    public static final String BROADCAST_RECEIVER_VERSION = "VersionPresenter";


    public VersionPresenter(){

    }
    /**
     * 注册广播接收器
     */
    public void  registerBroadcastReceiver(){
        broadcastReceiver = new VersionBroadcastReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(getView());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_RECEIVER_VERSION);
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    };

    /**
     * 注销广播接收器
     */
    public void  unRegisterBroadcastReceiver(){
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    };



    @Override
    public void deviceRegister() {
            Logger.d("访问"+paramsConfig.registerDeviceUrl+"注册设备!!!!!!");
            VersionService.initContext(getView());
            VersionService.startVersionService(VersionService.DEVICE_REGISTER_TASK);
    }

    @Override
    public void checkLatestVersion() {

        VersionService.startVersionService(VersionService.CHECK_LATEST_VERSION_TASK);
    }

    @Override
    public void downloadLatestVersionFile() {
        VersionService.startVersionService(VersionService.DOWNLOAD_LATEST_VERSION_TASK);
    }


    @Override
    public void stopDownloading() {
        DownloadFileInfo.getDownloadFileInfo().getFileInfo().setStop(true);
    }

    @Override
    public void upVersion() {
//        fileInfoDao.execSQL("drop table IF exists  file_info");
//        File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
//        String md5sum = Codec2.getMd5ByFile(file);
//        Logger.d("计算出文件的MD5值为:" + md5sum + "服务器计算出的MD5值为:" + downloadFileInfo.getMd5sum());
//        if(md5sum.equals(downloadFileInfo.getMd5sum()))
//            Logger.d("MD5匹配！！！！");
//        else
//            Logger.i("MD5不匹配！！！！！");
//        downloadFileInfo = null;
    }





    private class VersionBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultState = intent.getIntExtra("resultState",9);
            String msg = intent.getStringExtra("msg");
            if(resultState == RESULT_LATEST_VERSION_INFO_ERROR){
                getView().hintError((String) msg);
                return;
            }
            if(resultState == DOWNLOADING_ERROR){
                getView().hintError((String) msg);
                return;
            }
            if(resultState == DOWNLOADING_PROGRESS){
                getView().setDownloadProgress(getFileInfo().getProgress(),getFileInfo().getLength());
            }
            if(resultState == INIT_PROGRESS){
                getView().initDownloadProgressMax(getFileInfo().getLength(),getFileInfo().getProgress());
            }
            if(resultState == SHUNT_PROGRESS_DIALOG){
                getView().shuntProgressDialog();
            }
            getView().setLatestVersion(paramsConfig.version);
        }
    }




    /**
     * 初始化数据
     */
    @Override
    public void init() {
        fileInfoDao = new FileDaoImpl();
        paramsConfig = ParamsConfig.getParamsConfig();
        paramsConfig.init(getView());
    }
    public FileInfo getFileInfo(){
        return DownloadFileInfo.getDownloadFileInfo().getFileInfo();
    }

    public static IFileInfoDao getFileInfoDao(){
        return fileInfoDao;
    }

}
