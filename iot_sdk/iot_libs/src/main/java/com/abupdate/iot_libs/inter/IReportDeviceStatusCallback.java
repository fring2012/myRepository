package com.abupdate.iot_libs.inter;

/**
 * Created by fighter_lee on 2017/6/21.
 */

public interface IReportDeviceStatusCallback {

    void onReportSuccess();

    void onReportFail(int error);
}
