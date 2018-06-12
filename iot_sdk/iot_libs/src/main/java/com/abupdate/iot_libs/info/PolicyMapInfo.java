package com.abupdate.iot_libs.info;

/**
 * 服务端配置的下载，升级策略类
 * 详情请参见fota_sdk集成指南
 */
public class PolicyMapInfo {

    public String key_name;
    public String key_value;
    public String key_message;

    @Override
    public String toString() {
        return "PolicyMapInfo{" + "\n" +
                "key_name='" + key_name + '\'' + "\n" +
                "key_value='" + key_value + '\'' + "\n" +
                "key_message='" + key_message + '\'' + "\n" +
                '}';
    }
}
