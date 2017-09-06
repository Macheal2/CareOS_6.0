package com.cappu.launcherwin.basic.theme;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.Workspace;
import com.cappu.launcherwin.widget.ImagePiece;
import com.cappu.launcherwin.widget.LauncherLog;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import android.provider.Settings.Global;
import android.util.TypedValue;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
public class ThemeManager {

    private static final String TAG = "ThemeManager";
    public static final String ACTION_THEME_KEY = "care_theme_changed";
    public static final String EXTRA_THEME_ID = "care_theme_id";
    public static final String CARE_WALLPAPER = "data/data/com.cappu.launcherwin/care/wallpaper/current_wallpaper.png";

    public static final int THEME_NONE = -1;
    /**主题 炫彩主题 */
    public static final int THEME_COLORFUL = 0;
    /** 主题 极简风主题*/
//    public static final int THEME_SIMPLE = 1;
//    public static final int THEME_CLASSICAL = 8;
    /** 主题 中国风主题*/
    public static final int THEME_CHINESESTYLE = 2;
    /** 主题 简约风主题*/
//    public static final int THEME_SIMPLENESSSTYLE = 3;
    /** 主题 4x3主题*/
//    public static final int THEME_TWELVE_GRIDS = 4;//hejianfeng add for 4x3
    /** 主题 3x3主题*/
    public static final int THEME_NINE_GRIDS = 5;
    /** 默认主题 */
    public static final int THEME_DEFUALT = THEME_NINE_GRIDS;
    private int mCurrentThemeType = THEME_NONE;
    private Context mContext;
    private static ThemeManager sThemeManager;
    private int mThemeId = THEME_NONE;
    
    private File[] mFiles=null;
    private final static int[] THEMES = {R.style.Theme_Default,R.style.Theme_Default,R.style.Theme_ChineseStyle,
    	R.style.Theme_SimplenessStyle,R.style.Theme_Default,R.style.Theme_Default};

    private OnThemeChangedListener mThemeChangedListener;
    private Map<String,ContentValues> mBackupList=new HashMap<String,ContentValues>();
    
    public int mBubbleViewWidth=200;
    public int mBubbleViewHeight=200;
    
    private ImagePiece mImagePiece;
    
    private Bitmap curWallpaperBmp;
    
    public Bitmap getCurWallpaperBmp() {
		return curWallpaperBmp;
	}

