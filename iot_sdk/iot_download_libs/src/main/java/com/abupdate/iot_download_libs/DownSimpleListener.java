package com.abupdate.iot_download_libs;

import java.util.List;

/**
 * Created by raise.yang on 17/07/04.
 */
public abstract class DownSimpleListener implements IOnDownListener {

    public static DownSimpleListener INSTANCE = new DownSimpleListener() {
    };

    @Override
    public void on_start() {

    }

    @Override
    public void on_finished(List<DownEntity> successDownEntities, List<DownEntity> failedDownEntities) {

    }

    @Override
    public void on_success(DownEntity downEntity) {

    }

    @Override
    public void on_failed(DownEntity errDownEntity) {

    }

    @Override
    public void on_manual_cancel() {

    }

    @Override
    public void on_all_progress(int progress, long down_size, long total_size) {

    }

    @Override
    public void on_progress(DownEntity entity, int progress, long down_size, long total_size) {

    }
}
