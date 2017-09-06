package com.cappu.halllockscreen;

import android.app.Activity;
//import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
//import android.provider.Settings.System;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.cappu.halllockscreen.util.GestureHelper;
import com.cappu.halllockscreen.util.HallTimeLog;

public class HallLockScreenActivity extends Activity {
	public static final String FLIP_STATE = "flip_state";
	private static int FLIP_STATE_CLOSE = 0;
	private static int FLIP_STATE_OPEN = 1;
	public static final String TAG_PREFIX = "LeatheLockScreenAct";
	private View mBg;
	public ContentObserver mFlipStateChangeObserver;
	GestureHelper mGestureHelper;
	RollingRing mRing;
	private static HallLockScreenActivity mg_instance = null;

	public static HallLockScreenActivity get_mg_instance() {
		return mg_instance;
	}

	public void finshWindowLife() {
		if (this.mGestureHelper != null) {
			this.mGestureHelper.onStop(this);
		}
		if (this.mFlipStateChangeObserver != null) {
			getContentResolver().unregisterContentObserver(
					this.mFlipStateChangeObserver);
		}
		finish();
		overridePendingTransition(-1, -1);
		mg_instance = null;// weiyawei add
	}

	private boolean isFlipClosed() {
		// return Settings.System.getInt(getContentResolver(), "flip_state",
		// FLIP_STATE_OPEN) == FLIP_STATE_CLOSE;
		return true;// hejianfeng add
	}

	private void startLockScrennTimeWindow() {
		Intent localIntent = new Intent();
		localIntent.setClassName("com.nbbsw.lockscreen",
				"com.nbbsw.lockscreen.LockScreenViewActivity");
		localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(localIntent);
		overridePendingTransition(-1, -1);
	}

	public void onAttachedToWindow() {
		HallTimeLog.d("LeatheLockScreenAct", "onAttachedToWindow");
		super.onAttachedToWindow();
	}

	public void onBackPressed() {
	}

	public void onCreate(Bundle paramBundle) {
		overridePendingTransition(-1, -1);
		super.onCreate(paramBundle);
		 //无title    
        requestWindowFeature(Window.FEATURE_NO_TITLE);    
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						|WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(R.layout.main_circular_clock);// hejianfeng modif
		mg_instance = this;
		this.mBg = findViewById(R.id.bird_hall_time_screen);
		if (BirdFeatureOption.supportCircularRing()) {
			ViewStub localViewStub = (ViewStub) findViewById(R.id.window_rolling_ring_stub);
			if (localViewStub != null) {
				localViewStub.inflate();
				this.mRing = ((RollingRing) findViewById(R.id.window_rolling_ring));
			}
		}
		if (BirdFeatureOption.supportColorfulBackground()) {
			this.mBg.setBackgroundResource(R.drawable.hall_window_bg);
		}
		if (BirdFeatureOption.isWindowCircular()) {
			this.mGestureHelper = new GestureHelper(this, this.mBg);
		}
	}

	protected void onDestroy() {
		HallTimeLog.v("LeatheLockScreenAct", "onDestroy");
		super.onDestroy();
	}

	public void onDetachedFromWindow() {
		HallTimeLog.d("LeatheLockScreenAct", "onDetachedFromWindow");
		super.onDetachedFromWindow();
	}

	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		if (paramInt == 229) {
			finshWindowLife();
		}
		return super.onKeyDown(paramInt, paramKeyEvent);
	}

	protected void onResume() {
		super.onResume();
		HallTimeLog.v("LeatheLockScreenAct", "onResume");
		if (!isFlipClosed()) {
			finshWindowLife();
			overridePendingTransition(-1, -1);
		}
		if (this.mRing != null) {
			this.mRing.onResume();
		}
	}
}
