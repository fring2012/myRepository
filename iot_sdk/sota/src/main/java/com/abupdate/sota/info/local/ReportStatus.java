package com.abupdate.sota.info.local;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public enum ReportStatus {

    DOWNLOAD_SUCCESSS(Constants.DOWNLOAD_SUCCESS),
    DOWNLOAD_FAILED(Constants.DOWNLOAD_FAILED),
    UPDATE_SUCCESS(Constants.UPDATE_SUCCESS),
    UPDATE_FAILED(Constants.UPDATE_FAILED);

    int type;

    ReportStatus(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
