package sim.android.mtkcit.testitem;

import java.util.List;

import sim.android.mtkcit.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
//import sim.android.mtkcit.TestActivity;

public class HeartRateSenor extends TestBase {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	private final String TAG = "HeartRateSenor";

	private static final int SENSOR_TYPE_HEARTRATE = 21;
	private TextView mHeartRate;
	private SensorManager mSensorManager;
	private Sensor mHrSensor;

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
		finish();
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.test_heartrate_senor);// dengying add heart rate

		mHeartRate = (TextView) findViewById(R.id.tv_heartrate_senor_info);

		mHeartRate.setText("心率传感器数据："+"\n"+0);
		
		
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		btn_success = (Button) findViewById(R.id.btn_success);
		ct.initButton(btn_success);
		
		
		//Log.e(TAG, "检查BODY_SENEORS权限：" + hasPermission());
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		/*** 遍历出智能手表设备中所以的Seneor ***/
		//List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		//for (Sensor sensor : sensors) {
		//	Log.e(TAG, "遍历sensor:[name=" + sensor.getName() + ";type=" + sensor.getType() + ";vendor=" + sensor.getVendor() + "]");
		//}

		// Sensor.TYPE_HEART_RATE or 65562
		mHrSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_HEARTRATE);
		Log.e("TAG", "mHrSensor:" + mHrSensor.toString());
	}

	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		mSensorManager.registerListener(listener, mHrSensor, 3);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		mSensorManager.unregisterListener(listener, mHrSensor);
	}

	protected void onStop() {
		super.onStop();

	}

	SensorEventListener listener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub

			float rate = event.values[0];
			Log.e(TAG, "rate:" + rate);

			new UiThread(String.valueOf(rate)).start();
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

			Log.e(TAG, "accuracy:" + accuracy);

		}
	};

	/********* 检查是否打开权限,用于测试，没多大用处 ***********/
	public boolean hasPermission() {

		PackageManager pm = getPackageManager();
		int permission = pm.checkPermission("android.permission.BODY_SENSORS", "sim.android.mtkcit.testitem");
		boolean isPer = permission == PackageManager.PERMISSION_GRANTED ? true : false;
		return isPer;
	}

	class UiThread extends Thread {

		String rate;

		public UiThread(String rate) {
			super();
			this.rate = rate;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			Message msg = new Message();
			msg.what = 0;
			Bundle data = new Bundle();
			data.putString("rate", rate);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}

	}

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String rate = bundle.getString("rate");
			mHeartRate.setText("心率传感器数据："+"\n"+rate);
		};
	};
}
