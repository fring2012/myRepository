package com.abupdate.iot_sdk;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

//import com.abupdate.sota.SotaControler;
//import com.abupdate.sota.info.remote.NewAppInfo;
//import com.abupdate.sota.inter.CheckAllAppListener;
//import com.abupdate.sota.inter.CheckNewVersionListener;
//import com.abupdate.sota.inter.DownloadListener;
//import com.abupdate.sota.inter.multi.DownloadTask;
import com.abupdate.trace.Trace;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/9
 */
public class SotaActivity extends Activity {
    private static final String TAG = "SotaActivity";
//    List<NewAppInfo> newInfos;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sota);
//    }
//
//    public void click_check(View view) {
//        //        new Thread(new Runnable() {
//        //            @Override
//        //            public void run() {
//        //                SotaControler.checkAllApp().executed();
//        //            }
//        //        }).start();
//        SotaControler.checkAllApp()
//                .setCallbackToMain()
//                .enqueue(new CheckAllAppListener() {
//                    @Override
//                    public void onSuccess(List<String> packageNames) {
//                        Trace.d(TAG, "onSuccess() " + Thread.currentThread().getName());
//                        for (String packageName : packageNames) {
//                            Trace.d(TAG, "onSuccess() " + packageName);
//                        }
//                    }
//
//                    @Override
//                    public void onFailed(int code) {
//                        Trace.d(TAG, "onFailed() " + code);
//                    }
//
//                    @Override
//                    public void onNetError() {
//                        Trace.d(TAG, "onNetError() ");
//                    }
//                });
//    }
//
//    public void click_check_new(View view) {
//        SotaControler.checkNewVersion()
//                .setCallbackToMain()
//                .enqueue(new CheckNewVersionListener() {
//                    @Override
//                    public void onSuccess(List<NewAppInfo> newAppInfos) {
//
//                        for (NewAppInfo newAppInfo : newAppInfos) {
//                            Trace.d(TAG, "onSuccess() " + newAppInfo.getPackageName());
//                        }
//                        newInfos = newAppInfos;
//                    }
//
//                    @Override
//                    public void onFailed(int code) {
//                        Trace.d(TAG, "onFailed() " + code);
//                    }
//
//                    @Override
//                    public void onNetError() {
//                        Trace.d(TAG, "onNetError() ");
//                    }
//                });
//    }
//
//    public void click_download(View view) {
//        DownloadTask downloadTask = SotaControler.download();
//        for (NewAppInfo newInfo : newInfos) {
//            downloadTask.addDLTask(newInfo);
//        }
//        downloadTask.enqueue(new DownloadListener() {
//            @Override
//            public void onSotaAllFinished(List<NewAppInfo> successInfos, List<NewAppInfo> failedInfos) {
//                Trace.d(TAG, "onSotaAllFinished() "+successInfos.size());
//                Trace.d(TAG, "onSotaAllFinished() "+failedInfos.size());
//            }
//
//            @Override
//            public void onSotaSingleSuccess(NewAppInfo appInfo,String filePath) {
//                Trace.d(TAG, "onSotaSingleSuccess() "+appInfo.getPackageName());
//            }
//
//            @Override
//            public void onSotaSingleFail(NewAppInfo appInfo) {
//                Trace.d(TAG, "onSotaSingleFail() "+appInfo.getPackageName());
//            }
//
//            @Override
//            public void onSotaSingleProgress(NewAppInfo info, int progress, long down_size, long total_size) {
//                Trace.d(TAG, "onSotaSingleProgress() "+progress);
//            }
//
//            @Override
//            public void on_all_progress(int i, long l, long l1) {
//                Trace.d(TAG, "on_all_progress() "+i);
//            }
//        });
//    }
}
