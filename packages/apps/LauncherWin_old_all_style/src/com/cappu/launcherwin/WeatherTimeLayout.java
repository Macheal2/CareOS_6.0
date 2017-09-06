
package com.cappu.launcherwin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.netinfo.BaseCard;
import com.cappu.launcherwin.netinfo.widget.CappuHorizontalScrollView;
import com.cappu.launcherwin.netinfo.widget.NetDateDao;
import com.cappu.launcherwin.tools.DateUtils;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.tools.TeatherDateType;
import com.cappu.launcherwin.tools.TimeTools;
import com.cappu.launcherwin.widget.LauncherLog;


//added by yzs for talktime begin
import java.util.StringTokenizer;
import com.cappu.launcherwin.speech.SpeechTools;
import com.cappu.launcherwin.speech.SpeechTools.SpeakerListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
//added by yzs for talktime end

public class WeatherTimeLayout extends RelativeLayout implements OnChildViewClick,View.OnClickListener, SpeakerListener {
    
    private String TAG = "WeatherTimeLayout";
    
    SimpleDateFormat HH_time = new SimpleDateFormat("HH:mm");
    SimpleDateFormat hh_time = new SimpleDateFormat("hh:mm");
    private Context mContext;
    
    private RelativeLayout alarm_layout;
    private TextView time_widget;
    
    private TextView mTimeWidget1;
    private ImageView mTimeWidget2;
    private TextView mTimeWidget3;
    private LinearLayout mTimeWidgetSpot;
    
    //added by yzs for timeDate
    private TextView mTimeDate;

    private TextView am_or_pm;
    private TextView Alarm_clock_widget;
    private ImageView alarmImg;
    /** 当获取到天气数据后显示 */
    LinearLayout weather_layout_r;
    ImageView image_widget;
    TextView city_widget;
    TextView temperature_widget;//温度
    //TextView weather_widget;
    
    /**当获没有取到天气数据显示*/
    LinearLayout weather_notify;
    TextView noweatherInfo;

    
    CappuHorizontalScrollView mCappuHorizontalScrollView;
    List<Integer> mTypeList = new ArrayList<Integer>();
    List<NetDateDao> mNetDateDaoList = new ArrayList<NetDateDao>();
    

    
    private Drawable mBackground;
    
    private Launcher mLauncher;
    
    //WeatherBinder mWeatherBinder;
    
   // java.text.DateFormat mDF;// = new java.text.SimpleDateFormat("HH:mm");
    

    
    //added by yzs for talktime begin
    private final static boolean DEBUG = true;
    private boolean mEnableSpeech;
    private boolean mIsTalkSetting;
    private SpeechTools mSpeechTools;
    class TtsInitListener implements TextToSpeech.OnInitListener {
	    public void onInit(int status) {
		if (DEBUG) Log.d(TAG, "onInit for tts");
		if (status != TextToSpeech.SUCCESS) {
		    if(DEBUG) Log.e(TAG, "Could not initialize TextToSpeech.");
		        return;
		    }
  	    }
	}

    /**播放完成*/
    @Override
    public void onCompleted(){
        Log.i(TAG, " onCompleted is called ....");
        mEnableSpeech = false;
    }
    /**播放暂停*/
    @Override
    public void onSpeakPaused(){

    }
    //added by yzs for talktime end

    TimeTools mTimeTools;
    
    public WeatherTimeLayout(Context context) {
        this(context, null);
    }

    public WeatherTimeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    @SuppressLint("NewApi")
    public WeatherTimeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface();
        if(mSpeechTools == null)
            mSpeechTools = new SpeechTools(context);
        mSpeechTools.setSpeakerListener(WeatherTimeLayout.this);

