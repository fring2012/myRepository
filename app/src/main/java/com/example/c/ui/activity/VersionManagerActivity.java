package com.example.c.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.c.appdemo.R;
import com.example.c.been.FileInfo;
import com.example.c.been.ResultData;
import com.example.c.utils.Codec2;
import com.example.c.utils.DownloadManagerUtil;
import com.example.c.utils.PropertiesUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by C on 2018/4/3.
 */

public class VersionManagerActivity extends Activity {
    private ProgressBar downloadProgress;
    private ProgressBar progress;
    private Button check;
    private Button down;
    private Button up;
    private Button stop;
    private TextView versionInfo;
    private FileInfo downloadFileInfo ;
    private DownloadManagerUtil dmu;
    private SQLiteDatabase db;
    final VersionHandle versionHandle = new VersionHandle();


    private String registerDeviceUrl;
    private TelephonyManager telephonyManager;
    private PackageManager packageManager;
    private String checkVersionUrl;
    private String latestVersion;
    //得到SD卡路径
    private final  String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/AppDemo/";

    private  String deviceSecret; //singn key
    private  String deviceId = "252d1e9a82fdd0d64UC";
    private  final String mid = "e0aee11a";
    private  final String oem = "mi";
    private  final String models = "HM-Note4X";
    private  final String platform = "MSM8625";
    private  final String deviceType = "phone";
    private  final String sdkversion = "1.3.2_pre7";
    private  final String appversion = "1.3.2_pre7";
    private  final String version = "6.0.1";
    private  final String networkType = "WIFI";
    private  final String productId = "1522029924";
    private  final String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";

