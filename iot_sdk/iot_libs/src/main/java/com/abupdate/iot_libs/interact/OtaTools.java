package com.abupdate.iot_libs.interact;

import android.text.TextUtils;

import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.constant.SDKConfig;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.ProductInfo;
import com.abupdate.iot_libs.info.RegisterInfo;
import com.abupdate.iot_libs.inter.ILoginCallback;
import com.abupdate.iot_libs.inter.ILogoutCallback;
import com.abupdate.iot_libs.inter.IReportDeviceStatusCallback;
import com.abupdate.iot_libs.inter.MessageListener;
import com.abupdate.iot_libs.inter.OtaListener;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.mqtt_libs.connect.ConnectCommand;
import com.abupdate.mqtt_libs.connect.DisconnectCommand;
import com.abupdate.mqtt_libs.connect.MqttManager;
import com.abupdate.mqtt_libs.connect.PubCommand;
import com.abupdate.mqtt_libs.connect.SubCommand;
import com.abupdate.mqtt_libs.mqtt_service.MqttAndroidClient;
import com.abupdate.mqtt_libs.mqttv3.IMqttActionListener;
import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Created by fighter_lee on 2017/5/11.
 */

public class OtaTools {

    private static OtaTools mqttTools;
    private static final String TAG = "OtaTools";
    public Map<Integer, String> reply = new HashMap<>();
    private MqttAndroidClient client;
    private MessageListener messageListener = MessageListener.getInstance();

    private OtaTools() {
    }

    public static OtaTools getInstance() {
        if (mqttTools == null) {
            synchronized (OtaTools.class) {
                if (mqttTools == null) {
                    mqttTools = new OtaTools();
                }
            }
        }
        return mqttTools;
    }

    public enum State {
        Null, Connecting, Connected, Login, Logout, Disconnecting, Disconnected
    }

    private State currentState = State.Null;

    public State getState() {
        return currentState;
    }

    public void setState(State state) {
        currentState = state;
    }

    /**
     * 验证响应应答码
     *
     * @param messageType
     * @param replyno
     * @return
     */
    public boolean verifyReplyByNo(int messageType, String replyno) {
        String s = reply.get(messageType);
        if (TextUtils.equals(replyno, s)) {
            reply.remove(messageType);
            return true;
        } else {
            return false;
        }

    }


