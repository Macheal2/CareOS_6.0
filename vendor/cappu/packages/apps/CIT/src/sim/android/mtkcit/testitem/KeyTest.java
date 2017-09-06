package sim.android.mtkcit.testitem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sim.android.mtkcit.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManagerPolicy;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
//import com.android.internal.policy.impl.PhoneWindowManager;
//import com.mediatek.featureoption.SimcomFeatureOption;
//import com.mediatek.featureoption.SimcomIDOption;
//import sim.android.mtkcit.TestActivity;

public class KeyTest extends TestBase  {

	public Button btn_success;
	public Button btn_fail;
	private boolean flag;
	private int keyCnt[];
	private ArrayList<TextView> tvlist = new ArrayList<TextView>();
	private Activity mActivty;
	private WindowManager mWindowManager;
	private View mLockView;
	private LayoutParams mLockViewLayoutParams;
	/**
	 * volume up = 24; volume down = 25; home = 3; menu = 82; back = 4; search
	 * =84; camera = 27 earPhone = 85 torch = 301 fm = 300 ;
	 * 
	 */
    private String allKeys[];
	private String testKeys[];
	private int allKeycode[] = {24, 25, 82, 4, 3, 301}; //add by zxy for torch
	private int testKeycode[];
	// LayoutParams.TYPE_SYSTEM_ERROR;
	
//	private String allKeys[] = { "camera", "volume up", "volume down", "menu",
//			"back", "home", "search", "earPhone" ,"lowpower_on" ,"lowpower_off" ,"torch"}; // add by zxy for torch

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		this.setContentView(R.layout.keytest);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED); 
		TextView tv;
		testKeys = getResources().getStringArray(R.array.TestKeys);
		allKeys = getResources().getStringArray(R.array.AllKeys);
		keyCnt = new int[testKeys.length];
		testKeycode = new int[testKeys.length];
		LinearLayout ll = (LinearLayout) findViewById(R.id.test_key_listLinearLayout);
		for (int i = 0; i < testKeys.length; i++) {
			tv = new TextView(this);
			tv.setText(testKeys[i]);
			tv.setTextColor(R.drawable.black);
			tv.setWidth(LayoutParams.WRAP_CONTENT);
			// tv.setHeight(LayoutParams.WRAP_CONTENT);
			tv.setTextSize(24.0f);
			tvlist.add(tv);
			ll.addView(tv);
			for (int j = 0; j < allKeys.length; j++) {
				if (testKeys[i].equals(allKeys[j])) {
					testKeycode[i] = allKeycode[j];
				}
			}
		}
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);
		flag = true;
		onAttachedToWindow();
		write(1);
	}

	@Override
	protected void onStart() {
		initAllControl();
		super.onStart();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
        	write(0);
		super.onDestroy();
	}

	private void initAllControl() {
		int i = WindowManagerPolicy.FLAG_INJECTED;
		// WindowManagerPolicy.cit_home = true;
		// WindowManagerPolicy.c
		//LOGV(debugFlag, TAG, "KeyEvent.cit_home   " + KeyEvent.CIT_HOME);
		IntentFilter mediaButtonIntentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_BUTTON);
		MediaButtonBroadcastReceiver receiver = new MediaButtonBroadcastReceiver();
		this.registerReceiver(receiver, mediaButtonIntentFilter);
	}

	public void onClick(View v) {
		int id = v.getId();
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
	
	@Override
    public void onAttachedToWindow() {
		// set FLAG_HOMEKEY_DISPATCHED in onAttachedToWindow
       //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
       super.onAttachedToWindow();
    }

	private class MediaButtonBroadcastReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			LOGV(debugFlag, TAG, "MediaButtonBroadcastReceiver   onReceive");
			KeyEvent event = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if ((event != null)
					&& (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
				LOGV(debugFlag, TAG, "keyCode=" + event.getKeyCode());
			}
		}

	}

	// PhoneWindowManager pwm;
	private boolean debugFlag = true;
	private String TAG = "KeyTest";

	public boolean onKeyDown(int i, KeyEvent keyevent) {
		LOGV(debugFlag, TAG, "keyCode=" + i);

		return true;
	}

	public boolean onKeyUp(int keyCode, KeyEvent keyevent) {
		LOGV(debugFlag, TAG, "keyCode=" + keyevent.getKeyCode());

		int i = 0, j = 0;
		flag = true;
		for (j = 0; j < testKeycode.length; j++) {
			if (testKeycode[j] == keyCode) {
				break;
			}

		}
		if (j == testKeys.length) {
			return true;
		}

		keyCnt[j] = 1;
		changeColor(tvlist.get(j));
		for (i = 0; i < keyCnt.length; i++) {
			if (keyCnt[i] != 1) {
				flag = false;
				break;
			}
		}
		btn_success.setEnabled(flag);
		return true;

	}

	private void changeColor(TextView tv) {

		if (tv.getTag() == null) {
			tv.setTag(true);
			tv.setBackgroundResource(R.drawable.green);
			return;
		}
		if ((Boolean) tv.getTag()) {
			tv.setTag(false);
			tv.setBackgroundResource(R.drawable.gray);
		} else {
			tv.setTag(true);
			tv.setBackgroundResource(R.drawable.green);
		}

	}
	
	private void read() {
		try {
			File readFile = new File("proc/kpd_test_mode");
			FileReader inCmd = new FileReader(readFile);
			try {
				int checkValue = inCmd.read();
				LOGV(true, TAG, "zxy read checkValue="+checkValue);
			} catch (IOException e) {
				LOGV(true, TAG, "zxy IOException="+e.toString());
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			LOGV(true, TAG, "zxy FileNotFoundException="+e.toString());
			e.printStackTrace();
		}
	}

	private void write(int value) {
		File kpd_file = new File("proc/kpd_test_mode");
		FileWriter fr;

		try {
		        LOGV(true, TAG, "zxy write value="+value);
		
			fr = new FileWriter(kpd_file);
			fr.write(String.valueOf(value)); 
			fr.close();
		} catch (IOException e) {
			LOGV(true, TAG, "zxy IOException="+e.toString());
			e.printStackTrace();
		}
	}

}

