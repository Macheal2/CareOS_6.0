package sim.android.mtkcit.testitem;

import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.featureoption.SimcomFeatureOption;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
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
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.cittools.*;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITTools;

public class MotionSenorTest extends TestBase implements Handler.Callback {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	private String TAG = "MotionSenorTest";
	private float[][] MotionSenorCalibra = new float[16][3];
	private int calibraX;
	private int calibraY;
	private int calibraZ;
	private int m;
	private Handler myHandler;
	private boolean debugflag = true;
	private boolean calibraFlag = false;
	Timer timer;
	private CalibraTask calibraTimer;
	ImageView imageView[];
	private final SensorListener mListener;
	private SensorManager mSensorManager;
	private float mValues[];
	int m_nCurArrow;
	TextView ms_tv_XYZ[];
	Button bt_calibration;
	private int motionResult[];
	float x;
	float y;
	float z;
	private IBinder binder;
	private CitBinder citBinder;
	Button btn_clr_calibration;
	private Button btn_success;
	private Button btn_fail;
	private CITTools ct;

	public MotionSenorTest() {
		mListener = new SensListener();
		myHandler = new Handler(this);

	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mSensorManager = (SensorManager) getSystemService("sensor");
		imageView = new ImageView[4];
		ms_tv_XYZ = new TextView[3];
		motionResult = new int[4];
		setContentView(R.layout.motionsenor_test);
		initAllControl();
	}

	private void initAllControl() {
		ct = CITTools.getInstance(this);
//		binder = ServiceManager.getService("CitBinder");
//		citBinder = CitBinder.Stub.asInterface(binder);
		imageView[0] = (ImageView) findViewById(R.id.ms_arrow_up);
		imageView[1] = (ImageView) findViewById(R.id.ms_arrow_right);
		imageView[2] = (ImageView) findViewById(R.id.ms_arrow_down);
		imageView[3] = (ImageView) findViewById(R.id.ms_arrow_left);
		ms_tv_XYZ[0] = (TextView) findViewById(R.id.ms_tv_x);
		ms_tv_XYZ[1] = (TextView) findViewById(R.id.ms_tv_y);
		ms_tv_XYZ[2] = (TextView) findViewById(R.id.ms_tv_z);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);

