package sim.android.mtkcit.cittools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import android.widget.Button;

import sim.android.mtkcit.AutoTestActivity;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;
import sim.android.mtkcit.testitem.GPSTest;
import android.content.Context;

//import com.mediatek.featureoption.FeatureOption;
//import com.mediatek.common.featureoption.FeatureOption;
//import com.mediatek.featureoption.SimcomFeatureOption;
//import com.mediatek.featureoption.SimcomIDOption;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import android.net.wifi.WifiManager;

import android.os.IBinder;
import android.os.ServiceManager;
import sim.android.mtkcit.util.FeatureOption;

public class CITTools {
	private StorageManager mStorageManager = null;
	public String CITPath;
	public static int testID;
	private String filePath;
	private final String TAG = "CITTools";
	protected File[] mSystemSDCardMountPointPathList = null;
	private static final int REQUEST_WIFI_BT_ASK = 2;
	public static String SD1Path = null;
	public static String SD2Path = null;

	public static File SD1File;
	public static File SD2File;
	public static boolean sdsFlag;
	public int fmStation = 985;
	public boolean minualSD = false;
	public List<File> sdList;
	public static String[] storagePathList;
	public static boolean minuSD = false;
	public static boolean cd1flas = false;
	public static boolean cd2flas = false;
	private final String WIFI_CITTEST = "wifi_bt_cittest";
	BluetoothAdapter mAdapter;
	WifiManager mWifiManager;
	public static CITTools mctools = null;
	private Activity mActivity;
//	private IBinder binder;
//	private CitBinder citBinder;
	public CitBinder citBinder;
	
	public IBinder binder = null;
	public NvRAMAgent agent = null;
	/*
	 * public static final int MACHINE_SENSOR_AUTO = 0; public static final int
	 * MACHINE_AUTO = 1; public static final int PCBA_SENSOR_AUTO = 2; public
	 * static final int PCBA_AUTO = 3; public static final int WBG_AUTO = 4;
	 */

	public static final int PCBA_AUTO = 0;
	public static final int PCBA_SENSOR_AUTO = 1;
	public static final int MACHINE_AUTO = 2;
	public static final int MACHINE_SENSOR_AUTO = 3;
	public static final int WBG_AUTO = 4;


	public static boolean emmc = FeatureOption.MTK_EMMC_SUPPORT;

    // added by bruce for read/write NvRam begin
    private static final String NVRAM_PRODUCT_INFO_LID= "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
    private static final String NVRAM_HWMON_ACC_LID= "/data/nvram/APCFG/APRDCL/HWMON_ACC";
    private static final String NVRAM_HWMON_PS_LID= "/data/nvram/APCFG/APRDCL/HWMON_PS";
    // added by bruce for read/write NvRam end

	private CITTools(Activity activity) {
		mActivity = activity;
		
		binder = ServiceManager.getService("NvRAMAgent");
		agent = NvRAMAgent.Stub.asInterface(binder);
	}

	public static synchronized CITTools getInstance(Activity activity) {
		if (mctools == null) {
			mctools = new CITTools(activity);
		}
		return mctools;
	}

	public String getAvailMemory() {
		ActivityManager am = (ActivityManager) mActivity
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		Log.v(TAG, "mi.availMem=" + mi.availMem);
		return Formatter.formatFileSize(mActivity, mi.availMem);
	}

	public String getTotalMemory() {
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(TAG,str2+","+num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();

		} catch (IOException e) {
		}
		return Formatter.formatFileSize(mActivity, initial_memory);
	}

