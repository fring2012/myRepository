package com.abupdate.iot_libs.inter;

/**
 * Created by fighter_lee on 2017/7/2.
 */

public interface IStatusListener extends IListener{

    void onConnected();

    void onDisconnected();

    /**
     * 异常断连
     * @param error
     */
    void onAbnormalDisconnected(int error);

    void onError(int error);
}
