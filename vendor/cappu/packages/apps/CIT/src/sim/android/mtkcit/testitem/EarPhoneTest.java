/*
 *  HeadSet Test
 *
 *  FileName :  MICTest.java
 *  Creator  :  xuanyang.feng 
 *  Date     :  2011/11/11
 *  Comment  :  CIT Test -> HeadSet Test  use  AudioTrack and AudioRecord
 *   
 */
package sim.android.mtkcit.testitem;

import java.text.SimpleDateFormat;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
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
import sim.android.mtkcit.testitem.MikeTest.RecordThread;

public class EarPhoneTest extends TestBase {
	static final String TAG = "MicTest";

	WakeLock mWakeLock;
	private AudioManager mAudioManager;
	private RecordThread mRecordThread;

	ImageButton mRecordButton;
	TextView txtDescript;

	public int state = 0;
	public int mVolume = 0;
	public int nCurrentMusicVolume;
	public boolean flag = false;
	public float LeftVolume;
	public float RightVolume;
	private Button btn_success;
	private Button btn_fail;
	static final int mAudioSourceType = MediaRecorder.AudioSource.MIC;

	static final int SAMPLE_RATE_IN_HZ = 8000;

	static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;

	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	int mAudioBufferSimpleSize;

	AudioRecord audioRecord;
	AudioTrack audioTrack;
	int recBufSize, playBufSize;
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	VUMeter mVUMeter;
	VerticalSeekBar mSeekBarLeft, mSeekBarRight;

	private boolean isRun = false;
	private boolean isClosing = false;
	final Handler mHandler = new Handler();
	final Runnable mUpdataResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	@Override
	public void onCreate(Bundle icycle) {
		super.onCreate(icycle);

		setContentView(R.layout.test_mic);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		ct.initButton(btn_success);

		txtDescript = (TextView) findViewById(R.id.textDescript);

		// initResourceRefs();

	}

	@Override
	protected void onStart() {
		// SystemClock.sleep(1000);
		initResourceRefs();

		super.onStart();
	}

	@Override
	protected void onResume() {
		// add_s wangkang 2012.5.10 bug900

		// add_e wangkang 2012.5.10 bug900
		super.onResume();
	}

	/*
	 * private void initResourceRefs() { PowerManager pm = (PowerManager)
	 * getSystemService(Context.POWER_SERVICE); mWakeLock = pm
	 * .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MicTest");
	 * 
	 * mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	 * setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	 * 
	 * // add_s wangkang 2012.5.3 bug896 nCurrentMusicVolume = mAudioManager
	 * .getStreamVolume(AudioManager.STREAM_VOICE_CALL);
	 * mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
	 * mAudioManager .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
	 * 
	 * // initAudioRecord(); initAudioTrack(); mVUMeter = (VUMeter)
	 * findViewById(R.id.uvMeter); mVUMeter.setVolume(0); mVUMeter.invalidate();
	 * mRecordThread = new RecordThread(); Log.v(TAG, "initResourceRefs");
	 * mRecordThread.start(); }
	 */
	/*
	 * Whenever the UI is re-created (due f.ex. to orientation change) we have
	 * to reinitialize references to the views.
	 */
	private void initResourceRefs() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MicTest");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// mAudioManager.setMode(AudioManager.MODE_IN_CALL);

		nCurrentMusicVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		// recBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
		// channelConfiguration, audioEncoding);
		// Log.v(TAG, "recBufSize=" + recBufSize);
		// playBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ,
		// channelConfiguration, audioEncoding);
		//
		// audioRecord = new AudioRecord(mAudioSourceType, SAMPLE_RATE_IN_HZ,
		// channelConfiguration, audioEncoding, recBufSize);
		// audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
		// SAMPLE_RATE_IN_HZ, channelConfiguration, audioEncoding,
		// playBufSize, AudioTrack.MODE_STREAM);

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
						mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

		initAudioTrack();
		this.registerReceiver(mheadSetReceiver, new IntentFilter(
				Intent.ACTION_HEADSET_PLUG));
		// bt_success.setEnabled(true);
		AudioManager audiomanager = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (!audiomanager.isWiredHeadsetOn()) {
			txtDescript.setText(R.string.headset_plug_warning);
		}
		mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		mVUMeter.setVolume(0);

