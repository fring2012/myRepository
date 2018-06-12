package com.abupdate.iot_libs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.iot_libs.MqttAgentPolicy;
import com.abupdate.iot_libs.inter.MessageListener;
import com.abupdate.mqtt_libs.connect.MqttManager;
import com.abupdate.trace.Trace;

/**
 * Created by dashy on 2018/4/24.
 */

public class MqttAlarmReceiver extends BroadcastReceiver {
    private final String TAG = "MqttAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Trace.d(TAG, "onReceiver (): " + action);
        dispatch_action(action);
    }

    private void dispatch_action(String action) {
        switch (action) {
            case MqttAgentPolicy.CONNECT_ACTION:
                MqttAgentPolicy.connect();
                break;
            case MqttAgentPolicy.DISCONNECT_ACTION:
                MqttAgentPolicy.disConnect();
                break;
            case MqttAgentPolicy.RESET_MQTT_ACTION:
                MessageListener.getInstance().resetMqttPolicy();
                break;
            default:
                break;
        }
    }
}
