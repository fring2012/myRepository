package com.example.c.presenter.Presenter;

import com.example.c.presenter.common.code.Presenter;
import com.example.c.ui.activity.activity.RegisterActivity;

public interface IRegisterPresenter extends Presenter<RegisterActivity> {
    /**
     * 注册广播
     */
    void registerBroadcastReceiver();

    /**
     * 注销广播
     */
    void unRegisterBroadcastReceiver();

    /**
     * 注册账号
     */
    void registerAccount();
}
