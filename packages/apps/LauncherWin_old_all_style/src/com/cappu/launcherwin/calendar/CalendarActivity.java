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
import com.cappu.launcherwin.calendar.Workspace.TouchFinish;
import com.cappu.launcherwin.widget.CareDatePicker;
import com.cappu.launcherwin.widget.CareDatePickerDialog;
import com.cappu.launcherwin.widget.CareDatePickerDialog.OnDateSetListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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

public class CalendarActivity extends BasicActivity implements OnDateSetListener,OnClickListener,TouchFinish{

    private Workspace mWorkspace;
    /**当前月的 日历的 Adapter*/
    private CalendarView mCalendarView = null;
    /**下一个月的 日历的 Adapter*/
    private CalendarView mNextCalendarView = null;
    /**上一个月的 日历的 Adapter*/
    private CalendarView mLastCalendarView = null;
    
    private GridView mGridView = null;
    private GridView mLastGridView = null;
    private GridView mNextGridView = null;
    
    private TextView mTopText = null;
    private TextView mBottomText = null;
    
    Button JumpTodayDate;
    Button JumpDate;
    
    private int year_c = 0;
    private int month_c = 0;
    private int day_c = 0;
    /**当期日期*/
    private String mCurrentDate = "";
    
    private final static SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-M-d");
    
    String mYear;
    String mMonth;
    boolean debug = false;
    
    private final static String gridViewBG = "#ffffff";//grid vew line color
    
