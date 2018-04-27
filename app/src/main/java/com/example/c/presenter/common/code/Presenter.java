package com.example.c.presenter.common.code;

import com.example.c.ui.activity.common.BaseView;

public interface Presenter <V extends BaseView>{
    /**
     * 绑定activity
     * @param view
     * @return
     */
    void setView(V view);
    /**
     * 获取activity
     */
    V getView();
    /**
     * 解除绑定的activity
     */
    void detachView();
}
