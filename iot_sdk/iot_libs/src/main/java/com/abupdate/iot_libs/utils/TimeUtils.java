package com.abupdate.iot_libs.utils;

import android.os.Handler;
import android.os.Looper;

import com.abupdate.iot_libs.inter.TimeoutCallback;

/**
 * Created by fighter_lee on 2017/6/26.
 */

public class TimeUtils {

    private static final long DELAY_TIME = 5 * 1000;
    Handler handler;
    private TimeoutCallback timeoutCallback;
    private static TimeUtils timeUtils;

    public static TimeUtils get() {
        if (timeUtils == null) {
            timeUtils = new TimeUtils();
        }
        return timeUtils;
    }

    public TimeUtils() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void startTime(TimeoutCallback timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
        stopTime();
        handler.postDelayed(runnable, DELAY_TIME);
    }

    public void stopTime() {
        handler.removeCallbacks(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            timeoutCallback.onTimeout();
        }
    };
}
