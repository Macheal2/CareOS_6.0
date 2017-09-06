package com.android.deskclock.timer;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.AudioManager;
import android.media.AudioSystem; 
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerGlobal;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.net.Uri;

import com.android.deskclock.R;
import com.android.deskclock.TimerRingService;
import com.android.deskclock.Utils;

//import android.app.ViVoDialog;

public class MagcommTimerAlertActivity extends Activity {

    private static final String TAG = "TimerAlertActivity";
	public static MagcommTimerAlertActivity instance = null;
    private static Context mContext;
	private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        //String strStopTime = getIntent().getStringExtra("stop_time");
        instance=this;
        mContext=this;
        showAlertDialogAndRing();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.cancelTimesNotifications(this);
        Utils.cancelTimesUpNot(this);
    }

    @Override
    public void onPause() {  
    	if(instance!=null){
    		Utils.showTimesUpNot(this);
    	}
        super.onPause();
    }

    @Override
    public void onDestroy() {
    	Utils.cancelTimesUpNot(this);
        super.onDestroy();
    }
    
    private void playRingtone(){
	    Intent intent = new Intent();
	    intent.putExtra(Utils.RING_TYPE_INDEX, Utils.RING_TYPE_TIMERALER);
	    intent.setClass(this, TimerRingService.class);
	    mContext.startService(intent);
    }
    
    static void stopRingtone(){
	    Intent intent = new Intent();
	    intent.putExtra(Utils.RING_TYPE_INDEX, Utils.RING_TYPE_TIMERALER);
	    intent.setClass(mContext, TimerRingService.class);
	    mContext.stopService(intent);
    }
    
    private void showAlertDialogAndRing() {
    	playRingtone();
		if(dialog == null){
			dialog= new AlertDialog.Builder(this).setTitle(getString(R.string.timer_end_title))//AlertDialog
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
			        	@Override
			        	public void onClick(DialogInterface dialog, int which) {
			        		stopRingtone();
			        		instance=null;
			        		finish();
			        	}}).create();
						
			dialog.setCancelable(false);//blocking back key			
			Window win = dialog.getWindow();
	        WindowManager.LayoutParams winParams = win.getAttributes();        				   
	        win.setAttributes(winParams);			
			dialog.show();
		}	
    }

}