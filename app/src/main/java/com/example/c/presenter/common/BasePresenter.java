package com.example.c.presenter.common;

import com.example.c.presenter.common.code.Presenter;
import com.example.c.ui.activity.common.BaseView;
import com.example.c.utils.PropertiesUtils;

import java.util.Properties;


public class BasePresenter<V extends BaseView> implements Presenter<V> {
    private V activityView;
    protected Properties baseproperties;
    //下载文件路径


    public BasePresenter(){
        baseproperties = PropertiesUtils.getPropertes(activityView);
    }

    @Override
    public void setView(V view) {
        this.activityView = view;

    }

    @Override
    public V getView() {
        chechViewAttach();
        return activityView;
    }


    @Override
    public void detachView() {
        activityView = null;
    }

    private boolean isSetActivity(){
        return activityView != null;
    }
    public void chechViewAttach(){
        if(!isSetActivity())
            throw new ViewIsNullException();
    }
    public static class ViewIsNullException extends RuntimeException{
        public ViewIsNullException(){
            super("请求数据前请先调用 setView(View) 方法与View建立连接");
        }
    }
}
