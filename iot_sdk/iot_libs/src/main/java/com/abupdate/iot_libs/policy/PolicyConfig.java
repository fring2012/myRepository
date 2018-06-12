package com.abupdate.iot_libs.policy;

import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.inter.IParsePolicyListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raise.yang on 2016/07/18.
 */
public class PolicyConfig {
    public boolean wifi = true;
    public boolean storage_size = true;
    public boolean storage_path = true;
    public boolean battery = true;
    public boolean check_cycle = true;
    public boolean install_force = true;
    public boolean download_force = true;
    public boolean install_free_time = true;
    public boolean reboot_update_force = true;
    public boolean remind_cycle = true;

    public Map<String,IParsePolicyListener> parsePolicyListenerMap;

    private static PolicyConfig m_instance;

    private PolicyConfig() {
        parsePolicyListenerMap = new HashMap<>();
    }

    public static PolicyConfig getInstance() {
        if (m_instance == null) {
            synchronized (PolicyConfig.class) {
                if (m_instance == null) {
                    m_instance = new PolicyConfig();
                }
            }
        }
        return m_instance;
    }

    /**
     * 配置是否需要wifi环境，才能下载功能
     *
     * @param value
     * @return PolicyConfig
     */
    public PolicyConfig request_wifi(boolean value) {
        wifi = value;
        return this;
    }

    /**
     * 配置本地存储大小必须大于给定值才能下载功能
     *
     * @param value
     * @return PolicyConfig
     */
    public PolicyConfig request_storage_size(boolean value) {
        storage_size = value;
        return this;
    }

    /**
     * 配置下载包的下载路径
     *
     * @param value
     * @return PolicyConfig
     */
    public PolicyConfig request_storage_path(boolean value) {
        storage_path = value;
        return this;
    }

    /**
     * 配置最低电量要求功能
     *
     * @param value
     * @return PolicyConfig
     */
    public PolicyConfig request_battery(boolean value) {
        battery = value;
        return this;
    }

    /**
     * 配置循环检测版本周期
     *
     * @param value
     * @return PolicyConfig
     */
    public PolicyConfig request_check_cycle(boolean value) {
        check_cycle = value;
        return this;
    }

    /**
     * 强制升级
     * @param value
     * @return PolicyConfig
     */
    public PolicyConfig request_install_force(boolean value) {
        install_force = value;
        return this;
    }

    /**
     * 强制下载
     * @param value
     * @return
     */
    public PolicyConfig request_download_force(boolean value) {
        download_force = value;
        return this;
    }

    /**
     * 闲时升级
     * @param value
     * @return
     */
    public PolicyConfig request_install_free_time(boolean value) {
        install_free_time = value;
        return this;
    }

    /**
     * 重启强制升级
     * @param value
     * @return
     */
    public PolicyConfig request_reboot_update_force(boolean value) {
        reboot_update_force = value;
        return this;
    }

    /**
     * 提醒周期
     * @param value
     * @return
     */
    public PolicyConfig request_remind_cycle(boolean value) {
        remind_cycle = value;
        return this;
    }

    /**
     * 不采用默认的解析方式，自行解析策略
     * @param policyType
     * @return
     */
    public PolicyConfig parsePolicyYourself(OtaConstants.PolicyType policyType, IParsePolicyListener parsePolicyListener){
        parsePolicyListenerMap.put(policyType.getType(),parsePolicyListener);
        return this;
    }
}
