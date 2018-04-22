package com.example.c.presenter.PresenterImpl;



import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;


import com.example.c.been.FileInfo;
import com.example.c.been.ResultData;
import com.example.c.presenter.Presenter.IVersionPresenter;
import com.example.c.presenter.common.BasePresenter;
import com.example.c.ui.activity.activity.VersionManagerActivity;
import com.example.c.utils.Codec2;
import com.example.c.utils.DownloadManagerUtil;
import com.example.c.utils.PropertiesUtils;
import com.example.c.utils.SqliteUtil;
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

public class VersionPresenter extends BasePresenter<VersionManagerActivity> implements IVersionPresenter {


    private FileInfo downloadFileInfo ;
    private final  String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    private Handler handler;
    private SQLiteDatabase db;

    private String registerDeviceUrl;  //注册设备接口
    public String checkVersionUrl;  //检查更新接口
    public   String deviceSecret; //singn key
    public  String deviceId;    //设备id
    private   String mid;
    private   String oem;
    private   String models; //型号
    private   String platform; //平台
    private   String deviceType;
    private   String sdkversion; //sdk版本
    private   String appversion;
    private   String version;  //当前版本号
    private   String networkType; //网络类型 （WIFI 4G）
    private   String productId;
    private   String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";

    public  final int RESULT_LATES_VERSION_INFO_ERROR = 0;
    public  final int RESULT_LATES_VERSION_INFO_SUCCESS = 1;
    public  final int DOWNLOADING_PROGRESS = 2;
    public  final int DOWNLOADING_ERROE = 3;

    public VersionPresenter(){
        init();
    }

    @Override
    public void deviceRegister() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Logger.d("访问"+registerDeviceUrl+"注册设备!!!!!!");

