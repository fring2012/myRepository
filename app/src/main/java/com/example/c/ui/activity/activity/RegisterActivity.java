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
import com.example.c.service.LoginService;
import com.example.c.ui.activity.common.BaseView;
import com.orhanobut.logger.Logger;


public class RegisterActivity extends BaseView{
    private EditText regAccount;
    private EditText regPassword;
    private EditText againPassword;
    private Button register;
    private Messenger serviceMessenger;
    private boolean accessing = false;
    private RegisterBroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public static final String REGISTER_BROADCAST_RECEIVER_ACTION_NAME = "RegisterActivity";

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            serviceMessenger = new Messenger(iBinder);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceMessenger = null;
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        regAccount = (EditText)findViewById(R.id.reg_account);
        regPassword = (EditText)findViewById(R.id.reg_password);
        againPassword =(EditText)findViewById(R.id.again_password);
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.d("点击注册！");
                showProgressDialog("注册中。。。");
                sendRegisterInfo();

            }
        });
        Intent intent = new Intent(RegisterActivity.this, LoginService.class);
        bindService(intent,conn, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new RegisterBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REGISTER_BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);

    }

    @Override
    protected void onPause() {
        shuntProgressDialog();
        //注销广播接收器
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
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
    private class RegisterBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultState = intent.getIntExtra("resultState",4);
            Logger.d("接收到服务端返回："+resultState);
            if(resultState == 0) {
                shuntProgressDialog();
                hintError("账号已经存在！");
                return;
            }
            Intent intent2 = new Intent(RegisterActivity.this,MainActivity.class);
            startActivity(intent2);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("register关闭，关闭服务连接");
        unbindService(conn);
    }
    public void hintError(String msg){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }

    /**
     * 发送注册信息
     */
    private void sendRegisterInfo(){
        CharSequence accountText = regAccount.getText();
        if(accountText.length() == 0){
            shuntProgressDialog();
            Logger.d("注册账号为空！");
            hintError("请输入账号！");
            return;
        }
        CharSequence passwordText = regPassword.getText();
        if(passwordText.length() == 0){
            shuntProgressDialog();
            Logger.d("密码为空！");
            hintError("请输入密码！");
            return;
        }
        CharSequence againpasswordText = againPassword.getText();
        if(!againpasswordText.toString().equals(passwordText.toString())){
            shuntProgressDialog();
            Logger.d("两次密码不一致");
            hintError("两次密码不一致！");
            return;
        }
        //发送注册信息
        Intent intent = new Intent();
        intent.putExtra("account",accountText);
        intent.putExtra("password",passwordText);
        intent.putExtra("invoke","register");
        intent.setAction(LoginService.SERVICE_BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.sendBroadcast(intent);
    }

}
