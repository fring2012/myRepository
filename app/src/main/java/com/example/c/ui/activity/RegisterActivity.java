package com.example.c.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.c.appdemo.R;
import com.example.c.service.LoginService;
import com.orhanobut.logger.Logger;


public class RegisterActivity extends Activity{
    private EditText regAccount;
    private EditText regPassword;
    private EditText againPassword;
    private Button register;
    private Messenger serviceMessenger;
    private boolean accessing = false;
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
                if(accessing)
                    return;
                accessing = true;
                CharSequence accountText = regAccount.getText();
                if(accountText.length() == 0){
                    accessing = false;
                    Logger.d("注册账号为空！");
                    hintError("请输入账号！");
                    return;
                }
                CharSequence passwordText = regPassword.getText();
                System.out.println(passwordText.length() == 0);
                if(passwordText.length() == 0){
                    accessing = false;
                    Logger.d("密码为空！");
                    hintError("请输入密码！");
                    return;
                }
                CharSequence againpasswordText = againPassword.getText();
                if(!againpasswordText.toString().equals(passwordText.toString())){
                    accessing = false;
                    Logger.d("两次密码不一致");
                    hintError("两次密码不一致！");
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putCharSequence("account",accountText);
                bundle.putCharSequence("password",passwordText);
                Message msg = new Message();
                Messenger registerMessenger = new Messenger(new RegisterHandler());
                msg.setData(bundle);
                msg.what = 2;
                msg.replyTo = registerMessenger;
                try {
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        Intent intent = new Intent(RegisterActivity.this, LoginService.class);
        bindService(intent,conn, Service.BIND_AUTO_CREATE);
    }

    class RegisterHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Logger.d("接收到服务端返回："+msg.what);
            if(msg.what == 0) {
                hintError("账号已经存在！");
                accessing = false;
                return;
            }
            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
}
