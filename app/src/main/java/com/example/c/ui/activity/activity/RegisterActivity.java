package com.example.c.ui.activity.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.c.appdemo.R;
import com.example.c.presenter.Presenter.IRegisterPresenter;
import com.example.c.presenter.PresenterImpl.RegisterPresenter;
import com.example.c.service.LoginService;
import com.example.c.ui.activity.common.BaseView;
import com.orhanobut.logger.Logger;


public class RegisterActivity extends BaseView{
    private EditText regAccount;
    private EditText regPassword;
    private EditText againPassword;
    private Button register;
    private IRegisterPresenter registerPresenter;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        regAccount = (EditText)findViewById(R.id.reg_account);
        regPassword = (EditText)findViewById(R.id.reg_password);
        againPassword =(EditText)findViewById(R.id.again_password);
        register = (Button) findViewById(R.id.register);
        registerPresenter = new RegisterPresenter();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("点击注册！");
                showProgressDialog("注册中。。。");
                registerPresenter.registerAccount();

            }
        });
        Intent intent = new Intent(RegisterActivity.this, LoginService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播接收器
        registerPresenter.registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        //注销广播接收器
        registerPresenter.unRegisterBroadcastReceiver();
        super.onPause();
    }

    class RegisterHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Logger.d("接收到服务端返回："+msg.what);
            if(msg.what == 0) {
                shuntProgressDialog();
                hintError("账号已经存在！");
                return;
            }
            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public void hintError(String msg){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }

    public CharSequence getRegAccountText() {
        return regAccount.getText();
    }


    public CharSequence getRegPasswordText() {
        return regPassword.getText();
    }

    public CharSequence getAgainPasswordText() {
        return againPassword.getText();
    }


}
