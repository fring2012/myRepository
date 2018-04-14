package com.example.c.appdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.Toast;

import com.example.c.utils.Codec2;
import com.example.c.utils.PermissionUtil;
import com.example.c.utils.PropertiesUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.orhanobut.logger.Logger;

import java.io.IOException;
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
    private String checkVersionUrl;
    private String registerDeviceUrl;
    private TelephonyManager telephonyManager;
    private PackageManager packageManager;
    private String deviceId ="252d1e9a82fdd0d64UC";
    private String model;//设备型号
    private  String deviceSecret;
    private  final String mid = "9071cf7a-1120-4c3d-a886-5ce84b152062";
    private  final String oem = "mi";
    private  final String models = "HM-Note4X";
    private  final String platform = "MSM8625";
    private  final String deviceType = "phone";
    private  final String sdkversion = "1.3.2_pre7";
    private  final String appversion = "1.3.2_pre7";
    private  final String version = "7.0";
    private  final String networkType = "WIFI";
    private  final String productId = "1522029924";
            //"1523502439";
    private  final String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";
            //"23dbc31a4ec941f0b546d16deeda1c61";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_manager);
        check = (Button) findViewById(R.id.check);
        down = (Button) findViewById(R.id.down);
        up = (Button) findViewById(R.id.up);
        checkVersionUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("checkVersionUrl");
        registerDeviceUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("registerDeviceUrl");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        model = Build.MODEL;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                Logger.d("访问"+registerDeviceUrl+"注册设备!!!!!!");
                long timestamp = new Date().getTime()/1000;
                @SuppressLint("MissingPermission")
                String deviceId = telephonyManager.getDeviceId();
                Logger.d("设备id:"+deviceId);
                //"252d1e9a82fdd0d64UC";
                String signInfo = "252dcef6269e5b2FLyZ" + productId + timestamp;
                String sign = Codec2.getHmacMd5Str(signInfo, productSecret);
                FormBody.Builder formBody = new FormBody.Builder();
                Map<String, Object> params = new ArrayMap<>();
                params.put("mid", mid);
                params.put("oem",oem);
                params.put("models",models);
                params.put("platform",platform);
                params.put("deviceType",deviceType);
                params.put("timestamp", 1523606446l);//1523606446l
                params.put("sign", "6144af67ee2af9621127bc25ecf85660");
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
                    JsonElement je = new JsonParser().parse(responseBody);
                    String data = mGson.toJson(je.getAsJsonObject().get("data"));
                    je =  new JsonParser().parse(data);
                    deviceSecret =  mGson.toJson(je.getAsJsonObject().get("deviceSecret"));
                    Logger.d("返回得到deviceSecret："+deviceSecret);
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
                            System.out.println();
                            Logger.d("访问" + checkVersionUrl + "检测版本号");
                            long timestamp = new Date().getTime();
                            @SuppressLint("MissingPermission")
                            String deviceId = telephonyManager.getDeviceId();
                            Logger.d("设备id:"+deviceId);
                            String signInfo = "252d1e9a82fdd0d64UC" + productId + timestamp;
                            String sign = Codec2.getHmacMd5Str(signInfo, deviceSecret);
                            FormBody.Builder formBody = new FormBody.Builder();
                            Map<String, Object> params = new ArrayMap<>();
                            params.put("mid", "e0aee11a");
                            params.put("version", "6.0.1");
                            params.put("timestamp",  1523616493);
                            params.put("sign", "96b226a54d5ce3d2adcd037fe50cc143");
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

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

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
        if (PermissionUtil.isLacksOfPermission(this.getApplicationContext(),PermissionUtil.PERMISSION[0])) {
            Logger.d("动态申请权限------------->");
            ActivityCompat.requestPermissions(this, PermissionUtil.PERMISSION, 0x12);
        } else {
            //setDeviceId();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.d("执行到onRequestPermissionsResult，"+"requestCode值："+requestCode);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //同意权限申请
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Logger.d("获取deviceid前--->"+ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) +":"+ PackageManager.PERMISSION_GRANTED);
                    return;
                }

            }else { //拒绝权限申请
                Toast.makeText(this,"权限被拒绝了", Toast.LENGTH_SHORT).show();
            }
        }
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
