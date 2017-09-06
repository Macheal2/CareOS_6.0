package sim.android.mtkcit.testitem;

import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.featureoption.SimcomFeatureOption;
//import com.mediatek.featureoption.SimcomIDOption;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITJNI;
import sim.android.mtkcit.cittools.CitBinder;
import sim.android.mtkcit.cittools.CITTools;

public class ProximitySenor extends TestBase implements  Handler.Callback {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	// private CommonDrive cd;
	Timer timer;
	int PSValue;
	CITJNI citjni;
	int rawvalue;
	String TAG = "ProximitySenor";
	private Handler myHandler;
	private int psthd[];
	private CitBinder citBinder;
	private CITTools ct;
	private IBinder binder;
	private boolean tflag;
	private Button btn_Calibration;
	int calivalue;
	private Button btn_res_calibration;
	/*
	 * 
	 */
	private int sensor_type = 0;

	private void initAllControl() {
//		binder = ServiceManager.getService("CitBinder");
//		citBinder = CitBinder.Stub.asInterface(binder);
		tflag = true;
		citjni = new CITJNI();
		mSensorManager = (SensorManager) getSystemService("sensor");
		mProimitySensor = mSensorManager.getDefaultSensor(8);
		mProximityListener = new SenListener();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_Calibration) {
			// if ("cmd3623".equals(CITBroadcastReceiver.P_SENSOR_CALI_TYPE)) {
			//
			//
			// } else if ("tmd2771"
			// .equals(CITBroadcastReceiver.P_SENSOR_CALI_TYPE)) {
			//
			// }
			switch (CITBroadcastReceiver.p_sensor_type) {
			case 1:
				startActivity(new Intent(this,
						ProximityCalibration_tmd2771.class));
				break;

			case 2:
				startActivity(new Intent(this,
						ProximityCalibration_cm3623.class));
				break;
			default:
				break;
			}

			return;
		}
		if (id == R.id.btn_res_calibration) {
			showDialog();
			return;
		}
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

