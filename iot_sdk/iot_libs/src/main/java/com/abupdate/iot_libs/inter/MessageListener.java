package com.abupdate.iot_libs.inter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.abupdate.iot_libs.MqttAgentPolicy;
import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.BroadcastConsts;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.engine.LogManager;
import com.abupdate.iot_libs.info.PushMessageInfo;
import com.abupdate.iot_libs.interact.OtaTools;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.JsonAnalyticsUtil;
import com.abupdate.iot_libs.utils.TimeUtils;
import com.abupdate.mqtt_libs.mqttv3.IMqttDeliveryToken;
import com.abupdate.mqtt_libs.mqttv3.MqttCallback;
import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.mqtt_libs.mqttv3.MqttMessage;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by fighter_lee on 2017/5/15.
 */

public class MessageListener implements MqttCallback {

    private static final String TAG = "MessageListener";
    private static final String LOGOUT_RESPONSE = "logout/response";
    private static final String LOGIN_RESPONSE = "login/response";
    private static final String REPORT_DEVICE_STATUS_RESPONSE = "update/response";
    private static final String PUSH_NOTIFY = "notify";
    public static final int DISCONNECTED_OK = -1;
    private String oldStatus, newStatus;
    private Context mCx;
    private ILoginCallback mILoginCallback;
    private ILogoutCallback mILogoutCallback;
    private IReportDeviceStatusCallback mIReportDeviceStatusCallback;
    //响应超时时间
    private static final int RESPONSE_TIMEOUT = 5 * 1000;
    //是否响应超时
    private boolean timeout = true;
    private static MessageListener messageListener;
    private ConnectionLostListener mConnectionLostListener;

    enum State {
        Null, Login, Logout, ReportDeviceInfo
    }

    private State currentState = State.Null;

    private MessageListener() {
        mCx = OtaAgentPolicy.sCx;
    }

    public static MessageListener getInstance() {
        if (messageListener == null) {
            synchronized (MessageListener.class) {
                if (messageListener == null) {
                    messageListener = new MessageListener();
                }
            }
        }
        return messageListener;
    }

    /**
     * 连接已断开
     *
     * @param var1
     */
    @Override
    public void connectionLost(Throwable var1) {
        Trace.d(TAG, "connectionLost() ");
        if (null != mConnectionLostListener) {
            int reasonCode;
            if (null != var1 && var1 instanceof MqttException) {
                reasonCode = ((MqttException) var1).getReasonCode();
            } else if (var1 == null) {
                reasonCode = DISCONNECTED_OK;
            } else {
                reasonCode = MqttException.REASON_CODE_UNEXPECTED_ERROR;
            }
            mConnectionLostListener.onConnectLost(reasonCode);
        }
    }

