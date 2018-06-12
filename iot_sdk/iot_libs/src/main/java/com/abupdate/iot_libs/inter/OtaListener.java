package com.abupdate.iot_libs.inter;


import com.abupdate.iot_libs.interact.OtaTools;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.mqtt_libs.connect.MqttManager;
import com.abupdate.mqtt_libs.mqttv3.IMqttActionListener;
import com.abupdate.mqtt_libs.mqttv3.IMqttToken;
import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.trace.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by fighter_lee on 2017/5/11.
 */

public class OtaListener implements IMqttActionListener, MessageListener.ConnectionLostListener {

    private static final String TAG = "OtaListener";
    private static OtaListener mInstance;
    Object mLock = new Object();
    List<Action> subAction = new ArrayList<>();
    Action[] allSubAction = {Action.SUB_LOGIN, Action.SUB_LOGOUT, Action.SUB_NOTIFY};
    //三次重试sub
    private int reSubCount = 3;

    @Override
    public void onConnectLost(int error) {
        List<IListener> listeners = getListener(Action.CONNECT);
        if (MessageListener.DISCONNECTED_OK == error) {
            if (listeners != null) {
                for (IListener listener : listeners) {
                    ((IStatusListener) listener).onDisconnected();
                }
            }
        } else {
            if (listeners != null) {
                for (IListener listener : listeners) {
                    ((IStatusListener) listener).onAbnormalDisconnected(error);
                }
            }
            if (MqttException.REASON_CODE_NOT_AUTHORIZED != error &&
                    MqttException.REASON_CODE_FAILED_AUTHENTICATION != error) {

                //异常断开连接时开启重试机制
                MqttManager.getInstance().keepConnect(1 * 1000 * 60 * 30, System.currentTimeMillis() + 1 * 1000 * 30);
            }
        }
        //状态设置为等待连接的状态
        setAction(Action.CONNECT);
        OtaTools.getInstance().setState(OtaTools.State.Disconnected);
    }

    public enum Action {

        CONNECT,

        DISCONNECT,

        PUB_REPORT_DEVICEINFO,

        PUB_LOGIN,

        PUB_LOGOUT,

        SUB_LOGIN,

        SUB_LOGOUT,

        SUB_NOTIFY,

        SUB_REPORT_DEVICEINFO
    }

    public static OtaListener getInstance() {
        if (mInstance == null) {
            synchronized (OtaListener.class) {
                if (mInstance == null) {
                    mInstance = new OtaListener();
                }
            }
        }
        return mInstance;
    }

    private Action action;

    public OtaListener setAction(Action action) {
        this.action = action;
        if (Action.CONNECT == action) {
            MessageListener.getInstance().setConnectionLostListener(this);
        }
        return this;
    }

    private OtaListener() {

    }

    private Map<Enum, List<IListener>> listeners = new HashMap<>();

    public void addListener(Action action, IListener listener) {
        if (!listeners.containsKey(action)) {
            listeners.put(action, new ArrayList<IListener>());
        }
        listeners.get(action).add(listener);
    }

    public void removeAllListener(Action action) {
        if (listeners.containsKey(action)) {
            listeners.get(action).clear();
        }
    }

    public boolean removeListener(Action action, IListener listener) {
        if (listeners.containsKey(action)) {
            boolean remove = listeners.get(action).remove(listener);
            return remove;
        }
        return false;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        switch (action) {
            case CONNECT:
                Trace.d(TAG, "onSuccess() socket connect success");
                connect();
                break;
            case DISCONNECT:
                Trace.d(TAG, "onSuccess() socket disconnect success");
                disConnect();
                break;
            case PUB_REPORT_DEVICEINFO:
                Trace.d(TAG, "onSuccess() pub report deviceinfo");
                break;

            case PUB_LOGIN:
                Trace.d(TAG, "onSuccess() pub login");
                break;

            case PUB_LOGOUT:
                Trace.d(TAG, "onSuccess() pub logout");
                break;

            case SUB_LOGIN:
                lockNotify();
                addSubAction(Action.SUB_LOGIN);
                break;

            case SUB_LOGOUT:
                lockNotify();
                addSubAction(Action.SUB_LOGOUT);
                break;

            case SUB_NOTIFY:
                lockNotify();
                addSubAction(Action.SUB_NOTIFY);
                break;

            case SUB_REPORT_DEVICEINFO:
                lockNotify();
                break;


        }

    }

    private void disConnect() {
    }

    private void connect() {
        //订阅接口，若失败，进行三次重新订阅，若三次后仍然失败，则回调失败，并断连
        OtaTools.getInstance().setState(OtaTools.State.Connected);
        List<IListener> listeners = getListener(Action.CONNECT);
        doLogin(listeners);

    }

