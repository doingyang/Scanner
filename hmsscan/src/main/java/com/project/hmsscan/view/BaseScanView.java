package com.project.hmsscan.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class BaseScanView extends View {

    protected ValueAnimator valueAnimator;

    public BaseScanView(Context context) {
        super(context);
    }

    public BaseScanView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseScanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startAnim() {
    }

    public void cancelAnim() {
    }
}