package com.abupdate.iot_libs.engine;

import android.content.Context;
import android.os.Process;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.trace.Trace;

/**
 * @author fighter_lee
 * @date 2018/1/30
 */
public class CrashCatcher implements Thread.UncaughtExceptionHandler{
    private static final String TAG = "CrashCatcher";
    private Context mContext;
    private Thread.UncaughtExceptionHandler exceptionHandler;

    public static CrashCatcher mInstance;

    public static CrashCatcher getInstance() {
        if (mInstance == null) {
            synchronized (CrashCatcher.class) {
                if (mInstance == null) {
                    mInstance = new CrashCatcher();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handleException(e);
        if (exceptionHandler != null) {
            exceptionHandler.uncaughtException(t, e);
        }else{
            //退出程序
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }

    /**
     * 收集错误信息
     *
     * @param ex
     */
    private void handleException(Throwable ex) {
        if (ex == null) {
            return;
        }
        Trace.e(TAG,"Uncaught exception",ex);
        if (ex instanceof SecurityException && ex.getMessage().contains("READ_LOGS permission required")){
            Trace.e(TAG,"缺少系统权限，请进行平台签名！！！");
        }
        //若关闭了日志，则不上报
        if (!OtaAgentPolicy.getConfig().showTrace){
            return;
        }
        LogManager.getInstance().saveTraceLog();
    }
}
