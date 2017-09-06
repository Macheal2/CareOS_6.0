package com.cappu.keyguard;

import com.android.keyguard.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
//import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.widget.RelativeLayout;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.android.keyguard.KeyguardUpdateMonitor;

public class CappuLockScreenView extends RelativeLayout implements OnLongClickListener, CappuLockSreenType{

	private static final String TAG = "CappuLockScreenView";
	private static final boolean DEBUG = false;
	
    private static final int unlock = 0x1122;
    private static final int lock = 0x1133;
    private static final int keepScreenOn = 0x1144;

    private boolean mPowerPluggedIn;

    static SimpleDateFormat mDate = new SimpleDateFormat("yyyy-MM-dd-EE");
    private ImageView mCameraIV;
	
	//dengying@20160816 begin
    private ImageView img_person_animation;
    private TextView txt_pedometer;
    //dengying@20160816 end
	
    private Handler handler = new Handler();
    private CappuSlideView mCappuSlideView;

    Context mContext;
    CappuCalendar mCappuCalendar;
    private ImageView mHourLeft;
    private ImageView mHourRight;
    private ImageView mMinuteLeft;
    private ImageView mMinuteRight;

    private boolean mIsCharging;// 是否在充电
    private int mLevel;//当前剩余电量
    private int mScale;//电量最大值

    private RelativeLayout mCappuLockScreenCalendarView;
    private WaterWaveProgress mWaterWaveProgress;
    private TextView lock_screen_calendar_lunar;
    Calendar mDummyDate;
    ImageView date_mm;
    ImageView date_m;
    private BounceView mUnReadPhone, mUnreadMsg;
    
    private int[] time_number = new int[]{
	    R.drawable.cappu_scrond_time_0,
	    R.drawable.cappu_scrond_time_1,
	    R.drawable.cappu_scrond_time_2,
	    R.drawable.cappu_scrond_time_3,
	    R.drawable.cappu_scrond_time_4,
	    R.drawable.cappu_scrond_time_5,
	    R.drawable.cappu_scrond_time_6,
	    R.drawable.cappu_scrond_time_7,
	    R.drawable.cappu_scrond_time_8,
	    R.drawable.cappu_scrond_time_9
    };

    private int[] date_number = new int[]{
	    R.drawable.cappu_scrond_date_0,
	    R.drawable.cappu_scrond_date_1,
	    R.drawable.cappu_scrond_date_2,
	    R.drawable.cappu_scrond_date_3,
	    R.drawable.cappu_scrond_date_4,
	    R.drawable.cappu_scrond_date_5,
	    R.drawable.cappu_scrond_date_6,
	    R.drawable.cappu_scrond_date_7,
	    R.drawable.cappu_scrond_date_8,
	    R.drawable.cappu_scrond_date_9
    };

    private Handler subHandler = null;
    private Handler mSubCappuHandler = new Handler(){
       @Override
       public void handleMessage(Message msg){
           if(msg.what == unlock){
               gotoUnlock();
           }else if (msg.what == lock){
               cancel();
           }else if(msg.what == keepScreenOn){
               onTrunOnScreen();
           }
       }
    };

    TextView mouth;
    TextView week;
    private String mStrMonth;

    public CappuLockScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        onBatteryReceiver(context);
        
