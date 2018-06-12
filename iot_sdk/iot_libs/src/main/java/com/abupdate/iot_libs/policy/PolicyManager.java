package com.abupdate.iot_libs.policy;

import android.content.Context;
import android.util.Pair;

import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.engine.UnitProvide;
import com.abupdate.iot_libs.info.PolicyMapInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.IParsePolicyListener;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.trace.Trace;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 策略信息管理类，主要接管对后台下发策略的判断与使用<br/>
 */
public class PolicyManager {

    private static final String TAG = "PolicyManager";

    public static PolicyManager INSTANCE = new PolicyManager();

    private PolicyManager() {
    }

    /**
     * 下载对wifi要求
     *
     * @return true if need wifi.
     */
    public boolean  is_request_wifi() {
        if (!PolicyConfig.getInstance().wifi) {
            return false;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_DOWNLOAD_WIFI);
        if (null != listener) {
            return listener.doParse();
        }
        PolicyMapInfo wifi_info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_DOWNLOAD_WIFI);
        if (wifi_info != null) {
            if ("required".equals(wifi_info.key_value)) {
                return true;
            }
            Trace.d(TAG, "is_request_wifi()");
        }
        return false;
    }

    /**
     * 下载对剩余空间的要求
     *
     * @param path 下载文件的父目录 绝对路径
     * @return true if has more space
     */
    public boolean is_storage_space_enough(String path) {
        if (!PolicyConfig.getInstance().storage_size) {
            return true;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_DOWNLOAD_STORAGE_SIZE);
        if (null != listener) {
            return listener.doParse();
        }
        PolicyMapInfo size_info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_DOWNLOAD_STORAGE_SIZE);
        if (size_info != null) {
            try {
                long free_size = UnitProvide.getInstance().getStorageSpace(path);
                Trace.i(TAG, String.format("is_storage_space_enough() need_size = %s,free_size = %s,path = %s",
                        size_info.key_value, free_size, path));
                if (Long.parseLong(size_info.key_value) <= free_size) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
        return true;
    }

    /**
     * 升级包存放路径
     *
     * @return 所配置的路径，未做任何逻辑处理。若没有配，则返回null
     */
    public String get_storage_path() {
        if (!PolicyConfig.getInstance().storage_path) {
            return null;
        }
        PolicyMapInfo path_info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_DOWNLOAD_STORAGE_PATH);
        if (path_info != null) {
            return path_info.key_value;
        }
        return null;
    }


    /**
     * 升级电量要求,应该在返回true的时候，去升级
     *
     * @return false 配置了电量要求字段，并且当前手机电量小于配置电量，否则 true
     */
    public boolean is_battery_enough(Context ctx) {
        if (!PolicyConfig.getInstance().battery) {
            return true;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_INSTALL_BATTERY);
        if (null != listener) {
            return listener.doParse();
        }
        PolicyMapInfo battery_info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_INSTALL_BATTERY);
        if (battery_info != null) {
            int batteryLevel = UnitProvide.getInstance().getBatteryLevel(ctx);
            Trace.d(TAG, "batteryLevel mobile = " + batteryLevel + "  config = " + battery_info.key_value);
            try {
                if (batteryLevel >= Integer.parseInt(battery_info.key_value)) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return true;
            } catch (Exception e) {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * 是否强制升级
     *
     * @return true if auto upgrade
     */
    public boolean is_force_install() {
        if (!PolicyConfig.getInstance().install_force) {
            return false;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_INSTALL_FORCE);
        if (null != listener) {
            return listener.doParse();
        }
        Pair<Date, Date> installFreeTime = getForceInstallTime();
        if (null != installFreeTime) {
            String nowTime = UnitProvide.getInstance().getCalendar().get(Calendar.HOUR_OF_DAY)+":"+UnitProvide.getInstance().getCalendar().get(Calendar.MINUTE);
            if (Utils.timeCompare(nowTime, installFreeTime.first, installFreeTime.second)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测版本周期。单位：分钟。最小值60.
     *
     * @return -1：没有配置该项，-2：该项配置错误
     */
    public int get_check_cycle() {
        if (!PolicyConfig.getInstance().check_cycle) {
            return -1;
        }
        PolicyMapInfo cycle_info = null;
        try {
            cycle_info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_CHECK_CYCLE);
        } catch (Exception e) {
            return -1;
        }
        if (cycle_info != null) {
            try {
                int cycle = Integer.parseInt(cycle_info.key_value);
                return cycle > 60 ? cycle : 60;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -2;
            }
        } else {
            return -1;
        }
    }

    /**
     * 获取提醒周期
     * @return
     */
    public int get_remind_cycle() {
        if (!PolicyConfig.getInstance().remind_cycle){
            return -1;
        }
        PolicyMapInfo cycle_info = null;
        try {
            cycle_info = VersionInfo.getInstance().policyHashMap.get(OtaConstants.KEY_REMIND_CYCLE);
        } catch (Exception e) {
            return -1;
        }
        if (cycle_info != null) {
            try {
                int cycle = Integer.parseInt(cycle_info.key_value);
                return cycle > 60 ? cycle : 60;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -2;
            }
        } else {
            return -1;
        }
    }

    /**
     * 是否强制下载
     * @return
     */
    public boolean isDownloadForce() {
        if (!PolicyConfig.getInstance().download_force){
            return false;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_DOWNLOAD_FORCE);
        if (null != listener) {
            return listener.doParse();
        }
        return getInfoByKey(OtaConstants.KEY_DOWNLOAD_FORCE);
    }

    /**
     * 是否重启强制升级
     * @return
     */
    public boolean isRebootUpdateForce() {
        if (!PolicyConfig.getInstance().reboot_update_force){
            return false;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_REBOOT_UPDATE_FORCE);
        if (null != listener) {
            return listener.doParse();
        }
        return getInfoByKey(OtaConstants.KEY_REBOOT_UPDATE_FORCE);
    }

    /**
     * 当前时间是否处于闲时安装的时间
     * @return
     */
    public boolean isGetToInstallFreeTime() {
        if (!PolicyConfig.getInstance().install_free_time){
            return false;
        }
        IParsePolicyListener listener = PolicyConfig.getInstance().parsePolicyListenerMap.get(OtaConstants.KEY_INSTALL_FREE_TIME);
        if (null != listener) {
            return listener.doParse();
        }

        Pair<Date, Date> installFreeTime = getInstallFreeTime();
        if (null != installFreeTime) {
            String nowTime = UnitProvide.getInstance().getCalendar().get(Calendar.HOUR_OF_DAY)+":"+UnitProvide.getInstance().getCalendar().get(Calendar.MINUTE);
            if (Utils.timeCompare(nowTime, installFreeTime.first, installFreeTime.second)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取闲时安装的时间区间
     * @return
     */
    public Pair<Date,Date> getInstallFreeTime() {
        if (!PolicyConfig.getInstance().install_free_time){
            return null;
        }
        return getTimeInterval(OtaConstants.IntervalTimePolicy.type_install_free_time);
    }


    /**
     * 获取强制升级时间区间
     * @return
     */
    public Pair<Date,Date> getForceInstallTime() {
        if (!PolicyConfig.getInstance().install_force){
            return null;
        }
        return getTimeInterval(OtaConstants.IntervalTimePolicy.type_install_force);
    }

    public Pair<Date,Date> getTimeInterval(OtaConstants.IntervalTimePolicy policyType){
        PolicyMapInfo info = VersionInfo.getInstance().policyHashMap.get(policyType.getType());
        if (null != info) {
            try {
                JSONObject obj = new JSONObject(info.key_value);
                String from_time = obj.getString("from");
                String to_time = obj.getString("to");
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                Date from_data = df.parse(from_time);
                Date to_date = df.parse(to_time);
                if (from_data.getTime() != to_date.getTime()) {
                    return new Pair<>(from_data, to_date);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean getInfoByKey(String key) {
        PolicyMapInfo info = null;
        try {
            info = VersionInfo.getInstance().policyHashMap.get(key);
        } catch (Exception e) {
            return false;
        }
        if (null != info) {
            if ("true".equals(info.key_value)) {
                return true;
            }
        }
        return false;
    }

    public String displayPolicy() {
        StringBuilder displayStr = new StringBuilder();
        if (VersionInfo.getInstance().policyHashMap != null) {
            for (Map.Entry<String, PolicyMapInfo> mapInfo : VersionInfo.getInstance().policyHashMap.entrySet()) {
                displayStr.append(mapInfo.getKey()).append(":").append(mapInfo.getValue()).append("\n");
            }
        } else {
            displayStr.append("null");
        }
        return displayStr.toString();
    }
}
