package com.abupdate.iot_libs.inter;

/**
 * 上报回调接口，若上报失败则写入本地数据库
 */
public interface IReportResultCallback {
    /**
     * report result
     *
     */
    void onReportSuccess();

    /**
     * report result
     *
     */
    void onReportFail();

    /**
     * report fail due to net exception
     */
    void onReportNetFail();
}
