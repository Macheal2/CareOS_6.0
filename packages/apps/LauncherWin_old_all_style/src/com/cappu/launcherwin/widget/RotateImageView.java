package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class RotateImageView extends ImageView {
    private Animation mRotateAnimation;
    private boolean isAnim;

    public RotateImageView(Context context) {
        this(context, null);
        mRotateAnimation = AnimationUtils.loadAnimation(context, R.anim.refresh_rotate);
        isAnim = false;
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mRotateAnimation = AnimationUtils.loadAnimation(context, R.anim.refresh_rotate);
        isAnim = false;
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRotateAnimation = AnimationUtils.loadAnimation(context, R.anim.refresh_rotate);
        isAnim = false;
    }

    public boolean isStartAnim() {
        return isAnim;
    }

    public synchronized void startAnim() {
        stopAnim();
        this.startAnimation(mRotateAnimation);
        isAnim = true;

    }

    public synchronized void stopAnim() {
        if (isAnim) {
            this.clearAnimation();
            isAnim = false;
        }
    }
}
