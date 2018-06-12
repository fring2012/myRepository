package com.abupdate.iot_libs;

import android.content.Context;

import com.abupdate.iot_libs.info.CustomDeviceInfo;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.ProductInfo;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.utils.BeanUtils;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;

import org.json.JSONObject;

/**
 * @author fighter_lee
 * @date 2017/12/25
 * @describe 间接升级方案  如：手机检测版本然后通过蓝牙升级安卓手表
 */
public class IndirectOtaAgentPolicy {

    private static final String TAG = "IndirectOtaAgentPolicy";

    /**
     * 获取设备信息
     *
     * @return
     */
    public static String getDeviceInfo() {
        Trace.d(TAG, "getDeviceInfo() ");
        return DeviceInfo.getInstance().toString();
    }

    /**
     * 初始化设置设备信息
     */
    public static CustomDeviceInfo setDeviceInfo(String deviceInfo) {
        Trace.d(TAG, "setDeviceInfo() device info:" + deviceInfo);
        CustomDeviceInfo customDeviceInfo = new CustomDeviceInfo();
        try {
            JSONObject deviceJson = new JSONObject(deviceInfo);
            customDeviceInfo.mid = deviceJson.getString("mid");
            customDeviceInfo.version = deviceJson.getString("version");
            customDeviceInfo.oem = deviceJson.getString("oem");
            customDeviceInfo.models = deviceJson.getString("models");
            customDeviceInfo.platform = deviceJson.getString("platform");
            customDeviceInfo.deviceType = deviceJson.getString("deviceType");
            customDeviceInfo.requestPush = deviceJson.getString("requestPush");
            customDeviceInfo.productId = deviceJson.getString("productId");
            customDeviceInfo.product_secret = deviceJson.getString("productSecret");
        } catch (Exception e) {
            Trace.e(TAG, e);
        }
        return customDeviceInfo;
    }

    /**
     * 重新对设备信息赋值
     *
     * @param deviceInfo
     * @return
     */
    public static boolean resetDeviceInfo(String deviceInfo) {
        Trace.d(TAG, "resetDeviceInfo() device info:" + deviceInfo);
        try {
            JSONObject deviceJson = new JSONObject(deviceInfo);
            DeviceInfo.getInstance().mid = deviceJson.getString("mid");
            DeviceInfo.getInstance().version = deviceJson.getString("version");
            DeviceInfo.getInstance().oem = deviceJson.getString("oem");
            DeviceInfo.getInstance().models = deviceJson.getString("models");
            DeviceInfo.getInstance().platform = deviceJson.getString("platform");
            DeviceInfo.getInstance().deviceType = deviceJson.getString("deviceType");
            DeviceInfo.getInstance().requestPush = deviceJson.getString("requestPush");
            ProductInfo.getInstance().productId = deviceJson.getString("productId");
            ProductInfo.getInstance().productSecret = deviceJson.getString("productSecret");
        } catch (Exception e) {
            Trace.e(TAG, e);
        }
        return DeviceInfo.getInstance().isValid();
    }

    /**
     * 获取版本信息
     *
     * @return
     */
    public static String getVersionInfo() {
        Trace.d(TAG, "getVersionInfo() ");
        String versionInfo = SPFTool.getString(SPFTool.KEY_VERSION_INFO, "");
        return versionInfo;
    }

    /**
     * 设置版本信息
     *
     * @param versionInfo
     */
    public static void setVersionInfo(String versionInfo) {
        Trace.d(TAG, "setVersionInfo() version info:" + versionInfo);
        BeanUtils.setVersionInfo(versionInfo);
    }

    /**
     * 升级结果上报
     */
    public static void reportUpdate(Context context, boolean success) {
        Trace.d(TAG, "reportUpdate() result:" + success);
        ReportManager.getInstance(context).reportUpdateParamInfo(success ? 1 : 99);
    }

}
