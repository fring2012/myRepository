package com.example.administrator.ui_make.ui.branchview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.example.administrator.ui_make.R;
import com.example.administrator.ui_make.ui.branchview.pieceview.RoundTextView;
import com.example.administrator.ui_make.ui.branchview.viewgroup.LeafViewGroup;

public class BranchViewGroup extends FrameLayout implements IUpgradeView{
    private RoundTextView roundTextView;
    private LeafViewGroup leafViewGroup;
    private Object tag;
    public BranchViewGroup(@NonNull Context context) {
        super(context);
    }

    public BranchViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.branch_layout,this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initChildView();
    }


    private void initChildView(){
        leafViewGroup = (LeafViewGroup) findViewById(R.id.leaf_view_group);
        roundTextView = (RoundTextView) findViewById(R.id.round_text_view);
    }


    /**
     * 等待ui状态
     */
    public void goToWaitUi(){
        leafViewGroup.getScaleCircleView().setVisibility(VISIBLE);
        leafViewGroup.getScaleCircleView().startAnim();
        leafViewGroup.getRoundProgress().setVisibility(GONE);
        leafViewGroup.getImageView().setVisibility(GONE);
        leafViewGroup.getTextView().setVisibility(GONE);
    }

    /**
     * 下载中的状态
     */
    public void goToDownloadingStateUi(){
        leafViewGroup.getScaleCircleView().setVisibility(GONE);
        leafViewGroup.getScaleCircleView().clearAnimation();
        leafViewGroup.getRoundProgress().setVisibility(VISIBLE);
        leafViewGroup.getImageView().setVisibility(GONE);
        leafViewGroup.getTextView().setVisibility(VISIBLE);
    }




    @Override
    public IUpgradeView setText(String str) {
        roundTextView.setText(str);
        return this;
    }



    @Override
    public IUpgradeView setProgress(int i) {
        if (leafViewGroup.getRoundProgress().getVisibility() != VISIBLE){
            goToDownloadingStateUi();
        }
        leafViewGroup.setDownloadProgress(i);
        return this;
    }

    @Override
    public int getProgress() {
        return leafViewGroup.getRoundProgress().getProgress();
    }

    @Override
    public IUpgradeView setFinished() {
        leafViewGroup.getScaleCircleView().setVisibility(GONE);
        leafViewGroup.getScaleCircleView().clearAnimation();
        leafViewGroup.getRoundProgress().setVisibility(GONE);
        leafViewGroup.getImageView().setVisibility(VISIBLE);
        leafViewGroup.getTextView().setVisibility(GONE);
        return this;
    }

    @Override
    public IUpgradeView setIdle() {
        goToWaitUi();
        return this;
    }



}
