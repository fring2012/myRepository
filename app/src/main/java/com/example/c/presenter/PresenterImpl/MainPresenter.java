package com.example.c.presenter.PresenterImpl;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.example.c.presenter.common.BasePresenter;
import com.example.c.presenter.Presenter.IMainPresenter;
import com.example.c.service.LoginService;
import com.example.c.ui.activity.activity.MainActivity;
import com.example.c.ui.activity.activity.VersionManagerActivity;
import com.orhanobut.logger.Logger;

public class MainPresenter extends BasePresenter<MainActivity> implements IMainPresenter {
    private LoginBroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public static final String BROADCAST_RECEIVER_ACTION_NAME = "MainActivity";
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;


    public MainPresenter(){


    }

    public void login(){
        getView().showProgressDialog("登录中。。。");
        CharSequence accountText = getView().getAccount();  //获取账号
        CharSequence passwordText = getView().getPassword();//获取用户输入的密码
        if(accountText.length() == 0) {
            getView().shuntProgressDialog();
            getView().hintError("账号为空！");
            return;
        }
        if(passwordText.length() == 0){
            getView().shuntProgressDialog();
            getView().hintError("请输入密码！");
            return;
        }
        Logger.d("发送登录信息给服务！！！！！！账号:" + accountText + "密码:" + passwordText);
        Intent intent = new Intent(getView(),LoginService.class);
        intent.putExtra("invoke","login");
        intent.putExtra("account",accountText);
        intent.putExtra("password",passwordText);
        getView().startService(intent);
    }

    /**
     *
     */
    public void initLastAccount(){
        String lastAccount = getSharedPreferences().getString("lastAccount",null);
        if(lastAccount != null)
             getView().setAccountText(lastAccount);
    }

    public void  registerBroadcastReceiver(){
        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(getView());
        broadcastReceiver = new LoginBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }

    public void unRegisterBroadcastReceiver(){
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }




    public class LoginBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultState = intent.getIntExtra("resultState",3);
            //msg.what;
            Logger.d("接受到服务端返回：" + resultState);

            switch (resultState){
                case LoginService.ACCOUNT_NO_EXIST:
                    getView().shuntProgressDialog();
                    getView().hintError("账号不存在！");
                    break;
                case LoginService.LOGIN_SUCCESS:
                    Logger.d("登录成功！" + getView());
                    getEditor().putString("lastAccount",getView().getAccount());//保存最近登录的账号
                    getEditor().commit();
                    Intent intent2 = new Intent(getView(),VersionManagerActivity.class);
                    getView().startActivity(intent2);
                    break;
                case LoginService.PASSWORD_ERROR:
                    getView().shuntProgressDialog();
                    getView().hintError("密码错误！");
                    break;
            }
        }
    }

    @SuppressLint("WrongConstant")
    private SharedPreferences getSharedPreferences(){
        if(sp == null)
            sp = getView().getPreferences(Context.MODE_APPEND);
        return sp;
    }

    private SharedPreferences.Editor getEditor(){
        if(editor == null)
            editor = getSharedPreferences().edit();
        return editor;
    }
}
