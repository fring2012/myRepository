package com.abupdate.sota.engine;

import com.abupdate.sota.engine.thread.NamedRunnable;
import com.abupdate.sota.utils.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class Dispatcher {
    private static final String TAG = "Dispatcher";
    private int maxRequests = 64;
    private Runnable idleCallback;

    private ExecutorService executorService;

    private final Deque<NamedRunnable> readyAsyncCalls = new ArrayDeque<>();

    private final Deque<NamedRunnable> runningAsyncCalls = new ArrayDeque<>();

    public Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Dispatcher() {
    }

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Utils.threadFactory("OkHttp Dispatcher", false));
        }
        return executorService;
    }

    public synchronized void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        this.maxRequests = maxRequests;
        promoteCalls();
    }

    public synchronized int getMaxRequests() {
        return maxRequests;
    }

    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    public synchronized void enqueue(NamedRunnable call) {
        if (runningAsyncCalls.size() < maxRequests) {
            runningAsyncCalls.add(call);
            executorService().execute(call);
        } else {
            readyAsyncCalls.add(call);
        }
    }

    private void promoteCalls() {
        if (runningAsyncCalls.size() >= maxRequests) {
            // Already running max capacity.
            return;
        }
        if (readyAsyncCalls.isEmpty()) {
            // No ready calls to promote.
            return;
        }

        for (Iterator<NamedRunnable> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            NamedRunnable call = i.next();
            i.remove();
            runningAsyncCalls.add(call);
            executorService().execute(call);
            if (runningAsyncCalls.size() >= maxRequests){
                // Reached max capacity.
                return;
            }
        }
    }

    public void finished(NamedRunnable call) {
        finished(runningAsyncCalls, call, true);
    }

    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call))
                throw new AssertionError("Call wasn't in-flight!");
            if (promoteCalls)
                promoteCalls();
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized int queuedCallsCount() {
        return readyAsyncCalls.size();
    }

    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size();
    }

}
