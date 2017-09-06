package com.cappu.halllockscreen.util;

import java.util.Calendar;

import com.cappu.halllockscreen.R;

public class DateUtils {
    
	  //  农历月份
    private static String[] lunarMonth = {"正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"};
    //  农历日
    private static String[] lunarDay = {"初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"};
    /**
     * 获取农历月份
     * @return
     */
    public static String getLunarMonth() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int[] lunarDate = LunarCalendar.solarToLunar(year, month, day);
        return lunarMonth[lunarDate[1] - 1];
    }

    /**
     * 获取农历日
     * @return
     */
    public static String getLunarDay() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int[] lunarDate = LunarCalendar.solarToLunar(year, month, day);
        return lunarDay[lunarDate[2] - 1];
    }
}
