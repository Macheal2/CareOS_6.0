package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

public class MagneticTest extends TestBase {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	private float x = 10000;
	private float y = 10000;

	private float z = 10000;
	private float quality = 0;
	
	private SensorManager mSensorManagerQ;
    private Sensor mSensorQ;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		SensorManager sensormanager = (SensorManager) getSystemService("sensor");
		mSensorManager = sensormanager;
		
		mSensorManagerQ = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorQ = mSensorManagerQ.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        
		setContentView(R.layout.magnetic_test);
		mt_TextView = new TextView[4];
		mt_TextView[0] = (TextView) findViewById(R.id.mt_tv_x);
		mt_TextView[1] = (TextView) findViewById(R.id.mt_tv_y);
		mt_TextView[2] = (TextView) findViewById(R.id.mt_tv_z);
		mt_TextView[3] = (TextView) findViewById(R.id.mt_tv_quality);
		mt_TextView[0].setText("X = " + x);
		mt_TextView[1].setText("Y = " + y);
		mt_TextView[2].setText("Z = " + z);
		mt_TextView[3].setText("Quality = " + quality);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		mListener = new senListerner();
		ct.initButton(btn_success);

	}

	protected void onResume() {
		super.onResume();
		SensorManager sensormanager = mSensorManager;
		SensorListener sensorlistener = mListener;
		sensormanager.registerListener(sensorlistener, 8);
		
		mSensorManagerQ.registerListener(eventListener, mSensorQ,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	protected void onStop() {
		super.onStop();
		SensorManager sensormanager = mSensorManager;
		SensorListener sensorlistener = mListener;
		sensormanager.unregisterListener(sensorlistener);
		mSensorManagerQ.unregisterListener(eventListener);
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();
		
		if(id == R.id.btn_success){
			b.putInt("test_result", 1);
		}else{
			b.putInt("test_result", 0);
		}
		
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	private SensorListener mListener;
	private SensorManager mSensorManager;
	TextView mt_TextView[];

	private class senListerner implements SensorListener {

		public void onAccuracyChanged(int i, int j) {
		}

		public void onSensorChanged(int i, float af[]) {
			StringBuilder stringbuilder = new StringBuilder();
			String s = stringbuilder.append("X = ").append(af[0]).toString();
			mt_TextView[0].setText(s);
			StringBuilder stringbuilder1 = (new StringBuilder()).append("Y = ");
			String s1 = stringbuilder1.append(af[1]).toString();
			mt_TextView[1].setText(s1);
			StringBuilder stringbuilder2 = (new StringBuilder()).append("Z = ");
			String s2 = stringbuilder2.append(af[2]).toString();
			mt_TextView[2].setText(s2);
			x = af[0];
			y = af[1];
			z = af[2];
			if (x < 400 && y < 400 && z < 400) {
				btn_success.setEnabled(true);
			} else {
				btn_success.setEnabled(false);

			}

		}

	}
	
	private SensorEventListener eventListener = new SensorEventListener() {
		
		public void onSensorChanged(SensorEvent event) {
				mt_TextView[3].setText("Quality = " + event.accuracy);
			}
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//	LOGV(true, "feng", "accuracy=" + accuracy);
		//		mt_TextView[3].setText("Quality = " + accuracy);
			}
		};

}
