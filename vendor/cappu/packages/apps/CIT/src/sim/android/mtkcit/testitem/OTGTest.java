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
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class OTGTest extends TestBase {
	static final String TAG = "OTGTest";

	private  TextView             mTvOtg            = null;
	private  Button               mBtnSuccess       = null;
	private  Button               mBtnFail          = null;
	private  final int            EVENT_CHECK_OTG   = 1001;
	private  final int            COLOR_RED         = 0xffff0000;
	private  final int            COLOR_GREEN       = 0xff00ff00;
	private  MyTimerTask          mTask             = null;
	private  Timer                mTimer            = null;
	@Override
	public void onCreate(Bundle icycle) 
	{
		super.onCreate(icycle);

		setContentView(R.layout.test_otg);
		mBtnFail = (Button) findViewById(R.id.btn_fail);
		mBtnFail.setOnClickListener(this);
		mBtnSuccess = (Button) findViewById(R.id.btn_success);
		mBtnSuccess.setOnClickListener(this);
		mBtnSuccess.setEnabled(false);
		mTvOtg = (TextView) findViewById(R.id.otg_info);
		mTvOtg.setTextColor(COLOR_RED);
		mTask = new MyTimerTask();
		mTimer = new Timer(true);
		initButton();
		checkOtg();
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		mTimer.schedule(mTask,2000, 2000);
	}

	@Override
	protected void onPause() 
	{
		mTimer.cancel();
		super.onPause();
	}
	private void checkOtg()
	{
		synchronized(this)
		{
			File file = new File("/mnt/usbotg/otgtest.txt");

			if(!file.exists())
			{
				try
				{
					file.createNewFile(); 
				}
				catch(Exception ex)
				{
				}
			}

			if (file.exists())
			{
				mBtnSuccess.setEnabled(true);
				mTvOtg.setText(R.string.test_otg_plugin);
				mTvOtg.setTextColor(COLOR_GREEN);
			}
			else
			{
				mBtnSuccess.setEnabled(false);
				mTvOtg.setText(R.string.test_otg_plugout);
				mTvOtg.setTextColor(COLOR_RED);		
			}
		}
	}
	private Handler mHandler = new Handler()
	{
    	public void handleMessage(Message message)
		{
        	if( message.what == EVENT_CHECK_OTG )
		  	{
		  		checkOtg();
          	}
        }
	};
	class MyTimerTask extends TimerTask
	{
	  @Override
	  public void run() 
	  {
		   Message msg = mHandler.obtainMessage(EVENT_CHECK_OTG);
		   msg.sendToTarget();
	  }
	}
}
