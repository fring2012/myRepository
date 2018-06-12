package com.example.administrator.apk_up_receiver.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.abupdate.trace.Trace;
import com.example.administrator.apk_up_receiver.R;
import com.example.administrator.apk_up_receiver.receiver.ApkUpInfoReceiver;
import com.example.administrator.apk_up_receiver.utils.FileUtil;
import com.example.administrator.apk_up_receiver.utils.NetUtil;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button download = (Button) findViewById(R.id.download);
        Trace.setLevel(1);
        Trace.write_file(false);
        Trace.setShowPosition(true);
        download.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Trace.d(TAG,"sendBroadcast(intent);");
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(MainActivity.this,"com.example.administrator.apk_up_receiver.receiver.ApkUpInfoReceiver"));
                intent.setAction("receiver.ApkUpInfoReceiver");
                sendBroadcast(intent);
            }
        });

    }
}
