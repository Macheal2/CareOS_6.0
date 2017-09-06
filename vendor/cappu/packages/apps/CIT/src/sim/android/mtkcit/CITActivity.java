package sim.android.mtkcit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

//import com.mediatek.featureoption.FeatureOption;
//import com.mediatek.common.featureoption.FeatureOption;

import sim.android.mtkcit.testitem.BatteryInfo;
import sim.android.mtkcit.testitem.Version;
import sim.android.mtkcit.testitem.WifiAdmin;
import sim.android.mtkcit.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import sim.android.mtkcit.cittools.CITTools;
import android.bluetooth.BluetoothAdapter;

import sim.android.mtkcit.util.FeatureOption;

public class CITActivity extends ListActivity {
	private ArrayList<String> Items = new ArrayList<String>();
	private BluetoothAdapter mAdapter;
	private WifiAdmin mWifiAdmin;
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	public static final int VERSION_TEST = 2;

	public static final int BATTERY_INFO = 3;
	public static final int WBG_AUTO_TEST = 4;
	// add_s wangkang 2012.5.3
	public static final String PCBAGREEN = "PCBAGreenLight";
	public static final String MACHINEGREEN = "MachineGreenLight";
	// add_e wangkang 2012.5.3
	public static String TEST_TYPE = "test_type";
	public static int Testtype = -1;
	private String as[];
	private boolean bUsbDebugModified=false;
	String musicPath;
	String musicFile;
	private CITTools ct;
	// add_s wangkang 2012.5.3
	public static boolean freeTest;
	private String ps[];
	public static int flagLength = 4;

	// add_e wangkang 2012.5.3
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		Log.v("bruce_nan", "onListItemClick--------position=" + position);
		switch (position) {
		// modified by bruce for remove PCBA test begin
		/*
		case CITBroadcastReceiver.PCBA_TEST:
			Testtype = CITBroadcastReceiver.PCBA_TEST;
			// setPopelemState(); //add wangkang 2012.5.3
			intent.setClass(this, PCBAMain.class);
			startActivity(intent);
			break;
	    */
		//case CITBroadcastReceiver.MACHINE_TEST:
		case 0:
			Testtype = CITBroadcastReceiver.MACHINE_TEST;
			// setPopelemState(); //add wangkang 2012.5.3
			intent.setClass(this, MachineMain.class);
			startActivity(intent);
			break;
		//case CITBroadcastReceiver.VERSION_TEST:
		case 1:
			Testtype = CITBroadcastReceiver.VERSION_TEST;
			intent.setClass(this, Version.class);
			startActivity(intent);
			break;
		//case CITBroadcastReceiver.BATTERY_INFO:
		case 2:
			Testtype = CITBroadcastReceiver.BATTERY_INFO;
			intent.setClass(this, BatteryInfo.class);
			startActivity(intent);
			break;
		// modified by bruce for remove PCBA test end
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.main);
		getListItems();
		flagLength = CITBroadcastReceiver.CIT_Flag_Length;
		if (Settings.Secure.getInt(getContentResolver(),Settings.Secure.ADB_ENABLED, 0) == 0)
		{
			Settings.Secure.putInt(getContentResolver(),Settings.Secure.ADB_ENABLED, 1);
			bUsbDebugModified=true;
		}
		InputStream is = getResources().openRawResource(R.raw.test);
		InputStream is2 = getResources().openRawResource(R.raw.test_2);

		ct = CITTools.getInstance(this);

		ct.getSDPaths();
		if (CITTools.emmc) {
			if ((ct.checkSDCardMount(ct.SD2Path))) {
				Log.v("feng", ct.SD2Path);

				ct.cpFileToSD("test.mp3", is, ct.SD2Path + "/CIT");
				ct.cpFileToSD("test_2.mp3", is2, ct.SD2Path + "/CIT");
			} 
			//deleted by jiangyan for tip bug start
			//else
			//	Toast.makeText(this, R.string.speak_test_hasnot_sdcard2, 1).show();
			 //deleted by jiangyan for tip bug end
		} else {
			if ((ct.checkSDCardMount(ct.SD1Path))) {
				Log.v("feng", ct.SD1Path);

				ct.cpFileToSD("test.mp3", is, ct.SD1Path + "/CIT");
				ct.cpFileToSD("test_2.mp3", is2, ct.SD2Path + "/CIT");
			} else
				Toast.makeText(this, R.string.speak_test_hasnot_sdcard, 1)
						.show();
		}
		
//		// open BT for test.
//		mAdapter = BluetoothAdapter.getDefaultAdapter();
//		if (!mAdapter.isEnabled())
//			mAdapter.enable();
		
//		// open wifi for test.
//		mWifiAdmin = new WifiAdmin(this);
//		mWifiAdmin.closeWifi();
//		mWifiAdmin.openWifi();
	}

	private void cpMusicToSD() {
		musicPath = Environment.getExternalStorageDirectory().toString()
				+ "/CIT";
		File f = new File(musicPath);
		if (!f.exists()) {
			f.mkdirs();
		}
		musicFile = musicPath + "/test.mp3";
		f = new File(musicFile);
		// if (!f.exists()) {
		// InputStream is = getResources().openRawResource(R.raw.labelname);
		InputStream is = getResources().openRawResource(R.raw.test);
		Log.v("CIT", "get the test.mp3");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(musicFile);
			byte bt[] = new byte[1024];
			int c;
			while ((c = is.read(bt)) > 0) {
				fos.write(bt, 0, c);
			}
			fos.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			// }

		}
	}

	@Override
	protected void onDestroy() {
		if(bUsbDebugModified)
		{
			Settings.Secure.putInt(getContentResolver(),Settings.Secure.ADB_ENABLED, 0);
		}
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		getWindow().setType(WindowManager.LayoutParams.TYPE_BASE_APPLICATION);
		super.onStart();
	}

	private int getFlagLength() {
		/*
		 * String[] length = getResources().getStringArray(R.array.flagLength);
		 * Log.v("CIT", "Integer.parseInt(length[0] =" +
		 * Integer.parseInt(length[0]));
		 * 
		 * return Integer.parseInt(length[0]);
		 */
		return CITBroadcastReceiver.CIT_Flag_Length;
	}

	private void getListItems() {
		as = getResources().getStringArray(R.array.MainTest);
		int j = 0;
		do {
			if (j < as.length) {
				Items.add(as[j]);
				j++;
			} else {
				setListAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, Items));
				getListView().setTextFilterEnabled(true);
				return;
			}
		} while (true);
	}
	// private void setPopelemState(){
	// ps = getResources().getStringArray(R.array.testPopedom);
	// int i = 0;
	// do {
	// if((Testtype == PCBA_TEST) && ps[i].equals(PCBAGREEN)){
	// freeTest = true;
	// return;
	// }else if((Testtype == MACHINE_TEST) && ps[i].equals(MACHINEGREEN)){
	// freeTest = true;
	// return;
	// }else{
	// freeTest = false;
	// i++;
	// }
	// }while(i < ps.length);
	// }
}
