package sim.android.mtkcit.runintest;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.SystemProperties;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.R;
import sim.android.mtkcit.RunInTest;

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
import sim.android.mtkcit.testitem.VUMeter;

public class MicrophoneCycleTest extends RuninBaseActivity {

	private boolean debugFlag = true;
	private final String TAG = "MicrophoneCycleTest";
	private boolean testFlas = true;

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
	protected void onCreate(Bundle bundle) {

		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.microphonecycletest);

	}

	@Override
	protected void onResume() {
		initRec();
		super.onResume();
	}

	private void initRec() {

		new Thread(new mThread()).start();
		initResourceRefs();
	}

    private void initResourceRefs() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm
				.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MicTest");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);//modify by even
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
			mAudioTrack.setStereoVolume(0.3f, 0.3f);
			isRun = true;
			while (isRun) {
				int sampleRate = mAudioRecord.read(audioBuffer, 0,
						mAudioBufferSimpleSize);
				if (mAudioBufferSimpleSize < 0) {
					mAudioBufferSimpleSize = 0;
				}
				
				if ((mAudioTrack != null) && (!isClosing)){
					mAudioTrack.write(audioBuffer, 0, mAudioBufferSimpleSize);
				}

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
		isClosing = true;
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
            try{
                mAudioTrack.stop();
            }catch (IllegalStateException e)
            {
                e.printStackTrace();
            }

			mAudioTrack.release();
            mAudioManager.setMode(AudioManager.MODE_NORMAL); 

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
	}


	@Override
	void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, VibratorTest.class);
		startActivity(intent);
		this.finish();
	}
	
	class mThread implements Runnable {
		@Override
		public void run() {
			LOGV(debugFlag, TAG, "run() ");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOGV(debugFlag, TAG, "mThread error ");
				return;
			}
//			currentTime = System.currentTimeMillis();
//			LOGV(debugFlag, TAG, "currentTime=" + currentTime);
//			if (currentTime > RunInTest.stopTime) {
//				stopTest();
//
//			} else

                //modify by even
                //releaseRec();
				cyleTest();

		}
	}
}
