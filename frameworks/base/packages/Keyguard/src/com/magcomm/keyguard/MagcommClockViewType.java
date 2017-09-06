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

package com.magcomm.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.android.keyguard.R;

public class MagcommClockViewType extends RelativeLayout {
    private static Typeface mClockTypeface = null;
    private final static String M12 = "h:mm";
    private final static String M24 = "kk:mm";

    private Calendar mCalendar;
    private String mFormat;
    //private TextView mTimeView;

    private TextView mTimeHour;//time_hour
    private TextView mTimeMinute;//time_minute
    private TextView mDateMD;//date_md
    private TextView mDateWeek;//date_week
    private TextView mChineseLunar;//lunar
    private String[] mDateMonth, mDateDay;
    
    private ContentObserver mFormatChangeObserver;
    private int mAttached = 0;
    private final Handler mHandler = new Handler();
    private BroadcastReceiver mIntentReceiver;

    
    private static class TimeChangedReceiver extends BroadcastReceiver {
        private WeakReference<MagcommClockViewType> mClock;
        private Context mContext;

        public TimeChangedReceiver(MagcommClockViewType clock) {
            mClock = new WeakReference<MagcommClockViewType>(clock);
            mContext = clock.getContext();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Post a runnable to avoid blocking the broadcast.
            final boolean timezoneChanged =
                    intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
            final MagcommClockViewType clock = mClock.get();
            if (clock != null) {
                clock.mHandler.post(new Runnable() {
                    public void run() {
                        if (timezoneChanged) {
                            clock.mCalendar = Calendar.getInstance();
                        }
                        clock.updateTime();
                    }
                });
            } else {
                try {
                    mContext.unregisterReceiver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    };

//    static class AmPm {
//        private TextView mAmPmTextView;
//        private String mAmString, mPmString;
//
//        AmPm(View parent, Typeface tf) {
//            // No longer used, uncomment if we decide to use AM/PM indicator again
//            mAmPmTextView = (TextView) parent.findViewById(R.id.magcomm_am_pm);
//
//            if (mAmPmTextView != null && tf != null) {
//                mAmPmTextView.setTypeface(tf);
//            }
//
//            String[] ampm = new DateFormatSymbols().getAmPmStrings();
//            mAmString = ampm[0];
//            mPmString = ampm[1];
//        }
//
//        void setShowAmPm(boolean show) {
//            if (mAmPmTextView != null) {
//                mAmPmTextView.setVisibility(show ? View.VISIBLE : View.GONE);
//            }
//        }
//
//        void setIsMorning(boolean isMorning) {
//            if (mAmPmTextView != null) {
//                mAmPmTextView.setText(isMorning ? mAmString : mPmString);
//            }
//        }
//    }

    private static class FormatChangeObserver extends ContentObserver {
        private WeakReference<MagcommClockViewType> mClock;
        private Context mContext;
        public FormatChangeObserver(MagcommClockViewType clock) {
            super(new Handler());
            mClock = new WeakReference<MagcommClockViewType>(clock);
            mContext = clock.getContext();
        }
        @Override
        public void onChange(boolean selfChange) {
            MagcommClockViewType digitalClock = mClock.get();
            if (digitalClock != null) {
                digitalClock.setDateFormat();
                digitalClock.updateTime();
            } else {
                try {
                    mContext.getContentResolver().unregisterContentObserver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    }

    public MagcommClockViewType(Context context) {
        this(context, null);
    }

    public MagcommClockViewType(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mTimeHour = (TextView)findViewById(R.id.time_hour);
        mTimeMinute = (TextView)findViewById(R.id.time_minute);
        mDateMD = (TextView)findViewById(R.id.date_md);
        mDateWeek = (TextView)findViewById(R.id.date_week);
        mChineseLunar = (TextView)findViewById(R.id.lunar);
        
        mDateMonth = getResources().getStringArray(R.array.magcomm_lunarcal_month);
        mDateDay = getResources().getStringArray(R.array.magcomm_lunarcal_day);
        
        Typeface tf = getClockTypeface(MagcommLockscreen.CAPPU_REGULAR_FONT_STYLE);
        Log.e("hmq","CAPPU_REGULAR_FONT_STYLE=null ? "+(tf == null));
        if (mTimeHour != null && tf != null){
                mTimeHour.setTypeface(tf);
        }
        if (mTimeMinute != null && tf != null){
            mTimeMinute.setTypeface(tf);
        }
        
        tf = getClockTypeface(MagcommLockscreen.CAPPU_BOLD_FONT_STYLE);
        Log.e("hmq", "CAPPU_BOLD_FONT_STYLE=null ? " + (tf == null));
        if (mDateMD != null && tf != null) {
            mDateMD.setTypeface(tf);
        }
        if (mDateWeek != null && tf != null) {
            mDateWeek.setTypeface(tf);
        }
        if (mChineseLunar != null && tf != null) {
            mChineseLunar.setTypeface(tf);
        }
        
        mCalendar = Calendar.getInstance();
        setDateFormat();
        final Animation animation = new AlphaAnimation(1f, 0f);  
        animation.setDuration(1000); 
        animation.setInterpolator(new LinearInterpolator());  
        animation.setRepeatCount(Animation.INFINITE); 
        animation.setRepeatMode(Animation.REVERSE); 
        animation.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttached++;

        if (mIntentReceiver == null) {
            mIntentReceiver = new TimeChangedReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            mContext.registerReceiverAsUser(mIntentReceiver, UserHandle.OWNER, filter, null, null );
        }

        if (mFormatChangeObserver == null) {
            mFormatChangeObserver = new FormatChangeObserver(this);
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        }

        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached--;

        if (mIntentReceiver != null) {
            mContext.unregisterReceiver(mIntentReceiver);
        }
        if (mFormatChangeObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(
                    mFormatChangeObserver);
        }

        mFormatChangeObserver = null;
        mIntentReceiver = null;
    }

    void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    public void updateTime() {
        
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        CharSequence newTime = DateFormat.format(mFormat, mCalendar);

        String[] getTime = newTime.toString().split(":");

        int temp_hour = Integer.parseInt(getTime[0]);
        int temp_minute = Integer.parseInt(getTime[1]);
        

        
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
        String md = formatter.format(mCalendar.getTime());
        String [] array_week = getResources().getStringArray(R.array.array_week);
        String week = array_week[Calendar.getInstance().get(mCalendar.DAY_OF_WEEK) - 1];
/* Cappu:lau on: Wed, 26 Jul 2017 15:08:13 +0800
 * TODO: 界面农历显示错误 
 */
        //int[] lunar =  LunarCalendar.solarToLunar(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int[] lunar = LunarCalendar.solarToLunar(year, month, day);
// End of Cappu:lau
        String lunarString = getResources().getString(R.string.magcomm_lunar_unlock) + mDateMonth[lunar[1] -1] + mDateDay[lunar[2] -1];
        
        mTimeHour.setText(getTime[0]);
        mTimeMinute.setText(getTime[1]);
        mDateMD.setText(md);
        mDateWeek.setText(week);
        if(isZh()){
            mChineseLunar.setText(lunarString);
        }else{
            mChineseLunar.setText("");
        }
    }
    
    private String getCurrentDayOfWeek() {
        Resources mResources = mContext.getResources();
        String [] weeks = mResources.getStringArray(R.array.array_week);
        return weeks[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }
    
    private void setDateFormat() {
        mFormat = android.text.format.DateFormat.is24HourFormat(getContext()) ? M24 : M12;
//        mAmPm.setShowAmPm(mFormat.equals(M12));
    }

    private Typeface getClockTypeface() {
        if (mClockTypeface == null) {
            mClockTypeface = Typeface.createFromFile(MagcommLockscreen.ANDROID_CLOCK_FONT_FILE);
        }
        return mClockTypeface;
    }
    
    private Typeface getClockTypeface(String file) {
        return Typeface.createFromFile(file);
    }
    
    private boolean isZh() {
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
}
