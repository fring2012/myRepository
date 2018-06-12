package com.abupdate.iot_sdk;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.info.CustomDeviceInfo;
import com.abupdate.iot_libs.policy.PolicyConfig;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.trace.Trace;
import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Field;


/**
 * Created by fighter_lee on 2017/7/3.
 */

public class App extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Class<?> sdkConfig = Class.forName("com.abupdate.iot_libs.constant.SDKConfig");
            Field isTest = sdkConfig.getDeclaredField("isTest");
            isTest.setAccessible(true);
            isTest.setBoolean(false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        context = getApplicationContext();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        Trace.setShowPosition(true);
        initFota();
//        initSota();
    }

//    private void initSota() {
//        try {
//            SotaControler.init(this)
//                    .setSotaCustomDeviceInfo(new SotaCustomDeviceInfo()
//                            .setVersion(Constant.version)
//                            .setOem(Constant.oem)
//                            .setModels(Constant.model)
//                            .setDeviceType(Constant.deviceType)
//                            .setPlatform(Constant.platform)
//                            .setProductId(com.abupdate.iot_sdk.Constant.productId)
//                            .setProduct_secret(com.abupdate.iot_sdk.Constant.productSecret)
//                    ).setMid(Build.SERIAL)
//                    .commit();
//        } catch (com.abupdate.sota.security.FotaException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 初始化SDK升级包存放路径
     * 默认在data/data/包名/files下
     */
    private void initFota() {
        try {
            /*重要！！！
            mid标识每一个设备，请自定义mid，方便定位到每一台设备，也可以使用设备默认的serial或者imei作为唯一标识码
            使用imei作为设备mid 需要在manifest.xml文件中要添加 <uses-permission android:name="android.permission.READ_PHONE_STATE" />
            String mid = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();*/
            //            String mid = SPFTool.getString(FOTA_MID, "");
            //            if (TextUtils.isEmpty(mid)) {
            //                mid = Build.SERIAL;
            //                SPFTool.putString(FOTA_MID, mid);
            //            }
            String mid = Build.SERIAL;

            OtaAgentPolicy.init(context)
                    .setMid("4325")
                    .setCustomDeviceInfo(
                            new CustomDeviceInfo()
                                    .setVersion(Constant.version)
                                    .setOem(Constant.oem)
                                    .setModels(Constant.model)
                                    .setDeviceType(Constant.deviceType)
                                    .setRequestPush(Constant.requestPush)
                                    .setPlatform(Constant.platform)
                                    .setProductId(com.abupdate.iot_sdk.Constant.productId)
                                    .setProduct_secret(com.abupdate.iot_sdk.Constant.productSecret)
                    )
                    .setUpdatePath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/update.zip")
                    .commit();

            //            OtaAgentPolicy.init(context)
            //                    .setMid(mid)
            //                    .commit();

        } catch (FotaException e) {
            e.printStackTrace();
        }

        PolicyConfig.getInstance()
                .request_check_cycle(true)
                .request_install_force(true)
                .request_storage_path(true)
                .request_battery(true)
                .request_wifi(true)
                .request_download_force(true)
                .request_storage_size(true);

    }
}
