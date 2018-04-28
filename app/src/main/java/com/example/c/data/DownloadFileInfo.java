package com.example.c.data;

import com.example.c.been.FileInfo;

public class DownloadFileInfo {
    private static DownloadFileInfo downloadFileInfo;
    private FileInfo fileInfo;

    private DownloadFileInfo() {
    }

    public static DownloadFileInfo getDownloadFileInfo(){
        if(downloadFileInfo == null)
            downloadFileInfo = new DownloadFileInfo();
        return downloadFileInfo;
    }

    public static void setDownloadFileInfo(DownloadFileInfo downloadFileInfo) {
        DownloadFileInfo.downloadFileInfo = downloadFileInfo;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }
}
