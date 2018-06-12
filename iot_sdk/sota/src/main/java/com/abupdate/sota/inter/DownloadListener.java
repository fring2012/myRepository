package com.abupdate.sota.inter;

import com.abupdate.iot_download_libs.DownEntity;
import com.abupdate.iot_download_libs.IOnDownListener;
import com.abupdate.sota.info.remote.NewAppInfo;
import com.abupdate.sota.inter.multi.DownloadTask;
import com.abupdate.sota.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/12
 */
public abstract class DownloadListener implements IOnDownListener {

    @Override
    public void on_start() {
    }

    @Override
    public void on_finished(List<DownEntity> successList, List<DownEntity> failList) {
        List<NewAppInfo> successInfos = new ArrayList<>();
        List<NewAppInfo> failInfos = new ArrayList<>();
        for (DownEntity successDownEntity : successList) {
            if (DownloadTask.entityNewAppInfoMap.containsKey(successDownEntity)) {
                NewAppInfo successAppInfo = DownloadTask.entityNewAppInfoMap.get(successDownEntity);
                successInfos.add(successAppInfo);
                DownloadTask.entityNewAppInfoMap.remove(successDownEntity);
            }
        }
        for (DownEntity failDownEntity : failList) {
            if (DownloadTask.entityNewAppInfoMap.containsKey(failDownEntity)) {
                NewAppInfo failAppInfo = DownloadTask.entityNewAppInfoMap.get(failDownEntity);
                failInfos.add(failAppInfo);
                DownloadTask.entityNewAppInfoMap.remove(failDownEntity);
            }
        }
        DownloadTask.entityNewAppInfoMap.clear();
        onSotaAllFinished(successInfos, failInfos);
    }

    @Override
    public void on_success(DownEntity downEntity) {
        if (DownloadTask.entityNewAppInfoMap.containsKey(downEntity)) {
            NewAppInfo newAppInfo = DownloadTask.entityNewAppInfoMap.get(downEntity);
            String file = downEntity.file_path.replace(".temp", "");
            FileUtil.fileRename(downEntity.file_path, file);
            onSotaSingleSuccess(newAppInfo,file);

        }
    }

    @Override
    public void on_failed(DownEntity downEntity) {
        if (DownloadTask.entityNewAppInfoMap.containsKey(downEntity)) {
            NewAppInfo newAppInfo = DownloadTask.entityNewAppInfoMap.get(downEntity);
            onSotaSingleFail(newAppInfo);
        }
    }

    @Override
    public void on_manual_cancel() {

    }

    @Override
    public void on_all_progress(int i, long l, long l1) {
    }

    @Override
    public void on_progress(DownEntity downEntity, int i, long l, long l1) {
        if (DownloadTask.entityNewAppInfoMap.containsKey(downEntity)) {
            onSotaSingleProgress(DownloadTask.entityNewAppInfoMap.get(downEntity), i, l, l1);
        }
    }

    public void onSotaAllFinished(List<NewAppInfo> successInfos, List<NewAppInfo> failedInfos) {
    }

    public void onSotaSingleSuccess(NewAppInfo appInfo,String file) {
    }

    public void onSotaSingleFail(NewAppInfo appInfo) {
    }

    public void onSotaSingleProgress(NewAppInfo info, int progress, long down_size, long total_size) {
    }

}
