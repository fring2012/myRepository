package com.abupdate.iot_libs.info;

import android.text.TextUtils;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.Error;
import com.abupdate.iot_libs.inter.IRegisterListener;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;


/**
 * Created by fighter_lee on 2017/5/25.
 */

public class RegisterInfo {

    private static final String TAG = "RegisterInfo";

    private static RegisterInfo registerInfo;

    public String deviceSecret;

    public String deviceId;

    public RegisterInfo() {

    }

    public static RegisterInfo getInstance() {
        if (registerInfo == null) {
            synchronized (RegisterInfo.class) {
                if (registerInfo == null) {
                    registerInfo = new RegisterInfo();
                }
            }
        }
        return registerInfo;
    }

    public void init() {
        String device_secret = SPFTool.getString(SPFTool.KEY_DEVICE_SECRET, "");
        String deviceId = SPFTool.getString(SPFTool.KEY_DEVICE_ID, "");
        //应用初次启动初始化进行注册
        if (TextUtils.isEmpty(device_secret) || TextUtils.isEmpty(deviceId)) {
            //需要注册
            Trace.d("RegisterInfo", "init() first register");
            OtaAgentPolicy.registerAsync(new IRegisterListener() {
                @Override
                public void onSuccess() {
                    Trace.d("RegisterInfo", "init() register success ");
                }

                @Override
                public void onFailed(int error) {
                    Trace.w("RegisterInfo", "init() register fail, " + Error.getErrorMessage(error));
                }
            });
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
