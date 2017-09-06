package sim.android.mtkcit.testitem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import com.android.internal.telephony.PhoneConstants;
//import com.mediatek.featureoption.FeatureOption;
//import com.mediatek.common.featureoption.FeatureOption;
//import com.mediatek.featureoption.SimcomFeatureOption;
//import com.mediatek.featureoption.SimcomIDOption;

import sim.android.mtkcit.AutoTestActivity;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
import sim.android.mtkcit.cittools.CITTools;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.IBinder;
import com.mediatek.telephony.TelephonyManagerEx;
import sim.android.mtkcit.util.FeatureOption;


public class SIMSDTest extends TestBase implements OnCompletionListener  {

	private AlertDialog mDialog = null;
	private String TAG = "SIMSDTest";
	private TextView txt_item_name;
	private TextView txt_descript;
	private TextView txt_testType;
	private Vibrator vibrator = null; 
	boolean m_bBatteryTesting;
	boolean m_bTemperature;
	boolean m_bHeadsetTest;
	int nCurrentMusicVolume;
	String batteryStatus = "";
	AudioManager am = null;
	private MediaPlayer mp = null;
	public boolean IsAutoCITTesting = false;
	String s = "";
	String path1Music;
	String path2Music;
	public boolean sd1flag;
	public boolean sd2flag;
	private boolean logFlag = true;
	public boolean flag;
	private Button btn_success;
	private Button btn_fail;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.autotest_result);
		txt_testType = (TextView) findViewById(R.id.autotest_type);
		txt_item_name = (TextView) findViewById(R.id.item_name);
		txt_descript = (TextView) findViewById(R.id.descript);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);
