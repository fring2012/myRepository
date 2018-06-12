package com.abupdate.iot_libs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.method.SingleLineTransformationMethod;

import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.inter.IReportDeviceStatusCallback;
import com.abupdate.iot_libs.inter.IStatusListener;
import com.abupdate.iot_libs.inter.MessageListener;
import com.abupdate.iot_libs.inter.OtaListener;
import com.abupdate.iot_libs.interact.OtaTools;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.iot_libs.utils.TimeUtils;
import com.abupdate.mqtt_libs.connect.MqttManager;
import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.trace.Trace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by fighter_lee on 2017/5/23.
 */

public class MqttAgentPolicy {

    static AlarmManager alarmManager;
    private static final String TAG = "MqttAgentPolicy";
    private static final String CONNECT = "connect";
    private static final String DISCONNECT = "disconnect";
    public static final String CONNECT_ACTION = "com.abupdate.mqtt.action_connect";
    public static final String DISCONNECT_ACTION = "com.abupdate.mqtt.action_disconnect";
    public static final String RESET_MQTT_ACTION = "com.abupdate.mqtt.action_reset_mqtt";
    public static final String CONFIG_MQTT_CONNECT = "config_mqtt_connect";
    private static final long ONE_HOUR_IN_MILLS = 60 * 60 * 1000;
    private static final long ONE_DAY_IN_MILLS = 24 * 60 * 60 * 1000;
    private static int continuousTime = 0;
    private static final int MQTT_POLICY_RESET_CODE = 25;
    private static ArrayList<Integer> oldStatusList;

    public static void initMqtt() {
        MqttManager.getInstance().setContext(OtaAgentPolicy.sCx);
        MqttManager.getInstance().registerMessageListener(MessageListener.getInstance());
        MqttManager.getInstance().setTraceEnable(false);
        if ("ture".equals(DeviceInfo.getInstance().requestPush)) {
            connect();
        }
    }

    /**
     * 注册长连接状态回调
     *
     * @param listener
     */
    public static void registerStatusListener(IStatusListener listener) {
        OtaListener.getInstance().addListener(OtaListener.Action.CONNECT, listener);
    }

    /**
     * 注销长连接状态回调
     *
     * @param listener
     * @return
     */
    public static boolean unregisterStatusListener(IStatusListener listener) {
        return OtaListener.getInstance().removeListener(OtaListener.Action.CONNECT, listener);
    }

