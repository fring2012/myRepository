package com.abupdate.iot_libs.inter;


import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.info.VersionInfo;

/**
 * 检测版本状态：<br/>
 * 1001：token非法;<br/>
 * 1002：项目不存在;<br/>
 * 1003：参数缺失;<br/>
 * 1004：当前为最新版本<br/>
 * 1005: 版本号在后台不存在<br/>
 * 1101：设备参数初始化失败,存在设备参数为空<br/>
 * 1102:服务器返回数据异常<br/>
 * 注：<br/>
 * 后台数据解析异常调用onInvalidDate()<br/>
 * 状态1000返回onCheckSuccess()；<br/>
 * 其余状态返回onCheckFail()<br/>
 */
public interface ICheckVersionCallback {
    /**
     * check version success
     *
     * @see OtaAgentPolicy
     */
    void onCheckSuccess(VersionInfo versionInfo);

    /**
     * check version fail
     *
     * @param status   is result tag
     */
    void onCheckFail(int status);


}