//		if ((AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO)
//				|| (AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO)) {
//			// btn_success.setEnabled(CITBroadcastReceiver.MachinePass);
//			btn_success.setEnabled(false);
//
//		} else if ((AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO)
//				|| (AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO)) {
//			// btn_success.setEnabled(CITBroadcastReceiver.PCBAPass);
//			btn_success.setEnabled(false);
//
//		}
		Log.v(TAG, "oncreate");


	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume");
		initResourceRefs();
		super.onResume();
	}

	public void initResourceRefs() {
		s = "";
		am = (AudioManager) getSystemService("audio");
		s += getString(R.string.memory_info);
		int sdNum = ct.getSDPaths();
		Log.v(TAG, "sdNum = " + sdNum);
		s += ct.getAvailMemory() + "/" + ct.getTotalMemory() + "\n";
		// if (ct.getSDs()) {
		// LOGV(logFlag, TAG, "ct.getSDs()=" + ct.getSDs());
		// EmmcSimSDSpeakTest();
		// } else
		//
		if (ct.emmc) {
			EmmcSimSDSpeakTest();
		} else
			SimSDSpeakTest();
	}

	protected void EmmcSimSDSpeakTest() {
		LOGV(logFlag, TAG, "EmmcSimSDSpeakTest()");
		sd1flag = false;
		sd2flag = false;
		flag = true;

		int i = am.getStreamMaxVolume(3);
		int j = am.getStreamVolume(3);
		nCurrentMusicVolume = j;
		am.setStreamVolume(3, i, 0);
		ct.getSDPaths();
		if (!ct.checkSDCardMount(ct.SD1Path)) {
			s += getString(R.string.speak_test_hasnot_sdcard1);
			Toast.makeText(this, R.string.no_sd, 1).show();
			flag = false;
		} else {
			Log.v(TAG, "SD1Path()=" + ct.SD1Path);
			s += getString(R.string.SD1_info);
			s += ct.getSDinfo(ct.SD1Path, getBaseContext()) + "\n";
			// path1Music = ct.SD1Path + "/CIT/test.mp3";
			// File f = new File(path1Music);
			// if (!f.exists()) {
			// s += getString(R.string.speak_test_hasnot_sdcard1_file) + "\n";
			// sd1flag = false;
			// android.app.AlertDialog.Builder builder = (new
			// android.app.AlertDialog.Builder(
			// this))
			// .setTitle(R.string.speak_test_hasnot_sdcard1_file);
			// String s1 = getString(R.string.no_nusic);
			// android.app.AlertDialog.Builder builder1 = builder
			// .setMessage(s1);
			// mDialog = builder1.setPositiveButton(R.string.alert_dialog_ok,
			// null).create();
			// mDialog.show();
			// flag = false;
			//
			// } else
			sd1flag = true;

		}
		if (!ct.checkSDCardMount(ct.SD2Path)) {
			s += getString(R.string.speak_test_hasnot_sdcard2);
			Toast.makeText(this, R.string.no_sd, 1).show();
			flag = false;

		} else {
			LOGV(logFlag, TAG, "SD2Path()=" + ct.SD2Path);
			s += getString(R.string.SD2_info);
			s += ct.getSDinfo(ct.SD2Path, getBaseContext()) + "\n\n";
			path2Music = ct.SD2Path + "/CIT/test.mp3";
			LOGV(logFlag, TAG, "path2Music=" + path2Music);

			File f = new File(path2Music);
			if (!f.exists()) {
				s += getString(R.string.speak_test_hasnot_sdcard2_file);
				sd2flag = false;
				android.app.AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(
						this))
						.setTitle(R.string.speak_test_hasnot_sdcard2_file);
				String s1 = getString(R.string.no_nusic);
				android.app.AlertDialog.Builder builder1 = builder
						.setMessage(s1);
				mDialog = builder1.setPositiveButton(R.string.alert_dialog_ok,
						null).create();
				mDialog.show();
				flag = false;
			} else {
				sd2flag = true;
				playMusic(path2Music);
			}

		}
		if (sd1flag && sd2flag) {
			s += getString(R.string.speak_test_message);

		}
		s += "\n\n";
		s += SIMCardTest();
		s += "\n";
		// add_s wangkang 2012.03.29 Add Vibration Test
		// vibrationTest
		vibrator = (Vibrator) getSystemService("vibrator");
		long al[] = {2000L, 1000L };
		vibrator.vibrate(al, 0);
		s += getString(R.string.vibration_message);
		s += "\n\n";
		// add_e wangkang 2012.03.29 Add Vibration Test
		LOGV(logFlag, TAG, "flag ="+flag);

		if (!flag) {
			s += getString(R.string.fail);
		} else {
			btn_success.setEnabled(true);
			s += getString(R.string.success);
		}
		txt_descript.setText(s);
	}

	public void playMusic(String musicP) {
		LOGV(logFlag, TAG, "musicP=" + musicP);
		Uri u = Uri.parse(musicP);
		mp = MediaPlayer.create(this, u);
		// mp.setLooping(true);
		Log.v(TAG, "sd1flag=" + sd1flag + "sd2flag" + sd2flag);
		try {
			if (sd1flag && sd2flag) {
				// mp.prepare();
				//
				// mp.setOnCompletionListener(this);
				// } else
				mp.setLooping(true);

				mp.start();
			}
		} catch (Exception e) {
			Log.e(TAG, "play music error");
			return;
		}

	}

	protected void SimSDSpeakTest() {
		flag = true;

		sd1flag = false;
		sd2flag = true;
		String s = "";
		String pathMusic;
		String SDPath;
		int i = am.getStreamMaxVolume(3);
		int j = am.getStreamVolume(3);
		nCurrentMusicVolume = j;
		am.setStreamVolume(3, i, 0);
		if (!ct.checkSDCardMount(ct.SD1Path)) {
			s += getString(R.string.speak_test_hasnot_sdcard1);
			Toast.makeText(this, R.string.no_sd, 1).show();
			flag = false;
		} else {
			Log.v(TAG, "SD1Path()=" + ct.SD1Path);
			s += getString(R.string.SD1_info);
			s += ct.getSDinfo(ct.SD1Path, getBaseContext()) + "\n";
			path1Music = ct.SD1Path + "/CIT/test.mp3";
			LOGV(logFlag, TAG, "path2Music=" + path1Music);

			File f = new File(path1Music);
			if (!f.exists()) {
				s += getString(R.string.speak_test_hasnot_sdcard2_file);
				android.app.AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(
						this)).setTitle(R.string.speak_test_hasnot_sdcard_file);
				String s1 = getString(R.string.no_nusic);
				android.app.AlertDialog.Builder builder1 = builder
						.setMessage(s1);
				mDialog = builder1.setPositiveButton(R.string.alert_dialog_ok,
						null).create();
				mDialog.show();
				flag = false;
			} else {
				sd1flag = true;
				playMusic(path1Music);
			}

		}
		s += "\n\n";
		s += SIMCardTest();
		s += "\n";

		// add_s wangkang 2012.03.29 Add Vibration Test
		// vibrationTest
		vibrator = (Vibrator) getSystemService("vibrator");
		long al[] = { 0l, 2000L, 1000L };
		vibrator.vibrate(al, 0);
		s += getString(R.string.vibration_message);
		s += "\n\n";
		// add_e wangkang 2012.03.29 Add Vibration Test

		if (!flag) {
			s += getString(R.string.fail);
		} else {
			s += getString(R.string.success);
		}
		btn_success.setEnabled(flag);

		txt_descript.setText(s);

	}

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

	private String SIMCardTest() {

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
				flag = false;
				s += "Sim1:" + getString(R.string.test_sim_status_0) + "\n";
			} else if (sim1state == TelephonyManager.SIM_STATE_ABSENT) {
				flag = false;
				s += "Sim1:" + getString(R.string.test_sim_status_1) + "\n";
			} else {
				s += "Sim1:" + getSimStateName(sim1state) + ","
						+ getString(R.string.net_type)
						+ getNetworkTypeName(sim1Type) + "\n";
			}

			if (sim2state == TelephonyManager.SIM_STATE_UNKNOWN) {
				flag = false;

				s += "Sim2:" + getString(R.string.test_sim_status_0);
			} else if (sim2state == TelephonyManager.SIM_STATE_ABSENT) {
				flag = false;

				s += "Sim2:" + getString(R.string.test_sim_status_1);
			} else {
				s += "Sim2:" + getSimStateName(sim2state) + ","
						+ getString(R.string.net_type)
						+ getNetworkTypeName(sim2Type);
			}
		} else {
			sim1state = tm.getSimState(PhoneConstants.SIM_ID_1);
			sim1Type = tm.getNetworkType(PhoneConstants.SIM_ID_1);

			if (sim1state == TelephonyManager.SIM_STATE_UNKNOWN) {
				s += "Sim:" + getString(R.string.test_sim_status_0);
			} else if (sim1state == TelephonyManager.SIM_STATE_ABSENT) {
				s += "Sim:" + getString(R.string.test_sim_status_1);
			} else {
				s += "Sim: " + getString(R.string.card_state)
						+ getSimStateName(sim1state) + "\n"
						+ getString(R.string.net_type)
						+ getNetworkTypeName(sim1Type);
			}
		}
		return s;
	}

	public void onClick(View v) {
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

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");
		releaseRec();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void releaseRec() {

		// add_s wangkang 2012.03.29 Add Vibration Test
		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}
		// add_e wangkang 2012.03.29 Add Vibration Test

		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
		}
		am.setStreamVolume(3, nCurrentMusicVolume, 0);

	}

	int i = 1;

	@Override
	public void onCompletion(MediaPlayer mp) {

		if (i == 1) {
			playMusic(path2Music);
			i = 2;
		} else {
			playMusic(path1Music);
			i = 1;
		}

	}
}

