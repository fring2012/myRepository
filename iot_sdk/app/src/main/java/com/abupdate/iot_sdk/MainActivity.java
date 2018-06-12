package com.abupdate.iot_sdk;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.abupdate.http_libs.HttpIotUtils;
import com.abupdate.iot_libs.IndirectOtaAgentPolicy;
import com.abupdate.iot_libs.MqttAgentPolicy;
import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.BroadcastConsts;
import com.abupdate.iot_libs.constant.Error;
import com.abupdate.iot_libs.engine.LogManager;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.ICheckVersionCallback;
import com.abupdate.iot_libs.inter.IDownSimpleListener;
import com.abupdate.iot_libs.inter.IDownloadListener;
import com.abupdate.iot_libs.inter.IRebootUpgradeCallBack;
import com.abupdate.iot_libs.inter.IStatusListener;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.mqtt_libs.connect.MqttManager;
import com.abupdate.mqtt_libs.mqtt_service.MqttTraceHandler;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

//import HttpTools;

/**
 * Created by fighter_lee on 2017/7/3.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private com.abupdate.iot_sdk.App app;
    private IStatusListener iStatusListener;
    Context activity = this;
    public Button check;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = (com.abupdate.iot_sdk.App) getApplication();
        check = (Button) findViewById(R.id.check);
//        UpgradeParamInfo info = new UpgradeParamInfo(DeviceInfo.getInstance().mid,
//                "20",
//                String.valueOf(true ? 1 : 99)
//        );
//        ReportManager.getInstance(this).saveReportData(info);
        iStatusListener = new IStatusListener() {
            @Override
            public void onConnected() {
                Trace.d(TAG, "------------onConnected()------------ ");
            }

            @Override
            public void onDisconnected() {
                Trace.d(TAG, "------------onDisconnected()------------ ");
            }

            @Override
            public void onAbnormalDisconnected(int error) {
                Trace.d(TAG, "------------onAbnormalDisconnected()------------ " + error);
            }

            @Override
            public void onError(int error) {
                Trace.d(TAG, "------------onError()------------ " + error);
            }
        };
        //        MqttAgentPolicy.registerStatusListener(iStatusListener);

        MqttManager.getInstance().registerTraceListener(new MqttTraceHandler() {
            @Override
            public void traceDebug(String var1, String var2) {
                Trace.d(TAG, "traceDebug() " + var1 + "\n" + var2);
            }

            @Override
            public void traceError(String var1, String var2) {
                Trace.d(TAG, "traceError() " + var1 + "\n" + var2);
            }

            @Override
            public void traceException(String var1, String var2, Exception var3) {
                Trace.d(TAG, "traceException() " + var1 + "\n" + var2);
            }
        });
        MqttManager.getInstance().setTraceEnable(true);

        register_request_broadcast();
    }

    @Override
    protected void onDestroy() {
        MqttAgentPolicy.unregisterStatusListener(iStatusListener);
        super.onDestroy();
    }

    public void click_connect(View view) {
        MqttAgentPolicy.connect();
    }

    public void click_disconnect(View view) {
        MqttAgentPolicy.disConnect();
    }

    public void click_check_version(View view) {
//        OtaAgentPolicy.checkVersionAsync(new ICheckVersionCallback() {
//            @Override
//            public void onCheckSuccess(VersionInfo versionInfo) {
//                Trace.d(TAG, "onCheckSuccess() ");
//            }
//
//            @Override
//            public void onCheckFail(int status) {
//                Trace.d(TAG, "onCheckFail() " + status);
//            }
//        });

        Pair<Integer, VersionInfo> pair = OtaAgentPolicy.checkVersionExecute();
        Trace.d(TAG, "click_check_version() "+pair.first);
    }

    public void click_download(View view) {
//                OtaAgentPolicy.downloadEnqueue(new IDownloadListener() {
//                    @Override
//                    public void onPrepare() {
//                        Trace.d(TAG, "onPrepare() ");
//                    }
//
//                    @Override
//                    public void onDownloadProgress(long downSize, long totalSize) {
//                        Trace.d(TAG, "onDownloadProgress() "+"progress:"+((downSize + 0.5f)/totalSize)+",downSize:" + downSize + ",totalSize:" + totalSize);
//                    }
//
//                    @Override
//                    public void onFailed(int error) {
//                        Trace.d(TAG, "onFailed() " + error);
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        Trace.d(TAG, "onCompleted() ");
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        Trace.d(TAG, "onCancel() ");
//                    }
//                });
//        OtaAgentPolicy.downloadAsync(new IDownloadListener() {
//            @Override
//            public void onPrepare() {
//                Trace.d(TAG, "onPrepare() ");
//            }
//
//            @Override
//            public void onDownloadProgress(long downSize, long totalSize) {
//                Trace.d(TAG, "onDownloadProgress() "+(downSize*100)/totalSize);
//            }
//
//            @Override
//            public void onFailed(int error) {
//                Trace.d(TAG, "onFailed() "+error);
//            }
//
//            @Override
//            public void onCompleted() {
//                Trace.d(TAG, "onCompleted() ");
//            }
//
//            @Override
//            public void onCancel() {
//                Trace.d(TAG, "onCancel() ");
//            }
//        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                OtaAgentPolicy.downloadExecute(new IDownSimpleListener() {
                    @Override
                    public void on_start() {//工作线程中
                        Trace.d(TAG, "on_start() " + Thread.currentThread().getName());

//                        SystemClock.sleep(3000);
                    }

                    @Override
                    public void onCompleted(File file) {
                        Trace.d(TAG, "onCompleted() " + file.getName()+","+Thread.currentThread().getName());
                    }

                    @Override
                    public void onDownloadProgress(long downSize, long totalSize, int progress) {
                        Trace.d(TAG, "onDownloadProgress() " + progress);
                    }

                    @Override
                    public void onFailed(int error) {
                        Trace.d(TAG, "onFailed() " + error);
                    }

                    @Override
                    public void onCancel() {
                        Trace.d(TAG, "onCancel() ");
                    }
                },true);
                Trace.d(TAG, "完成");
            }
        }).start();


//        DLManager.getInstance().add(new DownEntity("https://mc20.monsieur-cuisine.com/20edd8d6-699c-43bd-b4a7-cb58db4efc41/DDFile.json","/mnt/sdcard/dd.txt"));
//        DLManager.getInstance().execAsync(new IDownSimpleListener() {
//            @Override
//            public void onCompleted(File file) {
//                Log.d(TAG, "onCompleted: ");
//                super.onCompleted(file);
//            }
//
//            @Override
//            public void onFailed(int error) {
//                Trace.d(TAG, "onFailed() "+error);
//                super.onFailed(error);
//            }
//        });
    }

    public void click_reboot(View view) {
        OtaAgentPolicy.rebootUpgrade(new IRebootUpgradeCallBack() {
            @Override
            public boolean rebootConditionPrepare() {
                SystemClock.sleep(3000);
                return true;
            }

            @Override
            public void onError(int error) {
                Trace.d(TAG, "onError() "+error);
            }
        });
    }

    public void click_cancel(View view) {
        OtaAgentPolicy.downloadCancel();
    }

    public void click_close_keep_connect(View view) {
        MqttAgentPolicy.stopKeepConnect();
    }

    public void click_http_request(View view) {

        //        JSONObject jsonObject = new JSONObject();
        //        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        //        try {
        //            jsonObject.put("mid", deviceInfo.mid);
        //            jsonObject.put("oem", deviceInfo.oem);
        //            jsonObject.put("models", deviceInfo.models);
        //            jsonObject.put("platform", deviceInfo.platform);
        //            jsonObject.put("deviceType", deviceInfo.deviceType);
        //            jsonObject.put("sdkversion", com.abupdate.mqtt_libs.BuildConfig.VERSION_NAME);
        //            jsonObject.put("version", deviceInfo.version);
        //        } catch (JSONException e) {
        //            e.printStackTrace();
        //            Trace.d(TAG, "doPostRegister() :" + e.toString());
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //            Trace.d(TAG, "doPostRegister() :" + e.toString());
        //        }
        //        Trace.d(TAG, "click_http_request() "+jsonObject.toString());
        //        final StringRequest stringRequest = new StringRequest("https://iotapi.abupdate.com/register/"+ DeviceInfo.getInstance().productId)
        //                .setMethod(HttpMethods.Post)
        //                .setHeaderContentType(RequestConfig.ContentType.TYPE_JSON)
        //                .setContent(jsonObject.toString().getBytes())
        //                .setHttpListener(new HttpListener() {
        //                    @Override
        //                    public void onSuccess(Data data, Response response) {
        //                        Trace.d(TAG, "onSuccess() "+Thread.currentThread().getName());
        //                        Trace.d(TAG, "onSuccess() Data:"+data.getApi()+"\n"+data.getMessage());
        //                        Trace.d(TAG, "onSuccess() "+response.resToString());
        //                    }
        //
        //                    @Override
        //                    public void onFailure(HttpException e, Response response) {
        //                        Trace.d(TAG, "onFailure() "+Thread.currentThread().getName());
        //                        Trace.d(TAG, "onFailure() 重试次数："+response.getRetryTimes());
        //                        Trace.d(TAG, "onFailure() 重定向次数："+response.getRedirectTimes());
        //                        Trace.d(TAG, "onFailure() 状态码："+(response.getHttpStatus()==null?"null":response.getHttpStatus().getDescriptionInChinese()));
        //                        Trace.d(TAG, "onFailure() "+response.resToString());
        //                    }
        //
        //                    @Override
        //                    public void onStart(AbstractRequest request) {
        //                        Trace.d(TAG, "onStart() "+Thread.currentThread().getName());
        //                        super.onStart(request);
        //                    }
        //
        //                    @Override
        //                    public void onEnd(Response response) {
        //                        Trace.d(TAG, "onEnd() "+Thread.currentThread().getName());
        //                        super.onEnd(response);
        //                    }
        //
        //                    @Override
        //                    public void onRetry(AbstractRequest request, int max, int times) {
        //                        Trace.d(TAG, "onRetry() max:"+max+",times:"+times);
        //                    }
        //
        //                    @Override
        //                    public void onRedirect(AbstractRequest request, int max, int times) {
        //                        Trace.d(TAG, "onRedirect() max:"+max+",times:"+times);
        //                        super.onRedirect(request, max, times);
        //                    }
        //                });
        ////        app.httpManager.enqueue(stringRequest);
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //                InternalResponse execute = app.httpManager.execute(stringRequest);
        //                Trace.d(TAG, "click_http_request() "+execute.isResultOk());
        //                Trace.d(TAG, "run() "+execute.getResult().getApi()+"\n"+execute.getResult().getMessage());
        //            }
        //        }).start();
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //                long l = System.currentTimeMillis();
        //                JSONObject jsonObject = new JSONObject();
        //                try {
        //                    jsonObject.put("msgId","130");
        //                    jsonObject.put("timestamp",""+l);
        //                } catch (JSONException e) {
        //                    e.printStackTrace();
        //                }
        //                HttpTools.getInstance().dopostMsgPushResponse("130");
        //            }
        //        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpIotUtils.postJson("https://baidu.com/xxx")
                            .json(new JSONObject("{\"a\":\"b\"}"))
                            .exec();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 需要动态注册的广播
     */
    public void register_request_broadcast() {
        OTABroadcastReceiver otaBroadcastReceiver = new OTABroadcastReceiver();
        //消息推送广播
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(otaBroadcastReceiver, new IntentFilter(BroadcastConsts.ACTION_FOTA_NOTIFY));

    }

    public void click_get_device_info(View view) {
//        Trace.d(TAG, "click_get_device_info() "+ IndirectOtaAgentPolicy.getDeviceInfo());
//        Trace.json(TAG,IndirectOtaAgentPolicy.getDeviceInfo());
//
//        IndirectOtaAgentPolicy.resetDeviceInfo(IndirectOtaAgentPolicy.getDeviceInfo().replace("V3","V4"));
//        Trace.d(TAG, "click_get_device_info() "+ DeviceInfo.getInstance().toString());

        String versionInfo = IndirectOtaAgentPolicy.getVersionInfo();
        Trace.json(TAG,versionInfo);
        IndirectOtaAgentPolicy.setVersionInfo(versionInfo.replace("V5","V4"));

        Trace.d(TAG, "click_get_device_info() "+VersionInfo.getInstance().getReleaseNoteByCurrentLanguage());
    }

    public void click_report_error_file(View view) {
//        for (int i = 0; i < 100; i++) {
//            LogManager.getInstance().saveTraceLog();
//        }
//        LogManager.getInstance().report();
        Trace.d(TAG, "click_report_error_file() "+(1 << 2));
        SPFTool.putLong(SPFTool.KEY_LAST_RECOVERY_TIME, System.currentTimeMillis());
        LogManager.getInstance().zipRecoveryLog();
        LogManager.getInstance().report();
        byte b = -1;

//        byte[] bytes = "ABCDEF".getBytes();
//        byte[] bytes1 = Encrypt.encryptMD5(bytes);
//        for (int i = 0; i < bytes1.length; i++) {
//            Trace.d(TAG, "click_report_error_file() "+bytes1[i]);
//        }
//        Encrypt.bytes2HexString(bytes1);
//        Trace.d(TAG, "click_report_error_file() ==================================");
//        String abcdef = Encrypt.getMd5("ABCDEF");
//        Trace.d(TAG, "click_report_error_file() "+abcdef);
    }

    public void click_to_sota(View view) {
        Intent intent = new Intent();
        intent.setClass(this,SotaActivity.class);
        startActivity(intent);
    }

    private class OTABroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {

                case BroadcastConsts.ACTION_FOTA_NOTIFY:
                    String body = intent.getStringExtra(BroadcastConsts.KEY_FOTA_NOTIFY);
                    Trace.d(TAG, "onReceive() notify:" + body);
                    show_notification(body);
                    break;
            }
        }
    }

    //通知栏显示更新消息
    public void show_notification(String msg) {
        Trace.d(TAG, "show_notification():" + msg);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("推送消息")
                        .setContentText(msg);
        Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0, resultIntent, 0);

        mBuilder.setContentIntent(contentIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1, mBuilder.build());
    }
}