	public void setCurWallpaperBmp(Bitmap curWallpaperBmp) {
		this.curWallpaperBmp = curWallpaperBmp;
		File f = new File(CARE_WALLPAPER);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			curWallpaperBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ThemeManager(Context context) {
        this.mContext = context;
        mContext.getContentResolver().registerContentObserver(getThemeUri(), true, mContentObserver);
        mThemeId = getThemeId();
        mImagePiece=new ImagePiece(mContext);
        if(getCurrentThemeType()==THEME_NINE_GRIDS){
        	int currentTheme=Settings.Global.getInt(mContext.getContentResolver(), "current_theme_bg", 0);
        	if(currentTheme==0){
        		mImagePiece.initPiece(R.drawable.bg_launcher_nine_default);
        		try {
	        		WallpaperManager wallpaperManager = WallpaperManager
	        				.getInstance(mContext);
	        		wallpaperManager.setResource(R.drawable.bg_launcher_nine_default);
        		} catch (Exception e) {
    				// TODO: handle exception
    			}
        	}else{
        		mImagePiece.initBitmapPiece(getCurrentWallPaper());
        	}
        }
        curWallpaperBmp=BitmapFactory.decodeFile(CARE_WALLPAPER);
    }
    
    public Uri getThemeUri(){
        return Settings.Global.getUriFor(ACTION_THEME_KEY);
         
    }
    //hejianfeng add start
    private Workspace mWorkspace;
    public void setWorkspace(Workspace mWorkspace){
    	this.mWorkspace=mWorkspace;
    }
    public Workspace getWorkspace(){
    	return mWorkspace;
    }
    //hejianfeng add end
    public CustomContentObserver mContentObserver = new CustomContentObserver();
    class CustomContentObserver extends ContentObserver {

        public CustomContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
			mCurrentThemeType = Settings.Global.getInt(
					mContext.getContentResolver(), ACTION_THEME_KEY,
					THEME_DEFUALT);
			updateTheme(mCurrentThemeType);
			LauncherLog.v(TAG, "onChange,jeff mCurrentThemeType="+mCurrentThemeType);
			ThemeRes.getInstance().getThemeFileName(mCurrentThemeType);
			mBackupList.clear();
			Bitmap mBitmap = ThemeRes
					.getInstance()
					.getThemeResBitmap(
							"com.cappu.launcherwin/com.cappu.launcherwin.applicationList.activity.GooduseActivity"
									+ "/" + getCurrentThemeType());
			if(mBitmap!=null){
				mBubbleViewWidth = mBitmap.getWidth();
				mBubbleViewHeight = mBitmap.getHeight();
			}
			if (getCurrentThemeType() == THEME_NINE_GRIDS) {
				mImagePiece.initBitmapPiece(getCurrentWallPaper());
			}
        }
    }
    public Drawable getCurrentWallPaper() {
		WallpaperManager wallpaperManager = WallpaperManager
				.getInstance(mContext);
		Drawable wallpaperDrawable = wallpaperManager.getDrawable();
		return wallpaperDrawable;
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
    public Map<String,ContentValues> getBackupList(){
    	return mBackupList;
    }
    public void setBackupList(Map<String,ContentValues> list){
    	mBackupList=new HashMap<String,ContentValues>(list);
    }
    public Bitmap getUnreadIcon(Bitmap bitmap,int num){
    	if(bitmap==null){
    		return null;
    	}
    	Bitmap mBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    	Paint paint = new Paint();
    	paint.setAntiAlias(true); 
		paint.setTextSize(40);
    	Canvas canvas = new Canvas(mBitmap);
    	canvas.drawBitmap(bitmap, 0, 0, paint);
    	Rect rect = new Rect();
    	String text=num+"";
    	if(num>99){
    		text="99+";
    	}
    	paint.getTextBounds(text,0,text.length(), rect);
    	Rect mRect = new Rect(bitmap.getWidth()-rect.width()-30,2,bitmap.getWidth(),62);
    	RectF rectF=new RectF(mRect);
    	paint.setColor(Color.RED);
    	if(num<10){
    		canvas.drawCircle(bitmap.getWidth()-rect.width()/2-30,30,30, paint);
    	}else{
    		canvas.drawRoundRect(rectF,30,30, paint);
    	}
    	paint.setColor(Color.WHITE);
    	if(num<10){
    		if(num==1){
    			canvas.drawText(text, bitmap.getWidth()-rect.width()-35, 43, paint);
    		}else{
    			canvas.drawText(text, bitmap.getWidth()-rect.width()-30, 43, paint);
    		}
    	}else{
    		if(num<20){
    			canvas.drawText(text, bitmap.getWidth()-rect.width()-18, 45, paint);
    		}else{
    			canvas.drawText(text, bitmap.getWidth()-rect.width()-15, 45, paint);
    		}
    	}
    	return mBitmap;
    }
    /**
     * 背景磁块
     * @return
     */
    public ImagePiece getImagePiece(){
    	return mImagePiece;
    }
    /**
     * ItemContacts是否有长按功能
     * @return
     */
	public boolean isSwitchIconBg() {
		return getCurrentThemeType() == THEME_COLORFUL;
	}
    /**
     * 获得默认路径下的所有文件列表
     * @return
     */
    public File[] getDefaultThemeListFile(){
    	if(mFiles ==null){
    		LauncherLog.v(TAG,"getDefaultThemeListFile, jeff mFiles ==null");
    		mFiles=getThemeListFile(LauncherApplication.CappuThemes);
    	}
    	return mFiles;
    }
    /**
     * 获得该路径下的所有文件列表
     * @param mFilePath 
     * @return
     */
    public File[] getThemeListFile(String mFilePath){
    	File file=new File(mFilePath);
    	if(!file.exists()){
    		LauncherLog.v(TAG,"getThemeListFile, jeff !file.exists()");
    		return null;
    	}
    	File[] files=file.listFiles();
    	return files;
    }
    /**
     * author hejianfeng
     * 根据主题中的default_workspace.xml 获得XmlPullParser
     * @param file 根据主题中的default_workspace.xml
     * @return 
     */
    public Document getThemeWorkspace(File file){
    	LauncherLog.v(TAG,"getThemeWorkspace, jeff file="+file);
    	if(!file.exists()){
    		LauncherLog.v(TAG,"getThemeWorkspace, jeff !file.exists()");
    		return null;
    	}
    	Document doc =null;
    	try {
	    	DocumentBuilderFactory mDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder mDocumentBuilder = mDocumentBuilderFactory.newDocumentBuilder();
	    	doc = mDocumentBuilder.parse(file);
	        doc.normalize();
    	}catch (Exception e) {
    		LauncherLog.v(TAG,"getThemeWorkspace, jeff Exception");
        }
    	return doc;
    }
    /**
     * 获得 ItemContacts 的icon图标的bitmap
     * @param intent
     * @param cellDefImage
     * @return
     */
    public Bitmap getItemContactsIcon(Intent intent,String cellDefImage){
    	return ThemeRes.getInstance().getThemeResContactsBitmap(intent.getComponent().getPackageName()+
				"/"+intent.getComponent().getClassName()+"/"+cellDefImage,false);
    }
    /**
     * author hejianfeng
     * 获得 AllApps 的icon图标的bitmap
     * @param pkg
     * @param zoom
     * @return
     */
    public Bitmap getAllAppsIcon(String pkg,Drawable defDrawable){
    	Bitmap mBitmap=null;
        mBitmap = ThemeRes.getInstance().getThemeResBitmap(pkg+"/"+getCurrentThemeType(),defDrawable);
        return mBitmap;
    }
    /**
     * author hejianfeng
     * 获得 快捷 icon图标的bitmap
     * @param pkg icon 包名
     * @return
     */
    public Bitmap getQuickIcon(String pkg){
    	Bitmap bm =null;
    	bm = ThemeRes.getInstance().getThemeResBitmap(pkg+"/"+getCurrentThemeType());
    	return bm;
    }
    /**
     * author hejianfeng
     * 获得 hotseat 的icon图标的bitmap
     * @param pkg icon 包名
     * @return
     */
    public Bitmap getHotSeatIcon(String pkg){
    	Bitmap bm =null;
    	bm = ThemeRes.getInstance().getThemeResBitmap(pkg+"/"+getCurrentThemeType());
    	return bm;
    }
    /**
     * 布局中是否存在 className Widget
     * @param intent
     * @param className 
     * @return
     */
    public boolean isExistWidget(Intent intent,String className){
    	return intent.getDataString().contains(className);
    }
    public int getCurrentThemeType(){
    	return getCurrentThemeType(mContext);
    }
    /**
     * 这个是获取主题类型
     * 返回 0 表示默认主题
     * */
    public int getCurrentThemeType(Context context){
        if(mCurrentThemeType == -1){
        	mCurrentThemeType = Settings.Global.getInt(mContext.getContentResolver(), ACTION_THEME_KEY,THEME_DEFUALT);
        }
        return mCurrentThemeType;
    }

    /**这个是获取主题id*/
    public int getThemeId() {
        mThemeId = Settings.Global.getInt(mContext.getContentResolver(), EXTRA_THEME_ID, THEME_NONE);
        if (mThemeId != THEME_NONE) {
            if(mThemeId!=THEMES[0] && mThemeId!=THEMES[1] && mThemeId!=THEMES[2]&& mThemeId!=THEMES[3]){//防止主题错乱 这里是很难走到的/出现国一次了
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
    	mThemeId = THEMES[themeType];
        if(mThemeId != THEME_NONE){
            Settings.Global.putInt(mContext.getContentResolver(), EXTRA_THEME_ID,mThemeId);
            if (mThemeChangedListener != null) {
                mThemeChangedListener.onThemeChanged(mThemeId);
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