    public static int mStopScreen = 0;
    public static boolean mIsStop = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_main);
        init();
        addTextToTopTextView();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mStopScreen = 0;
        mIsStop = false;
        
        CalendarView calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(mWorkspace.getCurScreen())).getAdapter();
        String year = calendarView.getShowYear();
        String month = calendarView.getShowMonth();
        if (Integer.parseInt(year) == Workspace.DEFAULT_END_YEAR) {
            if (Integer.parseInt(month) == 12) {
                mIsStop = true;
                mStopScreen = 1;
            }
        } else if (Integer.parseInt(year) == Workspace.DEFAULT_START_YEAR) {
            if (Integer.parseInt(month) == 1) {
                mIsStop = true;
                mStopScreen = -1;
            }
        }
    }
    
    private void init(){
        mYear = getString(R.string.year);
        mMonth = getString(R.string.month);
        
        mTopText = (TextView) findViewById(R.id.toptext);
        mBottomText = (TextView) findViewById(R.id.bottomtext);
        
        mTopText.setTextColor(Color.WHITE);
        mTopText.setTypeface(Typeface.DEFAULT_BOLD);
        mTopText.setGravity(Gravity.CENTER);
        
        JumpTodayDate = (Button) findViewById(R.id.topright);
        JumpDate  = (Button) findViewById(R.id.topleft);
        JumpTodayDate.setOnClickListener(this);
        JumpDate.setOnClickListener(this);
        
        mBottomText.setTextColor(Color.WHITE);
        mBottomText.setTypeface(Typeface.DEFAULT_BOLD);
        
        mWorkspace = (Workspace) findViewById(R.id.workspace);
        mWorkspace.setTouchFinish(this);
        
        Date date = new Date();
        mCurrentDate = mSimpleDateFormat.format(date); // 当期日期
        year_c = Integer.parseInt(mCurrentDate.split("-")[0]);
        month_c = Integer.parseInt(mCurrentDate.split("-")[1]);
        day_c = Integer.parseInt(mCurrentDate.split("-")[2]);
        
        /*初始化三个adapter*/
        mCalendarView = new CalendarView(this, getResources(), 0, 0, year_c, month_c, day_c);
        mNextCalendarView = new CalendarView(this, getResources(), 1, 0, year_c, month_c, day_c);
        mLastCalendarView = new CalendarView(this, getResources(), -1, 0, year_c, month_c, day_c);
        
        /*初始化三个GridView*/
        mGridView = InitGridView();
        mLastGridView = InitGridView();
        mNextGridView = InitGridView();
        
        /*将三个GridView加上adapter*/
        mGridView.setAdapter(mCalendarView);
        //flipper.addView(mGridView, 0);
        mLastGridView.setAdapter(mLastCalendarView);
        mNextGridView.setAdapter(mNextCalendarView);
        
        
        mWorkspace.addView(mGridView,0);
        mWorkspace.addView(mNextGridView,1);
        mWorkspace.addView(mLastGridView,2);
        
        mStopScreen = 0;
        mIsStop = false;
        if (year_c == Workspace.DEFAULT_END_YEAR) {
            if (month_c == 12) {
                mIsStop = true;
                mStopScreen = 1;
            }
        } else if (year_c == Workspace.DEFAULT_START_YEAR) {
            if (month_c == 1) {
                mIsStop = true;
                mStopScreen = -1;
            }
        }
    }
    
    // 添加头部的年份 闰哪月等信息
    public void addTextToTopTextView() {
        CalendarView calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(mWorkspace.getCurScreen())).getAdapter();
        String str = calendarView.getShowYear()+mYear+calendarView.getShowMonth()+mMonth+"\t";
        
        if (!calendarView.getLeapMonth().equals("") && calendarView.getLeapMonth() != null) {
            str+= "闰"+calendarView.getLeapMonth()+mMonth+"\t\n";
        }
        mTopText.setText(str);
        mBottomText.setText(calendarView.getAnimalsYear()+mYear+"("+calendarView.getCyclical()+mYear+")");
    }
    

    // 添加gridview
    private GridView InitGridView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        // 取得屏幕的宽度和高度
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int Width = display.getWidth();
        int Height = display.getHeight();

        GridView gridView = new GridView(this);
        gridView.setNumColumns(7);
        gridView.setColumnWidth(Width/7);
        
        Log.i("HHJ", "display.getHeight()/8:"+display.getHeight()/8+"    display:"+display.getWidth()+"     display:"+display.getHeight());

        /*if (Width == 480 && Height == 800) {
            gridView.setColumnWidth(69);
        }*/
        gridView.setGravity(Gravity.CENTER_VERTICAL);
        gridView.setSelector(new ColorDrawable(Color.BLACK)); // 去除gridView边框
        
        /**
         * 下面两个是加分割线*/
        //gridView.setVerticalSpacing(1);
        //gridView.setHorizontalSpacing(1);
        //gridView.setBackgroundColor(Color.parseColor(gridViewBG));

        gridView.setOnItemClickListener(new OnItemClickListener() {
            // gridView中的每一个item的点击事件
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                // 点击任何一个item，得到这个item的日期(排除点击的是周日到周六(点击不响应))
                mCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(mWorkspace.getCurScreen())).getAdapter();
                int startPosition = mCalendarView.getStartPositon();
                int endPosition = mCalendarView.getEndPosition();
                if (startPosition <= position && position <= endPosition) {
                    String scheduleDay = mCalendarView.getDateByClickItem(position).split("\\.")[0]; // 这一天的阳历
                    String scheduleLunarDay =mCalendarView.getDateByClickItem(position).split("\\.")[1];
                    // //这一天的阴历
                    String scheduleYear = mCalendarView.getShowYear();
                    String scheduleMonth = mCalendarView.getShowMonth();
                    String week = "";
                        // 得到这一天是星期几
                        switch (position % 7) {
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

                        Intent intent = new Intent(CalendarActivity.this, DetailsCalendarActivity.class);
                        intent.putExtra("year", (scheduleYear));
                        intent.putExtra("month", (scheduleMonth));
                        intent.putExtra("day", (scheduleDay));
                        startActivity(intent);
                }
            }
        });
        gridView.setLayoutParams(params);
        return gridView;
    }
    
    @Override
    public void upDate(int about) {
        CalendarView[] screen=debug ? new CalendarView[3] : null;
        
        /**在这里得到的calendarView   可能是mCalendarView，mNextCalendarView，mLastCalendarView这三个当中的一个/我们只要替换当前是前往下一个月还是上一个月即可*/
        CalendarView calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(mWorkspace.getCurScreen())).getAdapter();
        mCalendarView = calendarView;
        mStopScreen = 0;
        mIsStop = false;
        if(about == 1){
            if (Integer.parseInt(mCalendarView.getShowYear()) == Workspace.DEFAULT_END_YEAR){
                if(Integer.parseInt(mCalendarView.getShowMonth()) == 12){
                    mIsStop = true;
                    mStopScreen = 1;
                }
            }
            if(mWorkspace.getCurScreen() == 0 && !mIsStop){
                calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(1)).getAdapter();
                calendarView.init(0, 1, Integer.parseInt(mCalendarView.getShowYear()), Integer.parseInt(mCalendarView.getShowMonth()), day_c);
                GridView NextGridView = (GridView) mWorkspace.getChildAt(1);
                NextGridView.setAdapter(calendarView);
                
            }else if(mWorkspace.getCurScreen() == 1 && !mIsStop){
                calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(2)).getAdapter();
                calendarView.init(0, 1, Integer.parseInt(mCalendarView.getShowYear()), Integer.parseInt(mCalendarView.getShowMonth()), day_c);
                GridView NextGridView = (GridView) mWorkspace.getChildAt(2);
                NextGridView.setAdapter(calendarView);
                
            }else if(mWorkspace.getCurScreen() == 2 && !mIsStop){
                calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(0)).getAdapter();
                calendarView.init(0, 1, Integer.parseInt(mCalendarView.getShowYear()), Integer.parseInt(mCalendarView.getShowMonth()), day_c);
                GridView NextGridView = (GridView) mWorkspace.getChildAt(0);
                NextGridView.setAdapter(calendarView);
            }
            if (debug)Log.e("hmq","upDate="+about);
            if (debug)LogShow(screen);
        }else if(about == -1){
            if (Integer.parseInt(mCalendarView.getShowYear()) == Workspace.DEFAULT_START_YEAR){
                if(Integer.parseInt(mCalendarView.getShowMonth()) == 1){
                    mIsStop = true;
                    mStopScreen = -1;
                }
            }
            if(mWorkspace.getCurScreen() == 0 && !mIsStop){
                calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(2)).getAdapter();
                calendarView.init(0, -1, Integer.parseInt(mCalendarView.getShowYear()), Integer.parseInt(mCalendarView.getShowMonth()), day_c);
                mLastGridView = (GridView) mWorkspace.getChildAt(2);
                mLastGridView.setAdapter(calendarView);
                
            }else if(mWorkspace.getCurScreen() == 1 && !mIsStop){
                calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(0)).getAdapter();
                calendarView.init(0, -1, Integer.parseInt(mCalendarView.getShowYear()), Integer.parseInt(mCalendarView.getShowMonth()), day_c);
                mLastGridView = (GridView) mWorkspace.getChildAt(0);
                mLastGridView.setAdapter(calendarView);
                
            }else if(mWorkspace.getCurScreen() == 2 && !mIsStop){
                calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(1)).getAdapter();
                calendarView.init(0, -1, Integer.parseInt(mCalendarView.getShowYear()), Integer.parseInt(mCalendarView.getShowMonth()), day_c);
                mLastGridView = (GridView) mWorkspace.getChildAt(1);
                mLastGridView.setAdapter(calendarView);
            }
            if (debug)Log.e("hmq","upDate="+about);
            if (debug)LogShow(screen);
        }
        
        
        addTextToTopTextView();
    }

    @Override
    public void onClick(View v) {
        if(v == JumpTodayDate){
//            mCalendarView.init(0, 0, year_c, month_c, day_c);
//            mNextCalendarView.init(0, 1, year_c, month_c, day_c);
//            mLastCalendarView.init(0, -1, year_c, month_c, day_c);
            CalendarView[] screen=debug ? new CalendarView[3] : null;
            CalendarView calendarView;
            String curYearMonth, jumpYearMonth;
            calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(mWorkspace.getCurScreen())).getAdapter();
            curYearMonth = calendarView.getShowYear()+(calendarView.getShowMonth().length()>1 ? calendarView.getShowMonth(): "0"+calendarView.getShowMonth());
            jumpYearMonth = String.valueOf(year_c) + (String.valueOf(month_c).length()>1 ? String.valueOf(month_c): "0"+String.valueOf(month_c));

            if(mWorkspace.getCurScreen() == 0){
                Log.e("hmq","JumpTodayDate scrren=0 \t"+curYearMonth +" "+ jumpYearMonth);
                if (debug)LogShow(screen);
                if(Integer.parseInt(curYearMonth) > Integer.parseInt(jumpYearMonth)){
                    setAdapter2Grid(calendarView, 2, 0, 0);
                    mWorkspace.snapToPage(2);
                    setAdapter2Grid(calendarView, 1, 0, -1);
                    setAdapter2Grid(calendarView, 0, 0, 1);
                }else if(Integer.parseInt(curYearMonth) < Integer.parseInt(jumpYearMonth)){
                    setAdapter2Grid(calendarView, 1, 0, 0);
                    mWorkspace.snapToPage(mWorkspace.getCurScreen()+1);
                    setAdapter2Grid(calendarView, 0, 0, -1);
                    setAdapter2Grid(calendarView, 2, 0, 1);
                }
            }else if(mWorkspace.getCurScreen() == 1){
                Log.e("hmq","JumpTodayDate scrren=1 \t"+curYearMonth +" "+ jumpYearMonth);
                if (debug)LogShow(screen);
                if(Integer.parseInt(curYearMonth) > Integer.parseInt(jumpYearMonth)){
                    setAdapter2Grid(calendarView, 0, 0, 0);
                    mWorkspace.snapToPage(mWorkspace.getCurScreen()-1);
                    setAdapter2Grid(calendarView, 2, 0, -1);
                    setAdapter2Grid(calendarView, 1, 0, 1);
                }else if(Integer.parseInt(curYearMonth) < Integer.parseInt(jumpYearMonth)){
                    setAdapter2Grid(calendarView, 2, 0, 0);
                    mWorkspace.snapToPage(mWorkspace.getCurScreen()+1);
                    setAdapter2Grid(calendarView, 1, 0, -1);
                    setAdapter2Grid(calendarView, 0, 0, 1);
                }
            }else if(mWorkspace.getCurScreen() == 2){
                Log.e("hmq","JumpTodayDate scrren=2 \t"+curYearMonth +" "+ jumpYearMonth);
                if (debug)LogShow(screen);
                if(Integer.parseInt(curYearMonth) > Integer.parseInt(jumpYearMonth)){
                    setAdapter2Grid(calendarView, 1, 0, 0);
                    mWorkspace.snapToPage(mWorkspace.getCurScreen()-1);
                    setAdapter2Grid(calendarView, 0, 0, -1);
                    setAdapter2Grid(calendarView, 2, 0, 1);
                }else if(Integer.parseInt(curYearMonth) < Integer.parseInt(jumpYearMonth)){
                    setAdapter2Grid(calendarView, 0, 0, 0);
                    mWorkspace.snapToPage(0);
                    setAdapter2Grid(calendarView, 2, 0, -1);
                    setAdapter2Grid(calendarView, 1, 0, 1);
                }
            }
            addTextToTopTextView();
        } else if (v == JumpDate) {
            Log.i("hmq", "year_c:"+year_c+"   month_c-1:"+(month_c-1));
            CareDatePickerDialog careDatePickerDialog = new CareDatePickerDialog(CalendarActivity.this, year_c, month_c-1,day_c);
            careDatePickerDialog.setOnDateSetListener(this);
            careDatePickerDialog.show();
        } 
        
    }

    private void setAdapter2Grid(CalendarView calendarView, int setChildIndex, int isjumpYear, int isjumpMonth){
        calendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(setChildIndex)).getAdapter();
        calendarView.init(isjumpYear, isjumpMonth, year_c, month_c, day_c);
        mGridView = (GridView) mWorkspace.getChildAt(setChildIndex);
        mGridView.setAdapter(calendarView);
    }
    
    private void LogShow(CalendarView[] screen){
        CalendarView aa = (CalendarView) ((GridView) mWorkspace.getChildAt(mWorkspace.getCurScreen())).getAdapter();
        screen[0] = (CalendarView) ((GridView) mWorkspace.getChildAt(0)).getAdapter();
        screen[1] = (CalendarView) ((GridView) mWorkspace.getChildAt(1)).getAdapter();
        screen[2] = (CalendarView) ((GridView) mWorkspace.getChildAt(2)).getAdapter();
        for (int i = 0; i < screen.length; i++) {
            if (screen[i] == mNextCalendarView) {
                Log.e("hmq", "\t\t screen=" + i + "; adapter=mNextCalendarView; month=" + screen[i].getShowMonth());
            } else if (screen[i] == mCalendarView) {
                Log.e("hmq", "\t\t screen=" + i + "; adapter=mCalendarView; month=" + screen[i].getShowMonth());
            } else if (screen[i] == mLastCalendarView) {
                Log.e("hmq", "\t\t screen=" + i + "; adapter=mLastCalendarView; month=" + screen[i].getShowMonth());
            } else {
                Log.e("hmq", "error \t screen=" + i + ";month=" + screen[i].getShowMonth());
            }

        }
        Log.e("hmq","\t\t screen="+mWorkspace.getCurScreen()+"; year="+aa.getShowYear()+"; month="+aa.getShowMonth());
    }
    
    @Override
    public void onDateSet(CareDatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mStopScreen = 0;
        mIsStop = false;
        Log.e("hmq", "onDateSet year:"+year+"   monthOfYear:"+(monthOfYear)+"     dayOfMonth:"+dayOfMonth);
//        mCalendarView.init(0, 0, year, monthOfYear+1, dayOfMonth);
//        mNextCalendarView.init(0, 1, year, monthOfYear+1, dayOfMonth);
//        mLastCalendarView.init(0, -1, year, monthOfYear+1, dayOfMonth);
        if (year == Workspace.DEFAULT_END_YEAR){
            if((monthOfYear + 1) == 12){
                mIsStop = true;
                mStopScreen = 1;
            }
        }
        if (year == Workspace.DEFAULT_START_YEAR){
            if((monthOfYear + 1) == 1){
                mIsStop = true;
                mStopScreen = -1;
            }
        }
        if(mWorkspace.getCurScreen() == 0){
            if(mStopScreen != -1){
                mLastCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(2)).getAdapter();
                mLastCalendarView.init(0, -1, year, monthOfYear+1, dayOfMonth);
                GridView LastGridView = (GridView) mWorkspace.getChildAt(2);
                LastGridView.setAdapter(mLastCalendarView);
            }
            
            if(mStopScreen != 1){
                mNextCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(1)).getAdapter();
                mNextCalendarView.init(0, 1, year, monthOfYear+1, dayOfMonth);
                GridView mNextGridView = (GridView) mWorkspace.getChildAt(1);
                mNextGridView.setAdapter(mNextCalendarView);
            }
            
            mCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(0)).getAdapter();
            mCalendarView.init(0, 0, year, monthOfYear+1, dayOfMonth);
            mGridView = (GridView) mWorkspace.getChildAt(0);
            mGridView.setAdapter(mCalendarView);
