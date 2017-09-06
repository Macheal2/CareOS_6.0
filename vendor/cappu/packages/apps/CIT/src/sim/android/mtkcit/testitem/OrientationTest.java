package sim.android.mtkcit.testitem;

import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
//import sim.android.mtkcit.CommonDrive;

public class OrientationTest extends TestBase  {
    private static final String TAG = "OrientationTest";
    private TextView tvQuality;
    private TextView tvRid;
    private ArrowView avArrow;


    //private CommonDrive cmd;
    private boolean isTest;
    private int[] orienValues;
    private float[] mValues;

    private SensorManager mSensorManager;
    private Sensor mSensor;
   

    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);    
    
    setContentView(R.layout.orientation);
        isTest = true;
        //cmd = new CommonDrive();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        tvQuality = (TextView)findViewById(R.id.tv_quality);
        tvRid = (TextView)findViewById(R.id.tv_rid);
        avArrow = new ArrowView(this);

		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, Gravity.CENTER);
		addContentView(avArrow, lp);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
//		ct.initButton(btn_success);

	}

	protected void onResume() {
		super.onResume();
		isTest = true;
		mSensorManager.registerListener(eventListener, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

/*
		new Thread(){
			@Override
			public void run() {
				super.run();
				while (isTest) {
					try {
						sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//orienValues = cmd.getOrientationValues();
					mHandler.sendMessage(new Message());
				}
			}
		}.start();

*/
    }

/*
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
            if(orienValues != null){
			    tvQuality.setText("Quality = " + String.valueOf(orienValues[11]));
			
			    if(orienValues[11] >= 3)
			    {
			        bt_success.setEnabled(true);
			    }
			    else{
			        bt_success.setEnabled(false);
			    }
			
			    tvRid.setText("RID = " + String.valueOf((float)orienValues[8] * 360 / 65536));
            }
		}
	};

*/
	protected void onPause() {
		isTest = false;
            mSensorManager.unregisterListener(eventListener);

		SystemClock.sleep(500);
		super.onPause();
		Log.i(TAG, "onPause");
	}

	private SensorEventListener eventListener = new SensorEventListener() {
		
		public void onSensorChanged(SensorEvent event) {
			mValues = event.values;
			Log.i(TAG, "mValues[0] = " + mValues[0]);
			if (avArrow != null) {
				// tvQuality.setText("Quality = " +
				// String.valueOf(orienValues[11]));
				tvRid.setText("RID = " + String.valueOf((float) mValues[0]));
				tvQuality.setText("Quality = " + event.accuracy);

				avArrow.invalidate();
			}
		}
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//	LOGV(true, "feng", "accuracy=" + accuracy);
	//		tvQuality.setText("Quality = " + accuracy);

		}
	};


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

    private class ArrowView extends View {

    	private Paint mPaint = new Paint();
    	private Path mPath = new Path();
    	private boolean mAnimate;
    	private long mNextTime;

    	public ArrowView(Context context) {
    		super(context);
    		mPath.moveTo(0, -50);
    		mPath.lineTo(-20, 60);
    		mPath.lineTo(0, 50);
    		mPath.lineTo(20, 60);
    		mPath.close();
    		
    		mPaint.setAntiAlias(true);
    		mPaint.setColor(Color.WHITE);
    		mPaint.setStyle(Paint.Style.FILL);
    	}

    	@Override
    	protected void onDraw(Canvas canvas) {
    //			Paint paint = mPaint;
    		// canvas.drawColor(Color.WHITE);
    //			paint.setAntiAlias(true);
    //			paint.setColor(Color.WHITE);
    //			paint.setStyle(Paint.Style.FILL);
    		// paint.setTextSize(24);
    		int w = canvas.getWidth();
    		int h = canvas.getHeight();
    		int cx = w / 2;
    		int cy = h / 2;

    		canvas.save();
    		canvas.translate(cx, cy);
    		if (mValues != null) {
    			canvas.rotate(-mValues[0] );
    			canvas.drawPath(mPath, mPaint);
    		}
    		canvas.restore();
    	}
    }
}
