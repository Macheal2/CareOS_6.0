package com.cappu.launcherwin.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.util.Log;

/**目前是计算国内时区*/
public class TimeTools {
    private String TAG = "TimeTools";
    
    Calendar mCalendar;
    /**国内时区 +8*/
    int mTimeZone = 8;
    
    /**一秒的毫秒数*/
    private static final int MILLISECIBD = 1000;
    /**一分钟的毫秒数*/
    private static final int SECOND_MILLISECIBD = 60 * MILLISECIBD;
    /**一小时的毫秒数*/
    private static final int HOUR_MILLISECIBD = 60 * SECOND_MILLISECIBD;
    /**一天的毫秒数*/
    private static final int DAY_MILLISECIBD = 24 * HOUR_MILLISECIBD;
    
    public TimeTools(){
        mCalendar = Calendar.getInstance();
    }
    
    /**获取当天当前的小时数*/
    public int getCurrentHourOfDay(){
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }
    
    /**获取当天当前的分钟数*/
    public int getCurrentMinuteOfDay(){
        return mCalendar.get(Calendar.MINUTE);
    }
    
    /**获取当天当前的秒钟数*/
    public int getCurrentSecondOfDay(){
        return mCalendar.get(Calendar.SECOND);
    }
    
    /**判断当前是否是某个时间值*/
    public boolean compareTo(long millis){
        long currentMillis = millis % DAY_MILLISECIBD;//对当前秒数取余，得到这一天从零点到现在的秒数
        
        long hour = currentMillis / HOUR_MILLISECIBD+mTimeZone ;
        
        long minute = (currentMillis % HOUR_MILLISECIBD) / SECOND_MILLISECIBD;
        
        Log.i(TAG, "hour:"+hour+"    minute:"+minute+"    ");
        
        return true;
    }
    
    /**根据毫秒数计算的小时数  这个里面默认是二十四小时制*/
    public long getMillisTimeToHour(long millis){
        long currentMillis = millis % DAY_MILLISECIBD;//对当前秒数取余，得到这一天从零点到现在的秒数
        long hour = currentMillis / HOUR_MILLISECIBD+mTimeZone ;
        return hour;
    }
    
    /**获取一个毫秒中的分钟数*/
    public long getMillisTimeToMinute(long millis){
        long currentMillis = millis % DAY_MILLISECIBD;//对当前秒数取余，得到这一天从零点到现在的秒数
        long minute = (currentMillis % HOUR_MILLISECIBD) / SECOND_MILLISECIBD;
        return minute;
    }
    
    /**判断当前某个时间的小时与分钟数值是否相等*/
    public boolean compareToHourAndMinute(long millisArg1,long millisArg2){
        long hour1 = getMillisTimeToHour(millisArg1);
        long hour2 = getMillisTimeToHour(millisArg2);
        
        long minute1 = getMillisTimeToMinute(millisArg1);
        long minute2 = getMillisTimeToMinute(millisArg2);
        
        
        if(hour1 == hour2 && minute1 == minute2){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * 判断两个时间点大小  只判断小时与分钟的时间点
     * 
     * @param 如果millisArg1 大于 millisArg2 返回true  负责返回false
     * @param millisArg1  millisArg2 毫秒数
     * */
    public boolean  compareToTime(long millisArg1,long millisArg2){
        long hour1 = getMillisTimeToHour(millisArg1);
        long hour2 = getMillisTimeToHour(millisArg2);
        long hour2NoZone = hour2 - mTimeZone;//这里减去时区是因为我们中国处于当前的东八区
        
        long minute1 = getMillisTimeToMinute(millisArg1);
        long minute2 = getMillisTimeToMinute(millisArg2);
        
        long millis1 = getMillisTime(hour1, minute1);
        long millis2 = getMillisTime(hour2NoZone, minute2);
        
        Log.i(TAG, "hour1:"+hour1+"    minute1:"+minute1+"        hour2:"+hour2+"    hour2NoZone:"+hour2NoZone+"    minute2:"+minute2);
        if(millis1>millis2){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * 根据一个时间得到一个毫秒数
     * @param hour   小时
     * @param minute 分
     * */
    public long getMillisTime(long hour, long minute) {
        long millis = SECOND_MILLISECIBD * minute + HOUR_MILLISECIBD * (hour - mTimeZone) ;
        return millis;
    }
    
    /**给一个时间计算当前礼拜几*/
    public static String getWeek(long time) {
        final Locale mLocale = Locale.getDefault();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("EEEE", mLocale);
        String mDate = mDateFormat.format(new Date(time));
        return mDate;
    }
    
}
