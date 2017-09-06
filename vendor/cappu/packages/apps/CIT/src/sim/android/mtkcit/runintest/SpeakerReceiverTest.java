package sim.android.mtkcit.runintest;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import sim.android.mtkcit.R;
import sim.android.mtkcit.RunInTest;

public class SpeakerReceiverTest extends RuninBaseActivity implements
		OnCompletionListener {
	private TextView speakerTest;
	private TextView receiverTest;
	private TextView speakerTestRes;
	private TextView receiverTestRes;
	private boolean speakerFlag;
	private boolean receiverFlag;
	private boolean debugFlag = true;
	private final String TAG = "SpeakerReceiverTest";
	static final int mAudioReceiverType = MediaRecorder.AudioSource.MIC;
	static final int mAudioSperkerType = MediaRecorder.AudioSource.DEFAULT;
	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	private AudioManager mAudioManager;
	private MediaPlayer mp;
	private int nCurrentRecVolume;
	private int nCurrentSpeVolume;
	private String musicPath;

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		setContentView(R.layout.speakerreceivertest);
		speakerTest = (TextView) findViewById(R.id.tv_speaker_test);
		speakerTestRes = (TextView) findViewById(R.id.tv_speaker_test_result);
		receiverTest = (TextView) findViewById(R.id.tv_receiver_test);
		receiverTestRes = (TextView) findViewById(R.id.tv_receiver_test_result);
	}
	@Override
	protected void onResume() {
		super.onResume();
		initRec();

	}
	private void initRec() {
		LOGV(debugFlag, TAG, "initRec=");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ct.getSDPaths();
		musicPath = RunInTest.RUNIN + "/fhzy.mp3";
		LOGV(debugFlag, TAG, "musicPath=" + musicPath);

		try {
			mp = new MediaPlayer();
			mp.setDataSource(musicPath);
			mp.setLooping(false);
			mp.setOnCompletionListener(this);
		} catch (Exception e) {
			Log.e(TAG, "receiverTest mp.prepare() error");
		}
		if (ct.checkSDCardMount(ct.SD1Path))
			receiverTest();
	}

	private void receiverTest() {
		LOGV(debugFlag, TAG, " receiverTest()");

		receiverFlag = true;
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		nCurrentRecVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

		try {
			mp.prepare();
		} catch (Exception e) {
			Log.e(TAG, "receiverTest mp.prepare() error");
		}
		mp.start();
		receiverTestRes.setText(R.string.testing);
		LOGV(debugFlag, TAG, " receiverTest() begin ");

	}

	private void sperkerTest() {
		LOGV(debugFlag, TAG, "sperkerTest");
		speakerFlag = true;
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		nCurrentSpeVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		try {
			mp.prepare();
		} catch (Exception e) {
			Log.e(TAG, "receiverTest mp.prepare() error");
		}
		mp.start();
		speakerTestRes.setText(R.string.testing);

		LOGV(debugFlag, TAG, " sperkerTest() begin ");

	}



	@Override
	protected void onStop() {
		super.onStop();

		Log.v(TAG, "onStop");
		releaseResource();
	}
/*
	private void stopTest() {
		LOGV(debugFlag, TAG, "stopTest()");
		SharedPreferences settings = getSharedPreferences(RunInTest.PREFS_NAME,
				0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("test_complate", true);
		editor.commit();
		finish();

	}

	private void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, ScreenTest.class);
		startActivity(intent);
	}
*/
	private void releaseResource() {
		LOGV(debugFlag, TAG, "releaseResource()");


		if (mp != null) {
			mp.release();
			mp = null;
		}

		if (mAudioManager != null) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					nCurrentRecVolume, 0);
		
//			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//					nCurrentSpeVolume, 0);

			mAudioManager.setMode(AudioManager.MODE_NORMAL);
		}
		this.finish();

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (receiverFlag) {
			receiverTestRes.setText(R.string.test_finish);
			receiverFlag = false;
			sperkerTest();
			return;
		} else if (speakerFlag) {
			speakerTestRes.setText(R.string.test_finish);
			cyleTest();
		}

	}
	@Override
	void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, MicrophoneCycleTest.class);//modify by even
		startActivity(intent);		
	}

}
