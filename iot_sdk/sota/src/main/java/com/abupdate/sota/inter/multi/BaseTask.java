package com.abupdate.sota.inter.multi;

import android.os.Handler;
import android.os.Looper;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class BaseTask {

    private Handler handler;

    public BaseTask setCallbackToMain() {
        handler = new Handler(Looper.getMainLooper());
        return this;
    }

}
