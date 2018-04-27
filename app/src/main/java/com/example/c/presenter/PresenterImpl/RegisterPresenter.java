package com.example.c.presenter.PresenterImpl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.example.c.presenter.Presenter.IRegisterPresenter;
import com.example.c.presenter.common.BasePresenter;
import com.example.c.service.LoginService;
import com.example.c.ui.activity.activity.MainActivity;
import com.example.c.ui.activity.activity.RegisterActivity;
import com.orhanobut.logger.Logger;

public class RegisterPresenter extends BasePresenter<RegisterActivity> implements IRegisterPresenter{
    private RegisterBroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public static final String REGISTER_BROADCAST_RECEIVER_ACTION_NAME = "RegisterActivity";

    @Override
    public void registerBroadcastReceiver(){
        localBroadcastManager = LocalBroadcastManager.getInstance(getView());
        broadcastReceiver = new RegisterBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REGISTER_BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }
    @Override
    public void unRegisterBroadcastReceiver(){
        getView().shuntProgressDialog();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void registerAccount() {
        CharSequence accountText = getView().getRegAccountText();
        if(accountText.length() == 0){
            getView().shuntProgressDialog();
            Logger.d("注册账号为空！");
            getView().hintError("请输入账号！");
            return;
        }
        CharSequence passwordText = getView().getAgainPasswordText();
        if(passwordText.length() == 0){
            getView().shuntProgressDialog();
            Logger.d("密码为空！");
            getView().hintError("请输入密码！");
            return;
        }
        CharSequence againpasswordText = getView().getAgainPasswordText();
        if(!againpasswordText.toString().equals(passwordText.toString())){
            getView().shuntProgressDialog();
            Logger.d("两次密码不一致");
            getView().hintError("两次密码不一致！");
            return;
        }
        Intent intent = new Intent(getView(),LoginService.class);
        intent.putExtra("invoke","login");
        intent.putExtra("account",accountText);
        intent.putExtra("password",passwordText);
        getView().startService(intent);
    }


    private class RegisterBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultState = intent.getIntExtra("resultState",4);
            Logger.d("接收到服务端返回："+resultState);
            if(resultState == 0) {
                getView().shuntProgressDialog();
                getView().hintError("账号已经存在！");
                return;
            }
            Intent intent2 = new Intent(getView(),MainActivity.class);
            getView().startActivity(intent2);
        }
    }
}
