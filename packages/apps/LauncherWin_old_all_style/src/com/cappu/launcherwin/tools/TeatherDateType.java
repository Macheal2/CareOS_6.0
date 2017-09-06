
package com.cappu.launcherwin.tools;

import com.cappu.launcherwin.R;

import android.content.Context;
import android.content.res.Resources;


public class TeatherDateType {
    private Context mContext;
    private Resources mResources;
    
    public TeatherDateType(Context context){
        this.mContext = context;
        mResources = context.getResources();
    }
    /** public static final int SUNNY_DAY = 0;  // 晴，白天 
        public static final int DUST = 12;      // 尘（和晴，白天共用图标，下同）
        public static final int SUNNY_NIGHT = 1;    // 晴，夜晚
        public static final int CLOUDY_DAY = 2;     // 多云，白天
        public static final int CLOUDY_NIGHT = 3;   // 多云，夜晚
        public static final int RAINY_LIGHT = 4;        // 小雨
        public static final int RAINY_MIDDLE = 5;       // 中雨
        public static final int RAINY_HEAVY = 6;        // 大雨
        public static final int RAINY_SNOW = 7;         // 雨夹雪
        public static final int RAINY_THUNDER   = 8;    // 雷雨
        public static final int SNOWY_LIGHT = 9;        // 小雪
        public static final int SNOWY_HEAVY = 10;       // 大雪
        public static final int FOG = 11;                   // 雾
        public static final int OVERCAST = 13;              // 阴
        */
    public int getType(String description){
        int type = 14;//  String[] city=mResources.getStringArray(R.array.weather_data_type);
        for(int i =0;i<type;i++){
            if(description.contains(mResources.getString(R.string.weather_rain))){//如果是雨
                if(description.contains(mResources.getString(R.string.weather_snow))){//如果是雪
                    return 7;//RAINY_SNOW = 7;         // 雨夹雪
                }else if(description.contains(mResources.getString(R.string.weather_thunder))){//如果是雷雨
                    return 8;//RAINY_THUNDER   = 8;    // 雷雨
                }else if(description.contains(mResources.getString(R.string.weather_big))){//如果是大雨
                    return 6;//RAINY_HEAVY = 6;        // 大雨
                }else if(description.contains(mResources.getString(R.string.weather_storm))){//如果是暴雨
                    return 6;//RAINY_HEAVY = 6;        // 大雨
                }else if(description.contains(mResources.getString(R.string.weather_moderate))){//如果是中雨
                    return 5;//RAINY_MIDDLE = 5;       // 中雨
                }else{//description.contains(mResources.getString(R.string.weather_other)) //如果是或者是其他有雨天气
                    return 4;//RAINY_LIGHT = 4;        // 小雨
                }
            }else if(description.contains(mResources.getString(R.string.weather_snow)) || description.contains(mResources.getString(R.string.weather_ice))){//如果是雪或冰
                if(description.contains(mResources.getString(R.string.weather_big)) || description.contains(mResources.getString(R.string.weather_storm))){//如果是雪或冰 加 大 暴
                    return 10;//SNOWY_HEAVY = 10;       // 大雪
                }else if(description.contains(mResources.getString(R.string.weather_thunder))){//如果是雷雨
                    return 9;//SNOWY_LIGHT = 9;        // 小雪
                }
            }else if(description.contains(mResources.getString(R.string.weather_overcast))){
                return 13;//OVERCAST = 13;              // 阴
            }else if(description.contains(mResources.getString(R.string.weather_cloud))){
                return 2;//CLOUDY_DAY = 2;多云，白天      CLOUDY_NIGHT = 3;   // 多云，夜晚
            }else if(description.contains(mResources.getString(R.string.weather_fog)) || description.contains(mResources.getString(R.string.weather_haze))){//如果是雾/䨪
                return 11;
            }else if(description.contains(mResources.getString(R.string.weather_sand)) || description.contains(mResources.getString(R.string.weather_dust))){//如果是沙/尘
                return 12;
            }else{//如果都不是就为晴
                return 0;//SUNNY_DAY = 0;晴，白天      SUNNY_NIGHT = 1;晴，夜晚
            }
        }
        return 0;
    }
}