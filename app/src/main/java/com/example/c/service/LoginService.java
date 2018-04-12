package com.example.c.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;
import android.util.Log;

import com.orhanobut.logger.Logger;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class LoginService extends Service{
    private ArrayMap<String,String> accountArray = new ArrayMap<String,String>();
    private Messenger messenger = new Messenger(new LonginHandler());
    private final  int ACCOUNT_NO_EXIST = 0;//账号不存在
    private final  int PASSWORD_ERROR = 2;//密码错误
    private final int LOGIN_SUCCESS =1;//登录成功
    private final int IS_LOGIN = 1;
    private final int IS_REGISTER =2;
    private final int ACCOUNT_ALREADY_EXIST = 0;


    class LonginHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Logger.d("接收到客户端请求参数:"+msg.what);
            switch (msg.what){
                case IS_LOGIN:
                    Logger.d("执行登录验证！");
                    loginService(msg);
                    break;
                case IS_REGISTER:
                    Logger.d("执行注册服务！");
                    registerService(msg);
                    break;
            }
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("创建服务！");
        accountArray.put("zhangsan","123");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    /**
     * 登录服务
     */
    private void loginService(Message msg){
        int resultState = LOGIN_SUCCESS;
        Message resultMsg = new Message();
        Messenger client = msg.replyTo;
        String account = msg.getData().getCharSequence("account").toString();
        String password = accountArray.get(account);
        Logger.d("登录服务！！"+accountArray.get("123214a"));
        if(password == null) {
            resultState = ACCOUNT_NO_EXIST;
        }else{
            if(!password.equals(msg.getData().getCharSequence("password").toString()))
                resultState = PASSWORD_ERROR;
        }
        resultMsg.what = resultState;
        try {
            client.send(resultMsg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * 注册服务
     */
    private void registerService(Message msg){
        Message message = new Message();
        message.what = 1;
        String account = msg.getData().getCharSequence("account").toString();
        String passowrd = msg.getData().getCharSequence("password").toString();
        if(accountArray.get(account) != null) {
            message.what = ACCOUNT_ALREADY_EXIST;
        }else {
            accountArray.put(account,passowrd);
        }
        Messenger registerMessenger = msg.replyTo;
        try {
            registerMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("LoginService关闭————————————");
    }
}
