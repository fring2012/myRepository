package com.example.administrator.apk_up_receiver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;


import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.trace.Trace;
import com.example.administrator.apk_up_receiver.app.App;
import com.example.administrator.apk_up_receiver.engine.ApkUpEngine;


public class ApkUpInfoReceiver extends BroadcastReceiver{
    private static final String TAG = "ApkUpInfoReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Trace.d(TAG,"onReceive(Context context, Intent intent)");
        DeviceInfo.getInstance().version = App.getTargetPackageVersion();
        ApkUpEngine.getInstance().update(context);
    }

}
