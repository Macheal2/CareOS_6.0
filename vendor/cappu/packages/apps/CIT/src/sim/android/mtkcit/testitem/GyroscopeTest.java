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

public class GyroscopeTest extends TestBase  {

	private  SensorListener mListener;
	private SensorManager mSensorManager;
	private Sensor mGyroscopeSensor;
	private int mCurArrow;
	private TextView mTvGyroscope[];
	private Button mBtnSuccess;
	private Button mBtnFail;
	private void initAllControl() 
	{
		mTvGyroscope = new TextView[3];
		mTvGyroscope[0] = (TextView) findViewById(R.id.gyroscope_tv_x);
		mTvGyroscope[1] = (TextView) findViewById(R.id.gyroscope_tv_y);
		mTvGyroscope[2] = (TextView) findViewById(R.id.gyroscope_tv_z);
		mTvGyroscope[0].setText("X = -1");
		mTvGyroscope[1].setText("Y = -1");
		mTvGyroscope[2].setText("Z = -1");

		mBtnSuccess = (Button) findViewById(R.id.btn_success);
		mBtnSuccess.setOnClickListener(this);
		mBtnSuccess.setEnabled(false);
		mBtnFail = (Button) findViewById(R.id.btn_fail);
		mBtnFail.setOnClickListener(this);
		mListener = new SensorListener();
	}

	protected void onCreate(Bundle bundle) 
	{
		super.onCreate(bundle);
		mSensorManager = (SensorManager) getSystemService("sensor");
		mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		setContentView(R.layout.test_gyroscope);
		initAllControl();
		initButton();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		boolean flag = mSensorManager.registerListener(mListener,
				mGyroscopeSensor, 3);
		String s = (new StringBuilder()).append("bSucceed is ").append(flag)
				.toString();
		Log.d("Prominity", s);
	}

	@Override
	protected void onStop() 
	{
		mSensorManager.unregisterListener(mListener);
		super.onStop();
	}

	private class SensorListener implements SensorEventListener
	{
		@Override
		public void onSensorChanged(SensorEvent event) 
		{
			StringBuilder stringbuilder = (new StringBuilder()).append("X = ");
			float x = event.values[0];
			String s = stringbuilder.append(x).toString();
			mTvGyroscope[0].setText(s);

			StringBuilder stringbuilder1 = (new StringBuilder()).append("Y = ");
			float y = event.values[1];
			String s1 = stringbuilder1.append(y).toString();
			mTvGyroscope[1].setText(s1);

			StringBuilder stringbuilder2 = (new StringBuilder()).append("Z = ");
			float z = event.values[2];
			String s2 = stringbuilder2.append(z).toString();
			mTvGyroscope[2].setText(s2);

			mBtnSuccess.setEnabled(true);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) 
		{
		}

	}
}
