package com.abupdate.iot_libs.interact;

/**
 * Created by fighter_lee on 2017/5/3.
 */

import android.content.Context;
import android.text.TextUtils;

import com.abupdate.http_libs.HttpIotUtils;
import com.abupdate.http_libs.response.Response;
import com.abupdate.iot_libs.utils.HttpsUtils;
import com.abupdate.iot_libs.BuildConfig;
import com.abupdate.iot_libs.constant.SDKConfig;
import com.abupdate.iot_libs.engine.LogManager;
import com.abupdate.iot_libs.engine.TelephoneManager;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.DownParamInfo;
import com.abupdate.iot_libs.info.ErrorFileParamInfo;
import com.abupdate.iot_libs.info.ProductInfo;
import com.abupdate.iot_libs.info.RegisterInfo;
import com.abupdate.iot_libs.info.UpgradeParamInfo;
import com.abupdate.iot_libs.security.Codec2;
import com.abupdate.iot_libs.security.Encrypt;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.utils.DeviceUtils;
import com.abupdate.iot_libs.utils.JsonAnalyticsUtil;
import com.abupdate.iot_libs.utils.MyHostnameVerifier;
import com.abupdate.iot_libs.utils.NetUtils;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.trace.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


/**
 * 注册接口 http://{server:port}/ota/register
 */

public class HttpTools {

    private static final String TAG = "HttpTools";
    private static HttpTools httpTools;

    private HttpTools() {
    }

    public static HttpTools getInstance() {
        if (httpTools == null) {
            synchronized (HttpTools.class) {
                if (httpTools == null) {
                    httpTools = new HttpTools();
                }
            }
        }
        return httpTools;
    }

