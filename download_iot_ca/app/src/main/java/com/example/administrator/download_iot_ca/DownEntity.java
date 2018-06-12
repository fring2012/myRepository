package com.example.administrator.download_iot_ca;

public class DownEntity {
    public static final int VERIFY_MD5 = 1;
    public static final int VERIFY_SHA256 = 2;

    public String downPath; //绝对路径
    public String hash;
    public String fileSize;
    public String downloadUrl;
    public int verify_mode = VERIFY_MD5;
    public long downloaded_size; //已经下载的大小
    public int download_status;
    public volatile boolean download_cancel;

    public DownEntity(String downPath, String hash, String fileSize, String downloadUrl) {
        this.downPath = downPath;
        this.hash = hash;
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String toString() {
        return "DownEntity{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", downPath='" + downPath + '\'' +
                ", hash='" + hash + '\'' +
                ", fileSize=" + fileSize +
                ", downloaded_size=" + downloaded_size +
                ", download_status=" + download_status +
                ", download_cancel=" + download_cancel +
                '}';
    }
}
