package com.abupdate.sota.utils;

import android.content.Context;

import com.abupdate.sota.info.local.Error;
import com.abupdate.sota.info.remote.NewAppInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Json 解析工具类，用于对检测版本返回数据做解析，封装在各个info中
 */
public class JsonAnalyseUtil {

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

    public static int commonJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("status")) {
                int status = jsonObject.getInt("status");
                return status;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Error.SIGN_IS_ERROR;
    }

    //设置register信息
    public static void setRegisterInfo(String jsonStr, Context sCx) {
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.has("deviceSecret")) {
                    String deviceSecret = data.getString("deviceSecret");
                    SotaRegisterInfo.getInstance().deviceSecret = deviceSecret;
                    SPFTool.putString(SPFTool.KEY_DEVICE_SECRET, deviceSecret);
                }
                if (data.has("deviceId")) {
                    String deviceId = data.getString("deviceId");
                    SotaRegisterInfo.getInstance().deviceId = deviceId;
                    SPFTool.putString(SPFTool.KEY_DEVICE_ID, deviceId);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAllApp(String jsonStr) {
        List<String> packageNameList = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.has("packageName")) {
                    JSONArray packageName = data.getJSONArray("packageName");
                    for (int i = 0; i < packageName.length(); i++) {
                        packageNameList.add(packageName.getString(i));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return packageNameList;
    }

    public static List<NewAppInfo> getNewApp(String content) {
        List<NewAppInfo> appInfos = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has("data")) {
                JSONArray data = jsonObject.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject dataJson = data.getJSONObject(i);
                    String appIcon = "";
                    if (dataJson.has("appIcon")) {
                        appIcon = dataJson.getString("appIcon");
                    }

                    String appName = "";
                    if (dataJson.has("appName")) {
                        appName = dataJson.getString("appName");
                    }

                    String packageName = "";
                    if (dataJson.has("packageName")) {
                        packageName = dataJson.getString("packageName");
                    }

                    int versionCode = 0;
                    if (dataJson.has("versionCode")) {
                        versionCode = dataJson.getInt("versionCode");
                    }

                    String versionName = "";
                    if (dataJson.has("versionName")) {
                        versionName = dataJson.getString("versionName");
                    }

                    String downUrl = "";
                    if (dataJson.has("downUrl")) {
                        downUrl = dataJson.getString("downUrl");
                    }

                    long downSize = 0;
                    if (dataJson.has("downSize")) {
                        downSize = dataJson.getLong("downSize");
                    }

                    String description = "";
                    if (dataJson.has("description")) {
                        description = dataJson.getString("description");
                    }

                    String simpleDesc = "";
                    if (dataJson.has("simpleDesc")) {
                        simpleDesc = dataJson.getString("simpleDesc");
                    }

                    String downTimes = "";
                    if (dataJson.has("downTimes")) {
                        downTimes = dataJson.getString("downTimes");
                    }

                    String publishTime = "";
                    if (dataJson.has("publishTime")) {
                        publishTime = dataJson.getString("publishTime");
                    }

                    String sortNum = "";
                    if (dataJson.has("sortNum")) {
                        sortNum = dataJson.getString("sortNum");
                    }

                    boolean installForce = false;
                    if (dataJson.has("installForce")) {
                        installForce = (dataJson.getInt("installForce") == 0 ? true : false);
                    }

                    NewAppInfo newAppInfo = new NewAppInfo()
                            .setAppIcon(appIcon)
                            .setAppName(appName)
                            .setPackageName(packageName)
                            .setVersionCode(versionCode)
                            .setVersionName(versionName)
                            .setDownUrl(downUrl)
                            .setDownSize(downSize)
                            .setDescription(description)
                            .setSimpleDesc(simpleDesc)
                            .setDownTimes(downTimes)
                            .setPublishTime(publishTime)
                            .setSortNum(sortNum)
                            .setInstallForce(installForce);
                    appInfos.add(newAppInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appInfos;
    }
}
