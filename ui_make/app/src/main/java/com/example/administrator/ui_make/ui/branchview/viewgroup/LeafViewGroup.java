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

public class LeafViewGroup extends RelativeLayout {
    private static final String TAG = "LeafViewGroup";



    private ScaleCircleView scaleCircleView;
    private ImageView imageView;
    private TextView textView;
    private RoundProgressView roundProgress;



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


}
