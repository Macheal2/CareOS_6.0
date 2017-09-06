package com.cappu.halllockscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.cappu.halllockscreen.util.HallTimeLog;
import com.cappu.halllockscreen.util.DateUtils;

public class DigitalClock
  extends LinearLayout
{
  protected static String TAG_PREFIX = "DigitalClock";
  public static String mDateString = "";
  public static String mWeekString="";
  public static String mLunarString="";
  protected TextView mAmPm;
  Runnable mCheckRunnable = new Runnable()
  {
    public void run()
    {
      DigitalClock.this.updateTime();
      DigitalClock.this.mHandler.postDelayed(this, 1000L);
    }
  };
  protected Context mContext;
  protected TextView mDate;
  protected ImageView mDot;
  Handler mHandler = new Handler();
  protected ImageView mHourHigh;
  protected ImageView mHourLow;
  protected TextView mLunarView;
  protected ImageView mMinuteHigh;
  protected ImageView mMinuteLow;
  private boolean mShowHourHighWhenIs12Format = false;
  protected TextView mWeekDay;
  
  public DigitalClock(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    this.mContext = paramContext;
    ((LayoutInflater)paramContext.getSystemService("layout_inflater")).inflate(getContentId(), this);
    this.mHourHigh = ((ImageView)findViewById(R.id.bird_hour_high));
    this.mHourLow = ((ImageView)findViewById(R.id.bird_hour_low));
    this.mDot = ((ImageView)findViewById(R.id.bird_dot));
    this.mMinuteHigh = ((ImageView)findViewById(R.id.bird_minute_high));
    this.mMinuteLow = ((ImageView)findViewById(R.id.bird_minute_low));
    this.mAmPm = ((TextView)findViewById(R.id.bird_ampm));
    this.mDate = ((TextView)findViewById(R.id.date));
    this.mWeekDay = ((TextView)findViewById(R.id.weekday));
    this.mLunarView = ((TextView)findViewById(R.id.lunar));
    //hejianfeng add start
    mDateString=getDateString();
    mWeekString=getWeek();
    mLunarString=DateUtils.getLunarMonth()+DateUtils.getLunarDay();
    //hejianfeng add end
    this.mHandler.post(this.mCheckRunnable);
  }
  
  protected int getContentId()
  {
      return R.layout.circular_digital_clock;
  }
  
  protected int getImageResId(int paramInt)
  {
    switch (paramInt)
    {
    case 0: 
      return R.drawable.zzz_zero;
    case 1: 
      return R.drawable.zzz_one;
    case 2: 
      return R.drawable.zzz_two;
    case 3: 
      return R.drawable.zzz_third;
    case 4: 
      return R.drawable.zzz_four;
    case 5: 
      return R.drawable.zzz_five;
    case 6: 
      return R.drawable.zzz_six;
    case 7: 
      return R.drawable.zzz_seven;
    case 8: 
      return R.drawable.zzz_eight;
    case 9:
    	return R.drawable.zzz_nine;
    default: 
        return R.drawable.zzz_zero;
    }
  }
  
  public boolean is12TimeFormat()
  {
    return !"24".equals(android.provider.Settings.System.getString(getContext().getContentResolver(), "time_12_24"));
  }
  
	public void setResource(int paramInt1, int paramInt2, int paramInt3,
			int paramInt4, String paramString1, String paramString2,
			String paramString3) {
		this.mHourHigh.setImageResource(paramInt1);
		if ((!this.mShowHourHighWhenIs12Format)
				&& (paramInt1 == R.drawable.zzz_zero)) {
			ImageView localImageView = this.mHourHigh;
			if (is12TimeFormat()) {
				localImageView.setVisibility(8);
			}
		}else{
			ImageView localImageView = this.mHourHigh;
			localImageView.setVisibility(0);
		}
		this.mHourLow.setImageResource(paramInt2);
		if (this.mDot != null) {
			this.mDot.setImageResource(R.drawable.circular_clock_digital_dot);
		}
		this.mMinuteHigh.setImageResource(paramInt3);
		this.mMinuteLow.setImageResource(paramInt4);
		this.mDate.setText(paramString1);
		this.mWeekDay.setText(paramString2);
		//hejianfeng add start
		this.mLunarView.setText(mLunarString);
		HallTimeLog.v("hejianfeng", "mDate="+paramString1);
		HallTimeLog.v("hejianfeng", "mWeekDay="+paramString2);
		//hejianfeng add end
		if (this.mAmPm != null) {
			if (!is12TimeFormat()) {
				this.mAmPm.setVisibility(8);
				return;
			}else{
				this.mAmPm.setVisibility(0);
			}
			this.mAmPm.setText(paramString3);
		}
	}
  
  public void showHourHighWhenIs12Format(boolean paramBoolean)
  {
    this.mShowHourHighWhenIs12Format = paramBoolean;
  }
  //hejianfeng add start
	public static String getDateString() {
		String dateStr = DateFormat.getBestDateTimePattern(Locale.getDefault(),
				"MMMMd");
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateStr,
				Locale.getDefault());
		String str2 = dateFormat.format(new Date(System.currentTimeMillis()));
		return str2;
	}
	  /*获取星期几*/
    public static String getWeek(){
        Calendar cal = Calendar.getInstance();
        int i = cal.get(Calendar.DAY_OF_WEEK);
        switch (i) {
        case 1:
            return "星期日";
        case 2:
            return "星期一";
        case 3:
            return "星期二";
        case 4:
            return "星期三";
        case 5:
            return "星期四";
        case 6:
            return "星期五";
        case 7:
            return "星期六";
        default:
            return "";
        }
    }
  //hejianfeng add end 
  public void updateTime()
  {
    Calendar localCalendar = Calendar.getInstance();
    int i = localCalendar.get(Calendar.HOUR_OF_DAY);
    if (is12TimeFormat())
    {
      i %= 12;
      if (i == 0) {
        i = 12;
      }
    }
    int j = getImageResId(i / 10);
    int k = getImageResId(i % 10);
    int m = localCalendar.get(Calendar.MINUTE);
    int n = getImageResId(m / 10);
    int i1 = getImageResId(m % 10);
    int i2 = localCalendar.get(Calendar.AM_PM);
    
    setResource(j, k, n, i1, mDateString, mWeekString, android.text.format.DateUtils.getAMPMString(i2));
  }
}

