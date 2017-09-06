package sim.android.mtkcit.testitem;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
import sim.android.mtkcit.cittools.*;
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

public class MagenticCalibration extends TestBase implements
		Handler.Callback {

	private Timer timer;
	CITJNI citjni;
	private Handler myHandler;
	private final String TAG = "ProximityCalibration";
	Sensor mProimitySensor;
	String strPromityinfo;
	TextView tv_calibration;
	private Button m_Calibration;
	private IBinder binder;
	private CitBinder citBinder;
	private CITTools ct;
	private void initAllControl() {
//		binder = ServiceManager.getService("CitBinder");
//		citBinder = CitBinder.Stub.asInterface(binder);
		ct = CITTools.getInstance(this);
		citjni = new CITJNI();
		myHandler = new Handler(this);
		strPromityinfo = getString(R.string.proximity_calibration_info);
		tv_calibration = (TextView) findViewById(R.id.tv_calibration);
		m_Calibration = (Button) findViewById(R.id.m_calibration);
		m_Calibration.setOnClickListener(this);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.calibration);
		initAllControl();
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// timer.cancel();
	}

	int[] calivalue ={-1,-1,-1};
	int calivalueFlag;
	public void onClick(View v) {
		StringBuilder stringbuilder = new StringBuilder();

		 int id = v.getId();

		if (id == R.id.m_calibration) {
			strPromityinfo = getString(R.string.proximity_calibration_info);
			stringbuilder.append(strPromityinfo + "\n");
			stringbuilder.append(getString(R.string.cali_wait));
			tv_calibration.setText(stringbuilder);
			new Thread(new calibratThread()).start();
			
//			(new caThread()).start();
			return;

		} else
			finish();

	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle date;
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append(strPromityinfo + "\n");

		switch (msg.what) {

		case 0:
			date = msg.getData();
			calivalue = date.getIntArray("calivalue");
			if (calivalue[2] == 1) {
				try {
					ct.setPsCali_close(calivalue[0]);
					ct.setPsCali_far(calivalue[1]);
					ct.setPsCali_valid(calivalue[2]);
				} catch (RemoteException e) {
                  Log.e(TAG, "ct.setPsCali error");
					e.printStackTrace();
				}
				
				stringbuilder
						.append(getString(R.string.str_calibration_success));
			} else {
				stringbuilder
						.append(getString(R.string.str_calibration_failed));

			}
			stringbuilder.append("\n"+calivalue[2]);
			tv_calibration.setText(stringbuilder);

			break;
		}
		return false;
	}

	class calibratThread implements Runnable {

		@Override
		public void run() {
			calivalue = citjni.PSCali(); // 2s
			Log.v(TAG, "calivalue"+calivalue[0]+"calivalue"+calivalue[1]+"calivalue"+calivalue[2]);
			Message msg = new Message();
			Bundle date = new Bundle();
			date.putIntArray("calivalue", calivalue);
			msg.setData(date);
			msg.what = 0;
			myHandler.sendMessage(msg);
		}

	}
}
