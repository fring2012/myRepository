package com.example.c.been;

public class FileInfo {
    private String fileName;//文件名
    private String url;//下载地址
    private int length = 0;//文件大小
    private int finished = 0;//下载以已完成进度
    private boolean isStop = false;//是否暂停下载
    private boolean isDownLoading = false;//是否正在下载

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int len) {
        this.finished += len;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public boolean isDownLoading() {
        return isDownLoading;
    }

    public void setDownLoading(boolean downLoading) {
        isDownLoading = downLoading;
    }

    public String toString(){
        return "fileName;" + fileName
               + "  ; url:" + url
               + "  ; length:" + length
               + "  ; finished:" + finished
               + "  ; isStop:" + isStop
               + "  ; isDownLoading:" +isDownLoading;
    }
}
