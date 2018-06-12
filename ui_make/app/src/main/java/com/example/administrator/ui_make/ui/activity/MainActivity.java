package com.example.administrator.ui_make.ui.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.administrator.ui_make.R;
import com.example.administrator.ui_make.ui.branchview.IUpgradeView;
import com.example.administrator.ui_make.ui.branchview.viewgroup.LeafViewGroup;
import com.example.administrator.ui_make.ui.branchview.viewgroup.LeafViewGroup.LeafOnClickListener;

import java.io.File;

import butterknife.BindView;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.branch_ecu)
    IUpgradeView branchViewGroup;

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri.fromFile(new File(""));




        branchViewGroup.setLeafListener(new LeafOnClickListener(){

;            @Override
            public void checkVersion() {
                Log.d(TAG,"checkVersion()");
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.administrator.apk_up_receiver",
                        "com.example.administrator.apk_up_receiver.receiver.ApkUpInfoReceiver"));
                intent.setAction("receiver.ApkUpInfoReceiver");
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(intent);
                Log.d(TAG,"checkVersion(111)");
            }

            @Override
            public void downloadVersion() {
                branchViewGroup.setState(LeafViewGroup.UPGRADE_VERSION);
                branchViewGroup.setText("下载中");
                branchViewGroup.setProgress(60);
            }

            @Override
            public void rebootUpgrade() {
                branchViewGroup.setState(LeafViewGroup.CHECK_VERSION);
                branchViewGroup.setFinished();
            }
        });
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }


}
