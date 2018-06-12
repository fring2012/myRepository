package com.example.administrator.ui_make.ui.branchview.pieceview;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;


public class RoundTextView extends AppCompatTextView {
    private static final String TAG = "RoundTextView";
    private int lineWidth;
    private RectF rectF;
    private int currentProgress = 100;
    private int progressWidth;
    private int radii;
    private RectF oval;
    private int scaleDensity = 5;
    private int[] centre; //圆心坐标 cx = centre[0] cy = centre[1]
    private int[] textFrame; //文本框坐标



    public RoundTextView(Context context) {
        super(context);
    }

    public RoundTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG,"onDraw(Canvas canvas)");
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setARGB(0xFF,3,95,154);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(lineWidth,getHeight()/2 - 0.8f * radii,getWidth() - getHeight()/2 - 0.6f * radii,getHeight()/2 - 0.8f * radii, paint);
        canvas.drawLine(lineWidth,getHeight()/2 + radii ,getWidth()-getHeight()/2 , getHeight()/2+radii,paint);
        canvas.drawLine(lineWidth,getHeight()/2 - 0.8f * radii - lineWidth / 2 ,lineWidth,getHeight()/2+radii + lineWidth / 2,paint);
        canvas.drawCircle(centre[0] ,centre[1]  ,radii ,paint);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        lineWidth = height / 15 ; //线条粗细
        if(width > height){
            radii = height / 2  - lineWidth/2;
        }else {
            radii = width / 2 - lineWidth/2;
        }
        setCentre(width,height);

        lineWidth = radii / 15;
        radii = radii - progressWidth / 2;
    }


    public int[] getCentre() {
        return centre;
    }

    public void setCentre(int width,int height) {
        centre = new int[]{width - radii - lineWidth/2 , height/2};
    }

    public int[] getTextFrame() {
        return textFrame;
    }

    public void setTextFrame(int width,int height) {
        textFrame = new int[]{1,1};
        this.textFrame = textFrame;
    }
}