    private void doLogin(final List<IListener> listeners) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                subTask();
                if (subAction.size() == allSubAction.length) {
                    //登录
                    OtaTools.getInstance().login(new ILoginCallback() {
                        @Override
                        public void onLoginSuccess() {
                            if (listeners != null) {
                                for (IListener listener : listeners) {
                                    ((IStatusListener) listener).onConnected();
                                }
                            }
                        }

                        @Override
                        public void onLoginFail(int error) {
                            OtaService.selfDisconnect();
                        }

                        @Override
                        public void onLoginTimeout() {
                            OtaService.selfDisconnect();
                        }
                    });

                } else {
                    //断连然后下次重连
                    OtaService.selfDisconnect();
                }

            }

        }).start();

    }

    private List<IListener> getListener(Action action) {
        List<IListener> iListener = listeners.get(action);
        return iListener;
    }


    @Override
    public void onFailure(IMqttToken token, Throwable exception) {
        switch (action) {
            case CONNECT:
                Trace.d(TAG, "onFailure() connect");
                connect(exception);
                break;
            case DISCONNECT:
                Trace.d(TAG, "onFailure() disconnect");
                disconnect(exception);
                break;
            case PUB_REPORT_DEVICEINFO:
                Trace.d(TAG, "onFailure() pub report deviceinfo");
                break;

            case PUB_LOGIN:
                Trace.d(TAG, "onFailure() pub login");
                break;

            case PUB_LOGOUT:
                Trace.d(TAG, "onFailure() pub logout");
                break;

            case SUB_LOGIN:
                Trace.d(TAG, "onFailure() sub login");
                lockNotify();
                break;

            case SUB_LOGOUT:
                Trace.d(TAG, "onFailure() sub logout");
                lockNotify();
                break;

            case SUB_NOTIFY:
                Trace.d(TAG, "onFailure() sub notify");
                lockNotify();
                break;

            case SUB_REPORT_DEVICEINFO:
                Trace.d(TAG, "onFailure() sub report deviceinfo");
                lockNotify();
                break;

        }
    }

    public void disconnect(Throwable exception) {
        OtaTools.getInstance().setState(OtaTools.State.Disconnected);
        List<IListener> listeners = getListener(Action.CONNECT);
        if (listeners != null) {
            for (IListener listener : listeners) {
                ((IStatusListener) listener).onError(getError(exception));
            }
        }
        if (null == exception) {
            return;
        }
        Trace.e(TAG, "disconnect() exception:" + exception.toString());
        exception.printStackTrace();
    }

    public void connect(Throwable exception) {
        OtaTools.getInstance().setState(OtaTools.State.Disconnected);
        List<IListener> listeners = getListener(Action.CONNECT);
        if (listeners != null) {
            for (IListener listener : listeners) {
                ((IStatusListener) listener).onError(getError(exception));
            }
        }
        if (null != exception) {
            exception.printStackTrace();
            Trace.e(TAG, "connect() " + exception.toString());
        }
        //连接出错时进行连接重试(排除回调的已连接错误和正在连接错误以及)
        if (MqttManager.getInstance().isConneect() ||
                OtaTools.getInstance().getState() == OtaTools.State.Connecting) {
            return;
        }
        if (MqttException.REASON_CODE_NOT_AUTHORIZED != getError(exception) &&
                MqttException.REASON_CODE_FAILED_AUTHENTICATION != getError(exception)) {
            MqttManager.getInstance().keepConnect(1 * 1000 * 60 * 30, System.currentTimeMillis() + 1 * 1000 * 30);
        }
    }

    /**
     * 将Error状态回调
     *
     * @param exception
     */
    private int getError(Throwable exception) {
        if (exception instanceof MqttException) {
            MqttException mqttExc = (MqttException) exception;
            return mqttExc.getReasonCode();
        } else {
            return MqttException.REASON_CODE_UNEXPECTED_ERROR;
        }
    }

    private void lockNotify() {
        if (mLock != null) {
            synchronized (mLock) {
                mLock.notify();
            }
        }
    }

    private void threadLock() {
        if (mLock != null) {
            synchronized (mLock) {
                try {
                    mLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addSubAction(Action action) {
        if (null == subAction) {
            return;
        }
        if (!subAction.contains(action)) {
            subAction.add(action);
        }
    }

    private void subTask() {
        //确保订阅
        while (reSubCount > 0 && subAction.size() < allSubAction.length) {
            reSubCount--;
            if (null == allSubAction || allSubAction.length == 0) {
                return;
            }
            for (int i = 0; i < allSubAction.length; i++) {
                if (!subAction.contains(allSubAction[i])) {
                    if (allSubAction[i] == Action.SUB_LOGIN) {
                        OtaTools.getInstance().subLogin();
                        threadLock();
                    } else if (allSubAction[i] == Action.SUB_LOGOUT) {
                        OtaTools.getInstance().subLogout();
                        threadLock();
                    } else if (allSubAction[i] == Action.SUB_NOTIFY) {
                        OtaTools.getInstance().subNotify();
                        threadLock();
                    }
                }
            }
        }
        //还原3次
        reSubCount = 3;
    }
}
