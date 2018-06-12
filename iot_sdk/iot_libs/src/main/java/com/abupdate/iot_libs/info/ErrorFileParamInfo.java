package com.abupdate.iot_libs.info;

/**
 * @author fighter_lee
 * @date 2018/1/22
 */
public class ErrorFileParamInfo {

    public String mid;
    public String deltaID;
    public String errorType;
    public String uploadFile;

    public int _id;

    public ErrorFileParamInfo(String mid, String deltaID, String errorType, String uploadFile) {
        this.mid = mid;
        this.deltaID = deltaID;
        this.errorType = errorType;
        this.uploadFile = uploadFile;
    }

    public ErrorFileParamInfo(String mid, String errorType, String uploadFile) {
        this.mid = mid;
        this.errorType = errorType;
        this.uploadFile = uploadFile;
    }

    @Override
    public String toString() {
        return "ErrorFileParamInfo{" + "\n" +
                "mid='" + mid + '\'' + "\n" +
                "deltaID='" + deltaID + '\'' + "\n" +
                "errorType='" + errorType + '\'' + "\n" +
                "uploadFile='" + uploadFile + '\'' + "\n" +
                '}';
    }
}
