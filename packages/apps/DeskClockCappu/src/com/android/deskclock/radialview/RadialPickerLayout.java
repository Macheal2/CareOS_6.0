/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.deskclock.radialview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import com.android.deskclock.AnalogClock;

import java.util.HashMap;

import com.android.deskclock.R;


public class RadialPickerLayout extends FrameLayout implements OnTouchListener ,AudioManager.OnAudioFocusChangeListener{
    private static final String TAG = "RadialPickerLayout";

    private final int TOUCH_SLOP;
    private final int TAP_TIMEOUT;

    private static final int VISIBLE_DEGREES_STEP_SIZE = 30;
    private static final int HOUR_VALUE_TO_DEGREES_STEP_SIZE = 15;
    private static final int MINUTE_VALUE_TO_DEGREES_STEP_SIZE = 6;
    private static final int HOUR_INDEX=0;
    private static final int MINUTE_INDEX =1;
    private static final int SECOND_INDEX =2;   
    private static final int ENABLE_PICKER_INDEX=3 ;

    
    private Vibrator mVibrator;
    private long mLastVibrate;
    private int mLastValueSelected;

    private OnValueSelectedListener mListener;
    private boolean mTimeInitialized;
    private int mCurrentHours;
    private int mCurrentMinutes;
    private int mCurrentSeconds;
    private int mCurrentItemShowing;

    private ClockView mClockView;
    private RadialTextsView mHourRadialTextsView;
    private RadialTextsView mMinuteRadialTextsView;
    private RadialTextsView mSecondRadialTextsView;
    private RadialSelectorView mHourRadialSelectorView;
    private RadialSelectorView mMinuteRadialSelectorView;
    private RadialSelectorView mSecondRadialSelectorView;
    private View mGrayBox;

    private int[] mSnapPrefer30sMap;
    private boolean mInputEnabled;
    private boolean mDoingMove;
    private boolean mDoingTouch;
    private int mDownDegrees;
    private float mDownX;
    private float mDownY;
    private AccessibilityManager mAccessibilityManager;

    private AnimatorSet mTransition;
    private Handler mHandler = new Handler();
    private Context mContext;
    private MediaPlayer mediaPlayer;
    
