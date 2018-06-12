package com.abupdate.iot_libs.info;


import java.text.SimpleDateFormat;

/**
 * 下载升级包参数实体
 */
public class DownParamInfo {
    public String mid; //设备唯一标识，如IMEI、SN等，不同的设备一定不能重复
    public String deltaID; //升级包ID
    public String downloadStatus; //下载状态：1 –成功  99-失败
    public String downStart; //下载开始时间，格式：2015-01-20 10:11:13
    public String downEnd; //下载结束时间，格式：2015-01-20 10:13:44
    public String downIp;
    public int downSize;

    public int _id;

    public DownParamInfo() {
        this.mid = DeviceInfo.getInstance().mid;
    }

    /**
     * @param deltaID        升级包ID
     * @param downloadStatus 下载状态：1 –成功  99-失败
     * @param downStart      下载开始时间，格式：2015-01-20 10:11:13
     * @param downEnd        下载结束时间，格式：2015-01-20 10:11:13
     */
    public DownParamInfo(String deltaID, String downloadStatus, long downStart, long downEnd, int downSize, String downIp) {
        this.deltaID = deltaID;
        this.downEnd = String.valueOf(downEnd);
        this.downloadStatus = downloadStatus;
        this.downStart = String.valueOf(downStart);
        this.mid = DeviceInfo.getInstance().mid;
        this.downIp = downIp;
        this.downSize = downSize;
    }

    /**
     * @param deltaID        升级包ID
     * @param downloadStatus 下载状态：1 –成功  99-失败
     * @param downStart      下载开始时间，格式：2015-01-20 10:11:13
     * @param downEnd        下载结束时间，格式：2015-01-20 10:11:13
     * @param extStr         下载信息描述
     */
    public DownParamInfo(String deltaID, String downloadStatus, long downStart, long downEnd, String extStr) {
        this.deltaID = deltaID;
        this.downEnd = String.valueOf(downEnd);
        this.downloadStatus = downloadStatus;
        this.downStart = String.valueOf(downStart);
        this.mid = DeviceInfo.getInstance().mid;
    }

    public static String getFormatTime(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(time);
    }

    public String displayString() {
        return "mid" + mid + '\'' +
                ", deltaID='" + deltaID + '\'' +
                ", downloadStatus='" + downloadStatus + '\'' +
                ", downStart='" + downStart + '\'' +
                ", downEnd='" + downEnd + '\'';
    }

    @Override
    public String toString() {
        return "DownParamInfo{" + "\n" +
                "mid='" + mid + '\'' + "\n" +
                "deltaID='" + deltaID + '\'' + "\n" +
                "downloadStatus='" + downloadStatus + '\'' + "\n" +
                "downStart='" + downStart + '\'' + "\n" +
                "downEnd='" + downEnd + '\'' + "\n" +
                '}';
    }
}
