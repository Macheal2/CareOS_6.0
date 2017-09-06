package sim.android.mtkcit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.cittools.DBHelper;
import sim.android.mtkcit.testitem.*;

// added by bruce for sim/SD card test begin
import android.telephony.TelephonyManager;
import com.mediatek.telephony.TelephonyManagerEx;
//import com.mediatek.common.featureoption.FeatureOption;
import com.android.internal.telephony.PhoneConstants;
import android.content.DialogInterface;
import sim.android.mtkcit.util.FeatureOption;
// added by bruce for sim/SD card test end

public class AutoTestActivity extends Activity implements View.OnClickListener {
	boolean isAutoPassOrFail;
	boolean isAllowBack = true;
	int layoutId;
	public Button btn_success;
	public Button btn_fail;
	public CITTools ct = CITTools.getInstance(this);;
	public boolean mFirstFlag = false;
	public String[] AcitItemStr = null;
	public int test_idx = 0;
	public int test_cnt = 0;
	public PowerManager pm;
	public PowerManager.WakeLock mWakeLock;
	public KeyguardLock mLock;
	public static boolean IsAutoCITTesting;
	public final int REQUEST_ASK = 1;
	public final int REQUEST_ASK_RESULT = 2;
	public final String WIFI_CITTEST = "wifi_cittest";
	public String TAG = "AutoTestActivity";
	public static int auto_test_type = -1;
	public static final String AUTO_TEST_TYPE = "auto_test_type";
	public ArrayList<HashMap<String, String>> FailItems = new ArrayList<HashMap<String, String>>();
	public ArrayList<String> FailItemStrings = new ArrayList<String>();
	public TextView txt_item_name;
	public TextView txt_descript;
	public TextView txt_testType;
	public AudioManager am = null;
	boolean m_bTemperature;
	boolean m_bHeadsetTest;
	boolean m_bPowerTesting;
	public AlertDialog mDialog = null;
	public int nCurrentMusicVolume;
	public String batteryStatus = "";
	public Vibrator vibrator = null;
	public MediaPlayer mp = null;
	public int BatteryReceiverStatus = 0;
	public int PowerReceiverStatus = 0;
	public int mheadSetReceiverStatus = 0;
	public boolean notJumpClassTest = false;
	private boolean success_btn_pass = false;
	DBHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "oncreate");

		setContentView(R.layout.autotest_result);
		if (savedInstanceState != null) {
			Log.v(TAG, "bundle != null");
			auto_test_type = savedInstanceState.getInt(AUTO_TEST_TYPE);
			test_idx = savedInstanceState.getInt("test_idx");
			AcitItemStr = savedInstanceState.getStringArray("AcitItemStr");
			FailItems = (ArrayList<HashMap<String, String>>) savedInstanceState
					.getSerializable("FailItems");
			test_idx++;
		} else {
			Log.v(TAG, "bundle == null");
			Intent intentCit = getIntent();
			auto_test_type = intentCit.getIntExtra(AUTO_TEST_TYPE, 0);
		}
		txt_testType = (TextView) findViewById(R.id.autotest_type);
		txt_item_name = (TextView) findViewById(R.id.item_name);
		txt_descript = (TextView) findViewById(R.id.descript);

		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		am = (AudioManager) getSystemService("audio");
		mFirstFlag = true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Bundle bundle;
		int result = 0;
		boolean flag = false;
		Log.v(TAG, "onActivityResult----requestCode=" + requestCode);
		if (resultCode == RESULT_OK) {
			ContentValues values = new ContentValues();
			// values.put("failItem", (String) entry.getValue());

			helper.del(test_idx);
			// helper.insert(values);
		}

		if (requestCode == REQUEST_ASK) {
			if (resultCode == RESULT_OK) {
				bundle = data.getExtras();
				result = bundle.getInt("test_result");
				if (result == 1)
					flag = true;
			}
			Log.v(TAG, "result=" + result);
			if (!flag) {
				int i = 0;
				HashMap<String, String> map;
				for (i = 0; i < FailItems.size(); i++) {
					map = FailItems.get(i);
					if (map.containsKey(Integer.toString(test_idx))) {
						FailItems.remove(i);
					}
				}
				HashMap<String, String> map1 = new HashMap<String, String>();
				map1.put(Integer.toString(test_idx), AcitItemStr[test_idx]);
				FailItems.add(map1);
				Cursor c = helper.Query(Integer.toString(test_idx));
				if (c.getCount() == 0) {
					ContentValues values = new ContentValues();
					values.put("name", AcitItemStr[test_idx]);
					values.put("itemid", test_idx);
					helper.insert(values);
				}
			}
			if (resultCode == RESULT_OK) {
				test_idx++;
				if (test_idx >= test_cnt) {
					ShowTestResult(test_idx);
				} else {
					CommonTest(AcitItemStr[test_idx]);
				}
			} else {

			}
		} else if (requestCode == REQUEST_ASK_RESULT) {
//			Log.v(TAG, "requestCode===" + REQUEST_ASK_RESULT);
//			Log.v(TAG, "resultCode==" + resultCode);

			if (resultCode == RESULT_OK) {
				bundle = data.getExtras();
				result = bundle.getInt("test_result");
//				Log.v(TAG, "result=================" + result);
				if (result == 1) {
					Log.v(TAG, "result=1");
					btn_success.setEnabled(true);
				} else {
					// btn_success.setEnabled(false);
				}
			} else {
				// btn_success.setEnabled(false);
			}

		}
	}

	@Override
	protected void onRestart() {
		Log.v(TAG, "onRestart");
		Log.v(TAG, "onRestart IsAutoCITTesting =" + IsAutoCITTesting);
		if (IsAutoCITTesting) {
			Log.v(TAG, "IsAutoCITTesting =" + IsAutoCITTesting);

			IsAutoCITTesting = false;
			success_btn_pass = true;
			txt_descript.setText(R.string.complete);
			ShowTestResult(test_idx);
		}
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");

		initRec();
		pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"SimpleTimer");
		mWakeLock.acquire();

		// KeyguardManager mKeyGuardManager = (KeyguardManager)
		// getSystemService(KEYGUARD_SERVICE);
		// mLock = mKeyGuardManager.newKeyguardLock("TestActivity");
		// mLock.disableKeyguard();
	}

	protected void initRec() {
		// Log.v(TAG, "onStart IsAutoCITTesting = " + IsAutoCITTesting);
		Log.v(TAG, "initRec IsAutoCITTesting = " + IsAutoCITTesting);

		ct.CitBinderPrepare();
		helper = new DBHelper(this.getApplicationContext());
		// if ((auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO)
		// || (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO)) {
		// success_btn_pass = CITBroadcastReceiver.MachinePass;
		//
		// } else if ((auto_test_type ==
		// CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO)
		// || (auto_test_type ==
		// CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO)) {
		// success_btn_pass = CITBroadcastReceiver.PCBAPass;
		//
		// }

		// btn_success.setEnabled(success_btn_pass);

	}

	/*
	 * 
	 * <string name="Machine_Auto_Test">整机自动测试</string> <string
	 * name="PCBA_Auto_Test">PCBA自动测试</string> <string


	 * name="versionname">版本测试</string> <string
	 * name="WBG_Auto_Test">WBG自动测试</string> <string
	 * name="DEPUTY_AUTO_TEST">副板测试</string>
	 */
	@Override
	protected void onResume() {
		LOGV(true, TAG, "onResume");
		super.onResume();
		Log.i("bruce_nan", "AutoTestActivity_onResume_01: auto_test_type = " + auto_test_type);
		if (AcitItemStr == null) {
			if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_SENSOR_AUTO) {
				AcitItemStr = getResources().getStringArray(
						R.array.MachineSensorTestStrings);
				txt_testType.setText(R.string.Machine_Sensor_Auto_Test);
			} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO) {
				AcitItemStr = getResources().getStringArray(
						R.array.MachineAutoTestStrings);
				txt_testType.setText(R.string.Machine_Auto_Test);
			} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO) {
				AcitItemStr = getResources().getStringArray(
						R.array.PCBASensorTestStrings);
				txt_testType.setText(R.string.PCBA_Sensor_Auto_Test);
			} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO) {
				AcitItemStr = getResources().getStringArray(
						R.array.PCBAAutoTestStrings);
				txt_testType.setText(R.string.PCBA_Auto_Test);
			} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_WBG_AUTO) {
				AcitItemStr = getResources().getStringArray(
						R.array.WBGTestStrings);
				txt_testType.setText(R.string.WBG_Auto_Test);
			} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_DEPUTY) {
				AcitItemStr = getResources().getStringArray(
						R.array.DeputyTestString);
				txt_testType.setText(R.string.Deputy_Auto_Test);
			}
			
			

			test_cnt = AcitItemStr.length;
		}
		
		Log.i("bruce_nan", "AutoTestActivity_onResume_02: mFirstFlag = " + mFirstFlag + "; test_idx = " + test_idx);
		if (mFirstFlag) {
			mFirstFlag = false;
			CommonTest(AcitItemStr[test_idx]);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.v(TAG, "onSaveInstanceState");
		savedInstanceState.putInt("auto_test_type", auto_test_type);
		savedInstanceState.putInt("test_idx", test_idx);
		savedInstanceState.putStringArray("AcitItemStr", AcitItemStr);
		savedInstanceState.putSerializable("FailItems", FailItems);
		super.onSaveInstanceState(savedInstanceState);
	}

	// @Override
	// public void onRestoreInstanceState(Bundle savedInstanceState) {
	// Log.v(TAG, "onRestoreInstanceState");
	//
	// test_type = savedInstanceState.getInt("test_type");
	// test_idx = savedInstanceState.getInt("test_idx");
	// super.onRestoreInstanceState(savedInstanceState);
	// }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
		}
		// Checks whether a hardware keyboard is available
		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
			Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
		} else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
			Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");
		releaseRec();

		if (test_idx >= test_cnt) {
			test_idx = test_cnt - 1;
		}
		if (notJumpClassTest) {
			HandleAfterTest(AcitItemStr[test_idx]);

		}

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (test_idx < test_cnt) {
			menu.add(1, Menu.FIRST, 0, R.string.retest);
		}
		menu.add(2, Menu.FIRST + 1, 0, R.string.exit);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int i = 0;
		
		switch (item.getItemId()) {
		case 1:
			if (test_idx >= test_cnt) {
				test_idx = 0;
				btn_success.setVisibility(View.VISIBLE);
				btn_fail.setVisibility(View.VISIBLE);
				
				if (!FailItems.isEmpty()) {
					for (i = 0; i < FailItems.size(); i++) {
						FailItems.remove(i);
					}
				}
			}
			HandleAfterTest(AcitItemStr[test_idx]);
			CommonTest(AcitItemStr[test_idx]);
			break;
		case 2:
			finish();
			break;
		}
		return true;
	}

	protected void ShowTestResult(int idx) {
		Log.v(TAG, "ShowTestResult(" + idx + ")");
		int str_title = R.string.app_name;
		String strMessage = "";
		String str;
		if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO) {
			str_title = R.string.PCBA_Auto_Test;
		} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO) {
			str_title = R.string.PCBA_Sensor_Auto_Test;
		} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO) {
			str_title = R.string.Machine_Auto_Test;

		} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_SENSOR_AUTO) {
			str_title = R.string.Machine_Sensor_Auto_Test;

		} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_WBG_AUTO) {
			str_title = R.string.WBG_Auto_Test;

		} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_DEPUTY) {
			str_title = R.string.Deputy_Auto_Test;
		}
		if (idx < test_cnt) {
			strMessage = AcitItemStr[idx] + " " + "(" + (idx + 1) + "/"
					+ test_cnt + ")\n";
			txt_item_name.setText(strMessage);

		} else {
			btn_success.setVisibility(View.GONE);
			btn_fail.setVisibility(View.GONE);
			HashMap<String, String> map;
			Entry entry = null;
			txt_testType.setText(str_title);
			txt_descript.setText("");
			if (FailItems.isEmpty()) {
				txt_item_name.setText(R.string.result_all_success);
				if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO) {
					ct.setCitAutoTestFlag(CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO);
				} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO) {
					ct.setCitAutoTestFlag(CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO);
				} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO) {
					ct.setCitAutoTestFlag(CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO);
				} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_SENSOR_AUTO) {
					ct.setCitAutoTestFlag(CITBroadcastReceiver.TEST_TYPE_MACHINE_SENSOR_AUTO);
				} else if (auto_test_type == CITBroadcastReceiver.TEST_TYPE_WBG_AUTO) {
					ct.setCitAutoTestFlag(CITBroadcastReceiver.TEST_TYPE_WBG_AUTO);
				}
			} else {
				for (int i = 0; i < FailItems.size(); i++) {
					map = FailItems.get(i);
					Iterator it = map.entrySet().iterator();
					entry = (Entry) it.next();
					strMessage += (String) entry.getValue();
					strMessage += "\n";
				}
				txt_item_name.setText(R.string.result_fail_items);
				txt_descript.setText(strMessage);
			}
		}
	}

	protected void HandleAfterTest(String strAct) {
		int i = 0;
		String ItemStr[] = getResources().getStringArray(
				R.array.CommonTestStrings);
		for (i = 0; i < ItemStr.length; i++) {
			if (strAct.equals(ItemStr[i])) {
				break;
			}
		}
		Log.v(TAG, "HandleAfterTest(" + strAct + ")" + "i==" + i);

		switch (i) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:

			break;
		case 4:
			break;
		case 5:
			break;
		case 6:
			break;
		case 7:
			break;
		case 8:
			break;
		case 9:
			break;
		case 10:
			break;
		case 11:
//			ct.afterWifitest();
			break;
		case 12:
//			ct.afterBluetoothtest();
			break;
		case 13:
			break;
		case 14:
			break;
		case 15:

			break;
		case 16:
			break;
		case 17:
			break;
		case 18:
			break;
		case 22:

			break;
		case 26:
			break;
		case 31:
			break;
		case 33:
			notJumpClassTest = false;
			if (mp != null) {
				mp.stop();
				mp.release();
				mp = null;
			}
			if (am != null) {
				am.setStreamVolume(3, nCurrentMusicVolume, 0);

			}
			break;
		case 34:
			break;		// Hall Test
		case 35:
			break;		// Twinkle Test
		case 36:
			break;		// Dual MIC Test
		case 37:
			break;		// Dual Speaker Test
		case 38:
		case 39:
		case 40:
		case 41:
			break;
		default:
			break;
		}
	}

	protected String ShowTestInfo(int test_id) {
		String testItem = ct.getStringFromRes(this, test_id);
		return testItem;
	}

	protected void releaseRec() {

		if (mWakeLock != null) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	protected void CommonTest(String strAct/* , int type */) {
		int i = 0;
		String ItemStr[] = getResources().getStringArray(
				R.array.CommonTestStrings);
		for (i = 0; i < ItemStr.length; i++) {
			if (strAct.equals(ItemStr[i])) {
				break;
			}
		}
		Intent intent = new Intent();
		btn_success.setEnabled(true);

		Log.v(TAG, "CommonTest(" + strAct + ")" + "i==" + i);
		Log.i("bruce_nan", "CommonTest_nfl: i = " + i + "; strAct = " + strAct);
		switch (i) {
		case 0:
            //speakandStorageTest();
            storageTest(); //added by bruce for sd card test
			break;
		case 1:
			//SIMOrUIMCardTest();
			SIMCardTest(); //added by bruce for sim card test
			break;
		case 2:
			IsAutoCITTesting = true; 
			// modified by bruce for camera test begin
			//ct.cameraTest(this);
			intent.setClass(this, MainCamera.class);
			startActivity(intent);
			// modified by bruce for camera test end
			break;
		case 3:
			// chargeTest();
			intent.setClass(this, ChargeTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 4:
			powerTest();
			break;
		case 5:
			intent.setClass(this, LEDVibraTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 6:

			intent.setClass(this, MikeTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 7:
			intent.setClass(this, EarPhoneTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 8:
			intent.setClass(this, KeyTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 9:
			intent.setClass(this, MotionSenorTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 10:
			intent.setClass(this, MagneticTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;

		case 11:
			IsAutoCITTesting = true; 
//			ct.wifitest();
			// wifitest();
			intent.setClass(this, Wifirssitest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 12:
			IsAutoCITTesting = true; 
//			ct.bluetoothtest();
			intent.setClass(this, Bluetoothtest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 13:
			IsAutoCITTesting = true;
			intent.setClass(this, LCDTest.class);
			Log.v(TAG, "CommonTest  IsAutoCITTesting  =  " + IsAutoCITTesting);
			// startActivityForResult(intent, REQUEST_ASK);
			startActivity(intent);

			break;
		case 14:
			intent.setClass(this, FlashTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 15:
			// temperatureTest();
			break;
		case 16:
			intent.setClass(this, GPSTest.class);
			// IsAutoCITTesting = true;
			// btn_success.setEnabled(true);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 17:
//			intent.setClass(this, FMTest.class);
//			startActivityForResult(intent, REQUEST_ASK);
			IsAutoCITTesting = true; 
			ct.FMTest();

			break;

		case 18:
			// mod_s wangkang 2012.4.25 bug178
			// intent.setClassName("com.android.settings",
			// "com.android.settings.MasterClear");
			intent.setClassName("com.android.settings",
					"com.android.settings.Settings");
			intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
					"com.android.settings.MasterClear");
			// mod_e wangkang 2012.4.25 bug178
			IsAutoCITTesting = true;
			startActivity(intent);
			break;
		case 19:
			intent.setClass(this, Version.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 20:
			intent.setClass(this, LightSenor.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 21:
			intent.setClass(this, ProximitySenor.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 22:
			intent.setClass(this, SIMSDTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 23:
			IsAutoCITTesting = true;
			// modified by bruce for camera test begin
			//ct.subCameraTest(this);
			intent.setClass(this, SubCamera.class);
			startActivity(intent);
			// modified by bruce for camera test end
			break;
		case 26:
			intent.setClass(this, TouchTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		// add_s wangkang 2012.5.2 added keylightTest
		case 27:
			intent.setClass(this, KeylightTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		// add_e wangkang 2012.5.2 added keylightTest
		case 28:
			intent.setClass(this, MultiTouchActivity.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 29:
			intent.setClass(this, BrightnessTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 31:
			intent.setClass(this, OrientationTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 32:
			intent.setClass(this, VibraTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 33:
			notJumpClassTest = true;
			speakandStorageTest();
			break;
		case 34:
			intent.setClass(this, HallTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 35:		// Twinkle Test
			intent.setClass(this, TwinkleTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 36:		// Dual MIC Test
			intent.setClass(this, DualMicTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 37:		// Dual Speaker Test
			intent.setClass(this, DualSpeakerTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;
		case 38:
			intent.setClass(this, BarometerTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;		
		case 39:
			intent.setClass(this, OTGTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;		
		case 40:
			intent.setClass(this, HDMITest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;		
		case 41:
			intent.setClass(this, GyroscopeTest.class);
			startActivityForResult(intent, REQUEST_ASK);
			break;	
		// added by bruce for fp test begin
        case 42:
            Log.i("bruce_nan", "CommonTest_auto_nfl_42");
            IsAutoCITTesting = true;
			ct.fpTest();
			break;
		case 43:
			intent.setClass(this, HeartRateSenor.class);//dengying add heart rate
			startActivity(intent);
			break;					
        default:
			break;
        // added by bruce for fp test end
		}
	}

	private void speakandStorageTest() {
		String pathMusic;
		am = (AudioManager) getSystemService("audio");
		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
				mp.release();
				mp = null;
			}
		}

		int i = am.getStreamMaxVolume(3);
		int j = am.getStreamVolume(3);
		nCurrentMusicVolume = j;
		am.setStreamVolume(3, i, 0);
		mp = MediaPlayer.create(this, R.raw.test);
		txt_descript.setText(R.string.loudspeaker_test);

		try {
			mp.setLooping(true);
			mp.start();
			btn_success.setEnabled(true);
		} catch (Exception e) {
			return;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!isAllowBack) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		int i = 0;
		HashMap<String, String> map;
		for (i = 0; i < FailItems.size(); i++) {
			map = FailItems.get(i);
			if (map.containsKey(Integer.toString(test_idx))) {
				FailItems.remove(i);
			}
		}
		Log.v(TAG, "onClick");
		switch (id) {

		case R.id.btn_success:

			HandleAfterTest(AcitItemStr[test_idx]);

			test_idx++;
			if (test_idx >= test_cnt) {
				ShowTestResult(test_idx);
			} else {

				CommonTest(AcitItemStr[test_idx]);
			}
			break;
		case R.id.btn_fail:
			HashMap<String, String> map1 = new HashMap<String, String>();
			map1.put(Integer.toString(test_idx), AcitItemStr[test_idx]);
			FailItemStrings.add(AcitItemStr[test_idx]);
			FailItems.add(map1);
			HandleAfterTest(AcitItemStr[test_idx]);
			test_idx++;
			if (test_idx >= test_cnt) {
				ShowTestResult(test_idx);
			} else {

				CommonTest(AcitItemStr[test_idx]);
			}
			break;
		}
	}

	private BroadcastReceiver mPowerReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				Log.d(TAG, "ACTION_SCREEN_ON");
				// mScreenOff = false;
				btn_success.setEnabled(true);
				m_bPowerTesting = false; // add wangkang 2012.4.19 bug186

			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d(TAG, "ACTION_SCREEN_OFF");
				// mScreenOff = true;
			}
		}
	};

	protected void powerTest() {

		m_bPowerTesting = true; // add wangkang 2012.4.19 bug186
		btn_success.setEnabled(false);
		IsAutoCITTesting = false;
		Log.v(TAG, "powerTest  IsAutoCITTesting=" + IsAutoCITTesting);
		txt_descript.setText(R.string.power_message);
		ShowTestResult(test_idx);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		this.registerReceiver(mPowerReceiver, intentFilter);
		PowerReceiverStatus = 1;
	}

    // added by bruce for sim/SD card test begin
	private String getSimStateName(int simstate) {
		switch (simstate) {
		case TelephonyManager.SIM_STATE_UNKNOWN:
			return getString(R.string.test_sim_status_0);
		case TelephonyManager.SIM_STATE_ABSENT:
			return getString(R.string.test_sim_status_1);
		case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			return getString(R.string.test_sim_status_2);
		case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			return getString(R.string.test_sim_status_3);
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			return getString(R.string.test_sim_status_4);
		case TelephonyManager.SIM_STATE_READY:
			return getString(R.string.test_sim_status_5);
		default:
			return "";
		}
	}

	private String getNetworkTypeName(int netType) {
		switch (netType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "CDMA - EvDo rev. 0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "CDMA - EvDo rev. A";
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return "CDMA - EvDo rev. B";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "CDMA - 1xRTT";
		default:
			return "UNKNOWN";
		}
	}
	
	private void SIMCardTest() {
		//TelephonyManager tm = (TelephonyManager) (getSystemService(Context.TELEPHONY_SERVICE));
		TelephonyManagerEx tm = TelephonyManagerEx.getDefault();
		int sim1state;
		int sim2state;
		int sim1Type;
		int sim2Type;
		String s = "";
		if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
			sim1state = tm.getSimState(PhoneConstants.SIM_ID_1);
			sim2state = tm.getSimState(PhoneConstants.SIM_ID_2);
			sim1Type = tm.getNetworkType(PhoneConstants.SIM_ID_1);
			sim2Type = tm.getNetworkType(PhoneConstants.SIM_ID_2);

			if (sim1state == TelephonyManager.SIM_STATE_UNKNOWN) {
				s += "Sim1:\n" + getString(R.string.test_sim_status_0) + "\n";
			} else if (sim1state == TelephonyManager.SIM_STATE_ABSENT) {
				s += "Sim1:\n" + getString(R.string.test_sim_status_1) + "\n";
			} else {
				s += "Sim1: \n" // + getString(R.string.card_state)
						+ getSimStateName(sim1state)
						+ ","
						+ getString(R.string.net_type)
						+ getNetworkTypeName(sim1Type) + "\n";
			}

			if (sim2state == TelephonyManager.SIM_STATE_UNKNOWN) {
				s += "Sim2:\n" + getString(R.string.test_sim_status_0);
			} else if (sim2state == TelephonyManager.SIM_STATE_ABSENT) {
				s += "Sim2:\n" + getString(R.string.test_sim_status_1);
			} else {
				s += "Sim2: \n" // + getString(R.string.card_state)
						+ getSimStateName(sim2state)
						+ ","
						+ getString(R.string.net_type)
						+ getNetworkTypeName(sim2Type);
			}
		} else {
			sim1state = tm.getSimState(PhoneConstants.SIM_ID_1);
			sim1Type = tm.getNetworkType(PhoneConstants.SIM_ID_1);

			if (sim1state == TelephonyManager.SIM_STATE_UNKNOWN) {
				s += "Sim:\n" + getString(R.string.test_sim_status_0);
			} else if (sim1state == TelephonyManager.SIM_STATE_ABSENT) {
				s += "Sim:\n" + getString(R.string.test_sim_status_1);
			} else {
				s += "Sim: \n" + getString(R.string.card_state)
						+ getSimStateName(sim1state) + ","
						+ getString(R.string.net_type)
						+ getNetworkTypeName(sim1Type);
			}
		}

		AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(this))
				.setTitle(R.string.test_item_simoruim).setMessage(s);
		builder.setPositiveButton(R.string.alert_dialog_ok, 
		        new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                        HandleAfterTest(AcitItemStr[test_idx]);
            			test_idx++;
            			if (test_idx >= test_cnt) {
            				ShowTestResult(test_idx);
            			} else {
            				CommonTest(AcitItemStr[test_idx]);
            			}
                    }  
                });
        builder.setNegativeButton(R.string.alert_dialog_cancel,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
            			HashMap<String, String> map = new HashMap<String, String>();
            			map.put(Integer.toString(test_idx), AcitItemStr[test_idx]);
            			FailItemStrings.add(AcitItemStr[test_idx]);
            			FailItems.add(map);
            			HandleAfterTest(AcitItemStr[test_idx]);
            			test_idx++;
            			if (test_idx >= test_cnt) {
            				ShowTestResult(test_idx);
            			} else {

            				CommonTest(AcitItemStr[test_idx]);
            			}
                    }  
                });  
        builder.create().show();
		//builder.setPositiveButton(R.string.alert_dialog_ok, null).create().show();
		return;
	}

	private void storageTest() {
		String pathMusic;
		String text = "";

		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
				mp.release();
				mp = null;
			}
		}
		// mod_s wangkang 2012.4.24 bug189
		ct.getSDPaths();

		// SDCard1 test
		// Unnecessary!

		if (!ct.checkSDCardMount(ct.SD2Path)) {
		    text += "没有插入存储卡";
		}else{
		    text += "已经插入存储卡";;
		}

        AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(this))
				.setTitle(R.string.storagetest).setMessage(text);
		builder.setPositiveButton(R.string.alert_dialog_ok, 
		        new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                        HandleAfterTest(AcitItemStr[test_idx]);
            			test_idx++;
            			if (test_idx >= test_cnt) {
            				ShowTestResult(test_idx);
            			} else {
            				CommonTest(AcitItemStr[test_idx]);
            			}
                    }  
                });
        builder.setNegativeButton(R.string.alert_dialog_cancel,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
            			HashMap<String, String> map = new HashMap<String, String>();
            			map.put(Integer.toString(test_idx), AcitItemStr[test_idx]);
            			FailItemStrings.add(AcitItemStr[test_idx]);
            			FailItems.add(map);
            			HandleAfterTest(AcitItemStr[test_idx]);
            			test_idx++;
            			if (test_idx >= test_cnt) {
            				ShowTestResult(test_idx);
            			} else {

            				CommonTest(AcitItemStr[test_idx]);
            			}
                    }  
                });  
        builder.create().show();
	}
// added by bruce for sim/SD card test end

	protected void LOGV(boolean flag, String tag, String msg) {
		if (flag)
			Log.v(tag, msg);
	}

	protected void LOGE(boolean flag, String tag, String msg) {
		if (flag)
			Log.v(tag, msg);
	}

	protected void LOGI(String tag, String msg) {
		Log.v(tag, msg);
	}

}
