package com.abupdate.iot_libs.info;

/**
 * Created by fighter_lee on 2017/9/18.
 */

public class PushMessageInfo {

    public String msgId;

    public int _id;

    public PushMessageInfo() {

    }

    public PushMessageInfo(String msgId) {
        this.msgId = msgId;
    }

    @Override
    public String toString() {
        return "PushMessageInfo{" + "\n" +
                "msgId='" + msgId + '\'' + "\n" +
                '}';
    }
}
