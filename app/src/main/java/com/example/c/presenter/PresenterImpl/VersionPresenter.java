package com.example.c.presenter.PresenterImpl;



import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;


import com.example.c.been.FileInfo;
import com.example.c.been.ResultData;
import com.example.c.dao.DaoImpl.FileDaoImpl;
import com.example.c.dao.IFileInfoDao;
import com.example.c.presenter.Presenter.IVersionPresenter;
import com.example.c.presenter.common.BasePresenter;
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

public class VersionPresenter extends BasePresenter<VersionManagerActivity> implements IVersionPresenter {


    private FileInfo downloadFileInfo ;
    private final  String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    private Handler handler;
    private IFileInfoDao fileInfoDao;

    private String registerDeviceUrl;  //注册设备接口
    private String checkVersionUrl;  //检查更新接口
    private String deviceSecret; //singn key
    private String deviceId;    //设备id
    private String mid;
    private String oem;
    private String models; //型号
    private String platform; //平台
    private String deviceType;
    private String sdkversion; //sdk版本
    private String appversion;
    private String version;  //当前版本号
    private String networkType; //网络类型 （WIFI 4G）
    private String productId;
    private String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";

    public static final int RESULT_LATES_VERSION_INFO_ERROR = 0;
    public static final int RESULT_LATES_VERSION_INFO_SUCCESS = 1;
    public static final int DOWNLOADING_PROGRESS = 2;
    public static final int DOWNLOADING_ERROE = 3;
    public static final int SHUNT_PROGRESS_DIALOG = 4;

    public VersionPresenter(){

    }



    @Override
    public void deviceRegister() {

            Logger.d("访问"+registerDeviceUrl+"注册设备!!!!!!");

            long timestamp = new Date().getTime()/1000;
            Logger.d("设备id:"+deviceId);
            String signInfo = mid + productId + timestamp;
            String sign = Codec2.getHmacMd5Str(signInfo, productSecret);
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
            OkHttpManager.getInstance().getStringDoPostJsonSync(registerDeviceUrl,params);
            Gson mGson = new Gson();

            String responseBody =  OkHttpManager.getInstance().getStringDoPostJsonSync(registerDeviceUrl,params);
            Logger.d("接收返回数据：" + responseBody);
            ResultData resultData = mGson.fromJson(responseBody,ResultData.class);
            Logger.d("status:" + resultData.getStatus());
            if(!"1000".equals(resultData.getStatus())){
                sendMessage(RESULT_LATES_VERSION_INFO_ERROR,resultData.getMsg());
                return;
            }

            Map<String,Object> dataMap = resultData.getData();
            deviceSecret =  mGson.toJson(dataMap.get("deviceSecret"));
            deviceId = mGson.toJson(dataMap.get("deviceId"));
            deviceSecret = deviceSecret.replace("\"","");
            deviceId = deviceId.replace("\"","");
            Logger.d("返回得到deviceSecret："+deviceSecret+'\n'+
                    "返回得到deviceId:"+deviceId);
            //关闭progressDialog
            sendMessage(SHUNT_PROGRESS_DIALOG, null);
    }

