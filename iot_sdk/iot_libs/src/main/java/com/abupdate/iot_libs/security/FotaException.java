package com.abupdate.iot_libs.security;


/**
 * Created by fighter_lee on 2017/7/4.
 */

public class FotaException extends Exception {

    public static final int REASON_CODE_CLIENT_ERROR = 0;

    /**
     * AndroidManifest 缺少必要的权限和组件
     */
    public static final int REASON_CODE_MANIFEST_NOT_REGISTER = 201;
    public static final int REASON_CODE_DEVICE_PARAMETERS = 202;
    public static final int REASON_CODE_MANIFEST_META_DATA_ERROR = 203;
    public static final int REASON_ERROR_LOG_NOT_EXIST = 204;

    private int reasonCode;
    private Throwable cause;

    public FotaException(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public FotaException(Throwable cause) {
        this.reasonCode = 0;
        this.cause = cause;
    }

    public FotaException(int reason, Throwable cause) {
        this.reasonCode = reason;
        this.cause = cause;
    }

    public int getReasonCode() {
        return this.reasonCode;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String getMessage() {
        return getErrorMessage(this.reasonCode);
    }

    @Override
    public String toString() {
        String result = this.getMessage() + " (" + this.reasonCode + ")";
        if (this.cause != null) {
            result = result + " - " + this.cause.toString();
        }

        return result;
    }

    private String getErrorMessage(int reasonCode) {
        switch (reasonCode){
            case REASON_CODE_MANIFEST_NOT_REGISTER:
                return "AndroidManifest element and permissions is lack";
            case REASON_CODE_DEVICE_PARAMETERS:
                return "Fota [DeviceInfo] initPackagePath device parameters exception";
            case REASON_CODE_MANIFEST_META_DATA_ERROR:
                return "AndroidManifest meta-data is null or should start with string/";
            case REASON_ERROR_LOG_NOT_EXIST:
                return "error log file not exist";
            default:
                return "UnExpect Exception";
        }
    }
}
