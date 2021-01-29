package com.project.hmsscan.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.project.hmsscan.R;

public class ScanWechatView extends BaseScanView {

    private int scanMaginWith;
    private int scanMaginheight;

    private Canvas canvas;
    private Paint paintCenter;
    private Paint paint;
    private Bitmap scanLine;
    private Rect scanRect;
    private Rect lineRect;

    //扫描线位置
    private int scanLineTop;
    //透明度
    private int alpha = 100;

    private int bitmapHigh;

    private int radiusCenter = 50;

    public ScanWechatView(Context context) {
        super(context);
        init();
    }

    public ScanWechatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanWechatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scanLine = BitmapFactory.decodeResource(getResources(), R.drawable.scan_wechatline);

        bitmapHigh = scanLine.getHeight();

        scanRect = new Rect();
        lineRect = new Rect();
    }

    public void drawCenter(PointF pointf) {
        if (null != canvas) {
            paintCenter.setColor(Color.WHITE);
            canvas.drawCircle(pointf.x + scanRect.left - radiusCenter / 1f, pointf.y + scanRect.top - radiusCenter / 1f, radiusCenter + 5, paintCenter);
            paintCenter.setColor(Color.GREEN);
            canvas.drawCircle(pointf.x + scanRect.left - radiusCenter / 1f, pointf.y + scanRect.top - radiusCenter / 1f, radiusCenter - 5, paintCenter);
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scanMaginWith = getMeasuredWidth() / 10;
        scanMaginheight = getMeasuredHeight() >> 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        scanRect.set(scanMaginWith, scanMaginheight, getWidth() - scanMaginWith, getHeight() - scanMaginheight);
        startAnim();
        paint.setAlpha(alpha);
        lineRect.set(scanMaginWith, scanLineTop, getWidth() - scanMaginWith, scanLineTop + bitmapHigh);
        canvas.drawBitmap(scanLine, null, lineRect, paint);
    }

    @Override
    public void startAnim() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(scanRect.top, scanRect.bottom);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setDuration(4000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scanLineTop = (int) animation.getAnimatedValue();
                    int startHideHeight = (scanRect.bottom - scanRect.top) / 6;
                    alpha = scanRect.bottom - scanLineTop <= startHideHeight ? (int) (((double) (scanRect.bottom - scanLineTop) / startHideHeight) * 100) : 100;
                    postInvalidate();
                }
            });
            valueAnimator.start();
        }
    }

    @Override
    public void cancelAnim() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}