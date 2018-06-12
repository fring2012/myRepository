package com.abupdate.iot_libs.inter;

/**
 * Created by fighter_lee on 2017/5/25.
 */

public interface IRegisterListener {

    void onSuccess();

    void onFailed(int error);
}
