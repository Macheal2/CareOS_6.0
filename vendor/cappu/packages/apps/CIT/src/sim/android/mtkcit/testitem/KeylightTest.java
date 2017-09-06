package sim.android.mtkcit.testitem;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

public class KeylightTest extends TestBase  {
	static final String TAG = "FlashlightTest";
	WakeLock mWakeLock;
	ImageButton mflashButton;
	TextView txtDescript;
	int state;
	Context context = null;
	private boolean run;
	private PowerManager pm;

	// private LightsService.Light mKeyboardLight;

	@Override
	public void onCreate(Bundle icycle) {
		super.onCreate(icycle);
		// context.getApplicationContext();
		setContentView(R.layout.keylight_test);

		initResourceRefs();
		// mKeyboardLight=
	}

	//add_s wangkang 2012.5.2 bug914
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		if(state == 1 && (mWakeLock.isHeld() == false)){
			mflashButton.setImageResource(R.drawable.stop);
			txtDescript.setText(getString(R.string.open_keylight));
			state = 0;
		}
	}
	//add_e wangkang 2012.5.2 bug914
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		releaseWakeLock() ;
		super.onStop();
	}

	private void initResourceRefs() {
		mflashButton = (ImageButton) findViewById(R.id.mflashButton);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		txtDescript = (TextView) findViewById(R.id.textDescript);
		txtDescript.setText(getString(R.string.keylight_test));
		mflashButton.setOnClickListener(this);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
		mflashButton.setEnabled(true);
		btn_success.setEnabled(true);
		state = 0;
		// mWakeLock.release();
		pm = (PowerManager) this.getSystemService(this.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "keyguard");
		mWakeLock.acquire();
		state = 1;
	}

	public void onClick(View button) {
		int id = button.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.mflashButton) {
			if (state == 1) {
				//mod_s wangkang 2012.5.2 bug914
				if(mWakeLock.isHeld()){
					mWakeLock.release();
				}
				//mWakeLock.release();
				//mod_e wangkang 2012.5.2 bug914
				mflashButton.setImageResource(R.drawable.stop);
				txtDescript.setText(getString(R.string.open_keylight));
				state = 0;
			} else if (state == 0) {
				//releaseWakeLock();
				mWakeLock.acquire();
				mflashButton.setImageResource(R.drawable.play);
				txtDescript.setText(getString(R.string.close_keylight));
				state = 1;
			}
		} else {
			if (id == R.id.btn_success) {
				b.putInt("test_result", 1);
			}else{
				b.putInt("test_result", 0);
			}
			intent.putExtras(b);
			setResult(RESULT_OK, intent);
			finish();
		}		
		
	}

	 private void releaseWakeLock() {
			if (mWakeLock != null&&state==1) {
				mWakeLock.release();

			}
	 }

	private void acquireWakeLock() {
		if (mWakeLock != null) {
			mWakeLock.acquire();

		}
	}
}
