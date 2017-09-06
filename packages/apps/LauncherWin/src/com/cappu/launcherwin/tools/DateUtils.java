package com.cappu.launcherwin.tools;

import java.util.Calendar;

import com.cappu.launcherwin.R;

public class DateUtils {

    public static String[] tools = { "计算器", "记事本", "上网导航", "设置", "手电筒", "文件管理", "我在哪儿", "一键加速" };
    public static String[] games = { "中国象棋", "军棋" };
    public static String[] gooduse = { "二十四节气", "文件管理", "记事本" };
    public static String[] media = { "视频" };
    public static int Modulesbg[] = { R.drawable.btn_color_1, R.drawable.btn_color_2, R.drawable.btn_color_3, R.drawable.btn_color_4,
            R.drawable.btn_color_5, R.drawable.btn_color_6 };
    
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
