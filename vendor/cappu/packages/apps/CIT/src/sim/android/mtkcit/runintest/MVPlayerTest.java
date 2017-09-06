package sim.android.mtkcit.runintest;

import sim.android.mtkcit.RunInTest;

import android.content.Intent;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.widget.MediaController;
import android.widget.VideoView;
import sim.android.mtkcit.R;

public class MVPlayerTest extends RuninBaseActivity implements OnCompletionListener {
	MediaPlayer mediaPlayer;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean isPause;
	private boolean debugFlag = true;
	private final String TAG = "MVPlayerTest";
	private VideoView videoView;
	private MediaController mediaController;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
/*
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"MVPlayerTest");
		mWakeLock.acquire();
*/
		setContentView(R.layout.mvplayer);

	}

	private void initRec() {
		//new Thread(new mThread()).start();
		LOGV(debugFlag, TAG, "initRec=");
		videoView = (VideoView) findViewById(R.id.mVideoView);
		videoView.setOnCompletionListener(this);
		mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		ct.getSDPaths();
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(RunInTest.RUNIN + "/test.3gp");
		videoView.start();
	}

	@Override
	protected void onResume() {
		super.onResume();

		initRec();


	}
/*
	private void stopTest() {
		LOGV(debugFlag, TAG, "stopTest()");
		releaseResource();
		SharedPreferences settings = getSharedPreferences(RunInTest.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("test_complate", true);
		editor.commit();
		finish();
	}
*/
	@Override
	 void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		releaseResource();

		Intent intent = new Intent();
		intent.setClass(this, MemoryTest.class);
		startActivity(intent);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		LOGV(debugFlag, TAG, "onCompletion()");
		cyleTest();

	}
	private void releaseResource() {
		LOGV(debugFlag, TAG, "releaseResource()");
		this.finish();

	}
/*
	class mThread implements Runnable {
		@Override
		public void run() {
			while (mThreadFlag) {
				try {
					Thread.sleep(1000);

				} catch (InterruptedException e) {
					LOGV(debugFlag, TAG, "mThread error ");
					return;
				}
				currentTime = System.currentTimeMillis();
				LOGV(debugFlag, TAG, "currentTime=" + currentTime);
				LOGV(debugFlag, TAG, "stoptime=" + RunInTest.stopTime);

				if (currentTime > RunInTest.stopTime) {
					mThreadFlag = false;
					stopTest();
				}
			}
		}
	}
*/

}
