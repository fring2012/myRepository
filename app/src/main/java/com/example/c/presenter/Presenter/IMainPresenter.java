package com.example.c.presenter.Presenter;

import com.example.c.presenter.common.code.Presenter;
import com.example.c.ui.activity.activity.MainActivity;

public interface IMainPresenter extends Presenter<MainActivity> {
    /**
     * 注册广播接收器
     */
    void  registerBroadcastReceiver();

    /**
     * 注销广播接收器
     */
    void  unRegisterBroadcastReceiver();

    /**
     * 登录
     */
    void login();
}
