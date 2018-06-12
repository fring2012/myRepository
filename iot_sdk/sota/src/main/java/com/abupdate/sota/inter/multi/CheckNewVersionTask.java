package com.abupdate.sota.inter.multi;

import android.os.Handler;
import android.os.Looper;

import com.abupdate.sota.info.remote.NewAppInfo;
import com.abupdate.sota.inter.CheckNewVersionListener;
import com.abupdate.sota.inter.GenLocalApkInfoInter;
import com.abupdate.sota.inter.RealCall;
import com.abupdate.sota.network.RequestStack;
import com.abupdate.sota.network.base.BaseResponse;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class CheckNewVersionTask {

    Handler handler;
    private GenLocalApkInfoInter localApkInfoInter;

    /**
     * 回调到主线程
     *
     * @return
     */
    public CheckNewVersionTask setCallbackToMain() {
        handler = new Handler(Looper.getMainLooper());
        return this;
    }

    public CheckNewVersionResponse executed() {
        return RequestStack.getInstance().checkNewAppFlow(localApkInfoInter);
    }

    public void enqueue(CheckNewVersionListener listener) {
        RealCall realCall = new RealCall();
        if (handler != null) {
            realCall.setCallbcakToMain(handler);
        }
        RequestStack.getInstance().getDispatcher().enqueue(realCall.genCheckNewVersionAsy(listener));
    }

    public CheckNewVersionTask setGenLocalApkInfoInter(GenLocalApkInfoInter localApkInfoInter) {
        this.localApkInfoInter = localApkInfoInter;
        return this;
    }

    public static class CheckNewVersionResponse extends BaseResponse<List<NewAppInfo>> {

    }
}
