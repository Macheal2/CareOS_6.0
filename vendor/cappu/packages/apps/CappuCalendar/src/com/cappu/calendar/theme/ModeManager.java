package com.cappu.calendar.theme;

import com.cappu.calendar.R;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.View;

public class ModeManager {


    public static final String ACTION_MODE_KEY = "launcher_workspace_mode";//BasicKEY.MODE_KEY;
    public static final String MODE_KEY = "launcher_workspace_mode";

    public static final int MODE_NONE = -1;
    
    /**经典模式*/
    public static final int DEFAULT_MODE = 1;
    /**豪华模式*/
    public static final int SIMPLE_MODE = 2;
    /**极简模式*/
    public static final int MINIMALIST_MODE = 3;
    
    private static Context mContext;
    private static ModeManager mModeManager;
    private int mModeType;

    OnModeChangedListener mModeChangedListener;

    private ModeManager() {
        mContext.getContentResolver().registerContentObserver(getModeUri(), true, mContentObserver);
        mModeType = getModeType();
    }
    
    public Uri getModeUri(){
        return Settings.System.getUriFor(ACTION_MODE_KEY);
         
    }
    
    /**获取当前模式
    *   经典模式
    *       public static final int DEFAULT_MODE = 1;
    *   豪华模式
    *       public static final int SIMPLE_MODE = 2;
    *   极简模式
    *       public static final int MINIMALIST_MODE = 3;*/
    public int getCurrentMode(){
        return Settings.System.getInt(mContext.getContentResolver(), MODE_KEY, 2);
    }
    
    public CustomContentObserver mContentObserver = new CustomContentObserver();
    class CustomContentObserver extends ContentObserver {

        public CustomContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            int modeType = -1;
            try {
                modeType = Settings.System.getInt(mContext.getContentResolver(), ACTION_MODE_KEY);
                
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }
            
            if(ModeManager.getInstance().getCurrentMode() == ModeManager.SIMPLE_MODE || ModeManager.getInstance().getCurrentMode() == ModeManager.MINIMALIST_MODE){
                Settings.System.putInt(mContext.getContentResolver(), "shape_layout",0);
            }
            mModeChangedListener.onModeChanged(modeType);
        }
        
    }

    public static void init(Context context) {
        mContext = context;
        mModeManager = new ModeManager();
    }

    public static ModeManager getInstance() {
        if (mModeManager == null) {
            throw new IllegalStateException("Uninitialized");
        }

        return mModeManager;
    }

    public int getModeType() {
        return Settings.System.getInt(mContext.getContentResolver(), ACTION_MODE_KEY, 2);
    }

    private void updateTheme(int themeType) {
        int themeID = -1;

    }

    public void setModeChangedListener(OnModeChangedListener listener) {
        mModeChangedListener = listener;
    }

    public interface OnModeChangedListener {
        void onModeChanged(int modeType);
    }
    

}
