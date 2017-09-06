package sim.android.mtkcit.runintest;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.SystemProperties;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.R;
import sim.android.mtkcit.RunInTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITTools;

public class VibratorTest extends RuninBaseActivity {

	private boolean debugFlag = true;
	private final String TAG = "VibratorTest";
	private boolean testFlas = true;

	private PowerManager.WakeLock mWakeLock;
	Timer timer;
	private KeyguardLock mLock;
	private Vibrator vibrator;
	private boolean ledflag;
	private int m_nCurrentLED;
	private NotificationManager nm;
	private int ID_LED = 333;
	private PowerManager pm;
	private TextView tv;
	private Notification notification;


	@Override
	protected void onCreate(Bundle bundle) {

		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.run_in_simple_test);

	}

	@Override
	protected void onResume() {
		initRec();
		super.onResume();
	}

	private void initRec() {

		new Thread(new mThread()).start();
		vibrationTest();
	}

	private void vibrationTest() {

		vibrator = (Vibrator) getSystemService("vibrator");
		long al[] = { 1000l, 1000l };
		vibrator.vibrate(al, 0);
	}

	public void releaseRec() {
		ledflag = false;

		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}

	}

	@Override
	void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, MainCameraTest.class);
		startActivity(intent);
		this.finish();
	}
	
	class mThread implements Runnable {
		@Override
		public void run() {
			LOGV(debugFlag, TAG, "run() ");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOGV(debugFlag, TAG, "mThread error ");
				return;
			}
//			currentTime = System.currentTimeMillis();
//			LOGV(debugFlag, TAG, "currentTime=" + currentTime);
//			if (currentTime > RunInTest.stopTime) {
//				stopTest();
//
//			} else

                //modify by even
                releaseRec();
				cyleTest();

		}
	}
}
