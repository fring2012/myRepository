package com.abupdate.sota.info.remote;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class ApkInfo {

//    "appName": "饿了么",
//    "packageName": "xxxxxxxx",
//    "versionCode": 204,
//    "versionName":" 7.23.1"

    private String appName;

    private String packageName;

    private int versionCode;

    private String versionName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
