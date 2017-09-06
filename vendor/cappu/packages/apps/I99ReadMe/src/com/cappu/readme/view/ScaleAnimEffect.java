package com.cappu.readme.view;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * 缩放
 * 
 * @author
 * */
public class ScaleAnimEffect {
	private long duration;
	private float fromAlpha;
	private float fromXScale;
	private float fromYScale;
	private float toAlpha;
	private float toXScale;
	private float toYScale;

	public Animation alphaAnimation(float paramFloat1, float paramFloat2, long paramLong1, long paramLong2) {
		AlphaAnimation localAlphaAnimation = new AlphaAnimation(paramFloat1, paramFloat2);
		localAlphaAnimation.setDuration(paramLong1);
		localAlphaAnimation.setStartOffset(paramLong2);
		localAlphaAnimation.setInterpolator(new AccelerateInterpolator());
		return localAlphaAnimation;
	}

	public Animation createAnimation() {
		ScaleAnimation localScaleAnimation = new ScaleAnimation(this.fromXScale, this.toXScale, this.fromYScale, this.toYScale, 1, 0.5F, 1, 0.5F);
		localScaleAnimation.setFillAfter(true);
		localScaleAnimation.setInterpolator(new AccelerateInterpolator());
		localScaleAnimation.setDuration(this.duration);
		return localScaleAnimation;
	}

	public void setAttributs(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, long paramLong) {
		this.fromXScale = paramFloat1;
		this.fromYScale = paramFloat3;
		this.toXScale = paramFloat2;
		this.toYScale = paramFloat4;
		this.duration = paramLong;
	}
}