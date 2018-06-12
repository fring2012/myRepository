package com.abupdate.iot_libs.engine;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author fighter_lee
 * @date 2017/11/17
 */
public class FutureTaskPool {

    private FutureTaskPool() {
    }

    private volatile static FutureTaskPool futureThreadPool;

    private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    public static FutureTaskPool getInstance() {
        if (futureThreadPool == null) {
            synchronized (FutureTaskPool.class) {
                futureThreadPool = new FutureTaskPool();
            }
        }
        return futureThreadPool;
    }

    public <T> FutureTask<T> executeTask(Callable<T> callable) {
        if (null == threadExecutor) {
            threadExecutor = Executors.newSingleThreadExecutor();
        }
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        threadExecutor.submit(futureTask);
        return futureTask;
    }
}
