package com.example.c.utils;
//下载进度接口
public interface OnProgressListener {

    void updateProgress(int max, int progress);
}