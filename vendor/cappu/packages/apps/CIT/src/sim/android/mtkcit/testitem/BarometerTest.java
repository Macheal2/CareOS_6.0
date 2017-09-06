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

public class BarometerTest extends TestBase  
{

	private  SensorListener mListener;
	private  SensorManager mSensorManager;
	private  Sensor mBarometerSensor;
	private  TextView mTvBarometer;
	private  Button mBtnSuccess;
	private  Button mBtnFail;
	private void initAllControl() 
	{
		mTvBarometer = (TextView) findViewById(R.id.barometer_tv);
		mTvBarometer.setText("Pressure = -1");

		mBtnSuccess = (Button) findViewById(R.id.btn_success);
		mBtnSuccess.setOnClickListener(this);
		mBtnSuccess.setEnabled(false);
		mBtnFail = (Button) findViewById(R.id.btn_fail);
		mBtnFail.setOnClickListener(this);
		mListener = new SensorListener();
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		mSensorManager = (SensorManager) getSystemService("sensor");
		mBarometerSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_PRESSURE);

		setContentView(R.layout.test_barometer);
		initAllControl();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		boolean flag = mSensorManager.registerListener(mListener,
				mBarometerSensor, 3);
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
			StringBuilder stringbuilder = (new StringBuilder()).append("Pressure = ");
			float x = event.values[0];
			String s = stringbuilder.append(x).toString();
			mTvBarometer.setText(s);
			if( x > 800 )
			{
				mBtnSuccess.setEnabled(true);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) 
		{
		}

	}

}