                long timestamp = new Date().getTime()/1000;
                Logger.d("设备id:"+deviceId);
                String signInfo = mid + productId + timestamp;
                String sign = Codec2.getHmacMd5Str(signInfo, productSecret);
                FormBody.Builder formBody = new FormBody.Builder();
                Map<String, Object> params = new ArrayMap<>();
                params.put("mid", mid);
                params.put("oem",oem);
                params.put("models",models);
                params.put("platform",platform);
                params.put("deviceType",deviceType);
                params.put("timestamp", timestamp);//1523606446l
                params.put("sign", sign);  //"6144af67ee2af9621127bc25ecf85660"
                params.put("sdkversion",sdkversion);
                params.put("appversion",appversion);
                params.put("version", version);
                params.put("networkType",networkType);
                params.put("productId",productId);
                Gson mGson = new Gson();
                String jsonParams = mGson.toJson(params);
                Logger.d("发送请求参数:" + jsonParams );
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, jsonParams);


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(registerDeviceUrl)
                        .post(requestBody)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    Logger.d("接收返回数据：" + responseBody);
                    ResultData resultData = mGson.fromJson(responseBody,ResultData.class);
                    Logger.d("status:" + resultData.getStatus());
                    if(!"1000".equals(resultData.getStatus())){
                        Message msg = new Message();
                        msg.obj = resultData.getMsg();
                        msg.what = RESULT_LATES_VERSION_INFO_ERROR;
                        handler.sendMessage(msg);
                        return;
                    }

                    Map<String,Object> dataMap = resultData.getData();
                    deviceSecret =  mGson.toJson(dataMap.get("deviceSecret"));
                    deviceId = mGson.toJson(dataMap.get("deviceId"));
                    deviceSecret = deviceSecret.replace("\"","");
                    deviceId = deviceId.replace("\"","");
                    Logger.d("返回得到deviceSecret："+deviceSecret+'\n'+
                            "返回得到deviceId:"+deviceId);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void checkLatestVesion() {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    Logger.d("访问" + checkVersionUrl + "检测版本号" + "deviceId:");
                    long timestamp = new Date().getTime()/1000;
                    //传入deviceId、productId、timestamp和deviceSecret作为key通过HmacMd5计算sign
                    String sign = Codec2.getHmacMd5Str(deviceId + productId + timestamp, deviceSecret);
                    Logger.d("访问" + checkVersionUrl + "检测版本号" + "deviceId:" +deviceId);
                    FormBody.Builder formBody = new FormBody.Builder();
                    Map<String, Object> params = new ArrayMap<>();
                    params.put("mid", mid);//"e0aee11a"
                    params.put("version", version);
                    params.put("timestamp",  timestamp);//1523616493
                    params.put("sign", sign);//"96b226a54d5ce3d2adcd037fe50cc143"
                    params.put("networkType",networkType);
                    Gson mGson = new Gson();
                    String jsonParams = mGson.toJson(params);
                    Logger.d("发送请求参数："+jsonParams);

                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = RequestBody.create(JSON, jsonParams);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(checkVersionUrl)
                            .post(requestBody)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    Logger.d("接收返回数据："+responseBody);
                    ResultData resultData = mGson.fromJson(responseBody,ResultData.class);
                    //失败弹出提示信息
                    if(!"1000".equals(resultData.getStatus())){
                        Logger.d(responseBody);
                        Message msg = new Message();
                        msg.what = RESULT_LATES_VERSION_INFO_ERROR;
                        msg.obj = resultData.getMsg();
                        handler.sendMessage(msg);
                        return;
                    }
                    //解析json 字符串获取版本信息
                    Map<String,Object> data = resultData.getData();
                    Map<String,Object> version = (Map<String, Object>) data.get("version");
                    //结果信息发送给主线程
                    Message msg = new Message();
                    msg.what = RESULT_LATES_VERSION_INFO_SUCCESS;
                    msg.obj =  version.get("versionAlias");
                    handler.sendMessage(msg);

                    if(downloadFileInfo == null){
                        downloadFileInfo = new FileInfo();
                    }
                    //保存要下载文件的MD5sum
                    downloadFileInfo.setMd5sum((String) version.get("md5sum"));
                    //保存新版本下载地址和下载文件名称
                    downloadFileInfo.setFilenameAndUrl(((String) version.get("deltaUrl")).replace("\"",""));
                    //异步获取新版本下载文件大小
                    DownloadManagerUtil.getFileLength(downloadFileInfo);
                    while (downloadFileInfo.getLength() == 0){

                    }
                    Logger.d("要下载的文件信息:" + downloadFileInfo.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void downloadLaterVersionFile() {

        downloadFileInfo = new FileInfo();
        //检测版本更新
        checkLatestVesion();

        while (downloadFileInfo.getLength() == 0){

        }
        Logger.d("准备下载新版本！！！！！！");


        //如果file_name表不存在，创建表
        db.execSQL("create table if not exists file_info(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name VARCHAR NOT NULL UNIQUE," +
                "url TEXT NOT NULL," +
                "length TEXT DEFAULT 0 NOT NULL ," +
                "finished TEXT DEFAULT 0 NOT NULL," +
                "is_stop INTEGER DEFAULT 0 NOT NULL," +
                "is_downloading INTEGER DEFAULT 1 NOT NULL," +
                "md5sum TEXT) ");
        //获取查询结果集
        final Cursor cursor = db.query("file_info", null,"file_name = ?", new String[]{downloadFileInfo.getFileName()}, null, null,null);

        while (cursor.moveToNext()) {
            Logger.d("已经存在该文件信息！正在验证下载信息是否一致!!!!!!");
            FileInfo dbFileInfo = new FileInfo();
            dbFileInfo.setFileName(cursor.getString(1));
            dbFileInfo.setUrl(cursor.getString(2));
            dbFileInfo.setLength(cursor.getInt(3));
            dbFileInfo.setFinished(cursor.getInt(4));
            if (cursor.getInt(5) == 1)
                dbFileInfo.setStop(true);
            else
                dbFileInfo.setStop(false);
            if (cursor.getInt(6) == 1)
                dbFileInfo.setDownLoading(true);
            else
                dbFileInfo.setDownLoading(false);
            dbFileInfo.setMd5sum(cursor.getString(7));
            //如果数据库中的文件信息不是正在下载的状态中或者下载完成
            if(!dbFileInfo.isDownLoading()){
                //选择重新下载文件
                downloadFileInfo.setDownLoading(false);
                Logger.d("文件不是正在下载中。。。。");
                break;
            }
            //再对比数据库和服务器传过来的文件信息对比,看看是否一致
            if(!dbFileInfo.equals(downloadFileInfo)){
                //则选择重新下载文件
                downloadFileInfo.setDownLoading(false);
                Logger.d("文件信息不一致！！！！！");
                break;
            }
            //文件信息一致，再检查文件是否存在
            File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
            if(!file.exists()){
                //文件不存在，选择重新下载文件
                Logger.d(downloadFileInfo.getFileName()+"文件不存在！！！！！");
                downloadFileInfo.setDownLoading(false);
                break;
            }
            //对比下载到一半的文件大小和数据库进度是否一致
            if(!(file.length() == dbFileInfo.getFinished())){
                //文件不存在，选择重新下载文件
                downloadFileInfo.setDownLoading(false);
                Logger.d("文件大小不匹配！文件下载进度/文件大小:" + dbFileInfo.getFinished() + "/" + file.length());
                break;
            }
            downloadFileInfo.setDownLoading(true);
            downloadFileInfo.setFinished(dbFileInfo.getFinished());
            downloadFileInfo.setDownLoading(dbFileInfo.isDownLoading());
            Logger.d("下载信息一致,文件总大小:" + downloadFileInfo.getLength()+'\n');
            //设置进度条最大值
            getActivity().initDownloadProgressMax(downloadFileInfo.getLength(),downloadFileInfo.getFinished());

        }
        //如果文件是不正在下载中或者下载完成，则往数据库重新插入下载信息
        if(!downloadFileInfo.isDownLoading()){
            //清理数据库和文件
            db.delete("file_info","file_name=?", new String[]{downloadFileInfo.getFileName()});
            File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
            if(file.exists()){
                file.delete();
            };

            Logger.d("正在插入新下载信息！！！！");
            ContentValues cv = new ContentValues();
            cv.put("file_name",downloadFileInfo.getFileName());
            cv.put("url",downloadFileInfo.getUrl());
            cv.put("length",downloadFileInfo.getLength());
            cv.put("md5sum",downloadFileInfo.getMd5sum());
            db.insert("file_info",null,cv);
            downloadFileInfo.setFinished(0);

            Logger.d("下载信息插入成功，文件总大小：" + downloadFileInfo.getLength());
            getActivity().initDownloadProgressMax(downloadFileInfo.getLength(),0);

        }
        cursor.close();
        Logger.d("要下的文件信息:" + downloadFileInfo.toString()+";开始下载文件");

        //创建下载进度监听
        new DownloadProcess().start();
        //调用下载程序
        DownloadManagerUtil.downloadPontFile(downloadFileInfo);

    }

    @Override
    public void stopDownloading() {
        downloadFileInfo.setStop(true);
    }

    @Override
    public void upVersion() {
        db.execSQL("drop table IF exists  file_info");
        File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
        String md5sum = Codec2.getMd5ByFile(file);
        Logger.d("计算出文件的MD5值为:" + md5sum + "服务器计算出的MD5值为:" + downloadFileInfo.getMd5sum());
        if(md5sum.equals(downloadFileInfo.getMd5sum()))
            Logger.d("MD5匹配！！！！");
        else
            Logger.i("MD5不匹配！！！！！");
        downloadFileInfo = null;
    }

    @Override
    public void init() {
        Logger.d("baseproperties:"+baseproperties);
        handler = new VersionHandler();
        db = SqliteUtil.geSQLiteDatabase(DATABASE_PATH);
        registerDeviceUrl = PropertiesUtils.propertiesUrl(baseproperties.getProperty("registerDeviceUrl"));// https://iotapi.adups.com/register/
        checkVersionUrl = PropertiesUtils.propertiesUrl(baseproperties.getProperty("checkVersionUrl"));
        deviceId = baseproperties.getProperty("deviceId");//252d1e9a82fdd0d64UC;
        mid = baseproperties.getProperty("mid");//e0aee11a;
        oem = baseproperties.getProperty("oem");//mi;
        models = baseproperties.getProperty("models");//HM-Note4X;
        platform = baseproperties.getProperty("platform");//MSM8625;
        deviceType = baseproperties.getProperty("deviceType");//phone;
        sdkversion = baseproperties.getProperty("sdkversion");//1.3.2_pre7;
        appversion = baseproperties.getProperty("appversion");//1.3.2_pre7;
        version = baseproperties.getProperty("version");//6.0.1;
        networkType = baseproperties.getProperty("networkType");//WIFI;
        productId = baseproperties.getProperty("productId");//1522029924;
    }


    class VersionHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == RESULT_LATES_VERSION_INFO_ERROR){
                getActivity().hintError((String) msg.obj);
                return;
            }
            if(msg.what == DOWNLOADING_ERROE){
                getActivity().hintError((String) msg.obj);
                return;
            }
            if(msg.what == DOWNLOADING_PROGRESS){
                getActivity().setDownloadProgress(downloadFileInfo.getFinished(),downloadFileInfo.getLength());
            }
            getActivity().setLatestVersion(version);
        }
    }

    /**
     * 监听下载进度线程
     */
    private class DownloadProcess extends Thread{
        @Override
        public void run() {
            super.run();
            //创建下载进度监听
            while(true){
                try {
                    Thread.sleep(100);
                    Message msg = new Message();
                    msg.what = DOWNLOADING_PROGRESS;
                    handler.sendMessage(msg);
                    if(downloadFileInfo.getFinished() >= downloadFileInfo.getLength())
                        break;
                    if(downloadFileInfo.isStop())
                        break;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ContentValues cv = new ContentValues();
            cv.put("finished",downloadFileInfo.getFinished());
            //下载完成，进行md5校验
            if(downloadFileInfo.getFinished() == downloadFileInfo.getLength()) {
                //数据库中文件信息保存为不是下载中
                cv.put("is_downloading",0);
                File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
                String md5sum = Codec2.getMd5ByFile(file);
                Logger.d("计算出文件的MD5值为:" + md5sum + "服务器计算出的MD5值为:" + downloadFileInfo.getMd5sum());
                if (!md5sum.equals(downloadFileInfo.getMd5sum()) || !file.exists()) {
                    Logger.i("MD5不匹配！！！！！");
                    Message msg = new Message();
                    msg.what = DOWNLOADING_ERROE;
                    msg.obj = "文件下载错误";
                    file.delete();
                    handler.sendMessage(msg);
                    return;
                }else {
                    Logger.i("MD5匹配！！！！！");
                }


            }
            int success = db.update("file_info",cv,"file_name=?", new String[]{downloadFileInfo.getFileName()});
            if(success >0)
                Logger.d("保存数据库成功！！！！！！！！！");

        }
    }
}
