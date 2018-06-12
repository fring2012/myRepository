package com.abupdate.sota.inter;

import android.os.Handler;

import com.abupdate.sota.engine.thread.NamedRunnable;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.info.remote.ReportInfo;
import com.abupdate.sota.inter.multi.CheckAllAppTask;
import com.abupdate.sota.inter.multi.CheckNewVersionTask;
import com.abupdate.sota.network.RequestStack;
import com.abupdate.sota.network.base.CommonResponse;
import com.abupdate.trace.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class RealCall {
    private static final String TAG = "RealCall";
    private Handler handler;

    public RealCall() {
    }

    public RealCall setCallbcakToMain(Handler handler) {
        this.handler = handler;
        return this;
    }

    public CheckNewVersionAsy genCheckNewVersionAsy(CheckNewVersionListener listener) {
        return new CheckNewVersionAsy(listener);
    }

    public CheckAllAppAsy genCheckAllAppAsy(CheckAllAppListener listener) {
        return new CheckAllAppAsy(listener);
    }

    public ReportAsy genReportAsy() {
        return new ReportAsy();
    }

    public RegisterAsy genRegisterAsy() {
        return new RegisterAsy();
    }

    public class CheckNewVersionAsy extends NamedRunnable {
        private CheckNewVersionListener responseCallback;
        private GenLocalApkInfoInter genLocalApkInfoInter;

        public CheckNewVersionAsy(CheckNewVersionListener listener) {
            super("SOTA %s", "check new version");
            this.responseCallback = listener;
        }

        RealCall get() {
            return RealCall.this;
        }

        CheckNewVersionAsy setGenInter(GenLocalApkInfoInter inter) {
            this.genLocalApkInfoInter = inter;
            return this;
        }

        @Override
        protected void execute() {
            final CheckNewVersionTask.CheckNewVersionResponse response = RequestStack.getInstance().checkNewAppFlow(genLocalApkInfoInter);
            if (response.isOK) {
                if (responseCallback == null) {
                    Trace.e(TAG, "listener is null");
                    return;
                }
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onSuccess(response.getResult());
                        }
                    });
                } else {
                    responseCallback.onSuccess(response.getResult());
                }
            } else if (response.isNetError()) {
                netErrorCallback(responseCallback);
            } else {
                failResultCallback(responseCallback, response.errorCode);
            }
        }
    }

    public class RegisterAsy extends NamedRunnable {

        public RegisterAsy() {
            super("SOTA %s", "Register Task");
        }

        @Override
        protected void execute() {
            CommonResponse<SotaRegisterInfo> registerResponse = RequestStack.getInstance().doRegister();
        }
    }

    public class CheckAllAppAsy extends NamedRunnable {
        private CheckAllAppListener responseCallback;

        public CheckAllAppAsy(CheckAllAppListener listener) {
            super("SOTA %s", "CheckAllApp");
            this.responseCallback = listener;
        }

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            final CheckAllAppTask.CheckAllAppResponse response = RequestStack.getInstance().checkAllAppFlow();
            if (response.isOK) {
                if (responseCallback == null) {
                    Trace.e(TAG, "listener is null");
                    return;
                }
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            responseCallback.onSuccess(response.getResult());
                        }
                    });
                } else {
                    responseCallback.onSuccess(response.getResult());
                }
            } else if (response.isNetError()) {
                netErrorCallback(responseCallback);
            } else {
                failResultCallback(responseCallback, response.errorCode);
            }
        }
    }

    public class ReportAsy extends NamedRunnable {
        private List<ReportInfo> infos = new ArrayList<>();

        public ReportAsy() {
            super("SOTA %s", "CheckAllApp");
        }

        public ReportAsy addInfo(ReportInfo info) {
            infos.add(info);
            return this;
        }

        public ReportAsy addInfos(List<ReportInfo> infos) {
            if (null != infos) {
                infos.addAll(infos);
            }
            return this;
        }

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            RequestStack.getInstance().report(infos);
        }
    }

    public void netErrorCallback(final BaseListener listener) {
        if (listener == null) {
            Trace.e(TAG, "listener is null");
            return;
        }
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onNetError();
                }
            });
        } else {
            listener.onNetError();
        }

    }

    public void failResultCallback(final BaseListener listener, final int code) {
        if (listener == null) {
            Trace.e(TAG, "listener is null");
            return;
        }
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailed(code);
                }
            });
        } else {
            listener.onFailed(code);
        }
    }

}
