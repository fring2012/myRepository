package com.abupdate.iot_download_libs;

import java.util.List;

/**
 * # 异步下载过程中的回调监听
 * Created by y2222 on 17/07/04.
 */
public interface IOnDownListener {
    /**
     * # 开始下载
     * 该方法，会在执行开始下载时，立即回调
     * 该方法只回调一次
     */
    void on_start();

    /**
     * 所有下载任务下载成功，标明此次下载任务结束
     * 该方法只回调一次
     *
     * @param successDownEntities 所有下载成功的任务
     * @param failedDownEntities  所有下载失败的任务,请通过{@link DownEntity#download_status}查看下载失败状态
     */
    void on_finished(List<DownEntity> successDownEntities, List<DownEntity> failedDownEntities);

    /**
     * 某个任务下载成功
     * 回调次数为任务数减去回调on_failed()次数
     *
     * @param downEntity 下载成功的任务
     */
    void on_success(DownEntity downEntity);

    /**
     * 某个下载任务下载失败
     * 回调次数为任务数减去回调on_success()次数
     *
     * @param errDownEntity 下载失败的任务,请通过{@link DownEntity#download_status}查看下载失败状态
     */
    void on_failed(DownEntity errDownEntity);

    /**
     * 通过调用{@link DLManager#cancel_all()}来取消下载时回调,标明此次下载任务结束
     */
    void on_manual_cancel();

    /**
     * 下载任务总进度
     *
     * @param progress   下载任务总进度 range(0~100)
     * @param down_size  下载任务已下载大小
     * @param total_size 下载任务总大小
     */
    void on_all_progress(int progress, long down_size, long total_size);

    /**
     * 单个下载文件的下载进度
     *
     * @param progress   下载任务进度 range(0~100)
     * @param down_size  下载任务已下载大小
     * @param total_size 下载任务大小
     */
    void on_progress(DownEntity entity, int progress, long down_size, long total_size);
}
