package com.cappu.calendar.theme;


import com.cappu.calendar.R;
//import com.android.cappu.tools.KookSharedPreferences;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.Theme;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.util.TypedValue;

public class ThemeManager {

    private static final String TAG = "ThemeManager";
    public static final String ACTION_THEME_KEY = "care_theme_changed";
    // This will be used BroadCast and SharedPreferences
    public static final String EXTRA_THEME = "care_theme";
    public static final String EXTRA_THEME_ID = "care_theme_id";

    public static final int THEME_NONE = -1;
    /** 默认主题 经典主题 */
    public static final int THEME_DEFUALT = 0;
    /** 默认主题 古典主题*/
    public static final int THEME_CLASSICAL = 1;

    private static Context mContext;
    private static ThemeManager sThemeManager;
    
    private int mThemeType = THEME_NONE;
    private int mThemeId = THEME_NONE;

    private final static int[] THEMES = {0, 1};//{R.style.Theme_Default,R.style.Theme_Classical};

    OnThemeChangedListener mThemeChangedListener;

    private ThemeManager() {
        mContext.getContentResolver().registerContentObserver(getThemeUri(), true, mContentObserver);
        mThemeId = getThemeId();
        
    }
    
    public Uri getThemeUri(){
        return Settings.System.getUriFor(ACTION_THEME_KEY);
         
    }
    
    public CustomContentObserver mContentObserver = new CustomContentObserver();
    class CustomContentObserver extends ContentObserver {

        public CustomContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int themeType = -1;
            try {
                themeType = Settings.System.getInt(mContext.getContentResolver(), ACTION_THEME_KEY);
                
                updateTheme(themeType);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            
            mThemeChangedListener.onThemeChanged(getThemeId());
        }
    }

    public static void init(Context context) {
        mContext = context;
        sThemeManager = new ThemeManager();
    }

    public static ThemeManager getInstance() {
        if (sThemeManager == null) {
            throw new IllegalStateException("Uninitialized");
        }

        return sThemeManager;
    }
    
    /**
     * 这个是获取主题类型
     * 返回 0 表示默认主题
     * */
    public int getThemeType(){
        int themeType = -1;
        try {
            themeType = Settings.System.getInt(mContext.getContentResolver(), ACTION_THEME_KEY);
            if(themeType == -1){
                themeType = THEME_DEFUALT;
            }
        } catch (SettingNotFoundException e) {
            themeType = THEME_DEFUALT;
        }
        
        return themeType;
    }

    /**这个是获取主题id*/
    public int getThemeId() {
        mThemeId = Settings.System.getInt(mContext.getContentResolver(), EXTRA_THEME_ID, THEME_NONE);
        if (mThemeId != THEME_NONE) {
            
            if(mThemeId!=THEMES[0] && mThemeId!=THEMES[1]){//防止主题错乱 这里是很难走到的/出现国一次了
                Log.i("HHJ", "主题错乱了");
                updateTheme(THEME_DEFUALT);
                mThemeId=THEMES[THEME_DEFUALT];
            }
            
            return mThemeId;
        }else{
            updateTheme(THEME_DEFUALT);
            return mThemeId;
        }
    }

    private void updateTheme(int themeType) {
        int themeID = -1;

        switch (themeType) {
        case THEME_DEFUALT:
            themeID = THEMES[themeType];
            break;
        case THEME_CLASSICAL:
            themeID = THEMES[themeType];
            break;
        }
        if(themeID != THEME_NONE){
            mThemeId = themeID;
            //KookSharedPreferences.putInt(mContext, EXTRA_THEME, themeType);
            //KookSharedPreferences.putInt(mContext, EXTRA_THEME_ID, themeID);
            Settings.System.putInt(mContext.getContentResolver(), EXTRA_THEME,themeType);
            Settings.System.putInt(mContext.getContentResolver(), EXTRA_THEME_ID,themeID);
            if (mThemeChangedListener != null) {
                mThemeChangedListener.onThemeChanged(themeID);
            }
        }
    }

    public void setThemeChangedListener(OnThemeChangedListener listener) {
        mThemeChangedListener = listener;
    }

    public interface OnThemeChangedListener {
        void onThemeChanged(int theme);
    }
    
    /**
     * Resolves the given attribute id of the theme to a resource id
     */
    public static int getResourceId(Context context, int attrId) {
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(attrId, outValue, true);
        return outValue.resourceId;
    }
}