    public String doPostObtainProduct() {
        String url = SDKConfig.HTTP_BASE_URL+"/product/obtainProduct";
        JSONObject jsonObject = new JSONObject();
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        try {
            jsonObject.put("oem",deviceInfo.oem);
            jsonObject.put("models",deviceInfo.models);
            jsonObject.put("platform",deviceInfo.platform);
            jsonObject.put("deviceType",deviceInfo.deviceType);

        Response response = doPostJson(url, jsonObject);
        if(response.isResultOk()){
            String content = response.getContent();
            JSONObject contentJson = new JSONObject(content);
            if (contentJson.has("status")){
                int status = contentJson.getInt("status");
                if (!JsonAnalyticsUtil.isSuccess(status)){
                    return "";
                }
                if (contentJson.has("data")){
                    String data = contentJson.getString("data");
                    String sign = jsonObject.getString("sign");
                    String key = sign.substring(8, 24);
                    if (!TextUtils.isEmpty(sign)){
                        String s = Encrypt.decodeObtainProduct(key, data);
                        Trace.d(TAG, "doPostObtainProduct() :"+s);
                        return s;
                    }
                }
            }

        }} catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 注册，获取连接时的账号，密码
     *
     * @param deviceInfo
     * @param context
     * @return
     */
    public String doPostRegister(DeviceInfo deviceInfo, Context context) {
        String appVersion = Utils.getAppVersionName(context);
        String networkState = NetUtils.getNetworkState(context);
        String result;
        String url = SDKConfig.HTTP_BASE_URL + "/register/" + ProductInfo.getInstance().productId;
        JSONObject jsonObject = new JSONObject();
        try {
            String macAddress = DeviceUtils.getMacAddress(context);
            jsonObject.put("mid", deviceInfo.mid);
            jsonObject.put("oem", deviceInfo.oem);
            jsonObject.put("models", deviceInfo.models);
            jsonObject.put("platform", deviceInfo.platform);
            jsonObject.put("deviceType", deviceInfo.deviceType);
            jsonObject.put("sdkversion", BuildConfig.VERSION_NAME);
            if (!TextUtils.isEmpty(appVersion)) {
                jsonObject.put("appversion", appVersion);
            }
            jsonObject.put("version", deviceInfo.version);
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

        Response response = doPostJson(url, jsonObject);
        result = response.isResultOk() ? response.getContent() : "";
        return result;
    }

    public String checkVersion(DeviceInfo deviceInfo, Context context) {
        String networkState = NetUtils.getNetworkState(context);
        String result;
        String url = String.format("%s/product/%s/%s/ota/checkVersion", SDKConfig.HTTP_BASE_URL, ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);

        TelephoneManager.build().init(context);
        String cid = TelephoneManager.build().getCid();
        String lac = TelephoneManager.build().getLac();
        String mcc = TelephoneManager.build().getMcc();
        String mnc = TelephoneManager.build().getMnc();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mid", DeviceInfo.getInstance().mid);
            jsonObject.put("version", DeviceInfo.getInstance().version);
            jsonObject.put("networkType", networkState);
            if (!TextUtils.isEmpty(cid) && cid.length() <= 100) {
                jsonObject.put("cid", cid);
            }
            if (!TextUtils.isEmpty(lac) && lac.length() <= 100) {
                jsonObject.put("lac", lac);
            }
            if (!TextUtils.isEmpty(mcc) && mcc.length() <= 100) {
                jsonObject.put("mcc", mcc);
            }
            if (!TextUtils.isEmpty(mnc) && mnc.length() <= 100) {
                jsonObject.put("mnc", mnc);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Response response = doPostJson(url, jsonObject);
        result = response.isResultOk() ? response.getContent() : "";
        return result;
    }

    public String doPostReportDownResult(DownParamInfo downParamInfo) {
        //product/{productId}/device/{deviceId}/ota/reportUpgradeResult
        String url = String.format(SDKConfig.HTTP_BASE_URL + "/product/%s/%s/ota/reportDownResult", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        JSONObject body = new JSONObject();
        try {
            body.put("mid", downParamInfo.mid);
            body.put("deltaID", downParamInfo.deltaID);
            body.put("downloadStatus", downParamInfo.downloadStatus);
            body.put("downStart", downParamInfo.downStart);
            body.put("downEnd", downParamInfo.downEnd);
            body.put("downSize", downParamInfo.downSize);
            if (!TextUtils.isEmpty(downParamInfo.downIp)) {
                body.put("downIp", downParamInfo.downIp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response response = doPostJson(url, body);
        return response.isResultOk() ? response.getContent() : "";
    }

    public String doPostUpgradeResult(UpgradeParamInfo upgradeParamInfo) {
        //product/{productId}/device/{deviceId}/ota/reportUpgradeResult
        //        Trace.d(TAG, "doPostUpgradeResult() start.");
        String url = String.format(SDKConfig.HTTP_BASE_URL + "/product/%s/%s/ota/reportUpgradeResult", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        JSONObject body = new JSONObject();
        try {

            body.put("mid", upgradeParamInfo.mid);
            body.put("deltaID", upgradeParamInfo.deltaID);
            body.put("updateStatus", upgradeParamInfo.updateStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Response response = doPostJson(url, body);
        return response.isResultOk() ? response.getContent() : "";
    }

    public String doPostErrorLog(ErrorFileParamInfo info) throws FotaException {
        File logFile = new File(info.uploadFile);
        if (!logFile.exists()){
            throw new FotaException(FotaException.REASON_ERROR_LOG_NOT_EXIST);
        }
        Map<String, String> params = new HashMap<>();
        String url = String.format(SDKConfig.HTTP_BASE_URL + "/product/%s/%s/ota/reportErrorLog", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);

        Trace.d(TAG, "doPostErrorLog() log path:"+logFile.getAbsolutePath());
        params.put("mid",info.mid);
        if (!TextUtils.isEmpty(info.deltaID)) {
            params.put("deltaID", info.deltaID);
        }
        params.put("errorType",info.errorType);
        Trace.d(TAG, "doPostErrorLog() params:"+Utils.map2String(params));
        genSignAndTime(params);
        Response response = HttpIotUtils.postFile(url)
                .addFile(LogManager.FILE_KEY, logFile)
                .map(params)
                .exec();
        return response.isResultOk()?response.getContent():"";
    }

    public String doPostMsgPushResponse(String msgId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msgId", msgId);
            jsonObject.put("mid", DeviceInfo.getInstance().mid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = String.format(SDKConfig.HTTP_BASE_URL + "/product/%s/%s/notify/response", ProductInfo.getInstance().productId, RegisterInfo.getInstance().deviceId);
        Response response = doPostJson(url, jsonObject);
        return response.isResultOk() ? response.getContent() : "";
    }

    private static void genSignAndTime(JSONObject json, String baseUrl) {
        long timestamp = Utils.getSecondTime();
        String sign;
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        if (!deviceInfo.isValid()){
            Trace.e(TAG,"device info is invalid!");
            return;
        }
        if (baseUrl.contains("register")) {
            if (!ProductInfo.getInstance().isProductValid()){
                Trace.e(TAG,"Product info is invalid!");
                return;
            }
            sign = Codec2.getHmacMd5Str(deviceInfo.mid + ProductInfo.getInstance().productId + timestamp, ProductInfo.getInstance().productSecret);
        } else if (baseUrl.contains("obtainProduct")){
            sign = Encrypt.encryptObtainProduct();
        } else {
            if (!ProductInfo.getInstance().isProductValid() || !RegisterInfo.getInstance().isValid()){
                Trace.e(TAG,"Product info or Register info is invalid!");
                return;
            }
            sign = Codec2.getHmacMd5Str(RegisterInfo.getInstance().deviceId + ProductInfo.getInstance().productId + timestamp, RegisterInfo.getInstance().deviceSecret);
        }
        try {
            json.put("timestamp", timestamp);
            if (!baseUrl.contains("notify/response")) {
                json.put("sign", sign);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Trace.e(TAG, "genSignAndTime() " + e.toString());
        }
    }

    private static void genSignAndTime(Map<String,String> params){
        long timestamp = Utils.getSecondTime();
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        String sign = Codec2.getHmacMd5Str(RegisterInfo.getInstance().deviceId + ProductInfo.getInstance().productId + timestamp, RegisterInfo.getInstance().deviceSecret);
        params.put("timestamp",String.valueOf(timestamp));
        params.put("sign",sign);
    }

    private static Response doPostJson(String baseUrl, JSONObject json) {
        Trace.d(TAG, "doPostJson() request: url:" + baseUrl + " ,json:" + json.toString());
        //添加时间戳和签名
        genSignAndTime(json, baseUrl);
        Trace.d(TAG, "doPostJson() " + json.toString());
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = HttpsUtils.getSSLContext(new String(SDKConfig.KEY), "adcom.bks");
            if (sslContext != null){
                sslSocketFactory = sslContext.getSocketFactory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response response = HttpIotUtils.postJson(baseUrl)
                .json(json)
                .setHostnameVerifier(new MyHostnameVerifier())
                .setSslSocketFactory(sslSocketFactory)
                .build()
                .exec();
        Trace.d(TAG, "doPostJson() response: result:%s", response.isResultOk() ? response.getContent() : "null");
        if (!response.isResultOk()) {
            if (null != response.getException()) {
                Trace.e(TAG, "doPostJson() exception:", response.getException());
            }
        }
        return response;
    }
}
