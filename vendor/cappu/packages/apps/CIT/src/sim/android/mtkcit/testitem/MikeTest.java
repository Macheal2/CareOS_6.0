/*
 *  HeadSet Test
 *
 *  FileName : MikeTest.java
 *  Creator  :  xuanyang.feng
 *  Date     :  2011/12/1
 *  Comment  :  CIT Test -> HeadSet Test  use  AudioTrack and AudioRecord
 *   
 */
package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

public class MikeTest extends TestBase  {
	static final String TAG = "MikeTest";
	private boolean isRun = false;
	private boolean isClosing = false;
	WakeLock mWakeLock;
	private AudioManager mAudioManager;
	private RecordThread mRecordThread;
	int bufferReadResult;
	ImageButton mRecordButton;
	TextView txtDescript;
	byte[] 	tmpBuf;
	public int state = 0;
	public int mVolume = 0;
	public int nCurrentMusicVolume;
	public boolean flag = false;
	public float LeftVolume;
	public float RightVolume;

	static final int mSourceType = MediaRecorder.AudioSource.MIC;

	static final int SAMPLE_RATE_IN_HZ = 8000;

	static final int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
	private Button btn_success;
	private Button btn_fail;
	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	public static final int PCBA_TEST = 0;
	public static final int MACHINE_TEST = 1;
	int recBufSize = 0, playBufSize = 0;
	int mAudioBufferSize;
	int mAudioBufferSimpleSize;
	int mRecBufferSimpleSize;
	VUMeter mVUMeter;
	AudioRecord mAudioRecord;
	AudioTrack mAudioTrack;
	private int j;
	int sampleRate = 8000;
	int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	final Handler mHandler = new Handler();
	final Runnable mUpdataResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	@Override
	public void onCreate(Bundle icycle) {

		super.onCreate(icycle);
		setContentView(R.layout.test_mike);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);

	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	protected void onStart() {
		initResourceRefs();

		super.onStart();
	}

	private void initResourceRefs() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MicTest");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

		// add_s wangkang 2012.5.3 bug896
		nCurrentMusicVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
				mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);

		// initAudioRecord();
		initAudioTrack();
		mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
		mVUMeter.setVolume(0);
		mVUMeter.invalidate();
		mRecordThread = new RecordThread();
		Log.v(TAG, "initResourceRefs");
		mRecordThread.start();
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
		intent.putExtras(b);
		
		isClosing = true;
		releaseRec() ;
		setResult(RESULT_OK, intent);
		finish();
	}

	private void initAudioRecord() {
		Log.v(TAG, "initAudioRecord");

		recBufSize = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				channelConfiguration, audioEncoding);

		mAudioBufferSimpleSize = recBufSize / 2;
		mAudioRecord = new AudioRecord(mSourceType, SAMPLE_RATE_IN_HZ,
				channelConfiguration, audioFormat, recBufSize);
		int audioRecordState = mAudioRecord.getState();
		if (audioRecordState != AudioRecord.STATE_INITIALIZED) {
			Log.v(TAG, "AudioRecord is initialized");
			finish();
		}
	}

	private void initAudioTrack() {
		Log.v(TAG, "initAudioTrack");

		playBufSize = 2 * AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				channelConfiguration, audioEncoding);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
				SAMPLE_RATE_IN_HZ, channelConfiguration, audioFormat,
				playBufSize, AudioTrack.MODE_STREAM);
		int audioTrackState = mAudioTrack.getState();
		if (audioTrackState != AudioTrack.STATE_INITIALIZED) {
			Log.v(TAG, "AudioTrack is initialized");
			finish();
		}
		initAudioRecord();
	}

	short[] audioBuffer;

	class RecordThread extends Thread {

		public RecordThread() {
			super();
		}

		public void run() {
			Log.d("CIT", "Run");
			super.run();

			if (mAudioRecord == null)
				return;
			audioBuffer = new short[mAudioBufferSimpleSize];
			mAudioRecord.startRecording();
			int audioRecordState = mAudioRecord.getRecordingState();
			if (audioRecordState != AudioRecord.RECORDSTATE_RECORDING) {
				Log.v(TAG, "not recording");
				return;
			} else {
				Log.v(TAG, "start recording");

			}
			
			if (mAudioTrack == null)
				return;
			mAudioTrack.play();
			int audioTracktate = mAudioTrack.getPlayState();
			if (audioTracktate != AudioTrack.PLAYSTATE_PLAYING) {
				Log.v(TAG, "not playing");
				return;

			}

			Log.v(TAG, "start playing");
			//mAudioTrack.setStereoVolume(0.3f, 0.3f);
            mAudioTrack.setStereoVolume(1.0f, 1.0f); // modified by bruce for adjust mike stereo volume
			isRun = true;
			while (isRun) {
				int sampleRate = mAudioRecord.read(audioBuffer, 0,
						mAudioBufferSimpleSize);
				if (mAudioBufferSimpleSize < 0) {
					mAudioBufferSimpleSize = 0;
				}
				
				if ((mAudioTrack != null) && (!isClosing))
					mAudioTrack.write(audioBuffer, 0, mAudioBufferSimpleSize);

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

	@Override
	public void onStop() {
		releaseRec();
		super.onStop();
	}

	private void releaseRec() {
		if (mAudioManager != null) {
			//mod_s wangkang 2012.5.3	bug896
			mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
					nCurrentMusicVolume, 0);
			//mod_s wangkang 2012.5.3	bug896
		}
		if (mAudioTrack != null) {
			mAudioTrack.release();

		}
		if (mAudioRecord != null) {
			mAudioRecord.release();

		}
		isRun = false;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void updateUI() {
		
//		Log.i("MikeTest000", "updateUI(), mVolume=" + mVolume);
		
		mVUMeter.setVolume(mVolume);
		if ((!flag) && (mVolume > 3000)) {
			btn_success.setEnabled(true);
			flag = true;
		}
	}

}
