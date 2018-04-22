package com.example.c.ui.activity.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.c.appdemo.R;
import com.example.c.presenter.Presenter.IVersionPresenter;
import com.example.c.presenter.PresenterImpl.VersionPresenter;
import com.example.c.ui.activity.common.BaseView;
import com.orhanobut.logger.Logger;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by C on 2018/4/3.
 */

public class VersionManagerActivity extends BaseView {
    private ProgressBar downloadProgress;
    private Button check;  //检查更新按钮
    private Button down;  //下载按钮
    private Button up;
    private Button stop; //暂停下载按钮
    private TextView versionInfo; //版本信息
    private TextView downloadInfo; //下载信息
    private IVersionPresenter versionPresenter;
    private LinearLayout downloadList;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_manager);

        downloadProgress = (ProgressBar) findViewById(R.id.downloadProgress);
        downloadInfo = (TextView) findViewById(R.id.downloadInfo);
        downloadList = findViewById(R.id.downloadList);

        stop = (Button) findViewById(R.id.stop);
        check = (Button) findViewById(R.id.check);
        down = (Button) findViewById(R.id.down);
        up = (Button) findViewById(R.id.up);
        versionInfo = (TextView) findViewById(R.id.versionInfo);


        //telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        versionPresenter = new VersionPresenter();
        versionPresenter.setActivity(this);
        //设备注册
        versionPresenter.deviceRegister();


        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //检查是否有网络权限
                if(ActivityCompat.checkSelfPermission(VersionManagerActivity.this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                    //申请网络权限
                    ActivityCompat.requestPermissions(VersionManagerActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
                    return;
                }
                showProgressDialog("正在检测最新版本！");
                versionPresenter.checkLatestVesion();
                shuntProgressDialog();
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
                //下载文件
                versionPresenter.downloadLaterVersionFile();

                down.setVisibility(View.GONE);//将下载按钮隐藏
                stop.setVisibility(View.VISIBLE);//显示暂停按钮
                downloadList.setVisibility(View.VISIBLE);

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                down.setVisibility(View.VISIBLE);
                versionPresenter.stopDownloading();
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                versionPresenter.upVersion();
            }
        });


    }

    /**
     * 初始化进度条进度和最大值
     * @param max
     * @param progress
     */
    public void initDownloadProgressMax(int max,int progress){
        downloadProgress.setMax(max);
        downloadProgress.setProgress(progress);
    }

    /**
     * 改变进度条进度
     * @param finished
     * @param length
     */
    public void setDownloadProgress(int finished,int length){
        downloadProgress.setProgress(finished);
        downloadInfo.setText("下载进度:" + finished + '/' + length);
        if(finished == length) {
            downloadInfo.setText("下载完成");
            stop.setVisibility(View.GONE);
            down.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 改变版本信息
     * @param latestVersion
     */
    public void setLatestVersion(String latestVersion){
        versionInfo.setText("最新版本号:"+latestVersion);
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