package com.example.administrator.ui_make.ui.branchview.viewgroup;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.administrator.ui_make.R;
import com.example.administrator.ui_make.ui.branchview.pieceview.RoundProgressView;
import com.example.administrator.ui_make.ui.branchview.pieceview.ScaleCircleView;

public class LeafViewGroup extends RelativeLayout implements View.OnClickListener{
    private static final String TAG = "LeafViewGroup";
    public static final int CHECK_VERSION = 1;
    public static final int DOWNLOAD_VERSION = 2;
    public static final int UPGRADE_VERSION = 3;
    public static final int DISABLE = 4;


    private ScaleCircleView scaleCircleView;
    private ImageView imageView;
    private TextView textView;
    private RoundProgressView roundProgress;
    private LeafOnClickListener leafOnClickListener;
    private int state = 1;


    public LeafViewGroup(@NonNull Context context) {
        super(context);
    }

    public LeafViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.leaf_layout,this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initChildView();//初始化子类控件
    }

    private void initChildView(){
        scaleCircleView = (ScaleCircleView) findViewById(R.id.leaf_scale_circle_view);
        textView = (TextView) findViewById(R.id.leaf_text_view);
        imageView = (ImageView) findViewById(R.id.leaf_image_view);
        roundProgress = (RoundProgressView) findViewById(R.id.leaf_round_progress);

        scaleCircleView.setVisibility(View.VISIBLE);
        scaleCircleView.startAnim();
        textView.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        roundProgress.setVisibility(View.GONE);
    }

    /**
     * 设置progress进度和值
     * @param progress
     */
    public void setDownloadProgress(int progress){
        roundProgress.setProgress(progress);
        textView.setText(progress + "%");
    }




    public ScaleCircleView getScaleCircleView() {
        return scaleCircleView;
    }


    public ImageView getImageView() {
        return imageView;
    }


    public TextView getTextView() {
        return textView;
    }



    public RoundProgressView getRoundProgress() {
        return roundProgress;
    }

    public void setLeafOnClickListener(LeafOnClickListener l){
        this.leafOnClickListener = l;
        setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (state){
            case CHECK_VERSION:
                leafOnClickListener.checkVersion();
                break;
            case DOWNLOAD_VERSION:
                leafOnClickListener.downloadVersion();
                break;
            case UPGRADE_VERSION:
                leafOnClickListener.rebootUpgrade();
                 break;
        }
    }

    public static interface LeafOnClickListener{
        void checkVersion();

        void downloadVersion();

        void rebootUpgrade();
    }

    public void setState(int state) {
        this.state = state;
    }
}
