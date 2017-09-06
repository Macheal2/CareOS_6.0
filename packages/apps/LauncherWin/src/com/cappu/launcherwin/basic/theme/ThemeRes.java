package com.cappu.launcherwin.basic.theme;


import java.io.File;
import java.util.HashMap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.cappu.launcherwin.JavaNameOperating;
import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.speech.LauncherSpeechTools;
import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.zipUtil.XMLParse.Skin;
import com.cappu.launcherwin.zipUtil.ZipUtil;
import com.cappu.launcherwin.R;
public class ThemeRes {
    
    private static final String TAG = "ThemeRes";
    
    /**这个是主题每次启动后判断主题是否拷贝出来了  KookSharedPreferences 保存的*/
    public static final String THEMESEXIST = "themes";
    /**判断主题包是否解压*/
    public static final String UNZIP = "isUnzip";
    
    
    private Context mContext;
    private static ThemeRes sThemeRes;
    ZipUtil mZipUtil;
    
    private HashMap<String, Bitmap> mHashMap;// 图片对象缓存，key:图片的url
    private HashMap<String,Skin> mMapSkin;
    private String mCurrentThemeFileName="DefaultStyle";
    Matrix mMatrix;
    
    private ThemeRes(Context context) {
        this.mContext = context;
        mSpeechTools = new LauncherSpeechTools(context);//hejianfeng add init LauncherSpeechTools
        mHashMap = new HashMap<String, Bitmap>();
        UnZip(LauncherApplication.CappuDate + "theme", LauncherApplication.CappuThemes);
        initConfigSkin();
        getThemeFileName(ThemeManager.getInstance().getCurrentThemeType());
    }
    //hejianfeng add start
    private LauncherSpeechTools mSpeechTools;
    public LauncherSpeechTools getSpeechTools(){
    	return mSpeechTools;
    }
    //hejianfeng add end
    public static void init(Context context) {
        sThemeRes = new ThemeRes(context);
    }
    
    public void initConfigSkin(){
        if(mMapSkin != null){
            mMapSkin.clear();
        }
        mMapSkin = mZipUtil.getListSkin();
    }
    
    private Skin listSkinLoadBitmap(String packageNameClassNameType){
    	LauncherLog.v(TAG, "listSkinLoadBitmap, jeff packageNameClassNameType="+packageNameClassNameType);
        if(mMapSkin == null || mMapSkin.size() == 0){
            initConfigSkin();
        }
        
        if(mMapSkin != null && mMapSkin.containsKey(packageNameClassNameType)){
            return mMapSkin.get(packageNameClassNameType);
        }else{
            return null;
        }
    }
    /**
     * 获得相应模块布局的背景的名称
     * @param modeName
     * @return
     */
    public String getThemeBgName(String modeName,int themeTyle){
    	Skin skin = listSkinLoadBitmap(LauncherApplication.CappuPackage+"/"+modeName+"/"+themeTyle);
    	String bgName=null;
    	if (skin != null) {
    		bgName = JavaNameOperating
					.getFileNameNoEx(skin.mBackground);
		}
    	return bgName;
    }
    /**
     * 获得相应模块布局的背景色
     * @param modeName
     * @return
     */
    public Bitmap getThemeBackground(String modeName,int themeTyle,String fileName){
    	return getThemeResBitmap(LauncherApplication.CappuPackage+"/"+modeName+"/"+themeTyle,fileName);
    }
    /**
     * 获得相应模块布局的背景的名称
     * @param modeName
     * @return
     */
    public String getThemeBgName(String modeName){
    	return getThemeBgName(modeName,ThemeManager.getInstance().getCurrentThemeType());
    }
    /**
     * 获得相应模块布局的背景色
     * @param modeName
     * @return
     */
    public Bitmap getThemeBackground(String modeName){
    	return getThemeResBitmap(LauncherApplication.CappuPackage+"/"+modeName+"/"+ThemeManager.getInstance().getCurrentThemeType());
    }
    /**
     * 获得对应主题文件名主题的modeSelect
     * @param fileName
     * @return
     */
    public int getThemeModeSelect(String fileName){
    	Skin skin = listSkinLoadBitmap(LauncherApplication.CappuPackage+"/"+fileName+"/");
    	return Integer.parseInt(JavaNameOperating
				.getFileNameNoEx(skin.mBackground));
    }
    /**
     * 获得对应modeSelect主题的主题文件名
     * @return
     */
    public String getThemeFileName(int modeSelect){
    	Skin skin = listSkinLoadBitmap(LauncherApplication.CappuPackage+"/"+modeSelect+"/");
		if (skin != null) {
			mCurrentThemeFileName = JavaNameOperating
					.getFileNameNoEx(skin.mBackground);
		}
    	return mCurrentThemeFileName;
    }
    
