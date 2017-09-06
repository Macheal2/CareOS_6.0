package sim.android.mtkcit.testitem;

import com.mediatek.FMRadio.IFMRadioService;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.CITBroadcastReceiver;
import sim.android.mtkcit.R;

import android.hardware.Camera.Parameters;

public class FMTest extends TestBase implements Handler.Callback {
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	TextView tv_chargeinfo;
	AudioTrack audioTrack;
	private AudioManager mAudioManager;
	private int mCurrentVolume;
	Parameters parameters;
	private BroadcastReceiver mBatteryInfoReceiver;
	String batteryStatus;
	private String TAG = "FMTest";
	private boolean debugeFlag = true;
	private boolean mRun = true;
	private Handler myHandler;
	TextView tv_testinfo;
	TextView tv_fm_curfreq;
	TextView tv_freq;
	private String result;
	private boolean mbServiceStarted = false;
	private boolean mFMServiceBinded = false;
	private boolean devicesOpen = false;
	private boolean FMStart = false;
	private boolean FMFlag = false;
	RadioButton useEarphone;
	RadioButton useLoudspeaker;
	private String frequency[];;
	private int mCurrentStation = 985;
	private IFMRadioService mService = null;
	private boolean earPhoneOn = false;
	private ServiceConnection mServiceConnection = null;
	private PowerManager pm;
	private WakeLock mWakeLock;
	private RadioGroup mFMListenerType;
	private EditText mEditFreq = null;
	private Button btn_tune;
	private BroadcastReceiver mheadSetReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "onReceive");
			String s1 = getString(R.string.headset_plug_warning);
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				if (intent.getIntExtra("state", 0) == 1) {
					btn_fail.setEnabled(true);
					s1 = getString(R.string.fm_Curfreq);
					tv_freq.setText(mCurrentStation + "");

					earPhoneOn = true;
					onStateChanged(1);
				} else {
					earPhoneOn = false;
					s1 = getString(R.string.headset_plug_warning);
					onStateChanged(0);
					tv_freq.setText("...");
				}
			}
			tv_fm_curfreq.setText(s1);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate() ");
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.fm_test);
		tv_testinfo = (TextView) findViewById(R.id.test_info);
		tv_fm_curfreq = (TextView) findViewById(R.id.fm_curfreq);
		tv_freq = (TextView) findViewById(R.id.freq);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);

		mFMListenerType = (RadioGroup) findViewById(R.id.use_type);
		useEarphone = (RadioButton) findViewById(R.id.use_earphone);
		useEarphone.setOnClickListener(this);
		useLoudspeaker = (RadioButton) findViewById(R.id.use_loudspeaker);
		useLoudspeaker.setOnClickListener(this);

		mEditFreq = (EditText) findViewById(R.id.FMR_Freq_edit);
		btn_tune = (Button) findViewById(R.id.fm_tune);
		btn_tune.setOnClickListener(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


	}

	/**
 	public static int PCBA_FM_Frequency = 985;
	public static int Machine_FM_Frequency = 985;
	
 */
	@Override
	protected void onStart() {
		Log.v(TAG, "onStart() ");
		initAllControl();
		super.onStart();
	}

	private void initAllControl() {
		bindService();
		

		if (CITActivity.Testtype == CITActivity.MACHINE_TEST) {
//			mCurrentStation = Integer.parseInt(frequency[1].trim());
			mCurrentStation=CITBroadcastReceiver.Machine_FM_Frequency;
		}
		if (CITActivity.Testtype == CITActivity.PCBA_TEST) {
//			mCurrentStation = Integer.parseInt(frequency[0].trim());
			mCurrentStation = CITBroadcastReceiver.PCBA_FM_Frequency;
		}
		myHandler = new Handler(this);
		this.registerReceiver(mheadSetReceiver, new IntentFilter(
				Intent.ACTION_HEADSET_PLUG));
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
				.getClass().getName());
		mWakeLock.acquire();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.DEVICE_OUT_FM);
		mAudioManager.setStreamVolume(AudioManager.DEVICE_OUT_FM, 10, 0);

	}

	private void bindService() {
		Log.v(TAG, "bindService() ");

		mServiceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				Log.i(TAG, ">>> FMRadioCITTest.onServiceConnected");
				mService = IFMRadioService.Stub.asInterface(service);
				if (null == mService) {
					Log.e(TAG, "Error: null interface");
					finish();
				} else {
					startFM();

				}
			}

			public void onServiceDisconnected(ComponentName className) {
				mService = null;
				Log.v(TAG, "<<< FMTest.onServiceDisconnected");
			}
		};

		mFMServiceBinded = bindService(new Intent(
				"com.mediatek.FMRadio.IFMRadioService"), mServiceConnection,
				Context.BIND_AUTO_CREATE);

		if (!mFMServiceBinded) {
			Log.e(TAG, "Error: Cannot bind FM service");
			finish();
			return;
		} else {
			Log.v(TAG, "Bind FM service successfully.");
		}

	}

	public void releaseRec() {
		Log.v(TAG, "releaseRec");
		mAudioManager
				.setStreamVolume(AudioManager.DEVICE_OUT_FM, mCurrentVolume, 0);
		if (null != mheadSetReceiver) {
			unregisterReceiver(mheadSetReceiver);
			mheadSetReceiver = null;
		}
		// if (mbPlaying) {
		setMute(true);
		powerDown();
		mbPlaying = false;
		Log.i(TAG, "powerDown.");
		if (mWakeLock != null) {
			mWakeLock.release();
		}
		if (mFMServiceBinded) {
			Log.i(TAG, "unbindService.");

			unbindService(mServiceConnection);
			mFMServiceBinded = false;
		}
		mRun = false;
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume() ");

		mRun = true;
		super.onResume();
	}

	private void useEarphone(boolean use) {
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				mService.useEarphone(use);
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.v(TAG, "<<< FMRadioActivity.useEarphone");
	}

	private boolean isEarphoneUsed() {
		Log.v(TAG, ">>> FMRadioActivity.isEarphoneUsed");
		boolean bRet = true;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.isEarphoneUsed();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.v(TAG, "<<< FMRadioActivity.isEarphoneUsed: " + bRet);
		return bRet;
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause() ");

		// startFM();
		super.onPause();
	}

	private void startFM() {
		Log.v(TAG, "startFM() ");
		btn_success.setEnabled(false);
		if (isEarphoneUsed()) {
			useEarphone(false);
		}
		try {
			if (!isServiceInit()) {
				initService(mCurrentStation);
				new Thread(new InitialThread()).start();
			} else {

				Log.i(TAG, "FM service is already init.");
				if (isDeviceOpen()) {
					mbPlaying = isPowerUp();
					Log.i(TAG, "startFM--mbPlaying=" + mbPlaying);

					if (!mbPlaying) {
						Log.i(TAG, "powerUp((float) mCurrentStation / 10)");
						bRes = powerUp((float) mCurrentStation / 10);
					} else {
						bRes = tune((float) mCurrentStation / 10);

					}

				} else {
					Log.e(TAG, "Error: FM device is not open");
				}
			}
			FMFlag = mbPlaying || bRes;
			btn_success.setEnabled(FMFlag);
			if (FMFlag) {
				tv_freq.setText(mCurrentStation + "");

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "startFM erro");
		}

	}

	boolean bRes;

	private class InitialThread implements Runnable {

		@Override
		public void run() {

			if (!openDevice()) {
				// ... If failed, exit?
				Log.e(TAG, "Error: opendev failed.");
			} else {
				mbPlaying = isPowerUp();
				Log.i(TAG, "opendev succeed. mbPlaying = " + mbPlaying);

			}
			if (!mbPlaying) {
				bRes = powerUp((float) mCurrentStation / 10);
				Log.i(TAG, "opendev succeed. bRes = " + bRes);
			}
			Message msg = new Message();
			Bundle date = new Bundle();
			date.putBoolean("fmflag", mbPlaying || bRes);
			msg.setData(date);
			myHandler.sendMessage(msg);
		}

	}

	@Override
	protected void onStop() {
		Log.v(TAG, "onStop() ");
		releaseRec();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onClick(View v) {
		Log.v(TAG, "onClick");

		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.use_earphone) {
			useEarphone(true);
			return;

		}
		if (id == R.id.use_loudspeaker) {
			useEarphone(false);
			return;
		}
		if (id == R.id.fm_tune) {
			String s = mEditFreq.getText().toString();
			int freq = 0;
			try {
				freq = Integer.valueOf(s);
			} catch (NumberFormatException e) {
				Toast.makeText(this, R.string.fm_bad_format, Toast.LENGTH_SHORT)
						.show();
				return;
			}
			tuneToStation(freq);
			return;

		}

		Bundle b = new Bundle();
		Intent intent = new Intent();
		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		}
		if (id == R.id.btn_fail) {
			b.putInt("test_result", 0);
		}
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		Log.v(TAG, "finish");
		finish();
	}

	private void tuneToStation(int freq) {
		mbPlaying = isPowerUp();
		if (mbPlaying) {
			boolean bRes = tune((float) freq / 10);
			if (bRes) {
				Log.i(TAG, "Tune to the station succeeded.");
				mCurrentStation = freq;
				tv_freq.setText(mCurrentStation + "");
			} else {
				Log.e(TAG, "Error: Can not tune to the station.");
			}
		} else {
			mCurrentStation = freq;
			startFM();
		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle date;
		date = msg.getData();
		FMFlag = date.getBoolean("fmflag");
		btn_success.setEnabled(FMFlag);
		return false;
	}

	private int state;

	public void onStateChanged(int newState) {

		if (newState == 1) {
			state = 1;
			startFM();
			showRadio(true);

		} else {
			btn_success.setEnabled(false);
			showRadio(false);
			state = 0;
		}

	}

	private void showRadio(boolean clickable) {
		useEarphone.setClickable(clickable);
		useLoudspeaker.setClickable(clickable);
	}

	private int setMute(boolean mute) {
		Log.i(TAG, ">>> FMRadioCITTest.setMute");
		int iRet = -1;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				iRet = mService.setMute(mute);
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		return iRet;
	}

	private boolean mbPlaying = false;

	private boolean isServiceInit() {
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.isServiceInit();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.isServiceInit: " + bRet);
		return bRet;
	}

	private void initService(int iCurrentStation) {
		Log.i(TAG, ">>> FMRadioCITTest.initService: " + iCurrentStation);
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				mService.initService(iCurrentStation);
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.initService");
	}

	private boolean powerDown() {
		Log.i(TAG, ">>> FMRadioCITTest.powerDown");
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.powerDown();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.powerDown: " + bRet);
		return bRet;
	}

	private boolean openDevice() {
		Log.i(TAG, ">>> FMRadioCITTest.openDevice");
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.openDevice();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.openDevice: " + bRet);
		return bRet;
	}

	private boolean closeDevice() {
		Log.i(TAG, ">>> FMRadioCITTest.closeDevice");
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.closeDevice();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.closeDevice: " + bRet);
		return bRet;
	}

	private boolean isPowerUp() {
		Log.i(TAG, ">>> FMRadioCITTest.isPowerUp");
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.isPowerUp();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.isPowerUp: " + bRet);
		return bRet;
	}

	private boolean isDeviceOpen() {
		Log.i(TAG, ">>> FMRadioCITTest.isDeviceOpen");
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.isDeviceOpen();
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.i(TAG, "<<< FMRadioCITTest.isDeviceOpen: " + bRet);
		return bRet;
	}

	private boolean powerUp(float frequency) {
		Log.i(TAG, ">>> FMRadioCITTest.powerUp");
		boolean bRet = false;
		// mbOnPowerUp = true;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.powerUp(frequency);
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		// mbOnPowerUp = false;
		Log.i(TAG, "<<< FMRadioCITTest.powerUp: " + bRet);
		return bRet;
	}

	private boolean tune(float frequency) {
		boolean bRet = false;
		if (null == mService) {
			Log.e(TAG, "Error: No service interface.");
		} else {
			try {
				bRet = mService.tune(frequency);
			} catch (Exception e) {
				Log.e(TAG, "Exception: Cannot call service function.");
			}
		}
		Log.v(TAG, " tune: " + bRet);
		return bRet;
	}

}