//            mLastGridView = (GridView) mWorkspace.getChildAt(2);
//            mLastGridView.setAdapter(mLastCalendarView);
//            
//            mNextGridView = (GridView) mWorkspace.getChildAt(1);
//            mNextGridView.setAdapter(mNextCalendarView);
//            
//            mGridView = (GridView) mWorkspace.getChildAt(0);
//            mGridView.setAdapter(mCalendarView);
        }else if(mWorkspace.getCurScreen() == 1){
            if(mStopScreen != -1){
                mLastCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(0)).getAdapter();
                mLastCalendarView.init(0, -1, year, monthOfYear+1, dayOfMonth);
                GridView LastGridView = (GridView) mWorkspace.getChildAt(0);
                LastGridView.setAdapter(mLastCalendarView);
            }
            
            if(mStopScreen != 1){
                mNextCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(2)).getAdapter();
                mNextCalendarView.init(0, 1, year, monthOfYear+1, dayOfMonth);
                GridView mNextGridView = (GridView) mWorkspace.getChildAt(2);
                mNextGridView.setAdapter(mNextCalendarView);
            }
            
            mCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(1)).getAdapter();
            mCalendarView.init(0, 0, year, monthOfYear+1, dayOfMonth);
            GridView mGridView = (GridView) mWorkspace.getChildAt(1);
            mGridView.setAdapter(mCalendarView);
            
