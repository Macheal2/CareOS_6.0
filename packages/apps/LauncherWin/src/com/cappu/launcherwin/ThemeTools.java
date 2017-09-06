package com.cappu.launcherwin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.tools.AppComponentNameReplace;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ThemeTools {
    
    private Map<String, IconSkin> mMapIconSkin;
    private static final String TAG_TOPPACKAGES = "Page";
    private static final Canvas sCanvas = new Canvas();
    private static final Rect mAllAppBounds = new Rect();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    
    private Context mContext;
    
    private static AppComponentNameReplace mAppComponentNameReplace;
    
    public ThemeTools(Context context){
        this.mContext = context;
        mAppComponentNameReplace = new AppComponentNameReplace(context);
        readConfig();
    }
    
    public void readConfig() {
        if (mMapIconSkin == null) {
            mMapIconSkin = new HashMap<String, IconSkin>();
        } else {
            // return true;
        }
        try {
            XmlResourceParser parser = null;
             if(BasicKEY.LAUNCHER_VERSION == 4){
                 //parser = mContext.getResources().getXml(R.xml.klt_skin);
             } else{
                 parser = mContext.getResources().getXml(R.xml.skin);
             }
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_TOPPACKAGES);

            final int depth = parser.getDepth();

            int type;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser
                    .getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                String packageName = parser.getAttributeValue(null,"packageName");
                String className = parser.getAttributeValue(null, "className");
                String background = parser.getAttributeValue(null,"background");
                String apptype = parser.getAttributeValue(null,"type");
                if(mAppComponentNameReplace != null){
                    ComponentName componentName = mAppComponentNameReplace.Replace(packageName, className);
                    if (componentName != null){
                        packageName = componentName.getPackageName();
                        className = componentName.getClassName();
                    }
                }

                mMapIconSkin.put(packageName+"/"+className, new IconSkin(packageName, className,  background, apptype));
            }
        } catch (XmlPullParserException e) {
            Log.w("HHJ", "Got exception parsing toppackage.", e);
        } catch (IOException e) {
            Log.w("HHJ", "Got exception parsing toppackage.", e);
        }
    }

    /**这个是桌面workspace获取icon*/
    public Drawable getDrawableIcon(String packagename, String classname, Drawable d) {
        String IconBgName = null;
        Drawable drawable = null;

        IconBgName = getBackgroundIcon(packagename, classname);
        if (IconBgName != null) {
            int thumbRes = mContext.getResources().getIdentifier(IconBgName, "drawable", "com.cappu.launcherwin");
            if (thumbRes != 0) {
                drawable = mContext.getResources().getDrawable(thumbRes);
                return drawable;
            }else{
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**这个是allapp获取icon*/
    public Drawable getAppDrawableIcon(String packagename, String classname,Drawable d) {
        String IconBgName = null;
        Drawable drawable = null;

        IconBgName = getBackgroundIcon(packagename, classname)+"_i";
        if (IconBgName != null) {
            int thumbRes = mContext.getResources().getIdentifier(IconBgName, "drawable",
                    "com.cappu.launcherwin");
            if (thumbRes != 0) {
                drawable = mContext.getResources().getDrawable(thumbRes);
                return drawable;
            } else {
                return new BitmapDrawable(createAppIconBitmap(d, mContext));
            }
        }else {
            return new BitmapDrawable(createAppIconBitmap(d, mContext));
        }
    }
    static Bitmap createIconBitmap(Drawable icon, int width,int height) {
        synchronized (sCanvas) {
            if(icon == null){
                return null;
            }
            width=width-5;
            height=height-5;
			Bitmap mBubbleViewBitmap = Bitmap.createBitmap(width,
					height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mBubbleViewBitmap);
			icon.setBounds(5, 0, width-5,height-10);
			icon.draw(canvas);
			
			return mBubbleViewBitmap;
        }
    }
    static Bitmap createIconBitmapShadow(Drawable icon, int width,int height) {
        synchronized (sCanvas) {
            if(icon == null){
                return null;
            }
			Bitmap mBubbleViewBitmap = Bitmap.createBitmap(width,
					height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mBubbleViewBitmap);
			icon.setBounds(0, 0, width,height);
			icon.draw(canvas);
			return mBubbleViewBitmap;
        }
    }
    
    
    static Bitmap createAppIconBitmap(Drawable icon, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if(icon == null){
                return null;
            }
            final Resources resources = context.getResources();
            
            int width = (int) resources.getDimension(R.dimen.app_item_appicon_size);
            int height = (int) resources.getDimension(R.dimen.app_item_appicon_size);

            // no intrinsic size --> use default size
            int iconWidth =  icon.getIntrinsicWidth();
            int iconHeight =  icon.getIntrinsicHeight();
            if(iconWidth>width){
                width = iconWidth;
            }
            if(iconHeight>height){
                height = iconHeight;
            }

            final Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (width-iconWidth) / 2;
            final int top = (height-iconHeight) / 2;

            mAllAppBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+iconWidth, top+iconHeight);
            icon.draw(canvas);
            icon.setBounds(mAllAppBounds);

            return bitmap;
        }
    }
    
    
    public String getBackgroundIcon(String packageName, String className) {
 
        String IconBgName = null;
        if (mMapIconSkin.containsKey(packageName+"/"+className)) {
            IconSkin iconskin = mMapIconSkin.get(packageName+"/"+className);
            if (iconskin.getString(className) != null) {
                IconBgName = iconskin.getString(className);
            }
        }
        return JavaNameOperating.getFileNameNoEx(IconBgName);
    }
    
    
    public static class IconSkin {
        public IconSkin(String packagename, String classname,
                String background, String type) {
            mPackageName = packagename;
            mClassName = classname;
            mBackground = background;
            mType = type;
            putc(mClassName, mBackground);
        }

        public Map<String, String> RESOURCE_MAP = new HashMap<String, String>();; // key-
                                                                                 // mClassName,
                                                                                 // values-
                                                                                 // mResid

        public void putc(String classname, String bg) {
            RESOURCE_MAP.put(classname, bg);
        }

        public String getString(String classname) {
            return RESOURCE_MAP.get(mClassName);
        }
        
        public String getBanckgroundString(){
            return mBackground;
        }
        String mPackageName;
        String mClassName;
        String mBackground;
        String mType;
    }
}
