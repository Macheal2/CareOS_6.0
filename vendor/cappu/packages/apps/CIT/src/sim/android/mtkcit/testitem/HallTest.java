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

public class HallTest extends TestBase{
	private PowerManager.WakeLock mWakeLock;
	Timer timer;
	private KeyguardLock mLock;
	private Vibrator vibrator;
	private boolean ledflag;
	private int m_nCurrentLED;
	private NotificationManager nm;
	private int ID_LED = 333;
	private String TAG = "HallTest";
	private PowerManager pm;
	private TextView tv;
	private Notification notification;
	private Button btn_success;
	private Button btn_fail;
	private CITTools ct;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.simple_test);
		tv = (TextView) findViewById(R.id.test_title);
		ct = CITTools.getInstance(this);

//		String testItem = ct.getStringFromRes(this, 32);
		String testItem = getString(R.string.hall_test_text);
		tv.setText(testItem);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		initRec();
		super.onStart();
	}

	private void initRec() {

//		vibrationTest();
	}

	@Override
	protected void onStop() {
		releaseRec();
		super.onStop();
	}

	private void vibrationTest() {

		vibrator = (Vibrator) getSystemService("vibrator");
		long al[] = { 1000l, 2000l };
		vibrator.vibrate(al, 0);
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
		releaseRec();
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	public void releaseRec() {
		ledflag = false;

//		if (vibrator != null) {
//			vibrator.cancel();
//			vibrator = null;
//		}

	}

}
