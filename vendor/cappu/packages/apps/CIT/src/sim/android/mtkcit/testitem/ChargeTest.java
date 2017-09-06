package sim.android.mtkcit.testitem;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
import sim.android.mtkcit.AutoTestActivity;
import sim.android.mtkcit.cittools.CITShellExe;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class ChargeTest extends TestBase implements Handler.Callback {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	TextView tv_chargeinfo;
	// TextView tv_temperatureinfo;
	Camera camera;
	Parameters parameters;
	private BroadcastReceiver mBatteryInfoReceiver;
	String batteryStatus;
	boolean mBatteryTest = false;
	boolean mChargerTest = false;
	private String TAG = "ChargeTest";
	private CITShellExe cse;
	private String CHRGERVOLTAGE = "cat /sys/class/power_supply/battery/ChargerVoltage";
	private String BATTERYAVERAGECURRENT = "cat /sys/class/power_supply/battery/BatteryAverageCurrent";
	private String ChrgerVoltage = "0";
	private String BatteryAverageCurrent = "0";
	private boolean debugeFlag = true;
	private boolean mRun = true;
	private Handler myHandler;
	TextView tv_chargerinfo;
	private String result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.charge_test);
		tv_chargeinfo = (TextView) findViewById(R.id.chargeinfo);
		tv_chargerinfo = (TextView) findViewById(R.id.chargerinfo);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

		ct.initButton(btn_success);

		myHandler = new Handler(this);

	}

	@Override
	protected void onStart() {
		initAllControl();
		super.onStart();
	}

	private void initAllControl() {
		mRun = true;
		new Thread(new mThread()).start();
		mBatteryInfoReceiver = new BatteryReceiver();
		IntentFilter intentfilter = new IntentFilter(
				"android.intent.action.BATTERY_CHANGED");
		registerReceiver(mBatteryInfoReceiver, intentfilter);
	}

	public void releaseRec() {
		if (mBatteryInfoReceiver != null) {
			this.unregisterReceiver(mBatteryInfoReceiver);
			mBatteryInfoReceiver = null;
		}
		mRun = false;
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
		releaseRec();
		finish();
	}

	private String getChargerInfo(String location) {
		int ret;
		String[] cmd = { "/system/bin/sh", "-c", location };

		try {
			ret = CITShellExe.execCommand(cmd);
			if (ret != 0) {
				LOGV(debugeFlag, TAG, "exec  error");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error";

		}
		return CITShellExe.getOutput();

	}

	/***
	 * show the battery info
	 * 
	 * @param intent
	 * @param s
	 *            the action
	 */
	private void getBatteryInfo(Intent intent, String s) {
		int i = 0, j = 0, k = 0, current = 0, v = 0, mISenseVoltage = 0, InstatVolt = 0;
		float temp = 0;
		String s1 = "";

		if ("android.intent.action.BATTERY_CHANGED".equals(s)) {
			i = intent.getIntExtra("plugged", 0);
			j = intent.getIntExtra("status", 1);
			k = intent.getIntExtra("temperature", 0);
			v = intent.getIntExtra("voltage", 0);
			current = intent.getIntExtra("InstatVolt", 0);
			temp = intent.getIntExtra("temperature", 0) / 10;

			mISenseVoltage = intent.getIntExtra("ISenseVoltage", 0);
			// ChargerVoltage = intent.getIntExtra("ChargerVoltage", 0);
			// BatteryAverageCurrent =
			// intent.getIntExtra("BatteryAverageCurrent",
			// 0);
			Log.v(TAG, "plugged=" + i + ";status=" + j + ";temperature=" + k
					+ "InstatVolt=" + current);
			if (j == 2) {
				if (i > 0) {
					if (i == 1)
						batteryStatus = getString(R.string.battery_info_status_charging_ac);
					else
						batteryStatus = getString(R.string.battery_info_status_charging_usb);

				}
			} else if (j == 3) {
				batteryStatus = getString(R.string.battery_info_status_discharging);
			} else if (j == 4) {
				batteryStatus = getString(R.string.battery_info_status_notcharging);
			} else if (j == 5) {
				batteryStatus = getString(R.string.battery_info_status_full);
			} else {
				batteryStatus = getString(R.string.battery_info_status_unknow);
			}

			if (j == 4) {
				Toast warning = Toast.makeText(this,
						getString(R.string.battery_info_status_notcharging),
						Toast.LENGTH_LONG);
				warning.setGravity(Gravity.CENTER, 0, 0);
				warning.show();
			}
			// else {
			s1 = batteryStatus;
			s1 += "\n";
			s1 += getString(R.string.voltage) + v;
			s1 += "\n";
			s1 += getString(R.string.test_temperature_info) + temp;
			if (current < 0) {
				s1 += "(" + getString(R.string.temperature_abnorma) + ")";
			}
			if (temp > 50 || (j != 2 && j != 5)) {
				mBatteryTest = false;
			} else {
				mBatteryTest = true;
			}
		}
		tv_chargeinfo.setText(s1);

	}

	/***
	 * show the temperature info
	 * 
	 * @param intent
	 * @param s
	 */
	private boolean showTemperatureInfo(Intent intent, String s) {
		float i = 0;
		if ("android.intent.action.BATTERY_CHANGED".equals(s)) {
			i = intent.getIntExtra("temperature", 0) / 10;
			Log.v(TAG, "temperature=" + i);

			String s1 = (new StringBuilder())
					.append("The phone temperature is ").append(i).toString();

			StringBuilder stringbuilder = new StringBuilder();
			String s2 = getString(R.string.test_temperature_info);
			String s3 = stringbuilder.append(s2).append(i).toString();
			// .setText(s3);

		}
		if (i > 50)
			return false;
		else
			return true;
	}

	class mThread implements Runnable {
		String[] chargerInfo = new String[2];

		@Override
		public void run() {
			LOGV(debugeFlag, TAG, "RUN");
			while (mRun) {
				chargerInfo[0] = getChargerInfo(BATTERYAVERAGECURRENT);
				chargerInfo[1] = getChargerInfo(CHRGERVOLTAGE);
				LOGV(debugeFlag, TAG, "BATTERYAVERAGECURRENT = "
						+ chargerInfo[0] + "; CHRGERVOLTAGE" + chargerInfo[1]);

				Message msg = new Message();
				Bundle date = new Bundle();
				date.putStringArray("chargerinfo", chargerInfo);
				msg.setData(date);
				myHandler.sendMessage(msg);
			}
		}
	}

	private class BatteryReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {

			Log.v("fengxuanyang", "package~~~~~~~~~~" + intent.getPackage()
					+ "intent.getAction()~~~~" + intent.getAction());
			String s = intent.getAction();
			getBatteryInfo(intent, s);
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle date;
		date = msg.getData();
		String chargerValu;
		String chargerEle;
		chargerEle = date.getStringArray("chargerinfo")[0];
		chargerValu = date.getStringArray("chargerinfo")[1];

		String s1 = "";
		s1 += getString(R.string.charger_ma) + chargerEle;
		s1 += "\n";
		s1 += getString(R.string.charger_v) + chargerValu;
		s1 += "\n";
		if (Integer.parseInt(chargerEle) < 100) {
			s1 += "(" + getString(R.string.abnormal_value) + ")";
			s1 += "\n";
			mChargerTest = false;

		} else
			mChargerTest = true;
		if (mChargerTest && mBatteryTest) {
			result = getString(R.string.success);
			btn_success.setEnabled(true);
		} else
			result = getString(R.string.fail);
		s1 += "\n";
		s1 += result;
		tv_chargerinfo.setText(s1);
		return false;
	}
}
