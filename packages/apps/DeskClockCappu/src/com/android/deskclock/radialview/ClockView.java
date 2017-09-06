package com.android.deskclock.radialview;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews.RemoteView;
import com.android.deskclock.R;
import java.util.TimeZone;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class ClockView extends View {
    private Time mCalendar;

    private final Drawable mHourHand;
    private final Drawable mMinuteHand;
    private final Drawable mSecondHand;
    private final Drawable mDial;

    private final int mDialWidth;
    private final int mDialHeight;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private int mSeconds;
    private int mMinutes;
    private int mHours;
    private boolean mChanged;
    private final Context mContext;
    private String mTimeZoneId;
    private boolean mNoSeconds = false;
    private boolean mNoMinutes = false;
    private boolean mNoHours = false;
    
    private final float mDotRadius;
    private final float mDotOffset;
    private Paint mDotPaint;

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        Resources r = mContext.getResources();

        mDial = r.getDrawable(R.drawable.magcomm_clock_analog_dial_mipmap);
        mHourHand = r.getDrawable(R.drawable.magcomm_clock_analog_hour_mipmap);
        mMinuteHand = r.getDrawable(R.drawable.magcomm_clock_analog_minute_mipmap);
        mSecondHand = r.getDrawable(R.drawable.magcomm_clock_analog_second_mipmap);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClockView);
        mDotRadius = a.getDimension(R.styleable.ClockView_Radius, 0);
        mDotOffset = a.getDimension(R.styleable.ClockView_Offset, 0);
        final int dotColor = a.getColor(R.styleable.ClockView_Color, Color.WHITE);
        if (dotColor != 0) {
            mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDotPaint.setColor(dotColor);
        }

//        mCalendar = new Time();
//
        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

//        if (!mAttached) {
//            mAttached = true;
//            IntentFilter filter = new IntentFilter();
//
//            filter.addAction(Intent.ACTION_TIME_TICK);
//            filter.addAction(Intent.ACTION_TIME_CHANGED);
//            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
//
//            getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
//        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        //mCalendar = new Time();

        // Make sure we update to the current time
        //onTimeChanged();

        // tick the seconds
        //post(mClockTick);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        if (mAttached) {
//            getContext().unregisterReceiver(mIntentReceiver);
//            removeCallbacks(mClockTick);
//            mAttached = false;
//        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mDialWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mDialHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                                   (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        if (mDotRadius > 0f && mDotPaint != null) {
            canvas.drawCircle(x, y - (h / 2) + mDotOffset, mDotRadius, mDotPaint);
        }

        if(!mNoHours){
        	drawHand(canvas, mHourHand, x, y, mHours / 24.0f * 360.0f, changed);
        }
        if(!mNoMinutes){
        	drawHand(canvas, mMinuteHand, x, y, mMinutes / 60.0f * 360.0f, changed);
        }
        if (!mNoSeconds) {
            drawHand(canvas, mSecondHand, x, y, mSeconds / 60.0f * 360.0f, changed);
        }

        if (scaled) {
            canvas.restore();
        }
    }

    private void drawHand(Canvas canvas, Drawable hand, int x, int y, float angle,
          boolean changed) {
      canvas.save();
      canvas.rotate(angle, x, y);
      if (changed) {
          final int w = hand.getIntrinsicWidth();
          final int h = hand.getIntrinsicHeight();
          hand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
      }
      hand.draw(canvas);
      canvas.restore();
    }

    //private void onTimeChanged() {
        //mCalendar.setToNow();

//        if (mTimeZoneId != null) {
//            mCalendar.switchTimezone(mTimeZoneId);
//        }

//        int hour = mCalendar.hour;
//        int minute = mCalendar.minute;
//        int second = mCalendar.second;
  //      long millis = System.currentTimeMillis() % 1000;

//        mSeconds = 0;//(float) ((second * 1000 + millis) / 166.666);
//        mMinutes = 0;
//        mHour = 0;
//        mChanged = true;

        //updateContentDescription(mCalendar);
  //  }
    
    public void setSeconds(int Seconds) {
    	mChanged = true;
        mSeconds = Seconds;
    }
    
    public void setMinutes(int Minutes) {
    	mChanged = true;
        mMinutes = Minutes;
    }
    
    public void setHours(int hours) {
    	mChanged = true;
    	mHours = hours;
    }

//    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
//                String tz = intent.getStringExtra("time-zone");
//                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
//            }
//            onTimeChanged();
//            invalidate();
//        }
//    };

//    private final Runnable mClockTick = new Runnable () {
//
//        @Override
//        public void run() {
//            onTimeChanged();
//            invalidate();
//            ClockView.this.postDelayed(mClockTick, 1000);
//        }
//    };

//    private void updateContentDescription(Time time) {
//        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
//        String contentDescription = DateUtils.formatDateTime(mContext,
//                time.toMillis(false), flags);
//        setContentDescription(contentDescription);
//    }

//    public void setTimeZone(String id) {
//        mTimeZoneId = id;
//        onTimeChanged();
//    }

    public void enableSeconds(boolean enable) {
        mNoSeconds = !enable;
    }
    
    public void enableMinutes(boolean enable) {
        mNoMinutes = !enable;
    }
    
    public void enableHours(boolean enable) {
        mNoHours = !enable;
    }
}

