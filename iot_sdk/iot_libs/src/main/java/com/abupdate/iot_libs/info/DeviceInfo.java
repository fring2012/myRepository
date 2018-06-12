package com.abupdate.iot_libs.info;

import android.text.TextUtils;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class DeviceInfo {

    private static final String TAG = "DeviceInfo";

    public static final String KEY_MID_BACK = "key_mid_back";

    /**
     * 设备唯一标示码
     */
    public String mid;

    /**
     * 设备版本号
     */
    public String version;
    /**
     * 厂商信息，广升提供
     */
    public String oem;
    /**
     * 设备型号，同一个厂商下面不允许出现相同型号的设备。oem+ models组成一个项目
     */
    public String models;
    /**
     * 芯片平台信息，如MTK6582、SPRD8830、MSM9x15，广升给出平台列表
     */
    public String platform;
    /**
     * 设备类型，如phone、box、pad、mifi等，广升给出类型列表
     */
    public String deviceType;
    /**
     * 是否开启升级推送
     */
    public String requestPush;

    public static DeviceInfo mInstance;

    private static final String KEY_DEVICE_INFO = "key_device_info";

    private DeviceInfo() {

    }

    public static DeviceInfo getInstance() {
        if (mInstance == null) {
            synchronized (DeviceInfo.class) {
                if (mInstance == null) {
                    mInstance = new DeviceInfo();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化设备信息
     *
     * @throws FotaException
     */
    public void init() throws FotaException {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "init info", OtaConstants.SINGLE_LINE);
        try {
            ClassLoader cl = OtaAgentPolicy.sCx.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;

            Method get = SystemProperties.getMethod("get", paramTypes);

            this.version = (String) get.invoke(SystemProperties, new Object[]{"ro.fota.version"});
            this.oem = (String) get.invoke(SystemProperties, new Object[]{"ro.fota.oem"});
            this.models = (String) get.invoke(SystemProperties, new Object[]{"ro.fota.device"});
            this.platform = (String) get.invoke(SystemProperties, new Object[]{"ro.fota.platform"});
            this.deviceType = (String) get.invoke(SystemProperties, new Object[]{"ro.fota.type"});
            this.requestPush = (String) get.invoke(SystemProperties, new Object[]{"ro.fota.push"});

        } catch (Exception e) {
            throw new FotaException(FotaException.REASON_CODE_DEVICE_PARAMETERS, e);
        } finally {
            if (!compareProduct()) {
                ProductInfo.getInstance().reset();
                RegisterInfo.getInstance().reset();
            }
        }

        Trace.d(TAG, String.format("version:%s,oem:%s,models:%s,platform:%s,deviceType:%s,requestPush:%s", version, oem, models, platform, requestPush));
    }

    /**
     * 设置设备mid
     *
     * @param mid
     */
    public void initInfo(String mid) {
        Trace.d(TAG, "set mid:" + mid);
        this.mid = mid;
        SPFTool.putString(KEY_MID_BACK, mid);
    }

    /**
     * 方便测试时调用
     *
     * @param version
     * @param oem
     * @param models
     * @param platform
     * @param deviceType
     * @param requestPush
     * @return
     */
    public void initOtherInfo(String version, String oem, String models, String platform, String deviceType, String requestPush) throws FotaException {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "init other info", OtaConstants.SINGLE_LINE);
        this.oem = oem;
        this.models = models;
        this.platform = platform;
        this.deviceType = deviceType;
        this.requestPush = requestPush;
        this.version = version;
        if (!compareProduct()) {
            ProductInfo.getInstance().reset();
            RegisterInfo.getInstance().reset();
        }
        Trace.d(TAG, String.format("version:%s,oem:%s,models:%s,platform:%s,deviceType:%s,requestPush:%s", version, oem, models, platform, deviceType, requestPush));
    }

    public boolean isValid() {
        boolean valid = true;
        if (TextUtils.isEmpty(mid)) {
            Trace.d(TAG, "isValid() mid = null");
            valid = false;
        }
        if (TextUtils.isEmpty(oem)) {
            Trace.d(TAG, "isValid() oem = null");
            valid = false;
        }
        if (TextUtils.isEmpty(models)) {
            Trace.d(TAG, "isValid() models = null");
            valid = false;
        }
        if (TextUtils.isEmpty(platform)) {
            Trace.d(TAG, "isValid() platform = null");
            valid = false;
        }
        if (TextUtils.isEmpty(deviceType)) {
            Trace.d(TAG, "isValid() deviceType = null");
            valid = false;
        }
        if (TextUtils.isEmpty(requestPush)) {
            Trace.d(TAG, "isValid() requestPush = null");
            valid = false;
        }
        return valid;
    }

    /**
     * 将设备信息存储，实现无缝跨项目升级
     *
     * @return
     */
    private boolean compareProduct() {
        String storeInfo = SPFTool.getString(KEY_DEVICE_INFO, "");
        StringBuilder builder = new StringBuilder();
        String currentInfo = builder.append(DeviceInfo.getInstance().oem)
                .append(DeviceInfo.getInstance().models)
                .append(DeviceInfo.getInstance().platform)
                .append(DeviceInfo.getInstance().deviceType)
                .append(DeviceInfo.getInstance().requestPush)
                .toString();
        if (TextUtils.equals(storeInfo, currentInfo)) {
            return true;
        } else {
            SPFTool.putString(KEY_DEVICE_INFO, currentInfo);
            return false;
        }
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("mid", mid);
            object.put("version", version);
            object.put("oem", oem);
            object.put("models", models);
            object.put("platform", platform);
            object.put("deviceType", deviceType);
            object.put("requestPush", requestPush);
            object.put("productId", ProductInfo.getInstance().productId);
            object.put("productSecret", ProductInfo.getInstance().productSecret);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
