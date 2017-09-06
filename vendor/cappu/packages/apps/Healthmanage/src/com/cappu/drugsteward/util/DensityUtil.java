package com.cappu.drugsteward.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;

public class DensityUtil {
    public final static String Tag = "hmq";
    public final static String USERNAME_KEY = "drugsteward_user_";
    public final static SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 把日期转为字符串
     */
    public static String ConverToString(Date date) {
        return mSDF.format(date);
    }

    public static String CalendarToString(Calendar calDate) {
        return mSDF.format(calDate.getTime());
    }
    
    public static Calendar StringToCalendar(String strDate) {
        Date date = null;
        try {
            date = ConverToDate(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * 把字符串转为日期
     */
    public static Date ConverToDate(String strDate) throws Exception {
        return mSDF.parse(strDate);
    }

    /**
     * 获取两个日期之间的间隔天数
     * 
     * @return
     */
    public static int getGapCount(Calendar startDate, Calendar endDate) {
        if (startDate == null || endDate == null)
            return 999;
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        return (int) ((endDate.getTime().getTime() - startDate.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }
}
