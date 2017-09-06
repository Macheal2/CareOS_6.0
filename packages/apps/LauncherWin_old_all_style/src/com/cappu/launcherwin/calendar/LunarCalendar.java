package com.cappu.launcherwin.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LunarCalendar {
    /**农历的年份*/
    private int year; // 农历的年份
    /**农历的月份*/
    private int month;
    /**农历的日*/
    private int day;
    /**农历的月份*/
    private String lunarMonth; // 农历的月份
    private boolean leap;
    
    /**闰的是哪个月*/
    public int leapMonth = 0; // 闰的是哪个月
    /**天干*/
    /**地支*/
    private static String[] Gan = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸" };//天干;
    private static String[] Zhi = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥" };
    
    String[][] JiMonthS = { { "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申", "癸酉", "甲戌", "乙亥", "丙子", "丁丑" },
                            { "戊寅", "己卯", "庚辰", "辛巳", "壬午", "癸未", "甲申", "乙酉", "丙戌", "丁亥", "戊子", "己丑" },
                            { "庚寅", "辛卯", "壬辰", "癸巳", "甲午", "乙未", "丙申", "丁酉", "戊戌", "己亥", "庚子", "辛丑" },
                            { "壬寅", "癸卯", "甲辰", "乙巳", "丙午", "丁未", "戊申", "己酉", "庚戌", "辛亥", "壬子", "癸丑" },
                            { "甲寅", "乙卯", "丙辰", "丁巳", "戊午", "己未", "庚申", "辛酉", "壬戌", "癸亥", "甲子", "乙丑" }};
    private String JiDay;
    private String JiMonth;
    
    private String[] Animals;

    final static String chineseNumber[] = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊" };
    static SimpleDateFormat chineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    final static long[] lunarInfo = new long[] { 0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, 0x04ae0,
            0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54,
            0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, 0x0d4a0,
            0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5d0, 0x14573,
            0x052d0, 0x0a9a8, 0x0e950, 0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0,
            0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6, 0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50,
            0x06d40, 0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960,
            0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0,
            0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, 0x05aa0,
            0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0,
            0x0aa50, 0x1b255, 0x06d20, 0x0ada0 };

    // 农历部分假日
    final static String[] lunarHoliday = new String[] { "0101 春节", "0115 元宵","0202 龙抬头", "0505 端午", "0707 七夕", "0715 中元", "0815 中秋", "0909 重阳", "1208 腊八",
            "1224 小年", "0100 除夕" };

    // 公历部分节假日
    final static String[] solarHoliday = new String[] { "0101 元旦", "0214 情人节", "0308 妇女节", "0312 植树节", "0315 消费者权益日", "0401 愚人节", "0501 劳动节", "0504 青年节",
            "0512 护士节", "0601 儿童节", "0701 建党\n香港回归纪念日", "0801 建军节", "0910 教师节", "1001 国庆节", "1024 联合国日","1126 感恩节","1213 南京大屠杀纪念日",
            "1220 澳门回归纪念日", "1225 圣诞" };

    
    // ====== 传回农历 y年的总天数
    final private static int yearDays(int y) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((lunarInfo[y - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(y));
    }

    // ====== 传回农历 y年闰月的天数
    final private static int leapDays(int y) {
        if (leapMonth(y) != 0) {
            if ((lunarInfo[y - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }

    /** 传入农历 y 年闰哪个月 1-12 , 没闰传回 0*/
    final private static int leapMonth(int y) {
        return (int) (lunarInfo[y - 1900] & 0xf);
    }
    
    /** 
     * 传入阳历 y 年闰哪个月 1-12 , 没闰传回 0
     * 这里是为了解决leapMonth这个方法中当阳历比农历多一年时跳转中还有 闰年 的情况
     * */
    final public static int leapSolarMonth(int y) {
        return (int) (lunarInfo[y - 1900] & 0xf);
    }

    // ====== 传回农历 y年m月的总天数
    final private static int monthDays(int y, int m) {
        if ((lunarInfo[y - 1900] & (0x10000 >> m)) == 0)
            return 29;
        else
            return 30;
    }

    // ====== 传回农历 y年的生肖
    final public String animalsYear(int year) {
        if(Animals == null){
            Animals = new String[] { "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪" };
        }
        return Animals[(year - 4) % 12];
    }

    // ====== 传入 月日的offset 传回干支, 0=甲子
    final private static String cyclicalm(int num) {
        if(Gan == null){
            Gan = new String[] { "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸" };//天干
        }
        if(Zhi == null){
            Zhi = new String[] { "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥" }; //地支
        }
        return (Gan[num % 10] + Zhi[num % 12]);
    }

    /**纪年算法 传入 offset 传回干支, 0=甲子*/
    final public String cyclical(int year) {
        int num = year - 1900 + 36;
        return (cyclicalm(num));
    }
    
    /**纪日算法*/
    private void JiDay(int year, int month, int day) {
        int C = year / 100;
        int Y = year % 100;
        int M = month;
        int D = day;

        if (M == 1 || M == 2) {
            M += 12;
        }
        int i = 0;
        if (M % 2 == 0) {
            i = 6;
        } else {
            i = 0;
        }

        int tiangan = 4 * C + C / 4 + 5 * Y + Y / 4 + (3 * (M + 1)) / 5 + D - 3;
        int dizhi = 8 * C + C / 4 + 5 * Y + Y / 4 + (3 * (M + 1)) / 5 + D + 7 + i;

        int indexGan = tiangan % 10;
        int indexZhi = dizhi % 12;
        if(indexGan == 0){
            indexGan = 10;
        }
        
        if(indexZhi == 0){
            indexZhi = 12;
        }
        //Log.i("HHJ", "tiangan:"+tiangan+"  indexGan:"+indexGan+"    dizhi:"+dizhi+"  indexZhi:"+indexZhi);
        JiDay = Gan[indexGan-1] + Zhi[indexZhi-1];
    }
    
    public String getJiDay(){
        return JiDay;
    }
    
    final public String jiri(int year,int month,int day){
        int gan = 4*(year/100)+ 5* year%100;
        return null;
    }
    

    public static String getChinaDayString(int day) {
        String chineseTen[] = { "初", "十", "廿", "卅" };
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30)
            return "";
        if (day == 10)
            return "初十";
        else
            return chineseTen[day / 10] + chineseNumber[n];
    }
    

    public String getLunarDate(int year_log, int month_log, int day_log, boolean isday) {
        return getLunarDate(year_log, month_log, day_log, null, isday);
    }
    /** */
    /**
     * 传出y年m月d日对应的农历. yearCyl3:农历年与1864的相差数 ? monCyl4:从1900年1月31日以来,闰月数
     * dayCyl5:与1900年1月31日相差的天数,再加40 ?
     * 
     * isday: 这个参数为false---日期为节假日时，阴历日期就返回节假日 ，true---不管日期是否为节假日依然返回这天对应的阴历日期
     * 
     * @param cal
     * @return
     */
    public String getLunarDate(int year_log, int month_log, int day_log, String jieqi, boolean isday) {
        int yearCyl, monCyl, dayCyl;
        // int leapMonth = 0;
        String nowadays;
        Date baseDate = null;
        Date nowaday = null;
        boolean father_day = false;
        boolean mother_day = false;
        
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        if(month_log == 6 && day_log > 14 && day_log < 22){
            //int index_week = 0;
            for(int i = 15; i < day_log + 1; i++){
                try {
                    c.setTime(format1.parse((year_log+"-"+month_log+"-"+i)));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                    //index_week += 1;
                    father_day = i == day_log ? true : false;
                    break;
                }
            }
        }else if(month_log == 5 && day_log > 7 && day_log < 15){
            //int index_week = 0;
            for(int i = 8; i < day_log+1; i++){
                try {
                    c.setTime(format1.parse((year_log+"-"+month_log+"-"+i)));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (c.get(Calendar.DAY_OF_WEEK) == 1) {
                    //index_week += 1;
                    mother_day = i == day_log ? true : false;
                    break;
                }
            }
        }
        
        try {
            baseDate = chineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            e.printStackTrace(); // To change body of catch statement use
            // Options | File Templates.
        }

        nowadays = year_log + "年" + month_log + "月" + day_log + "日";
        try {
            nowaday = chineseDateFormat.parse(nowadays);
        } catch (ParseException e) {
            e.printStackTrace(); // To change body of catch statement use
            // Options | File Templates.
        }

        // 求出和1900年1月31日相差的天数
        int offset = (int) ((nowaday.getTime() - baseDate.getTime()) / 86400000L);
        dayCyl = offset + 40;
        monCyl = 14;

        // 用offset减去每农历年的天数
        // 计算当天是农历第几天
        // i最终结果是农历的年份
        // offset是当年的第几天
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 10000 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }
        // 农历年份
        year = iYear;
        setYear(year); // 设置公历对应的农历年份

        yearCyl = iYear - 1864;
        
        leapMonth = leapMonth(iYear); // 闰哪个月,1-12 
        
        Log.i("hehangjun", "闰哪个月,1-12:"+leapMonth+"    iYear:"+iYear+"    这个是传进来的year_log："+year_log);
        
        leap = false;

        // 用当年的天数offset,逐个减去每月（农历）的天数，求出当天是本月的第几天
        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {
            // 闰月
            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !leap) {
                --iMonth;
                leap = true;
                daysOfMonth = leapDays(year);
            } else
                daysOfMonth = monthDays(year, iMonth);

            offset -= daysOfMonth;
            // 解除闰月
            if (leap && iMonth == (leapMonth + 1))
                leap = false;
            if (!leap)
                monCyl++;
        }
        // offset为0时，并且刚才计算的月份是闰月，要校正
        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (leap) {
                leap = false;
            } else {
                leap = true;
                --iMonth;
                --monCyl;
            }
        }
        // offset小于0时，也要校正
        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;
        }
        month = iMonth;
        setLunarMonth(chineseNumber[month - 1] + "月"); // 设置对应的阴历月份
        day = offset + 1;

        if (!isday) {
            String HolidayVacations = "";
            
            // 如果日期为节假日则阴历日期则返回节假日
            // setLeapMonth(leapMonth);
            for(int i = 0; i < DetailsCalendarActivity.mSolarterm_array.length; i++){
                //返回节气
                String sd = DetailsCalendarActivity.mSolarterm_array[i].split(" ")[0]; // 节假日的日期
                String sdv = DetailsCalendarActivity.mSolarterm_array[i].split(" ")[1]; // 节假日的名称
                String in_year = sd.substring(0, 4);
                if (Integer.parseInt(in_year)+1 < year_log){//迅速定位当前年下的节气 (可能包含去年部分节气)
                    i+=24;
                    continue;
                }
                
                if (Integer.parseInt(in_year) > year_log){//超出定位年份直接跳出循环
                    break;
                }
                
                if (Integer.parseInt(in_year) != year_log){//与迅速定位相关 除去包含的去年部分节气
                    continue;
                }else{
                    if (Integer.parseInt(sd.split("-")[1]) != month_log){
                        continue;
                    }
                }
                
                String smd;

                smd = year_log + "-" + (month_log<10 ? ("0"+month_log) : month_log) + "-" + (day_log<10 ? ("0"+day_log) : day_log);
                if(sd.trim().equals(smd.trim())){
                    if (HolidayVacations == "")
                        HolidayVacations = sdv;
                    else
                        HolidayVacations = HolidayVacations+"\n"+sdv;
                    break;
                }
            }
            
            if (father_day) {
                if (HolidayVacations == "")
                    HolidayVacations = "父亲节";
                else
                    HolidayVacations = HolidayVacations + "\n" + "父亲节";
            }
            
            if (mother_day) {
                if (HolidayVacations == "")
                    HolidayVacations = "母亲节";
                else
                    HolidayVacations = HolidayVacations + "\n" + "母亲节";
            }
            
            for (int i = 0; i < solarHoliday.length; i++) {
                // 返回公历节假日名称
                String sd = solarHoliday[i].split(" ")[0]; // 节假日的日期
                String sdv = solarHoliday[i].split(" ")[1]; // 节假日的名称
                String smonth_v = month_log + "";
                String sday_v = day_log + "";
                String smd = "";
                if (month_log < 10) {
                    smonth_v = "0" + month_log;
                }
                if (day_log < 10) {
                    sday_v = "0" + day_log;
                }
                smd = smonth_v + sday_v;
                if (sd.trim().equals(smd.trim())) {
                    if (HolidayVacations == "")
                        HolidayVacations = sdv;
                    else
                        HolidayVacations = HolidayVacations+"\n"+sdv;
                    //return sdv;
                }
            }
            //Log.e("hmq","jieqi="+ year_log+month_log+day_log+" ;"+jieqi);
            
            for (int i = 0; i < lunarHoliday.length; i++) {
                // 返回农历节假日名称
                String ld = lunarHoliday[i].split(" ")[0]; // 节假日的日期
                String ldv = lunarHoliday[i].split(" ")[1]; // 节假日的名称
                String lmonth_v = month + "";
                String lday_v = day + "";
                String lmd = "";
                if (month < 10) {
                    lmonth_v = "0" + month;
                }
                if (day < 10) {
                    lday_v = "0" + day;
                }
                lmd = lmonth_v + lday_v;
                if (ld.trim().equals(lmd.trim())) {
                    if (HolidayVacations == "")
                        HolidayVacations = ldv;
                    else
                        HolidayVacations = HolidayVacations+"\n"+ldv;
                    //return ldv;
                }
            }
            
            if (HolidayVacations != ""){
                return HolidayVacations;
            }
        }
        JiDay(year_log, month_log,day_log);
        JiMonth(month);
        if (day == 1)
            return chineseNumber[month - 1] + "月";
        else
            return getChinaDayString(day);

    }
    
    /**传入农历的月*/
    private void JiMonth(int month){
        int num = year - 1900 + 36;//年干
        String tiangan = null;
        
        /*这里算月的天支
         * 
         * 若遇甲或己的年份，正月是丙寅；遇上乙或庚之年，正月为戊寅；遇上丙或辛之年，正月为庚寅；遇上丁或壬之年，正月为壬寅；遇上戊或癸之年，正月为甲寅。依照正月之干支，其余月份按干支推算即可*/
        if(((num % 10) == 0 || (num % 10) == 5) && month == 1){/*甲或己*/
            tiangan = JiMonthS[0][0];
        }else if(((num % 10) == 1 || (num % 10) == 6) && month == 1){/*乙或庚*/
            tiangan = JiMonthS[1][0];
        }else if(((num % 10) == 2 || (num % 10) == 7) && month == 1){/*丙或辛*/
            tiangan = JiMonthS[2][0];
        }else if(((num % 10) == 3 || (num % 10) == 8) && month == 1){/*丁或壬*/
            tiangan = JiMonthS[3][0];
        }else if(((num % 10) == 4 || (num % 10) == 9) && month == 1){/*戊或癸*/
            tiangan = JiMonthS[4][0];
        }else{
            if(((num % 10) == 0 || (num % 10) == 5)){
                tiangan = JiMonthS[0][month-1];
            }else if(((num % 10) == 1 || (num % 10) == 6)){
                tiangan = JiMonthS[1][month-1];
            }else if(((num % 10) == 2 || (num % 10) == 7)){
                tiangan = JiMonthS[2][month-1];
            }else if(((num % 10) == 3 || (num % 10) == 8)){
                tiangan = JiMonthS[3][month-1];
            }else if(((num % 10) == 4 || (num % 10) == 9)){
                tiangan = JiMonthS[4][month-1];
            }
        }
        
        JiMonth = tiangan;
        
    }
    
    public String getJiMonth(){
        return JiMonth;
    }

    public String toString() {
        if (chineseNumber[month - 1] == "一" && getChinaDayString(day) == "初一")
            return "农历" + year + "年";
        else if (getChinaDayString(day) == "初一")
            return chineseNumber[month - 1] + "月";
        else
            return getChinaDayString(day);
        // return year + "年" + (leap ? "闰" : "") + chineseNumber[month - 1] +
        // "月" + getChinaDayString(day);
    }


    public int getLeapMonth() {
        return leapMonth;
    }

    public void setLeapMonth(int leapMonth) {
        this.leapMonth = leapMonth;
    }
    
    public boolean isLeap(){
        return leap;
    }

    /**
     * 得到当前日期对应的阴历月份
     * 
     * @return
     */
    public String getLunarMonth() {
        return lunarMonth;
    }

    public void setLunarMonth(String lunarMonth) {
        this.lunarMonth = lunarMonth;
    }

    /**
     * 得到当前年对应的农历年份
     * 
     * @return
     */
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
