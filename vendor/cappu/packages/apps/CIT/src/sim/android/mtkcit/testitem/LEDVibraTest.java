package sim.android.mtkcit.testitem;

import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.featureoption.SimcomFeatureOption;

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
import sim.android.mtkcit.testitem.LCDTest.timetask;

public class LEDVibraTest extends TestBase  {
	private PowerManager.WakeLock mWakeLock;
	Timer timer;
	private KeyguardLock mLock;
	private Vibrator vibrator;
	private boolean ledflag;
	private int m_nCurrentLED = 2;
	private NotificationManager nm;
	private int ID_LED = 333;
	private String TAG = "LEDVibraTest";
	private PowerManager pm;
	private MTask mtask;
	private Button btn_success;
	private Button btn_fail;
	private TextView tv;
	private Notification notification;
         private CITTools ct ;
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.test_ledvibra);
		tv = (TextView) findViewById(R.id.test_title);
		// if (SimcomFeatureOption.FMWK_ACER_C8_SHOW_LED) {
		// tv.setText(R.string.cit_ledvibra_test_c8);
		// }
		ct = CITTools.getInstance(this);
		String testItem = ct.getStringFromRes(this, 5);
		tv.setText(testItem);

		// String s = ct.getStringFromRes(this, 13);
	}

	@Override
	protected void onStart() {
		pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"SimpleTimer");
		KeyguardManager mKeyGuardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		mLock = mKeyGuardManager.newKeyguardLock("LEDVibraTest");
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);


		initRec();
		super.onStart();
	}

	@Override
	protected void onStop() {
		releaseRec();
		super.onStop();
	}

	private void vibrationTest() {

		vibrator = (Vibrator) getSystemService("vibrator");
		long al[] = { 0l, 1000l, 1000l };
		vibrator.vibrate(al, 0);
	}

	class MTask extends TimerTask {

		public void run() {
			while(ledflag) {
				try {
					Thread.sleep(500L);
					Log.v(TAG, "run()");

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.v("timetask", "timetask  error");
					return;
				}
				showLED(m_nCurrentLED);
				m_nCurrentLED++;
				if (m_nCurrentLED == 3) {
					m_nCurrentLED = 0;
				}
			}

		}

		private void showLED(int m_nCurrentLED) {
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			 notification = new Notification();
			notification.flags = Notification.FLAG_SHOW_LIGHTS;
			// added by bruce for LED test begin
			notification.icon = R.drawable.icon;
			notification.setLatestEventInfo(LEDVibraTest.this, "", "", null); 
			// added by bruce for LED test end
			if (m_nCurrentLED == 0) {

				notification.ledARGB = Color.GREEN;
				nm.notify(ID_LED, notification);
			}
			if (m_nCurrentLED == 1) {
	
				notification.ledARGB = Color.BLUE;
				nm.notify(ID_LED, notification);
			}

			if (m_nCurrentLED == 2) {
				notification.ledARGB = Color.RED;
				nm.notify(ID_LED, notification);
			}
			if(!ledflag)  {
				nm.cancelAll();
			}
		}
  
	}
   int  fxy ;
	private void LEDTest() {
		ledflag = true;

		timer = new Timer();
		 mtask= new MTask();
		timer.schedule(mtask, 0, 500);
	}

	public void onClick(View button) {
		if (!button.isEnabled())
			return;

		int id = button.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}
		fxy=1;
		releaseRec();
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	public void releaseRec() {
		ledflag = false;


		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}

		if (timer != null) {
			timer.cancel();
			timer = null;
			
		}
		if(mtask!=null)  {
			mtask.cancel();
			mtask=null;
		}
		if (mLock != null) {
			mLock.reenableKeyguard();
			mLock = null;
		}
		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
		if (nm != null) {
			nm.cancelAll();
			nm = null;
		}
	}

	private void initRec() {
		mLock.disableKeyguard();
		Log.v(TAG, "mWakeLock.acquire();");
		mWakeLock.acquire();
	    //vibrationTest();
		LEDTest();
	}
}
