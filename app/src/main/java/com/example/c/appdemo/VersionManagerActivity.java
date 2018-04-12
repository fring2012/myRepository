package com.example.c.appdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.example.c.utils.Codec2;
import com.example.c.utils.PropertiesUtils;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by C on 2018/4/3.
 */

public class VersionManagerActivity extends Activity {
    private Button check;
    private Button down;
    private Button up;
    private String checkVersionUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_manager);
        check = (Button)findViewById(R.id.check);
        down = (Button)findViewById(R.id.down);
        up = (Button)findViewById(R.id.up);
        checkVersionUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("checkVersionUrl");
        Logger.d("检测版本号url:"+checkVersionUrl);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println();
                                Logger.d("访问"+checkVersionUrl+"检测版本号");
                                String mid = "e0aee11a";
                                String version = "v1";
                                long  timestamp = new Date().getTime();
                                String deviceId = "252d1e9a82fdd0d64UC";
                                String productId = "1522029924";
                                String signInfo = deviceId+productId+timestamp;
                                String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";
                                String sign = Codec2.getHmacMd5Str(signInfo,productSecret);
                                FormBody.Builder formBody = new FormBody.Builder();
                                formBody.add("mid",mid);
                                formBody.add("version",version);
                                formBody.add("timestamp",new Long(timestamp).toString());
                                formBody.add("sign",sign);
                                System.out.println("?mid="+mid+"&version="+version+"&timestamp="+timestamp+"&sign="+sign);
                                OkHttpClient.Builder mBuilder = new OkHttpClient.Builder();
                                mBuilder.sslSocketFactory(createSSLSocketFactory());
                                mBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
                                OkHttpClient client =  mBuilder.build();
                                Request request =  new Request.Builder().url(checkVersionUrl)
                                              .post(formBody.build())
                                              .build();

                                Response response = client.newCall(request).execute();
                                Logger.d(response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return false;
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
}
