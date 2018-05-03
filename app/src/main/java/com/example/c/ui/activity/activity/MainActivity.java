package com.example.c.ui.activity.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abupdate.iot_libs.info.DeviceInfo;
import com.example.c.appdemo.R;
import com.example.c.presenter.Presenter.IMainPresenter;
import com.example.c.presenter.PresenterImpl.MainPresenter;
import com.example.c.service.LoginService;
import com.example.c.ui.activity.common.BaseView;
import com.example.c.utils.PropertiesUtils;
import com.orhanobut.logger.Logger;

import java.util.Properties;

public class MainActivity extends BaseView {
    private Button login;
    private Button reg;
    private TextView account;
    private TextView password;
    private IMainPresenter mainPresenter;






    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Properties properties = PropertiesUtils.getPropertes(getApplicationContext());
        login = (Button) findViewById(R.id.login);
        reg = findViewById(R.id.reg);
        account = (TextView) findViewById(R.id.account);
        password = (TextView)findViewById(R.id.password);
        mainPresenter = new MainPresenter();
        mainPresenter.setView(this);
        mainPresenter.initLastAccount();
        Logger.d("DeviceInfo:" + DeviceInfo.getInstance().toString());
//



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainPresenter.login();
            }
        });

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog("正在打开注册界面。。。");
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPresenter.registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        mainPresenter.unRegisterBroadcastReceiver();
        shuntProgressDialog();
        super.onPause();
    }



    public String getAccount() {
        return  account.getText().toString();
    }


    public String getPassword() {
        return  password.getText().toString();
    }


    public void setAccountText(String text){
        if(account != null)
            account.setText(text);

    }
    @Override
    protected void onDestroy() {
        Logger.d("login界面关闭！关闭服务连接");
        super.onDestroy();
    }
    public void hintError(String msg){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }



}
