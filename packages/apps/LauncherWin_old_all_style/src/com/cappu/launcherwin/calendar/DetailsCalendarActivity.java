package com.cappu.launcherwin.calendar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.calendar.Workspace.ProcessDate;
import com.cappu.launcherwin.calendar.Workspace.TouchFinish;
import com.cappu.launcherwin.widget.CareDatePickerDialog.OnDateSetListener;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;

public class DetailsCalendarActivity extends BasicActivity implements OnClickListener,TouchFinish{

    final static String chineseNumber[] = { "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二" };
    
    private Workspace mWorkspace;
    static SQLiteDatabase mSQLiteDatabase;//数据库
    private final String DATABASE_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/vote";
    private String DATABASE_FILENAME = "db_vote.db";

 // 数据库存储路径
    String filePath = "data/data/com.cappu.launcherwin/databases/calendar.db";
    // 数据库存放的文件夹 data/data/com.main.jh 下面
    String pathStr = "data/data/com.cappu.launcherwin/databases";
    
    String mYear;
    String mMonth;
    String mDay;
    
    String mYearStr;
    String mMonthStr;
    String mDayStr;
    
    Button mMonthCalendar;
    Button mTodatDetails;
    
    private TextView mTopText = null;
    private TextView bottomtext = null;
    
    private Handler mHandler = new Handler();
    
    private LunarCalendar mLunarCalendar = null;
    
    Calendar mCalendar;
    
    private Calendar mLastDate;
    private Calendar mNextDate;
    private Calendar mCurrentDate;
    