    public void connect() {
        MqttManager mqttManager = MqttManager.getInstance();
        String username = ProductInfo.getInstance().productId + "/" + RegisterInfo.getInstance().deviceId;
        String password = RegisterInfo.getInstance().deviceSecret;
        ConnectCommand connectCommand = ConnectCommand.getInstance()
                .setClientId(DeviceInfo.getInstance().mid)
                .setServer(SDKConfig.MQTT_HOST)
                .setPort(SDKConfig.MQTT_SSL_PORT)
                .setTimeout(10)
                .setKeepAlive(100)
                .setLastWill(
                        createLastWill(),
                        String.format("product/%s/%s/logout", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId),
                        1,
                        false)
                .setCleanSession(false)
                .setSsl("/assets/adcom.bks", new String(SDKConfig.KEY))
                .setUserNameAndPassword(username, password);
        try {
            mqttManager.connect(connectCommand, OtaListener.getInstance().setAction(OtaListener.Action.CONNECT));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置遗愿消息
     *
     * @return
     */
    private String createLastWill() {
        JSONObject last_will = new JSONObject();
        JSONObject body = new JSONObject();
        try {
            body.put("type", 2);
            last_will.put("seqno", seqnoCreate());
            last_will.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return last_will.toString();
    }

    /**
     * 断开连接
     */
    public void disConnect(IMqttActionListener listener) {
        DisconnectCommand disconnectCommand = new DisconnectCommand();
        try {
            MqttManager.getInstance().disConnect(disconnectCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开连接
     */
    public void disConnect(IMqttActionListener listener, long quiesceTimeout) {
        DisconnectCommand disconnectCommand = new DisconnectCommand().setQuiesceTimeout(quiesceTimeout);
        try {
            MqttManager.getInstance().disConnect(disconnectCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置登录监听
     *
     * @param listener
     */
    public void setLoginListener(ILoginCallback listener) {
        if (null == messageListener) {
            Trace.e(TAG, "setLoginListener() messageListener is null");
            return;
        }
        messageListener.setLoginListener(listener);
    }

    /**
     * 设置登出监听
     *
     * @param listener
     */
    public void setLogoutListener(ILogoutCallback listener) {
        if (null == messageListener) {
            Trace.e(TAG, "setLoginListener() messageListener is null");
            return;
        }
        messageListener.setLogoutListener(listener);
    }

    /**
     * 设置上报设备状态监听
     *
     * @param callback
     */
    public void setReportDeviceStatusListener(IReportDeviceStatusCallback callback) {
        if (null == messageListener) {
            Trace.e(TAG, "setReportDeviceStatusListener() messageListener is null");
            return;
        }
        messageListener.setReportDeviceStatusListener(callback);
    }

    /**
     * @param message {
     *                "temperature":90,
     *                "light": "green"
     *                }
     */
    public void reportDeviceInfo(String message, IReportDeviceStatusCallback callback) {
        //product/{productId}/{deviceId}/shadow/update
        Trace.d(TAG, "reportDeviceInfo() start.");
        setReportDeviceStatusListener(callback);
        JSONObject jsonObject;
        String seqnoCreate = seqnoCreate();
        try {
            JSONObject reported = new JSONObject(message);
            jsonObject = new JSONObject();
            JSONObject body = new JSONObject();
            body.put("firewareVersion", DeviceInfo.getInstance().version);
            body.put("reported", reported);
            jsonObject.put("seqno", seqnoCreate);
            jsonObject.put("body", body);

        } catch (JSONException e) {
            e.printStackTrace();
            Trace.e(TAG, "reportDeviceInfo() JsonException:" + e.toString());
            Trace.e(TAG, "reportDeviceInfo() 请确认传入的消息符合json格式");
            return;
        }

        String topic = String.format("product/%s/%s/shadow/update"
                , ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        pub(topic, jsonObject.toString(), 1, false, OtaListener.getInstance().setAction(OtaListener.Action.PUB_REPORT_DEVICEINFO));
        reply.put(OtaConstants.MQTT_REPORT_DEVICE, seqnoCreate);
    }

    public void reportDeviceInfo(IReportDeviceStatusCallback callback) {
        reportDeviceInfo("{}", callback);
    }

    public void subReportDeviceInfo() {
        //product/{productId}/{deviceId}/shadow/update/response
        String topic = String.format("product/%s/%s/shadow/update/response", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        sub(topic, 1, OtaListener.getInstance().setAction(OtaListener.Action.SUB_REPORT_DEVICEINFO));
    }


    public void login(ILoginCallback listener) {
        Trace.d(TAG, "login() start");
        if (!DeviceInfo.getInstance().isValid()) {
            Trace.e(TAG, "login() device info is null");
            return;
        }

        setLoginListener(listener);

        String topic = String.format("product/%s/%s/login"
                , ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);

        JSONObject body = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        String current_time = String.valueOf(Utils.getSecondTime());
        String seqno = seqnoCreate();
        try {
            body.put("timestamp", current_time);
            body.put("sign", ProductInfo.getInstance().productId + current_time);

            jsonObject.put("body", body);
            jsonObject.put("seqno", seqno);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pub(topic, jsonObject.toString(), 1, false, OtaListener.getInstance().setAction(OtaListener.Action.PUB_LOGIN));
        reply.put(OtaConstants.MQTT_LOGIN, seqno);
    }

    /**
     * 设备登出
     *
     * @param initiative if true ,主动登出  if false,非主动登出
     */
    public void logout(boolean initiative, ILogoutCallback listener) {
        Trace.d(TAG, "logout() start.");

        setLogoutListener(listener);

        String topic = String.format("product/%s/%s/logout"
                , ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        JSONObject object = new JSONObject();
        String seqno = seqnoCreate();
        try {
            object.put("seqno", seqno);
            JSONObject body = new JSONObject();
            body.put("type", initiative ? 1 : 2);
            object.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pub(topic, object.toString(), 1, false, OtaListener.getInstance().setAction(OtaListener.Action.PUB_LOGOUT));
        if (initiative) {
            reply.put(OtaConstants.MQTT_LOGOUT, seqno);
        }
    }

    public void pub(String topic, String message, int qos, boolean retained, IMqttActionListener listener) {
        Trace.d(TAG, "pub() topic:"+topic+"\n"+"message:"+message);
        PubCommand pubCommand = new PubCommand().setTopic(topic).setQos(qos).setMessage(message).setRetained(retained);
        try {
            MqttManager.getInstance().pub(pubCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subLogin() {
        //product/{productId}/{deviceId}/login/response
//        Trace.d(TAG, "subLogin() ");
        String topic = String.format("product/%s/%s/login/response", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        sub(topic, 1, OtaListener.getInstance().setAction(OtaListener.Action.SUB_LOGIN));
    }

    public void subLogout() {
        //product/{productId}/{deviceId}/logout/response
//        Trace.d(TAG, "subLogout() ");
        String topic = String.format("product/%s/%s/logout/response", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        sub(topic, 1, OtaListener.getInstance().setAction(OtaListener.Action.SUB_LOGOUT));
    }


    public void subNotify() {
//        Trace.d(TAG, "subNotify() start.");

        String topic = String.format("product/%s/%s/notify", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        sub(topic, 1, OtaListener.getInstance().setAction(OtaListener.Action.SUB_NOTIFY));
    }

    public void sub(String topic, int qos, IMqttActionListener listener) {
//        Trace.d(TAG, "sub() start.topic:"+topic);
        SubCommand subCommand = new SubCommand().setQos(qos).setTopic(topic);
        try {
            MqttManager.getInstance().sub(subCommand, listener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每一个请求的是唯一的
     * 客户端向服务端发送：
     * “D_”+clientid+”_”+时间搓+3位随机数
     */
    public String seqnoCreate() {
        StringBuffer buffer = new StringBuffer();
        long timeMillis = Utils.getSecondTime();
        buffer.append("D_").append(RegisterInfo.getInstance().deviceId).append(timeMillis);
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            int nextInt = random.nextInt(10);
            buffer.append(nextInt);
        }
        return buffer.toString();
    }

}
