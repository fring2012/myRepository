package com.abupdate.iot_libs.inter;

/**
 * Created by fighter_lee on 2017/6/19.
 */

public interface ILogoutCallback {

    void onLogoutSuccess();

    void onLogoutFail(int error);

    void onLogoutTimeout();

}
