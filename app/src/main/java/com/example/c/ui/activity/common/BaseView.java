package com.example.c.ui.activity.common;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.c.ui.activity.common.code.Ui;

public class BaseView extends Activity  implements Ui {
    
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void showProgressDialog(String msg) {
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    @Override
    public void shuntProgressDialog() {
        if(progressDialog != null)
            progressDialog.dismiss();
    }

}
