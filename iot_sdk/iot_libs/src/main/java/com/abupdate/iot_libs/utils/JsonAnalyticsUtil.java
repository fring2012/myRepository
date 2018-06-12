package com.abupdate.iot_libs.utils;

import android.content.Context;

import com.abupdate.iot_libs.constant.Error;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Json 解析工具类，用于对检测版本返回数据做解析，封装在各个info中
 */
public class JsonAnalyticsUtil {

    public static final int SUCCESS = 1000; //check version success

    public static boolean isSuccess(int status) {
        if (status == SUCCESS) {
            return true;
        }
        return false;
    }

    public static boolean responseSuccess(int i) {
        if (i == 1000) {
            return true;
        }
        return false;
    }

    //解析下载及上报 共用json
    public static boolean reportJson(String jsonStr) {
        try {
            JSONObject jobj = new JSONObject(jsonStr);
            if (jobj.has("status")) {
                int status = jobj.getInt("status");
                return isSuccess(status);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int registerJson(String json, Context sCx) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("status")) {
                int status = jsonObject.getInt("status");
                return status;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Error.REGISTER_SIGN_ERROR;
    }

    public static int responseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("body")) {
                JSONObject body = jsonObject.getJSONObject("body");
                if (body.has("status")) {
                    int status = body.getInt("status");
                    return status;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Error.RESPONSE_ERROR;
    }

    //解析 版本json
    public static int versionJson(String jsonStr) {
        try {
            JSONObject body = new JSONObject(jsonStr);
            if (body.has("status")) {
                int status = body.getInt("status");
                return status;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Error.SERVER_DATA_ERROR;
    }

    //解析 login/response json，返回status字段
    public static String loginJson(String jsonStr) {
        try {
            String status;
            JSONObject jsonObject = new JSONObject(jsonStr);
            if (jsonObject.getJSONObject("body").getJSONObject("response").has("status")) {
                status = jsonObject.getJSONObject("body").getJSONObject("response").getString("status");
                return status;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