	private void showDialog() {

		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle("清除校准数据");
		// builder.setPositiveButton(
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int res = 0;
						
						try {
							// if (SimcomFeatureOption.MMI_PLATFORM_CIT_CM3623)
							// {
							// the PSCali2 only set the value , not get value
							// we can use to set value
							switch (CITBroadcastReceiver.p_sensor_type) {
							case 1:
							case 2:
								res = citjni.PSCali2(0, 0, 0);
								ct.setPsCali_close(0); 
								ct.setPsCali_far(0);
								ct.setPsCali_valid(0);
								Log.v(TAG, "PSCali2(0, 0, 0)");
								Log.v("citjni.PSCali2", "res = " + res);
								
								res = citjni.PSCali3(0);
								//ct.setPsCali_far_far(0);
								//ct.setPsCali_far_far_flag(0);
								Log.v("citjni.PSCali3", "res = " + res);
								
								break;
							default:
								break;
							}
							// if ("cmd3623"
							// .equals(CITBroadcastReceiver.P_SENSOR_CALI_TYPE)
							// || "tmd2771"
							// .equals(CITBroadcastReceiver.P_SENSOR_CALI_TYPE))
							// {
							//
							// }
							Log.v(TAG, "CITBroadcastReceiver.p_sensor_type = "
									+ CITBroadcastReceiver.p_sensor_type);
							getRawPSValue();

						} catch (RemoteException e) {
							Log.e(TAG, "ct.setPsCali error");
							e.printStackTrace();

						}
					}
				});
		builder.setNeutralButton("取消",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.test_proximitysenor);
		myHandler = new Handler(this);

		// initAllControl();
		ct = CITTools.getInstance(this);
		imageView = new ImageView[2];
		imageView[0] = (ImageView) findViewById(R.id.test_proximitysensor_gray);
		imageView[1] = (ImageView) findViewById(R.id.test_proximitysensor_green);
		strPromityinfo = getString(R.string.proximitysenor_info);
		mtv_pro_info = (TextView) findViewById(R.id.tv_proximitysenor_info);
		mtv_pro_info.setText(strPromityinfo);
		btn_Calibration = (Button) findViewById(R.id.btn_Calibration);
		btn_Calibration.setVisibility(View.GONE); // added by bruce 
		btn_Calibration.setOnClickListener(this);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);

		btn_res_calibration = (Button) findViewById(R.id.btn_res_calibration);
		btn_res_calibration.setVisibility(View.GONE); // added by bruce 
		btn_res_calibration.setOnClickListener(this);
		if (!CITBroadcastReceiver.P_SENSOR_CALI) {
			btn_Calibration.setVisibility(View.GONE);
			btn_res_calibration.setVisibility(View.GONE);
		}
		Log.v(TAG, "oncreate");
	}

	@Override
	protected void onResume() {
		initAllControl();
		Log.v(TAG, "onResume()");
		super.onResume();
		boolean flag = mSensorManager.registerListener(mProximityListener,
				mProimitySensor, 3);
		String s = (new StringBuilder()).append("bSucceed is ").append(flag)
				.toString();
		if (flag) {
			getRawPSValue();
		}
		Log.d(TAG, s);
	}

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop()");
		releaseResource();
		super.onStop();


	}
	private void releaseResource() {
		// TODO Auto-generated method stub
		Log.v(TAG, "releaseResource() ");
		SensorManager sensormanager = mSensorManager;
		SensorEventListener sensoreventlistener = mProximityListener;
		sensormanager.unregisterListener(sensoreventlistener);
		if(timer!=null) {
			timer.cancel();		
			timer=null;
		}
		tflag = false;

	}

	ImageView imageView[];
	Sensor mProimitySensor;
	private SensorEventListener mProximityListener;
	private SensorManager mSensorManager;
	TextView mtv_pro_info;
	String strPromityinfo;
	// Button bt_Calibration;

	boolean bChanged = false;

	private class SenListener implements SensorEventListener

	{

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
		}

		@Override
		public void onSensorChanged(SensorEvent sensorevent) {

			/*
			// LK@2011-10-12 modify the judge value and way
			// if (sensorevent.values[0] <= 3F) {
			if (sensorevent.values[1] >= 6f) {

			//if (sensorevent.values[0] == 0) {
				imageView[0].setVisibility(0);
				imageView[1].setVisibility(4);
				result = getString(R.string.close);
				if (!bChanged) {
					getWindow().setBackgroundDrawableResource(R.drawable.green);
					bChanged = true;
					bt_success.setEnabled(true);
				}
			} else if (sensorevent.values[1] < 3f) {
				// else if (sensorevent.values[0] == 1) {
				result = getString(R.string.far);
				imageView[0].setVisibility(0);
				imageView[1].setVisibility(4);
				if (bChanged)
					getWindow().setBackgroundDrawableResource(R.drawable.blue);
			} else {
				imageView[0].setVisibility(4);
				imageView[1].setVisibility(0);
					result=getString(R.string.success);
					
					
				
		 }
			stringbuilder = new StringBuilder();

			s1 = stringbuilder.append(strPromityinfo).append("\n")
					.append(sensorevent.values[1]).append("\n").append(result)
					.toString();
			mtv_pro_info.setText(s1);*/
		 }

	}


	@Override
	public boolean handleMessage(Message msg) {
		Bundle date;
		switch (msg.what) {

		case 0:
			date = msg.getData();
			rawvalue = date.getInt("psvalue");

			judgePass(rawvalue);
			break;
		case 1:
			date = msg.getData();
			calivalue = date.getInt("calivalue");

			break;
		}
		return false;
	}
	
	private void judgePass(int mvalue) {
		String result = null;
		psthd = citjni.getPSTHD();
		Log.v(TAG, "psthd[0]=" + psthd[0] + "psthd[1]=" + psthd[1]
				+ "psthd[2]=" + psthd[2]);
	if (mvalue > psthd[0]) {
		imageView[0].setVisibility(0);
		imageView[1].setVisibility(4);
		result = getString(R.string.close);
		if (!bChanged) {
			getWindow().setBackgroundDrawableResource(R.drawable.green);
			bChanged = true;
			btn_success.setEnabled(true);
		}
	}
		// else if(SimcomFeatureOption.CMN_PLATFORM_CUSTOMER_NAME
		// .equals(SimcomIDOption.CMN_ID_PHILIPS)? mvalue < psthd[1]&&mvalue>=0
		// : mvalue < psthd[0]&&mvalue>=0)
		else if (mvalue < psthd[1] && mvalue >= 0) {
			result = getString(R.string.far);
			imageView[0].setVisibility(0);
			imageView[1].setVisibility(4);
			if (bChanged)
				getWindow().setBackgroundDrawableResource(R.drawable.blue);
			bChanged=false;
		} else {
			imageView[0].setVisibility(4);
			imageView[1].setVisibility(0);
			result = getString(R.string.success);
		}
		StringBuilder stringbuilder = new StringBuilder();
		String s1;

		// if (SimcomFeatureOption.CMN_PLATFORM_CUSTOMER_NAME
		// .equals(SimcomIDOption.CMN_ID_PHILIPS)) {
		if (true) {
			s1 = stringbuilder.append(strPromityinfo).append("\n")
					.append(rawvalue).append("\n").append(result).append("\n")
					.append(getString(R.string.pls_thd_close) + psthd[0])
					.append("\n").append(getString(R.string.pls_thd_far))
					.append(psthd[1]).append("\n")
					.append(getString(R.string.pls_valid)).append(psthd[2])
					.toString();
		} else
			s1 = stringbuilder.append(strPromityinfo).append("\n")
					.append(rawvalue).append("\n").append(result).append("\n")
					.append(getString(R.string.pls_thd) + psthd[0]).toString();
		mtv_pro_info.setText(s1);
	}

	private void getRawPSValue() {
		// psthd = citjni.getPSTHD();
		psthd = citjni.getPSTHD();
		Log.v(TAG, "psthd[0]=" + psthd[0] + "psthd[1]=" + psthd[1]
				+ "psthd[2]=" + psthd[2]);
		timer = new Timer();
		timetask timetask1 = new timetask();
		timer.schedule(timetask1, 100l, 100l);
	}
	class timetask extends TimerTask {

		public void run() {
			while(tflag) {
			try {
				Thread.sleep(100L);
				Log.v(TAG, "run()");

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v(TAG, "timetask  error");
				return;
			}
			if(citjni.getPSValue()!=-1) {
				PSValue = citjni.getPSValue();

				}
				Log.d(TAG,
						"send:"
								+ String.valueOf(Thread.currentThread().getId()));
				Message msg = new Message();
				Bundle date = new Bundle();
				date.putInt("psvalue", PSValue);
				msg.setData(date);
				msg.what = 0;
				myHandler.sendMessage(msg);
				 Log.v(TAG, "PSValue=" + PSValue);
			}
		}
	}


}
