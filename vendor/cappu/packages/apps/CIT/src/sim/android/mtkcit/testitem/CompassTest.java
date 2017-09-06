package sim.android.mtkcit.testitem;

import sim.android.mtkcit.R;
import android.app.Activity;
import android.hardware.*;
import sim.android.mtkcit.*;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CompassTest extends TestBase  {

	ImageView imageView[];
	private  SensorListener mListener;
	private SensorManager mSensorManager;
	private Sensor mCompassSensor;
	private float mValues[];
	int m_nCurArrow;
	TextView ms_tv_XYZ[];
	private int motionResult[];

	private void initAllControl() {
		ms_tv_XYZ[0] = (TextView) findViewById(R.id.ms_tv_x);
		ms_tv_XYZ[1] = (TextView) findViewById(R.id.ms_tv_y);
		ms_tv_XYZ[2] = (TextView) findViewById(R.id.ms_tv_z);
		ms_tv_XYZ[0].setText("X = -1");
		ms_tv_XYZ[1].setText("Y = -1");
		ms_tv_XYZ[2].setText("Z = -1");

		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);
		mListener = new SensorListener();
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mSensorManager = (SensorManager) getSystemService("sensor");
		mCompassSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		imageView = new ImageView[4];
		ms_tv_XYZ = new TextView[3];
		motionResult = new int[4];
		setContentView(R.layout.test_compass);
		initAllControl();
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean flag = mSensorManager.registerListener(mListener,
				mCompassSensor, 3);
		String s = (new StringBuilder()).append("bSucceed is ").append(flag)
				.toString();
		Log.d("Prominity", s);
	}

	@Override
	protected void onStop() {
		//
		// SensorManager sensormanager = mSensorManager;
		// SensorEventListener sensoreventlistener = mListener;
		// sensormanager.unregisterListener(sensoreventlistener);
		mSensorManager.unregisterListener(mListener);
		super.onStop();
	}

	private class SensorListener implements SensorEventListener

	{

		@Override
		public void onSensorChanged(SensorEvent event) {
			// event.

			StringBuilder stringbuilder = (new StringBuilder()).append("X = ");
			float x = event.values[0];
			String s = stringbuilder.append(x).toString();
			ms_tv_XYZ[0].setText(s);

			StringBuilder stringbuilder1 = (new StringBuilder()).append("Y = ");
			float y = event.values[1];
			String s1 = stringbuilder1.append(y).toString();
			ms_tv_XYZ[1].setText(s1);

			StringBuilder stringbuilder2 = (new StringBuilder()).append("Z = ");
			float z = event.values[2];
			String s2 = stringbuilder2.append(z).toString();
			ms_tv_XYZ[2].setText(s2);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
