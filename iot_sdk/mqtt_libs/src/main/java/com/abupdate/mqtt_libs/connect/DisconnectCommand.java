package com.abupdate.mqtt_libs.connect;

import com.abupdate.mqtt_libs.mqttv3.IMqttActionListener;
import com.abupdate.mqtt_libs.mqttv3.MqttException;

/**
 * Created by fighter_lee on 2017/6/30.
 */

public class DisconnectCommand implements Command {

    public DisconnectCommand setQuiesceTimeout(long quiesceTimeout) {
        this.quiesceTimeout = quiesceTimeout;
        return this;
    }

    private long quiesceTimeout;


    @Override
    public void execute(IMqttActionListener listener) throws MqttException {
        MqttManager.getInstance().getConnect().getClient()
                .disconnect(quiesceTimeout);
    }
}