		mSeekBarLeft = (VerticalSeekBar) findViewById(R.id.SeekBarLeft);
		mSeekBarLeft.setMax(100);
		mSeekBarLeft.setProgress(50);
		LeftVolume = 0.5f;
		mSeekBarRight = (VerticalSeekBar) findViewById(R.id.SeekBarRight);
		mSeekBarRight.setMax(100);
		mSeekBarRight.setProgress(100);
		RightVolume = 1.0f;
		mSeekBarLeft
				.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
					// @Override
					public void onStopTrackingTouch(
							VerticalSeekBar verticalSeekBar) {
						float vol = (float) (verticalSeekBar.getProgress())
								/ (float) (verticalSeekBar.getMax());
						LeftVolume = vol;
						audioTrack.setStereoVolume(LeftVolume, RightVolume);
					}

					public void onStartTrackingTouch(
							VerticalSeekBar verticalSeekBar) {
					}

					public void onProgressChanged(
							VerticalSeekBar verticalSeekBar, int progress,
							boolean fromUser) {
					}
				});
		mSeekBarRight
				.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {

					public void onStopTrackingTouch(
							VerticalSeekBar verticalSeekBar) {
						float vol = (float) (verticalSeekBar.getProgress())
								/ (float) (verticalSeekBar.getMax());
						RightVolume = vol;
						audioTrack.setStereoVolume(LeftVolume, RightVolume);
					}

					public void onStartTrackingTouch(
							VerticalSeekBar verticalSeekBar) {
					}

					public void onProgressChanged(
							VerticalSeekBar verticalSeekBar, int progress,
							boolean fromUser) {
					}

				});

	}

	private void initAudioTrack() {
		Log.v(TAG, "initAudioTrack");

		playBufSize = 2 * AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				channelConfiguration, audioEncoding);

		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
				SAMPLE_RATE_IN_HZ, channelConfiguration, audioEncoding,
				playBufSize, AudioTrack.MODE_STREAM);
		int audioTrackState = audioTrack.getState();
		if (audioTrackState != AudioTrack.STATE_INITIALIZED) {
			Log.v(TAG, "AudioTrack is initialized");
			finish();
		}
		initAudioRecord();
	}

	private void initAudioRecord() {
		Log.v(TAG, "initAudioRecord");

		recBufSize = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				channelConfiguration, audioEncoding);
		mAudioBufferSimpleSize = recBufSize / 2;

		audioRecord = new AudioRecord(mAudioSourceType, SAMPLE_RATE_IN_HZ,
				channelConfiguration, audioEncoding, recBufSize);
		int audioRecordState = audioRecord.getState();
		if (audioRecordState != AudioRecord.STATE_INITIALIZED) {
			Log.v(TAG, "AudioRecord is initialized");
			finish();
		}
	}

	private BroadcastReceiver mheadSetReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String s1 = getString(R.string.test_item_heakloopback);
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				if (intent.getIntExtra("state", 0) == 1) {
					btn_fail.setEnabled(true);
					// mRecordButton.setEnabled(true);

					// txtDescript.setText(s1);
					txtDescript
							.setText(getString(R.string.headloopback_test_message));

					onStateChanged(1);
				} else {
					if (audioRecord != null) {
						Log.v(TAG,
								"audioRecord.getState()="
										+ audioRecord.getState());
						audioRecord.getState();
						audioRecord.stop();
					}
					s1 = getString(R.string.headset_plug_warning);
					btn_fail.setEnabled(false);
					// mRecordButton.setEnabled(false);
					txtDescript.setText(s1);
				}
			}
		}
	};

	public void onClick(View button) {
		if (!button.isEnabled())
			return;

		int id = button.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();
		//
		// if(id == R.id.recordButton){
		// if(state == 0){
		// Log.d("CIT", "start record");
		// onStateChanged(1);
		// } else if (state == 1) {
		// onStateChanged(0);
		// }
		// }
		// else {

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}
		intent.putExtras(b);
