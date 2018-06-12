package com.abupdate.sota.info.remote;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class NewAppInfo {

    private String appIcon;

    private String appName;

    private String packageName;

    private int versionCode;

    private String versionName;

    private String downUrl;

    private long downSize;

    private String description;

    private String simpleDesc;

    private String downTimes;

    private String publishTime;

    private String sortNum;

    private boolean installForce;

    public String getAppIcon() {
        return appIcon;
    }

    public NewAppInfo setAppIcon(String appIcon) {
        this.appIcon = appIcon;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public NewAppInfo setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public NewAppInfo setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public NewAppInfo setVersionCode(int versionCode) {
        this.versionCode = versionCode;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public NewAppInfo setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public NewAppInfo setDownUrl(String downUrl) {
        this.downUrl = downUrl;
        return this;
    }

    public long getDownSize() {
        return downSize;
    }

    public NewAppInfo setDownSize(long downSize) {
        this.downSize = downSize;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NewAppInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSimpleDesc() {
        return simpleDesc;
    }

    public NewAppInfo setSimpleDesc(String simpleDesc) {
        this.simpleDesc = simpleDesc;
        return this;
    }

    public String getDownTimes() {
        return downTimes;
    }

    public NewAppInfo setDownTimes(String downTimes) {
        this.downTimes = downTimes;
        return this;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public NewAppInfo setPublishTime(String publishTime) {
        this.publishTime = publishTime;
        return this;
    }

    public String getSortNum() {
        return sortNum;
    }

    public NewAppInfo setSortNum(String sortNum) {
        this.sortNum = sortNum;
        return this;
    }

    public boolean isInstallForce() {
        return installForce;
    }

    public NewAppInfo setInstallForce(boolean installForce) {
        this.installForce = installForce;
        return this;
    }

}