    /**packageNameClassName 这个参数包含了包名、类名、主题类型
     * zoom                 这个参数是获取的图片是否缩小
     * */
    public Bitmap getThemeResContactsBitmap(String packageNameClassName,boolean zoom){
        Bitmap bitmap = null;
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            bitmap = mHashMap.get(packageNameClassName);
            if(bitmap == null){
                initConfigSkin();
            }
        }else{// 缓存中没有的化向缓存中添加一次
            Skin skin = listSkinLoadBitmap(packageNameClassName);
            if(skin == null){
                bitmap = null;
            }else{
                bitmap = loadBitmap(skin.mPackageName+"/"+skin.mClassName+"/"+skin.mThemesType,skin.mBackground,"DefaultStyle");
            }
        }
        return bitmap;
    }
    
    /**packageNameClassName 这个参数包含了包名、类名、主题类型*/
    public Bitmap getThemeResBitmap(String packageNameClassName){
        Bitmap bitmap = null;
        
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            bitmap = mHashMap.get(packageNameClassName);
            if(bitmap == null){
                Log.i(TAG, "********************图片在缓存中为空****************");
                initConfigSkin();
            }
        }else{
            Skin skin = listSkinLoadBitmap(packageNameClassName);
            Log.i(TAG, "listSkinLoadBitmap :"+packageNameClassName+"    skin:"+(skin == null));
            if(skin == null){
                bitmap = null;
            }else{
                bitmap = loadBitmap(skin.mPackageName+"/"+skin.mClassName+"/"+skin.mThemesType,skin.mBackground);
            }
        }
        return bitmap;
    }
    private  Bitmap getThemeResBitmap(String packageNameClassName,String fileName){
        Bitmap bitmap = null;
        
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            bitmap = mHashMap.get(packageNameClassName);
            if(bitmap == null){
                Log.i(TAG, "********************图片在缓存中为空****************");
                initConfigSkin();
            }
        }else{
            Skin skin = listSkinLoadBitmap(packageNameClassName);
            Log.i(TAG, "listSkinLoadBitmap :"+packageNameClassName+"    skin:"+(skin == null));
            if(skin == null){
                bitmap = null;
            }else{
                bitmap = loadBitmap(skin.mPackageName+"/"+skin.mClassName+"/"+skin.mThemesType,skin.mBackground,fileName);
            }
        }
        return bitmap;
    }
    /**获取默认的经典模式下面的icon 供替换里面的app显示 以及应用里使用    packageNameClassName 这个参数包含了包名、类名*/
    public Bitmap getDefModeIcon(String packageNameClassName,Drawable defDrawable){

        Bitmap bitmap = null;
        packageNameClassName = packageNameClassName+"_i1";
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            bitmap = mHashMap.get(packageNameClassName);
            if(bitmap == null){
                Skin skin = listSkinLoadBitmap(packageNameClassName);
                bitmap = loadBitmap(packageNameClassName, skin.mBackground);
            }
        }else{
            Skin skin = listSkinLoadBitmap(packageNameClassName);
            if(skin == null){
                bitmap = null;
            }else{
                bitmap = loadBitmap(skin.mPackageName+"/"+skin.mClassName+"/_i1",skin.mBackground);
            }
        }
        
        if(bitmap == null){
            return ((BitmapDrawable) defDrawable).getBitmap();
        }else{
            return bitmap;
        }
    
    }
    
    /**
     * packageNameClassName 这个
     * 参数包含了包名、类名、主题类型
     * defDrawable          这个参数是返回的一个默认的图
     * */
    public Bitmap getThemeResBitmap(String packageNameClassName,Drawable defDrawable){
        Bitmap bitmap = null;
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            bitmap = mHashMap.get(packageNameClassName);
            if(bitmap == null){
                Skin skin = listSkinLoadBitmap(packageNameClassName);
                bitmap = loadBitmap(packageNameClassName, skin.mBackground);
            }
        }else{
            Skin skin = listSkinLoadBitmap(packageNameClassName);
            if(skin == null){
                bitmap = null;
            }else{
                bitmap = loadBitmap(skin.mPackageName+"/"+skin.mClassName+"/"+skin.mThemesType,skin.mBackground);
            }
        }
        
        if(bitmap == null){
        	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        		return setPhotoFrame(((BitmapDrawable) defDrawable).getBitmap());
        	}else{
        		return ((BitmapDrawable) defDrawable).getBitmap();
        	}
        }else{
            return bitmap;
        }
    }
    //hejianfeng add start
    private Bitmap getScreenBmpRes(int res,int width,int height) {
		Log.v("jeff_tag", "width="+width+",height="+height);
		Bitmap mScreenBitmap = Bitmap.createBitmap(width,
				height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mScreenBitmap);
		Drawable bg = mContext.getResources().getDrawable(res);
		bg.setBounds(0, 0, width,height);
		bg.draw(canvas);
		return mScreenBitmap;
	}
    private Bitmap resizeImage(Bitmap bitmap, int w, int h)   
    {    
        Bitmap BitmapOrg = bitmap;    
        int width = BitmapOrg.getWidth();    
        int height = BitmapOrg.getHeight();    
        int newWidth = w;    
        int newHeight = h;    
  
        float scaleWidth = ((float) newWidth) / width;    
        float scaleHeight = ((float) newHeight) / height;    
  
        Matrix matrix = new Matrix();    
        matrix.postScale(scaleWidth, scaleHeight);    
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,    
                        height, matrix, true);    
        return resizedBitmap;    
    } 
	private Bitmap setCutBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
		return output;
	}
	public Bitmap setPhotoFrame(Bitmap headBitmap){
		Bitmap bitmap=getScreenBmpRes(R.drawable.photo_frame,200,200);
		Bitmap cutBitmap=setCutBitmap(resizeImage(headBitmap,175,175));
    	Bitmap mBitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas=new Canvas(mBitmap);
		Paint mPaint=new Paint();
		mPaint.setAntiAlias(true); 
		canvas.drawBitmap(cutBitmap, 12.5f, 6.5f, mPaint);
		return mBitmap;
    }
    //hejianfeng add end
    public static ThemeRes getInstance() {
        if (sThemeRes == null) {
            throw new IllegalStateException("Uninitialized");
        }
        return sThemeRes;
    }
    
    public void addZipThemes(String zipFile){
        Log.d(TAG, "检测安装包是否安装，且刷新一次config 文件:");
        UnZip(zipFile, LauncherApplication.CappuThemes);
    }
    
    /**
     * 第一个参数为解压的压缩包，
     * 第二个为解压的路径
     * */
    private void UnZip(String zipFile,String unZipPath){
        mZipUtil = new ZipUtil(mContext);
        boolean isUnZip = false;
        String currentVersion = APKInstallTools.getVersionName(mContext, mContext.getPackageName());
        String oldVersiong = KookSharedPreferences.getString(mContext, "version");
        try {
            JavaNameOperating.getFileNameNoEx(zipFile);
            if(oldVersiong != null){
                isUnZip = KookSharedPreferences.getString(mContext, zipFile) == null || !oldVersiong.equals(currentVersion);
            }else{
                isUnZip = KookSharedPreferences.getString(mContext, zipFile) == null || currentVersion != null;
            }
            
            Log.i(TAG, "这个文件解压成功过,不再需要解压 :"+zipFile+"   "+KookSharedPreferences.getString(mContext, zipFile)+"   currentVersion:"+currentVersion+"   oldVersiong:"+oldVersiong);
            if(isUnZip){
                isUnZip = mZipUtil.UnZipAllfile(zipFile, unZipPath);
            }
        } catch (Exception e) {
            isUnZip = false;
            Log.i(TAG, "ThemeRes UnZip  zipFile:"+zipFile+"    unZipPath:"+unZipPath+"    Exception:"+e.toString());
        }
        
        if(isUnZip){
            KookSharedPreferences.putString(mContext, zipFile, "yes");//解压成功之后,下面要删除压缩包
            KookSharedPreferences.putString(mContext, "version",currentVersion);
            File zipFiles = new File(zipFile);
            if(zipFiles.delete()){
                Log.i(TAG, "zip is unzip ,now is delete:"+zipFile);
            }
            initConfigSkin();/*刷新一次config文件*/
        }
    }
    
    /**
     * 加载图片前显示到指定的ImageView中，图片的url保存在视图对象的Tag中
     *
     * @param imageView
     *            要显示图片的视图
     * @param defaultBitmap
     *            加载需要显示的提示正在加载的默认图片对象
     */
    private Bitmap loadBitmap(String packageNameClassName,String iconName) {
        LauncherLog.v(TAG, "loadBitmap,jeff packageNameClassName="+packageNameClassName+",iconName="+iconName);
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            Bitmap bitmap = mHashMap.get(packageNameClassName);
            if (bitmap != null) {// 如果图片对象不为空，说明对应的图片存在，并返回
                return bitmap;
            } else {// 如果为空，需要将其从缓存中删除（其bitmap对象已被回收释放，需要重新加载）
                mHashMap.remove(packageNameClassName);
            }
        }
        try {
            if (iconName != null && packageNameClassName!=null) {
            	String appIconPath =LauncherApplication.CappuThemes+
                		mCurrentThemeFileName+"/"+iconName;
                final Bitmap bitmap = BitmapFactory.decodeFile(appIconPath);
                LauncherLog.v(TAG, "loadBitmap,jeff appIconPath="+appIconPath);
                if(bitmap!=null){
                    mHashMap.put(packageNameClassName, bitmap);
                }
                return bitmap;
            }else{
                return null;
            }
        } catch (Exception e) {
            Log.i(TAG, "加载图片  线程  ：" + e.toString());
            return null;
        }
    }
    private Bitmap loadBitmap(String packageNameClassName,String iconName,String fileName) {
        LauncherLog.v(TAG, "loadBitmap,jeff packageNameClassName="+packageNameClassName+",iconName="+iconName);
        if (mHashMap.containsKey(packageNameClassName)) {// 判断缓存中是否有
            Bitmap bitmap = mHashMap.get(packageNameClassName);
            if (bitmap != null) {// 如果图片对象不为空，说明对应的图片存在，并返回
                return bitmap;
            } else {// 如果为空，需要将其从缓存中删除（其bitmap对象已被回收释放，需要重新加载）
                mHashMap.remove(packageNameClassName);
            }
        }
        try {
            if (iconName != null && packageNameClassName!=null) {
            	String appIconPath =LauncherApplication.CappuThemes+
            			fileName+"/"+iconName;
                final Bitmap bitmap = BitmapFactory.decodeFile(appIconPath);
                LauncherLog.v(TAG, "loadBitmap,jeff appIconPath="+appIconPath);
                if(bitmap!=null){
                    mHashMap.put(packageNameClassName, bitmap);
                }
                return bitmap;
            }else{
                return null;
            }
        } catch (Exception e) {
            Log.i(TAG, "加载图片  线程  ：" + e.toString());
            return null;
        }
    }
    
    
}
