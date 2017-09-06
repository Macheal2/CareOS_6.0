package com.cappu.halllockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import com.cappu.halllockscreen.util.GestureHelper;
import com.cappu.halllockscreen.util.HallTimeLog;
import java.util.TimeZone;

public class AnalogClock
  extends View
{
  public static final int ANALOG_STYLE = 2;
  private static final String TAG_PREFIX = "analogClock";
  int ANALOG_DIAL_TIME = 4;
  int ANALOG_DIAL_WEEK = 5;
  int ANALOG_HAND_HOUR = 0;
  int ANALOG_HAND_MINUTE = 1;
  int ANALOG_HAND_SECOND = 2;
  int ANALOG_HAND_WEEK = 3;
  int ANALOG_MONTHDAY_TEXT_BG = 6;
  int DRAWABLE_NUM = 7;
  private Drawable[][] mAnalogStyle;
  private boolean mAttached;
  private Time mCalendar;
  private boolean mChanged;
  private final Runnable mClockTick = new Runnable()
  {
    public void run()
    {
      AnalogClock.this.onTimeChanged();
      AnalogClock.this.invalidate();
      AnalogClock.this.postDelayed(AnalogClock.this.mClockTick, 1000L);
    }
  };
  private int[] mColor;
  private final Context mContext;
  private int mCurrentAnalogStyle = 0;//hejianfeng modif
  private final Handler mHandler = new Handler();
  private float mHour;
  private Drawable mHourHand;
  private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
    {
//      if (paramAnonymousIntent.getAction().equals("android.intent.action.TIMEZONE_CHANGED"))
//      {
//        String str = paramAnonymousIntent.getStringExtra("time-zone");
//        AnalogClock.access(AnalogClock.this, new Time(TimeZone.getTimeZone(str).getID()));
//      }
      AnalogClock.this.onTimeChanged();
      AnalogClock.this.invalidate();
    }
  };
  boolean mIsFirstShow = true;
  private Drawable mMinuteHand;
  private float mMinutes;
  private float mMonth;
  private int mMonthDay;
  private Drawable mMonthDayTextBg;
  private int mMonthDayTextBgHeight;
  private int mMonthDayTextBgWidth;
  private int mMonthDayTextHeight = 0;
  private int mMonthDayTextSize;
  private int mMonthDayTextWidth = 0;
  private boolean mNoMonth = false;
  private boolean mNoMonthDay = false;
  private boolean mNoSeconds = false;
  private boolean mNoWeek = false;
  private Paint mPaint = new Paint();
  private Drawable mSecondHand;
  private float mSeconds;
  private Drawable mTestDial;
  private Drawable mTimeDial;
  private final int mTimeDialHeight;
  private final int mTimeDialWidth;
  private String mTimeZoneId;
  private float mWeek;
  private Drawable mWeekDial;
  private Drawable mWeekHand;
  
  public AnalogClock(Context paramContext)
  {
    this(paramContext, null);
  }
  
  public AnalogClock(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 0);
  }
  
  public AnalogClock(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    this.mContext = paramContext;
    this.mMonthDayTextSize = getResources().getDimensionPixelSize(R.dimen.bird_analog_clock_monthday_text_size);
    Drawable[][] arrayOfDrawable= new Drawable[2][];
    Drawable[] arrayOfDrawable1 = new Drawable[7];
    arrayOfDrawable1[0] = getAnalogDrawable(R.drawable.clock_analog_silver_hour);
    arrayOfDrawable1[1] = getAnalogDrawable(R.drawable.clock_analog_silver_minute);
    arrayOfDrawable1[2] = getAnalogDrawable(R.drawable.clock_analog_silver_second);
    arrayOfDrawable1[3] = getAnalogDrawable(R.drawable.clock_analog_silver_week);
    arrayOfDrawable1[4] = getAnalogDrawable(R.drawable.clock_analog_silver_dial_time);
    arrayOfDrawable1[5] = getAnalogDrawable(R.drawable.clock_analog_silver_dial_week);
    arrayOfDrawable1[6] = getAnalogDrawable(R.drawable.clock_analog_silver_monthday_bg);
    arrayOfDrawable[0] = arrayOfDrawable1;
    Drawable[] arrayOfDrawable2 = new Drawable[7];
    arrayOfDrawable2[0] = getAnalogDrawable(R.drawable.clock_analog_blue_hour);
    arrayOfDrawable2[1] = getAnalogDrawable(R.drawable.clock_analog_blue_minute);
    arrayOfDrawable2[2] = getAnalogDrawable(R.drawable.clock_analog_blue_second);
    arrayOfDrawable2[3] = getAnalogDrawable(R.drawable.clock_analog_blue_week);
    arrayOfDrawable2[4] = getAnalogDrawable(R.drawable.clock_analog_blue_dial_time);
    arrayOfDrawable2[5] = getAnalogDrawable(R.drawable.clock_analog_blue_dial_week);
    arrayOfDrawable2[6] = getAnalogDrawable(R.drawable.clock_analog_blue_monthday_bg);
    arrayOfDrawable[1] = arrayOfDrawable2;
    this.mAnalogStyle = arrayOfDrawable;
    int[] arrayOfInt = new int[2];
    arrayOfInt[0] = getResources().getColor(R.color.clock_analog_silver_monthday_text_color);
    arrayOfInt[1] = getResources().getColor(R.color.clock_analog_blue_monthday_text_color);
    this.mColor = arrayOfInt;
    setCurrentAnalogStyle(this.mCurrentAnalogStyle);
    this.mCalendar = new Time();
    this.mTimeDialWidth = this.mTimeDial.getIntrinsicWidth();
    this.mTimeDialHeight = this.mTimeDial.getIntrinsicHeight();
    setMonthDayTextColor();
    this.mPaint.setTextSize(this.mMonthDayTextSize);
    this.mPaint.setAntiAlias(true);
    Typeface localTypeface = Typeface.create(Typeface.SERIF, 1);
    this.mPaint.setTypeface(localTypeface);
    Rect localRect = new Rect();
    this.mPaint.getTextBounds("99", 0, 2, localRect);
    this.mMonthDayTextWidth = (localRect.right - localRect.left);
    this.mMonthDayTextHeight = (localRect.bottom - localRect.top);
    this.mMonthDayTextBgWidth = this.mMonthDayTextBg.getIntrinsicWidth();
    this.mMonthDayTextBgHeight = this.mMonthDayTextBg.getIntrinsicHeight();
  }
  
  private void drawAnalogSet(int paramInt1, Canvas paramCanvas, int paramInt2, int paramInt3, int paramInt4, int paramInt5, boolean paramBoolean)
  {
    if (paramBoolean) {
      this.mTimeDial.setBounds(paramInt2 - paramInt4 / 2, paramInt3 - paramInt5 / 2, paramInt2 + paramInt4 / 2, paramInt3 + paramInt5 / 2);
    }
    this.mTimeDial.draw(paramCanvas);
    int i = paramInt3 + paramInt5 / 6;
    drawDial(paramCanvas, this.mWeekDial, paramInt2, i, paramBoolean);
    drawHand(paramCanvas, this.mWeekHand, paramInt2, i, 360.0F * ((this.mWeek - 1.0F) / 7.0F), paramBoolean);
    int j = paramInt2 + paramInt4 / 4 + this.mMonthDayTextBgWidth / 3;
    drawDial(paramCanvas, this.mMonthDayTextBg, j, paramInt3, paramBoolean);
    StringBuilder localStringBuilder = new StringBuilder();
    String str="";
    if (this.mMonthDay < 10) {
    	str="0";
    }
//    for (String str = "0";; str = "")
//    {
      paramCanvas.drawText(str + this.mMonthDay, j - this.mMonthDayTextWidth / 2, paramInt3 + this.mMonthDayTextHeight / 2, this.mPaint);
      drawHand(paramCanvas, this.mHourHand, paramInt2, paramInt3, 360.0F * (this.mHour / 12.0F), paramBoolean);
      drawHand(paramCanvas, this.mMinuteHand, paramInt2, paramInt3, 360.0F * (this.mMinutes / 60.0F), paramBoolean);
      if (!this.mNoSeconds) {
        drawHand(paramCanvas, this.mSecondHand, paramInt2, paramInt3, 360.0F * (this.mSeconds / 60.0F), paramBoolean);
      }
//      return;
//    }
  }
  
  private void drawDial(Canvas paramCanvas, Drawable paramDrawable, int paramInt1, int paramInt2, boolean paramBoolean)
  {
    if (paramBoolean)
    {
      int i = paramDrawable.getIntrinsicWidth();
      int j = paramDrawable.getIntrinsicHeight();
      paramDrawable.setBounds(paramInt1 - i / 2, paramInt2 - j / 2, paramInt1 + i / 2, paramInt2 + j / 2);
    }
    paramDrawable.draw(paramCanvas);
  }
  
  private void drawHand(Canvas paramCanvas, Drawable paramDrawable, int paramInt1, int paramInt2, float paramFloat, boolean paramBoolean)
  {
    paramCanvas.save();
    paramCanvas.rotate(paramFloat, paramInt1, paramInt2);
    if (paramBoolean)
    {
      int i = paramDrawable.getIntrinsicWidth()*2/3;
      int j = paramDrawable.getIntrinsicHeight()*2/3;
      paramDrawable.setBounds(paramInt1 - i / 2, paramInt2 - j / 2, paramInt1 + i / 2, paramInt2 + j / 2);
    }
    paramDrawable.draw(paramCanvas);
    paramCanvas.restore();
  }
  
  private Drawable getAnalogDrawable(int paramInt)
  {
    return getResources().getDrawable(paramInt);
  }
  
  private String getStyle(int paramInt)
  {
    switch (paramInt)
    {
    case 0: 
      return "silver";
    default: 
    	return "blue";
    }
  }
  
  private void onTimeChanged()
  {
    this.mCalendar.setToNow();
    if (this.mTimeZoneId != null) {
      this.mCalendar.switchTimezone(this.mTimeZoneId);
    }
    int i = this.mCalendar.hour;
    int j = this.mCalendar.minute;
    int k = this.mCalendar.second;
    this.mSeconds = k;
    this.mMinutes = (j + k / 60.0F);
    this.mHour = (i + this.mMinutes / 60.0F);
    this.mWeek = this.mCalendar.weekDay;
    this.mMonth = this.mCalendar.month;
    this.mMonthDay = this.mCalendar.monthDay;
    this.mChanged = true;
    updateContentDescription(this.mCalendar);
  }
  
  private void setMonthDayTextColor()
  {
    this.mPaint.setColor(this.mColor[this.mCurrentAnalogStyle]);
  }
  
  private void updateContentDescription(Time paramTime)
  {
    setContentDescription(DateUtils.formatDateTime(this.mContext, paramTime.toMillis(false), 129));
  }
  
  public void changeStyle()
  {
    this.mCurrentAnalogStyle = (1 + this.mCurrentAnalogStyle);
    this.mCurrentAnalogStyle %= 2;
    setCurrentAnalogStyle(this.mCurrentAnalogStyle);
    invalidate();
  }
  
  public void enableSeconds(boolean paramBoolean)
  {
    if (!paramBoolean) {}
    for (boolean bool = true;; bool = false)
    {
      this.mNoSeconds = bool;
      return;
    }
  }
  
  public int getCurrentAnalogStyle()
  {
    return this.mCurrentAnalogStyle;
  }
  
  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    if (!this.mAttached)
    {
      this.mAttached = true;
      IntentFilter localIntentFilter = new IntentFilter();
      localIntentFilter.addAction("android.intent.action.TIME_TICK");
      localIntentFilter.addAction("android.intent.action.TIME_SET");
      localIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
      getContext().registerReceiver(this.mIntentReceiver, localIntentFilter, null, this.mHandler);
    }
    this.mCalendar = new Time();
    onTimeChanged();
    post(this.mClockTick);
  }
  
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    if (this.mAttached)
    {
      getContext().unregisterReceiver(this.mIntentReceiver);
      removeCallbacks(this.mClockTick);
      this.mAttached = false;
    }
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    boolean bool = this.mChanged;
    if (bool) {
      this.mChanged = false;
    }
    int i = getWidth();
    int j = getHeight();
    int k = i / 2;
    int m = j / 2;
    if ((this.mIsFirstShow) && (getVisibility() == 0))
    {
      for (int n = 0; n < 2; n++) {
        if (n != this.mCurrentAnalogStyle) {
          drawAnalogSet(n, paramCanvas, k, m, i, i, bool);
        }
      }
      this.mIsFirstShow = false;
    }
    drawAnalogSet(this.mCurrentAnalogStyle, paramCanvas, k, m, i, i, bool);
    if (0 != 0) {
      paramCanvas.restore();
    }
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    this.mChanged = true;
  }
  
  public void setCurrentAnalogStyle(int paramInt)
  {
    this.mCurrentAnalogStyle = paramInt;
    if ((this.mCurrentAnalogStyle < 0) || (this.mCurrentAnalogStyle >= 2))
    {
      this.mCurrentAnalogStyle = 0;
      GestureHelper.storeStyle(getContext(), this.mCurrentAnalogStyle);
    }
    HallTimeLog.d("analogClock", "style=" + getStyle(this.mCurrentAnalogStyle));
    this.mHourHand = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_HAND_HOUR];
    this.mMinuteHand = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_HAND_MINUTE];
    this.mSecondHand = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_HAND_SECOND];
    this.mWeekHand = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_HAND_WEEK];
    this.mTimeDial = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_DIAL_TIME];
    this.mWeekDial = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_DIAL_WEEK];
    this.mMonthDayTextBg = this.mAnalogStyle[this.mCurrentAnalogStyle][this.ANALOG_MONTHDAY_TEXT_BG];
    setMonthDayTextColor();
  }
  
  public void setTimeZone(String paramString)
  {
    this.mTimeZoneId = paramString;
    onTimeChanged();
  }
}

