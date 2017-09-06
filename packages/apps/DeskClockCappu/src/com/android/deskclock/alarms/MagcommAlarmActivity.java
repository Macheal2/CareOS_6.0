/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.deskclock.alarms;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Button;
import com.android.deskclock.LogUtils;
import com.android.deskclock.R;
import com.android.deskclock.SettingsActivity;
import com.android.deskclock.Utils;
import com.android.deskclock.provider.AlarmInstance;
import com.android.deskclock.widget.fall.*;

import java.util.Calendar;

public class MagcommAlarmActivity extends Activity implements View.OnClickListener {

    /**
     * MagcommAlarmActivity listens for this broadcast intent, so that other applications can snooze the
     * alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    /**
     * MagcommAlarmActivity listens for this broadcast intent, so that other applications can dismiss
     * the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
     */
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    private static final String LOGTAG = MagcommAlarmActivity.class.getSimpleName();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtils.v(LOGTAG, "Received broadcast: %s", action);

            if (!mAlarmHandled) {
                switch (action) {
                    case ALARM_SNOOZE_ACTION:
                        snooze();
                        finish();
                        break;
                    case ALARM_DISMISS_ACTION:
                        dismiss();
                        finish();
                        break;
                    case AlarmService.ALARM_DONE_ACTION:
                        finish();
                        break;
                    default:
                        LogUtils.i(LOGTAG, "Unknown broadcast: %s", action);
                        break;
                }
            } else {
                LogUtils.v(LOGTAG, "Ignored broadcast: %s", action);
            }
        }
    };

    private AlarmInstance mAlarmInstance;
    private boolean mAlarmHandled;
    private String mVolumeBehavior;

    private boolean mReceiverRegistered;
    
	private Button mSnoozeView , mDismissView;
	//private FallView mFallView;
	private ViewGroup mRootView;
	private ImageView mAnimView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final long instanceId = AlarmInstance.getId(getIntent().getData());
        mAlarmInstance = AlarmInstance.getInstance(getContentResolver(), instanceId);
        if (mAlarmInstance == null) {
            // The alarm got deleted before the activity got created, so just finish()
            LogUtils.e(LOGTAG, "Error displaying alarm for intent: %s", getIntent());
            finish();
            return;
        } else if (mAlarmInstance.mAlarmState != AlarmInstance.FIRED_STATE) {
            LogUtils.i(LOGTAG, "Skip displaying alarm for instance: %s", mAlarmInstance);
            finish();
            return;
        }

        LogUtils.i(LOGTAG, "Displaying alarm for instance: %s", mAlarmInstance);

        // Get the volume/camera button behavior setting
        mVolumeBehavior = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                        SettingsActivity.DEFAULT_VOLUME_BEHAVIOR);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        ///M: Don't show the wallpaper when the alert arrive. @{
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        ///@}

        // In order to allow tablets to freely rotate and phones to stick
        // with "nosensor" (use default device orientation) we have to have
        // the manifest start with an orientation of unspecified" and only limit
        // to "nosensor" for phones. Otherwise we get behavior like in b/8728671
        // where tablets start off in their default orientation and then are
        // able to freely rotate.
        if (!getResources().getBoolean(R.bool.config_rotateAlarmAlert)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }

        setContentView(R.layout.magcomm_alarm_alert);

        
        Calendar cal = Calendar.getInstance();// 当前日期
        int hour = cal.get(Calendar.HOUR_OF_DAY);// 获取小时
        int minute = cal.get(Calendar.MINUTE);// 获取分钟
        int minuteOfDay = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数
        final int start = 18 * 60;// 起始时间 18:00的分钟数
        final int end = 6 * 60;// 结束时间 6:00的分钟数
        int id=R.drawable.magcomm_pond;
        if (minuteOfDay >= start || minuteOfDay <= end) {
        	id=R.drawable.magcomm_pond1;
        } else {
        	id=R.drawable.magcomm_pond;
        }
        // Setup GlowPadController
        mRootView= (ViewGroup) findViewById(R.id.alarm_alert);
        //mFallView=  new FallView(this,id);
        //mRootView.addView(mFallView);
        
        mAnimView=(ImageView)findViewById(R.id.alarm_anim);
        
        Animation mRotate = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        mRotate.setFillAfter(true);
        mRotate.setInterpolator(new BounceInterpolator());
        mRotate.setAnimationListener(new AnimationListener(){
        		@Override
				public void onAnimationStart(Animation animation) {
        			int[] location = new int[2];
        	        mAnimView.getLocationOnScreen(location);
        	        final float x=(float)location[0]+mAnimView.getWidth()/2;
			        final float y=(float)location[1]+mAnimView.getHeight()/2;
        			//mFallView.setRender(x,y);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					int[] location = new int[2];
			        mAnimView.getLocationOnScreen(location);
			        final float x=(float)location[0]+mAnimView.getWidth()/2;
			        final float y=(float)location[1]+mAnimView.getHeight()/2;
					//mFallView.setRender(x,y);
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					//mFallView.setRender(x,y);
				}
        	} );
        mAnimView.startAnimation(mRotate);
        	
        //mSnoozeView = (Button)findViewById(R.id.snooze);
        //mSnoozeView.setOnClickListener(mClickListener);
        mDismissView = (Button)findViewById(R.id.dismiss);
        if(mDismissView!=null){
        	mDismissView.setOnClickListener(this);
        }
        


        final TextView titleView = (TextView) findViewById(R.id.title);
        final TextClock digitalClock = (TextClock) findViewById(R.id.digital_clock);
        //final View pulseView = mContentView.findViewById(R.id.pulse);

        titleView.setText(mAlarmInstance.getLabelOrDefault(this));
        Utils.setTimeFormat(digitalClock,
                getResources().getDimensionPixelSize(R.dimen.main_ampm_font_size));


        final IntentFilter filter = new IntentFilter(AlarmService.ALARM_DONE_ACTION);
        filter.addAction(ALARM_SNOOZE_ACTION);
        filter.addAction(ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;
    }

    @Override
    public void onDestroy() {
        // Skip if register didn't happen to avoid IllegalArgumentException
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent keyEvent) {
        // Do this in dispatch to intercept a few of the system keys.
        LogUtils.v(LOGTAG, "dispatchKeyEvent: %s", keyEvent);

        switch (keyEvent.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm.
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (!mAlarmHandled && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    switch (mVolumeBehavior) {
                        case SettingsActivity.VOLUME_BEHAVIOR_SNOOZE:
                            snooze();
                            finish();
                            break;
                        case SettingsActivity.VOLUME_BEHAVIOR_DISMISS:
                            dismiss();
                            finish();
                            break;
                        default:
                            break;
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(keyEvent);
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
    }

    @Override
    public void onClick(View view) {
    	
    	   switch(view.getId()){
//       		case R.id.snooze:
//           		snooze();
//           		finish();
//       			break;
    	   		case R.id.dismiss:
    	   			dismiss();
    	   			finish();
    	   			break;
    	   		default:
           // Code should never reach here.                   
    	   }

    }

    private void snooze() {
        mAlarmHandled = true;
        LogUtils.v(LOGTAG, "Snoozed: %s", mAlarmInstance);
        AlarmStateManager.setSnoozeState(this, mAlarmInstance, false /* showToast */);
    }

    private void dismiss() {
        mAlarmHandled = true;
        LogUtils.v(LOGTAG, "Dismissed: %s", mAlarmInstance);
        AlarmStateManager.setDismissState(this, mAlarmInstance);
    }

    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:    	  	
          	  	snooze();
          	  	finish();
                break;
        }
        return true;
    }
}
