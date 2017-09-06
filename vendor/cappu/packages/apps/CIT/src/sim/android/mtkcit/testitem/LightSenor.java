package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.content.Intent;
import android.hardware.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITJNI;
import sim.android.mtkcit.cittools.CITShellExe;
import sim.android.mtkcit.cittools.CITTools;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LightSenor extends TestBase implements Handler.Callback {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	private final String TAG = "LightSensor";
	Timer timer;
	int ALSValue;
	CITJNI citjni;
	int rawvalue;
	private Handler myHandler;
	Sensor mLightSensor;
	private final SensorEventListener mLightSensorListener;
	private SensorManager mSensorManager;
	TextView mtv_Light_info;
	String strLightinfo;
	boolean bMaxBrightness;
	boolean bAutoAdjust;

	public LightSenor() {
		mLightSensorListener = new SensorListener();
	}

	private void initAllControl() {
		strLightinfo = getString(R.string.test_lightsensor_info);
		mtv_Light_info = (TextView) findViewById(R.id.tv_light_senor_info);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		btn_success = (Button) findViewById(R.id.btn_success);
		ct.initButton(btn_success);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
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
		releaseResource();
		finish();
	}

	private void releaseResource() {
		SensorManager sensormanager = mSensorManager;
		SensorEventListener sensoreventlistener = mLightSensorListener;
		sensormanager.unregisterListener(sensoreventlistener);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.test_light_senor);
		myHandler = new Handler(this);
		initAllControl();
		TextView textview = mtv_Light_info;
		String s = strLightinfo;
		textview.setText(s);
		SensorManager sensormanager = (SensorManager) getSystemService("sensor");
		mSensorManager = sensormanager;
		Sensor sensor = mSensorManager.getDefaultSensor(5);
		mLightSensor = sensor;
		bMaxBrightness = false;
		bAutoAdjust = isAutoBrightness();
		if (bAutoAdjust)
			Log.v(TAG, "bAutoAdjust=" + bAutoAdjust);
		stopAutoBrightness();
		setBrightness(25);
	}

	boolean flag;

	protected void onResume() {
		super.onResume();
		flag = mSensorManager.registerListener(mLightSensorListener,
				mLightSensor, 3);
		String s = (new StringBuilder()).append("bSucceed is ").append(flag)
				.toString();
		if (flag) {
			getRawALSValue();
		}
		Log.d(TAG, s);
	}

	private void getRawALSValue() {
		// TODO Auto-generated method stub
		citjni = new CITJNI();
		timer = new Timer();
		timetask timetask1 = new timetask();
		timer.schedule(timetask1, 100l, 100l);
	}

	protected void onStop() {
		super.onStop();
		releaseResource();
		if (bAutoAdjust)
			startAutoBrightness();
		mSensorManager.unregisterListener(mLightSensorListener);
		flag = false;
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

	private void setBrightness(int brightness) {
		Log.v(TAG, "setBrightness=" + brightness);
		Window win = getWindow();
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
		// lp.screenBrightness = Float.valueOf(brightness);
		win.setAttributes(lp);

	}

	private boolean debugFlag = true;

	private void setBrightness2(int brightness) {
		try {
			// String BRIGHTNESS_FILE =
			// "/sys/class/leds/lcd-backlight/brightness";

			String commond = "echo " + brightness
					+ " > /sys/class/leds/lcd-backlight/brightness";
			String[] cmd = { "/system/bin/sh", "-c", commond };
			int ret = CITShellExe.execCommand(cmd);
			CITTools.LOGV(debugFlag, TAG, cmd[0] + cmd[1] + cmd[2]);
			if (0 == ret) {
				CITTools.LOGV(debugFlag, TAG, "execCommand success "
						+ CITShellExe.getOutput());

			} else {
				CITTools.LOGV(debugFlag, TAG, "execCommand error ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	class SensorListener implements SensorEventListener

	{

		public void onAccuracyChanged(Sensor sensor, int i) {
		}

		public void onSensorChanged(SensorEvent sensorevent) {

			/*
			 * TextView textview = mtv_Light_info; StringBuilder stringbuilder =
			 * new StringBuilder(); String s = strLightinfo; StringBuilder
			 * stringbuilder1 = stringbuilder.append(s).append("\n"); float f =
			 * sensorevent.values[1]; //float f = sensorevent.values[0]; String
			 * s1 = stringbuilder1.append(f).toString(); textview.setText(s1);
			 * if(f>10) { if(!bMaxBrightness) { setBrightness(255);
			 * bMaxBrightness = true; } } else { if(bMaxBrightness) {
			 * setBrightness(25); bMaxBrightness = false;
			 * bt_success.setEnabled(true); } }
			 */
		}
	}
	class timetask extends TimerTask {

		public void run() {
//			try {
//				Thread.sleep(500L);
//				Log.v(TAG, "run()");
//
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				Log.v(TAG, "timetask  error");
//				return;
//			}
			ALSValue = citjni.getALSValue();
			Log.v(TAG, "als="+citjni.getALSValue());
			Log.d(TAG, "send:" + String.valueOf(Thread.currentThread().getId()));
			Message msg = new Message();
			Bundle date = new Bundle();
			date.putInt("alsvalue", ALSValue);
			msg.setData(date);
			msg.what = 0;
			myHandler.sendMessage(msg);
			Log.v(TAG, "ALSValue="+ALSValue);
		}

	}
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			Bundle date = msg.getData();
			rawvalue = date.getInt("alsvalue");

			judgePass(rawvalue);
			break;

		}
		return false;
	}

	private void judgePass(int f) {

		TextView textview = mtv_Light_info;
		StringBuilder stringbuilder = new StringBuilder();
		String s = strLightinfo;
		StringBuilder stringbuilder1 = stringbuilder.append(s).append("\n");
		//float f = sensorevent.values[1];
		//float f = sensorevent.values[0];
		String s1 = stringbuilder1.append(f).toString();
		textview.setText(s1);
		if (f > CITBroadcastReceiver.L_SENSOR_CALI_THRESHOLD) {
			if (!bMaxBrightness) {
				// setBrightness(255);
				ct.setBrightness(255);
				bMaxBrightness = true;
			}
		} else {
			if (bMaxBrightness) {
				// setBrightness(25);
				ct.setBrightness(20);

				bMaxBrightness = false;
				btn_success.setEnabled(true);
			}
		}
			
	}

}
