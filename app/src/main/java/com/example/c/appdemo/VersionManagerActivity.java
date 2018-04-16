package com.example.c.appdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.c.been.ResultData;
import com.example.c.utils.Codec2;
import com.example.c.utils.DownloadManagerUtil;
import com.example.c.utils.PermissionUtil;
import com.example.c.utils.PropertiesUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.URL;
import java.security.Permission;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
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
    private Button check;
    private Button down;
    private Button up;
    private TextView versionInfo;

    private String registerDeviceUrl;
    private TelephonyManager telephonyManager;
    private PackageManager packageManager;
    private String deviceId = "252d1e9a82fdd0d64UC";
    private String model;//设备型号
    private String deviceSecret; //singn key
    private String bakUrl;
    private String deltaUrl = "http://iotdown.mayitek.com/1522029924/2331580/8a3fccbc-ae3c-445a-a3b7-e28524802293.exe";
    private String checkVersionUrl;
    private String latestVersion;

    private  final String mid = "e0aee11a";
    private  final String oem = "mi";
    private  final String models = "HM-Note4X";
    private  final String platform = "MSM8625";
    private  final String deviceType = "phone";
    private  final String sdkversion = "1.3.2_pre7";
    private  final String appversion = "1.3.2_pre7";
    private  final String version = "7.0";
    private  final String networkType = "WIFI";
    private  final String productId = "1522029924";
    private  final String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_manager);
        check = (Button) findViewById(R.id.check);
        down = (Button) findViewById(R.id.down);
        up = (Button) findViewById(R.id.up);
        versionInfo = (TextView) findViewById(R.id.versionInfo);
        //checkVersionUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("checkVersionUrl");
        registerDeviceUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("registerDeviceUrl") + productId;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final VersionHandle versionHandle = new VersionHandle();

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Logger.d("访问"+registerDeviceUrl+"注册设备!!!!!!");
                long timestamp = new Date().getTime()/1000;
                // @SuppressLint("MissingPermission")
                //String deviceId = telephonyManager.getDeviceId();
                Logger.d("设备id:"+deviceId);
                //"252d1e9a82fdd0d64UC";
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
//                    JsonElement je = new JsonParser().parse(responseBody);
//                    String data = mGson.toJson(je.getAsJsonObject().get("data"));
                    ResultData resultData = mGson.fromJson(responseBody,ResultData.class);
                    Logger.d("status:" + resultData.getStatus());
                    if(!"1000".equals(resultData.getStatus())){
                        hintError(resultData.getMsg());
                        return;
                    }

                    Map<String,Object> dataMap = resultData.getData();
                    deviceSecret =  mGson.toJson(dataMap.get("deviceSecret"));
                    deviceId = mGson.toJson(dataMap.get("deviceId"));
                    deviceSecret = deviceSecret.replace("\"","");
                    deviceId = deviceId.replace("\"","");
                    Logger.d("返回得到deviceSecret："+deviceSecret+'\n'+
                                "返回得到deviceId:"+deviceId);
                    //9dde8ff666a532f32252f8a18247755e
                    //chang---6b5d0b97b5e78dcf9b4d534090501c29
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
                            System.out.println();
                            Logger.d("访问" + checkVersionUrl + "检测版本号");
                            long timestamp = new Date().getTime()/1000;
//                            @SuppressLint("MissingPermission")
//                            String deviceId = telephonyManager.getDeviceId();
//                            Logger.d("设备id:"+deviceId);
                            String signInfo = deviceId + productId + timestamp;
                            String sign = Codec2.getHmacMd5Str(signInfo, deviceSecret);
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
                                return;
                            }
                            Map<String,Object> data = resultData.getData();
                            Map<String,Object> version = (Map<String, Object>) data.get("version");
                            Message msg = new Message();
                            msg.obj =  version.get("versionAlias");
                            versionHandle.sendMessage(msg);
                            deltaUrl = ((String) version.get("deltaUrl")).replace("\"","");
                            Logger.d("version:"+deltaUrl);

//                            JsonElement je = new JsonParser().parse(response.body().string());
//                            String version = je.getAsJsonObject().get("version").getAsString();
//                            Logger.d("检测到当前版本号:"+version);
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
                if(ActivityCompat.checkSelfPermission(VersionManagerActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(VersionManagerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    return;
                }
                Uri uri = Uri.parse(deltaUrl);
                String[] bakUrlSplit = deltaUrl.split("/");
                //DownloadManagerUtil.downloadAPK(deltaUrl,getApplicationContext(),bakUrlSplit[bakUrlSplit.length-1]);
                DownloadManagerUtil.downloadFile(deltaUrl,getApplicationContext(),bakUrlSplit[bakUrlSplit.length-1]);
            }
        });

    }
    private class VersionHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            latestVersion = (String) msg.obj;
            versionInfo.append('\n'+"最新版本号:"+latestVersion);
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