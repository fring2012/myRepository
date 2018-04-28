package com.example.c.presenter.Presenter;

import com.example.c.presenter.common.code.Presenter;
import com.example.c.ui.activity.activity.VersionManagerActivity;


public interface IVersionPresenter extends Presenter<VersionManagerActivity>{
    /**
     * 注册广播
     */
    void registerBroadcastReceiver();
    /**
     * 注销广播
     */
    void unRegisterBroadcastReceiver();
    /**
     * 设备注册
     */
    void deviceRegister();

    /**
     * 检测更新
     */
    void checkLatestVersion();

    /**
     * 下载最新版本文件
     */
    void downloadLatestVersionFile();

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
