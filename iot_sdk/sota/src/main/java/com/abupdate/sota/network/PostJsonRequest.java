package com.abupdate.sota.network;

import com.abupdate.http_libs.HttpIotUtils;
import com.abupdate.http_libs.response.Response;
import com.abupdate.sota.info.local.Error;
import com.abupdate.sota.info.remote.SotaDeviceInfo;
import com.abupdate.sota.info.remote.SotaProductInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.network.base.BaseResponse;
import com.abupdate.sota.network.base.ISign;
import com.abupdate.sota.network.base.Request;
import com.abupdate.sota.security.Codec2;
import com.abupdate.sota.security.Encrypt;
import com.abupdate.sota.utils.JsonAnalyseUtil;
import com.abupdate.sota.utils.TimeUtils;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public abstract class PostJsonRequest<T extends BaseResponse, V> extends Request<T> implements ISign {
    private static final String TAG = "PostJsonRequest";
    private T baseResponse;
    private String url;
    private JSONObject json;

    PostJsonRequest(T t) {
        this.baseResponse = t;
    }

    public PostJsonRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public PostJsonRequest setRequestParams(JSONObject json) {
        this.json = json;
        return this;
    }

    @Override
    protected Response doRequest() {
        Trace.json(TAG, json.toString());
        JSONObject signJson = genSign(json);
        if (null == signJson) {
            return null;
        }
        return HttpIotUtils.postJson(url)
                .json(signJson)
                .build()
                .exec();
    }

    @Override
    protected T parseNetworkResponse(Response response) {
        if (null == response) {
            baseResponse.isOK = false;
            baseResponse.errorCode = Error.ERROR;
        } else if (!response.isResultOk()) {
            baseResponse.isOK = false;
            baseResponse.errorCode = Error.NET_ERROR;
        } else {
            Trace.d(TAG, "parseNetworkResponse() result:" + response.getContent());
            int status = JsonAnalyseUtil.commonJson(response.getContent());
            if (JsonAnalyseUtil.isSuccess(status)) {
                baseResponse.isOK = true;
                baseResponse.result = parseSuccessResult(response);
            } else {
                baseResponse.isOK = false;
                baseResponse.errorCode = status;
            }
        }
        return baseResponse;
    }

    public abstract V parseSuccessResult(Response response);

    @Override
    public JSONObject genSign(JSONObject json) {
        long timestamp = TimeUtils.getSecondTime();
        String sign;
        SotaDeviceInfo sotaDeviceInfo = SotaDeviceInfo.getInstance();
        if (!sotaDeviceInfo.isValid()) {
            Trace.e(TAG, "device info is invalid!");
            return null;
        }
        if (url.contains("register")) {
            if (!SotaProductInfo.getInstance().isProductValid()) {
                Trace.e(TAG, "Product info is invalid!");
                return null;
            }
            sign = Codec2.getHmacMd5Str(sotaDeviceInfo.mid + SotaProductInfo.getInstance().productId + timestamp, SotaProductInfo.getInstance().productSecret);
        } else if (url.contains("obtainProduct")) {
            sign = Encrypt.encryptObtainProduct();
        } else {
            if (!SotaProductInfo.getInstance().isProductValid() || !SotaRegisterInfo.getInstance().isValid()) {
                Trace.e(TAG, "Product info or Register info is invalid!");
                return null;
            }
            sign = Codec2.getHmacMd5Str(SotaRegisterInfo.getInstance().deviceId + SotaProductInfo.getInstance().productId + timestamp, SotaRegisterInfo.getInstance().deviceSecret);
        }
        try {
            json.put("timestamp", timestamp);
            json.put("sign", sign);
        } catch (JSONException e) {
            e.printStackTrace();
            Trace.e(TAG, "genSignAndTime() " + e.toString());
        }
        return json;
    }
}