    public ProcessDate mProcessDate;
    /*节气保存数组*/
    public static String mSolarterm_array[]; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_main);
        mProcessDate = ProcessDate.SHOW;
        if(mSQLiteDatabase == null){
            //mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(getDatabasePath("calendar.db"), null);
            mSQLiteDatabase = openDatabase(this);
        }
        init();
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                if(Integer.parseInt(mMonth)<10){
                    mMonth = "0"+mMonth;
                }
                if(Integer.parseInt(mDay)<10){
                    mDay = "0"+mDay;
                }
                int matchdate = Integer.parseInt(mYear+mMonth+mDay);
                setCurScreenData(Integer.parseInt(mYear), Integer.parseInt(mMonth),Integer.parseInt(mDay));
            }
        }).run();
        
    }
    
    public static SQLiteDatabase getCalendarSQLite(){
        return mSQLiteDatabase;
    }
 
    /*
     * 打开数据库
     */
    public SQLiteDatabase openDatabase(Context context) {
        File jhPath = new File(filePath);
        // 查看数据库文件是否存在
        if (jhPath.exists()) {
            // 存在则直接返回打开的数据库
            return SQLiteDatabase.openOrCreateDatabase(jhPath, null);
        } else {
            // 不存在先创建文件夹
            File path = new File(pathStr);
            if (path.mkdir()) {
                System.out.println("创建成功");
            } else {
                System.out.println("创建失败");
            }
            try {
                // 得到资源
                AssetManager am = context.getAssets();
                // 得到数据库的输入流
                InputStream is = am.open("calendardata.db");
                // 用输出流写到SDcard上面
                FileOutputStream fos = new FileOutputStream(jhPath);
                // 创建byte数组 用于1KB写一次
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                // 最后关闭就可以了
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                Log.i("HHJ", "错了"+e.toString());
            }
            // 如果没有这个数据库 我们已经把他写到SD卡上了，然后在执行一次这个方法 就可以返回数据库了
            return SQLiteDatabase.openOrCreateDatabase(jhPath, null);
        }
    }
    
    public void setProcessDate(ProcessDate processDate){
        this.mProcessDate = processDate;
    }
    public ProcessDate getProcessDate(){
        return mProcessDate;
    }
    
    public void setCurScreenData(int year,int month,int day) {
        setCurScreenData(-1, year, month, day);
    }
    
    public void setCurScreenData(int changePage, int year,int month,int day) {
        boolean isChagePage = true;
        if(changePage == -1)isChagePage = false;
        int tempMonth;
        int tempDay;
        String tempMonthStr = null;
        String tempDayStr = null;
        /**标题 显示阳历日期*/
        mTopText.setText(year+mYearStr+month+mMonthStr+day+mDayStr+"\t");
        CalendarActivity.mIsStop = false;
        CalendarActivity.mStopScreen = 0;
        if (year == Workspace.DEFAULT_END_YEAR){
            if(month == 12 && day == 31){
                CalendarActivity.mIsStop = true;
                CalendarActivity.mStopScreen = 1;
            }
        }else if (year == Workspace.DEFAULT_START_YEAR){
            if(month == 1 && day == 1){
                CalendarActivity.mIsStop = true;
                CalendarActivity.mStopScreen = -1;
            }
        }
        
        /**当前显示的屏数据*/
        if(!isChagePage){
            View view = mWorkspace.getChildAt(mWorkspace.getCurScreen());
            ViewHolder vh = (ViewHolder) view.getTag();
            tempMonth = mCurrentDate.get(Calendar.MONTH)+1;
            tempDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);
            if(tempMonth<10){
                tempMonthStr = "0"+tempMonth;
            }else{
                tempMonthStr = ""+tempMonth;
            }
            
            if(tempDay<10){
                tempDayStr = "0"+tempDay;
            }else{
                tempDayStr = ""+tempDay;
            }
            
            Details c = getDetails(mCurrentDate.get(Calendar.YEAR)+"-"+tempMonthStr+"-"+tempDayStr,mCurrentDate);
            setViewData(vh, c);
        }
        
        /**上一屏显示的数据*/
        //if(CalendarActivity.mStopScreen == -1){
        if(!isChagePage || (isChagePage && changePage == -1)){
            tempMonth = mLastDate.get(Calendar.MONTH)+1;
            tempDay = mLastDate.get(Calendar.DAY_OF_MONTH);
            if(tempMonth<10){
                tempMonthStr = "0"+tempMonth;
            }else{
                tempMonthStr = ""+tempMonth;
            }
            if(tempDay<10){
                tempDayStr = "0"+tempDay;
            }else{
                tempDayStr = ""+tempDay;
            }
            
            Details lastDetails = getDetails(mLastDate.get(Calendar.YEAR)+"-"+tempMonthStr+"-"+tempDayStr,mLastDate);
            if(mWorkspace.getCurScreen() == 0){
                View lastview = mWorkspace.getChildAt(2);
                ViewHolder lastvh = (ViewHolder) lastview.getTag();
                setViewData(lastvh, lastDetails);
            }else if(mWorkspace.getCurScreen() == 1){
                View lastview = mWorkspace.getChildAt(0);
                ViewHolder lastvh = (ViewHolder) lastview.getTag();
                setViewData(lastvh, lastDetails);
            }else if(mWorkspace.getCurScreen() == 2){
                View lastview = mWorkspace.getChildAt(1);
                ViewHolder lastvh = (ViewHolder) lastview.getTag();
                setViewData(lastvh, lastDetails);
            }
        }
        
        /**下一屏显示的数据*/
        //if(CalendarActivity.mStopScreen == 1){
        if(!isChagePage || (isChagePage && changePage == 1)){
            tempMonth = mNextDate.get(Calendar.MONTH)+1;
            tempDay = mNextDate.get(Calendar.DAY_OF_MONTH);
            if(tempMonth<10){
                tempMonthStr = "0"+tempMonth;
            }else{
                tempMonthStr = ""+tempMonth;
            }
            if(tempDay<10){
                tempDayStr = "0"+tempDay;
            }else{
                tempDayStr = ""+tempDay;
            }
            Details nextDetails = getDetails(mNextDate.get(Calendar.YEAR)+"-"+tempMonthStr+"-"+tempDayStr,mNextDate);
            if(mWorkspace.getCurScreen() == 0){
                View lastview = mWorkspace.getChildAt(1);
                ViewHolder lastvh = (ViewHolder) lastview.getTag();
                setViewData(lastvh, nextDetails);
            }else if(mWorkspace.getCurScreen() == 1){
                View lastview = mWorkspace.getChildAt(2);
                ViewHolder lastvh = (ViewHolder) lastview.getTag();
                setViewData(lastvh, nextDetails);
            }else if(mWorkspace.getCurScreen() == 2){
                View lastview = mWorkspace.getChildAt(0);
                ViewHolder lastvh = (ViewHolder) lastview.getTag();
                setViewData(lastvh, nextDetails);
            }
        }
        
        mProcessDate = ProcessDate.SHOW;
    }
    
    public Details getDetails(String matchdate,Calendar currentDate){
        Cursor cursor = mSQLiteDatabase.query("HL_s", null, " datetime(sDate) = datetime('" + matchdate + "')", null, null, null, null);
        
        Details details = null;
        try {
            final int idIndex = cursor.getColumnIndexOrThrow("ID");
            final int dateIndex = cursor.getColumnIndexOrThrow("sDate");
            final int nYearIndex = cursor.getColumnIndexOrThrow("nYear");
            final int nMonthIndex = cursor.getColumnIndexOrThrow("nMonth");
            final int nDayIndex = cursor.getColumnIndexOrThrow("nDay");
            final int nToDayIndex = cursor.getColumnIndexOrThrow("nToDay");
            final int yIndex = cursor.getColumnIndexOrThrow("Yi");
            final int jIndex = cursor.getColumnIndexOrThrow("Ji");
            final int cIndex = cursor.getColumnIndexOrThrow("Chong");
            final int sIndex = cursor.getColumnIndexOrThrow("Sha");
            final int chIdIndex = cursor.getColumnIndexOrThrow("Cheng");
            final int zcIndex = cursor.getColumnIndexOrThrow("ZhengChong");
            final int tsIndex = cursor.getColumnIndexOrThrow("TaiShen");
            final int jqIndex = cursor.getColumnIndexOrThrow("SolarTerm");

            Log.i("HHJ", "matchdate:" + matchdate+"    cursor:"+cursor.getCount());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);
                String date = cursor.getString(dateIndex);//阳历日期
                String nYear = cursor.getString(nYearIndex);//阴历年
                String nMonth = cursor.getString(nMonthIndex);//阴历月
                String nDay = cursor.getString(nDayIndex);//阴历日
                String nToDay = cursor.getString(nToDayIndex);//农历日期
                String yi = cursor.getString(yIndex);//黄历宜
                String ji = cursor.getString(jIndex);//黄历忌
                String chong = cursor.getString(cIndex);//冲
                String sha = cursor.getString(sIndex);//煞
                String feng = cursor.getString(chIdIndex);//五行
                String zhengchong = cursor.getString(zcIndex);//正冲
                String taishen = cursor.getString(tsIndex);//胎神
                String jieqi = cursor.getString(jqIndex);//节气
                
                details = new Details(id, date, nYear, nMonth, nDay, nToDay, yi, ji, chong, sha, feng, zhengchong, taishen, jieqi, currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH)+1,currentDate.get(Calendar.DAY_OF_MONTH));
                //Log.i("HHJ", "details:" + details.toString());
            }
        } catch (Exception e) {
            Log.i("HHJ", "Exception:" + e.toString());
        }finally{
            cursor.close();
        }
        if(details == null){
            details = new Details(-1, null, null, null, null, null, null, null, null, null, null, null, null, null, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH)+1, currentDate.get(Calendar.DAY_OF_MONTH));
        }
        return details;
    }
    
    public void setViewData(ViewHolder viewHolder,Details details){
        String lunarDay = mLunarCalendar.getLunarDate(details.year, details.month, details.day, details.jieqi, true);
        //Log.i("HHJ", "details.year:"+details.year+"     details.month:"+details.month+"   details.day:"+details.day+"    农历:"+mLunarCalendar.getLunarMonth()+"    lunarDay:"+lunarDay);
        mCalendar.set(details.year, details.month-1, details.day);
        if (lunarDay.substring(1, 2).equals("月")){
            lunarDay = "初一";
        }
        String week = null;
        switch ((mCalendar.get(Calendar.DAY_OF_WEEK) - 1) % 7) {
        case 0:
            week = "星期日";
            break;
        case 1:
            week = "星期一";
            break;
        case 2:
            week = "星期二";
            break;
        case 3:
            week = "星期三";
            break;
        case 4:
            week = "星期四";
            break;
        case 5:
            week = "星期五";
            break;
        case 6:
            week = "星期六";
            break;
        }
        //Log.i("HHJ", "(mCalendar.get(Calendar.DAY_OF_WEEK) - 1):"+(mCalendar.get(Calendar.DAY_OF_WEEK) - 1)+"     week:"+week);
        
        /*这里本来要在农历前面加上 闰 字 的
         * if(!(mLunarCalendar.leapSolarMonth(details.year) == 0)){
            String.valueOf(mLunarCalendar.leapSolarMonth(details.year));
            if(mLunarCalendar.getLunarMonth().substring(0, 1).equals(chineseNumber[mLunarCalendar.leapSolarMonth(details.year)-1])){
                viewHolder.Lunar.setText("农历 润"+mLunarCalendar.getLunarMonth());
            }
            Log.i("HHJ", "这里闰月:"+String.valueOf(mLunarCalendar.leapSolarMonth(details.year))+"    "+chineseNumber[mLunarCalendar.leapSolarMonth(details.year)-1]);
        }*/
        Log.i("HHJ", "mLunarCalendar:"+mLunarCalendar.getLeapMonth()+"   "+mLunarCalendar.isLeap());
//        if(mLunarCalendar.isLeap()){
//            viewHolder.Lunar.setText("农历润"+mLunarCalendar.getLunarMonth());
//        }else{
//            viewHolder.Lunar.setText("农历"+mLunarCalendar.getLunarMonth());
//        }
        if(!TextUtils.isEmpty(details.today)){
            int nongliIndex = details.today.indexOf("月");
            String nongli = details.today.substring(0, nongliIndex+1);
            if(nongli.trim().contains("11"))
                nongli = nongli.replace("11", "冬");
            if(nongli.trim().contains("12"))
                nongli = nongli.replace("12", "腊");
            viewHolder.Lunar.setText(nongli);
        }
        
        viewHolder.Week.setText(week);
        viewHolder.LunarDay.setText(lunarDay);
        //viewHolder.dizhi.setText(mLunarCalendar.cyclical(details.year)+" "+mLunarCalendar.animalsYear(details.year)+getString(R.string.year));
        if(!TextUtils.isEmpty(details.nY)){
            String nShengxiao = details.nY.substring(details.nY.length()-2, details.nY.length()-1);
            Cursor cursor = mSQLiteDatabase.query("AnimalZodiac", null, " nYear_s = '" + nShengxiao + "'", null, null, null, null);
            int index = cursor.getColumnIndexOrThrow("Value_s");
            String yShengxiao="";
            while (cursor.moveToNext()) {
                yShengxiao = cursor.getString(index);
            }
            cursor.close();
            viewHolder.dizhi.setText(details.nY + "("+ yShengxiao + ")");
        }
        //viewHolder.dizhi_month_day.setText(mLunarCalendar.getJiMonth()+getString(R.string.month)+"  "+mLunarCalendar.getJiDay()+getString(R.string.day));
        viewHolder.dizhi_month_day.setText(details.nM+" "+details.nD);
        if(!TextUtils.isEmpty(details.yi)){
            viewHolder.Lunar_yi.setText(details.yi);
        }
        
        if(!TextUtils.isEmpty(details.feng)){
            viewHolder.Wuxing.setText(details.feng+"执位");
        }
        
        if(!TextUtils.isEmpty(details.ji)){
            viewHolder.Lunar_ji.setText(details.ji);
        }
        
        if(!(TextUtils.isEmpty(details.chong) || TextUtils.isEmpty(details.zhengchong) || TextUtils.isEmpty(details.sha))){
            String tostring = "";
            
            if((details.chong).trim().contains("生肖"))
                tostring += (details.chong).replace("生肖", "");
            
            if((details.zhengchong).trim().contains("正冲正冲"))
                tostring += "("+(details.zhengchong).replace("正冲正冲", "")+")";

            if(!TextUtils.isEmpty(details.sha))
                tostring += details.sha;
            
            viewHolder.Ghosts.setText(tostring);
        }
        
        if(!TextUtils.isEmpty(details.taishen)){
            viewHolder.Tai_shen.setText(details.taishen);
        }
        
        if(!TextUtils.isEmpty(details.jieqi)){
            viewHolder.JieQi.setVisibility(View.VISIBLE);
            viewHolder.JieQi.setText(details.jieqi);
        }else{
            viewHolder.JieQi.setVisibility(View.INVISIBLE);
        }
        
    }
    
    public int Day(int year,int month,int date){
        int c, y, day;
        if (month < 3) {
            year -= 1;
            month += 10;
        }
        if (month > 2)
            month -= 2;
        y = year % 10; // 以每年的三月作为第一个月计算，一、二月作为十一、十二月计算
        c = (year - y) / 100;
        day = (date + (13 * month - 1) / 5 + y + y / 4 + c / 4 - 2 * c) % 7;// 引用数论中结论
        while (day < 0)
            day += 7;
        return (day);
    }
    
    class ViewHolder{
        /**农历月*/
        TextView Lunar;
        /**周*/
        TextView Week;
        /**农历日*/
        TextView LunarDay;
        /**地支 年 如壬辰年*/
        TextView dizhi;
        /**地支 年 如甲戌月癸酉日*/
        TextView dizhi_month_day;
        /**宜*/
        TextView Lunar_yi;
        /**忌*/
        TextView Lunar_ji;
        /**五行*/
        TextView Wuxing;
        /**冲煞*/
        TextView Ghosts;
        /**胎神*/
        TextView Tai_shen;
        /**节气*/
        TextView JieQi;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        
        mYear = intent.getStringExtra("year");
        mMonth = intent.getStringExtra("month");
        mDay = intent.getStringExtra("day");
        Calendar c=Calendar.getInstance();
        if(mYear == null || mMonth == null || mDay == null){
            mYear = c.get(Calendar.YEAR)+"";
            mMonth = (c.get(Calendar.MONTH)+1)+"";
            mDay = c.get(Calendar.DAY_OF_MONTH)+"";
        }
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                if(Integer.parseInt(mMonth)<10){
                    mMonth = "0"+mMonth;
                }
                if(Integer.parseInt(mDay)<10){
                    mDay = "0"+mDay;
                }
                int matchdate = Integer.parseInt(mYear+mMonth+mDay);
                setCurScreenData(Integer.parseInt(mYear), Integer.parseInt(mMonth),Integer.parseInt(mDay));
            }
        }).run();
    }
    
    private void init(){
        mYear = getIntent().getStringExtra("year");
        mMonth = getIntent().getStringExtra("month");
        mDay = getIntent().getStringExtra("day");
        
        
        Calendar c=Calendar.getInstance();
        if(mYear == null || mMonth == null || mDay == null){
            mYear = c.get(Calendar.YEAR)+"";
            mMonth = (c.get(Calendar.MONTH)+1)+"";
            mDay = c.get(Calendar.DAY_OF_MONTH)+"";
        }
        
        
        mWorkspace = (Workspace) findViewById(R.id.workspace);
        mWorkspace.setTouchFinish(this);
        mWorkspace.setActivity(this);
        mWorkspace.addView(getView(mWorkspace), 0);
        mWorkspace.addView(getView(mWorkspace), 1);
        mWorkspace.addView(getView(mWorkspace), 2);
        
        mTopText = (TextView) findViewById(R.id.toptext);
        mTopText.setTextColor(Color.WHITE);
        mTopText.setTypeface(Typeface.DEFAULT_BOLD);
        
        bottomtext = (TextView) findViewById(R.id.bottomtext);
        bottomtext.setVisibility(View.GONE);
        
        mYearStr = getString(R.string.year);
        mMonthStr = getString(R.string.month);
        mDayStr = getString(R.string.day);
        
        mLunarCalendar = new LunarCalendar();
        mCalendar = Calendar.getInstance();
        
        mLastDate = Calendar.getInstance();
        mNextDate = Calendar.getInstance();
        mCurrentDate = Calendar.getInstance();
        
        mCurrentDate.set(Integer.parseInt(mYear), Integer.parseInt(mMonth)-1, Integer.parseInt(mDay));
        mNextDate.set(Integer.parseInt(mYear), Integer.parseInt(mMonth)-1, Integer.parseInt(mDay));
        mNextDate.add(Calendar.DAY_OF_MONTH, 1);
        mLastDate.set(Integer.parseInt(mYear), Integer.parseInt(mMonth)-1, Integer.parseInt(mDay));
        mLastDate.add(Calendar.DAY_OF_MONTH, -1);
        
        mMonthCalendar = (Button) findViewById(R.id.topleft);
        mMonthCalendar.setText("月历");
        mTodatDetails  = (Button) findViewById(R.id.topright);
        mMonthCalendar.setOnClickListener(this);
        mTodatDetails.setOnClickListener(this);
        
        if(!isSolarTermArrayExists()){
            String[] columns = {"sDate","SolarTerm"};
            Cursor cursor = mSQLiteDatabase.query("HL_s", columns, " SolarTerm <> \'\'", null, null, null, null);
            try {
                final int dateIndex = cursor.getColumnIndexOrThrow("sDate");
                final int jqIndex = cursor.getColumnIndexOrThrow("SolarTerm");
                mSolarterm_array = new String[cursor.getCount()];
                while (cursor.moveToNext()) {
                    String date = cursor.getString(dateIndex);//阳历日期
                    date = date.substring(0, date.indexOf(" "));
                    String jieqi = cursor.getString(jqIndex);//节气
                    mSolarterm_array[cursor.getPosition()] = date+" "+jieqi;

                }
            } catch (Exception e) {
                Log.e("hmq", "Exception:" + e.toString());
            }finally{
                cursor.close();
            }
        }
    }

    /**
     * edit by hmq
     * this init Chinese calendar solar term
     * To determine whether there is a array
     */
    private boolean isSolarTermArrayExists(){
        if (mSolarterm_array == null)
            return false;
        if (mSolarterm_array.length < 2)
            return false;
        return true;
    }
    
    public View getView(ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.details_calendar, parent, false);
        ViewHolder vh = new ViewHolder();
        vh.Lunar = (TextView) view.findViewById(R.id.Lunar);
        vh.Week = (TextView) view.findViewById(R.id.Week);
        vh.LunarDay = (TextView) view.findViewById(R.id.LunarDay);
        vh.dizhi = (TextView) view.findViewById(R.id.dizhi);
        vh.dizhi_month_day = (TextView) view.findViewById(R.id.dizhi_month_day);
        vh.Lunar_yi = (TextView) view.findViewById(R.id.Lunar_yi);
        vh.Lunar_ji = (TextView) view.findViewById(R.id.Lunar_ji);
        vh.Wuxing = (TextView) view.findViewById(R.id.Wuxing);
        vh.Ghosts = (TextView) view.findViewById(R.id.Ghosts);
        vh.Tai_shen = (TextView) view.findViewById(R.id.Tai_shen);
        vh.JieQi = (TextView) view.findViewById(R.id.jieqi);
        view.setTag(vh);
        return view;
    }
    @Override
    public void upDate(final int about) {
        mHandler.post(new Runnable() {
            public void run() {
                
                mCurrentDate.add(Calendar.DAY_OF_MONTH, about);
                mNextDate.add(Calendar.DAY_OF_MONTH, about);
                mLastDate.add(Calendar.DAY_OF_MONTH, about);
                
                setCurScreenData(about, mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH)+1,mCurrentDate.get(Calendar.DAY_OF_MONTH));
                
                mProcessDate = ProcessDate.SHOW;
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == mMonthCalendar){
            startActivity(new Intent(this, CalendarActivity.class));
            overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
            finish();
        }else if(v == mTodatDetails){
            mHandler.post(new Runnable() {
                public void run() {
                    Calendar c=Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);
                    
                    mCurrentDate.set(year, month, day);
                    
                    mNextDate.set(year, month, day);
                    mNextDate.add(Calendar.DAY_OF_MONTH, 1);
                    
                    mLastDate.set(year, month, day);
                    mLastDate.add(Calendar.DAY_OF_MONTH, -1);
                    
                    setCurScreenData(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH)+1,mCurrentDate.get(Calendar.DAY_OF_MONTH));
                }
            });
        }
    }
    
