package com.cappu.launcherwin;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class ItemWidget extends ItemInfo {

    String background;
    String aliasTitle;
    CharSequence title;
    
    /**背景的图片res*/
    int mBackgroundRes;
    
    Context mContext;
    PackageManager packageManager;
//    /**
//     * 磁块的背景
//     */
//    public Bitmap pieceBg;
//    /**
//     * 磁块的半透背景
//     */
//    public Bitmap pieceHalfBg;
    ItemWidget(Context context) {
        this.mContext = context;
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
        
        packageManager = mContext.getPackageManager();
    }

    public void init() {
        /*背景*/
		if (!TextUtils.isEmpty(background)) {
			mBackgroundRes = mContext.getResources().getIdentifier(background,
					"drawable", "com.cappu.launcherwin");
		}
        
        /*名称*/
        if(!TextUtils.isEmpty(aliasTitle)){
            title = getAliasTitle(aliasTitle);
        }
        
        textSize = Settings.Global.getInt(mContext.getContentResolver(), "textSize", mContext.getResources().getDimensionPixelSize(R.dimen.xl_text_size));
    }
    
    private String getAliasTitle(String aliasTitle) {
        int thumbRes = mContext.getResources().getIdentifier(aliasTitle, "string", "com.cappu.launcherwin");
        String aliastitle = mContext.getResources().getString(thumbRes);
        return aliastitle;
    }
    
    /**获取磁块背景res*/
    public int getBackgroundRes(){
        return mBackgroundRes;
    }
    
    public String getTitle(){
        return (String) title;
    }
    
    public int getTextSize(){
        return textSize;
    }

    /**获取磁块背景*/
    public String getBackgroundName(){
        return background;
    }
}
