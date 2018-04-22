package com.example.c.presenter.Presenter;

import com.example.c.presenter.common.code.Presenter;
import com.example.c.ui.activity.activity.VersionManagerActivity;


public interface IVersionPresenter extends Presenter<VersionManagerActivity>{
    /**
     * 设备注册
     */
    void deviceRegister();

    /**
     * 检测更新
     */
    void checkLatestVesion();

    /**
     * 下载最新版本文件
     */
    void downloadLaterVersionFile();

    /**
     * 暂停下载
     */
    void stopDownloading();

    /**
     * 更新下载
     */
    void upVersion();
    /**
     * 初始化手机版本信息
     */
    void init();
}
