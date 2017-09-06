package com.cappu.launcherwin.calendar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.widget.SpecialCalendar;

import android.R.integer;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 日历gridview中的每一个item显示的textview
 * 
 * @author jack_peng
 * 
 */
public class CalendarView extends BaseAdapter {

    /**是否为闰年*/
    private boolean isLeapyear = false; // 是否为闰年
    /**某月的总天数*/
    private int daysOfMonth = 0; // 某月的天数
    /**某月第一天为星期几*/
    private int dayOfWeek = 0; // 具体某一天是星期几
    /**上一个月的总天数*/
    private int lastDaysOfMonth = 0; // 上一个月的总天数
    private Context mContext;
    
    private boolean[] mCurrentMonth = new boolean[49];// 获取的日期是否为当前月
    /**
     *     将一个月中的每一天对应礼拜 数据存储
     *     dayNumber 从零到六储存的是礼拜日到礼拜六
     * 
     * */
    private String[] dayNumber = new String[49]; // 一个gridview中的日期存入此数组中
    private static String week[] = { "日", "一", "二", "三", "四", "五", "六" };
    private static int weekImg[] = { R.drawable.cal_xqri, R.drawable.cal_xq1, R.drawable.cal_xq2, R.drawable.cal_xq3, R.drawable.cal_xq4, R.drawable.cal_xq5, R.drawable.cal_xq6 };
    
    /**特殊的工具,是否闰年,每月的天数,以及没月的第一天是星期几*/
    private SpecialCalendar mSpecialCalendar = null;
    /**农历工具类*/
    private LunarCalendar mLunarCalendar = null;
    private Resources mResources = null;
    
    /**这个是当前日期选中的图片*/
    private int mDrawableDaySelected = -1;
    private Drawable mDrawableDayUnSelected = null;
    
    /**礼拜的头颜色*/
    private int mDrawableWeek = -1;
    
    /**得到当前的年份*/
    private String currentYear = "";
    /**得到当前的月份*/
    private String currentMonth = "";
    /**得到当前的日*/
    private String currentDay = "";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
    private int currentFlag = -1; // 用于标记当天
    

    private String showYear = ""; // 用于在头部显示的年份
    private String showMonth = ""; // 用于在头部显示的月份
    private String animalsYear = "";
    private String leapMonth = ""; // 闰哪一个月
    private String cyclical = ""; // 天干地支
    // 系统当前时间
    private String sysDate = "";
    private String sys_year = "";
    private String sys_month = "";
    private String sys_day = "";

    // 日程时间(需要标记的日程日期)
    private String sch_year = "";
    private String sch_month = "";
    private String sch_day = "";
    
    private WindowManager mWindowManager;
    private Display mDisplay;
    private int ItemWidth = -1;
    private int ItemHeight = -1;

    /*设置GridView 小格内最小值*/
    private final static int mGridMinHeight = 70;
    
    /*设置周的字体大小*/
    private final static int mWeekTextSize = 23;
    private final static String mWeekBackground = "#EFD8DB";
    private final static String mWeekDayBlack = "#393939";
    private final static String mWeekDayGary = "#737373";
    /*设置日期内字体大小*/
    private final static int mDayTextSize = 15;
/*    public CalendarView() {
        Date date = new Date();
        sysDate = sdf.format(date); // 当期日期
        sys_year = sysDate.split("-")[0];
        sys_month = sysDate.split("-")[1];
        sys_day = sysDate.split("-")[2];

    }*/

    /**
     *   jumpMonth 当前要滑动的月
     *   jumpYear  当前要滑动的年
     *   
     *   year_c    当前年
     *   month_c   当前月
     *   day_c     当前日
     *   
     *   */
    public CalendarView(Context context, Resources rs, int jumpMonth, int jumpYear, int year_c, int month_c, int day_c) {
        /*this();*/
        this.mContext = context;
        this.mResources = rs;
        this.init(jumpYear, jumpMonth, year_c, month_c,day_c);
    }
    
