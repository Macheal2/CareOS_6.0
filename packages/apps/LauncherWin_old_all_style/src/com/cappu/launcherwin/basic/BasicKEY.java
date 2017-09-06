package com.cappu.launcherwin.basic;

import android.content.Context;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.R;

public class BasicKEY {
    
    Context mContext;
    public BasicKEY(Context context){
        mContext = context;
//        LAUNCHER_VERSION = mContext.getResources().getInteger(R.integer.launcher_version);
    }
    public static final String MODE_KEY = "launcher_workspace_mode";
    public static final String MODE_CHANGE_ACTION = "com.cappu.launcherwin.modeChange";
    
    public static final String LAUNCHER_BANCKGROUND = "launcher_backgroud";
    
    public static final String LAUNCHER_WEATHER_SPEECH = "launcher_weather_speech";
    
    public static final String WEATHER_SPEECH_STATUS = "weather_speech_status";
    
    
    public static final String THEME_CHANGE_ACTION = "com.cappu.launcherwin.themeChange";
    //hejianfeng add start
    public static final String THEME_WALLPAPER_ACTION = "com.cappu.launcherwin.themeWallpaperChange";
    public static final String THEME_ADD_ACTION = "com.cappu.launcherwin.themeAdd";
    public static final String THEME_DELETE_ACTION = "com.cappu.launcherwin.themeDelete";
    //hejianfeng add end
    
    /**晨想老人机桌面*/
    public static final int CAPPU_LAUNCHER = 1;
    public static int LAUNCHER_VERSION =1;
    
    
    /**以下是系统里面的KEY值,为里线上版本的兼容，请将系统key值放本文件    begin*/
    public static final String TALKING_CLOCK = "talking_clock";
    public static final String BOOTAUDIO_SETTING = "bootaudio_setting";
    //added by yzs for Android4.4 begin 20150701
    public static final String ENABLE_DATA_CONN_SECRET_CODE = "enable_data_conn_secretcode";
    public static final String DATA_CONN_SECRET_CODE = "data_conn_secretcode";
    //added by yzs for Android4.4 end 20150701
    /**以下是系统里面的KEY值,为里线上版本的兼容，请将系统key值放本文件    end*/
    
    
}
