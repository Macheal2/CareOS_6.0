package com.cappu.launcherwin.nativejni;


import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

/**日历里面的一些算法*/
public class KookNativeCalendar extends KookNative {
    
    /**天干地支里面的纪年算法*/
    public native int[] getJiYear(int year);
    
    /**天干地支里面的纪月算法*/
    public native int[] getJiMonth(int year,int mounth,int day);
    
    /**天干地支里面的纪日算法*/
    public native int[] getJiDay(int year,int mounth,int day);
    

}
