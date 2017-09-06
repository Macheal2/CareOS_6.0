package com.cappu.halllockscreen.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import com.cappu.halllockscreen.AnalogClock;
import com.cappu.halllockscreen.DigitalClock;
//import com.cappu.halllockscreen.RollingRing;
import com.cappu.halllockscreen.anim.CustomRotateAnimation;
import com.cappu.halllockscreen.R;
import android.provider.Settings;

public class GestureHelper implements View.OnTouchListener,
		Animation.AnimationListener {
	public static final int ANALOG_CLOCK_COUNT = 2;
	public static final int ANALOG_CLOCK_DEFAULT = 0;
	public static final int BIRD_HALL_TIME_STYLE_ANALOG_BLUE = 1;
	public static final int BIRD_HALL_TIME_STYLE_ANALOG_SILVER = 0;
	public static final int BIRD_HALL_TIME_STYLE_DIGITAL = 99;
	private static final int FLING_MIN_DISTANCE = 120;
	private static final int FLING_MIN_VELOCITY = 200;
	public static final String TAG_PREFIX = "GestureHelper";
	AnalogClock mAnalogTime;
	View mContainer;
	Context mContext;
	int mCurrentTimeStyle;
	DigitalClock mDigitalTime;
	private boolean mDuringAnimate = false;
	GestureDetector mGestureDetector;
	SwitchGesture mGestureListener = new SwitchGesture();

	// RollingRing mRing;

	public GestureHelper(Context paramContext, View paramView) {
		this.mContext = paramContext;
		this.mContainer = paramView.findViewById(R.id.bird_time_container);
		// this.mRing =
		// ((RollingRing)paramView.findViewById(R.id.window_rolling_ring));
		this.mDigitalTime = ((DigitalClock) paramView
				.findViewById(R.id.time_digital));
		this.mAnalogTime = ((AnalogClock) paramView
				.findViewById(R.id.time_analog));
		HallTimeLog
				.v("jeff", "hejianfeng mDigitalTime id=" + R.id.time_digital);
		HallTimeLog.v("jeff", "hejianfeng mDigitalTime=" + mDigitalTime);
		this.mGestureDetector = new GestureDetector(this.mGestureListener);
		this.mCurrentTimeStyle = getInitStyle(paramView.getContext());
		if (this.mCurrentTimeStyle == 99) {
			this.mDigitalTime.setVisibility(View.VISIBLE);
			this.mAnalogTime.setVisibility(View.GONE);
		} else {
			this.mDigitalTime.setVisibility(View.GONE);
			this.mAnalogTime.setVisibility(View.VISIBLE);
			this.mAnalogTime.setCurrentAnalogStyle(this.mCurrentTimeStyle);
		}
		this.mDigitalTime.setOnTouchListener(this);
		this.mAnalogTime.setOnTouchListener(this);
	}

	private void applyRotation(boolean paramBoolean, float paramFloat1,
			float paramFloat2) {
		if (this.mDuringAnimate) {
			return;
		}
		CustomRotateAnimation localCustomRotateAnimation = new CustomRotateAnimation(
				paramBoolean, paramFloat1, paramFloat2,
				this.mContainer.getWidth() / 2.0F,
				this.mContainer.getHeight() / 2.0F, 310.0F, true);
		localCustomRotateAnimation.setDuration(500L);
		localCustomRotateAnimation.setFillAfter(true);
		localCustomRotateAnimation
				.setInterpolator(new AccelerateInterpolator());
		localCustomRotateAnimation.setAnimationListener(this);
		this.mContainer.startAnimation(localCustomRotateAnimation);
	}

	private boolean currentAnalog() {
		return this.mAnalogTime.getVisibility() == View.VISIBLE;
	}

	public static int getInitStyle(Context paramContext) {
		return Settings.Global.getInt(paramContext.getContentResolver(),
                "hall_ui_display",99);
	}

	public static void storeStyle(Context paramContext, int paramInt) {
//		Settings.System.putInt(paramContext.getContentResolver(), "hall_ui_display",
//				paramInt);
	}

	private void switchAnalogStyle() {
		if (this.mAnalogTime.getVisibility() == View.GONE) {
			if (this.mDigitalTime != null) {
				DigitalClock localDigitalClock = this.mDigitalTime;
				localDigitalClock.setVisibility(View.GONE);
			}
			if (this.mAnalogTime != null) {
				AnalogClock localAnalogClock = this.mAnalogTime;
				localAnalogClock.setVisibility(View.VISIBLE);
			}
		}
		this.mAnalogTime.changeStyle();
		this.mCurrentTimeStyle = this.mAnalogTime.getCurrentAnalogStyle();
		storeStyle(this.mContext, this.mCurrentTimeStyle);
		HallTimeLog.d("GestureHelper", "switchAnalogStyle,mCurrentTimeStyle="
				+ this.mCurrentTimeStyle);
	}

	private void switchTimeUI() {
		if (this.mDigitalTime.getVisibility() == View.GONE) {
			if (this.mDigitalTime != null) {
				DigitalClock localDigitalClock = this.mDigitalTime;
				localDigitalClock.setVisibility(View.VISIBLE);
				this.mCurrentTimeStyle = 99;
			}
			if (this.mAnalogTime != null) {
				AnalogClock localAnalogClock = this.mAnalogTime;
				localAnalogClock.setVisibility(View.GONE);
			}
		} else {
			if (this.mDigitalTime != null) {
				DigitalClock localDigitalClock = this.mDigitalTime;
				localDigitalClock.setVisibility(View.GONE);
			}
			if (this.mAnalogTime != null) {
				AnalogClock localAnalogClock = this.mAnalogTime;
				localAnalogClock.setVisibility(View.VISIBLE);
				this.mCurrentTimeStyle = this.mAnalogTime
						.getCurrentAnalogStyle();
			}
		}
		storeStyle(this.mContext, this.mCurrentTimeStyle);
		HallTimeLog.d("GestureHelper", "switchTimeUI,mCurrentTimeStyle="
				+ this.mCurrentTimeStyle);
	}

	public void onAnimationEnd(Animation paramAnimation) {
		this.mDuringAnimate = false;
		CustomRotateAnimation localCustomRotateAnimation1 = (CustomRotateAnimation) paramAnimation;
		boolean bool = localCustomRotateAnimation1.currentRotateY();
		float f1 = this.mContainer.getWidth() / 2.0F;
		float f2 = this.mContainer.getHeight() / 2.0F;
		HallTimeLog.v("jeff", "onAnimationEnd");
		if (bool) {
			switchTimeUI();
		} else {
			switchAnalogStyle();
		}
		CustomRotateAnimation localCustomRotateAnimation2 = new CustomRotateAnimation(
				bool, localCustomRotateAnimation1.getOldEndAngle(),
				localCustomRotateAnimation1.getNewEndAngle(), f1, f2, 620.0F,
				false);
		localCustomRotateAnimation2.setDuration(500L);
		localCustomRotateAnimation2.setFillAfter(true);
		localCustomRotateAnimation2
				.setInterpolator(new DecelerateInterpolator());
		this.mContainer.startAnimation(localCustomRotateAnimation2);
	}

	public void onAnimationRepeat(Animation paramAnimation) {
	}

	public void onAnimationStart(Animation paramAnimation) {
		this.mDuringAnimate = true;
	}

	public void onStop(Context paramContext) {
		storeStyle(paramContext, this.mCurrentTimeStyle);
	}

	public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
		this.mGestureDetector.onTouchEvent(paramMotionEvent);
		return true;
	}

	class SwitchGesture extends GestureDetector.SimpleOnGestureListener {
		public SwitchGesture() {
			HallTimeLog.d("GestureHelper", "SwitchGesture--------.");
		}

		public boolean onDoubleTap(MotionEvent paramMotionEvent) {
			HallTimeLog.d("GestureHelper", "onDoubleTap--------.");
			return true;
		}

		public boolean onDoubleTapEvent(MotionEvent paramMotionEvent) {
			return true;
		}

		public boolean onDown(MotionEvent paramMotionEvent) {
			return true;
		}

		public boolean onFling(MotionEvent paramMotionEvent1,
				MotionEvent paramMotionEvent2, float paramFloat1,
				float paramFloat2) {
			float f1 = 90.0F;
			float f2;// hejianfeng add
			GestureHelper localGestureHelper2;// hejianfeng add
			HallTimeLog.d("jeff", "X="
					+ (paramMotionEvent1.getX() - paramMotionEvent2.getX()));
			HallTimeLog.d("jeff", "Y="
					+ (paramMotionEvent1.getY() - paramMotionEvent2.getY()));
			HallTimeLog.d("jeff", "paramFloat1=" + paramFloat1);
			HallTimeLog.d("jeff", "paramFloat2=" + paramFloat2);
			String str2;
			String str1;
			GestureHelper localGestureHelper1;
			if ((Math.abs(paramMotionEvent1.getX() - paramMotionEvent2.getX()) > 240.0F)
					&& (paramFloat1 < -400.0F)) {
				str2 = "left";
				HallTimeLog.d("GestureHelper", str2);
				localGestureHelper2 = GestureHelper.this;
				f2 = -90.0F;
				localGestureHelper2.applyRotation(true, 0.0F, f2);
			} else if ((Math.abs(paramMotionEvent2.getX()
					- paramMotionEvent1.getX()) > 240.0F)
					&& (paramFloat1 > 400.0F)) {
				str2 = "right";
				HallTimeLog.d("GestureHelper", str2);
				localGestureHelper2 = GestureHelper.this;
				f2 = 90.0F;
				localGestureHelper2.applyRotation(true, 0.0F, f2);
			} else if ((Math.abs(paramMotionEvent2.getY()
					- paramMotionEvent1.getY()) > 240.0F)
					&& (paramFloat2 > 400.0F)
					&& GestureHelper.this.currentAnalog()) {
				str1 = "down";
				HallTimeLog.d("GestureHelper", str1);
				localGestureHelper1 = GestureHelper.this;
				f1 = -90.0F;
				localGestureHelper1.applyRotation(false, 0.0F, f1);
			} else if ((Math.abs(paramMotionEvent1.getY()
					- paramMotionEvent2.getY()) > 240.0F)
					&& (paramFloat2 < -400.0F)
					&& GestureHelper.this.currentAnalog()) {
				str1 = "up";
				HallTimeLog.d("GestureHelper", str1);
				localGestureHelper1 = GestureHelper.this;
				f1 = 90.0F;
				localGestureHelper1.applyRotation(false, 0.0F, f1);
			}
			return false;
		}

		public void onLongPress(MotionEvent paramMotionEvent) {
		}

		public boolean onScroll(MotionEvent paramMotionEvent1,
				MotionEvent paramMotionEvent2, float paramFloat1,
				float paramFloat2) {
			return true;
		}

		public void onShowPress(MotionEvent paramMotionEvent) {
		}

		public boolean onSingleTapConfirmed(MotionEvent paramMotionEvent) {
			return true;
		}

		public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
			return true;
		}
	}
}