    /**
     * 断开长连接
     */
    public static void disConnect() {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "disconnect", OtaConstants.SINGLE_LINE);
        SPFTool.putBoolean(CONFIG_MQTT_CONNECT, false);
        OtaService.selfDisconnect();
    }

    /**
     * 关闭断连重试机制
     */
    public static void stopKeepConnect() {
        Trace.d("MqttAgentPolicy", "stopKeepConnect() start.");
        if (MqttManager.getInstance().isKeepConnect()) {
            OtaService.startByAction(OtaService.ACTION_DISCONNECT);
        } else {
            Trace.d(TAG, "stopKeepConnect() is not config keep connect!");
        }
    }

    /**
     * 连接
     */
    public static void connect() {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "connect", OtaConstants.SINGLE_LINE);
        SPFTool.putBoolean(CONFIG_MQTT_CONNECT, true);
        if (MqttManager.getInstance().isConneect()) {
            Trace.d("MqttAgentPolicy", "connect() is connected");
            OtaListener.getInstance().connect(new MqttException(new Throwable("is connected")));
            return;
        }
        if (OtaTools.getInstance().getState() == OtaTools.State.Connecting) {
            Trace.d("MqttAgentPolicy", "connect() is connecting");
            OtaListener.getInstance().connect(new MqttException(new Throwable("is connecting")));
            return;
        }
        //参数条件
        if (!DeviceInfo.getInstance().isValid()) {
            Trace.d(TAG, "connect() params is not valid");
            return;
        }
        OtaService.startByAction(OtaService.ACTION_CONNECT);
    }

    /**
     * 设定定时连接Mqtt的策略，设定之前需要取消旧的策略（若存在的话）
     *
     * @param context
     * @param status  服务器返回的最新的status，有如下两种格式：
     *                ①定时长连接，{"status":"1016"}
     *                ②定时间段长连接，{"status":"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24"}
     */
    public static void setDelayMqttPolicy(Context context, String status) {
        oldStatusList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (status.length() == 4) {
            //status长度为4，说明是定时长连接
            long from, to;
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(status.substring(0, 2)));
            from = calendar.getTime().getTime() + 10000;
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(status.substring(2, 4)));
            to = calendar.getTime().getTime() - 10000;
            if (from < System.currentTimeMillis()) {
                from += ONE_DAY_IN_MILLS;
            }
            setDelayMqttTask(context, Integer.parseInt(status.substring(0, 2)), from, CONNECT);
            if (to < System.currentTimeMillis()) {
                to += ONE_DAY_IN_MILLS;
            }
            setDelayMqttTask(context, Integer.parseInt(status.substring(2, 4)), to, DISCONNECT);

            //重置Mqtt连接策略
            resetDelayMqttTask(context);
        } else {
            //长度不为4，说明status是定时间段长连接
            String statusList[] = status.split(",");
            long[] fromList = new long[statusList.length];
            long[] toList = new long[statusList.length];
            //遍历数组，获取每一个定时间段
            for (int i = 0; i < statusList.length; i++) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(statusList[i]));
                //起始时间=终止时间减一小时，加10秒避免冲突
                fromList[i] = calendar.getTimeInMillis() - ONE_HOUR_IN_MILLS + 10000;
                toList[i] = calendar.getTimeInMillis() - 10000;
                Trace.d(TAG, "Set Delay Mqtt Task:  " + (Integer.parseInt(statusList[i]) - 1) + OtaConstants.SINGLE_LINE + statusList[i]);
                /**
                 *  定时间段与当前时间进行比较，分为三种情况：
                 *  ①：起止时间都大于当前时间，则可直接设置定时连接断开任务（需判断是否连续）。
                 *  ②：起始时间小于当前时间，终止时间大于当前时间，则直接将起始时间加一天，设置定时连接断开任务（断开时间需要判断是否连续）。
                 *  ③：起止时间都小于当前时间，则可直接将起止时间加一天设置定时连接断开任务（需判断是否连续）。
                 */
                if (fromList[i] > System.currentTimeMillis()) {
                    setDelayMqttTask(context, Integer.parseInt(statusList[i]), fromList[i], CONNECT);
                    //判断i时间段与i+1时间段是否连续,非等于1，则不连续，可直接终止触发时机
                    for (int ii = i + 1; ii < statusList.length; ii++) {
                        if (Integer.parseInt(statusList[ii]) - Integer.parseInt(statusList[ii - 1]) == 1) {
                            continuousTime++;
                        } else {
                            break;
                        }
                    }
                    toList[i] += continuousTime * ONE_HOUR_IN_MILLS;
                    setDelayMqttTask(context, Integer.parseInt(statusList[i]), toList[i], DISCONNECT);
                    if (continuousTime > 0) {
                        i += continuousTime;
                        continuousTime = 0;
                    }
                    continue;
                }
                if (fromList[i] < System.currentTimeMillis() && toList[i] > System.currentTimeMillis()) {
                    setDelayMqttTask(context, Integer.parseInt(statusList[i]), fromList[i] + ONE_DAY_IN_MILLS, CONNECT);
                    for (int ii = i + 1; ii < statusList.length; ii++) {
                        if (Integer.parseInt(statusList[ii]) - Integer.parseInt(statusList[ii - 1]) == 1) {
                            continuousTime++;
                        } else {
                            break;
                        }
                    }
                    toList[i] += continuousTime * ONE_HOUR_IN_MILLS;
                    setDelayMqttTask(context, Integer.parseInt(statusList[i]), toList[i], DISCONNECT);
                    if (continuousTime > 0) {
                        i += continuousTime;
                        continuousTime = 0;
                    }
                    continue;
                }
                if (fromList[i] < System.currentTimeMillis() && toList[i] < System.currentTimeMillis()) {
                    setDelayMqttTask(context, Integer.parseInt(statusList[i]), fromList[i] + ONE_DAY_IN_MILLS, CONNECT);
                    for (int ii = i + 1; ii < statusList.length; ii++) {
                        if (Integer.parseInt(statusList[ii]) - Integer.parseInt(statusList[ii - 1]) == 1) {
                            continuousTime++;
                        } else {
                            break;
                        }
                    }
                    toList[i] += continuousTime * ONE_HOUR_IN_MILLS + ONE_DAY_IN_MILLS;
                    setDelayMqttTask(context, Integer.parseInt(statusList[i]), toList[i], DISCONNECT);
                    if (continuousTime > 0) {
                        i += continuousTime;
                        continuousTime = 0;
                    }
                    continue;
                }
            }
            //重置Mqtt连接策略
            resetDelayMqttTask(context);
        }
    }

    public static void reportDeviceStatus(String message, IReportDeviceStatusCallback callback) {
        if (!MqttManager.getInstance().isConneect()) {
            Trace.d("MqttAgentPolicy", "reportDeviceStatus() is disconnected");
            return;
        }
        if (OtaTools.getInstance().getState() != OtaTools.State.Login) {
            Trace.e("MqttAgentPolicy", "reportDeviceStatus() device is off line");
            return;
        }
        OtaTools.getInstance().reportDeviceInfo(message, callback);
    }

    /**
     * 是否在线
     *
     * @return
     */
    public static boolean isConnected() {
        return MqttManager.getInstance().isConneect();
    }

    /**
     * 设定Mqtt的定时连接和断开的任务
     *
     * @param context
     * @param requestCode 通常是传入的定时间段长连接的终止时间
     * @param triggerTime 定时任务触发时间
     * @param intent      定时任务类型 CONNECT 或者 DISCONNECT
     */
    private static void setDelayMqttTask(Context context, int requestCode, long triggerTime, String intent) {
        oldStatusList.add(requestCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (intent.equals(CONNECT)) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, PendingIntent.getBroadcast(context, requestCode, new Intent(CONNECT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, PendingIntent.getBroadcast(context, requestCode, new Intent(DISCONNECT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
            }
        } else {
            if (intent.equals(CONNECT)) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, PendingIntent.getBroadcast(context, requestCode, new Intent(CONNECT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, PendingIntent.getBroadcast(context, requestCode, new Intent(DISCONNECT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
            }
        }
        Trace.d(TAG, "setDelayMqttTask()： " + intent + ", " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(triggerTime) + "， requestCode: " + requestCode);
    }

    /**
     * 取消单个设定的Mqtt定时策略
     *
     * @param context
     * @param requestCode 通常是设定的定时时间段的终止时间
     * @param intent      CONNECT 或者 DISCONNECT
     */
    private static void cancelDelayMqttTask(Context context, int requestCode, String intent) {
        if (intent.equals(CONNECT)) {
            alarmManager.cancel(PendingIntent.getBroadcast(context, requestCode, new Intent(CONNECT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
            Trace.d(TAG, "cancel Delay Mqtt Connect: " + requestCode);
        } else {
            alarmManager.cancel(PendingIntent.getBroadcast(context, requestCode, new Intent(DISCONNECT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
            Trace.d(TAG, "cancel Delay Mqtt DisConnect: " + requestCode);
        }
    }

    /**
     * 取消全部设定的Mqtt定时策略
     *
     * @param context
     */
    public static void cancelDelayMqttTask(Context context) {
        if (oldStatusList != null && oldStatusList.size() != 0) {
            for (Integer i : oldStatusList) {
                cancelDelayMqttTask(context, i, CONNECT);
                cancelDelayMqttTask(context, i, DISCONNECT);
            }
        }
    }

    /**
     * 重置mqtt定时连接策略（将定时策略恢复到最近一次从服务器获取的status）
     *
     * @param context
     */
    public static void resetDelayMqttTask(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        Long triggerTime = calendar.getTimeInMillis() + 30 * 1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, PendingIntent.getBroadcast(context, MQTT_POLICY_RESET_CODE, new Intent(RESET_MQTT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, PendingIntent.getBroadcast(context, MQTT_POLICY_RESET_CODE, new Intent(RESET_MQTT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT));
        }
        Trace.d(TAG, "setDelayMqttTask()： " + "reset mqtt action" + ", " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(triggerTime) + "， requestCode: " + MQTT_POLICY_RESET_CODE);
    }
}
