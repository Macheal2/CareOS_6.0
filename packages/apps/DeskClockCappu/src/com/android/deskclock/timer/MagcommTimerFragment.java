/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.deskclock.timer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.android.deskclock.provider.Alarm;
import com.android.deskclock.radialview.*;
import com.android.deskclock.radialview.RadialPickerLayout.OnValueSelectedListener;
import com.android.deskclock.CircleButtonsLayout;
import com.android.deskclock.DeskClock;
import com.android.deskclock.DeskClock.OnTapListener;
import com.android.deskclock.CircleTimerView;
import com.android.deskclock.DeskClockFragment;
import com.android.deskclock.LabelDialogFragment;
import com.android.deskclock.R;
import com.android.deskclock.TimerRingService;
import com.android.deskclock.timer.Timers;
import com.android.deskclock.Utils;
import com.android.deskclock.timer.CountingTimerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.android.deskclock.widget.sgv.SgvAnimationHelper.AnimationIn;
import com.android.deskclock.widget.sgv.SgvAnimationHelper.AnimationOut;
import com.android.deskclock.widget.sgv.StaggeredGridView;
import com.android.deskclock.widget.sgv.GridAdapter;

public class MagcommTimerFragment extends DeskClockFragment implements OnValueSelectedListener{

    private static final String TAG = "TimerFragment";
    public static final String TIMER_SETUP_VIEW = "deskclock.timers.timersetup";
    public static final int HOUR_INDEX = 0;
    public static final int MINUTE_INDEX = 1;
    public static final int SECOND_INDEX = 2;
    public static final int ENABLE_PICKER_INDEX=3;
    public static boolean mIsRunning;
    
    private static final int NOTIFICATION_ID = R.drawable.stat_notify_timer;
    private static final int REQUEST_CODE_RINGTONE = 1;
    private static final Uri NO_RINGTONE_URI = Uri.EMPTY;
    private static final String NO_RINGTONE = NO_RINGTONE_URI.toString();
    private static String ringtone;
    private static SharedPreferences mPrefs;
    private static boolean mIsClockView=true;
    private static boolean mIsState;
    private static Context mContext;
    
    private TextView mHourView;
    private TextView mMinuteView;
    private TextView mSecondView;
    private TextView mTimerRingtoneName;
    private Button mCancel,mStart,mStop;
    
    private View mTimerSetup;
    private View mTimerCount;
    private ImageButton mTimerRingSet;
    
    private CountTimer mCountTimer;
    private long mSecnum;
    private long mOriginalTime;
    private long mTimeMillisCount;

    private NotificationManager mNotificationManager;
    private PendingIntent mPendingIntent;
    private AlarmManager mAlarmManager;
    private AudioManager mAudioManager;    
    private CountingTimerView mTimeText;
    private CircleTimerView mCircleView;
//    private WheelView mHours;
//    private WheelView mMins;
//    private WheelView mSeconds;
    private RadialPickerLayout mTimePicker;
    private int mBlue;
    private int mBlack;
    private int mColor;
    private int mPageIndex;
    
    public MagcommTimerFragment() {
    }

    class CountTimer extends CountDownTimer {

        public CountTimer(long millisInFuture, long countDownInterval, Context context) {
            super(millisInFuture, countDownInterval);
            mContext = context;
            if(mIsClockView){
            	setTime(millisInFuture);
            }else{
              mTimeText.setTime(millisInFuture, true, true);
              mCircleView.setIntervalTime(mOriginalTime);
              mCircleView.abortIntervalAnimation();
            }
        }
      
        @Override
        public void onFinish() {
            mIsRunning = false;
        	mTimePicker.setTime(0,0,0);
        	setTimeReset(0);
            showTimeSetupPage();
	        Utils.clearTimerSharedPref(mPrefs);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mSecnum = millisUntilFinished;
            if (mSecnum < 0) {
                if (mStop != null) {
                	mStop.setEnabled(false);
                }
                if (mCancel != null) {
                	mCancel.setEnabled(false);
                	mCancel.setBackgroundResource(R.drawable.magcomm_ic_cancel_normal);
                }
                if (mStart != null) {
                	mStart.setEnabled(false);
                }
            }
            
            if(mIsClockView){
            	setTime(millisUntilFinished);
            }else{
	            mTimeText.setTime(millisUntilFinished, true, true);            
	            mCircleView.setPassedTime(mOriginalTime-millisUntilFinished, true);		       
            }
            
            if(mIsState){
	            mIsState=false;
	            showCountDownTimeView();
	        }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Cache instance data and consume in first call to setupPage()
    	super.onCreate(savedInstanceState); 
    	mContext = getActivity();
    	mPrefs =PreferenceManager.getDefaultSharedPreferences(getActivity());
		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);       
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.magcomm_timer_fragment, container, false);
        
