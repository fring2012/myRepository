package com.abupdate.iot_libs.info;

/**
 * 系统升级参数实体
 */
public class UpgradeParamInfo {
    public String mid; //设备唯一标识，如IMEI、SN等，不同的设备一定不能重复
    public String deltaID; //升级包ID
    public String updateStatus; //升级状态：1 –成功  2-失败

    public int _id;

    public UpgradeParamInfo() {
        this.mid = DeviceInfo.getInstance().mid;
    }

    public UpgradeParamInfo(String mid, String deltaID, String updateStatus) {
        this.mid = mid;
        this.deltaID = deltaID;
        this.updateStatus = updateStatus;
    }

    @Override
    public String toString() {
        return "UpgradeParamInfo{" + "\n" +
                "updateStatus='" + updateStatus + '\'' + "\n" +
                "deltaID='" + deltaID + '\'' + "\n" +
                "mid='" + mid + '\'' + "\n" +
                '}';
    }
}
