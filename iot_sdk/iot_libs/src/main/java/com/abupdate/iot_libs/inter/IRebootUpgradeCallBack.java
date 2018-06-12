package com.abupdate.iot_libs.inter;

/**
 * 升级错误状态码
 * 升级文件不存在或者文件长度为0 7004
 */
public interface IRebootUpgradeCallBack {

    /**
     * 升级条件
     * @return
     */
    boolean rebootConditionPrepare();

    /**
     * reboot前错误码回调
     * @param error
     */
    void onError(int error);
}