    public interface OnValueSelectedListener {
        void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance);
    }

    public RadialPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(this);
        ViewConfiguration vc = ViewConfiguration.get(context);
        TOUCH_SLOP = vc.getScaledTouchSlop();
        TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
        mDoingMove = false;

        mClockView = new ClockView(context);
        addView(mClockView);

        mHourRadialTextsView = new RadialTextsView(context);
        addView(mHourRadialTextsView);
        mMinuteRadialTextsView = new RadialTextsView(context);
        addView(mMinuteRadialTextsView);       
        mSecondRadialTextsView = new RadialTextsView(context);
        addView(mSecondRadialTextsView);

        mHourRadialSelectorView = new RadialSelectorView(context);
        addView(mHourRadialSelectorView);
        mMinuteRadialSelectorView = new RadialSelectorView(context);
        addView(mMinuteRadialSelectorView);
        mSecondRadialSelectorView = new RadialSelectorView(context);
        addView(mSecondRadialSelectorView);

        // Prepare mapping to snap touchable degrees to selectable degrees.
        preparePrefer30sMap();

        mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        mLastVibrate = 0;
        mLastValueSelected = -1;

        mInputEnabled = true;
        mGrayBox = new View(context);
        mGrayBox.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mGrayBox.setBackgroundColor(getResources().getColor(R.color.transparent_black));
        mGrayBox.setVisibility(View.INVISIBLE);
        addView(mGrayBox);

        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        mTimeInitialized = false;
    }

    /**
     * Measure the view to end up as a square, based on the minimum of the height and width.
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int minDimension = Math.min(measuredWidth, measuredHeight);

        super.onMeasure(MeasureSpec.makeMeasureSpec(minDimension, widthMode),
                MeasureSpec.makeMeasureSpec(minDimension, heightMode));
    }

    public void setOnValueSelectedListener(OnValueSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Initialize the Layout with starting values.
     * @param context
     * @param initialHoursOfDay
     * @param initialMinutes
     */
    public void initialize(Context context, int initialHoursOfDay, int initialMinutes,int initialSeconds) {
        if (mTimeInitialized) {
            Log.e(TAG, "Time has already been initialized.");
            return;
        }


        // Initialize the circle and AM/PM circles if applicable.
//        mCircleView.initialize(context);
//        mCircleView.invalidate();
        
        mContext=context;
        // Initialize the hours and minutes numbers.
        Resources res = context.getResources();
        int[] hours = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22};
        int[] minutes = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
        int[] senconds = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
        String[] hoursTexts = new String[12];
        String[] minutesTexts = new String[12];
        String[] sencondsTexts = new String[12];
        for (int i = 0; i < 12; i++) {
            hoursTexts[i] = String.format("%02d", hours[i]) ;
            minutesTexts[i] = String.format("%02d", minutes[i]);
            sencondsTexts[i] = String.format("%02d", senconds[i]);
        }
        mHourRadialTextsView.initialize(res,hoursTexts,true);
        mHourRadialTextsView.invalidate();
        mMinuteRadialTextsView.initialize(res, minutesTexts,false);
        mMinuteRadialTextsView.invalidate();
        mSecondRadialTextsView.initialize(res, sencondsTexts,false);
        mSecondRadialTextsView.invalidate();


        // Initialize the currently-selected hour and minute.
        setValueForItem(HOUR_INDEX, initialHoursOfDay);
        setValueForItem(MINUTE_INDEX, initialMinutes);
        setValueForItem(SECOND_INDEX, initialSeconds);
        int hourDegrees = initialHoursOfDay * HOUR_VALUE_TO_DEGREES_STEP_SIZE;
        mHourRadialSelectorView.initialize(context,true,hourDegrees);    
        int minuteDegrees = initialMinutes * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
        mMinuteRadialSelectorView.initialize(context,false,minuteDegrees);
        int secondDegrees = initialSeconds * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
        mSecondRadialSelectorView.initialize(context,false,secondDegrees);

        mTimeInitialized = true;
    }

    public void setTime(int hours, int minutes,int seconds) {
        setItem(HOUR_INDEX, hours);
        setItem(MINUTE_INDEX, minutes);
        setItem(SECOND_INDEX, seconds);
    }

    /**
     * Set either the hour or the minute. Will set the internal value, and set the selection.
     */
    public void setItem(int index, int value) {
        if (index == HOUR_INDEX) {
            setValueForItem(HOUR_INDEX, value);
            int hourDegrees = value * HOUR_VALUE_TO_DEGREES_STEP_SIZE;
            mHourRadialSelectorView.setSelection(hourDegrees,false);
            mHourRadialSelectorView.invalidate();
        } else if (index == MINUTE_INDEX) {
            setValueForItem(MINUTE_INDEX, value);
            int minuteDegrees = value * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
            mMinuteRadialSelectorView.setSelection(minuteDegrees,false);
            mMinuteRadialSelectorView.invalidate();
        }else if (index == SECOND_INDEX) {
            setValueForItem(SECOND_INDEX, value);
            int secondDegrees = value * MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
            mSecondRadialSelectorView.setSelection(secondDegrees,false);
            mSecondRadialSelectorView.invalidate();	
        }
    }



    public int getHours() {
        return mCurrentHours;
    }

    public int getMinutes() {
        return mCurrentMinutes;
    }
    
    public int getSeconds() {
        return mCurrentSeconds;
    }
    
    public int getTime() {
        return mCurrentHours*3600+mCurrentMinutes*60+mCurrentSeconds;
    }

    /**
     * If the hours are showing, return the current hour. If the minutes are showing, return the
     * current minute.
     */
    private int getCurrentlyShowingValue() {
        int currentIndex = getCurrentItemShowing();
        if (currentIndex == HOUR_INDEX) {
            return mCurrentHours;
        } else if (currentIndex == MINUTE_INDEX) {
            return mCurrentMinutes;
        }else if (currentIndex == SECOND_INDEX) {
            return mCurrentSeconds;
        } else {
            return -1;
        }
    }


    /**
     * Set the internal value for the hour, minute, or AM/PM.
     */
    private void setValueForItem(int index, int value) {
        if (index == HOUR_INDEX) {
        	mClockView.setHours(value);
        	mClockView.invalidate();
            mCurrentHours = value;
        } else if (index == MINUTE_INDEX){
        	mClockView.setMinutes(value);
        	mClockView.invalidate();
            mCurrentMinutes = value;
        } else if (index == SECOND_INDEX){
        	mClockView.setSeconds(value);
        	mClockView.invalidate();
            mCurrentSeconds = value;
        }
    }



    /**
     * Split up the 360 degrees of the circle among the 60 selectable values. Assigns a larger
     * selectable area to each of the 12 visible values, such that the ratio of space apportioned
     * to a visible value : space apportioned to a non-visible value will be 14 : 4.
     * E.g. the output of 30 degrees should have a higher range of input associated with it than
     * the output of 24 degrees, because 30 degrees corresponds to a visible number on the clock
     * circle (5 on the minutes, 1 or 13 on the hours).
     */
    private void preparePrefer30sMap() {
        // We'll split up the visible output and the non-visible output such that each visible
        // output will correspond to a range of 14 associated input degrees, and each non-visible
        // output will correspond to a range of 4 associate input degrees, so visible numbers
        // are more than 3 times easier to get than non-visible numbers:
        // {354-359,0-7}:0, {8-11}:6, {12-15}:12, {16-19}:18, {20-23}:24, {24-37}:30, etc.
        //
        // If an output of 30 degrees should correspond to a range of 14 associated degrees, then
        // we'll need any input between 24 - 37 to snap to 30. Working out from there, 20-23 should
        // snap to 24, while 38-41 should snap to 36. This is somewhat counter-intuitive, that you
        // can be touching 36 degrees but have the selection snapped to 30 degrees; however, this
        // inconsistency isn't noticeable at such fine-grained degrees, and it affords us the
        // ability to aggressively prefer the visible values by a factor of more than 3:1, which
        // greatly contributes to the selectability of these values.

        // Our input will be 0 through 360.
        mSnapPrefer30sMap = new int[361];

        // The first output is 0, and each following output will increment by 6 {0, 6, 12, ...}.
        int snappedOutputDegrees = 0;
        // Count of how many inputs we've designated to the specified output.
        int count = 1;
        // How many input we expect for a specified output. This will be 14 for output divisible
        // by 30, and 4 for the remaining output. We'll special case the outputs of 0 and 360, so
        // the caller can decide which they need.
        int expectedCount = 8;
        // Iterate through the input.
        for (int degrees = 0; degrees < 361; degrees++) {
            // Save the input-output mapping.
            mSnapPrefer30sMap[degrees] = snappedOutputDegrees;
            // If this is the last input for the specified output, calculate the next output and
            // the next expected count.
            if (count == expectedCount) {
                snappedOutputDegrees += 6;
                if (snappedOutputDegrees == 360) {
                    expectedCount = 7;
                } else if (snappedOutputDegrees % 30 == 0) {
                    expectedCount = 14;
                } else {
                    expectedCount = 4;
                }
                count = 1;
            } else {
                count++;
            }
        }
    }

    /**
     * Returns mapping of any input degrees (0 to 360) to one of 60 selectable output degrees,
     * where the degrees corresponding to visible numbers (i.e. those divisible by 30) will be
     * weighted heavier than the degrees corresponding to non-visible numbers.
     * See {@link #preparePrefer30sMap()} documentation for the rationale and generation of the
     * mapping.
     */
    private int snapPrefer30s(int degrees) {
        if (mSnapPrefer30sMap == null) {
            return -1;
        }
        return mSnapPrefer30sMap[degrees];
    }

    /**
     * Returns mapping of any input degrees (0 to 360) to one of 12 visible output degrees (all
     * multiples of 30), where the input will be "snapped" to the closest visible degrees.
     * @param degrees The input degrees
     * @param forceAboveOrBelow The output may be forced to either the higher or lower step, or may
     * be allowed to snap to whichever is closer. Use 1 to force strictly higher, -1 to force
     * strictly lower, and 0 to snap to the closer one.
     * @return output degrees, will be a multiple of 30
     */
    private int snapOnly30s(int degrees, int forceHigherOrLower) {
        int stepSize = HOUR_VALUE_TO_DEGREES_STEP_SIZE;
        int floor = (degrees / stepSize) * stepSize;
        int ceiling = floor + stepSize;
        if (forceHigherOrLower == 1) {
            degrees = ceiling;
        } else if (forceHigherOrLower == -1) {
            if (degrees == floor) {
                floor -= stepSize;
            }
            degrees = floor;
        } else {
            if ((degrees - floor) < (ceiling - degrees)) {
                degrees = floor;
            } else {
                degrees = ceiling;
            }
        }
        return degrees;
    }

    /**
     * For the currently showing view (either hours or minutes), re-calculate the position for the
     * selector, and redraw it at that position. The input degrees will be snapped to a selectable
     * value.
     * @param degrees Degrees which should be selected.
     * @param isInnerCircle Whether the selection should be in the inner circle; will be ignored
     * if there is no inner circle.
     * @param forceToVisibleValue Even if the currently-showing circle allows for fine-grained
     * selection (i.e. minutes), force the selection to one of the visibly-showing values.
     * @param forceDrawDot The dot in the circle will generally only be shown when the selection
     * is on non-visible values, but use this to force the dot to be shown.
     * @return The value that was selected, i.e. 0-23 for hours, 0-59 for minutes.
     */
    private int reselectSelector(int degrees,boolean forceDrawDot) {
        if (degrees == -1) {
            return -1;
        }
        
        int currentShowing = getCurrentItemShowing();
        int stepSize;
        if(currentShowing == HOUR_INDEX){
        	degrees = snapOnly30s(degrees,0);
        }else{
        	degrees = snapPrefer30s(degrees);
        }

        RadialSelectorView radialSelectorView;
        if (currentShowing == HOUR_INDEX) {
            radialSelectorView = mHourRadialSelectorView;
            stepSize = HOUR_VALUE_TO_DEGREES_STEP_SIZE;
        } else if(currentShowing==MINUTE_INDEX) {
            radialSelectorView = mMinuteRadialSelectorView;
            stepSize = MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
        }else{
        	radialSelectorView = mSecondRadialSelectorView;
            stepSize = MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
        }
        radialSelectorView.setSelection(degrees,forceDrawDot);
        radialSelectorView.invalidate();

        if (degrees == 360) {
            degrees = 0;
        }

        int value = degrees / stepSize;

        if (currentShowing == HOUR_INDEX) {
        	mClockView.setHours(value);
        	mClockView.invalidate();
        } else if(currentShowing==MINUTE_INDEX) {
        	mClockView.setMinutes(value);
        	mClockView.invalidate();
       }else{
            mClockView.setSeconds(value);
            mClockView.invalidate();
       }
        
       return value;
    }

    /**
     * Calculate the degrees within the circle that corresponds to the specified coordinates, if
     * the coordinates are within the range that will trigger a selection.
     * @param pointX The x coordinate.
     * @param pointY The y coordinate.
     * @param forceLegal Force the selection to be legal, regardless of how far the coordinates are
     * from the actual numbers.
     * @param isInnerCircle If the selection may be in the inner circle, pass in a size-1 boolean
     * array here, inside which the value will be true if the selection is in the inner circle,
     * and false if in the outer circle.
     * @return Degrees from 0 to 360, if the selection was within the legal range. -1 if not.
     */
    private int getDegreesFromCoords(float pointX, float pointY, boolean forceLegal) {
        int currentItem = getCurrentItemShowing();
        if (currentItem == HOUR_INDEX) {
            return mHourRadialSelectorView.getDegreesFromCoords(
                    pointX, pointY, forceLegal);
        } else if (currentItem == MINUTE_INDEX) {
            return mMinuteRadialSelectorView.getDegreesFromCoords(
                    pointX, pointY, forceLegal);
        }else if (currentItem == SECOND_INDEX) {
            return mSecondRadialSelectorView.getDegreesFromCoords(
                    pointX, pointY, forceLegal);
        }else {
            return -1;
        }
    }

    /**
     * Get the item (hours or minutes) that is currently showing.
     */
    public int getCurrentItemShowing() {
        if (mCurrentItemShowing != HOUR_INDEX && mCurrentItemShowing != MINUTE_INDEX
        		&& mCurrentItemShowing != SECOND_INDEX) {
            Log.e(TAG, "Current item showing was unfortunately set to "+mCurrentItemShowing);
            return -1;
        }
        return mCurrentItemShowing;
    }

    /**
     * Set either minutes or hours as showing.
     * @param animate True to animate the transition, false to show with no animation.
     */
    public void setCurrentItemShowing(int index, boolean animate) {
        if (index != HOUR_INDEX && index != MINUTE_INDEX && index != SECOND_INDEX&&index != ENABLE_PICKER_INDEX) {
            Log.e(TAG, "TimePicker does not support view at index "+index);
            return;
        }

        int lastIndex = getCurrentItemShowing();
        mCurrentItemShowing = index;

        if (animate && (index != lastIndex)) {
            ObjectAnimator[] anims = new ObjectAnimator[4];
            if (index == MINUTE_INDEX) {
            	if(lastIndex == HOUR_INDEX){
                    anims[0] = mHourRadialTextsView.getDisappearAnimator();
                    anims[1] = mHourRadialSelectorView.getDisappearAnimator();
            	}else{
                    anims[0] = mSecondRadialTextsView.getDisappearAnimator();
                    anims[1] = mSecondRadialSelectorView.getDisappearAnimator();
            	}

                anims[2] = mMinuteRadialTextsView.getReappearAnimator();
                anims[3] = mMinuteRadialSelectorView.getReappearAnimator();

            } else if (index == HOUR_INDEX){
                anims[0] = mHourRadialTextsView.getReappearAnimator();
                anims[1] = mHourRadialSelectorView.getReappearAnimator();
                if(lastIndex == MINUTE_INDEX){
                	anims[2] = mMinuteRadialTextsView.getDisappearAnimator();
                    anims[3] = mMinuteRadialSelectorView.getDisappearAnimator();
                }else{                  
                    anims[2] = mSecondRadialTextsView.getDisappearAnimator();
                    anims[3] = mSecondRadialSelectorView.getDisappearAnimator();
                }
            }else if(index==SECOND_INDEX){
            	if(lastIndex == HOUR_INDEX){
                    anims[0] = mHourRadialTextsView.getDisappearAnimator();
                    anims[1] = mHourRadialSelectorView.getDisappearAnimator();
            	}else{
                    anims[0] = mMinuteRadialTextsView.getDisappearAnimator();
                    anims[1] = mMinuteRadialSelectorView.getDisappearAnimator();  
            	}
                anims[2] = mSecondRadialTextsView.getReappearAnimator();
                anims[3] = mSecondRadialSelectorView.getReappearAnimator();         
            }

            if (mTransition != null && mTransition.isRunning()) {
                mTransition.end();
            }
            mTransition = new AnimatorSet();
            mTransition.playTogether(anims);
            mTransition.start();
        } else {
            int hourAlpha = (index == HOUR_INDEX) ? 255 : 0;
            int minuteAlpha = (index == MINUTE_INDEX) ? 255 : 0;
            int secondAlpha =(index == SECOND_INDEX) ? 255 : 0;
            mHourRadialTextsView.setAlpha(hourAlpha);
            mHourRadialSelectorView.setAlpha(hourAlpha);
            mMinuteRadialTextsView.setAlpha(minuteAlpha);
            mMinuteRadialSelectorView.setAlpha(minuteAlpha);
            mSecondRadialTextsView.setAlpha(secondAlpha);
            mSecondRadialSelectorView.setAlpha(secondAlpha);
        }

    }
    
