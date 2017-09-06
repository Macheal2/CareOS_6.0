package sim.android.mtkcit.testitem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
import sim.android.mtkcit.AutoTestActivity;

public class BrightnessTest extends TestBase implements
		SeekBar.OnSeekBarChangeListener {
	/** Called when the activity is first created. */
	TextView textView;
	boolean bMaxBrightness;
	boolean bAutoAdjust;
	WindowManager.LayoutParams lp;
	Window win;
	SeekBar mseekBar;
	private String mCurrentBrightness;
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	int flag1 = 0;
	int flag2 = 0;
   String flag3;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_brightness);
		textView = (TextView) findViewById(R.id.MyBrightness);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		
		ct.initButton(btn_success);
		bAutoAdjust = isAutoBrightness();
		mseekBar = (SeekBar) findViewById(R.id.BrightnessSeekBar);

		Log.v("fengxuanyyang", "seekBar=" + mseekBar);
		int iCurrentBrightness = (int) (android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, 255));
		mseekBar.setProgress(iCurrentBrightness);
		mseekBar.setOnSeekBarChangeListener(this);
		 mCurrentBrightness=getBrightness();
		// String
		 String current_brightness=getResources().getString(R.string.current_brightness);
		 current_brightness += iCurrentBrightness;
		 textView.setText(current_brightness);
		if (bAutoAdjust)
			stopAutoBrightness();
			
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		if (fromUser) {
			Integer tmpInt = seekBar.getProgress();
			setBrightness(tmpInt);
			Log.v("fengxuanyang", "tmpInt=" + tmpInt);

		}		// mCurrentBrightness=getScreenBrightness(this);
		mCurrentBrightness = getBrightness();

		String current_brightness = getResources().getString(
				R.string.current_brightness);
//		current_brightness += mCurrentBrightness;
		current_brightness += progress;
		textView.setText(current_brightness);
		judgePass(mCurrentBrightness);

		
	}

	protected void onStop() {
		super.onStop();
		if (bAutoAdjust)
			startAutoBrightness();
	}

	private void setBrightness(int brightness) {
		int temp = brightness;
		
		if (temp < 1)
			temp = 1;
		else if(temp > 255)
			temp = 255;
		
		win = getWindow();
		lp = win.getAttributes();
		lp.screenBrightness = Float.valueOf(temp/*brightness*/) * (1f / 255f);
		win.setAttributes(lp);
	}

	private int getScreenBrightness(Activity activity) {
		int nowBrightnessValue = 0;
		ContentResolver resolver = activity.getContentResolver();
		try {
			nowBrightnessValue = android.provider.Settings.System.getInt(
					resolver, Settings.System.SCREEN_BRIGHTNESS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nowBrightnessValue;
	}

	private String getBrightness() {
		// / while(int i =3) {
		byte[] buffer = new byte[4];
		String result = android.provider.Settings.System.getInt(
				getContentResolver(),
				android.provider.Settings.System.SCREEN_BRIGHTNESS, 255)+"";
	//	String str ;
		String BRIGHTNESS_FILE = "/sys/class/leds/lcd-backlight/brightness";
		FileInputStream fis;
		try {
			fis = new FileInputStream(BRIGHTNESS_FILE);
			fis.read(buffer);
			result = new String(buffer);
			Log.v("fengxuanyang",  result);
			fis.close();
			// Log.v("fengxuanyang", "result="+result);
		} catch (Exception e) {
			Log.v("fengxuanyang", "read  BRIGHTNESS_FILE  error");
			e.printStackTrace();
		}
		return result;

	}

	private void startAutoBrightness() {
		Settings.System.putInt(getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	private void stopAutoBrightness() {
		Settings.System.putInt(getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	private boolean isAutoBrightness() {
		boolean automicBrightness = false;
		try {
			automicBrightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return automicBrightness;
	}
	String mCurrentBrightness1;
	String mCurrentBrightness2;
	int i ;
	private void judgePass(String mCurrentBrightness) {
		mCurrentBrightness1=mCurrentBrightness;
		Log.v("fengxuanyang",mCurrentBrightness1+"----"+mCurrentBrightness2 );
		if(i==1) {
			mCurrentBrightness2=mCurrentBrightness;
			if (mCurrentBrightness2.equals(mCurrentBrightness2) ) {
				Log.v("fengxuanyang",mCurrentBrightness1+"----"+mCurrentBrightness2 );
				btn_success.setEnabled(true);
			}
		}
		i++;
		Log.v("fengxuanyang", mCurrentBrightness);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	// OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {
	//
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	// if (fromUser) {
	// Integer tmpInt = seekBar.getProgress();
	// System.out.println(tmpInt);
	// // 51 (seek scale) * 5 = 255 (max brightness)
	// // Old way
	// android.provider.Settings.System.putInt(getContentResolver(),
	// android.provider.Settings.System.SCREEN_BRIGHTNESS,
	// tmpInt); // 0-255
	// tmpInt = Settings.System.getInt(getContentResolver(),
	// Settings.System.SCREEN_BRIGHTNESS, -1);
	// // Cupcake way..... sucks
	// WindowManager.LayoutParams lp = getWindow().getAttributes();
	// // lp.screenBrightness = 1.0f;
	// // Float tmpFloat = (float)tmpInt / 255;
	// if (0<= tmpInt && tmpInt <= 255) {
	// lp.screenBrightness = tmpInt;
	// }
	//
	//
	// getWindow().setAttributes(lp);
	// }
	//
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekBar seekBar) {
	// // TODO Auto-generated method stub
	// // put awesomeness here
	// }
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar seekBar) {
	// // TODO Auto-generated method stub
	// // and here too
	// }
	// };
}