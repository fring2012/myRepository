package com.example.c.appdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.c.service.LoginService;
import com.example.c.utils.PropertiesUtils;
import com.orhanobut.logger.Logger;

import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    private Button login;
    private Button reg;
    private TextView account;
    private TextView password;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Messenger serviceMessenger;
    private ProgressBar pb;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //绑定服务，建立服务端的Service
            serviceMessenger = new Messenger(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceMessenger = null;
        }
    };
    private String serverUrl;
    private String serverPort;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Properties properties = PropertiesUtils.getPropertes(getApplicationContext());
        serverUrl = properties.getProperty("serverUrl");
        serverPort = properties.getProperty("serverPort");
        login = (Button) findViewById(R.id.login);
        reg = findViewById(R.id.reg);
        account = (TextView) findViewById(R.id.account);
        password = (TextView)findViewById(R.id.password);
        sp = getPreferences(Context.MODE_APPEND);
        editor = sp.edit();
        String lastAccount = sp.getString("lastAccount",null);
        if(account != null)
            account.setText(lastAccount);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence accountText = account.getText();  //获取账号
                CharSequence passwordText = password.getText();//获取用户输入的密码
                if(accountText.length() == 0) {
                    hintError("请输入账号！");
                    Logger.d("账号为空！");
                    return;
                }
                if(passwordText.length() == 0){
                    Logger.d("密码为空！");
                    hintError("请输入密码！");
                    return;
                }
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putCharSequence("account",accountText);
                bundle.putCharSequence("password",passwordText);
                msg.setData(bundle);
                msg.what = 1;
                Messenger client = new Messenger(new ClientHandler());
                msg.replyTo = client;
                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        Intent intent = new Intent(MainActivity.this,LoginService.class);
        bindService(intent,conn, Service.BIND_AUTO_CREATE);

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }


    private class ClientHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int resultState = msg.what;
            Logger.d("接受到服务端返回："+msg.what);

            switch (resultState){
                case 0:
                    hintError("账号不存在！");
                    break;
                case 1:
                    Intent intent = new Intent(MainActivity.this,VersionManagerActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    hintError("密码错误！");
                    break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("login界面关闭！关闭服务连接");
        unbindService(conn);
    }
    public void hintError(String msg){
        new AlertDialog.Builder(this)
                .setTitle("错误")
                .setMessage("账号不存在")
                .setPositiveButton("确定",null)
                .show();
    }
}
