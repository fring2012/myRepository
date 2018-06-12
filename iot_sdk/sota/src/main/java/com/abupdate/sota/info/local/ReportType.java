package com.abupdate.sota.info.local;

/**
 * @author fighter_lee
 * @date 2018/3/23
 */
public enum  ReportType {

    DOWNLOAD(Constants.DOWNLOAD),
    UPDATE(Constants.UPDATE);

    String type;

    ReportType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
