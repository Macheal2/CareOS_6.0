/*
 *  HeadSet Test
 *
 *  FileName :RECTest.java
 *  Creator  :  xuanyang.feng
 *  Date     :  2011/12/1
 *  Comment  :  CIT Test -> HeadSet Test  use  AudioTrack and AudioRecord
 *   
 */
package sim.android.mtkcit.testitem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

public class RECTest extends TestBase implements Handler.Callback,
		ITaskCallBack , View.OnClickListener {
	static final String TAG = "RECTest";
	private Button btn_success;
	private Button btn_fail;
	WakeLock mWakeLock;
	private AudioManager mAudioManager;
	private MediaPlayer mp;

	ImageButton mRecordButton;

	TextView txtDescript;
	public int se_Time = 0;
	public int min_Time = 0;
	public int ho_Time = 0;
	public int state = 0;
	public int mVolume = 0;
	public int nCurrentMusicVolume;
	public boolean flag = false;
	public float LeftVolume;
	public float RightVolume;
	byte[] buffer;
	byte[] tmpBuf;
	static final int mAudioSourceType = MediaRecorder.AudioSource.MIC;

	static final int SAMPLE_RATE_IN_HZ = 8000;

	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;

	int recBufSize = 0, playBufSize = 0;
	private TextView timeView;
	private PowerManager pm;
	public Timer timer;
	private Handler myHandler;
	private int total = 0;
	private String time;

	@Override
	public void onCreate(Bundle icycle) {

		super.onCreate(icycle);
		setContentView(R.layout.test_rec);
		timeView = (TextView) findViewById(R.id.time_show);
		myHandler = new Handler(this);

	}

	@Override
	protected void onStart() {
		initResourceRefs();

		super.onStart();
	}

	private void initResourceRefs() {

		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MicTest");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// modified by bruce for REC test begin
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		//mAudioManager.setMode(AudioManager.MODE_RINGTONE);
		// modified by bruce for REC test end
		nCurrentMusicVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Log.i("bruce_nan", "RECTest_nfl_01: nCurrentMusicVolume = " + nCurrentMusicVolume);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nCurrentMusicVolume, 0);
		// modified by bruce for REC test begin
		//mp = MediaPlayer.create(this, R.raw.test);
		//mp = MediaPlayer.create(this, R.raw.cit_rec);
        //modify by even
        mp = MediaPlayer.create(this, R.raw.fhzy);//∑€∫Ï‘Î“Ù
		// modified by bruce for REC test end
		mp.setLooping(true);
		try {
			mp.prepare();
		} catch (Exception e) {
			Log.e(TAG, "mp.prepare() error");
		}
		mp.start();
		startTimer();
	}

	public void onClick(View button) {
		if (!button.isEnabled())
			return;

		int id = button.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}
		if (mp != null) {
			mp.stop();
		}
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onStop() {

		releaseResource();
		super.onStop();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	private void releaseResource() {
		timer.cancel();

		if (mp != null) {
			mp.release();
			mp = null;
		}
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				nCurrentMusicVolume, 0);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			Bundle date = msg.getData();
			se_Time = date.getInt("time");
			if (se_Time == 60) {
				min_Time++;
				se_Time = 0;
				if (se_Time == 60) {
					ho_Time++;
					se_Time = 0;
				}
			}
			time = ho_Time + " : " + min_Time + " : " + se_Time;
			timeView.setText(time);

			Log.d(TAG,
					"HandlerMessage:"
							+ String.valueOf(Thread.currentThread().getId()));
			Log.d(TAG, "msgDate:" + String.valueOf(date.getInt("time")));
			break;

		}
		return false;
	}

	public class Task extends TimerTask {

		private ITaskCallBack iTask;

		public Task(ITaskCallBack iTaskCallBack) {
			super();
			iTask = iTaskCallBack;
		}

		public void setCallBack(ITaskCallBack iTaskCallBack) {
			iTask = iTaskCallBack;
		}

		@Override
		public void run() {
			iTask.TaskRun();
		}

	}

	private synchronized void startTimer() {

		timer = new Timer();
		Task updateTimerValuesTask = new Task(this);
		timer.schedule(updateTimerValuesTask, 1000, 1000);
	}

	private void updateTimerValues() {
		se_Time++;

		Log.d(TAG, "send:" + String.valueOf(Thread.currentThread().getId()));
		Message msg = new Message();
		Bundle date = new Bundle();
		date.putInt("time", se_Time);
		msg.setData(date);
		msg.what = 0;
		myHandler.sendMessage(msg);

	}

	@Override
	public void TaskRun() {
		updateTimerValues();

	}
}
