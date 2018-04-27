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

import com.example.c.appdemo.R;
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
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private LoginBroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    public static final String BROADCAST_RECEIVER_ACTION_NAME = "MainActivity";


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
        sp = getPreferences(Context.MODE_APPEND);
        editor = sp.edit();
        String lastAccount = sp.getString("lastAccount",null);
        if(account != null)
            account.setText(lastAccount);

        //开启服务
        Intent startIntent = new Intent(this, LoginService.class);
        startService(startIntent);



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressDialog("登录中。。。");
                CharSequence accountText = account.getText();  //获取账号
                CharSequence passwordText = password.getText();//获取用户输入的密码
                if(accountText.length() == 0) {
                    shuntProgressDialog();
                    hintError("账号为空！");
                    return;
                }
                if(passwordText.length() == 0){
                    shuntProgressDialog();
                    hintError("请输入密码！");
                    return;
                }
                Logger.d("发送登录信息给服务！！！！！！账号:" + accountText + "密码:" + passwordText);
                //通过广播发送信息给服务
                Intent intent = new Intent();
                intent.putExtra("account",accountText);
                intent.putExtra("password",passwordText);
                intent.putExtra("invoke","login");
                intent.setAction(LoginService.SERVICE_BROADCAST_RECEIVER_ACTION_NAME);
                localBroadcastManager.sendBroadcast(intent);
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

        //注册广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new LoginBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_RECEIVER_ACTION_NAME);
        localBroadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        shuntProgressDialog();
        super.onPause();
    }



    public String getAccount() {
        return (String) account.getText();
    }


    public String getPassword() {
        return (String) password.getText();
    }

    public class LoginBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int resultState = intent.getIntExtra("resultState",3);
            //msg.what;
            Logger.d("接受到服务端返回：" + resultState);

            switch (resultState){
                case LoginService.ACCOUNT_NO_EXIST:
                    shuntProgressDialog();
                    hintError("账号不存在！");
                    break;
                case LoginService.LOGIN_SUCCESS:
                    Logger.d("登录成功！");
                    editor.putString("lastAccount",account.getText().toString());//保存最近登录的账号
                    editor.commit();
                    Intent intent2 = new Intent(MainActivity.this,VersionManagerActivity.class);
                    startActivity(intent2);
                    break;
                case LoginService.PASSWORD_ERROR:
                    shuntProgressDialog();
                    hintError("密码错误！");
                    break;
            }
        }
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