//            mLastGridView = (GridView) mWorkspace.getChildAt(0);
//            mLastGridView.setAdapter(mLastCalendarView);
//            
//            mNextGridView = (GridView) mWorkspace.getChildAt(2);
//            mNextGridView.setAdapter(mNextCalendarView);
//            
//            mGridView = (GridView) mWorkspace.getChildAt(1);
//            mGridView.setAdapter(mCalendarView);
        }else if(mWorkspace.getCurScreen() == 2){
            if(mStopScreen != -1){
                mLastCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(1)).getAdapter();
                mLastCalendarView.init(0, -1, year, monthOfYear+1, dayOfMonth);
                GridView LastGridView = (GridView) mWorkspace.getChildAt(1);
                LastGridView.setAdapter(mLastCalendarView);
            }
            
            if(mStopScreen != 1){
                mNextCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(0)).getAdapter();
                mNextCalendarView.init(0, 1, year, monthOfYear+1, dayOfMonth);
                GridView mNextGridView = (GridView) mWorkspace.getChildAt(0);
                mNextGridView.setAdapter(mNextCalendarView);
            }
            
            mCalendarView = (CalendarView) ((GridView)mWorkspace.getChildAt(2)).getAdapter();
            mCalendarView.init(0, 0, year, monthOfYear+1, dayOfMonth);
            GridView mGridView = (GridView) mWorkspace.getChildAt(2);
            mGridView.setAdapter(mCalendarView);
            
//            mLastGridView = (GridView) mWorkspace.getChildAt(1);
//            mLastGridView.setAdapter(mLastCalendarView);
//            
//            mNextGridView = (GridView) mWorkspace.getChildAt(0);
//            mNextGridView.setAdapter(mNextCalendarView);
//            
//            mGridView = (GridView) mWorkspace.getChildAt(2);
//            mGridView.setAdapter(mCalendarView);
        }
        addTextToTopTextView();
    }

}