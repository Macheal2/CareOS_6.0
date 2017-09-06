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

public class HDMITest extends TestBase 
{
	static final String TAG = "HdmiTest";
	private  TextView mTvHdmi;
	private  Button mBtnSuccess;
	private  Button mBtnFail;

	@Override
	public void onCreate(Bundle icycle) 
	{
		super.onCreate(icycle);

		setContentView(R.layout.test_hdmi);
		mBtnFail = (Button) findViewById(R.id.btn_fail);
		mBtnFail.setOnClickListener(this);
		mBtnSuccess = (Button) findViewById(R.id.btn_success);
		mBtnSuccess.setOnClickListener(this);
		mBtnSuccess.setEnabled(false);
		mTvHdmi = (TextView) findViewById(R.id.test_hdmi);
		initButton();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		this.registerReceiver(mhdmiReceiver, new IntentFilter(Intent.ACTION_HDMI_PLUG));
	}

	@Override
	protected void onStop() 
	{
		this.unregisterReceiver(mhdmiReceiver);
		super.onStop();
	}
	private BroadcastReceiver mhdmiReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_HDMI_PLUG)) 
			{
				if (intent.getIntExtra("state", 0) == 1) 
				{
					mBtnSuccess.setEnabled(true);
					mTvHdmi.setText(getString(R.string.test_hmdi_plugin));
				} 
				else 
				{
					mBtnSuccess.setEnabled(false);
					mTvHdmi.setText(getString(R.string.test_hmdi_plugout));
				}
			}
		}
	};

}
