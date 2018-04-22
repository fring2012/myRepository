package com.example.c.presenter.common.code;

import com.example.c.ui.activity.common.BaseView;

public interface Presenter <V extends BaseView>{
    /**
     * 绑定activity
     * @param activity
     * @return
     */
    void setActivity(V activity);
    /**
     * 获取activity
     */
    V getActivity();
    /**
     * 解除绑定的activity
     */
    void detachActivity();
}