//    @Override  
//    public boolean dispatchTouchEvent(MotionEvent ev) {  
//        
//         return super.dispatchTouchEvent(ev);  
//    }
//    
//    1. @Override  
//     public boolean onInterceptTouchEvent(MotionEvent ev) {  
//         Log.i("MyLinearLayouts", "onInterceptTouchEvent! ");  
//         return false;  
//     }  
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float eventX = event.getX();
        final float eventY = event.getY();
        int degrees;
        int value;  
        long millis = SystemClock.uptimeMillis();
        
        //v.setSoundEffectsEnabled(true);
        //v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        mDownDegrees = getDegreesFromCoords(eventX, eventY, false);
        ViewGroup localViewGroup = (ViewGroup)getParent();
        if(localViewGroup != null&&mDownDegrees!=-1)
        {
          localViewGroup.requestDisallowInterceptTouchEvent(true);
          if ((localViewGroup.getParent() instanceof ViewGroup))
            localViewGroup = (ViewGroup)localViewGroup.getParent();
          else
            localViewGroup = null;
        }
        
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mInputEnabled) {
                    return true;
                }
                //v.playSoundEffect(SoundEffectConstants.CLICK);
                mDownX = eventX;
                mDownY = eventY;
                
                mLastValueSelected = -1;
                mDoingMove = false;
                mDoingTouch = true;
                    // If we're in accessibility mode, force the touch to be legal. Otherwise,
                    // it will only register within the given touch target zone.
                    boolean forceLegal = mAccessibilityManager.isTouchExplorationEnabled();
                    // Calculate the degrees that is currently being touched.
                    mDownDegrees = getDegreesFromCoords(eventX, eventY, forceLegal);
                    if (mDownDegrees != -1) {
                    	play();
                        // If it's a legal touch, set that number as "selected" after the
                        // TAP_TIMEOUT in case the user moves their finger quickly.
                        tryVibrate();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDoingMove = true;
                                int value = reselectSelector(mDownDegrees,true);
                                mLastValueSelected = value;
                                mListener.onValueSelected(getCurrentItemShowing(), value, false);
                            }
                        }, TAP_TIMEOUT);
                    }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!mInputEnabled) {
                    // We shouldn't be in this state, because input is disabled.
                    Log.e(TAG, "Input was disabled, but received ACTION_MOVE.");
                    return true;
                }
                //v.playSoundEffect(SoundEffectConstants.CLICK);
                float dY = Math.abs(eventY - mDownY);
                float dX = Math.abs(eventX - mDownX);