//    @Override
//    public void onBackPressed() {
//        startActivity(new Intent(this, CalendarActivity.class));
//        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
//        finish();
//    }
    
    class Details{
        public int id;
        public int year;
        public int month;
        public int day;
        
        public String date;
        public String nY;
        public String nM;
        public String nD;
        public String today;
        public String yi;
        public String ji;
        public String chong;
        public String sha;
        public String feng;
        public String zhengchong;
        public String taishen;
        public String jieqi;
        
        public Details(int id, String date, String ny, String nm, String nd, String today, String yi, String ji, String chong, String sha, String feng, String zhengchong, String taishen, String jq, int year, int month, int day){
            this.id = id;
            this.date = date;
            this.nY = ny;
            this.nM = nm;
            this.nD = nd;
            this.today = today;
            this.yi = yi;
            this.ji = ji;
            this.chong = chong;
            this.sha = sha;
            this.feng = feng;
            this.zhengchong = zhengchong;
            this.taishen = taishen;
            this.jieqi = jq;
            this.year = year;
            this.month = month;
            this.day = day;
        }
        @Override
        public String toString() {
            return "Details [id=" + id + ", year=" + year + ", month=" + month + ", day=" + day + ", date=" + date + ", yi=" + yi + ", ji=" + ji
                    + ", chong=" + chong + ", sha=" + sha + ", feng=" + feng + ", zhengchong=" + zhengchong + ", taishen=" + taishen + "]";
        }
        
    }

}