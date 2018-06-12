package com.abupdate.iot_download_libs.segmentDownload;

/**
 * Created by fighter_lee on 2017/9/5.
 */

public class SegmentDownInfo {

    private int num;
    private String md5;
    private long startpos;
    private long endpos;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getStartpos() {
        return startpos;
    }

    public void setStartpos(long startpos) {
        this.startpos = startpos;
    }

    public long getEndpos() {
        return endpos;
    }

    public void setEndpos(long endpos) {
        this.endpos = endpos;
    }
}