//                if (!mDoingMove && dX <= TOUCH_SLOP && dY <= TOUCH_SLOP) {
//                    // Hasn't registered down yet, just slight, accidental movement of finger.
//                    break;
//                }

                // If we're in the middle of touching down on AM or PM, check if we still are.
                // If so, no-op. If not, remove its pressed state. Either way, no need to check
                // for touches on the other circle.
                

                if (mDownDegrees == -1) {
                    // Original down was illegal, so no movement will register.
                    break;
                }

                // We're doing a move along the circle, so move the selection as appropriate.
                mDoingMove = true;
                mHandler.removeCallbacksAndMessages(null);
                degrees = getDegreesFromCoords(eventX, eventY, true);
                
                if (degrees != -1) {
                    value = reselectSelector(degrees, true);
                    if (value != mLastValueSelected) {
                    	int i=value-mLastValueSelected;
                    	if(mediaPlayer==null){
                        	play();
                        }
                        tryVibrate();
                        mLastValueSelected = value;
                        mListener.onValueSelected(getCurrentItemShowing(), value, false);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!mInputEnabled) {
                    // If our touch input was disabled, tell the listener to re-enable us.
                    Log.d(TAG, "Input was disabled, but received ACTION_UP.");
                    mListener.onValueSelected(ENABLE_PICKER_INDEX, 1, false);
                    return true;
                }

                mHandler.removeCallbacksAndMessages(null);
                mDoingTouch = false;

                // If we're touching AM or PM, set it as selected, and tell the listener.
                

                // If we have a legal degrees selected, set the value and tell the listener.
                if (mDownDegrees != -1) {
                    degrees = getDegreesFromCoords(eventX, eventY, mDoingMove);
                    if (degrees != -1) {
                        value = reselectSelector(degrees,false);                       
                        setValueForItem(getCurrentItemShowing(), value);
                        mListener.onValueSelected(getCurrentItemShowing(), value, false);
                    }
                }
                mDoingMove = false;
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Try to vibrate. To prevent this becoming a single continuous vibration, nothing will
     * happen if we have vibrated very recently.
     */
    public void tryVibrate() {
        if (mVibrator != null) {
            long now = SystemClock.uptimeMillis();
            // We want to try to vibrate each individual tick discretely.
            if (now - mLastVibrate >= 125) {
                mVibrator.vibrate(5);
                mLastVibrate = now;
            }
        }
    }

    /**
     * Set touch input as enabled or disabled, for use with keyboard mode.
     */
    public boolean trySettingInputEnabled(boolean inputEnabled) {
        if (mDoingTouch && !inputEnabled) {
            // If we're trying to disable input, but we're in the middle of a touch event,
            // we'll allow the touch event to continue before disabling input.
            return false;
        }
        mInputEnabled = inputEnabled;
        mGrayBox.setVisibility(inputEnabled? View.INVISIBLE : View.VISIBLE);
        return true;
    }

    /**
     * Necessary for accessibility, to ensure we support "scrolling" forward and backward
     * in the circle.
     */
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
      super.onInitializeAccessibilityNodeInfo(info);
      info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
      info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    /**
     * Announce the currently-selected time when launched.
     */
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Clear the event's current text so that only the current time will be spoken.
            event.getText().clear();
            Time time = new Time();
            time.hour = getHours();
            time.minute = getMinutes();
            long millis = time.normalize(true);
            int flags = DateUtils.FORMAT_SHOW_TIME;
            String timeString = DateUtils.formatDateTime(getContext(), millis, flags);
            event.getText().add(timeString);
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    /**
     * When scroll forward/backward events are received, jump the time to the higher/lower
     * discrete, visible value on the circle.
     */
    @SuppressLint("NewApi")
    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        }

        int changeMultiplier = 0;
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            changeMultiplier = 1;
        } else if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            changeMultiplier = -1;
        }
        if (changeMultiplier != 0) {
            int value = getCurrentlyShowingValue();
            int stepSize = 0;
            int currentItemShowing = getCurrentItemShowing();
            if (currentItemShowing == HOUR_INDEX) {
                stepSize = HOUR_VALUE_TO_DEGREES_STEP_SIZE;
            } else if (currentItemShowing == MINUTE_INDEX) {
                stepSize = MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
            }else  if (currentItemShowing == SECOND_INDEX){
            	stepSize = MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
            }

            int degrees = value * stepSize;
            degrees = snapOnly30s(degrees, changeMultiplier);
            value = degrees / stepSize;
            int maxValue = 0;
            int minValue = 0;
            if (currentItemShowing == HOUR_INDEX) {             
                maxValue = 23;
            } else {
                maxValue = 55;
            }
            if (value > maxValue) {
                // If we scrolled forward past the highest number, wrap around to the lowest.
                value = minValue;
            } else if (value < minValue) {
                // If we scrolled backward past the lowest number, wrap around to the highest.
                value = maxValue;
            }
            setItem(currentItemShowing, value);
            mListener.onValueSelected(currentItemShowing, value, false);
            return true;
        }

        return false;
    }
    
    private void play(){
    	mediaPlayer = new MediaPlayer(); 
    	if (mediaPlayer!= null) {  
	    	try {  
		    	AssetFileDescriptor afd = mContext.getAssets().openFd("sounds/Keypress.ogg");
		    	mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());	    	
		    	mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);  
		    	mediaPlayer.prepare();  		    
		    	mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
		    		@Override
				    public void onCompletion(MediaPlayer mp) {  
				    	   mp.stop();
				           mp.release();
				           mediaPlayer= null; 				           
				    	}  
			    	}); 
		    	
		    	mediaPlayer.setOnErrorListener(new OnErrorListener() { 
		    		@Override
			    	public boolean onError(MediaPlayer mp, int what, int extra) {  
				    		mp.stop();
				            mp.release();
				            mediaPlayer = null;
				            return true;  
				    	
				    	}  
			    	});  		     	    		 
		    	mediaPlayer.start(); 
	    	} catch (Exception ex) {
	    		
	    	}  
    	}  
    	
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        // Do nothing
    }
}