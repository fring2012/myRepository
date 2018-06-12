package com.abupdate.iot_download_libs;

import android.app.Activity;
import android.os.Handler;

import com.abupdate.trace.Trace;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2017/12/21
 */
public class CallBackManager {
    private static final String TAG = "CallBackManager";
    public static CallBackManager mInstance;
    private Handler handler;
    Object lock = new Object();
    private IOnDownListener listener;
    private boolean shouldLock = true;

    public static CallBackManager getInstance() {
        if (mInstance == null) {
            synchronized (CallBackManager.class) {
                if (mInstance == null) {
                    mInstance = new CallBackManager();
                }
            }
        }
        return mInstance;
    }


    public void setCallbackOnUIThread(Activity activity) {
        shouldLock = true;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler = new Handler();
                unLock();
            }
        });
        lock();
    }

    public void setCallbackOnUIThread(Handler handler) {
        this.handler = handler;
    }

    public void setListener(IOnDownListener m_listener) {
        this.listener = m_listener;
    }

    public void on_start() {
        listener.on_start();
    }

    public void on_progress(final DownEntity downEntity, final int progress, final long downloaded_size, final long file_size) {
        if (null == handler) {
            listener.on_progress(downEntity, progress, downloaded_size, file_size);
        } else {
            shouldLock = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.on_progress(downEntity, progress, downloaded_size, file_size);
                    unLock();
                }
            });
            lock();
        }
    }

    public void on_failed(final DownEntity downEntity) {
        if (null == handler) {
            listener.on_failed(downEntity);
        } else {
            shouldLock = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.on_failed(downEntity);
                    unLock();
                }
            });
            lock();
        }
    }

    public void on_success(final DownEntity downEntity) {
        if (null == handler) {
            listener.on_success(downEntity);
        } else {
            shouldLock = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.on_success(downEntity);
                    unLock();
                }
            });
            lock();
        }
    }

    public void on_all_progress(final int cur_progress, final long total_downing_size, final long final_total_size) {
        if (null == handler) {
            listener.on_all_progress(cur_progress, total_downing_size, final_total_size);
        } else {
            shouldLock = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.on_all_progress(cur_progress, total_downing_size, final_total_size);
                    unLock();
                }
            });
            lock();
        }
    }

    public void on_manual_cancel() {
        if (null == handler) {
            listener.on_manual_cancel();
        } else {
            shouldLock = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.on_manual_cancel();
                    unLock();
                }
            });
            lock();
        }
    }

    public void on_finished(final List<DownEntity> m_success_entity_list, final List<DownEntity> m_failed_entity_list) {
        if (null == handler) {
            listener.on_finished(m_success_entity_list, m_failed_entity_list);
        } else {
            shouldLock = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.on_finished(m_success_entity_list, m_failed_entity_list);
                    unLock();
                }
            });
            lock();
        }
    }

    private void lock() {
        synchronized (lock) {
            if (!shouldLock){
                return;
            }
            try {
                lock.wait(5000);
            } catch (Exception e) {
                Trace.e(TAG, e);
            }
        }
    }

    private void unLock() {
        synchronized (lock) {
            try {
                shouldLock = false;
                lock.notify();
            } catch (Exception e) {
                Trace.e(TAG, e);
            }
        }
    }
}
