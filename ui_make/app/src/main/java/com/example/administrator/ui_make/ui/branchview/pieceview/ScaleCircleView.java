package com.example.administrator.ui_make.ui.branchview.pieceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.example.administrator.ui_make.R;


public class ScaleCircleView extends View {
    private static final String TAG = "CircleView";
    private int currentProgress = 100;
    private int progressWidth;
    private int radii;
    private RectF oval;
    private int scaleDensity = 5;
    private int scaleWidth;  //刻度长度

    private Animation animation;





    public ScaleCircleView(Context context) {
        super(context);
    }

    public ScaleCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        oval = new RectF();
        animation = AnimationUtils.loadAnimation(context, R.anim.scale_circle_rotate);
        animation.setInterpolator(new LinearInterpolator());//插入现行插值器
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();

        paint.setAntiAlias(true);

        paint.setStrokeWidth(progressWidth);
        paint.setStyle(Style.STROKE);

        for (int i = 0; i < currentProgress; i++){
            int a = 0xFF - i * 3;
            if(a < 0){
                a = 0;
            }
            paint.setARGB(a,255,255,255);
            if (i % scaleDensity == 0){
                canvas.drawArc(oval,270 - i * 3.6f, -scaleWidth ,false,paint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width >= height) {
            radii = height / 2;
        }else {
            radii = width / 2;
        }
        setMeasuredDimension(radii * 2,radii * 2);

        progressWidth = radii / 4;

        oval.left = progressWidth * 2;
        oval.top = progressWidth * 2;
        oval.right = radii * 2 - progressWidth * 2;
        oval.bottom = radii * 2 - progressWidth *  2;

        scaleWidth = 360 * scaleDensity / 200;

    }


    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int progress) {
        if(progress >= 0 && progress <= 100) {
            currentProgress = progress;
            invalidate();
        }
    }

    /***
     * 开启动画
     */
    public void startAnim(){
        this.clearAnimation();
        this.startAnimation(animation);
    }

    public void stopAnim(){
        this.clearAnimation();
    }

    public int getProgressWidth() {
        return progressWidth;
    }

    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
    }

    public int getRadii() {
        return radii;
    }

    public void setRadii(int radii) {
        this.radii = radii;
    }

    public RectF getOval() {
        return oval;
    }

    public void setOval(RectF oval) {
        this.oval = oval;
    }

    public int getScaleDensity() {
        return scaleDensity;
    }

    public void setScaleDensity(int scaleDensity) {
        this.scaleDensity = scaleDensity;
    }

    public int getScaleWidth() {
        return scaleWidth;
    }

    public void setScaleWidth(int scaleWidth) {
        this.scaleWidth = scaleWidth;
    }
}
