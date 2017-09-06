package sim.android.mtkcit.runintest;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.RunInTest;
import android.os.PowerManager;
import android.util.Log;
import android.os.PowerManager.WakeLock;
import android.content.Context;

abstract class RuninBaseActivity extends Activity {

	public static long diruction;
	public static long beginTime;
	public static long currentTime;

	public boolean mThreadFlag;
	private boolean debugFlag = true;
	private final String TAG = "RuninBaseActivity";
	public CITTools ct;
	private boolean testFlas = true;
	public PowerManager pm;
	public WakeLock mWakeLock;

	abstract void cyleTest();

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
        //modify by even
        if (currentTime > RunInTest.stopTime) {
				stopTest();

		}
        //end

	}

	@Override
	protected void onResume() {
		super.onResume();

		mThreadFlag = true;
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//pm.setBacklightBrightnessOff(false);
		pm.setBacklightBrightness(200);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
				.getClass().getName());
		mWakeLock.acquire();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		new Thread(new mThread()).start();
		ct = CITTools.getInstance(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.v(TAG, "onStop");
		mThreadFlag = false;
		if (mWakeLock != null) {
			mWakeLock.release();
		}
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

	public void LOGV(boolean debugFlag, String tAG, String string) {
		if (debugFlag) {
			Log.v(TAG, string);
		}
	}

	public class mThread implements Runnable {
		@Override
		public void run() {
			LOGV(debugFlag, TAG, "run() ");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOGV(debugFlag, TAG, "mThread error ");
				return;
			}
			currentTime = System.currentTimeMillis();
			LOGV(debugFlag, TAG, "currentTime=" + currentTime);
			LOGV(debugFlag, TAG, "RunInTest.stopTime=" + RunInTest.stopTime);

			if (currentTime > RunInTest.stopTime) {
				stopTest();

			}
			// else cyleTest();

		}

	}
}
