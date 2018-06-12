package com.example.administrator.ui_make.ui.activity;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.example.administrator.ui_make.R;
import com.example.administrator.ui_make.ui.branchview.IUpgradeView;


import butterknife.BindView;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.branch_ecu)
    IUpgradeView branchViewGroup;

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

}
