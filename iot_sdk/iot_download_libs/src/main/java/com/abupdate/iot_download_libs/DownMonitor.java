package com.abupdate.iot_download_libs;

import com.abupdate.trace.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * # 下载监视器
 * 监视所有下载任务，下载线程的状态，回调给调用者
 * Created by raise.yang on 17/07/13.
 */

public class DownMonitor implements Callable<Boolean> {
    private String TAG = "DownMonitor";
    private List<DownEntity> m_entity_list;//存放所有下载任务
    private List<DownEntity> m_downing_entity_list;//存放正在下载的列表
    private List<DownEntity> m_success_entity_list = new ArrayList<>();//存放下载成功的列表
    private List<DownEntity> m_failed_entity_list = new ArrayList<>();//存放下载失败的列表
    private List<DownThread> m_thread_list;//存放所有下载子线程

    private DLManager m_dlManager;
    private final long final_total_size;//所有下载任务总大小

    public DownMonitor() {
        m_dlManager = DLManager.getInstance();
        m_entity_list = new ArrayList<>(m_dlManager.getEntity_list());
        m_downing_entity_list = new ArrayList<>(m_entity_list);
        m_thread_list = new ArrayList<>(m_dlManager.getThread_list());
        long total_size = 0;
        for (DownEntity entity : m_downing_entity_list) {
            total_size += entity.file_size;
        }
        final_total_size = total_size;
    }

    /**
     * 删除下载成功的任务的临时文件
     * 删除因md5校验失败的临时文件
     */
    private void delete_temp_files() {
        for (DownEntity entity : m_success_entity_list) {
            for (DownThread downThread : m_thread_list) {
                if (entity.equals(downThread.downEntity)) {
                    downThread.delete_temp_record_file();
                }
            }
        }
        for (DownEntity entity : m_failed_entity_list) {
            if (entity.download_status == DownError.ERROR_MD5_VERIFY_FAILED) {
                for (DownThread downThread : m_thread_list) {
                    if (entity.equals(downThread.downEntity)) {
//                        downThread.delete_temp_record_file();
                        DLManager.getInstance().deleteTempFile(entity);
                    }
                }
            }
            if (entity.download_status == DownError.ERROR_BLOCK_VERIFY_FAIL) {
                for (DownThread downThread : m_thread_list) {
                    if (entity.equals(downThread.downEntity)) {
//                        downThread.delete_temp_record_file();
                        DLManager.getInstance().deleteTempFile(entity);
                    }
                }
            }
        }
    }

    private boolean verify_md5(DownEntity downEntity) {
        String local_md5 = DownUtils.getMd5ByFile(new File(downEntity.file_path));
        Trace.i(TAG, "verify_md5() local md5:" + local_md5 + ",server md5:" + downEntity.md5);
        //该文件下载成功，需要判断md5是否相等
        if (downEntity.md5 == null) {
            Trace.d(TAG, "md5 == null，不需要校验");
            return true;
        } else if (local_md5.equalsIgnoreCase(downEntity.md5)) {
            //MD5校验成功
            Trace.d(TAG, "md5校验成功");
            return true;
        } else {
            Trace.e(TAG, "run() entity:" + downEntity.file_size + "md5校验失败");
            return false;
        }
    }

    /**
     * 所有已经下载完成的大小
     *
     * @return
     */
    private long get_all_downloaded_file_size() {
        long finished_file_size = 0;
        for (DownEntity entity : m_entity_list) {
            finished_file_size += entity.downloaded_size;
        }
        // Trace.i(TAG, "get_all_downloaded_file_size() %s", finished_file_size);
        return finished_file_size;
    }

