package com.abupdate.sota.info.remote;

import android.text.TextUtils;

import com.abupdate.sota.inter.RealCall;
import com.abupdate.sota.network.RequestStack;
import com.abupdate.sota.utils.SPFTool;
import com.abupdate.trace.Trace;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class SotaRegisterInfo {

    private static final String TAG = "RegisterInfo";

    private static SotaRegisterInfo sotaRegisterInfo;

    public String deviceSecret;

    public String deviceId;

    public SotaRegisterInfo() {

    }

    public static SotaRegisterInfo getInstance() {
        if (sotaRegisterInfo == null) {
            synchronized (SotaRegisterInfo.class) {
                if (sotaRegisterInfo == null) {
                    sotaRegisterInfo = new SotaRegisterInfo();
                }
            }
        }
        return sotaRegisterInfo;
    }

    public void init() {
        String device_secret = SPFTool.getString(SPFTool.KEY_DEVICE_SECRET, "");
        String deviceId = SPFTool.getString(SPFTool.KEY_DEVICE_ID, "");
        //应用初次启动初始化进行注册
        if (TextUtils.isEmpty(device_secret) || TextUtils.isEmpty(deviceId)) {
            //需要注册
            Trace.d("RegisterInfo", "init() first register");
            RequestStack.getInstance().getDispatcher().enqueue(new RealCall().genRegisterAsy());
        }
        if (!TextUtils.isEmpty(device_secret)) {
            this.deviceSecret = device_secret;
        }
        if (!TextUtils.isEmpty(deviceId)) {
            this.deviceId = deviceId;
        }
    }

    public boolean isValid() {
        boolean valid = true;
        if (TextUtils.isEmpty(deviceSecret)) {
            Trace.d(TAG, "isValid() deviceSecret = null");
            valid = false;
        }
        if (TextUtils.isEmpty(deviceId)) {
            Trace.d(TAG, "isValid() deviceId = null");
            valid = false;
        }
        return valid;
    }

    public void reset() {
        Trace.d(TAG, "register info reset");
        deviceSecret = "";
        deviceId = "";
        SPFTool.putString(SPFTool.KEY_DEVICE_SECRET, "");
        SPFTool.putString(SPFTool.KEY_DEVICE_ID, "");
    }

}
