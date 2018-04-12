package com.example.c.appdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.example.c.utils.PropertiesUtils;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by C on 2018/4/3.
 */

public class VersionManagerActivity extends Activity {
    private Button check;
    private Button down;
    private Button up;
    private String checkVersionUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_manager);
        check = (Button)findViewById(R.id.check);
        down = (Button)findViewById(R.id.down);
        up = (Button)findViewById(R.id.up);
        checkVersionUrl = PropertiesUtils.getPropertes(getApplicationContext()).getProperty("checkVersionUrl");
        Logger.d("检测版本号url:"+checkVersionUrl);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Logger.d("访问"+checkVersionUrl+"检测版本号");
                                OkHttpClient client = new OkHttpClient();
                                Request reuqest =  new Request.Builder().url(checkVersionUrl).build();
                                Response response = client.newCall(reuqest).execute();
                                Logger.d(response.body());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
            return true;
        return false;
    }
}
