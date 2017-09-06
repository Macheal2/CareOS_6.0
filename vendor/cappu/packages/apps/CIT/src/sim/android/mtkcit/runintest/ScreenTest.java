package sim.android.mtkcit.runintest;

import java.util.Timer;
import java.util.TimerTask;


import android.os.Bundle;

import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import sim.android.mtkcit.RunInTest;

public class ScreenTest extends RuninBaseActivity {

	private boolean debugFlag = true;
	private final String TAG = "ScreenTest";

	private LCDView lcdView;
	Timer timer;
	private KeyguardLock mLock;
	KeyguardManager keyguardManager;
	WakeLock screenTestWakeLock;
	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setFullscreen();
		lcdView = new LCDView(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(lcdView);
		mTimerTask mtask = new mTimerTask();
		timer.schedule(mtask, 0, 1000);
	}

	@Override
	protected void onResume() {

		super.onResume();
		keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		mLock = keyguardManager.newKeyguardLock(this.getClass().getName());
		mLock.disableKeyguard();
	}

	/*
	 * private void initRec() { //new Thread(new mThread()).start();
	 * LOGV(debugFlag, TAG, "initRec="); //pm = (PowerManager)
	 * getSystemService(Context.POWER_SERVICE);
	 * 
	 * }
	 * 
	 * class mThread implements Runnable {
	 * 
	 * @Override public void run() { while (mThreadFlag) { try {
	 * Thread.sleep(1000); } catch (InterruptedException e) { LOGV(debugFlag,
	 * TAG, "mThread error "); return; } currentTime =
	 * System.currentTimeMillis(); LOGV(debugFlag, TAG, "currentTime=" +
	 * currentTime); LOGV(debugFlag, TAG, "stoptime=" + RunInTest.stopTime);
	 * 
	 * if (currentTime > RunInTest.stopTime) { mThreadFlag = false; stopTest();
	 * } } } }
	 */
	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");

		super.onStop();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		this.finish();

	}

	private void stopTest() {
		LOGV(debugFlag, TAG, "stopTest()");
		SharedPreferences settings = getSharedPreferences(RunInTest.PREFS_NAME,
				0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("test_complate", true);
		editor.commit();
		finish();
	}

	/*
	 * 
	 * 
	 * private void releaseResource() { mThreadFlag = false;
	 * 
	 * }
	 */
	private void closeScreen() {
		LOGV(debugFlag, TAG, "closeScreen");
		screenTestWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getName());
		screenTestWakeLock.acquire();
	}

	private void openScreen() {
		LOGV(debugFlag, TAG, "openScreen");
		if (screenTestWakeLock != null && screenTestWakeLock.isHeld()) {
			screenTestWakeLock.release();
		}
	}

	class LCDView extends View {
		private static final int C_CLOSE = 1;

		private static final int C_OOPEN = 2;

		private static final int C_WHITE = 3;

		private static final int C_BLACK = 4;

		private static final int C_RED = 5;

		private static final int C_GREEN = 6;

		private static final int C_BLUE = 7;
		private int m_nCurrentPage = 1;

		public LCDView(Context context) {

			super(context);
			m_nCurrentPage = 1;
		}

		private void palette_Display(Canvas canvas, int i) {
			LOGV(debugFlag, TAG, "i=" + i);
			switch (i) {

			case C_CLOSE:
				closeScreen();
				break;
			case C_OOPEN:
				openScreen();
				break;

			case C_WHITE:
				canvas.drawColor(Color.WHITE);
				break;
			case C_BLACK:
				canvas.drawColor(Color.BLACK);
				break;
			case C_RED:
				canvas.drawColor(Color.RED);
				break;
			case C_GREEN:
				canvas.drawColor(Color.GREEN);
				break;
			case C_BLUE:
				canvas.drawColor(Color.BLUE);
				cyleTest();
				break;
			default:
				LOGV(debugFlag, TAG, "default  break;");
				break;
			}
			m_nCurrentPage++;

		}

		protected void onDraw(Canvas canvas) {

			LOGV(debugFlag, TAG, "onDraw,,m_nCurrentPage = " + m_nCurrentPage);
			palette_Display(canvas, m_nCurrentPage);
		}

	}

	public ScreenTest() {

		timer = new Timer();
	}

	private void setFullscreen() {
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
	}

	class mTimerTask extends TimerTask {

		public void run() {
			try {
				Thread.sleep(500L);
				Log.v("timetask", "run()");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v("timetask", "timetask  error");
				return;
			}
			lcdView.postInvalidate();
		}

	}

	@Override
	void cyleTest() {

		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, MVPlayerTest.class);
		startActivity(intent);

	}
}
