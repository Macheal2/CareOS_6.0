package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class BatteryInfo extends TestBase {

	TextView tv_batteryinfo;
	TextView tv_result;
	private BroadcastReceiver mBatteryInfoReceiver;
	private String TAG = "BatteryInfo";
	private  final int VOLTAGE_MIN= 3400;
	private  final int VOLTAGE_MAX= 4350;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.battery_info);
		
		tv_batteryinfo = (TextView) findViewById(R.id.battery_info_res);
		tv_result = (TextView) findViewById(R.id.result);
		
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

		TextView tvJudge = (TextView) findViewById(R.id.judge);
		tvJudge.setText(String.format(getString(R.string.battery_info_judge),VOLTAGE_MIN,VOLTAGE_MAX));
		
		ct.initButton(btn_success);

	}

	@Override
	protected void onStart() {
		initAllControl();
		super.onStart();
	}

	private void initAllControl() {
		BroadcastReceiver mBatteryInfoReceiver = new BatteryReceiver();
		IntentFilter intentfilter = new IntentFilter(
				"android.intent.action.BATTERY_CHANGED");
		registerReceiver(mBatteryInfoReceiver, intentfilter);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		releaseRec();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void releaseRec() {
		if (mBatteryInfoReceiver != null) {
			this.unregisterReceiver(mBatteryInfoReceiver);
			mBatteryInfoReceiver = null;
		}
	}

	private void showBatteryInfo(Intent intent, String s) {
		int voltage = 0;
		float temp = 0;

		if ("android.intent.action.BATTERY_CHANGED".equals(s)) {
			voltage = intent.getIntExtra("voltage", 0);
			tv_batteryinfo.setText(voltage + "mV");
		}
		if (voltage > VOLTAGE_MIN && voltage < VOLTAGE_MAX) {
			tv_result.setText(R.string.success);
			btn_success.setEnabled(true);
		} else
			tv_result.setText(R.string.fail);
	}

	private class BatteryReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			showBatteryInfo(intent, s);
		}
	}

}
