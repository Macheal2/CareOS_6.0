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
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import sim.android.mtkcit.R;
import sim.android.mtkcit.AutoTestActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.testitem.LCDTest.timetask;

public class TestBase extends Activity implements View.OnClickListener {

	public Button btn_success;
	public Button btn_fail;
	public CITTools ct;
	public PowerManager pm;
	public WakeLock mWakeLock;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.simple_test);
		ct = CITTools.getInstance(this);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//pm.setBacklightBrightnessOff(false);
		pm.setBacklightBrightness(200);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
				.getClass().getName());
		mWakeLock.acquire();
	}

	public void initButton() {
		ct.initButton(btn_success);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mWakeLock != null) {
			mWakeLock.release();
		}
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
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	public static void LOGV(boolean debugflag, String TAG, String msg) {
		if (debugflag)
			Log.v(TAG, msg);
	}

	public static void LOGI(boolean debugflag, String TAG, String msg) {
		if (debugflag)
			Log.v(TAG, msg);
	}

	public static void LOGE(String TAG, String msg) {
		Log.e(TAG, msg);
	}
}
