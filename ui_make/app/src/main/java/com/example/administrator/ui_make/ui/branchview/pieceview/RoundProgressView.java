package com.example.administrator.ui_make.ui.branchview.pieceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RoundProgressView extends View {
    private static final String TAG = "RoundProgress";
    private int lineWidth;
    private int progress = 0;
    private RectF oval;

    public RoundProgressView(Context context) {
        super(context);
    }

    public RoundProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        oval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw(Canvas canvas)");
        Paint paint = new Paint();
        paint.setStrokeWidth(lineWidth);
        paint.setAntiAlias(true);
        paint.setARGB(0xFF,1,106,170);
        paint.setStyle(Style.STROKE);
        canvas.drawArc(oval,-90,progress  * 3.6f,false,paint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        lineWidth = width / 12;
        oval.left = lineWidth/2;
        oval.top = lineWidth/2;
        oval.bottom = width - lineWidth / 2;
        oval.right = width - lineWidth /2;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        Log.d(TAG,progress + "");
        if(progress >=0 && progress <= 100) {
            this.progress = progress;
            invalidate();
        }
    }
}