    private  final int RESULT_LATES_VERSION_INFO_ERROR = 0;
    private  final int RESULT_LATES_VERSION_INFO_SUCCESS = 1;
    private  final int DOWNLOADING_PROGRESS = 2;
    private  final int DOWNLOADING_ERROE = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_manager);
        downloadProgress = (ProgressBar) findViewById(R.id.downloadProgress);
        progress = (ProgressBar) findViewById(R.id.progress);

        stop = (Button) findViewById(R.id.stop);
        check = (Button) findViewById(R.id.check);
        down = (Button) findViewById(R.id.down);
        up = (Button) findViewById(R.id.up);
        versionInfo = (TextView) findViewById(R.id.versionInfo);

        registerDeviceUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("registerDeviceUrl") + productId;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        dmu = new DownloadManagerUtil();
        //连接数据库
        String datapath = DATABASE_PATH+"appDemo.db";
        File dir = new File(DATABASE_PATH);
        if(!dir.exists())
            dir.mkdirs();
        db = SQLiteDatabase.openOrCreateDatabase(datapath,null);
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
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON, jsonParams);


                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(registerDeviceUrl)
                        .post(requestBody)
                        .build();
                Logger.d(""+jsonParams);
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    String responseBody = response.body().string();
                    Logger.d("接收返回数据：" + responseBody);
                    ResultData resultData = mGson.fromJson(responseBody,ResultData.class);
                    Logger.d("status:" + resultData.getStatus());
                    if(!"1000".equals(resultData.getStatus())){
                        ///
                        //
                        //
                        //获取文件长度

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

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        try {

                            checkVersionUrl = "https://iotapi.adups.com/product/" + productId + "/" + deviceId + "/ota/checkVersion";
                            Logger.d("访问" + checkVersionUrl + "检测版本号");
                            long timestamp = new Date().getTime()/1000;
                            //传入deviceId、productId、timestamp和deviceSecret作为key通过HmacMd5计算sign
                            String sign = Codec2.getHmacMd5Str(deviceId + productId + timestamp, deviceSecret);
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
                                msg.what =  RESULT_LATES_VERSION_INFO_ERROR;
                                msg.obj = resultData.getMsg();
                                versionHandle.sendMessage(msg);
                                return;
                            }
                            //解析json 字符串获取版本信息
                            Map<String,Object> data = resultData.getData();
                            Map<String,Object> version = (Map<String, Object>) data.get("version");
                            //结果信息发送给主线程
                            Message msg = new Message();
                            msg.what = RESULT_LATES_VERSION_INFO_SUCCESS;
                            msg.obj =  version.get("versionAlias");
                            versionHandle.sendMessage(msg);

                            downloadFileInfo = new FileInfo();
                            //保存要下载文件的MD5sum
                            downloadFileInfo.setMd5sum((String) version.get("md5sum"));
                            //保存新版本下载地址和下载文件名称
                            downloadFileInfo.setFilenameAndUrl(((String) version.get("deltaUrl")).replace("\"",""));
                            //异步获取新版本下载文件大小
                            dmu.getFileLength(downloadFileInfo);
                            while (downloadFileInfo.getLength() == 0){

                            }

                            Logger.d("要下载的文件信息:" + downloadFileInfo.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        });

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查是否有权限
                if (ActivityCompat.checkSelfPermission(VersionManagerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VersionManagerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return;
                }
                if (downloadFileInfo == null){
                    //检查更新
                    check.performClick();
                }
                while(downloadFileInfo == null){

                }
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
                    downloadProgress.setMax(downloadFileInfo.getLength());
                    downloadProgress.setProgress(downloadFileInfo.getFinished());

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
                    downloadProgress.setMax(downloadFileInfo.getLength());

                }
                cursor.close();
                Logger.d("要下的文件信息:" + downloadFileInfo.toString()+";开始下载文件");

                //创建下载进度监听
                new DownloadProcess().start();
                //调用下载程序
                dmu.downloadPontFile(downloadFileInfo);
                down.setVisibility(View.GONE);//将下载按钮隐藏
                stop.setVisibility(View.VISIBLE);//显示暂停按钮
                downloadProgress.setVisibility(View.VISIBLE);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                down.setVisibility(View.VISIBLE);
                downloadFileInfo.setStop(true);
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.execSQL("drop table IF exists  file_info");


                File file = new File(DATABASE_PATH, downloadFileInfo.getFileName());
                String md5sum = Codec2.getMd5ByFile(file);
                Logger.d("计算出文件的MD5值为:" + md5sum + "服务器计算出的MD5值为:" + downloadFileInfo.getMd5sum());
                if(md5sum.equals(downloadFileInfo.getMd5sum()))
                    Logger.d("MD5匹配！！！！");
                else
                    Logger.i("MD5不匹配！！！！！");
                if(file.exists())
                    file.delete();
                downloadFileInfo = null;
            }
        });


    }

    private class VersionHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == RESULT_LATES_VERSION_INFO_ERROR){
                hintError((String) msg.obj);
                return;
            }
            if(msg.what == DOWNLOADING_PROGRESS){
                downloadProgress.setProgress(downloadFileInfo.getFinished());
                return;
            }
            if(msg.what == DOWNLOADING_ERROE){
                hintError((String) msg.obj);
                return;
            }
            latestVersion = (String) msg.obj;
            versionInfo.append('\n'+"最新版本号:"+latestVersion);
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
                    Thread.sleep(1000);
                    if(downloadFileInfo.getFinished() >= downloadFileInfo.getLength())
                        break;
                    if(downloadFileInfo.isStop())
                        break;
                    Message msg = new Message();
                    msg.what = DOWNLOADING_PROGRESS;
                    versionHandle.sendMessage(msg);
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
                    versionHandle.sendMessage(msg);
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
    /**
     *禁止返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return false;
    }




    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("执行到--------->onResume");
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                hintError("未设置权限！");
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
//        Logger.d("执行到--------->onResume");
//        if (PermissionUtil.isLacksOfPermission(this,PermissionUtil.PERMISSION[0])) {
//            Logger.d("动态申请权限------------->");
//            ActivityCompat.requestPermissions(this, PermissionUtil.PERMISSION, 0x12);
//        } else {
//            //setDeviceId();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.d("执行到onRequestPermissionsResult，"+"requestCode值："+requestCode);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
                down.performClick();
            }else { //拒绝权限申请
                Logger.d("权限被拒绝！！");
                Toast.makeText(this,"权限被拒绝了", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void hintError(String msg){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }
    /**
     * 默认信任所有的证书
     * TODO 最好加上证书认证，主流App都有自己的证书
     *
     * @return
     */
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


    //    private void setDeviceId() {
//        SharedPrefUtil.putString(getApplicationContext(), Constants.KEY_DEVICE_ID, Config.getDeviceID());
//    }
}