	public void cpFileToSD(String fileName, InputStream is, String sdPath) {
		CITPath = sdPath;
		Log.v(TAG, "CITPath=" + CITPath);
		File f = new File(CITPath);
		if (!f.exists()) {
			f.mkdirs();
		}
		filePath = CITPath + "/" + fileName;
		f = new File(filePath);
		Log.v(TAG, "get the test.mp3");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filePath);
			byte bt[] = new byte[1024];
			int c;
			while ((c = is.read(bt)) > 0) {
				fos.write(bt, 0, c);
			}
			fos.close();
			is.close();
		} catch (Exception e) {
			Log.v(TAG, "file not found");
			e.printStackTrace();
			// }

		}
	}

	public boolean checkFile(String fileName, String path) {
		String filePath = path + "/" + fileName;
		Log.v(TAG, "filePath=" + filePath);
		File f = new File(filePath);
		if (!f.exists()) {
			return false;
		} else
			return true;
	}

	/**
	 * help to create file
	 * 
	 * @param fileName
	 * @param filePath
	 * @return
	 */
	public boolean creatFile(String fileName, String filePath) {

		LOGV(true, TAG, "fileName=" + fileName + "   filePath=" + filePath);

		File f = new File(filePath);
		if (!f.exists()) {
			f.mkdirs();
		}
		f = new File(filePath + "/" + fileName);
		LOGV(true, TAG, filePath + "/" + fileName);
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/***
	 * GPSTest
	 */
	public void GPSTest(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, GPSTest.class);
		context.startActivity(intent);
	}

	/**
	 * wifi test
	 */
	public void wifitest() {
		mWifiManager = (WifiManager) (mActivity
				.getSystemService(Context.WIFI_SERVICE));
		WIFIstate = mWifiManager.getWifiState();
		Log.v(TAG, "wifitest---WIFIstate=" + WIFIstate);

		if (WIFIstate == 3) {
			mWifiManager.setWifiEnabled(false);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, "thread sleep   fail!");
			}
		}

		mWifiManager.setWifiEnabled(true);

		ComponentName component = new ComponentName("com.android.settings",
				"com.android.settings.wifi.WifiSettings");
		Intent intent = new Intent();
		intent.setComponent(component);
		Bundle mBundle = new Bundle();
		mBundle.putString(WIFI_CITTEST, WIFI_CITTEST);
		intent.putExtras(mBundle);
		mActivity.startActivityForResult(intent, REQUEST_WIFI_BT_ASK);
	}

	/**
	 * help to test bt
	 */
	public void bluetoothtest() {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		BTstate = mAdapter.getState();
		Log.v(TAG, "bluetoothtest--BTstate=" + BTstate);

		if (BTstate == 12) {
			try {
				mAdapter.disable();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, "thread sleep   fail!");
			}
		}
		if (!mAdapter.enable()) {
			Log.e(TAG, "open bt fail!");
		}
		ComponentName component = new ComponentName("com.android.settings",
				"com.android.settings.bluetooth.BluetoothSettings");
		Intent intent = new Intent();
		intent.setComponent(component);
		Bundle mBundle = new Bundle();
		mBundle.putString(WIFI_CITTEST, WIFI_CITTEST);
		intent.putExtras(mBundle);
		mActivity.startActivityForResult(intent, REQUEST_WIFI_BT_ASK);
	}

	int BTstate;

	/**
	 * after bt test
	 */
	public void afterBluetoothtest() {
		if (mAdapter != null) {
			BTstate = mAdapter.getState();
		} else

			mAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.v(TAG, "mAdapter=" + mAdapter);

		Log.v(TAG, "afterBluetoothtest---BTstate=" + BTstate);
		if (BTstate == 11 || BTstate == 13) {
			return;

		} else if (BTstate == 12 && !mAdapter.disable()) {
			Log.e(TAG, "close bt fail !");
		}
		mAdapter = null;

	}

	int WIFIstate;

	/**
	 * after wifi test
	 */
	public void afterWifitest() {

		if (mWifiManager != null) {
			WIFIstate = mWifiManager.getWifiState();
			Log.v(TAG, "afterWifitest---WIFIstate=" + WIFIstate);
		} else
			mWifiManager = (WifiManager) (mActivity
					.getSystemService(Context.WIFI_SERVICE));
		if (WIFIstate == 2) {
			return;
		} else if (WIFIstate == 3 && !mWifiManager.setWifiEnabled(false)) {
			Log.e(TAG, "close wifi fail !");
		}
		mWifiManager = null;

	}

	/**
	 * used to get sds path after this method you can get the sd path through
	 * CITTools.SD1Path CITTools.SD2Path
	 */
	public int getSDPaths() {
		mStorageManager = (StorageManager) (mActivity
				.getSystemService(Context.STORAGE_SERVICE));
		storagePathList = mStorageManager.getVolumePaths();
		if (storagePathList != null) {
			mSystemSDCardMountPointPathList = new File[storagePathList.length];
			for (int i = 0; i < storagePathList.length; i++) {
				mSystemSDCardMountPointPathList[i] = new File(
						storagePathList[i]);
			}
			if (storagePathList.length >= 2) {
				if(FeatureOption.MTK_2SDCARD_SWAP) {
					SD1Path = storagePathList[1];		// emmc card
					SD2Path = storagePathList[0];		// SD card
				} else {
					SD1Path = storagePathList[0];		// emmc card
					SD2Path = storagePathList[1];		// SD card
				}
		        
				CITPath = SD2Path + "/CIT";
				LOGV(true, TAG, "SDCard path: " + SD1Path);
				LOGV(true, TAG, "SDCard path: " + SD2Path);
			} else if (storagePathList.length == 1) {
				SD1Path = storagePathList[0];
				CITPath = SD1Path + "/CIT";
				LOGV(true, TAG, "SDCard path: " + SD1Path);
			}
			return storagePathList.length;
		}
		return 0;
	}

	/**
	 * private static final boolean IS_CU = SystemProperties.get(
	 * "ro.operator.optr").equals("OP02");
	 */

	/**
	 * This method checks whether SDcard is mounted or not
	 * 
	 * @param mountPoint
	 *            the mount point that should be checked
	 * @return true if SDcard is mounted, false otherwise
	 */
	public boolean checkSDCardMount(String mountPoint) {
		getSDPaths();
		if (mountPoint == null) {
			return false;
		}
		String state = null;
		state = mStorageManager.getVolumeState(mountPoint);
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	/***
	 * sub camera test
	 */
	public void subCameraTest(Activity mActivity) {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			Intent intent = new Intent();
//			intent.setClassName("com.android.camera", "com.android.camera.Camera");		// 相机测试,出现让操作员选择"相机"提示框\E3??
			intent.putExtra("camerasensortype", 1);
			intent.putExtra("autofocus", true);
			intent.putExtra("fullScreen", false);
			intent.putExtra("showActionIcons", false);
			// for the camera that not default the bg camera
			intent.putExtra("cit_cameraid", 1);
			mActivity.startActivity(intent);
			// startActivityForResult(intent, 0);
			//
			// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// intent.putExtra("camerasensortype", 1); // 调用前置摄像\E5\A4?			mActivity.startActivity(intent);

		} catch (ActivityNotFoundException exception) {
			Log.d(TAG, "the camera activity is not exist");
			String s = mActivity.getString(R.string.device_not_exist);
			AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(
					mActivity)).setTitle(R.string.test_item_camera).setMessage(
					s);
			builder.setPositiveButton(R.string.alert_dialog_ok, null).create()
					.show();
		}
	}
	// private void masterclear() {
	// Intent intent = new Intent();
	// intent.setClassName("com.android.settings",
	// "com.android.settings.MasterClear");
	// IsAutoCITTesting = true;
	// startActivity(intent);
	// }
	//
	//
	public void cameraTest(Activity mActivity) {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			Intent intent = new Intent();
//			intent.setClassName("com.android.camera", "com.android.camera.Camera");		// 相机测试,出现让操作员选择"相机"提示框\E3??
			intent.putExtra("camerasensortype", 1);
			intent.putExtra("autofocus", true);
			intent.putExtra("fullScreen", false);
			intent.putExtra("showActionIcons", false);
			// for the camera that not default the bg camera
			intent.putExtra("cit_cameraid", 0);
			mActivity.startActivity(intent);
		} catch (ActivityNotFoundException exception) {
			Log.d(TAG, "the camera activity is not exist");
			String s = mActivity.getString(R.string.device_not_exist);
			AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(
					mActivity)).setTitle(R.string.test_item_camera).setMessage(
					s);
			builder.setPositiveButton(R.string.alert_dialog_ok, null).create()
					.show();
		}
	}

	public void FMTest() {
		ComponentName component = new ComponentName("com.android.fmradio", "com.android.fmradio.FmMainActivity");
		Intent intent = new Intent();
		intent.setComponent(component);
		Bundle mBundle = new Bundle();
		mBundle.putString(WIFI_CITTEST, WIFI_CITTEST);
		intent.putExtras(mBundle);
		mActivity.startActivityForResult(intent, REQUEST_WIFI_BT_ASK);
	}
	
	public void fpTest(){
		//Modify by liyiming
		ComponentName component = new ComponentName("com.goodix.testtool", "com.goodix.testtool.StartTestActivity");
		Intent intent = new Intent();
		intent.setComponent(component);
		mActivity.startActivityForResult(intent, 1);
	}

	/*
	 * before 4.0 use this method to master
	 */
	private void masterclear(Activity mActivity) {
		Intent intent = new Intent();
		intent.setClassName("com.android.settings",
				"com.android.settings.MasterClear");
		mActivity .startActivity(intent);
	}
	/**
	 * getSD card mempry info
	 * 
	 * @param path
	 *            sdPath
	 * @param context
	 * @return info
	 */
	public String getSDinfo(String path, Context context) {
		StatFs statfs = new StatFs(path);
		long blocSize = statfs.getBlockSize();
		long totalBlocks = statfs.getBlockCount();
		long availaBlock = statfs.getAvailableBlocks();
		long totalSize = totalBlocks * blocSize;
		long availale = totalSize - availaBlock * blocSize;
		return Formatter.formatFileSize(context, availale) + "/"
				+ Formatter.formatFileSize(context, totalSize);
	}

	/**
	 * read content from the text file
	 * 
	 * @param fileName
	 * @return content
	 */
	public String readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		String s = "";
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 1;
			while ((tempString = reader.readLine()) != null) {
				line++;
				s = s + tempString;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			LOGE(TAG, "read error");
			return "error";
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return s;
	}

	/**
	 * write content to the file
	 * 
	 * @param fileName
	 * @param content
	 * @return
	 */
	public boolean writeMethod(String fileName, String content) {
		CITTools.LOGV(true, TAG, fileName + "-----------" + content);
		FileWriter writer = null;
		try {
			writer = new FileWriter(fileName);
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
				}
			}
		}
		return true;
	}

	/**
	 * change the state of the test botton
	 */
	public void initButton(Button btn_success) {

		if ((AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_AUTO)
				|| (AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_MACHINE_SENSOR_AUTO)) {
			Log.v(TAG, "CITBroadcastReceiver.MachinePass = "
					+ CITBroadcastReceiver.MachinePass);

			 btn_success.setEnabled(CITBroadcastReceiver.MachinePass);
//			btn_success.setEnabled(false);

		} else if ((AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_AUTO)
				|| (AutoTestActivity.auto_test_type == CITBroadcastReceiver.TEST_TYPE_PCBA_SENSOR_AUTO)) {
			 btn_success.setEnabled(CITBroadcastReceiver.PCBAPass);
//			btn_success.setEnabled(false);

		}
	}
	/**
	 * call this method before control the CitBinders
	 */
	public void CitBinderPrepare() {
//		binder = ServiceManager.getService("CitBinder");
//		citBinder = CitBinder.Stub.asInterface(binder);
	}

	/**
	 * set nvRam SNCit flag
	 */

	/**
	 * MACHINE_AUTO_TEST
	 */

	/**
	 * MACHINE_SENSOR_AUTO_TEST
	 */

	/**
	 * MACHINE_SENSOR_AUTO_TEST
	 */

	public void setCitAutoTestFlag(int location) {
		 int result = 0;
		 int i = 0;
		 
		 try{
			 //byte[] buff = agent.readFile(35);		// AP_CFG_REEB_PRODUCT_INFO_LID
             byte[] buff = agent.readFileByName(NVRAM_PRODUCT_INFO_LID);

			 Log.e(TAG, "setCitAutoTestFlag,buff len="+buff.length+",location="+location);
			 Log.e(TAG, "buff ="+buff);
		   
			 buff[location] = 1;
		      
			 //result = agent.writeFile(35, buff);
             result = agent.writeFileByName(NVRAM_PRODUCT_INFO_LID, buff);
		   
			 if(0 == result){
				 Toast.makeText(mActivity, R.string.set_sn_error, 1).show();
			 }
		 } 
		 catch (SecurityException se)
		 {
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "setCitAutoTestFlag,agent writeFile fail");
		 }
		 catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
	}

	/**
	 * get nvRam SNCit flag
	 */
	public int getFlag(int data) throws RemoteException {
		int result = 0;
		int i = 0;
		
		//byte[] buff = agent.readFile(35);		// AP_CFG_REEB_PRODUCT_INFO_LID
        if (agent != null){
        byte[] buff = agent.readFileByName(NVRAM_PRODUCT_INFO_LID);

		Log.e(TAG, "getFlag,buff ="+buff);
		
		for(i = 0;i <data ;i++){ 
			if(buff[i]==1){
				int j =0; 
				int num = 1;
				
				for( j=0; j<data-i-1; j++){
					num = num *2 ;
				}
				
				result = result +num;
			}
		}
        }
			  
		return result;
	}
	
	public int getCitFlag() {
		int citflag = -1;
		int l = 4;
		l = CITActivity.flagLength;
		Log.i(TAG, "getFlag  l = "+ l);

		try {
//			citflag = citBinder.getFlag(l);
			citflag = getFlag(l);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return citflag;
	}

	public String getStringFromRes(Context context, int resId) {

		String ItemStr[] = context.getResources().getStringArray(
				R.array.CommonTestStrings);
		return ItemStr[resId];
	}
	/**
	 * stop the screen back-light auto
	 * 
	 * @param con
	 */
	public void stopAutoBrightness(Context con) {
		Settings.System.putInt(con.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
	}

	/**
	 * start the screen back-light auto
	 * 
	 * @param con
	 */
	public void startAutoBrightness(Context con) {
		Settings.System.putInt(con.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}
   
	
	private boolean debugFlag = true ;
	/***
	 * change the back-light
	 * @param brightness
	 */
	public void setBrightness(int brightness) {
		synchronized (this) {
			try {

				// String BRIGHTNESS_FILE =
				// "/sys/class/leds/lcd-backlight/brightness";

				String commond = "echo " + brightness
						+ " > /sys/class/leds/lcd-backlight/brightness";
				String[] cmd = { "/system/bin/sh", "-c", commond };
				int ret = CITShellExe.execCommand(cmd);
				LOGV(debugFlag, TAG, cmd[0] + cmd[1] + cmd[2]);
				if (0 == ret) {
					LOGV(debugFlag, TAG, "execCommand success "
							+ CITShellExe.getOutput());

				} else {
					LOGV(debugFlag, TAG, "execCommand error ");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * set GsCali_x
	 */
//	public boolean setGsCali_x(int x) {
//		boolean citflag = false;
//		try {
////			citflag = citBinder.setGsCali_x(x);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return citflag;
//	}
//
//	/**
//	 * set GsCali_y
//	 */
//	public boolean setGsCali_y(int y) {
//		boolean citflag = false;
//		try {
////			citflag = citBinder.setGsCali_y(y);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return citflag;
//	}
//
//	/**
//	 * set GsCali_z
//	 */
//	public boolean setGsCali_z(int z) {
//		boolean citflag = false;
//		try {
////			citflag = citBinder.setGsCali_z(z);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return citflag;
//	}

	/**
	 * after set the cals of x,y,z use this to cals p-sensor
	 *
	 * #define C_HWMON_ACC_AXES    3
	* typedef struct
	* {
	*     int offset[C_HWMON_ACC_AXES];
	* } NVRAM_HWMON_ACC_STRUCT;
	*/
	public int citBinder_GetGsCali(){
		return 1;
	}
	
	public int citBinder_SetGsCali(int x, int y, int z){
		return 1;
	}
	
	public int citBinder_SaveGsCali(int x, int y, int z){
		 int result = 0;
		 int i = 0;
		 
		 try{
			 //byte[] buff = agent.readFile(13);		// AP_CFG_RDCL_HWMON_ACC_LID
             byte[] buff = agent.readFileByName(NVRAM_HWMON_ACC_LID);
		   
			 buff[0] = (byte)(x*65536/9807);
			 buff[1] = (byte)(y*65536/9807);
			 buff[2] = (byte)(z*65536/9807);
		      
			 //result = agent.writeFile(13, buff);
             result = agent.writeFileByName(NVRAM_HWMON_ACC_LID, buff);
		 } 
		 catch (SecurityException se)
		 {
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "citBinder_SaveGsCali,agent writeFile fail");
		 }
		 catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }

		return 1;
	}
	
	public int citBinder_GsCali(int x, int y, int z) throws RemoteException {
		int ret = 0;
		
		ret = citBinder_SetGsCali(x, y, z);
		if(ret<0)
		{	
			return -3;
		}
		
		ret = citBinder_GetGsCali();
		if(ret<0)
		{	
			return -4;
		}
		
		ret = citBinder_SaveGsCali(x, y, z);
		if(ret<0)
		{	
			return -2;
		}
		
		Log.i(TAG, "citBinder_GsCali  result = "+ ret);
		return 1;
	}
	
	public int GsCali(int x, int y, int z) {
		int citflag = -1;
		try {
//			if (citBinder.setGsCali_x(x) && citBinder.setGsCali_y(y)
//					&& citBinder.setGsCali_z(z))
//
//				citflag = citBinder.GsCali();
			citflag = citBinder_GsCali(x, y, z);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return citflag;
	}

	public int ClrGsCali() throws RemoteException {
		
		return 1;
	}
	
	/**
	 *  Set Proximity sensor data
	 */
	/*
	 * typedef struct {
			char imei1Status;
			char imei2Status;
			char barcode1Status;
			char barcode2Status;
			char wifiAddrStatus;
			char btAddrStatus;
			char r1Status;
			char r2Status;
		} check_status;
		
		#define BARCODE_LEN     32
		#define IMEI_LEN        32
		#define PSCALI_LEN        32  //longxuewei
		typedef struct {
			char barcode[BARCODE_LEN];    //PCBA for acer
			char barcode2[BARCODE_LEN];   //complete machine
			char imei[IMEI_LEN];       //SIM1
			char imei2[IMEI_LEN];		 //SIM2
			char uuid[IMEI_LEN];       //uuid for aliyun of acer
			char uuid2[IMEI_LEN];		 //uuid for aliyun of acer
			check_status check;
		#if 1  //longxuewei
			char pscali_close[PSCALI_LEN];
			char pscali_far[PSCALI_LEN];
			char pscali_valid[PSCALI_LEN];
		#endif
		} product_info_simcom;
	 */

  //将int型转化成byte数组
   private static  byte[] getBytes(int data){
            byte[] bytes = new byte[4];
            bytes[0] = (byte)(data&0xff);
            bytes[1] = (byte)((data&0xff00)>>8);
            bytes[2] = (byte)((data&0xff0000)>>16);        
            bytes[3] = (byte)((data&0xff000000)>>24);
            return bytes;
   }  
   //将byte数组转换成整型
    public static int byte2int(byte[] res) {          
               int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) | ((res[2] << 24) >>> 8) | (res[3] << 24);   
              return targets;   
    }
   
	public void setPsCali_close(int data) throws RemoteException {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(35);		// AP_CFG_REEB_PRODUCT_INFO_LID
		   

			//buff[625] = (byte)(data/1000);		// 64+40 32+32+32+32+32+32+8 + 1
			//buff[626] = (byte)((data%1000)/100);	
			//buff[627] = (byte)((data%100)/10);
			//buff[628] = (byte)(data%10);	// 32+32+32+32+32+32+8 + 2
		      
			//result = agent.writeFile(35, buff);

			//byte[] buff = agent.readFile(19);		// AP_CFG_RDCL_HWMON_PS_LID	
            byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
		
		    //Log.i(TAG,"vaild=" + buff[0]); 
			//buff[7] = (byte)(data/1000);		
			//buff[6] = (byte)((data%1000)/100);	
			//buff[5] = (byte)((data%100)/10);
			//buff[4] = (byte)(data%10);
			//buff = getBytes(data);
			buff[4] = (byte)(data&0xff);
            buff[5] = (byte)((data&0xff00)>>8);
            buff[6] = (byte)((data&0xff0000)>>16);        
            buff[7] = (byte)((data&0xff000000)>>24);
			//result = agent.writeFile(19, buff);
            result = agent.writeFileByName(NVRAM_HWMON_PS_LID, buff);

			//Log.i(TAG, "setPsCali_close  buff[4] = "+ buff[4]);			
			//Log.i(TAG, "setPsCali_close  buff[5] = "+ buff[5]);
			//Log.i(TAG, "setPsCali_close  buff[6] = "+ buff[6]);
			//Log.i(TAG, "setPsCali_close  buff[7] = "+ buff[7]);
			Log.i(TAG, "setPsCali_close  result = "+ result);
			Log.i(TAG, "getFlag22  buff.length = "+ buff.length);

			byte[] buff1 = agent.readFile(73);		// AP_CFG_RDCL_HWMON_BACKUP_LID
			buff1[28] = (byte)(data&0xff);
            buff1[29] = (byte)((data&0xff00)>>8);
            buff1[30] = (byte)((data&0xff0000)>>16);        
            buff1[31] = (byte)((data&0xff000000)>>24);
			result = agent.writeFile(73, buff1);
			
		} 
		catch (SecurityException se)
		 {
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "setPsCali_close,agent writeFile fail");
		 }
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void setPsCali_far(int data) throws RemoteException {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(19);		// AP_CFG_REEB_PRODUCT_INFO_LID
            byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
		    //Log.i(TAG,"close=" + buff[4]+","+buff[5]+","+buff[6]+","+buff[7]); 
			//buff[11] = (byte)(data/1000);		
			//buff[10] = (byte)((data%1000)/100);	
			//buff[9] = (byte)((data%100)/10);
			//buff[8] = (byte)(data%10);
			//buff = getBytes(data);
		    buff[8] = (byte)(data&0xff);
            buff[9] = (byte)((data&0xff00)>>8);
            buff[10] = (byte)((data&0xff0000)>>16);        
            buff[11] = (byte)((data&0xff000000)>>24);  
			//result = agent.writeFile(19, buff);
            result = agent.writeFileByName(NVRAM_HWMON_PS_LID, buff);

			//Log.i(TAG, "setPsCali_far  buff[8] = "+ buff[8]);			
			//Log.i(TAG, "setPsCali_far  buff[9] = "+ buff[9]);
			//Log.i(TAG, "setPsCali_far  buff[10] = "+ buff[10]);
			//Log.i(TAG, "setPsCali_far  buff[11] = "+ buff[11]);
			Log.i(TAG, "setPsCali_far  result = "+ result);
			Log.i(TAG, "getFlag22  buff.length = "+ buff.length);

			byte[] buff1 = agent.readFile(73);		// AP_CFG_RDCL_HWMON_BACKUP_LID
			buff1[32] = (byte)(data&0xff);
            buff1[33] = (byte)((data&0xff00)>>8);
            buff1[34] = (byte)((data&0xff0000)>>16);        
            buff1[35] = (byte)((data&0xff000000)>>24);
			result = agent.writeFile(73, buff1);
			
		} 
		catch (SecurityException se)
		 {
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "setPsCali_far,agent writeFile fail");
		 }
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setPsCali_valid(int data) throws RemoteException {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(19);		// AP_CFG_REEB_PRODUCT_INFO_LID
            byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
			//buff[3] = (byte)(data/1000);		
			//buff[2] = (byte)((data%1000)/100);	
			//buff[1] = (byte)((data%100)/10);
			//buff[0] = (byte)(data%10);
			//buff = getBytes(data);
			buff[0] = (byte)(data&0xff);
            buff[1] = (byte)((data&0xff00)>>8);
            buff[2] = (byte)((data&0xff0000)>>16);        
            buff[3] = (byte)((data&0xff000000)>>24);
			//result = agent.writeFile(19, buff);
            result = agent.writeFileByName(NVRAM_HWMON_PS_LID, buff);
			Log.i(TAG, "setPsCali_valid  buff[0] = "+ buff[0]);			
			Log.i(TAG, "setPsCali_valid  result = "+ result);
			Log.i(TAG, "getFlag22  buff.length = "+ buff.length);
			
			byte[] buff1 = agent.readFile(73);		// AP_CFG_RDCL_HWMON_BACKUP_LID
			buff1[24] = (byte)(data&0xff);
            buff1[25] = (byte)((data&0xff00)>>8);
            buff1[26] = (byte)((data&0xff0000)>>16);        
            buff1[27] = (byte)((data&0xff000000)>>24);
			result = agent.writeFile(73, buff1);
			Log.i(TAG, "setPsCali_valid222  buff1[0] = "+ buff1[0]);			
				
		} 
		catch (SecurityException se)
		 {
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "setPsCali_valid,agent writeFile fail");
		 }
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setPsCali_far_far(int data) throws RemoteException {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(19);		// AP_CFG_REEB_PRODUCT_INFO_LID
            byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
	        Log.i(TAG,"valid=" + buff[633]);	   
			buff[634] = (byte)(data/1000);		// 64+40 32+32+32+32+32+32+8 + 1
			buff[635] = (byte)((data%1000)/100);	
			buff[636] = (byte)((data%100)/10);
			buff[637] = (byte)(data%10);	// 32+32+32+32+32+32+8 + 2
		      
			//result = agent.writeFile(19, buff);
            result = agent.writeFileByName(NVRAM_HWMON_PS_LID, buff);
			
			Log.i(TAG, "setPsCali_close  result = "+ result);
			Log.i(TAG, "getFlag22  buff.length = "+ buff.length);
			 Log.i(TAG,"far far =" + buff[634]+","+buff[635]+","+buff[636]+","+buff[637]);   
		} 
		catch (SecurityException se)
		{
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "setPsCali_far_far,agent writeFile fail");
		}
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setPsCali_far_far_flag(int data) throws RemoteException {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(19);		// AP_CFG_REEB_PRODUCT_INFO_LID
            byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
	        Log.i(TAG,"valid=" + buff[638]);	   
			buff[638] = (byte)(data);	
		      
			//result = agent.writeFile(19, buff);
            result = agent.writeFileByName(NVRAM_HWMON_PS_LID, buff);
			
			Log.i(TAG, "setPsCali_far_far_flag  result = "+ result);
			Log.i(TAG,"setPsCali_far_far_flag =" + buff[638]);   
		} 
		catch (SecurityException se)
		{
			// TODO Auto-generated catch block
			se.printStackTrace();
			Log.e(TAG, "setPsCali_far_far_flag,agent writeFile fail");
		}
		catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getPsCali_far_far_flag() {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(19);		// AP_CFG_REEB_PRODUCT_INFO_LID
            byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
			
			result = (int)buff[638];
				
			Log.i(TAG, "getPsCali_far_far_flag  = "+ buff[633]);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public int getPsCali_valid() throws RemoteException {
		int result = 0;
		int i = 0;
		 
		try {
			//byte[] buff = agent.readFile(19);		// AP_CFG_REEB_PRODUCT_INFO_LID
			byte[] buff = agent.readFileByName(NVRAM_HWMON_PS_LID);
			result = (int)buff[0];
				
			Log.i(TAG, "getPsCali_valid  valid = "+ buff[0]);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	/**
	 * Log helper
	 * 
	 * @param flag
	 * @param tag
	 * @param msg
	 */
	public static void LOGV(boolean debugflag, String TAG, String msg) {
		if (debugflag)
			Log.v(TAG, msg);
	}

	public static void LOGI(boolean debugflag, String TAG, String msg) {
		if (debugflag)
			Log.v(TAG, msg);
	}

	public static void LOGE(String TAG, String msg) {
		Log.e(TAG, msg);
	}
}
