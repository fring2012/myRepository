package com.abupdate.iot_libs.engine;

import android.os.Handler;

import com.abupdate.iot_libs.constant.Error;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.ICheckVersionCallback;
import com.abupdate.iot_libs.inter.IDownloadListener;
import com.abupdate.iot_libs.inter.IRebootUpgradeCallBack;
import com.abupdate.iot_libs.inter.IRegisterListener;
import com.abupdate.trace.Trace;

/**
 * @author fighter_lee
 * @date 2017/12/21
 */
public class IOTCallbackManager {

    private static final String TAG = "IOTCallbackManager";
    public Handler handler;
    Object lock = new Object();
    private static IOTCallbackManager mInstance;

    private static IDownloadListener sDownloadListener;
    private static ICheckVersionCallback sCheckVersionListener;
    private static IRegisterListener sRegisterListener;
    private static IRebootUpgradeCallBack sIRebootUpgradeCallBack;

    public static IOTCallbackManager getInstance() {
        if (mInstance == null) {
            synchronized (IOTCallbackManager.class) {
                if (mInstance == null) {
                    mInstance = new IOTCallbackManager();
                }
            }
        }
        return mInstance;
    }

    public void setCallbackOnUIThread(Handler handler) {
        this.handler = handler;
    }

    public void onRegisterSuccess() {
        if (null != sRegisterListener) {
            sRegisterListener.onSuccess();
        }
    }

    public void onRegisterFailed(int registerResult) {
        if (null != sRegisterListener) {
            sRegisterListener.onFailed(registerResult);
        }
    }

    public void onCheckVersionSuccess() {
        Trace.d(TAG, "%s%s%s", OtaConstants.DOUBLE_LINE, "onCheckVersionSuccess", OtaConstants.DOUBLE_LINE);
        handler.post(new Runnable() {
            @Override
            public void run() {
                sCheckVersionListener.onCheckSuccess(VersionInfo.getInstance());
            }
        });
    }

    public void onCheckVersionFailed(final int error) {
        Trace.d(TAG, "%s%s%s", OtaConstants.DOUBLE_LINE, "onCheckVersionFailed", OtaConstants.DOUBLE_LINE);
        Trace.d(TAG, "onCheckVersionFailed() error code:" + error + ",message:" + Error.getErrorMessage(error));
        if (sCheckVersionListener != null) {
            if (handler == null){
                Trace.d(TAG, "onCheckVersionFailed() handler == null");
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (sCheckVersionListener != null) {
                        sCheckVersionListener.onCheckFail(error);
                    }else{
                        Trace.d(TAG, "onCheckVersionFailed() sCheckVersionListener==null");
                    }
                }
            });
        } else {
            Trace.d(TAG, "onCheckVersionFailed() sCheckVersionListener==null");
        }
    }

    public void downloadCallback(final int i, final long downSize, final long totalSize, final int errorCode) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                switch (i) {
                    case OtaConstants.DOWNLOAD_CALLBACK_PREPARE:
                        sDownloadListener.onPrepare();
                        break;
                    case OtaConstants.DOWNLOAD_CALLBACK_PROGRESS:
                        sDownloadListener.onDownloadProgress(downSize, totalSize);
                        break;
                    case OtaConstants.DOWNLOAD_CALLBACK_CANCEL:
                        sDownloadListener.onCancel();
                        break;
                    case OtaConstants.DOWNLOAD_CALLBACK_SUCCESS:
                        sDownloadListener.onCompleted();
                        Trace.d(TAG, "%s%s%s", OtaConstants.DOUBLE_LINE, "download success", OtaConstants.DOUBLE_LINE);
                        break;
                    case OtaConstants.DOWNLOAD_CALLBACK_FAILED:
                        sDownloadListener.onFailed(errorCode);
                        Trace.d(TAG, "%s%s%s", OtaConstants.DOUBLE_LINE, "download failed", OtaConstants.DOUBLE_LINE);
                        Trace.d(TAG, "onUpdateFailed() error code:" + errorCode + ",message:" + Error.getErrorMessage(errorCode));
                        break;
                    default:
                        sDownloadListener.onFailed(Error.ERROR);
                }
            }
        });

    }

    public void onUpdateFailed(final int error) {
        Trace.d(TAG, "%s%s%s", OtaConstants.DOUBLE_LINE, "onUpdateFailed", OtaConstants.DOUBLE_LINE);
        Trace.d(TAG, "onUpdateFailed() error code:" + error + ",message:" + Error.getErrorMessage(error));
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (sIRebootUpgradeCallBack != null) {
                    sIRebootUpgradeCallBack.onError(error);
                }
            }
        });
    }

    private void lock() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (Exception e) {
                Trace.e(TAG, e);
            }
        }
    }

    private void unLock() {
        synchronized (lock) {
            try {
                lock.notify();
            } catch (Exception e) {
                Trace.e(TAG, e);
            }
        }
    }

    public static void setRegisterListener(IRegisterListener registerListener) {
        sRegisterListener = registerListener;
    }

    public static void setDownloadListener(IDownloadListener listener) {
        sDownloadListener = listener;
    }

    public static void setCheckVersionListener(ICheckVersionCallback listener) {
        sCheckVersionListener = listener;
    }

    public static void setUpdateCallBack(IRebootUpgradeCallBack iRebootUpgradeCallBack) {
        sIRebootUpgradeCallBack = iRebootUpgradeCallBack;
    }

    public static IDownloadListener getDownloadListener() {
        return sDownloadListener;
    }

    public static ICheckVersionCallback getCheckVersionListener() {
        return sCheckVersionListener;
    }

    public static IRegisterListener getRegisterListener() {
        return sRegisterListener;
    }

    public static IRebootUpgradeCallBack getIRebootUpgradeCallBack() {
        return sIRebootUpgradeCallBack;
    }

}
