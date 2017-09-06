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
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.cittools.CitBinder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.Context;
import com.android.internal.telephony.PhoneConstants;

//import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import sim.android.mtkcit.util.FeatureOption;

// end
public class Version extends TestBase{
	int len = 17;
	TextView tv_Model, tv_software, tv_builddate, tv_hardware, tv_baseband, tv_sn;
	TextView tv_gsm_ft, tv_gsm_bt, tv_wcdma_bt, tv_wcdma_ft;

	private IBinder binder;
	private CitBinder citBinder;
	private static int citFlag;
	TextView tv_CitFlag, tv_Imei1, tv_Imei2;
	String imeiStr = null, imeiStr2 = null;
	private Button btn_success;
	private Button btn_fail;
	private String TAG = "Version";
	private CITTools ct;

	private void initAllControl() {
		ct = CITTools.getInstance(this);

		tv_Model = (TextView) findViewById(R.id.sn_model);
		tv_software = (TextView) findViewById(R.id.software_title);
//		tv_builddate = (TextView) findViewById(R.id.build_date);
		tv_hardware = (TextView) findViewById(R.id.hardware_title);
		tv_baseband = (TextView) findViewById(R.id.baseband_version);
		tv_sn = (TextView) findViewById(R.id.sn_version);
		// BT FT TEST
		tv_gsm_bt = (TextView) findViewById(R.id.gsm_bt);
		tv_gsm_ft = (TextView) findViewById(R.id.gsm_ft);
		tv_wcdma_bt = (TextView) findViewById(R.id.wcdma_bt);
		tv_wcdma_ft = (TextView) findViewById(R.id.wcdma_ft);

		tv_CitFlag = (TextView) findViewById(R.id.cit_flag);
		tv_Imei1 = (TextView) findViewById(R.id.imei1);
		if(FeatureOption.MTK_GEMINI_SUPPORT)
		{
			tv_Imei2 = (TextView) findViewById(R.id.imei2);
		}
		// end
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

//		ct.initButton(btn_success);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TelephonyManager teleMgr = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);			
		this.setContentView(R.layout.test_version);

		initAllControl();
		String customVerStr = getSystemproString("ro.custom.build.version");
		Log.i("bruce_nan", "CIT_version_nfl_01: customVerStr = " + customVerStr);
		String[] arrTmp = customVerStr.split("_A");
		String s = getString(R.string.sn_model);
		s += ":";
		// modified by bruce for Version test begin
        //s += arrTmp[0];
        String model = getSystemproString("ro.product.model");
        Log.i("bruce_nan", "CIT_version_nfl_02: model = " + model);
        s += model;
        // modified by bruce for Version test end
		tv_Model.setText(s);
		s = getString(R.string.software_title);
		s += ":";
        //s += customVerStr;
        String diaplayId = getSystemproString("ro.build.display.id");
        Log.i("bruce_nan", "CIT_version_nfl_03: diaplayId = " + diaplayId);
        s += diaplayId;
		tv_software.setText(s);
		
//		s = getString(R.string.build_date);
//		s += ":";
//		s += getSystemproString("ro.build.date");
//		tv_builddate.setText(s);

		String hardware = getSystemproString("ro.board.platform");
		Log.i("bruce_nan", "CIT_version_nfl_04: hardware = " + hardware);
		s = getString(R.string.hardware_title);
		s += ":";
        //s += getSystemproString("ro.hardware.version");
        s += hardware.toUpperCase();
        
        //s += Build.HARDWARE;
		tv_hardware.setText(s);

		s = getString(R.string.baseband_version);
		s += ":";
		s += getSystemproString("gsm.version.baseband");
		tv_baseband.setText(s);

		s = getString(R.string.sn_version);
		s += ":";
		//String sn = teleMgr.getSimSerialNumber();
		//String sn = Build.SERIAL;
//		String[] snString = this.getResources()
//				.getStringArray(R.array.snLength);
//		len = Integer.parseInt(snString[0].trim());

        String sn = SystemProperties.get("gsm.serial");
		len =  CITBroadcastReceiver.SN_Length;
		Log.i(TAG, "len ="+len);
		Log.i("bruce_nan", "CIT_version_nfl_05: sn = " + sn + "; length = " + sn.length());
		//sn = sn.substring(0, sn.lastIndexOf(" "));
		sn = sn.replaceAll("\\s*", "");
		if ((sn != null) && (sn.length() >= 17)){
		    sn = sn.substring(0, 17);
		}
		Log.i("bruce_nan", "CIT_version_nfl_06: sn = " + sn + "; length = " + sn.length());
        
		if (sn == null) {
			s += "can not get the sn ";
			tv_sn.setText(s);
			return;
		}else if (sn.length() == len) {

			sn = sn.trim();
			Log.i(TAG, "sn.trim() =" + sn);

			String s1 = getString(R.string.GSM_BT);
			String s2 = getString(R.string.GSM_FT);
			String s3 = getString(R.string.WCDMA_BT);
			String s4 = getString(R.string.WCDMA_FT);
			char ch2 = 'n';
			char ch = 'a';
			if (len == 18 || len == 17) {
				ch2 = sn.charAt(15);
				ch = sn.charAt(16);
			} else if (len == 16) {
				ch2 = sn.charAt(14);
				ch = sn.charAt(15);
			}

			if (ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5') {
				if (ch == '2' || ch == '3' || ch == '4' || ch == '5') {
					s1 += ":GSM BT pass";
					s2 += ":GSM FT pass";
				} else {
					s1 += ":GSM BT pass";
					s2 += ":GSM FT not pass";
				}
			} else {
				s1 += ":GSM BT not pass";
				s2 += ":GSM FT not pass";
			}

			if (ch2 == '3' || ch2 == '4' || ch2 == '5' || ch2 == '6'
					|| ch2 == '7') {
				if (ch2 == '5' || ch2 == '6' || ch2 == '7') {
					s3 += ":WCDMA BT pass";
					s4 += ":WCDMA FT pass";
				} else {
					s3 += ":WCDMA BT pass";
					s4 += ":WCDMA FT not pass";
				}
			} else {
				s4 += ":WCDMA FT not pass";
				s3 += ":WCDMA BT not pass";
			}
			if ((CITBroadcastReceiver.INSERT_SN) && (sn.length() >= 2)) {
				sn = sn.substring(0, sn.length() - 2) + "  "
						+ sn.substring(sn.length() - 2);
			}

			s += sn;
			tv_sn.setText(s);
			tv_gsm_bt.setText(s1);
			tv_gsm_ft.setText(s2);
			tv_wcdma_bt.setText(s3);
			tv_wcdma_ft.setText(s4);
			// return;
		} else {
			if ((CITBroadcastReceiver.INSERT_SN) && (sn.length() >= 2)) {
				sn = sn.substring(0, sn.length() - 2) + "  "
						+ sn.substring(sn.length() - 2);
			}
			s += sn;
			tv_sn.setText(s);
			tv_gsm_bt.setText(R.string.sn_code);
		}
		String s_citFlag = "0000";

		citFlag = getCitBinder();
		Log.i(TAG, "citFlag" + citFlag);
		s_citFlag = Integer.toBinaryString(citFlag);
		Log.i(TAG, "s_citFlag" + s_citFlag);

		s = getString(R.string.CIT_FLAG);
	/*	String[] flagLength = this.getResources().getStringArray(
				R.array.flagLength);
		Log.v(TAG, flagLength[0]);

		int i = Integer.parseInt(flagLength[0]);//5
	//	Integer.toBinaryString(i);
*/
		int i =CITBroadcastReceiver.CIT_Flag_Length;
		
		int m = i - s_citFlag.length();
		Log.i(TAG, m + "");
		for (int j = 0; j < m; j++) {
			s_citFlag = "0" + s_citFlag;
		}
		s += s_citFlag;
		tv_CitFlag.setText(s);

		String invalid = getString(R.string.IMEIinfo);
		s = getString(R.string.IMEI1);
        //hucheng modify start @160801
		//imeiStr = ((TelephonyManagerEx) TelephonyManagerEx.getDefault()).getDeviceId(PhoneConstants.SIM_ID_1);         
        imeiStr =  teleMgr.getDeviceId(PhoneConstants.SIM_ID_1);
		if (imeiStr != null)
			s += imeiStr;
		else
			s += invalid;
		tv_Imei1.setText(s);

		if(FeatureOption.MTK_GEMINI_SUPPORT)
		{
		s = getString(R.string.IMEI2);
		//imeiStr2 = ((TelephonyManagerEx) TelephonyManagerEx.getDefault()).getDeviceId(PhoneConstants.SIM_ID_2);
         imeiStr2 = teleMgr.getDeviceId(PhoneConstants.SIM_ID_2);
        //hucheng modify end @160801
		if (imeiStr != null)
			s += imeiStr2;
		else
			s += invalid;
		tv_Imei2.setText(s);
		}

		return;
		// end
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

	// get the information use
	private static String getSystemproString(String property) {
		return SystemProperties.get(property, "unknown");
	}

	/*
	 * private String getSnVersion() { String strSN = "809"; try { strSN +=
	 * mNv.getSNNumber(); } catch (IOException e) { } return strSN; }
	 */
	private String getSoftwareVersion() {
		String softwareTitle = getSystemproString("ro.build.display.id");
		String softwareTime = getSystemproString("ro.build.version.incremental");
		/*
		 * // if < 18 ? prehaps hava no time if (softwareTime.length() > 18) {
		 * int nPos = softwareTime.indexOf("."); nPos =
		 * softwareTime.indexOf(".", nPos); softwareTime =
		 * softwareTime.substring(nPos + 1); // modify 2011-1-5 nPos =
		 * softwareTime.indexOf("."); nPos = softwareTime.indexOf(".", nPos);
		 * 
		 * softwareTime = softwareTime.substring(nPos + 1);
		 * 
		 * StringBuffer time = new StringBuffer(softwareTime); time.insert(4,
		 * '-'); time.insert(7, '-'); time.replace(10, 11, " "); time.insert(13,
		 * ':'); time.insert(16, ':');
		 * 
		 * softwareTitle += "\n" + time.toString(); }
		 */
		softwareTitle += "\n" + softwareTime;
		return softwareTitle;

	}

	/**
	 * get nvRam SNCit flag
	 */
	private int getCitBinder() {

		ct.CitBinderPrepare();
		citFlag = ct.getCitFlag();

		return citFlag;
	}

}