    @Override
    public void checkLatestVersion() {
            Logger.d("访问" + checkVersionUrl);
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
        Response response = null;
        try {
             response = client.newCall(request).execute();
             String responseBody = response.body().string();
                Logger.d("接收返回数据："+responseBody);
                ResultData resultData = mGson.fromJson(responseBody,ResultData.class);
                //失败弹出提示信息
                if(!"1000".equals(resultData.getStatus())){
                    sendMessage(RESULT_LATES_VERSION_INFO_ERROR,resultData.getMsg());
                    return;
                }
                //解析json 字符串获取版本信息
                Map<String,Object> data = resultData.getData();
                Map<String,Object> version = (Map<String, Object>) data.get("version");
                //结果信息发送给主线程
                sendMessage(RESULT_LATES_VERSION_INFO_SUCCESS, (String) version.get("versionAlias"));



                downloadFileInfo = new FileInfo();

                //保存要下载文件的MD5sum
                downloadFileInfo.setMd5sum((String) version.get("md5sum"));
                //保存新版本下载地址和下载文件名称
                downloadFileInfo.setFilenameAndUrl(((String) version.get("deltaUrl")).replace("\"",""));
                //异步获取新版本下载文件大小
                DownloadManagerUtil.getFileLength(downloadFileInfo);
                Logger.d("要下载的文件信息:" + downloadFileInfo.toString());
                //关闭progressDialog
                sendMessage(SHUNT_PROGRESS_DIALOG, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadLatestVersionFile() {

        //如果文件是不正在下载中或者下载完成，则往数据库重新插入下载信息
        if(!compareDbFileInfo(downloadFileInfo)){
            File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
            if(file.exists()){
                file.delete();
            };
            Logger.d("正在插入新下载信息！！！！");
            //往数据库插入新下载数据
            fileInfoDao.insertFileInfo(downloadFileInfo);
            downloadFileInfo.setProgress(0);
            Logger.d("下载信息插入成功，文件总大小：" + downloadFileInfo.getLength());
            getView().initDownloadProgressMax(downloadFileInfo.getLength(),0);
        }

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

    /**
     * 将消息发送到队列
     * @param what
     * @param msg
     */
    private void sendMessage(int what,String msg){
        Message message = new Message();
        message.what = what;
        message.obj =  msg;
        handler.sendMessage(message);
    }

    /**
     * 比较文件信息对象和数据库文件信息是否一致
     * @param fileInfo 要比较的文件信息对象
     * @return
     */
    public boolean compareDbFileInfo(FileInfo fileInfo){
        if(fileInfo == null)
            return false;
        if(fileInfo.getFileName() == null || fileInfo.getFileName().isEmpty())
            return  false;
        FileInfo dbFileInfo = fileInfoDao.getFileInfo(fileInfo.getFileName());
        if(dbFileInfo == null)
            return  false;
        //如果数据库中的文件信息不是正在下载的状态中或者下载完成
        if(!dbFileInfo.isDownLoading()){
            Logger.d("文件不是正在下载中。。。。");
            return false;
        }
        //再对比数据库和服务器传过来的文件信息对比,看看是否一致
        if(!dbFileInfo.equals(downloadFileInfo)){
            Logger.d("文件信息不一致！！！！！");
            return false;
        }
        //文件信息一致，再检查文件是否存在
        File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
        if(!file.exists()){
            Logger.d(downloadFileInfo.getFileName()+"文件不存在！！！！！");
            return false;
        }
        //对比下载到一半的文件大小和数据库进度是否一致
        if(!(file.length() == dbFileInfo.getProgress())){
            //文件不存在，选择重新下载文件
            Logger.d("文件大小不匹配！文件下载进度/文件大小:" + dbFileInfo.getProgress() + "/" + file.length());
            return false;
        }
        fileInfo.setDownLoading(true);
        fileInfo.setProgress(dbFileInfo.getProgress());
        fileInfo.setDownLoading(dbFileInfo.isDownLoading());
        Logger.d("下载信息一致,文件总大小:" + downloadFileInfo.getLength()+'\n');
        //设置进度条最大值
        getView().initDownloadProgressMax(downloadFileInfo.getLength(),downloadFileInfo.getProgress());
        return true;
    }


    class VersionHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == RESULT_LATES_VERSION_INFO_ERROR){
                getView().hintError((String) msg.obj);
                return;
            }
            if(msg.what == DOWNLOADING_ERROE){
                getView().hintError((String) msg.obj);
                return;
            }
            if(msg.what == DOWNLOADING_PROGRESS){
                getView().setDownloadProgress(downloadFileInfo.getProgress(),downloadFileInfo.getLength());
            }
            if(msg.what == SHUNT_PROGRESS_DIALOG){
                getView().shuntProgressDialog();
            }
            getView().setLatestVersion(version);
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
                    sendMessage(DOWNLOADING_PROGRESS,null);
                    if(downloadFileInfo.getProgress() >= downloadFileInfo.getLength())
                        break;
                    if(downloadFileInfo.isStop())
                        break;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //下载完成，进行md5校验
            if(downloadFileInfo.getProgress() == downloadFileInfo.getLength()) {
                //数据库中文件信息保存为不是下载中
                downloadFileInfo.setDownLoading(false);
                File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
                String md5sum = Codec2.getMd5ByFile(file);
                Logger.d("计算出文件的MD5值为:" + md5sum + "服务器计算出的MD5值为:" + downloadFileInfo.getMd5sum());
                if (!md5sum.equals(downloadFileInfo.getMd5sum()) || !file.exists()) {
                    Logger.i("MD5不匹配！！！！！");
                    sendMessage(DOWNLOADING_ERROE,"文件下载错误");
                    file.delete();
                    return;
                }else {
                    Logger.i("MD5匹配！！！！！");
                }
            }
            int success = fileInfoDao.upDateFileInfoProgress(downloadFileInfo);
            if(success >0)
                Logger.d("保存数据库成功！！！！！！！！！");

        }
    }

    /**
     * 初始化数据
     */
    @Override
    public void init() {
        Logger.d("baseproperties:"+baseproperties);
        handler = new VersionHandler();
        fileInfoDao = new FileDaoImpl();
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
    public String getDeviceSecret() {
        return deviceSecret;
    }

    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