        registerBoradcastReceiver(context);//dengying@20160816
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG,"CappuLockScreenView,onFinishInflate");
        if(mContext == null){
            mContext = getContext();
        }
        init();

        mTimeHandler.sendEmptyMessage(12);
        mTimeHandler.sendEmptyMessage(24);
    }

    private void init() {
        mHourLeft = (ImageView) findViewById(R.id.cappu_hour_left);
        mHourRight = (ImageView) findViewById(R.id.cappu_hour_right);
        mMinuteLeft = (ImageView) findViewById(R.id.cappu_minute_left);
        mMinuteRight = (ImageView) findViewById(R.id.cappu_minute_right);

        date_mm = (ImageView) findViewById(R.id.cappu_date_mm);
        date_m = (ImageView) findViewById(R.id.cappu_date_m);

        mouth = (TextView) findViewById(R.id.cappu_mouth);

        week = (TextView) findViewById(R.id.cappu_week);
	    mCappuSlideView = (CappuSlideView)findViewById(R.id.cappu_slidingTabLock);
	    if(mCappuSlideView != null){
	        mCappuSlideView.setCappuLockSreenType(this);
	    }
        
        lock_screen_calendar_lunar = (TextView) findViewById(R.id.cappu_lock_screen_calendar_lunar);

        if(WidgetUtil.isZh(mContext)){
            lock_screen_calendar_lunar.setVisibility(View.VISIBLE);
        }else{
            lock_screen_calendar_lunar.setVisibility(View.INVISIBLE);
        }

        mCappuLockScreenCalendarView = (RelativeLayout) findViewById(R.id.cappu_calendar_container);
        mWaterWaveProgress = (WaterWaveProgress) findViewById(R.id.cappu_keyguard_waterprogress);        

        mWaterWaveProgress.setMaxProgress(100);
        mWaterWaveProgress.animateWave();
       
        mUnReadPhone = (BounceView) findViewById(R.id.cappu_unread_phone);
        mUnreadMsg = (BounceView) findViewById(R.id.cappu_unread_msg);

        mCameraIV  = (ImageView) findViewById(R.id.cappu_camera_iv);
        mCameraIV.setOnLongClickListener(this);
        
        //dengying@20160816 begin
        img_person_animation = (ImageView) findViewById(R.id.img_animation_person);
		 txt_pedometer = (TextView) findViewById(R.id.txt_pedometer);
		 
		 if(isPedometerRun()){
			 img_person_animation.setImageResource(R.drawable.animation_person);
			 
			 txt_pedometer.setVisibility(View.VISIBLE);
			 img_person_animation.setVisibility(View.VISIBLE);
			 
			 AnimationDrawable animationDrawable = (AnimationDrawable) img_person_animation.getDrawable();
			 animationDrawable.start();
		 }else{
			 txt_pedometer.setVisibility(View.INVISIBLE);
			 img_person_animation.setVisibility(View.INVISIBLE);
		 }
		 
		 String steps = SystemProperties.get("persist.pedometer.steps", "0");
		 txt_pedometer.setText(" " + steps + mContext.getString(R.string.step));
		//dengying@20160816 end
    }
    
    //dengying@20160816 begin
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){ 
        @Override 
        public void onReceive(Context context, Intent intent) { 
            String action = intent.getAction(); 
            if(action.equals("magcomm.pedometer.update")){ 
                int steps = intent.getIntExtra("steps", 0);
           
            	  Log.i("dengyingPedometer", "CappuLockScreenView.java magcomm.pedometer.update steps= " + steps);
            	  
    		      txt_pedometer.setText(" " + steps + mContext.getString(R.string.step));
            } 
        } 
    }; 
    
    public void registerBoradcastReceiver(Context context){ 
        IntentFilter myIntentFilter = new IntentFilter(); 
        myIntentFilter.addAction("magcomm.pedometer.update"); 
        context.registerReceiver(mBroadcastReceiver, myIntentFilter); 
    }     
         
    @Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		
		mContext.unregisterReceiver(mBroadcastReceiver);
	}
    
    private boolean isPedometerRun(){ 
    	ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
     	List<RunningTaskInfo> list = am.getRunningTasks(50);
    	boolean isAppRunning = false;
    	
    	for (RunningTaskInfo info : list) {
    		if (info.topActivity.getPackageName().equals("com.magcomm.pedometer")) {
            		isAppRunning = true;
           		break;
         	}
     	}
    	
    	Log.i("dengyingPedometer", "CappuLockScreenView.java isPedometerRun = " + isAppRunning);
    	
    	return isAppRunning;
    }   
   //dengying@20160816 end    
    
    public void onBatteryReceiver(Context context){
      
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        Intent batteryIntent = context.registerReceiver(null, ifilter);
        //你可以读到充电状态,如果在充电，可以读到是usb还是交流电
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        mIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        mLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);        
        mScale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);    

        Log.i(TAG, "onReceiver is called and mIsCharging = " + mIsCharging + " and mLevel = " + mLevel);    
    }  

    public void setBatteryChange(KeyguardUpdateMonitor.BatteryStatus status, int level){
        boolean isChargingOrFull = status.status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status.status == BatteryManager.BATTERY_STATUS_FULL;
        mPowerPluggedIn = status.isPluggedIn() && isChargingOrFull;

        if(mPowerPluggedIn){
            if(level == 100){
                mWaterWaveProgress.stopAnimate();
                mWaterWaveProgress.setChangStatus(mContext.getResources().getString(R.string.keyguard_charged));
            }else{
                mWaterWaveProgress.setChangStatus(mContext.getResources().getString(R.string.keyguard_plugged_in));
                mWaterWaveProgress.animateWave();
            }
            mWaterWaveProgress.setProgress(level);
            mWaterWaveProgress.setVisibility(View.VISIBLE);
            mCappuLockScreenCalendarView.setVisibility(View.GONE);
        }else{
            mWaterWaveProgress.setVisibility(View.GONE);
            mCappuLockScreenCalendarView.setVisibility(View.VISIBLE);
        }           
    }

    public void setChangedTime(String formatTime){
        Log.i(TAG,"setChangedTime is called and formatTime  = " + formatTime);
          
	    int time_hour = Integer.parseInt(formatTime.split(":")[0]);
	    int time_minute = Integer.parseInt(formatTime.split(":")[1]);
	        
	    int time_hour_left = (int)time_hour / 10;
	    int time_hour_right = (int)time_hour % 10;
	    int time_minute_left = (int)time_minute / 10;
	    int time_minute_right = (int)time_minute % 10;

	    mHourLeft.setImageDrawable(mContext.getResources().getDrawable(time_number[time_hour_left]));
	    mHourRight.setImageDrawable(mContext.getResources().getDrawable(time_number[time_hour_right]));
	    mMinuteLeft.setImageDrawable(mContext.getResources().getDrawable(time_number[time_minute_left]));
	    mMinuteRight.setImageDrawable(mContext.getResources().getDrawable(time_number[time_minute_right]));
        mTimeHandler.sendEmptyMessage(12);
    }

    Handler mTimeHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 12) {
                synchronized (this) {
                    Log.i(TAG,"update Times ");
                    Calendar now = Calendar.getInstance();
                    mDummyDate = Calendar.getInstance();
                    mDummyDate.setTimeZone(now.getTimeZone());
                    Date date = mDummyDate.getTime();
                    try {
                        String dateStr = getCurrentDate(date);
                        int year = Integer.parseInt(dateStr.substring(0, 4));
                        int month =Integer.parseInt(dateStr.substring(5, 7));
                        int day = Integer.parseInt(dateStr.substring(8,10));
                        String weeks = dateStr.substring(11,dateStr.length());
                        String dateString = CappuCalendar.getDay(System.currentTimeMillis());
                        lock_screen_calendar_lunar.setText(dateString);
                        String StrMonth =dateStr.substring(5,7);
                        String StrDay = dateStr.substring(8,10);
                        char[] cday = StrDay.toCharArray();
                        if(WidgetUtil.isZh(mContext)){
                            mStrMonth = year+mContext.getString(R.string.i99_year) + StrMonth + mContext.getString(R.string.cappu_month);
                        } else{
                            mStrMonth = year+mContext.getString(R.string.i99_year) + StrMonth;
                        }                    
                        mouth.setText(ToDBC(mStrMonth));
                        week.setText(getCurrentDayOfWeek());				
			            int date_left = Integer.parseInt(StrDay) / 10;
			            int date_right = Integer.parseInt(StrDay) % 10;
			            date_mm.setImageDrawable(mContext.getResources().getDrawable(date_number[date_left]));
			            date_m.setImageDrawable(mContext.getResources().getDrawable(date_number[date_right]));         
                    } catch (Exception e) {
                        Log.e(TAG, "CappuLockScreenView :" + e.toString());
                    }

                }
            }else if(msg.what == 24){
                Log.i(TAG, "handleMessage is called and msg == 24 and mIsCharging = " + mIsCharging);
                if(mIsCharging){
                    if(mLevel == 100){
                        mWaterWaveProgress.setChangStatus(mContext.getResources().getString(R.string.keyguard_charged));
                        mWaterWaveProgress.stopAnimate();
                    }else{
                        mWaterWaveProgress.setChangStatus(mContext.getResources().getString(R.string.keyguard_plugged_in));
                        mWaterWaveProgress.animateWave();
                    }
                    mWaterWaveProgress.setProgress(mLevel);
                    mWaterWaveProgress.setVisibility(View.VISIBLE);
                    mCappuLockScreenCalendarView.setVisibility(View.GONE);
                }else{
                    mWaterWaveProgress.setVisibility(View.GONE);
                    mCappuLockScreenCalendarView.setVisibility(View.VISIBLE);
                }         
            }
        }
    };

    private String ToDBC(String input) {   
        char[] c = input.toCharArray();   
        for (int i = 0; i < c.length; i++) {   
            if (c[i] == 12288) {   
                c[i] = (char) 32;   
                continue;   
            }   
            if (c[i] > 65280 && c[i] < 65375)   
                c[i] = (char) (c[i] - 65248);   
        }   
        return new String(c);   
    }  

    public String getCurrentDayOfWeek() {
        Resources mResources = mContext.getResources();
        String [] weeks = mResources.getStringArray(R.array.array_week);
        return weeks[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**获取当前日期*/
    private String getCurrentDate(Date date) {
        return mDate.format(date);
    }

    int getColor(int i) {
        int a = 0;
        if (i == 1 || i == 5) {
            a = 0x58;
        } else if (i == 2 || i == 4) {
            a = 0xa8;
        } else if (i == 3) {
            a = 0xff;
        }
        return Color.argb(a, 0xff, 0xff, 0xff);
    }

    @Override
    public boolean onLongClick(View v) {       
        if(v ==  mCameraIV){
      	    //do nothing
            mCappuSlideView.startAnimator(0, mCappuSlideView.getSlidableLength(), 0.0f, true);
        }
        return false;
    }

    @Override
    public void UnLock(){
	    showUp();
    }


    @Override
    public void keepScreenOn(){
        Message m = new Message();
        m.what = keepScreenOn;
        mSubCappuHandler.sendMessage(m);
    }
    
    public void onTrunOnScreen(){
        Message m = new Message();
        m.what = CappuLockBaseView.CAPPU_STATUS_SCREENON;
        subHandler.handleMessage(m);
    }

    public void gotoUnlock(){
        Message m = new Message();
        m.what = 0;
        subHandler.handleMessage(m);
    }

    private void showUp(){
        Message m = new Message();
        m.what = unlock;
        mSubCappuHandler.sendMessage(m);
    }

    private void showDown(){
        Message m = new Message();
        m.what = lock;
        mSubCappuHandler.sendMessage(m);
    }
  
    public void cancel(){
       //do nothing
    }

    public void setSubHandler(Handler handler){
        subHandler = handler;
    }

    public void reflashPhoneUnread(boolean visible, boolean savedMode){
        if(DEBUG)Log.i(TAG, "reflashPhoneUnread is called and visible = " + visible + " and savedMode = " + savedMode);
        
        if(visible && !savedMode){
            mUnReadPhone.setVisibility(View.VISIBLE);
        }else{
            mUnReadPhone.setVisibility(View.GONE);
        }
    }

    public void reflashUnreadMessage(boolean visible, boolean savedMode){
        if(DEBUG)Log.i(TAG, "reflashUnreadMessage is called and visible = " + visible + " and savedMode = " + savedMode);
        if(visible && !savedMode){
            mUnreadMsg.setVisibility(View.VISIBLE);
        }else{
            mUnreadMsg.setVisibility(View.GONE);
        }
    }
}
