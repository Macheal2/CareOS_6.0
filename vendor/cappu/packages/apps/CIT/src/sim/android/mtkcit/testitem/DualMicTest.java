package sim.android.mtkcit.testitem;

import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.featureoption.SimcomFeatureOption;
import android.media.AudioSystem;
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

public class DualMicTest extends TestBase{
	private String TAG = "DualMicTest";

	private Button btn_main;
	private Button btn_ref;
	private Button btn_success;
	private Button btn_fail;
	private CITTools ct;
	private boolean main_flag;
	private boolean ref_flag;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.test_dualmic);

		ct = CITTools.getInstance(this);

		btn_main = (Button) findViewById(R.id.main_btn);
		btn_ref = (Button) findViewById(R.id.ref_btn);
		btn_main.setOnClickListener(this);
		btn_ref.setOnClickListener(this);
		
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
		
		ct.initButton(btn_success);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		releaseRec();
		super.onStop();
	}

	public void onClick(View button) {
		if (!button.isEnabled())
			return;

		int id = button.getId();
		
		if (id == R.id.main_btn) {
//			Log.i(TAG, "id == R.id.main_btn");
			btn_main.setEnabled(false);
			btn_ref.setEnabled(true);
			main_flag = true;
			AudioSystem.setParameters("SET_LOOPBACK_TYPE=0");
			AudioSystem.setParameters("SET_LOOPBACK_TYPE=1,3");
			
			if (ref_flag == true)
				btn_success.setEnabled(true);
			
			return;
		} else if (id == R.id.ref_btn) {
//			Log.i(TAG, "id == R.id.ref_btn");
			btn_main.setEnabled(true);
			btn_ref.setEnabled(false);
			ref_flag = true;
			AudioSystem.setParameters("SET_LOOPBACK_TYPE=0");
			AudioSystem.setParameters("SET_LOOPBACK_TYPE=3,3");
			
			if (main_flag == true)
				btn_success.setEnabled(true);
			
			return;
		}
		
//		Log.i(TAG, "id == ++++++");
		
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
		// for test
		AudioSystem.setParameters("SET_LOOPBACK_TYPE=0");
	}

}