    @Override
    public Boolean call() throws Exception {
        Trace.d(TAG, "call() ");
        CallBackManager.getInstance().on_start();
        //记录通知的进度%
        int all_progress_post = 0;
        //循环检索每个任务的下载状态是否完成
        while (true) {
            // Trace.w(TAG, "run() while开始 正在下载任务数%s,下载成功任务数%s", m_downing_entity_list.size(), m_success_entity_list.size());
            //检索正在下载的列表进度
            for (int i = 0; i < m_downing_entity_list.size(); i++) {
                DownEntity downEntity = m_downing_entity_list.get(i);
                // Trace.d(TAG, "run() entity url = " + downEntity.url);
                //下载文件的已下载大小
                long cur_file_downing_size = 0;
                //下载错误
                boolean is_error = false;
                //该文件是否下载完成
                boolean is_finished = true;
                List<DownThread> temp_thread_list = new ArrayList<>(m_thread_list);
                for (int j = 0; j < temp_thread_list.size(); j++) {
                    DownThread downThread = temp_thread_list.get(j);
                    if (downEntity.equals(downThread.downEntity)) {
                        //如果是该下载文件的线程
                        cur_file_downing_size += downThread.downing_length;
                        // Trace.i(TAG, "run() downThread.downing_length = " + downThread.downing_length);
                        is_error = downThread.downEntity.download_status != DownError.NO_ERROR;
                        //线程池没有关闭，下载任务没有完成
                        if (is_finished && !downThread.is_finished()) {
                            is_finished = false;
                        }
                    }
                    temp_thread_list.remove(j--);
                }
                //防止下载进度变小
                if (downEntity.downloaded_size < cur_file_downing_size) {
                    downEntity.downloaded_size = cur_file_downing_size;
                }
                if (downEntity.downloaded_size > downEntity.file_size){
                    is_finished = true;
                }
                // Trace.w(TAG, "run() is_finished = %s;", is_finished);
                //回调该文件下载进度
                if (!downEntity.download_cancel) {
                    int progress = (int) (downEntity.downloaded_size * 100 / downEntity.file_size);
                    if (progress != 0) {
                        CallBackManager.getInstance().on_progress(downEntity, progress, downEntity.downloaded_size, downEntity.file_size);
                    }
                }

                //下载出错，download_cancel=true
                if (is_error) {
                    Trace.e(TAG, "run() download error, cancel other thread download tasks.error="+downEntity.download_status);
                    DLManager.getInstance().cancel_one(downEntity);
                }
                if (is_finished) {
                    // Trace.w(TAG, "run() is_finished");
                    m_downing_entity_list.remove(i--);

                    if (downEntity.download_cancel) {
                        m_failed_entity_list.add(downEntity);
                        if (downEntity.download_status != DownError.NO_ERROR) {
                            CallBackManager.getInstance().on_failed(downEntity);
                        }
                    } else {
                        // 没有错误的下载结束
                        if (verify_md5(downEntity)) {
                            if (!downEntity.download_cancel) {
                                CallBackManager.getInstance().on_success(downEntity);
                            }
                            m_success_entity_list.add(downEntity);
                        } else {
                            downEntity.download_status = DownError.ERROR_MD5_VERIFY_FAILED;
                            if (!downEntity.download_cancel) {
                                CallBackManager.getInstance().on_failed(downEntity);
                            }
                            m_failed_entity_list.add(downEntity);
                        }
                    }
                }
            }
            long total_downing_size = get_all_downloaded_file_size();
            if (total_downing_size != 0) {
                int cur_progress = (int) (total_downing_size * 100 / final_total_size);
                if (cur_progress > all_progress_post) {
                    //只有进度变化之后，才会通知到UI
                    all_progress_post = cur_progress;
                    Trace.i(TAG, "run() all_progress_post = %s", all_progress_post);
                    CallBackManager.getInstance().on_all_progress(cur_progress, total_downing_size, final_total_size);
                }
            }
            // Trace.d(TAG, "run() m_downing_entity_list.size() = " + m_downing_entity_list.size());


            if (m_downing_entity_list.size() == 0) {
                // Trace.w(TAG, "run() while结束");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if (DLManager.getInstance().is_cancel) {
                break;
            }
        }
        // Trace.i(TAG, "run() 下载任务结束,is_cancel = %s", DLManager.getInstance().is_cancel);
        //所有文件下载完成
        //1.删除临时文件->下载成功的任务
        //2.回调下载完成
        //3.重置DLManager状态
        delete_temp_files();
        if (DLManager.getInstance().is_cancel) {
            CallBackManager.getInstance().on_manual_cancel();
        } else {
            CallBackManager.getInstance().on_finished(m_success_entity_list, m_failed_entity_list);
        }
        DLManager.getInstance().reset();
        DLManager.getInstance().is_downloading = false;
        //        Trace.w(TAG, "run() download end.");
        return true;
    }
}
