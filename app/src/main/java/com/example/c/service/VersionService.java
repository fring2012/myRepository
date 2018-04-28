package com.example.c.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;

import com.example.c.been.FileInfo;
import com.example.c.been.ResultData;
import com.example.c.data.DownloadFileInfo;
import com.example.c.data.ParamsConfig;
import com.example.c.presenter.PresenterImpl.VersionPresenter;
import com.example.c.ui.activity.activity.VersionManagerActivity;
import com.example.c.utils.Codec2;
import com.example.c.utils.DownloadManagerUtil;
import com.example.c.utils.OkHttpManager;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.Date;
import java.util.Map;

import okhttp3.FormBody;

public class VersionService extends IntentService {
    private static Context a;

    private final  String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    public static final String DEVICE_REGISTER_TASK = "deviceRegister"; //设备注册
    public static final String CHECK_LATEST_VERSION_TASK = "checkLatestVersion";//检测最新版本
    public static final String DOWNLOAD_LATEST_VERSION_TASK = "downloadLatestVersion";//下载最新版本

    public VersionService() {
        super("OkHttpService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String task = intent.getStringExtra("task");
        if (DEVICE_REGISTER_TASK.equals(task)) {
            //访问服务器注册设备
            deviceRegister();
        } else if (CHECK_LATEST_VERSION_TASK.equals(task)) {
            //检测版本号
            checkLatestVersion();
        } else if (DOWNLOAD_LATEST_VERSION_TASK.equals(task)) {
            //下载
            downloadLatestVersionFile();
        } else {

        }
    }

    /**
     * 开启VersionService
     *
     * @param task
     */
    public static void startVersionService(String task) {
        Intent intent = new Intent(a, VersionService.class);
        intent.putExtra("task", task);
        a.startService(intent);
    }

    public static void initContext(Context context) {
        a = context;
    }


    public void deviceRegister() {
        Logger.d("getParamsConfig()");
        Logger.d("访问" + getParamsConfig().registerDeviceUrl + "注册设备!!!!!!");

        long timestamp = new Date().getTime() / 1000;
        Logger.d("设备id:" + getParamsConfig().deviceId);
        String signInfo = getParamsConfig().mid + getParamsConfig().productId + timestamp;
        String sign = Codec2.getHmacMd5Str(signInfo, getParamsConfig().productSecret);
        Map<String, Object> params = new ArrayMap<>();
        params.put("mid", getParamsConfig().mid);
        params.put("oem", getParamsConfig().oem);
        params.put("models", getParamsConfig().models);
        params.put("platform", getParamsConfig().platform);
        params.put("deviceType", getParamsConfig().deviceType);
        params.put("timestamp", timestamp);//1523606446l
        params.put("sign", sign);  //"6144af67ee2af9621127bc25ecf85660"
        params.put("sdkversion", getParamsConfig().sdkversion);
        params.put("appversion", getParamsConfig().appversion);
        params.put("version", getParamsConfig().version);
        params.put("networkType", getParamsConfig().networkType);
        params.put("productId", getParamsConfig().productId);
        Gson mGson = new Gson();

        String responseBody = OkHttpManager.getInstance().getStringDoPostJsonSync(getParamsConfig().registerDeviceUrl, params);
        Logger.d("接收返回数据：" + responseBody);
        ResultData resultData = mGson.fromJson(responseBody, ResultData.class);
        Logger.d("status:" + resultData.getStatus());
        if (!"1000".equals(resultData.getStatus())) {
            sendBroadcast(resultData.getMsg(), VersionPresenter.RESULT_LATEST_VERSION_INFO_ERROR);
            return;
        }

        Map<String, Object> dataMap = resultData.getData();
        getParamsConfig().deviceSecret = mGson.toJson(dataMap.get("deviceSecret"));
        getParamsConfig().deviceId = mGson.toJson(dataMap.get("deviceId"));
        getParamsConfig().deviceSecret = getParamsConfig().deviceSecret.replace("\"", "");
        getParamsConfig().deviceId = getParamsConfig().deviceId.replace("\"", "");
        Logger.d("返回得到deviceSecret：" + getParamsConfig().deviceSecret + '\n' +
                "返回得到deviceId:" + getParamsConfig().deviceId);
        //关闭progressDialog
        sendBroadcast(null, VersionPresenter.SHUNT_PROGRESS_DIALOG);
    }

    private void checkLatestVersion() {
        Logger.d("访问" + getParamsConfig().checkVersionUrl);
        long timestamp = new Date().getTime() / 1000;
        //传入deviceId、productId、timestamp和deviceSecret作为key通过HmacMd5计算sign
        String sign = Codec2.getHmacMd5Str(getParamsConfig().deviceId + getParamsConfig().productId + timestamp, getParamsConfig().deviceSecret);
        Logger.d("访问" + getParamsConfig().checkVersionUrl + "检测版本号" + "deviceId:" + getParamsConfig().deviceId);
        FormBody.Builder formBody = new FormBody.Builder();
        Map<String, Object> params = new ArrayMap<>();
        params.put("mid", getParamsConfig().mid);//"e0aee11a"
        params.put("version", getParamsConfig().version);
        params.put("timestamp", timestamp);//1523616493
        params.put("sign", sign);//"96b226a54d5ce3d2adcd037fe50cc143"
        params.put("networkType", getParamsConfig().networkType);
        Gson mGson = new Gson();
        //访问checkVersionUrl检查最新版本
        String responseBody = OkHttpManager.getInstance().getStringDoPostJsonSync(getParamsConfig().checkVersionUrl, params);
        Logger.d("接收返回数据：" + responseBody);
        ResultData resultData = mGson.fromJson(responseBody, ResultData.class);
        //失败弹出提示信息
        if (!"1000".equals(resultData.getStatus())) {
            sendBroadcast(resultData.getMsg(), VersionPresenter.RESULT_LATEST_VERSION_INFO_ERROR);
            return;
        }
        //解析json 字符串获取版本信息
        Map<String, Object> data = resultData.getData();
        Map<String, Object> version = (Map<String, Object>) data.get("version");
        //结果信息发送给主线程
        sendBroadcast((String) version.get("versionAlias"), VersionPresenter.RESULT_LATEST_VERSION_INFO_SUCCESS);
        DownloadFileInfo.getDownloadFileInfo().setFileInfo(new FileInfo());
        //保存要下载文件的MD5sum
        getFileInfo().setMd5sum((String) version.get("md5sum"));
        //保存新版本下载地址和下载文件名称
        getFileInfo().setFilenameAndUrl(((String) version.get("deltaUrl")).replace("\"", ""));
        //异步获取新版本下载文件大小
        DownloadManagerUtil.getFileLength(getFileInfo());
        Logger.d("要下载的文件信息:" + getFileInfo().toString());
        //关闭progressDialog
        sendBroadcast(null, VersionPresenter.SHUNT_PROGRESS_DIALOG);
    }

    private void downloadLatestVersionFile() {
       // 如果文件是不正在下载中或者下载完成，则往数据库重新插入下载信息
        if(!compareDbFileInfo(getFileInfo())){
            File file = new File(DATABASE_PATH, getFileInfo().getFileName());
            if(file.exists()){
                file.delete();
            };
            Logger.d("正在插入新下载信息！！！！");
            //往数据库插入新下载数据
            VersionPresenter.getFileInfoDao().insertFileInfo(getFileInfo());
            getFileInfo().setProgress(0);
            Logger.d("下载信息插入成功，文件总大小：" + getFileInfo().getLength());
            sendBroadcast(null,VersionPresenter.INIT_PROGRESS);
            //getView().initDownloadProgressMax(getFileInfo().getLength(),0);
        }

        Logger.d("要下的文件信息:" + getFileInfo().toString()+";开始下载文件");
        //创建下载进度监听
        new DownloadProcess().start();
        //调用下载程序
        DownloadManagerUtil.downloadPontFile(getFileInfo());
    }
    /**
     *
     */
    public boolean compareDbFileInfo(FileInfo fileInfo){
        FileInfo dbFileInfo = VersionPresenter.getFileInfoDao().getFileInfo(fileInfo.getFileName());
        Logger.d("dbFileInfo:" + dbFileInfo.toString() + "\n" + "fileInfo:" + fileInfo.toString());
        if(!dbFileInfo.equals(fileInfo)){
            Logger.d("文件信息不匹配！！！！");
            return false;
        }
        if(!dbFileInfo.isDownLoading()){
            Logger.d("文件信息不是正在下载中！！！！");
            return false;
        }
        File file = new File(DATABASE_PATH,fileInfo.getFileName());
        if(!file.exists()){
            Logger.d("文件不存在！！！！");
            return false;
        }
        fileInfo.setProgress(dbFileInfo.getProgress());
        sendBroadcast(null,VersionPresenter.INIT_PROGRESS);
        return true;
    }

    /**
     * 发送广播
     *
     * @param msg
     * @param resultState
     */
    private void sendBroadcast(String msg, int resultState) {
        Intent intent = new Intent();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(null);
        intent.putExtra("msg", msg);
        intent.putExtra("resultState", resultState);
        intent.setAction(VersionPresenter.BROADCAST_RECEIVER_VERSION);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * 获取正在下载文件的信息
     *
     * @return
     */
    public FileInfo getFileInfo() {
        return DownloadFileInfo.getDownloadFileInfo().getFileInfo();
    }

    /**
     *
     */
    public ParamsConfig getParamsConfig(){
        return ParamsConfig.getParamsConfig();
    }


    /**
     * 监听下载进度线程
     */
    public  class DownloadProcess extends Thread{
        @Override
        public void run() {
            super.run();
            //创建下载进度监听
            while(true){
                try {
                    Thread.sleep(100);
                    sendBroadcast(null,VersionPresenter.DOWNLOADING_PROGRESS);
                    if(getFileInfo().getProgress() >= getFileInfo().getLength())
                        break;
                    if(getFileInfo().isStop())
                        break;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //下载完成，进行md5校验
            if(getFileInfo().getProgress() == getFileInfo().getLength()) {
                //数据库中文件信息保存为不是下载中
                getFileInfo().setDownLoading(false);
                File file = new File(DATABASE_PATH, getFileInfo().getFileName());
                String md5sum = Codec2.getMd5ByFile(file);
                Logger.d("计算出文件的MD5值为:" + md5sum + "服务器计算出的MD5值为:" + getFileInfo().getMd5sum());
                if (!md5sum.equals(getFileInfo().getMd5sum()) || !file.exists()) {
                    Logger.i("MD5不匹配！！！！！");
                    sendBroadcast("文件下载错误",VersionPresenter.DOWNLOADING_ERROR);
                    file.delete();
                    return;
                }else {
                    Logger.i("MD5匹配！！！！！");
                }
            }
            int success = VersionPresenter.getFileInfoDao().upDateFileInfoProgress(getFileInfo());
            if(success >0)
                Logger.d("保存数据库成功！！！！！！！！！");

        }
    }
}
