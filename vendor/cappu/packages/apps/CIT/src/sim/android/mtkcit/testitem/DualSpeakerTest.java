package sim.android.mtkcit.testitem;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.featureoption.SimcomFeatureOption;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.MediaPlayer;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.testitem.LCDTest.timetask;
import sim.android.mtkcit.cittools.CITJNI;

public class DualSpeakerTest extends TestBase{
	private String TAG = "DualSpeakerTest";

	private Button btn_main;
	private Button btn_ref;
	private Button btn_success;
	private Button btn_fail;
	private CITTools ct;
	private boolean main_flag;
	private boolean ref_flag;
	private MediaPlayer mp;
	private AudioManager am;
	private int nCurrentMusicVolume;
	private AlertDialog mDialog = null;
	CITJNI citjni;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.test_dualspk);

		ct = CITTools.getInstance(this);
		am = (AudioManager) getSystemService("audio");

		btn_main = (Button) findViewById(R.id.main_btn);
		btn_ref = (Button) findViewById(R.id.ref_btn);
		btn_main.setOnClickListener(this);
		btn_ref.setOnClickListener(this);
		
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
		
		ct.initButton(btn_success);
		
		citjni = new CITJNI();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		releaseRec();
		super.onStop();
	}

	public void onClick(View button) {
		if (!button.isEnabled())
			return;

		int id = button.getId();
		
		if (id == R.id.main_btn) {
//			Log.i(TAG, "id == R.id.main_btn");
			btn_main.setEnabled(false);
			btn_ref.setEnabled(true);
			main_flag = true;
			
			speakandStorageTest();
			citjni.setLeftSpeaker();
			
			if (ref_flag == true)
				btn_success.setEnabled(true);
			
			return;
		} else if (id == R.id.ref_btn) {
//			Log.i(TAG, "id == R.id.ref_btn");
			btn_main.setEnabled(true);
			btn_ref.setEnabled(false);
			ref_flag = true;
			
			speakandStorageTest();
			citjni.setRightSpeaker();
			
			if (main_flag == true)
				btn_success.setEnabled(true);
			
			return;
		}
		
//		Log.i(TAG, "id == ++++++");
		
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}
		releaseRec();
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	public void releaseRec() {
		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
				mp.release();
				mp = null;
			}
		}
		
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}
	
	private void speakandStorageTest() {
		String pathMusic;

		if (mp != null) {
			if (mp.isPlaying()) {
				mp.stop();
				mp.release();
				mp = null;
			}
		}
		// mod_s wangkang 2012.4.24 bug189
		ct.getSDPaths();

		// SDCard1 test
		// Unnecessary!
		if (ct.emmc) {
			// if(false) {
			if (!ct.checkSDCardMount(ct.SD2Path)) {
				Toast.makeText(this, R.string.no_sd, 1).show();

			} else {
				if (!btn_main.isEnabled())
					pathMusic = ct.SD2Path + "/CIT/test.mp3";
				else
					pathMusic = ct.SD2Path + "/CIT/test_2.mp3";
				
				File f = new File(pathMusic);
				if (!f.exists()) {
					Toast.makeText(this, R.string.speak_test_hasnot_sdcard2_file, 1).show();
				} else {
					int i = am.getStreamMaxVolume(3);
					int j = am.getStreamVolume(3);
					nCurrentMusicVolume = j;
					am.setStreamVolume(3, i, 0);
					Uri u = Uri.parse(pathMusic);
					mp = MediaPlayer.create(this, u);

					try {
						mp.setLooping(true);
						mp.start();
					} catch (Exception e) {
						return;
					}
				}
			}
		} else {
			if (!ct.checkSDCardMount(ct.SD1Path)) {
				Toast.makeText(this, R.string.no_sd, 1).show();

			} else {
				if (!btn_main.isEnabled())
					pathMusic = ct.SD2Path + "/CIT/test.mp3";
				else
					pathMusic = ct.SD2Path + "/CIT/test_2.mp3";
				
				File f = new File(pathMusic);
				if (!f.exists()) {
					Toast.makeText(this, R.string.speak_test_hasnot_sdcard2_file, 1).show();
				} else {
					int i = am.getStreamMaxVolume(3);
					int j = am.getStreamVolume(3);
					nCurrentMusicVolume = j;
					am.setStreamVolume(3, i, 0);
					Uri u = Uri.parse(pathMusic);
					mp = MediaPlayer.create(this, u);
					
					try {
						mp.setLooping(true);
						mp.start();
					} catch (Exception e) {
						return;
					}
				}
			}

		}
	}	//speakandStorageTest()
	
	
}
