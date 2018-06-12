package com.abupdate.sota.inter.multi;

import com.abupdate.iot_download_libs.DLManager;
import com.abupdate.iot_download_libs.DownEntity;
import com.abupdate.sota.SotaControler;
import com.abupdate.sota.info.remote.NewAppInfo;
import com.abupdate.sota.inter.DownloadListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fighter_lee
 * @date 2018/3/12
 */
public class DownloadTask {

    private static DownloadTask mInstance;

    public static DownloadTask getInstance() {
        if (mInstance == null) {
            synchronized (DownloadTask.class) {
                if (mInstance == null) {
                    mInstance = new DownloadTask();
                }
            }
        }
        return mInstance;
    }

    public static Map<DownEntity, NewAppInfo> entityNewAppInfoMap = new HashMap<>();

    public DownloadTask addDLTask(NewAppInfo info) {
        String tempPath = genTempFileName(info);
        DownEntity downEntity = new DownEntity(info.getDownUrl(), tempPath, info.getDownSize());
        if (DLManager.getInstance().add(downEntity)) {
            entityNewAppInfoMap.put(downEntity, info);
        }
        return this;
    }

    public void executed(DownloadListener downloadListener) {
        DLManager.getInstance().execute(downloadListener);
    }

    public void enqueue(DownloadListener downloadListener) {
        DLManager.getInstance().execAsync(downloadListener);
    }

    /**
     * 回调到主线程
     *
     * @return
     */
    public DownloadTask setCallbackToMain(boolean callbackToMain) {
        DLManager.getInstance().setCallbackOnUIThread(callbackToMain);
        return this;
    }

    private String genTempFileName(NewAppInfo info) {
        StringBuilder builder = new StringBuilder();
        builder.append(SotaControler.getConfig().downloadDir)
                .append(File.separator)
                .append(info.getAppName())
                .append("_")
                .append(info.getVersionName())
                .append(".apk")
                .append(".temp");
        return builder.toString();
    }
}
