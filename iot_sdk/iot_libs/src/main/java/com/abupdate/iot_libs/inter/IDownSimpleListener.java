package com.abupdate.iot_libs.inter;

import com.abupdate.iot_download_libs.DownEntity;
import com.abupdate.iot_download_libs.IOnDownListener;
import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.utils.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author fighter_lee
 * @date 2017/12/20
 */
public abstract class IDownSimpleListener implements IOnDownListener {
    @Override
    public void on_start() {
    }

    @Override
    public void on_success(DownEntity downEntity) {
        if (new File(downEntity.file_path).exists()) {
            if (new File(OtaAgentPolicy.getConfig().updatePath).exists()){
                new File(OtaAgentPolicy.getConfig().updatePath).delete();
            }
            FileUtil.fileRename(downEntity.file_path, OtaAgentPolicy.config.updatePath);
        }
        onCompleted(new File(OtaAgentPolicy.getConfig().updatePath));
    }

    @Override
    public void on_failed(DownEntity errDownEntity) {
        onFailed(errDownEntity.download_status);
    }

    @Override
    public void on_manual_cancel() {
        onCancel();
    }

    @Override
    public void on_finished(List<DownEntity> successDownEntities, List<DownEntity> failedDownEntities) {

    }

    @Override
    public void on_all_progress(int progress, long down_size, long total_size) {

    }

    @Override
    public void on_progress(DownEntity entity, int progress, long down_size, long total_size) {
        onDownloadProgress(down_size, total_size, progress);
    }

    /**
     * 下载成功回调
     *
     * @param file
     */
    public void onCompleted(File file) {
    }

    /**
     * 下载进度回调
     *
     * @param downSize
     * @param totalSize
     * @param progress
     */
    public void onDownloadProgress(long downSize, long totalSize, int progress) {
    }

    /**
     * 下载失败回调
     *
     * @param error
     */
    public void onFailed(int error) {
    }

    /**
     * 取消下载回调
     */
    public void onCancel() {
    }

}
