package com.abupdate.mqtt_libs.connect;

import com.abupdate.mqtt_libs.mqttv3.IMqttActionListener;
import com.abupdate.mqtt_libs.mqttv3.MqttException;

/**
 * Created by fighter_lee on 2017/6/30.
 */

public interface Command {

    void execute(IMqttActionListener listener) throws MqttException;

}