    @Override
    public void messageArrived(String var1, MqttMessage var2) throws Exception {
        String message = new String(var2.getPayload());
        Trace.d(TAG, "messageArrived() :" + var1);
        Trace.d(TAG, "messageArrived() :" + message);
        if (currentState == State.Null && !var1.endsWith(PUSH_NOTIFY)) {
            Trace.d(TAG, "messageArrived() state is null");
            return;
        }
        if (var1.endsWith(LOGIN_RESPONSE)) {

            String reply = getReply(message);
            if (OtaTools.getInstance().verifyReplyByNo(OtaConstants.MQTT_LOGIN, reply)) {
                timeout = false;
                int i = JsonAnalyticsUtil.responseJson(message);
                if (JsonAnalyticsUtil.responseSuccess(i)) {
                    //登录成功
                    Trace.d(TAG, "messageArrived() login response success!");
                    mILoginCallback.onLoginSuccess();
                    OtaTools.getInstance().setState(OtaTools.State.Login);
                    String status = JsonAnalyticsUtil.loginJson(message);
                    newStatus = status;
                    if (!TextUtils.isEmpty(status) && !newStatus.equals(oldStatus)) {
                        if (!TextUtils.isEmpty(oldStatus)) {
                            MqttAgentPolicy.cancelDelayMqttTask(mCx);
                        }
                        MqttAgentPolicy.setDelayMqttPolicy(mCx, newStatus);
                        oldStatus = newStatus;
                    }
                } else {
                    //登录失败
                    Trace.d(TAG, "messageArrived() login response failed!");
                    mILoginCallback.onLoginFail(i);
                }

            }

        } else if (var1.endsWith(LOGOUT_RESPONSE)) {

            String reply = getReply(message);
            if (OtaTools.getInstance().verifyReplyByNo(OtaConstants.MQTT_LOGOUT, reply)) {
                Trace.d(TAG, "messageArrived() logout response success!");
                timeout = false;
                int i = JsonAnalyticsUtil.responseJson(message);
                if (JsonAnalyticsUtil.responseSuccess(i)) {
                    //登出成功
                    mILogoutCallback.onLogoutSuccess();
                    OtaTools.getInstance().setState(OtaTools.State.Logout);
                } else {
                    //登出失败
                    mILogoutCallback.onLogoutFail(i);
                }

            }

        } else if (var1.endsWith(REPORT_DEVICE_STATUS_RESPONSE)) {
            //上报设备信息返回
            /*{
                "seqno":"{seqno}",
                    "replyno":"{replyno}",
                    "body":{
                "status": 1000, --1000：表示成功 其他表示失败
                "msg": "success"
            }
            }*/
            String reply = getReply(message);
            if (OtaTools.getInstance().verifyReplyByNo(OtaConstants.MQTT_REPORT_DEVICE, reply)) {
                Trace.d(TAG, "messageArrived() report device info success");
                timeout = false;
                int i = JsonAnalyticsUtil.responseJson(message);
                if (JsonAnalyticsUtil.responseSuccess(i)) {
                    //上报成功
                    mIReportDeviceStatusCallback.onReportSuccess();
                } else {
                    //上报失败
                    mIReportDeviceStatusCallback.onReportFail(i);
                }
            }

        } else if (var1.endsWith(PUSH_NOTIFY)) {
            //收到消息推送
            //             {
            //                "msgId":"12231112",
            //                        "msgType":1,
            //                        "content":"122"
            //            }
            JSONObject jsonObject = new JSONObject(message);

            if (jsonObject.has("body")) {
                JSONObject body = jsonObject.getJSONObject("body");

                if (body.has("msgId")) {
                    final String msgId = body.getString("msgId");
                    //当前为主线程
                    ReportManager.getInstance(mCx).savePushResponseData(new PushMessageInfo(msgId));
                    OtaService.startByAction(OtaService.ACTION_REPORT);
                }
                if (body.has("msgType")) {
                    int msgType = body.getInt("msgType");
                    Trace.d(TAG, "msgType (): " + msgType);
                    switch (msgType) {
                        case 1:
                            //1：OTA升级推送
                            if (body.has("content")) {
                                String content = body.getString("content");
                                Intent intent = new Intent();
                                intent.setAction(BroadcastConsts.ACTION_FOTA_NOTIFY);
                                intent.putExtra(BroadcastConsts.KEY_FOTA_NOTIFY, content);
                                mCx.sendBroadcast(intent, "permission.com.abupdate.fota.update");
                                //LocalBroadcastManager.getInstance(mCx).sendBroadcast(intent);
                            }
                            break;
                        case 2:
                            //2：升级错误日志上报
                            LogManager.getInstance().report();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private String getReply(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (jsonObject.has("replyno")) {
                String replyno = jsonObject.getString("replyno");
                return replyno;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken var1) {

    }

    /**
     * 设置登录监听
     *
     * @param listener
     */
    public void setLoginListener(ILoginCallback listener) {
        this.mILoginCallback = listener;
        setCurrentState(State.Login);
        timeStart();
    }

    /**
     * 设置登出监听
     *
     * @param listener
     */
    public void setLogoutListener(ILogoutCallback listener) {
        this.mILogoutCallback = listener;
        setCurrentState(State.Logout);
        timeStart();
    }

    /**
     * 设置上报设备状态监听
     *
     * @param listener
     */
    public void setReportDeviceStatusListener(IReportDeviceStatusCallback listener) {
        this.mIReportDeviceStatusCallback = listener;
        setCurrentState(State.ReportDeviceInfo);
        timeStart();
    }

    private void timeStart() {
//        Trace.d(TAG, "timeStart() ");
        synchronized (MessageListener.class) {
            TimeUtils.get().startTime(new TimeoutCallback() {
                @Override
                public void onTimeout() {
                    if (timeout == true) {
                        if (currentState == State.Login) {
                            mILoginCallback.onLoginTimeout();
                        }

                        if (currentState == State.Logout) {
                            mILogoutCallback.onLogoutTimeout();
                        }

                        if (currentState == State.ReportDeviceInfo) {
                            mIReportDeviceStatusCallback.onReportFail(MqttException.REASON_CODE_CLIENT_TIMEOUT);
                        }
                        setCurrentState(State.Null);
                    }
                }
            });
        }
    }

    private void setCurrentState(State currentState) {
        timeout = true;
        this.currentState = currentState;
    }

    public interface ConnectionLostListener {
        void onConnectLost(int error);
    }

    public void setConnectionLostListener(ConnectionLostListener listener) {
        this.mConnectionLostListener = listener;
    }

    public void resetMqttPolicy(){
        MqttAgentPolicy.setDelayMqttPolicy(mCx, newStatus);
    }
}
