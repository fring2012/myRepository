package com.example.c.service;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.ArrayMap;
import android.util.Log;

import com.example.c.ui.activity.activity.MainActivity;
import com.example.c.ui.activity.activity.RegisterActivity;
import com.orhanobut.logger.Logger;


public class LoginService extends Service{
    private ArrayMap<String,String> accountArray ;
    private LocalBroadcastManager localBroadcastManager ;
    private BroadcastReceiver broadcastReceiver;

    public static final int ACCOUNT_NO_EXIST = 0; //账号不存在
    public static final int PASSWORD_ERROR = 2;   //密码错误
    public static final int LOGIN_SUCCESS =1;      //登录成功

    public static final int ACCOUNT_ALREADY_EXIST = 0;
    public static final String SERVICE_BROADCAST_RECEIVER_ACTION_NAME = "LoginService";




    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("创建服务！");
        accountArray = new ArrayMap<String,String>();
        accountArray.put("zhangsan","123");

        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new ServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SERVICE_BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }


    /**
     * 登录服务
     */
    private void loginService(String account, String password){
        int resultState = LOGIN_SUCCESS;
        String psw = accountArray.get(account);
        Logger.d("登录服务！！");
        if(psw == null) {
            resultState = ACCOUNT_NO_EXIST;
        }else{
            if(!psw.equals(password))
                resultState = PASSWORD_ERROR;
        }
        Intent intent = new Intent();
        intent.putExtra("resultState",resultState);
        intent.setAction(MainActivity.BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * 注册服务
     */
    private void registerService(String account , String password){

        int resultState = 1;
        if(accountArray.get(account) != null) {
            resultState = ACCOUNT_ALREADY_EXIST;
        }else {
            accountArray.put(account,password);
        }
        Intent intent = new Intent();
        intent.putExtra("resultState",resultState);
        intent.setAction(RegisterActivity.REGISTER_BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.sendBroadcast(intent);

    }

    @Override
    public void onDestroy() {
        //注销广播接收器
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ServiceBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d("接收到请求，正在处理。。。。。");
            String account = intent.getCharSequenceExtra("account").toString();
            String invoke = intent.getStringExtra("invoke");
            String password = intent.getCharSequenceExtra("password").toString();
            Logger.d("账号:" + account + "密码:" + password);
            if ("login".equals(invoke)){
                loginService(account,password);
            }else if("register".equals(invoke)){
                registerService(account,password);
            }

        }
    }
}
