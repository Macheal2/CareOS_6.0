package android.cappuutil;

import android.content.Context;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.R;

public class ThemeManager {
    private String TAG = "ThemeManager";
    private Context mContext;
    private static ThemeManager sThemeManager;
    
    public static final String ACTION_THEME_KEY = "care_theme_changed";
    
    public static final int THEME_DEFUALT = 0;
    public static final int THEME_CLASSICAL = 1;
    
    private ThemeManager(Context context) {
        this.mContext = context;
    }
    
    public static void init(Context context) {
        sThemeManager = new ThemeManager(context);
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
    public int getThemeType(Context context){
        int themeType = -1;
        try {
            themeType = Settings.System.getInt(context.getContentResolver(), ACTION_THEME_KEY);
            if(themeType == -1){
                themeType = THEME_DEFUALT;
            }
        } catch (SettingNotFoundException e) {
            themeType = THEME_DEFUALT;
        }
        
        Log.i(TAG,"ThemeManager themeType:"+themeType);
        return themeType;
    }
    
    public int getCurrentThemeBackgroundResource(Context context){
        if(getThemeType(context) == THEME_CLASSICAL){
            return R.drawable.care_img_theme_green;
        }else{
            return R.drawable.care_img_theme_blue;
        }
    }
}
