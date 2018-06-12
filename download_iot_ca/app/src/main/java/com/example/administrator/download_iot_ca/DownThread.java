package com.example.administrator.download_iot_ca;

public class DownThread implements Runnable {
    private String downloadUrl;
    private String downPath;
    private String start;
    private String end;
    public DownThread(DownEntity downEntity){
        downloadUrl = downEntity.downloadUrl;
        downPath = downEntity.downPath;
    }
    @Override
    public void run() {

    }
}
