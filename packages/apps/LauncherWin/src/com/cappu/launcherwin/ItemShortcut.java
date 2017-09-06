package com.cappu.launcherwin;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.tools.AppAliasesTools;
import com.cappu.launcherwin.widget.LauncherLog;
//import com.cappu.launcherwin.tools.KookSharedPreferences;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;



/**
 * Represents an item in the launcher.
 */
class ItemShortcut extends ItemInfo {
    
	private String TAG="ItemShortcut";
    String background;
    Intent intent;
    CharSequence title;
    Bitmap mBitmap;
    String aliasTitle = null;
    String aliasTitleBackground = null;
    /**背景的图片res*/
    int mBackgroundRes;
    
    Context mContext;
    PackageManager packageManager;
    
    public ThemeTools mThemeTools;
//    /**
//     * 磁块的背景
//     */
//    public Bitmap pieceBg;
//    /**
//     * 磁块的半透背景
//     */
//    public Bitmap pieceHalfBg;
    ItemShortcut(Context context) {
        this.mContext = context;
        itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
        packageManager = mContext.getPackageManager();
    }
    //hejianfeng add start
	/*
	 * 获取程序 图标
	 */
	private Drawable getAppIcon(String packname) {
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname,
					0);
			return info.loadIcon(packageManager);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return null;
	}

	/*
	 * 获取程序的名字
	 */
	private  String getAppName(String packname) {
		
		try {
			ApplicationInfo info = packageManager.getApplicationInfo(packname,
					0);
			return info.loadLabel(packageManager).toString();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
    public void init() {
        /*背景*/
		if (!TextUtils.isEmpty(background)) {
			mBackgroundRes = mContext.getResources().getIdentifier(background,
					"drawable", "com.cappu.launcherwin");
		}
        
        /*名称*/
		if (!TextUtils.isEmpty(aliasTitle)) {
			title = getAliasTitle(aliasTitle);
		} else {
			if (intent.getComponent().getPackageName()
					.equals("com.cappu.launcherwin")) {
				try {
					ActivityInfo info = packageManager.getActivityInfo(
							intent.getComponent(), 0);
					title = info.loadLabel(packageManager).toString();
				} catch (PackageManager.NameNotFoundException nnfe) {
					Log.i("HHJ",
							"PackageManager.NameNotFoundException:"
									+ nnfe.toString());
				}
			} else {
				title = AppAliasesTools.replaceAppAliases(mContext, intent
						.getComponent().getPackageName(), intent.getComponent()
						.getClassName(), getAppName(intent.getComponent()
						.getPackageName()));
			}
		}
        
		/* icon */
		if (intent != null) {
			/** 下面将icon 去掉是为了隐藏所有的图片到压缩包里面去 */
			// hejianfeng add
			try{
			mBitmap = ThemeManager.getInstance().getQuickIcon(
					intent.getComponent().getPackageName() + "/"
							+ intent.getComponent().getClassName());
			if (mBitmap == null) {
				LauncherLog.v(
						TAG,
						"init,jeff mBubbleViewWidth="
								+ ThemeManager.getInstance().mBubbleViewWidth
								+ ",mBubbleViewHeight="
								+ ThemeManager.getInstance().mBubbleViewHeight);
				Drawable icon = getAppIcon(intent.getComponent()
						.getPackageName());

				if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS) {
					Bitmap bitmapShadow = mThemeTools.createIconBitmapShadow(icon,
							ThemeManager.getInstance().mBubbleViewWidth,
							ThemeManager.getInstance().mBubbleViewHeight);
					mBitmap = ThemeRes.getInstance().setPhotoFrame(bitmapShadow);
				} else {
					mBitmap = mThemeTools.createIconBitmap(icon,
							ThemeManager.getInstance().mBubbleViewWidth,
							ThemeManager.getInstance().mBubbleViewHeight);
				}
			}
			}catch(Exception e){
				mBitmap=null;
			}

		}
        textSize = Settings.Global.getInt(mContext.getContentResolver(), "textSize", mContext.getResources().getDimensionPixelSize(R.dimen.xl_text_size));
    }
    
    private String getAliasTitle(String aliasTitle) {
        try {
            int thumbRes = mContext.getResources().getIdentifier(aliasTitle, "string", "com.cappu.launcherwin");
            String aliastitle = mContext.getResources().getString(thumbRes);
            return aliastitle;
        } catch (Exception e) {
            Log.i("HHJ"," aliasTitle:"+aliasTitle+"没找到");
            return "";
        }
        
    }
    
    /**获取应用的icon图片*/
    public Bitmap getIconDrawable(){
        if(mBitmap != null){
            return mBitmap;
        }else{
            return null;
        }
    }
    
    /**获取磁块背景res*/
    public int getBackgroundRes(){
        return mBackgroundRes;
    }
    
    /**获取磁块背景*/
    public String getBackgroundName(){
        return background;
    }
    
    public String getTitle(){
        return (String) title;
    }
    
    public void setThemeTools(ThemeTools themeTools){
        this.mThemeTools = themeTools;
    }
    
    public int getTextSize(){
        return textSize;
    }

    @Override
    public String toString() {
        return super.toString()+"        前面是父类   "+"ItemShortcut [background=" + background + ", intent=" + intent + ", title=" + title + ", aliasTitle="
                + aliasTitle + ", aliasTitleBackground=" + aliasTitleBackground + ", mBackgroundRes=" + mBackgroundRes + ", mContext=" + mContext
                + ", packageManager=" + packageManager + ", mThemeTools=" + mThemeTools + "]";
    }
    
    
}
