package com.cappu.download;


import android.content.Context;
import android.content.SharedPreferences;


public class PushTypeSharedPreferences {
    /**关于push里存储的一些数据*/
    public final static String PUSH_PREFERENCES = "push_preferences";
    
    /**boolean 是否激活*/
    public final static String PUSH_TYPE_ACTIVATION_KEY = "activation";
    public final static int PUSH_TYPE_ACTIVATION_VALUES = 0x11;
    
    /**系统推送设置*/
    public final static String PUSH_TYPE_SETTINGS_KEY = "push_settings";
    public final static int PUSH_TYPE_SETTINGS_VALUES = 0x12;
    /**long 首次联网到首次推送的时间间隔，默认半小时*/
    public final static String PUSH_TYPE_SETTINGS_FIRST_INTERVAL = "push_first_interval";
    /**push当前日期   String 记录推送设置的日期 */
    public final static String PUSH_TYPE_SETTINGS_CURRENT_DAY = "push_current_day";
    /**long 获取列表的时间间隔，防止有新的推送进来*/
    public final static String PUSH_TYPE_SETTINGS_LIST_TIME_INTERVAL = "push_list_time_interval";
    /**long 推送单条信息的时间间隔*/
    public final static String PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL = "push_time_interval";
    /**当日push个数*/
    public final static String PUSH_TYPE_SETTINGS_CURRENT_DAY_NUM = "push_current_day_num";
    /**每日推送上限，到上限后推送将关闭，第二天启动*/
    public final static String PUSH_TYPE_SETTINGS_CURRENT_DAY_NUM_MAX = "push_current_day_num_max";
    /**下次启动时间*/
    public final static String PUSH_TYPE_SETTINGS_NEXT_TIME = "push_next_time";
    
    /**推送的列表*/
    public final static String PUSH_TYPE_LIST_KEY = "push_list";
    public final static int PUSH_TYPE_LIST_VALUES = 0x13;
    
    /**推送单条详情*/
    public final static int PUSH_TYPE_DETAIL_VALUES = 0x14;
    /**推送单条详情中有ICON*/
    public final static int PUSH_TYPE_DETAIL_ICON_VALUES = 0x15;
    /**推送单条详情中有BANNER*/
    public final static int PUSH_TYPE_DETAIL_BANNER_VALUES = 0x16;
    
    /**每天的毫秒数*/
    public final static long DATE_MILLIS = 24 * 60 * 60 * 1000;
    
    /**当前推送的类型*/
    int mPushType;
    /**long 首次联网到首次推送的时间间隔，默认半小时*/
    long mPushFirstInterval;
    
    
    /******************************************  下面为下载apk的部分  ************************************/
    /**获取apk列表*/
    public final static String PUSH_YTPE_DOWNLOAD_APK_LIST_KEY = "apk_list";
    /**请求apk列表的类型值*/
    public final static int PUSH_TYPE_DOWNLOAD_APK_LIST_VALUES = 0x20;
    /**页数*/
    public static final String APP_PI = "pi";
    public static final String APP_PN = "pn";
    /**rn 是在服务器上总共的apk数量*/
    public static final String APP_RN = "rn";
    /**每页请求的个数*/
    public static final String APP_PS = "ps";
    
    
    
    
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mEditor;
    Context mContext;
    
    public PushTypeSharedPreferences(Context context){
        mSharedPreferences = context.getSharedPreferences(PUSH_PREFERENCES, 0);
        mEditor = mSharedPreferences.edit();
        mContext = context;
    }
    
    public String getStringToSettingDate(String key,String defValues){
        checkValues();
        return mSharedPreferences.getString(key, defValues);
    }
    
    public long getLongToSettingDate(String key,long defValues){
        checkValues();
        return mSharedPreferences.getLong(key, defValues);
    }
    
    public int getIntToSettingDate(String key,int  defValues){
        checkValues();
        return mSharedPreferences.getInt(key, defValues);
    }
    
    public boolean getBooleanToSettingDate(String key,boolean  defValues){
        checkValues();
        return mSharedPreferences.getBoolean(key, defValues);
    }
    
    public void setStringToSettingDate(String key,String values){
        checkValues();
        mEditor.putString(key, values);
        mEditor.commit();
    }
    
    public void setLongToSettingDate(String key,long values){
        checkValues();
        mEditor.putLong(key, values);
        mEditor.commit();
    }
    
    public void setIntToSettingDate(String key,int  values){
        checkValues();
        mEditor.putInt(key, values);
        mEditor.commit();
    }
    
    public void setBooleanToSettingDate(String key,boolean values){
        checkValues();
        mEditor.putBoolean(key, values);
        mEditor.commit();
    }
    
    private void checkValues(){
        if(mSharedPreferences == null || mEditor==null){
            throw new NullPointerException("sharedPreferences or SharedPreferences.Editor is null");
        }
    }
    
    
    public static String getTypeCHName(Context context ,int values){
        return getName(context,values);
    }

    private static String getName(Context context ,int index) {
        String name = null;
        switch (index) {
        case PUSH_TYPE_ACTIVATION_VALUES:
            name = context.getString(R.string.push_activation);
            break;
        case PUSH_TYPE_SETTINGS_VALUES:
            name = context.getString(R.string.push_setting);
            break;
        case PUSH_TYPE_LIST_VALUES:
            name = context.getString(R.string.push_list);
            break;
        case PUSH_TYPE_DETAIL_VALUES:
            name = context.getString(R.string.push_detail);
            break;
        case PUSH_TYPE_DETAIL_ICON_VALUES:
            name = context.getString(R.string.push_detail_icon);
            break;
        case PUSH_TYPE_DETAIL_BANNER_VALUES:
            name = context.getString(R.string.push_detail_banner);
            break;
        case PUSH_TYPE_DOWNLOAD_APK_LIST_VALUES:
            name = context.getString(R.string.push_download_apk_list);
        default:
            break;
        }
        return name;
    }
    
    /*pushFirstInterval = sp.getLong(PushUtils.PUSH_FIRST_INTERVAL, 30 * 60 * 1000);//推送的时间间隔  30 * 60 * 1000
    pushFirst = sp.getBoolean(PushUtils.PUSH_FIRST, true);//是否为是第一次push
    pushCurrentDay = sp.getString(PushUtils.PUSH_CURRENT_DAY, PushUtils.PUSH_DEFAULT_STR);//push当前日期
    pushCurrentDayNum = sp.getInt(PushUtils.PUSH_CURRENT_DAY_NUM, 0);//当日push个数
    pushCurrentDayNumMax = sp.getInt(PushUtils.PUSH_CURRENT_DAY_NUM_MAX, 5);
    pushSettings = sp.getString(PushUtils.PUSH_SETTINGS, PushUtils.PUSH_DEFAULT_STR);//push设置
    pushList = sp.getString(PushUtils.PUSH_LIST, PushUtils.PUSH_DEFAULT_STR);//push list
    pushListNextTime = sp.getLong(PushUtils.PUSH_LIST_NEXT_TIME, -1);//push list的下一次时间
    pushNextTime = sp.getLong(PushUtils.PUSH_NEXT_TIME, -1);//下一次push的时间
    pushListTimeInterval = sp.getInt(PushUtils.PUSH_LIST_TIME_INTERVAL, 1800);//push list的时间间隔
    pushTimeInterval = sp.getInt(PushUtils.PUSH_TIME_INTERVAL, 1800);//push 的时间间隔
    activate = sp.getBoolean(PushUtils.ACTIVATE, false);//是否激活
*/}
