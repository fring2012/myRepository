package com.abupdate.sota.inter.multi;

import android.os.Handler;
import android.os.Looper;

import com.abupdate.sota.inter.CheckAllAppListener;
import com.abupdate.sota.inter.RealCall;
import com.abupdate.sota.network.RequestStack;
import com.abupdate.sota.network.base.BaseResponse;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class CheckAllAppTask {

    private Handler handler;

    /**
     * 回调到主线程
     * @return
     */
    public CheckAllAppTask setCallbackToMain() {
        handler = new Handler(Looper.getMainLooper());
        return this;
    }

    /**
     * 同步执行方法
     * @return
     */
    public CheckAllAppResponse executed() {
        return RequestStack.getInstance().checkAllAppFlow();
    }

    /**
     * 异步执行方法
     * @param listener
     */
    public void enqueue(CheckAllAppListener listener) {
        RealCall realCall = new RealCall();
        if (handler != null){
            realCall.setCallbcakToMain(handler);
        }
        RequestStack.getInstance().getDispatcher().enqueue(realCall.genCheckAllAppAsy(listener));
    }

    public static class CheckAllAppResponse extends BaseResponse<List<String>> {
    }
}
