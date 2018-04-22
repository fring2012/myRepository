package com.example.c.ui.activity.common.code;

public interface Ui {
    /**
     * 抛出等待进度条
     * @param msg
     */
    void showProgressDialog(String msg);

    /**
     * 关闭等待进度条
     */
    void shuntProgressDialog();


}
