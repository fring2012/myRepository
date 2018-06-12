package com.abupdate.http_libs.inter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.abupdate.http_libs.exception.HttpException;
import com.abupdate.http_libs.request.base.Request;
import com.abupdate.http_libs.response.Response;

/**
 * Created by fighter_lee on 2017/7/19.
 */

public abstract class HttpListener {

    private static final String TAG = HttpListener.class.getSimpleName();

    private static final int M_START = 1;
    private static final int M_SUCCESS = 2;
    private static final int M_FAILURE = 3;
    private static final int M_RETRY = 4;
    private static final int M_REDIRECT = 5;
    private static final int M_END = 6;

    private HttpHandler handler;
    private boolean runOnUiThread = true;
    private HttpListener linkedListener;
    private long delayMillis;
    private boolean disableListener = false;

    /**
     * default run on UI thread
     */
    public HttpListener() {
        this(true);
    }

    /**
     * 设置延时回调（模拟网络很慢）
     *
     * @param delayMillis
     */
    public HttpListener(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public HttpListener(boolean runOnUiThread) {
        setRunOnUiThread(runOnUiThread);
    }


    public final HttpListener getListener() {
        return linkedListener;
    }

    public final boolean isRunOnUiThread() {
        return runOnUiThread;
    }

    public final HttpListener setRunOnUiThread(boolean runOnUiThread) {
        this.runOnUiThread = runOnUiThread;
        if (runOnUiThread) {
            handler = new HttpHandler(Looper.getMainLooper());
        } else {
            handler = null;
        }
        return this;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public HttpListener setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    /**
     * note: hold an implicit reference to outter class
     */
    private class HttpHandler extends Handler {
        private HttpHandler(Looper looper) {
            super(looper);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            if (disableListener) {
                return;
            }
            Object[] data;
            switch (msg.what) {
                case M_START:
                    onStart((Request) msg.obj);
                    break;
                case M_SUCCESS:
                    data = (Object[]) msg.obj;
                    onSuccess((String) data[0], (Response) data[1]);
                    break;
                case M_FAILURE:
                    data = (Object[]) msg.obj;
                    onFailure((HttpException) data[0], (Response) data[1]);
                    break;
                case M_RETRY:
                    data = (Object[]) msg.obj;
                    onRetry((Request) data[0], (Integer) data[1], (Integer) data[2]);
                    break;
                case M_REDIRECT:
                    data = (Object[]) msg.obj;
                    onRedirect((Request) data[0], (Integer) data[1], (Integer) data[2]);
                    break;
                case M_END:
                    onEnd((Response) msg.obj);
                    break;
            }
        }
    }

    //____________lite called method ____________
    public final void notifyCallStart(Request req) {
        if (disableListener) {
            return;
        }
        if (runOnUiThread) {
            Message msg = handler.obtainMessage(M_START);
            msg.obj = req;
            handler.sendMessage(msg);
        } else {
            onStart(req);
        }
        if (linkedListener != null) {
            linkedListener.notifyCallStart(req);
        }
    }

    public final void notifyCallSuccess(String data, Response response) {
        delayOrNot();
        if (disableListener) {
            return;
        }
        if (runOnUiThread) {
            Message msg = handler.obtainMessage(M_SUCCESS);
            msg.obj = new Object[]{data, response};
            handler.sendMessage(msg);
        } else {
            onSuccess(data, response);
        }
        if (linkedListener != null) {
            linkedListener.notifyCallSuccess(data, response);
        }
    }

    public final void notifyCallFailure(HttpException e, Response response) {
        delayOrNot();
        if (disableListener) {
            return;
        }
        if (runOnUiThread) {
            Message msg = handler.obtainMessage(M_FAILURE);
            msg.obj = new Object[]{e, response};
            handler.sendMessage(msg);
        } else {
            onFailure(e, response);
        }
        if (linkedListener != null) {
            linkedListener.notifyCallFailure(e, response);
        }
    }


    public final void notifyCallRetry(Request req, int max, int times) {
        if (disableListener) {
            return;
        }
        if (runOnUiThread) {
            Message msg = handler.obtainMessage(M_RETRY);
            msg.obj = new Object[]{req, max, times};
            handler.sendMessage(msg);
        } else {
            onRetry(req, max, times);
        }
        if (linkedListener != null) {
            linkedListener.notifyCallRetry(req, max, times);
        }
    }

    public final void notifyCallRedirect(Request req, int max, int times) {
        if (disableListener) {
            return;
        }
        if (runOnUiThread) {
            Message msg = handler.obtainMessage(M_REDIRECT);
            msg.obj = new Object[]{req, max, times};
            handler.sendMessage(msg);
        } else {
            onRedirect(req, max, times);
        }
        if (linkedListener != null) {
            linkedListener.notifyCallRedirect(req, max, times);
        }
    }

    public final void notifyCallEnd(Response response) {
        if (disableListener) {
            return;
        }
        if (runOnUiThread) {
            Message msg = handler.obtainMessage(M_END);
            msg.obj = response;
            handler.sendMessage(msg);
        } else {
            onEnd(response);
        }
        if (linkedListener != null) {
            linkedListener.notifyCallEnd(response);
        }
    }

    private boolean delayOrNot() {
        if (delayMillis > 0) {
            try {
                Thread.sleep(delayMillis);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * true,不会回调
     *
     * @param able
     * @return
     */
    public void disableListener(boolean able) {
        this.disableListener = able;
    }

    public void onStart(Request request) {
    }

    public void onSuccess(String data, Response response) {
    }

    public void onFailure(HttpException e, Response response) {
    }

    public void onRetry(Request request, int max, int times) {
    }

    public void onRedirect(Request request, int max, int times) {
    }

    public void onEnd(Response response) {
    }

}