    public void init(int jumpYear,int jumpMonth,int year_c,int month_c,int day_c){
        Log.e("hmq","init jumpYear"+jumpYear+";jumpMonth="+jumpMonth+";year_c="+year_c+";month_c="+month_c+";day_c="+day_c);
        if(mSpecialCalendar == null){
            mSpecialCalendar = new SpecialCalendar();
        }
        
        if(mWindowManager == null){
            mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
        
        if(mDisplay == null){
            mDisplay = mWindowManager.getDefaultDisplay();
        }
        
        if(ItemWidth == -1){
            ItemWidth = mDisplay.getWidth() / 7;
        }
        
        if(ItemHeight == -1){
            ItemHeight = mDisplay.getHeight()/8;
        }
        
        if(mLunarCalendar == null){
            mLunarCalendar = new LunarCalendar();
        }

        if(mDrawableDaySelected == -1){
            mDrawableDaySelected = R.drawable.widget_today_bg;
        }
        
        if(mDrawableWeek == -1){
            mDrawableWeek = Color.parseColor(mWeekBackground);
        }
        
        int stepYear = year_c + jumpYear;
        int stepMonth = month_c + jumpMonth;
        if (stepMonth > 0) {
            // 往下一个月滑动
            if (stepMonth % 12 == 0) {
                stepYear = year_c + stepMonth / 12 - 1;
                stepMonth = 12;
            } else {
                stepYear = year_c + stepMonth / 12;
                stepMonth = stepMonth % 12;
            }
        } else {
            // 往上一个月滑动
            stepYear = year_c - 1 + stepMonth / 12;
            stepMonth = stepMonth % 12 + 12;
            if (stepMonth % 12 == 0) {

            }
        }
        updateCurrenDate(stepYear, stepMonth, day_c);
        /*currentYear = String.valueOf(stepYear);
        currentMonth = String.valueOf(stepMonth); // 得到本月= // （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
        currentDay = String.valueOf(day_c); // 得到当前日期是哪天
        getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));*/
    }
    
    public void moveNextMonth(){
        int stepYear = Integer.parseInt(getShowYear());
        int stepMonth = Integer.parseInt(getShowMonth()) + 1;
        int day_c = Integer.parseInt(currentDay);
        int year_c = stepYear;
        Log.i("HHJ", "moveNextMonth stepYear:"+stepYear+"   stepMonth:"+stepMonth+"   day_c:"+day_c+"    year_c:"+year_c);
        if (stepMonth > 0) {
            // 往下一个月滑动
            if (stepMonth % 12 == 0) {
                stepYear = year_c + stepMonth / 12 - 1;
                stepMonth = 12;
            } else {
                stepYear = year_c + stepMonth / 12;
                stepMonth = stepMonth % 12;
            }
        } else {
            // 往上一个月滑动
            stepYear = year_c - 1 + stepMonth / 12;
            stepMonth = stepMonth % 12 + 12;
            if (stepMonth % 12 == 0) {

            }
        }
        
        updateCurrenDate(stepYear, stepMonth, day_c);
    }
    
    public void moveLastMonth(){

        Log.i("HHJ", "当前 moveLastMonth getShowYear():"+getShowYear()+"   getShowMonth():"+getShowMonth()+"   currentDay:"+currentDay);
        int stepYear = Integer.parseInt(getShowYear());
        int stepMonth = Integer.parseInt(getShowMonth()) - 1;
        int day_c = Integer.parseInt(currentDay);
        int year_c = stepYear;
        Log.i("HHJ", "moveLastMonth stepYear:"+stepYear+"   stepMonth:"+stepMonth+"   day_c:"+day_c+"    year_c:"+year_c);
        if (stepMonth > 0) {
            // 往下一个月滑动
            if (stepMonth % 12 == 0) {
                stepYear = year_c + stepMonth / 12 - 1;
                stepMonth = 12;
            } else {
                stepYear = year_c + stepMonth / 12;
                stepMonth = stepMonth % 12;
            }
        } else {
            // 往上一个月滑动
            stepYear = year_c - 1 + stepMonth / 12;
            stepMonth = stepMonth % 12 + 12;
            if (stepMonth % 12 == 0) {

            }
        }
        updateCurrenDate(stepYear, stepMonth, day_c);
    
    }
    
    Date mDate;
    public void updateCurrenDate(int year, int month,int day){
        //Log.i("HHJ", "updateCurrenDate year:"+year+"   month:"+month+"   day:"+day);
        if(mDate == null){
            mDate = new Date();
            sysDate = sdf.format(mDate); // 当期日期
            sys_year = sysDate.split("-")[0];
            sys_month = sysDate.split("-")[1];
            sys_day = sysDate.split("-")[2];
        }
        
        currentYear = String.valueOf(year);
        currentMonth = String.valueOf(month); // 得到跳转到的月份
        currentDay = String.valueOf(day); // 得到跳转到的天
        Log.e("hmq","02 year="+currentYear+";month="+currentMonth+"\t\t\t updateCurrenDate-getCalendar");
        getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));
    }

    public CalendarView(Context context, Resources rs, int year, int month, int day) {
        /*this();*/
        this.mContext = context;
        mSpecialCalendar = new SpecialCalendar();
        mLunarCalendar = new LunarCalendar();
        this.mResources = rs;
        Log.e("hmq","01 year="+year+";month="+month+";day="+day+"\t\t\t CalendarView-updateCurrenDate");
        updateCurrenDate(year, month, day);
/*        currentYear = String.valueOf(year);
        ; // 得到跳转到的年份
        currentMonth = String.valueOf(month); // 得到跳转到的月份
        currentDay = String.valueOf(day); // 得到跳转到的天

        getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));*/

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return dayNumber.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
    LayoutInflater vi = null;
    ViewHolder mViewHolder;
    StyleSpan mStyleSpan;
    RelativeSizeSpan mRelativeSizeSpanMin;
    RelativeSizeSpan mRelativeSizeSpanMax;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.calendar, parent,false);
            mViewHolder = new ViewHolder();
            mViewHolder.mRootView = (RelativeLayout) convertView.findViewById(R.id.rootview);
            mViewHolder.mTextView = (TextView) convertView.findViewById(R.id.tvtext);
            mViewHolder.mLunarText = (TextView) convertView.findViewById(R.id.lunar_text);
            mViewHolder.mToDayImg = (ImageView) convertView.findViewById(R.id.icon);
            mViewHolder.mWeekTittle = (ImageView) convertView.findViewById(R.id.weekTittle);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        
        if(ItemWidth == -1){
            ItemWidth = parent.getWidth();
        }
        if(ItemHeight == -1){
            ItemHeight = parent.getHeight();
        }
        
        if(position < 7){
            convertView.setMinimumHeight(mGridMinHeight);
        }else{
            convertView.setMinimumWidth(ItemWidth);
            convertView.setMinimumHeight(ItemHeight);
            
            //Log.i("HHJ", "ItemWidth:"+ItemWidth+"    ItemHeight:"+ItemHeight);
        }
        
        if(mStyleSpan == null){
            mStyleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        }
        
        if(mRelativeSizeSpanMin == null){
            mRelativeSizeSpanMin = new RelativeSizeSpan(1.2f);
        }
        
        if(mRelativeSizeSpanMax == null){
            mRelativeSizeSpanMax = new RelativeSizeSpan(0.75f);
        }
        
        String d = "";
        String dv = "";
        if(mCurrentMonth[position]){//只显示当月日期其他都为空
            d = dayNumber[position].split("\\.")[0];//日期
            dv = dayNumber[position].split("\\.")[1];//农历
            
            SpannableString sp = new SpannableString(d + "\n" + dv);
            sp.setSpan(mStyleSpan, 0, d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sp.setSpan(mRelativeSizeSpanMin, 0, d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (dv != null || dv != "") {
                //sp.setSpan(mRelativeSizeSpanMax, d.length() + 1, dayNumber[position].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mViewHolder.mTextView.setText(d);
            mViewHolder.mLunarText.setText(dv);
        }
        //mViewHolder.mTextView.setTextColor(Color.GRAY);
        if (position < 7) {
         // 设置标题周
            mViewHolder.mWeekTittle.setImageResource(weekImg[position]);
            mViewHolder.mTextView.setVisibility(View.GONE);
            mViewHolder.mLunarText.setVisibility(View.GONE);
            mViewHolder.mRootView.setBackgroundColor(mDrawableWeek);
            
//            mViewHolder.mTextView.setText(d);
//            if(position%6 == 0 ){
//                mViewHolder.mTextView.setTextColor(Color.RED);
//            }else{
//                mViewHolder.mTextView.setTextColor(Color.BLACK);
//            }
//            mViewHolder.mTextView.setTextSize(mWeekTextSize);
//            mViewHolder.mLunarText.setVisibility(View.GONE);
//            
        }

        if (position < daysOfMonth + dayOfWeek + 7 && position >= dayOfWeek + 7) {
            // 当前月信息显示
            
            if((position)%7 == 0 || (position+1)%7 == 0 ){
                mViewHolder.mTextView.setTextColor(Color.RED);
                mViewHolder.mLunarText.setTextColor(Color.RED);
            }else{
                mViewHolder.mTextView.setTextColor(Color.parseColor(mWeekDayBlack));
                mViewHolder.mLunarText.setTextColor(Color.parseColor(mWeekDayGary));
            }
        }
        
        if(!mCurrentMonth[position]){
            mViewHolder.mTextView.setTextColor(Color.GRAY);
            //mViewHolder.mTextView.setBackgroundColor(0xFFFF0000);
        }
        
        if (currentFlag == position) {
            // 设置当天的背景
            mViewHolder.mToDayImg.setVisibility(View.VISIBLE);
            mViewHolder.mToDayImg.setImageResource(mDrawableDaySelected);
            //mViewHolder.mTextView.setTextColor(Color.BLACK);
        }else{
            mViewHolder.mToDayImg.setVisibility(View.GONE);
        }
        return convertView;
        
    }
    
    static class ViewHolder{
        public RelativeLayout mRootView;
        public TextView mTextView;
        public TextView mLunarText;
        public ImageView mToDayImg;
        public ImageView mWeekTittle;
    }

    /**得到某年的某月的天数且这月的第一天是星期几*/
    public void getCalendar(int year, int month) {
        isLeapyear = mSpecialCalendar.isLeapYear(year); // 是否为闰年
        daysOfMonth = mSpecialCalendar.getDaysOfMonth(isLeapyear, month); // 某月的总天数
        dayOfWeek = mSpecialCalendar.getWeekdayOfMonth(year, month); // 某月第一天为星期几
        lastDaysOfMonth = mSpecialCalendar.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
        Log.e("hmq","03 year="+year+";month="+month+"\t\t\t getCalendar-getweek");
        getweek(year, month);
    }

    
    /**将一个月中的每一天的值添加入数组dayNuber中*/
    private void getweek(int year, int month) {
        int j = 1;
        int flag = 0;
        String lunarDay = "";
        currentFlag = -1;
        
        for (int i = 0; i < dayNumber.length; i++) {
            // 周一
            if (i < 7) {   /**这是第一排数据是周日到周六*/
                dayNumber[i] = week[i] + "." + " ";
                mCurrentMonth[i] = true;
            } else if (i < dayOfWeek + 7) { /**这里存储的前一个月数据,因为你 7+dayOfWeek 前面为空位,固然储存里上一个月数据*/
                int temp = lastDaysOfMonth - dayOfWeek + 1 - 7;
                lunarDay = mLunarCalendar.getLunarDate(year, month - 1, temp + i, false);
                dayNumber[i] = (temp + i) + "." + lunarDay;
                mCurrentMonth[i] = false;
            } else if (i < daysOfMonth + dayOfWeek + 7) { /**本月数据*/
                String day = String.valueOf(i - dayOfWeek + 1 - 7); // 得到的日期
                lunarDay = mLunarCalendar.getLunarDate(year, month, i - dayOfWeek + 1 - 7, false);
                dayNumber[i] = i - dayOfWeek + 1 - 7 + "." + lunarDay;
                mCurrentMonth[i] = true;
                // 对于当前月才去标记当前日期
                if (sys_year.equals(String.valueOf(year)) && sys_month.equals(String.valueOf(month)) && sys_day.equals(day)) {
                    // 笔记当前日期
                    currentFlag = i;
                }
                setShowYear(String.valueOf(year));
                setShowMonth(String.valueOf(month));
                setAnimalsYear(mLunarCalendar.animalsYear(year));
                
                /**下面这里这个方法是为了防止从闰年滑到非闰年会出现润字的情况   特别注意*/
                //setLeapMonth(mLunarCalendar.leapMonth == 0 ? "" : String.valueOf(mLunarCalendar.leapMonth));
                setLeapMonth(mLunarCalendar.leapSolarMonth(year) == 0 ? "":String.valueOf(mLunarCalendar.leapSolarMonth(year)));
                /**以上这里这个方法是为了防止从闰年滑到非闰年会出现润字的情况   特别注意*/
                
                setCyclical(mLunarCalendar.cyclical(year));
            } else { // 下一个月
                lunarDay = mLunarCalendar.getLunarDate(year, month + 1, j, false);
                dayNumber[i] = j + "." + lunarDay;
                mCurrentMonth[i] = false;
                j++;
            }
        }

    }
    
    public void matchScheduleDate(int year, int month, int day) {

    }

    /**
     * 点击每一个item时返回item中的日期
     * 
     * @param position
     * @return
     */
    public String getDateByClickItem(int position) {
        return dayNumber[position];
    }

    /**
     * 在点击gridView时，得到这个月中第一天的位置
     * 
     * @return
     */
    public int getStartPositon() {
        return dayOfWeek + 7;
    }

    /**
     * 在点击gridView时，得到这个月中最后一天的位置
     * 
     * @return
     */
    public int getEndPosition() {
        return (dayOfWeek + daysOfMonth + 7) - 1;
    }

    public String getShowYear() {
        return showYear;
    }

    public void setShowYear(String showYear) {
        this.showYear = showYear;
    }

    public String getShowMonth() {
        return showMonth;
    }

    public void setShowMonth(String showMonth) {
        this.showMonth = showMonth;
    }

    public String getAnimalsYear() {
        return animalsYear;
    }

    /**设置生肖年*/
    public void setAnimalsYear(String animalsYear) {
        this.animalsYear = animalsYear;
    }

    public String getLeapMonth() {
        return leapMonth;
    }

    public void setLeapMonth(String leapMonth) {
        this.leapMonth = leapMonth;
    }

    public String getCyclical() {
        return cyclical;
    }

    public void setCyclical(String cyclical) {
        this.cyclical = cyclical;
    }
}
