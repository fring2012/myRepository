package com.abupdate.mqtt_libs.connect;

import com.abupdate.mqtt_libs.mqttv3.IMqttActionListener;
import com.abupdate.mqtt_libs.mqttv3.MqttException;

/**
 * Created by fighter_lee on 2017/6/30.
 */

public class PubCommand implements Command {

    private String mTopic;
    private int mQos;
    private String mMsg;
    private boolean mRetained;

    public PubCommand setTopic(String topic) {
        this.mTopic = topic;
        return this;
    }

    public PubCommand setQos(int qos) {
        this.mQos = qos;
        return this;
    }

    public PubCommand setMessage(String message) {
        this.mMsg = message;
        return this;
    }

    public PubCommand setRetained(boolean retained){
        this.mRetained = retained;
        return this;
    }

    @Override
    public void execute(IMqttActionListener listener) throws MqttException {
        MqttManager.getInstance().getConnect().getClient()
                .publish(mTopic, mMsg.getBytes(), mQos, mRetained, null, listener);
    }
}
