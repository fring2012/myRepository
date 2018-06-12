package com.abupdate.iot_libs.inter;

/**
 * Created by fighter_lee on 2017/5/16.
 */

public interface IDownloadListener{

    void onPrepare();

    void onDownloadProgress(long downSize, long totalSize);

    void onFailed(int error);

    void onCompleted();

    void onCancel();
}