        // Handle arguments from parent
        mBlue = getResources().getColor(R.color.blue);
        mBlack = getResources().getColor(R.color.magcomm_time_seconds_color);
        mColor = getResources().getColor(R.color.magcomm_time_text_color);
       
        mTimerSetup = v.findViewById(R.id.timer_setup);
        mTimePicker = (RadialPickerLayout) v.findViewById(R.id.time_picker);
        mHourView=(TextView) v.findViewById(R.id.hours);
        mMinuteView=(TextView) v.findViewById(R.id.minutes);
        mSecondView=(TextView) v.findViewById(R.id.seconds);        
        mTimePicker.setOnValueSelectedListener(this);
        mTimePicker.initialize(getActivity(),0,0,0);
        
        mTimePicker.invalidate();
        mPageIndex=Timers.PAGE_SETUP_TIMER;
     
        mTimerRingSet=(ImageButton)v.findViewById(R.id.timer_ring_set);
        final String ringtoneTitle;
        ringtone=getDefaultRingtone(mContext);
        if (NO_RINGTONE_URI.equals(Uri.parse(ringtone))) {
            ringtoneTitle = mContext.getResources().getString(R.string.silent_alarm_summary);        
        } else {    
            ringtoneTitle = getRingToneTitle(Uri.parse(ringtone));
        }
        //mTimerRingtoneName=(TextView)v.findViewById(R.id.timer_ring_name);
        //mTimerRingtoneName.setText(ringtoneTitle);
        mTimerRingSet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	launchRingTonePicker(Uri.parse(ringtone));
            	
            }
        });
   
        mTimerCount=v.findViewById(R.id.timer_count);
        mTimerCount.setVisibility(View.GONE);
        mTimeText = (CountingTimerView)v.findViewById(R.id.timer_time_text); 
        mCircleView = (CircleTimerView)v.findViewById(R.id.timer_time);
        mCircleView.setTimerMode(true);
        mCircleView.invalidate();
        
        mHourView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentItemShowing(HOUR_INDEX, false, false, true);
                mTimePicker.tryVibrate();
            }
        });
        
        mMinuteView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentItemShowing(MINUTE_INDEX, false, false, true);
                mTimePicker.tryVibrate();
            }
        });
        
        mSecondView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentItemShowing(SECOND_INDEX, false, false, true);
                mTimePicker.tryVibrate();
            }
        });
        
        mStart = (Button)v.findViewById(R.id.timer_start);
        mStop = (Button)v.findViewById(R.id.timer_stop);
        mCancel = (Button)v.findViewById(R.id.timer_reset);

        mCancel.setOnClickListener(new OnClickListener() {
		    @Override
            public void onClick(View v) {
                // New timer create if timer length is not zero
                // Create a new timer object to track the timer and
                // switch to the timers view.

                if (mSecnum >= 1 * 1000) {                 
                    if (mCountTimer != null) {
                        mCountTimer.cancel();
                    }
                    if (mPendingIntent != null) {
                        mAlarmManager.cancel(mPendingIntent);
                    }
                    mIsRunning = false; 
                    if(!mIsClockView){
                    	circleViewDone();
                    }
                	mTimePicker.setTime(0,0,0);
                	setTimeReset(0);
    		    	showTimeSetupPage();
    		    	mNotificationManager.cancel(NOTIFICATION_ID);
    		        Utils.clearTimerSharedPref(mPrefs);
                }
            }
		});
        mCancel.setEnabled(false);
        mCancel.setBackgroundResource(R.drawable.magcomm_ic_cancel_normal);
        
        mStop.setOnClickListener(new OnClickListener() {
		    @Override
            public void onClick(View v) {
  
                if (mIsRunning) {
                    if (mSecnum >= 1 * 1000) {
                        mIsRunning = false;
                        mStop.setText(R.string.timer_resume);
                        if (mCountTimer != null) {
                            mCountTimer.cancel();     
                        }
                        if (mPendingIntent != null) {
                            mAlarmManager.cancel(mPendingIntent);
                        }
                        if(!mIsClockView){
	                        circleViewStop();
	                        mCircleView.setPassedTime(mOriginalTime-mSecnum, true);
                        }
                        //stopRingtone();
                    }
                }else{
                	mIsRunning = true;
                	mStop.setText(R.string.timer_stop);
                	if(!mIsClockView){
                		circleViewPause();
                	}
                    startCountTime(mSecnum);
                    //playRingtone();
                }
            }
		});
        mStop.setVisibility(View.GONE);
		
        mStart.setOnClickListener(new OnClickListener() {
		    @Override
            public void onClick(View v) {
		    	mOriginalTime= mTimePicker.getTime()*1000;
		    	mSecnum = mOriginalTime;
		    	if (mSecnum > 0) {            
		    		if(!mIsClockView){
		    			circleViewStart();
		    		}
		    		showCountDownTimeView();
		        	startCountTime(mSecnum);
                }
		        //playRingtone();
            }
		});
			
		setTimeReset(0);      
        return v;
    }


    @Override
    public void onResume() {
        Intent newIntent = null;
        if (getActivity() instanceof DeskClock) {
            DeskClock activity = (DeskClock) getActivity();
            activity.registerPageChangedListener(this);
            newIntent = activity.getIntent();
        }
        
        super.onResume();
      
        long lastMarkedTime = mPrefs.getLong(Timers.TIMER_MARK, 0);
        long mNowTime =Utils.getTimeNow();
        mIsRunning = mPrefs.getBoolean(Timers.TIMER_RUN_STATE, false);
        mSecnum = mPrefs.getLong(Timers.TIMER_REMAINDER_MILLISECS, 0);
        mOriginalTime=mPrefs.getLong(Timers.TIMER_ORIGINAL_TIME,0);
        mPageIndex=mPrefs.getInt(Timers.TIMER_FRAGMENT_PAGE,Timers.PAGE_SETUP_TIMER);
        if(mIsRunning){
            mIsState=true;
            if (mCountTimer != null) {
	                mCountTimer.cancel();
	                mCountTimer = null;    
    	        }
	            mCountTimer = new CountTimer(mSecnum - (mNowTime-lastMarkedTime), 100, mContext);
	            mCountTimer.start();
	            //playRingtone();
	    }else{	        	
	        if(mPageIndex == Timers.PAGE_COUNTDOWN_TIMER) {
	            showCountDownTimeView();
	            if(!mIsClockView){
	            	mTimeText.setTime(mSecnum, true, true);
		            mCircleView.setIntervalTime(mOriginalTime);
		            mCircleView.setPassedTime(mOriginalTime-mSecnum, true);
	            }else{
	                setTime(mSecnum);
	            }
	            mStop.setText(R.string.timer_resume);
	        } else {  
	                showTimeSetupPage();
	        }
	    }

        if (newIntent != null) {
			processIntent(newIntent);
        }	
        
        mNotificationManager.cancel(NOTIFICATION_ID);        
    }
    
    
    public void processIntent(Intent intent) {
        // switch to timer setup view
        if (intent.getBooleanExtra(TIMER_SETUP_VIEW, false)) {
        	showTimeSetupPage();
        }
    }

    @Override
    public void onPause() {

        if (getActivity() instanceof DeskClock) {
            ((DeskClock)getActivity()).unregisterPageChangedListener(this);
        }
        super.onPause();
        
        if (mCountTimer != null) {
            mCountTimer.cancel();
            mCountTimer = null;
        }
//        if(mIsRunning){
//        	stopRingtone();  
//        }
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(Timers.TIMER_RUN_STATE, mIsRunning);
        editor.putInt(Timers.TIMER_FRAGMENT_PAGE, mPageIndex);
        editor.putLong(Timers.TIMER_REMAINDER_MILLISECS, mSecnum);
        editor.putLong(Timers.TIMER_ORIGINAL_TIME,mOriginalTime);
        editor.putLong(Timers.TIMER_MARK, Utils.getTimeNow());
        editor.apply();

        mTimerCount.setVisibility(View.GONE);
    }

    
    @Override
    public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
    	
        if (pickerIndex == HOUR_INDEX) {
            setHour(newValue); 
            mTimePicker.setItem(HOUR_INDEX,newValue);
            if (autoAdvance) {
                setCurrentItemShowing(MINUTE_INDEX, false, true, true);
            }
        } else if (pickerIndex == MINUTE_INDEX){
            setMinute(newValue);
            mTimePicker.setItem(MINUTE_INDEX,newValue);
            if (autoAdvance) {
                setCurrentItemShowing(SECOND_INDEX, false, true, true);
            }
        } else if (pickerIndex == SECOND_INDEX) {
        	setSecond(newValue);
        	mTimePicker.setItem(SECOND_INDEX,newValue);
        	if (autoAdvance) {
                setCurrentItemShowing(HOUR_INDEX, false, true, true);
            }
        }
    }
    
    
    private void showCountDownTimeView() {
    	mPageIndex=Timers.PAGE_COUNTDOWN_TIMER; 
    	mStart.setVisibility(View.GONE);
    	mCancel.setEnabled(true);
    	mCancel.setBackgroundResource(R.drawable.magcomm_ic_cancel_activated);
    	mStop.setVisibility(View.VISIBLE);
    	if(!mIsClockView){
	    	mTimerSetup.setVisibility(View.GONE);    	
	    	mTimerCount.setVisibility(View.VISIBLE);
    	}else{
    		setCurrentItemShowing(ENABLE_PICKER_INDEX, false, false, false);
    		//setCurrentItemShowing(HOUR_INDEX, false, true, true);
    		mTimePicker.setEnabled(false);
    		mHourView.setEnabled(false);
    		mMinuteView.setEnabled(false);
    		mSecondView.setEnabled(false);  		
    	}
    }

    private void showTimeSetupPage() {
    	mPageIndex=Timers.PAGE_SETUP_TIMER;
    	
    	//mTimePicker.setTime(0,0,0);
    	//setTimeReset(0);
    	//stopRingtone();
    	mStart.setVisibility(View.VISIBLE);
    	mCancel.setEnabled(false);
    	mCancel.setBackgroundResource(R.drawable.magcomm_ic_cancel_normal);
    	mStop.setVisibility(View.GONE);
    	mStop.setText(R.string.timer_stop);
    	if(!mIsClockView){
    		circleViewDone();
    		mTimerSetup.setVisibility(View.VISIBLE);   	
    		mTimerCount.setVisibility(View.GONE);
    	}else{
    		setCurrentItemShowing(HOUR_INDEX, false, true, true);
    		mTimePicker.setEnabled(true);
    		mHourView.setEnabled(true);
    		mMinuteView.setEnabled(true);
    		mSecondView.setEnabled(true);
    	}
    }
    
    private void circleViewStart() {
        mCircleView.startIntervalAnimation();
        mTimeText.setTimeStrTextColor(false, true);
        mTimeText.showTime(true); 
    }

    private void circleViewPause() {
        mCircleView.pauseIntervalAnimation();
        mTimeText.setTimeStrTextColor(false, true);
        mTimeText.showTime(true);   
    }

    private void circleViewStop() {
        mCircleView.stopIntervalAnimation();
        mTimeText.setTimeStrTextColor(false, true);
        mTimeText.showTime(true); 
    }
    
    private void circleViewDone() {
        mCircleView.stopIntervalAnimation();
        mCircleView.invalidate();
        mTimeText.setTimeStrTextColor(true, false);
    }
    
    private String millSecsToString(long millsecs) {
        long hours, minutes, seconds, totalSecs, hundreds;

        totalSecs = millsecs / 1000;
        hours = totalSecs / 60 / 60;
        minutes = totalSecs / 60 % 60;
        seconds = totalSecs % 60;
        hundreds = (millsecs - totalSecs * 1000) / 10;

        if (hundreds != 0) {
            seconds++;
            if (seconds == 60) {
                seconds = 0;
                minutes++;
                if (minutes == 60) {
                    minutes = 0;
                    hours++;
                }
            }
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startCountTime(long millsecs) {

        mIsRunning = true;
        mTimeMillisCount = millsecs;
        Intent intent = new Intent(mContext, MagcommTimerAlertActivity.class);
        intent.putExtra("stop_time", millSecsToString(millsecs));
        mPendingIntent = PendingIntent.getActivity(mContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                Utils.getTimeNow() + millsecs, mPendingIntent);
        mCountTimer = new CountTimer(millsecs, 100, mContext);
        mCountTimer.start();
 
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RINGTONE:
                    saveRingtoneUri(data);
                    break;
                default:                   
            }
        }
    }
    
    private void launchRingTonePicker(Uri ringtone) {        
        Uri oldRingtone = NO_RINGTONE_URI.equals(ringtone) ? null : ringtone;
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, oldRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        startActivityForResult(intent, REQUEST_CODE_RINGTONE);
    }
    
    private void saveRingtoneUri(Intent intent) {
        Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) {
            uri = NO_RINGTONE_URI;
        }    
        // Save the last selected ringtone as the default for new alarms
        if (!NO_RINGTONE_URI.equals(uri)) {     
            setDefaultRingtone(uri.toString());
            //mTimerRingtoneName.setText(getRingToneTitle(uri));          
        }else{
        	setDefaultRingtone(uri.toString());
            //mTimerRingtoneName.setText(R.string.silent_alarm_summary); 
        }
        ringtone=uri.toString();
    }
    
    /**
     * M: Set the internal used default Ringtones
     */
    public void setDefaultRingtone(String defaultRingtone) {
        
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Timers.TIMER_DEFAULT_RINGTONE, defaultRingtone);
        editor.apply();
    }

    /**
     * M: Get the internal used default Ringtones
     */
    public static String getDefaultRingtone(Context context){
    	Uri systemDefaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(context,
                RingtoneManager.TYPE_ALARM);
        if(systemDefaultRingtone==null){
        	systemDefaultRingtone = Uri.parse("content://settings/system/alarm_alert");
        }
        String defaultRingtone = mPrefs.getString(Timers.TIMER_DEFAULT_RINGTONE, "123");
        if(defaultRingtone.equals("123")){
        	defaultRingtone=systemDefaultRingtone.toString();
        	SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(Timers.TIMER_DEFAULT_RINGTONE, defaultRingtone);
            editor.apply();
        }       
        return defaultRingtone;
    }
    
    private String getRingToneTitle(Uri uri) {
            Ringtone ringTone = RingtoneManager.getRingtone(mContext, uri);
            String title = ringTone.getTitle(mContext);
            return title;
    }
    
    private void setTime(long time) {
    	
        long hundreds, seconds, minutes, hours;
        seconds = time / 1000;
        hundreds = (time - seconds * 1000) / 10;
        minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        hours = minutes / 60;
        minutes = minutes - hours * 60;
        	
    	setHour((int)hours);
    	setMinute((int)minutes);
    	setSecond((int)seconds);
    	mTimePicker.setTime((int)hours,(int)minutes,(int)seconds);
    }
    
    private void setTimeReset(int time) {
    	setHour(time);
    	setMinute(time);
    	setSecond(time);	
    }
    
    private void setHour(int value) { 
        CharSequence text = String.format("%02d", value);
        mHourView.setText(text);
    }

    private void setMinute(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format("%02d", value);
        mMinuteView.setText(text);
    }
    
    private void setSecond(int value) {
        if (value == 60) {
            value = 0;
        }
        CharSequence text = String.format("%02d", value);      
        mSecondView.setText(text);    
    }

    // Show either Hours or Minutes.
    private void setCurrentItemShowing(int index, boolean animateCircle, boolean delayLabelAnimate,
            boolean announce) {
    	
        mTimePicker.setCurrentItemShowing(index, animateCircle);

        TextView labelToAnimate;
        if (index == HOUR_INDEX) {
            int hours = mTimePicker.getHours();     
            labelToAnimate = mHourView;           
        } else if(index == MINUTE_INDEX) {
            int minutes = mTimePicker.getMinutes();
            labelToAnimate = mMinuteView;         
        }else{
        	int seconds = mTimePicker.getSeconds();
            labelToAnimate = mSecondView;
        }

        int hourColor = (index == HOUR_INDEX)? mBlue : mColor;
        int minuteColor = (index == MINUTE_INDEX)? mBlue : mColor;
        int secondColor = (index == SECOND_INDEX)? mBlue : mBlack;
        mHourView.setTextColor(hourColor);
        mMinuteView.setTextColor(minuteColor);
        mSecondView.setTextColor(secondColor);
    }  
    
//    public static void playRingtone(){
//    	Intent intent = new Intent(mContext, TimerRingService.class);
//	    intent.putExtra(Utils.RING_TYPE_INDEX, Utils.RING_TYPE_TIMERRUN);	    
//	    mContext.startService(intent);
//    }
//    
//    private void stopRingtone(){
//    	Intent intent = new Intent(mContext, TimerRingService.class);
//    	//intent.putExtra(Utils.RING_TYPE_INDEX, Utils.RING_TYPE_TIMERRUN);
//    	mContext.stopService(intent);
//    }
    
    private void setMute(boolean mute){  	
    	mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION,mute);
    }
}
