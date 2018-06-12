package com.abupdate.sota.network;

import android.text.TextUtils;

import com.abupdate.http_libs.response.Response;
import com.abupdate.sota.BuildConfig;
import com.abupdate.sota.SotaControler;
import com.abupdate.sota.info.local.SDKConfig;
import com.abupdate.sota.info.remote.SotaDeviceInfo;
import com.abupdate.sota.info.remote.SotaProductInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.network.base.CommonResponse;
import com.abupdate.sota.utils.DeviceUtils;
import com.abupdate.sota.utils.NetUtils;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class RegisterRequest extends PostJsonRequest<CommonResponse<SotaRegisterInfo>,SotaRegisterInfo> {

    private static final String TAG = "RegisterRequest";

    RegisterRequest() {
        this(new CommonResponse<SotaRegisterInfo>());
    }

    RegisterRequest(CommonResponse<SotaRegisterInfo> registerInfoCommonResponse) {
        super(registerInfoCommonResponse);
    }

    @Override
    protected Response doRequest() {
        String appVersion = DeviceUtils.getAppVersionName(SotaControler.sContext);
        String networkState = NetUtils.getNetworkState(SotaControler.sContext);
        String url = SDKConfig.HTTP_BASE_URL + "/register/" + SotaProductInfo.getInstance().productId;
        JSONObject jsonObject = new JSONObject();
        try {
            String macAddress = DeviceUtils.getMacAddress(SotaControler.sContext);
            SotaDeviceInfo sotaDeviceInfo = SotaDeviceInfo.getInstance();
            jsonObject.put("mid", sotaDeviceInfo.mid);
            jsonObject.put("oem", sotaDeviceInfo.oem);
            jsonObject.put("models", sotaDeviceInfo.models);
            jsonObject.put("platform", sotaDeviceInfo.platform);
            jsonObject.put("deviceType", sotaDeviceInfo.deviceType);
            jsonObject.put("sdkversion", BuildConfig.VERSION_NAME);
            if (!TextUtils.isEmpty(appVersion)) {
                jsonObject.put("appversion", appVersion);
            }
            jsonObject.put("version", sotaDeviceInfo.version);
            if (!TextUtils.isEmpty(macAddress)) {
                jsonObject.put("mac", macAddress);
            }
            if (!TextUtils.isEmpty(networkState)) {
                jsonObject.put("networkType", networkState);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Trace.d(TAG, "doPostRegister() :" + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Trace.d(TAG, "doPostRegister() :" + e.toString());
        }
        setUrl(url).setRequestParams(jsonObject);
        return super.doRequest();
    }

    @Override
    public SotaRegisterInfo parseSuccessResult(Response response) {
        return SotaRegisterInfo.getInstance();
    }

}