//		audioTrack.release();
//		audioRecord.release();
		
		isClosing = true;
		
		if (audioTrack != null) {
			audioTrack.release();

		}
		if (audioRecord != null) {
			audioRecord.release();
		}
		
		setResult(RESULT_OK, intent);
		finish();
		// }
	}

	private int j;

	class RecordThread extends Thread {

		// private boolean isRun = false;
		public RecordThread() {
			super();
		}

		short[] audioBuffer;

		public void run() {
			// Log.d("CIT", "Run");
			// super.run();
			// // for read
			// byte[] buffer = new byte[mAudioBufferSimpleSize];
			// audioRecord.startRecording();
			// audioTrack.play();
			// audioTrack.setStereoVolume(LeftVolume, RightVolume);
			// isRun = true;
			// while (isRun) {
			// int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
			// if (bufferReadResult < 0) {
			// bufferReadResult = 0;
			// }
			// byte[] tmpBuf = new byte[bufferReadResult];
			// System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
			// audioTrack.write(tmpBuf, 0, tmpBuf.length);
			//
			// int v = 0;
			// // buffer
			// for (int i = 0; i < buffer.length; i++) {
			// v += buffer[i] * buffer[i];
			// }
			//
			// mVolume = v / (bufferReadResult + 1);
			// Log.d("CIT", String.valueOf(mVolume));
			//
			// // Message message = mHandler.obtainMessage();
			// // message.what = volume;
			// // mHandler.dispatchMessage(message);
			// mHandler.post(mUpdataResults);
			// }
			// // audioTrack.stop();
			// // audioRecord.stop();
			// finish();
			Log.d("CIT", "Run");
			super.run();

			if (audioRecord == null)
				return;
			audioBuffer = new short[mAudioBufferSimpleSize];
			audioRecord.startRecording();
			int audioRecordState = audioRecord.getRecordingState();
			if (audioRecordState != AudioRecord.RECORDSTATE_RECORDING) {
				Log.v(TAG, "not recording");
				return;
			} else {
				Log.v(TAG, "start recording");

			}

			if (audioTrack == null)
				return;
			audioTrack.play();
			int audioTracktate = audioTrack.getPlayState();
			if (audioTracktate != AudioTrack.PLAYSTATE_PLAYING) {
				Log.v(TAG, "not playing");
				return;

			}

			Log.v(TAG, "start playing");
			audioTrack.setStereoVolume(0.5f, 1.0f);
			isRun = true;
			while (isRun) {
				int sampleRate = audioRecord.read(audioBuffer, 0,
						mAudioBufferSimpleSize);
				if (mAudioBufferSimpleSize < 0) {
					mAudioBufferSimpleSize = 0;
				}
				
				if ((audioTrack != null) && (!isClosing))
					audioTrack.write(audioBuffer, 0, mAudioBufferSimpleSize);

				int v = 0;
				for (int i = 0; i < audioBuffer.length; i++) {
					v += audioBuffer[i] * audioBuffer[i];
				}
				mVolume = v / (50 * (sampleRate + 1));
				Log.v(TAG, "mVolume=" + mVolume);
				j++;
				if (j < 5) {
					mVolume = 20;
				}
				mHandler.post(mUpdataResults);

			}

			finish();
		}

		public void pause() {
			// Activity onPause
			isRun = false;
		}

		public void start() {
			// Activity onResume
			if (!isRun) {
				super.start();
			}
		}

	}

	/*
	 * Handle the "back" hardware key.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (state) {
			case 0:
				finish();
				break;
			case 1:
				onStateChanged(0);
				break;
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onStop() {
		releaseRec();
		super.onStop();
	}

	@Override
	protected void onPause() {
		if (state == 1) {
			onStateChanged(0);
		}
		super.onPause();
	}

	private void releaseRec() {
		if (mAudioManager != null) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					nCurrentMusicVolume, 0);
			mAudioManager.setMode(AudioManager.MODE_NORMAL);
		}
		if (audioTrack != null) {
			audioTrack.release();

		}
		if (audioRecord != null) {
			audioRecord.release();

		}
		if (mheadSetReceiver != null) {
			this.unregisterReceiver(mheadSetReceiver);

		}
		isRun = false;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	private boolean mFirstUpdate = true;

	private void updateUI() {
		if (mFirstUpdate) {
			SystemClock.sleep(1000l);
			mFirstUpdate = false;
			mVolume = 0;
			// return;
		}
		
//		Log.i("MikeTest000", "updateUI(), mVolume=" + mVolume);
		mVUMeter.setVolume(mVolume);
		if ((!flag) && (mVolume > 3000)) {
			btn_success.setEnabled(true);
			flag = true;
		}
	}

	/*
	 * private Handler mHandler = new Handler() { public void
	 * handleMessage(Message response) { int what = response.what;
	 * mVUMeter.setVolume(what);
	 * 
	 * } };
	 */

	public void onStateChanged(int newState) {

		if (newState == 1) {
			mWakeLock.acquire();
			state = 1;
			// mRecordButton.setImageResource(R.drawable.stop);
			txtDescript.setText(getString(R.string.headloopback_test_message));
			SystemClock.sleep(1000l);
			mRecordThread = new RecordThread();
			mRecordThread.start();
		} else {
			// if (audioRecord != null) {
			// audioRecord.stop();
			// audioRecord.release();
			// }
			state = 0;
			// mRecordButton.setImageResource(R.drawable.play);
			// txtDescript.setText(getString(R.string.test_item_heakloopback));
			mRecordThread.pause();
			if (mWakeLock.isHeld())
				mWakeLock.release();
		}

	}

}
