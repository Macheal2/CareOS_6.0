package sim.android.mtkcit.testitem;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
import sim.android.mtkcit.cittools.CitBinder;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITJNI;
import sim.android.mtkcit.testitem.LCDTest.timetask;
import android.app.Activity;
import android.hardware.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.cittools.CITTools;

public class ProximityCalibration_cm3623 extends TestBase implements
		Handler.Callback {

	CITJNI citjni;
	private Handler myHandler;
	private final String TAG = "ProximityCalibration2C8";
	Sensor mProimitySensor;
	String strPromityinfo;
	TextView tv_calibration;
	TextView tv_calibration_result;

	TextView tv_calibration_farresulr;
	TextView tv_calibration_3cmresult;
	TextView tv_calibration_4cmresult;
	TextView tv_ps_value;

	private Button far_Calibration;
	private Button close_Calibration;
	private Button middle_Calibration;
	private boolean closeBoolean = false;
	private int closeVal;
	private boolean farBoolean = false;
	private int farVal;
	private boolean middleBoolean = false;
	private int middleVal;
	private int PSVal;
	private boolean caliBoolean = false;
	private IBinder binder;
	private CitBinder citBinder;
	private CITTools ct;
	private boolean tflag = false;
	private SensorManager mSensorManager;
	private SensorEventListener mProximityListener;
	private int valid = 0;

	/**
	 * far----------the distance is more than 3cm middle ---------the distance
	 * is 3cm close ----------the distance is 2cm
	 */
	private void initAllControl() {
//		binder = ServiceManager.getService("CitBinder");
//		citBinder = CitBinder.Stub.asInterface(binder);
		ct = CITTools.getInstance(this);
		citjni = new CITJNI();
		myHandler = new Handler(this);

		tv_calibration = (TextView) findViewById(R.id.tv_calibration);

		tv_calibration_result = (TextView) findViewById(R.id.tv_calibration_result);
		tv_ps_value = (TextView) findViewById(R.id.tv_ps_value);

		close_Calibration = (Button) findViewById(R.id.ps_calibration_close);
		close_Calibration.setOnClickListener(this);
		close_Calibration.setEnabled(closeBoolean);
		far_Calibration = (Button) findViewById(R.id.ps_calibration_far);
		far_Calibration.setOnClickListener(this);
		far_Calibration.setEnabled(farBoolean);
		middle_Calibration = (Button) findViewById(R.id.ps_calibration_middle);
		middle_Calibration.setOnClickListener(this);
		middle_Calibration.setEnabled(middleBoolean);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

		tv_calibration_farresulr = (TextView) findViewById(R.id.tv_far_calibration_res);
		tv_calibration_4cmresult = (TextView) findViewById(R.id.tv_4cm_calibration_res);
		tv_calibration_3cmresult = (TextView) findViewById(R.id.tv_3cm_calibration_res);
		far_Calibration.setEnabled(true);
		middle_Calibration.setEnabled(false);
		close_Calibration.setEnabled(false);
		strPromityinfo = getString(R.string.proximity_calibration_info_far);

	}

	private void releaseResource() {
		Log.v(TAG, "releaseResource() ");
		SensorManager sensormanager = mSensorManager;
		SensorEventListener sensoreventlistener = mProximityListener;
		sensormanager.unregisterListener(sensoreventlistener);
		tflag = false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ps_calibration_c8);
		initAllControl();

	}

	@Override
	protected void onResume() {
		tflag = true;
		mSensorManager = (SensorManager) getSystemService("sensor");
		mProimitySensor = mSensorManager.getDefaultSensor(8);
		mProximityListener = new SenListener();
		boolean flag = mSensorManager.registerListener(mProximityListener,
				mProimitySensor, 3);
		if (flag) {
			new Thread(new getValThread()).start();

		}
		super.onResume();

	}

	@Override
	protected void onStop() {
		releaseResource();
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private boolean debugeFlag = true;
	  private boolean caliFlag = false ;
	public void onClick(View v) {
		StringBuilder stringbuilder = new StringBuilder();
		switch (v.getId()) {
		case R.id.ps_calibration_far:
			farBoolean = true;
			farVal = PSVal;

			if (farVal < CITBroadcastReceiver.P_SENSOR_CALI_THRESHOLD) {
				strPromityinfo = getString(R.string.proximity_calibration_info_4cm);
				middle_Calibration.setEnabled(true);
				far_Calibration.setEnabled(false);
				close_Calibration.setEnabled(false);
				
				new Thread(new calibratFarThread()).start();
				tv_calibration_farresulr.setText(R.string.has_calibration);

			} else
				tv_calibration_farresulr.setText(R.string.no_calibration);
			LOGV(debugeFlag, TAG, "farVal = " + farVal);
			break;

		case R.id.ps_calibration_middle:
			middleVal = PSVal;

			if (middleVal - farVal > 2) {
				close_Calibration.setEnabled(true);
				strPromityinfo = getString(R.string.proximity_calibration_info_3cm);
				far_Calibration.setEnabled(false);
				middle_Calibration.setEnabled(false);
				tv_calibration_4cmresult.setText(R.string.has_calibration);
				middleBoolean = true;

			} else
				tv_calibration_4cmresult.setText(R.string.no_calibration);

			LOGV(debugeFlag, TAG, "middleVal = " + middleVal);
			break;

		case R.id.ps_calibration_close:
			closeVal = PSVal;
			if (closeVal - middleVal > 2) {
				close_Calibration.setEnabled(false);
				far_Calibration.setEnabled(true);
				middle_Calibration.setEnabled(false);
				tv_calibration_4cmresult.setText(R.string.has_calibration);
				closeBoolean = true;
				tv_calibration_3cmresult.setText(R.string.has_calibration);
				LOGV(debugeFlag, TAG, "closeVal = " + closeVal);
				new Thread(new calibratThread()).start();
				tv_calibration_result.setText(R.string.cali_wait);
				strPromityinfo = getString(R.string.proximity_calibration_info_far);

			} else {
				tv_calibration_3cmresult.setText(R.string.no_calibration);

			}

			break;
		default:
			finish();
		}

		tv_calibration.setText(strPromityinfo);

	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle date;
		StringBuilder stringbuilder = new StringBuilder();

		switch (msg.what) {

		case 0:
			LOGV(debugeFlag, "feng", "valid=" + valid);
			if (valid == 1) {
				new Thread(new saveThread()).start();
				stringbuilder
						.append(getString(R.string.str_calibration_success));
			} else {
				stringbuilder
						.append(getString(R.string.str_calibration_failed));

			}
			stringbuilder.append("  [ " + valid + " ] ");
			tv_calibration_result.setText(stringbuilder);
			middleBoolean = false;
			farBoolean = false;
			closeBoolean = false;

			break;
		case 1:

			date = msg.getData();
			PSVal = date.getInt("psvalue");
			tv_ps_value.setText("PSVal=" + PSVal);
		}

		return false;
	}

	private class SenListener implements SensorEventListener

	{

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
		}

		@Override
		public void onSensorChanged(SensorEvent sensorevent) {
		}

	}

	class saveThread implements Runnable {

		@Override
		public void run() {
			LOGV(debugeFlag, "feng", "middleVal="+middleVal);
			try {
				ct.setPsCali_close(closeVal);
				ct.setPsCali_far(middleVal);
				ct.setPsCali_valid(valid);
				
				if (0 == ct.getPsCali_far_far_flag())
				{
					ct.setPsCali_far_far(farVal);
					ct.setPsCali_far_far_flag(1);
				}
				
				caliFlag = true ;
			} catch (RemoteException e) {
				Log.e(TAG, "ct.setPsCali error");
				e.printStackTrace();
				caliFlag = true ;

			}
		}
	}

	class calibratThread implements Runnable {

		@Override
		public void run() {

			int res = citjni.PSCali2(middleVal, closeVal,1); // 2s
			Log.v("citjni.PSCali2", "res = " + res);
			if (res == 0) {
				valid = 1;
			} else
				valid = 0;
			Message msg = new Message();
			msg.what = 0;
			myHandler.sendMessage(msg);
		}

	}
	
	class calibratFarThread implements Runnable {

		@Override
		public void run() {
			int res = 0;
			
			if (0 == ct.getPsCali_far_far_flag())
			{
				res = citjni.PSCali3(farVal); // 2s
				Log.v("citjni.PSCali3", "res = " + res);
			}
//			if (res == 0) {
//				valid = 1;
//			} else
//				valid = 0;
//			Message msg = new Message();
//			msg.what = 0;
//			myHandler.sendMessage(msg);
		}

	}

	class getValThread implements Runnable {

		public void run() {
			while (tflag) {
				if (citjni.getPSValue() != -1) {
					PSVal = citjni.getPSValue();

				}
				Message msg = new Message();
				Bundle date = new Bundle();
				date.putInt("psvalue", PSVal);
				msg.setData(date);
				msg.what = 1;
				myHandler.sendMessage(msg);
				// Log.v(TAG, "----------PSValue=" + PSVal);
			}
		}
	}
}