		bt_calibration = (Button) findViewById(R.id.btn_motion_calibration);
		bt_calibration.setOnClickListener(this);
		btn_clr_calibration = (Button) findViewById(R.id.btn_clr_calibration);
		btn_clr_calibration.setOnClickListener(this);
		if (!CITBroadcastReceiver.G_SENSOR_CALI) {
			bt_calibration.setVisibility(View.GONE);
			btn_clr_calibration.setVisibility(View.GONE);
		}

	}

	private int GetDirection(float X, float Y) {
		int image;
		float absX = Math.abs(X);
		float absY = Math.abs(Y);
		if (absX > absY) {
			if (X < 0)
				image = 3;
			else
				image = 1;
		} else {
			if (Y < 0)
				image = 2;
			else
				image = 0;
		}

		return image;

	}

	/**
	 * x_cali = -x; y_cali=-y; z_cali = 9807 + z;
	 */
	private void getCalibraVal() {
		float totalX = 0, totalY = 0, totalZ = 0;
		for (int i = 0; i < 16; i++) {
			totalX += MotionSenorCalibra[i][0];
			totalY += MotionSenorCalibra[i][1];
			totalZ += MotionSenorCalibra[i][2];

		}
		calibraX = (int) (totalX * 1000 / 16);
		calibraY = (int) (totalY * 1000 / 16);
		calibraZ = (int) (9807 + totalZ * 1000 / 16);
		CITTools.LOGV(debugflag, TAG, "calibraX=" + calibraX + "calibraY="
				+ calibraY + "calibraZ=" + calibraZ);
	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle date;
		int resule = -1;
		StringBuilder stringbuilder = new StringBuilder();
		date = msg.getData();
		resule = date.getInt("calibraRes");
		if (resule == 1) {
			Toast.makeText(this, R.string.str_calibration_success,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.str_calibration_failed,
					Toast.LENGTH_SHORT).show();
		}
		bt_calibration.setClickable(true);

		CITTools.LOGV(debugflag, TAG, "calibraRes=" + resule);
		return false;
	}

	protected void onResume() {
		super.onResume();
		// SensorManager sensormanager = mSensorManager;
		// SensorListener sensorlistener = mListener;
		mSensorManager.registerListener(mListener, 2);
	}

	protected void onStop() {
		releaseRec();
		super.onStop();

	}

	private void showDialog() {

		AlertDialog.Builder builder = new Builder(this);

		builder.setTitle(R.string.res_calibration);
		// builder.setPositiveButton(
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							ct.ClrGsCali();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		builder.setNeutralButton(R.string.alert_dialog_cancel,
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void releaseRec() {
		if (mSensorManager != null && mListener != null) {
			mSensorManager.unregisterListener(mListener, 2);

		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_motion_calibration) {
			// ensure that the phone is horizontal
			if (Math.abs(mValues[0]) < CITBroadcastReceiver.G_SENSOR_CALI_THRESHOLD
					&& Math.abs(mValues[1]) < CITBroadcastReceiver.G_SENSOR_CALI_THRESHOLD) {

				bt_calibration.setClickable(false);
				// if (true) {
				timer = new Timer();
				calibraTimer = new CalibraTask();
				timer.schedule(calibraTimer, 0, 50);

			} else {
				Toast.makeText(this, R.string.str_calibration_failed,
						Toast.LENGTH_SHORT).show();
			}

		} else if (id == R.id.btn_clr_calibration) {
			showDialog();
		} else {
			if (id == R.id.btn_success) {
				b.putInt("test_result", 1);
			} else {
				b.putInt("test_result", 0);
			}
			intent.putExtras(b);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	private class SensListener implements SensorListener {

		public void onAccuracyChanged(int i, int j) {
		}

		public void onSensorChanged(int i, float af[]) {
			// Log.v(TAG, "onSensorChanged");
			MotionSenorTest motionsenortest;
			mValues = af;
			TextView textview = ms_tv_XYZ[0];
			StringBuilder stringbuilder = (new StringBuilder()).append("X = ");
			x = mValues[0];
			String s = stringbuilder.append(x).toString();
			textview.setText(s);
			TextView textview1 = ms_tv_XYZ[1];
			StringBuilder stringbuilder1 = (new StringBuilder()).append("Y = ");
			y = mValues[1];
			String s1 = stringbuilder1.append(y).toString();
			textview1.setText(s1);
			TextView textview2 = ms_tv_XYZ[2];
			StringBuilder stringbuilder2 = (new StringBuilder()).append("Z = ");
			z = mValues[2];
			String s2 = stringbuilder2.append(z).toString();
			textview2.setText(s2);
			motionsenortest = MotionSenorTest.this;
			int newArrow;
			newArrow = motionsenortest.GetDirection(x, y);
			// Log.v(TAG, "newArrow=" + newArrow);
			if (newArrow == m_nCurArrow && newArrow == 0) {
				imageView[m_nCurArrow].setVisibility(0);

			}
			if (newArrow != m_nCurArrow) {
				imageView[m_nCurArrow].setVisibility(4);
				imageView[newArrow].setVisibility(0);
				m_nCurArrow = newArrow;
				motionResult[m_nCurArrow] = 1;
			}

			for (i = 0; i < motionResult.length; i++) {
				if (motionResult[i] == 0) {
					break;
				}
			}
			if (i >= motionResult.length) {
				btn_success.setEnabled(true);
			}

		}

	}

	class CalibraTask extends TimerTask {
		@Override
		public void run() {
			MotionSenorCalibra[m][0] = x;
			MotionSenorCalibra[m][1] = y;
			MotionSenorCalibra[m][2] = z;
			CITTools.LOGV(debugflag, TAG, MotionSenorCalibra[m][0] + ","
					+ MotionSenorCalibra[m][1] + "," + MotionSenorCalibra[m][2]
					+ "m===" + m);
			m++;
			if (m == 16) {
				m = 0;
				calibraFlag = true;
			if (timer != null) { 
				timer.cancel();
                              }

				new Thread(new calibratThread()).start();
				return;
			}
			CITTools.LOGV(debugflag, TAG, "tomer run!");

		}
	}

	class calibratThread implements Runnable {

		@Override
		public void run() {
			CITTools.LOGV(debugflag, TAG, "calibratThread");

			getCalibraVal();
			ct.CitBinderPrepare();
			int calibraRes = ct.GsCali(calibraX, calibraY, calibraZ);
			CITTools.LOGV(debugflag, TAG, "calibraRes=" + calibraRes);
			Message msg = new Message();
			Bundle date = new Bundle();
			date.putInt("calibraRes", calibraRes);
			msg.setData(date);
			myHandler.sendMessage(msg);
		}

	}
}
