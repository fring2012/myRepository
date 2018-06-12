package com.abupdate.sota.info.remote;

/**
 * @author fighter_lee
 * @date 2018/3/24
 */
public class ReportInfo {

    public int _id;

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getReportType() {
        return reportType;
    }

    public int getReportStatus() {
        return reportStatus;
    }

    private String appName;

    private String packageName;

    private int versionCode;

    private String versionName;

    private String reportType;

    private int reportStatus;

    public void setId(int id) {
        this._id = id;
    }

    public ReportInfo(String appName, String packageName, int versionCode, String versionName, String reportType, int reportStatus) {
        this.appName = appName;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.reportType = reportType;
        this.reportStatus = reportStatus;
    }

}
