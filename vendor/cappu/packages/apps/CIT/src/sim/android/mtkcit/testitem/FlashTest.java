package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

import java.lang.reflect.Method;





import android.os.IBinder;
//import android.os.IHardwareService;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;


public class FlashTest extends TestBase  {
	// / private QcNvItems mNv;

	TextView tv_title;
	//private IHardwareService hardwareservice ;
 	Camera camera = null;
 	Parameters parameters;

  //  private ComboPreferences  mPreferences;

	private void initAllControl() {
		tv_title = (TextView) findViewById(R.id.flashtest_title);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

		ct.initButton(btn_success);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.test_flash);
       	initAllControl();

   //     mPreferences = new ComboPreferences(this);

	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		setFlashOn();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		setFlashOff();	//add wangkang 2012.5.10 the same as bug900
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		//setFlashOff();	//del wangkang 2012.5.10 the same as bug900
		super.onDestroy();
	}

	private void setFlashOn(){
		try
		{
		 camera = Camera.open(0);
		 parameters = camera.getParameters();
		 parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		 camera.setParameters(parameters);
		 btn_success.setEnabled(true);
		}
		catch(Exception ex)
		{
			
		}

	}	

	private void setFlashOff(){
		try
		{
			if( camera != null )
			{
				 parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				 camera.setParameters(parameters);
				 camera.release();
				 camera = null;
			}
		}
		catch(Exception ex)
		{
			
		}
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
		finish();
	}

}
