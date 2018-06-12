package com.abupdate.iot_download_libs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.abupdate.trace.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * # 下载管理类
 * 持有所有下载任务
 * 使用方法：
 * <code>
 * //创建下载实体
 * DownEntity downEntity1 = new DownEntity(url,file_path,md5,file_size);
 * DownEntity downEntity2 = new DownEntity(url,file_path,md5,file_size);
 * //添加下载实体
 * DLManager.getInstance().add(downEntity1);
 * DLManager.getInstance().add(downEntity2);
 * //开始下载
 * DLManager.getInstance().execAsync(downloadListener);
 * <p>
 * downloadListener = new DownSimpleListener();
 * </code>
 */
public class DLManager {
    private static final String TAG = "DLManager";
    private static DLManager m_instance;
    //下载队列max = 20
    private List<DownEntity> m_entity_list = new ArrayList<DownEntity>();
    private List<DownThread> m_thread_list = new ArrayList<DownThread>();

    private IOnDownListener m_listener = DownSimpleListener.INSTANCE;

    volatile boolean is_downloading;
    volatile boolean is_cancel;


    //创建线程的工厂类
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "DLManager_" + mCount.getAndIncrement());
        }
    };
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CPU_COUNT + 1, CPU_COUNT * 2 + 1, 1,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1024), sThreadFactory);

    private ThreadPoolExecutor thread_pool = THREAD_POOL_EXECUTOR;
    public Context mCx;

    private DLManager() {
    }

    public static DLManager getInstance() {
        if (m_instance == null) {
            m_instance = new DLManager();
        }
        return m_instance;
    }

    public void setContext(Context context) {
        this.mCx = context;
    }

    /**
     * 是否正在下载
     *
     * @return true if is downloading.
     */
    public boolean is_downloading() {
        return is_downloading;
    }

    public List<DownEntity> getEntity_list() {
        return m_entity_list;
    }

    public List<DownThread> getThread_list() {
        return m_thread_list;
    }

    public IOnDownListener getListener() {
        return m_listener;
    }

    /**
     * 添加下载entry
     *
     * @param downEntity
     * @return
     */
    public boolean add(DownEntity downEntity) {
        if (is_downloading) {
            Trace.w(TAG, "add() can't add entity if download task is starting.");
            return false;
        }
        if (m_entity_list.contains(downEntity)) {
            Trace.w(TAG, "add() can't add entity if the downEntity(url) was add.");
            return false;
        }
        // 重置状态，防止多次下载引起的引用传递问题
        downEntity.download_cancel = false;
        downEntity.download_status = DownError.NO_ERROR;
        m_entity_list.add(downEntity);
        return true;
    }

    public void setCallbackOnUIThread(boolean main) {
        if (main){
            Handler handler = new Handler(Looper.getMainLooper());
            CallBackManager.getInstance().setCallbackOnUIThread(handler);
        }else{
            CallBackManager.getInstance().setCallbackOnUIThread((Handler) null);
        }
    }

    public void add(List<DownEntity> downEntities) {
        for (int i = 0; i < downEntities.size(); i++) {
            add(downEntities.get(i));
        }
    }

    public boolean execute(IOnDownListener onDownListener) {
        Trace.d(TAG, "execute() ");
        if (is_downloading) {
            Trace.d(TAG, "Download task has begun, can't repeat it.");
            return false;
        }
        synchronized (DLManager.this) {
            if (is_downloading) {
                Trace.d(TAG, "Download task has begun, can't repeat it.");
                return false;
            }
            is_downloading = true;
        }
        if (m_entity_list.size() == 0) {
            Trace.e(TAG, "execAsync() e = " + "down entity is null");
            is_downloading = false;
            return false;
        }
        //        Trace.i(TAG, "execAsync() entity[0] = %s", m_entity_list.get(0));
        if (onDownListener != null) {
            m_listener = onDownListener;
        }
        CallBackManager.getInstance().setListener(m_listener);

        for (DownEntity entity : m_entity_list) {
            m_thread_list.addAll(DownThreadGenerator.gen_threads(entity));
        }
        //        Trace.i(TAG, "execAsync() m_thread_list.size() = %s", m_thread_list.size());
        for (int i = 0; i < m_thread_list.size(); i++) {
            if (!m_thread_list.get(i).is_finished()) {
                //分配线程池去下载
                thread_pool.execute(m_thread_list.get(i));
            }
        }

        FutureTask<Boolean> futureTask = new FutureTask<>(new DownMonitor());

        futureTask.run();
        try {
            //TODO 如果get没有结果，会没有回调结果  java.util.concurrent.ExecutionException
            //            SystemClock.sleep(5000);
            Boolean get = futureTask.get();
            Trace.d(TAG, "execute() :" + get);
        } catch (Exception e) {
            Trace.e(TAG, e);
        }
        return true;

    }

    /**
     * # 开启异步下载
     * 若下载任务已经打开，则返回false
     *
     * @param onDownListener 下载任务监听器
     * @return true if invoke success.
     */
    public boolean execAsync(IOnDownListener onDownListener) {
        Trace.d(TAG, "execAsync() start. --%s", THREAD_POOL_EXECUTOR.getActiveCount());

        if (is_downloading) {
            Trace.d(TAG, "Download task has begun, can't repeat it.");
            return false;
        }
        synchronized (DLManager.this) {
            if (is_downloading) {
                Trace.d(TAG, "Download task has begun, can't repeat it.");
                return false;
            }
            is_downloading = true;
        }
        if (m_entity_list.size() == 0) {
            Trace.e(TAG, "execAsync() e = " + "down entity is null");
            is_downloading = false;
            return false;
        }
        //        Trace.i(TAG, "execAsync() entity[0] = %s", m_entity_list.get(0));
        if (onDownListener != null) {
            m_listener = onDownListener;
        }
        CallBackManager.getInstance().setListener(m_listener);

        thread_pool.execute(new Runnable() {
            @Override
            public void run() {
                //产生下载线程
                for (DownEntity entity : m_entity_list) {
                    m_thread_list.addAll(DownThreadGenerator.gen_threads(entity));
                }
                //        Trace.i(TAG, "execAsync() m_thread_list.size() = %s", m_thread_list.size());
                for (int i = 0; i < m_thread_list.size(); i++) {
                    if (!m_thread_list.get(i).is_finished()) {
                        //分配线程池去下载
                        thread_pool.execute(m_thread_list.get(i));
                    }
                }
                //独立与线程池之外的线程，用于检查下载过程
                FutureTask<Boolean> futureTask = new FutureTask<>(new DownMonitor());
                futureTask.run();
            }
        });
        return true;
    }

    void reset() {
        m_entity_list.clear();
        m_thread_list.clear();
        m_listener = DownSimpleListener.INSTANCE;
        is_cancel = false;
    }

    /**
     * 取消当前下载任务，结果会回调到下载监听中
     */
    public void cancel_all() {
        Trace.d(TAG, "cancel_all");
        if (!is_downloading) {
            Trace.w(TAG, "cancel_all() invalid,because the task isn't downloading.");
            return;
        }
        is_cancel = true;
        for (int i = 0; i < m_entity_list.size(); i++) {
            m_entity_list.get(i).download_cancel = true;
            //            Trace.i(TAG, "cancel_all() entity = %s,is_cancel = %s", m_entity_list.get(i).file_size, m_entity_list.get(i).download_cancel);
        }
    }

    public void cancel_one(DownEntity entity) {
        Trace.d(TAG, "cancel_one() ");
        if (!is_downloading) {
            Trace.w(TAG, "cancel_all() invalid,because the task isn't downloading.");
            return;
        }
        entity.download_cancel = true;
    }

    public boolean deleteTempFile(DownEntity entity) {
        File temp_folder = DownUtils.get_temp_folder(entity);
        File file = new File(entity.file_path);
        boolean fileDel = file.delete();
        boolean temDel = DownUtils.delDir(temp_folder.getAbsolutePath());
        return fileDel && temDel;
    }

}