        /*下面这个说明 是从 R.styleable.CareStyle 主题样式里面获取  R.attr.topbarStyle 这个样式*/
        try {
            /*TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CareStyle, R.attr.workspaceMainWidget, 0);*/
            TypedArray a = context.obtainStyledAttributes(attrs,new int[]{R.attr.WorkspaceMainWidget}, R.attr.WorkspaceMainWidget, 0);
            mBackground = a.getDrawable(0);//R.drawable.care_dialog_title_bg
            Log.i(TAG, "WeatherTimeLayout mBackground e:"+(mBackground==null));
            setBackground(mBackground);
            a.recycle();
        } catch (Exception e) {
            Log.i(TAG, "Exception e:"+e.toString());
        }
        mContext = context;
        if(context instanceof Launcher){
            mLauncher = (Launcher) context;
        }

    }
    private Typeface mTypeface;
    private void setTypeface() {
        // TODO Auto-generated method stub
        mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/minilishu.ttf");
    }
    public void setListener(Context launcher){
        if(time_widget!=null){
            time_widget.setOnClickListener(this);
        }
        if(mTimeWidget1!=null){
            mTimeWidget1.setOnClickListener(this);
        }
        if(mTimeWidget2!=null){
            mTimeWidget2.setOnClickListener(this);
        }
        if(mTimeWidget3!=null){
            mTimeWidget3.setOnClickListener(this);
        }
        if(alarm_layout!=null){
            alarm_layout.setOnClickListener(this);
        }
        if(weather_notify != null){
            weather_notify.setOnClickListener(this);
            weather_layout_r.setOnClickListener(this);
        }
        
        mTimeTools = new TimeTools();
    }
    

    //added by yzs for timeDate
    @SuppressLint("NewApi")
    public void updateTimeDate(){
        String dateStr = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMMd");
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateStr, Locale.getDefault());
        String dateString = dateFormat.format(new Date(System.currentTimeMillis()));
        mTimeDate.setText(dateString +" "+DateUtils.getLunarMonth()+DateUtils.getLunarDay());
    }
    
    
    @Override
    protected void onFinishInflate() {
        time_widget = (TextView) findViewById(R.id.time_widget);
        //added by wangyang 
        mTimeWidget1 = (TextView) findViewById(R.id.time_widget_1);
        mTimeWidget2 = (ImageView) findViewById(R.id.time_widget_2);
        mTimeWidget3 = (TextView) findViewById(R.id.time_widget_3);
        mTimeWidgetSpot = (LinearLayout)findViewById(R.id.time_widget_spot);
        //added by yzs for timeDate
        mTimeDate = (TextView) findViewById(R.id.time_date);

        Alarm_clock_widget = (TextView) findViewById(R.id.Alarm_clock_widget);
        alarmImg = (ImageView) findViewById(R.id.alarmImg);
        am_or_pm = (TextView) findViewById(R.id.am_or_pm);

        weather_layout_r = (LinearLayout) findViewById(R.id.weather_layout_r);
        image_widget = (ImageView) findViewById(R.id.image_widget);
        city_widget = (TextView) findViewById(R.id.city_widget);
        temperature_widget = (TextView) findViewById(R.id.temperature_widget);
        
        noweatherInfo = (TextView) findViewById(R.id.noweatherInfo);
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            time_widget.setTextColor(Color.BLACK);
            time_widget.setTypeface(mTypeface);
            mTimeWidget1.setTextColor(Color.BLACK);
            mTimeWidget1.setTypeface(mTypeface);
            mTimeWidget3.setTextColor(Color.BLACK);
            mTimeWidget3.setTypeface(mTypeface);
            mTimeDate.setTextColor(Color.BLACK);
            mTimeDate.setTypeface(mTypeface);
            Alarm_clock_widget.setTextColor(Color.BLACK);
            Alarm_clock_widget.setTypeface(mTypeface);
            am_or_pm.setTextColor(Color.BLACK);
            am_or_pm.setTypeface(mTypeface);
            city_widget.setTextColor(Color.BLACK);
            city_widget.setTypeface(mTypeface);
            temperature_widget.setTextColor(Color.BLACK);
            temperature_widget.setTypeface(mTypeface);
            noweatherInfo.setTextColor(Color.BLACK);
            noweatherInfo.setTypeface(mTypeface);
            alarmImg.setVisibility(View.INVISIBLE);
            Log.i("WANGYANG", "onFinishInflate---------THEME_CHINESESTYLE");
        }
        weather_notify = (LinearLayout) findViewById(R.id.weather_notify);
        
        mCappuHorizontalScrollView = (CappuHorizontalScrollView) findViewById(R.id.sub);
        alarm_layout = (RelativeLayout) findViewById(R.id.alarm_layout);
        setListener(getContext());
        
        super.onFinishInflate();
    }   

    
    /**
     * 用来判断服务是否运行.
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    Calendar mDummyDate;// = Calendar.getInstance();
    
    /**时间跳动设置*/
    public void setTimeLayout(Calendar calendar){
        final Date date = calendar.getTime();
        ContentResolver cv = mContext.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
        if (strTimeFormat == null) {
            strTimeFormat = "12";
        }
        if (am_or_pm != null && am_or_pm.getVisibility() != View.VISIBLE) {
            am_or_pm.setVisibility(View.VISIBLE);
        }
        if (am_or_pm != null && "12".equals(strTimeFormat)) {
            strTimeFormat = "12";
            if (date.getHours() < 12 && date.getHours() >= 0) {
                am_or_pm.setText(getContext().getString(R.string.am));
            } else {
                am_or_pm.setText(getContext().getString(R.string.pm));
            }
            am_or_pm.postInvalidate();
        } else if (am_or_pm != null && (DateFormat.is24HourFormat(getContext()) || "24".equals(strTimeFormat))) {
            am_or_pm.setText("          ");
            strTimeFormat = "24";
        }
        String currenttime = getCurrentTime(date, strTimeFormat, calendar.getTimeZone());
        String[] currentTimeSplit = currenttime.split(":");
        Log.i("wangyangweadd", " currentTimeSplit             "+currentTimeSplit.length);
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            time_widget.setVisibility(View.GONE);
            mTimeWidgetSpot.setVisibility(View.VISIBLE);
            mTimeWidget1.setText(currentTimeSplit[0].toString());
            mTimeWidget3.setText(currentTimeSplit[1].toString());
        }else{
            time_widget.setVisibility(View.VISIBLE);
            mTimeWidgetSpot.setVisibility(View.GONE);
            if(time_widget != null){
                Log.i(TAG, "currenttime           "+currenttime);
                time_widget.setText(currenttime);
            }
        }

        //added by yzs for time speech begin
        updateTimeSpeech();
        //added by yzs for time speech end
    }
    
    //added by yzs for time speech begin
    private void updateTimeSpeech(){
        SimpleDateFormat sdf = null;		
        if(DEBUG)Log.i(TAG, "is24HourFormat() = " + is24HourFormart());

        if(is24HourFormart()){
            sdf = new SimpleDateFormat("HH:mm");  
        } else {
            sdf = new SimpleDateFormat("hh:mm a");  
        }

        String date = sdf.format(new java.util.Date());
        SimpleDateFormat sdf_min = new SimpleDateFormat("mm");  
        String date_min = sdf_min.format(new java.util.Date());

        StringTokenizer stk = new StringTokenizer(date, " ");
        String[] result = new String[stk.countTokens()];
        String resultDate = "";
        int k = 0;
        while(stk.hasMoreTokens()){
            result[k] = stk.nextToken();
            k++;
        }
        if(result.length > 1){
           resultDate += result[1] + result[0];
        }else{
           resultDate += date;
        }

        if(DEBUG)Log.i(TAG, "date_min = " + date_min);

        if(date_min.equals("59")){
            mEnableSpeech = true;
            if(DEBUG) Log.i(TAG, "resources will create .....");
        }else if(date_min.equals("00")){
            mIsTalkSetting = getTimeSpeech(mContext);
       	
            if(DEBUG)Log.i(TAG, "will startSpeech and mSpeechTools = " + mSpeechTools + " and mIsTalkSetting = " + mIsTalkSetting);
	        String talkingString = getResources().getString(R.string.care_talking_time) + resultDate + getResources().getString(R.string.care_talking_time_end);

            if(mIsTalkSetting && mEnableSpeech)
	            mSpeechTools.startSpeech(talkingString);			
        }else{
           if(DEBUG) Log.i(TAG, "resources will destroy .....");
           if(mSpeechTools != null){
              mSpeechTools.stopSpeech();
           }
        }
    }
    
    private boolean is24HourFormart() {
	    return DateFormat.is24HourFormat(mContext);
    }

    private boolean getTimeSpeech(Context context){
        return Settings.Global.getInt(context.getContentResolver(), "talking_clock",0) == 1 ? true : false;
    }
    //added by yzs for time speech end


    
    public void updateNetLook(){
        
        Uri uri = Uri.parse("content://com.cappu.download/downloadText");
        String[] projection = null;
        String selection = null;//" type = '"+type+"' or type ='"+NHInt+"'";
        String[] selectionArgs = null;
        String sortOrder ="_id asc";
        final ContentResolver contentResolver = mContext.getContentResolver();
        
        List<Integer> typeList = getNetType();
        mNetDateDaoList.clear();
        
        for (int type:typeList) {
            
            selection = " flag = '"+type+"'  and  pushStatus = '"+1+"'"+" and bannerStatus = '"+1+"'";
            final Cursor c = contentResolver.query(uri,projection, selection, selectionArgs, sortOrder);
            try {
                final int dateIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_DATE);
                final int titleIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_TITLE);
                final int introduceIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_INTRODUCE);
                final int addressIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_URL);
                final int iconIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_ICON_PATH);
                final int bannerIndex = c.getColumnIndexOrThrow(BaseCard.TEXT_BANNER_PATH);
                
                while (c.move(c.getCount())) {
                    NetDateDao nd = new NetDateDao();
                    nd.date = c.getString(dateIndex);
                    nd.title = c.getString(titleIndex);
                    nd.introduce = c.getString(introduceIndex);
                    nd.address = c.getString(addressIndex);
                    nd.banner = c.getString(bannerIndex);
                    nd.icon = c.getString(iconIndex);
                    nd.type = type;
                    mNetDateDaoList.add(nd);
                }
            }catch(Exception e){
                Log.i(TAG, "Exception 466:"+e.toString());
            }finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        Log.i(TAG,"354 mCappuHorizontalScrollView.setData(mNetDateDaoList)");
        if(mCappuHorizontalScrollView != null){
            Log.i(TAG,"354 mCappuHorizontalScrollView.setData(mNetDateDaoList)    mNetDateDaoList.size():"+mNetDateDaoList.size());
            mCappuHorizontalScrollView.setData(mNetDateDaoList);
        }
        
    }
    
    private List<Integer> getNetType(){
        mTypeList.clear();
        Uri uri = Uri.parse("content://com.cappu.download/downloadText");
        String[] projection = {"flag"};
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder ="_id asc";
        final ContentResolver contentResolver = mContext.getContentResolver();
        final Cursor c = contentResolver.query(uri,projection, selection, selectionArgs, sortOrder);
        try {
            final int typeIndex = c.getColumnIndexOrThrow("flag");
            
            while (c.moveToNext()) {
                int type = c.getInt(typeIndex);
                if(!mTypeList.contains(type)){
                    mTypeList.add(type);
                }
            }
        }catch(Exception e){
            Log.i(TAG, "Exception 466:"+e.toString());
        }finally {
            if (c != null) {
                c.close();
            }
        }
        Log.i(TAG, "mTypeList:"+mTypeList.size()+"       "+mTypeList.toString());
        return mTypeList;
    }    
    
    public boolean getWeatherStatus(Context context){
        return Settings.Global.getInt(context.getContentResolver(), BasicKEY.WEATHER_SPEECH_STATUS, getResources().getInteger(R.integer.weather_speech_status)) == 1?true:false;
    }

    /**获取当前时间*/
    private String getCurrentTime(Date date,String strTimeFormat,TimeZone timeZone) {
        if(strTimeFormat.equals("24")){
            HH_time.setTimeZone(timeZone);
            return HH_time.format(date);
        }else{
            hh_time.setTimeZone(timeZone);
            return hh_time.format(date);
        }
        
    }
    //hejianfeng add start
    public void updataWeathers(){
    	Uri uri = Uri
				.parse("content://com.cappu.weather.database.WeatherProvider/FORECAST");
		Cursor cursor = null;

		try {
			cursor = mContext.getContentResolver().query(uri, null, null, null,
					null);
			if (cursor.moveToFirst()) {
				String city=cursor.getString(cursor.getColumnIndex("CITYNAME"));
				String low=cursor.getString(cursor.getColumnIndex("NIGHTTEMP"));
				String height=cursor.getString(cursor.getColumnIndex("DAYTEMP"));
				LauncherLog.v(TAG, "updataWeathers,jeff city="+city);
				String temperature=low+"~"+height+"℃";
				LauncherLog.v(TAG, "updataWeathers,jeff temperature="+temperature);
				city_widget.setText(city);
				temperature_widget.setText(temperature);
			}
		} catch (Exception e) {
			LauncherLog.v(TAG, "updataWeathers,jeff Exception:"+e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		uri = Uri
				.parse("content://com.cappu.weather.database.WeatherProvider/OBSERVE_WEATHER");

		try {
			cursor = mContext.getContentResolver().query(uri, null, null, null,
					null);
			if (cursor.moveToFirst()) {
				String weather=cursor.getString(cursor.getColumnIndex("WEATHER"));
				LauncherLog.v(TAG, "updataWeathers,jeff weather="+weather);
				image_widget.setImageResource(getWeatherImage(weather,isNight()));
			}
		} catch (Exception e) {
			LauncherLog.v(TAG, "updataWeathers,jeff Exception:"+e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
    }
    private boolean isNight(){
    	  SimpleDateFormat sdf = new SimpleDateFormat("HH");  
          String hour= sdf.format(new Date());  
          int k  = Integer.parseInt(hour)  ;  
          if ((k>=0 && k<6) ||(k >=18 && k<24)){  
            return true;  
          } else {  
              return false;  
          }
    }
    /**
     * 通过气象编号得到气象图片
     *
     * @param WeatherPhenomenonNumber
     * @return
     */
    private int getWeatherImage(String WeatherPhenomenonNumber, boolean isNight) {
        int weatherImageId;
        if (WeatherPhenomenonNumber != null && !WeatherPhenomenonNumber.equals("")) {
            switch (Integer.parseInt(WeatherPhenomenonNumber.trim())) {
                case 0:
                    if (isNight) {
                        weatherImageId = R.drawable.ic_001_icon;
                    } else {
                        weatherImageId = R.drawable.ic_00_icon;
                    }
                    return weatherImageId;
                case 1:
                    if (isNight) {
                        weatherImageId = R.drawable.ic_011_icon;
                    } else {
                        weatherImageId = R.drawable.ic_01_icon;
                    }
                    return weatherImageId;
                case 2:
                    weatherImageId = R.drawable.ic_02_icon;
                    return weatherImageId;
                case 3:
                    weatherImageId = R.drawable.ic_03_icon;
                    return weatherImageId;
                case 4:
                    weatherImageId = R.drawable.ic_04_icon;
                    return weatherImageId;
                case 5:
                    weatherImageId = R.drawable.ic_05_icon;
                    return weatherImageId;
                case 6:
                    weatherImageId = R.drawable.ic_06_icon;
                    return weatherImageId;
                case 7:
                    weatherImageId = R.drawable.ic_07_icon;
                    return weatherImageId;
                case 8:
                    weatherImageId = R.drawable.ic_08_icon;
                    return weatherImageId;
                case 9:
                    weatherImageId = R.drawable.ic_09_icon;
                    return weatherImageId;
                case 10:
                    weatherImageId = R.drawable.ic_10_icon;
                    return weatherImageId;
                case 11:
                    weatherImageId = R.drawable.ic_11_icon;
                    return weatherImageId;
                case 12:
                    weatherImageId = R.drawable.ic_12_icon;
                    return weatherImageId;
                case 13:
                    weatherImageId = R.drawable.ic_13_icon;
                    return weatherImageId;
                case 14:
                    weatherImageId = R.drawable.ic_14_icon;
                    return weatherImageId;
                case 15:
                    weatherImageId = R.drawable.ic_15_icon;
                    return weatherImageId;
                case 16:
                    weatherImageId = R.drawable.ic_16_icon;
                    return weatherImageId;
                case 17:
                    weatherImageId = R.drawable.ic_17_icon;
                    return weatherImageId;
                case 18:
                    if (isNight) {
                        weatherImageId = R.drawable.ic_181_icon;
                    } else {
                        weatherImageId = R.drawable.ic_18_icon;
                    }
                    return weatherImageId;
                case 19:
                    weatherImageId = R.drawable.ic_19_icon;
                    return weatherImageId;
                case 20:
                    weatherImageId = R.drawable.ic_20_icon;
                    return weatherImageId;
                case 21:
                    weatherImageId = R.drawable.ic_21_icon;
                    return weatherImageId;
                case 22:
                    weatherImageId = R.drawable.ic_22_icon;
                    return weatherImageId;
                case 23:
                    weatherImageId = R.drawable.ic_23_icon;
                    return weatherImageId;
                case 24:
                    weatherImageId = R.drawable.ic_24_icon;
                    return weatherImageId;
                case 25:
                    weatherImageId = R.drawable.ic_25_icon;
                    return weatherImageId;
                case 26:
                    weatherImageId = R.drawable.ic_26_icon;
                    return weatherImageId;
                case 27:
                    weatherImageId = R.drawable.ic_27_icon;
                    return weatherImageId;
                case 28:
                    weatherImageId = R.drawable.ic_28_icon;
                    return weatherImageId;
                case 29:
                    weatherImageId = R.drawable.ic_29_icon;
                    return weatherImageId;
                case 30:
                    weatherImageId = R.drawable.ic_30_icon;
                    return weatherImageId;
                case 31:
                    weatherImageId = R.drawable.ic_31_icon;
                    return weatherImageId;
                case 53:
                    weatherImageId = R.drawable.ic_53_icon;
                    return weatherImageId;
                default:
                    return 0;
            }

        } else {
            return 0;
        }

    }
    //hejianfeng add end
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        	return;
        }
        final int count = getChildCount();
        
        for (int i = 0; i < count; i++) {
            if(getChildAt(i) instanceof LinearLayout || getChildAt(i) instanceof CappuHorizontalScrollView){
                if(getChildAt(i).getId() == R.id.main){
                	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                		getChildAt(i).layout(0, t, r-12, b-220);
                	}else{
                		getChildAt(i).layout(0, t, r, b);
                	}
                }else if(getChildAt(i).getId() == R.id.sub){
                    int Height = findViewById(R.id.main).getMeasuredHeight();
                    if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                    	getChildAt(i).layout(0, Height+DensityUtil.dip2px(getContext(),8), r, b);
                    }else{
                    	getChildAt(i).layout(0, Height+DensityUtil.dip2px(getContext(),1), r, b);
                    }
                }
            }else{
                super.onLayout(changed, l, t, r, b);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        	return;
        }
        /*1068 764*/
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heigh = MeasureSpec.getSize(heightMeasureSpec);
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if(getChildAt(i) instanceof LinearLayout || getChildAt(i) instanceof CappuHorizontalScrollView){
                if(getChildAt(i).getId() == R.id.main){
                	int heighChild;
                	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                		heighChild = (435*heigh)/764;
                	}else{
                		heighChild = (450*heigh)/764;
                	}
                    int heightMeasureSpecChild = MeasureSpec.makeMeasureSpec(heighChild, MeasureSpec.EXACTLY);
                    getChildAt(i).measure(widthMeasureSpec, heightMeasureSpecChild);
                }else if(getChildAt(i).getId() == R.id.sub){
                	int heighChild;
                	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                		heighChild = (305*heigh)/764;
                	}else{
                		heighChild = (314*heigh)/764;
                	}
                    int heightMeasureSpecChild = MeasureSpec.makeMeasureSpec(heighChild, MeasureSpec.EXACTLY);
                    getChildAt(i).measure(widthMeasureSpec, heightMeasureSpecChild);
                }
                
            }
        }
    }

    @Override
    public void onClick(Context context) {
        
    }

    
    @Override
    protected void onDetachedFromWindow() {
        boolean isStop = false;
        
        Log.i(TAG, "view 销毁的时候执行这个 isStop:"+isStop);
        super.onDetachedFromWindow();
    }
    
    
    int mCountViewGroup = 0;
    public void deleteAllView(ViewGroup rootView){
        Log.i(TAG, "deleteAllView rootView:"+rootView.getChildCount());
        alarm_layout.removeAllViewsInLayout();
        removeView(alarm_layout);
        alarm_layout = null;
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            removeView(mTimeWidget1);
            removeView(mTimeWidget3);
            mTimeWidget1 = null;
            mTimeWidget3 = null;
        }else{
            removeView(time_widget);
            time_widget = null;
        }
        
        
        removeView(am_or_pm);
        am_or_pm  = null;
        
        removeView(Alarm_clock_widget);
        Alarm_clock_widget = null;
        
        removeView(alarmImg);
        alarmImg = null;
        weather_layout_r.removeAllViewsInLayout();
        removeView(weather_layout_r);
        weather_layout_r = null;
        
        removeView(image_widget);
        image_widget  = null;
        
        removeView(city_widget);
         city_widget  = null;
         removeView(temperature_widget);
        temperature_widget  = null;
        removeView(weather_notify);
        weather_notify.removeAllViewsInLayout();
        weather_notify = null;
        
        mCappuHorizontalScrollView.deleteView();
        removeView(mCappuHorizontalScrollView);
        mCappuHorizontalScrollView = null;
        
        removeAllViewsInLayout();
    }
    
    @Override
    protected void onAttachedToWindow() {
        Log.i(TAG, "view 添加的时候执行这个");
        super.onAttachedToWindow();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.weather_notify
                || v.getId() == R.id.weather_layout_r) {
            try {
                Intent intent = new Intent();
//                ComponentName cn = new ComponentName("com.android.magcomm",
//                        "com.android.magcomm.weather.WeatherActivity");
                ComponentName cn = new ComponentName("com.cappu.weather",
                        "com.cappu.weather.ui.activity.SplashActivity");
                intent.setComponent(cn);
                mLauncher.getSpeechTools().startSpeech(mLauncher.getString(R.string.launcher_weather),mLauncher.getSpeechStatus());
                mLauncher.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(mLauncher, mLauncher.getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
            }

		} else if (v.getId() == R.id.time_widget
				|| v.getId() == R.id.time_widget_1
				|| v.getId() == R.id.time_widget_2
				|| v.getId() == R.id.time_widget_3) {
			Log.i(TAG, "v.getId() == R.id.time_widget");

			Intent intent = new Intent();
			ComponentName cn = new ComponentName("com.android.deskclock",
					"com.android.deskclock.DeskClock");
			intent.setComponent(cn);
			intent.putExtra("deskclock.select.tab", 1);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mLauncher.getSpeechTools().startSpeech(
					mLauncher.getString(R.string.launcher_time_setting),
					mLauncher.getSpeechStatus());
			mLauncher.startActivity(intent);

		} else if (v.getId() == R.id.alarm_layout) {
            try {
				Intent intent = new Intent();
				ComponentName cn = new ComponentName("com.android.deskclock",
						"com.android.deskclock.DeskClock");
				intent.setComponent(cn);
				intent.putExtra("deskclock.select.tab", 0);// dengjianzhang@20150906
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);// added by yzs
				mLauncher.getSpeechTools().startSpeech(
						mLauncher.getString(R.string.launcher_ararm_clock),
						mLauncher.getSpeechStatus());
				mLauncher.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(mLauncher, mLauncher.getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
            }
            
        } 
    }
    
}
