package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.netinfo.NetLookActivity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

public class RoundImageView extends ImageView {
    
    public static String TAG = "RoundImageView";
    
    private enum ViewType{
        news,health,travel,finances
    }
    
    private ViewType mViewType;
    
    /**
     * 图片的类型，方形or圆角
     */
    private int type;
    /**方形*/
    public static final int TYPE_SQUARE = 0;
    /**圆角*/
    public static final int TYPE_ROUND = 1;
    /**
     * 圆角大小的默认值
     */
    private static final int BODER_RADIUS_DEFAULT = 10;
    /**
     * 圆角的大小
     */
    private int mBorderRadius;

    /**
     * 绘图的Paint
     */
    private Paint mBitmapPaint;
    /**
     * 圆角的半径
     */
    private int mRadius;
    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix;
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;
    /**
     * view的宽度
     */
    private int mWidth;
    private RectF mRoundRect;
    
    private String mImagePath = null;

    public RoundImageView(Context context, AttributeSet attrs) {

        super(context, attrs);
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        mBorderRadius = a.getDimensionPixelSize(R.styleable.RoundImageView_borderRadius,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BODER_RADIUS_DEFAULT, getResources().getDisplayMetrics()));// 默认为10dp
        type = 0;
        
        int viewType = a.getInt(R.styleable.RoundImageView_type, 0);
        if(viewType == 1){
            mViewType = ViewType.news;
        }else if(viewType == 2){
            mViewType = ViewType.health;
        }
        a.recycle();
    }

    public RoundImageView(Context context,int viewType) {
        this(context, null);
        if(viewType == 1){
            mViewType = ViewType.news;
        }else if(viewType == 2){
            mViewType = ViewType.health;
        }
    }
    
    

    private Bitmap bmp;
    /**
     * 初始化BitmapShader
     */
    private void setUpShader() {
        if (mImagePath == null) {
            if(getDrawable() == null){
                if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
                    setScaleType(ScaleType.FIT_XY);
                    if(mViewType == ViewType.health){
                    	setBackgroundResource(R.drawable.application_travel_mode_5);// 汪洋 修改：之前新闻健康右边显示的是健康图片、这里替换成旅游
                        
                    }else if(mViewType == ViewType.news){
                        Log.i("WANGYANG", "news"+(mViewType == ViewType.news));
                        setBackgroundResource(R.drawable.application_news_mode_5);
                    }
                }else if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                	 if(mViewType == ViewType.health){
                		 setBackgroundResource(R.drawable.application_travel_mode_nine);// 汪洋 修改：之前新闻健康右边显示的是健康图片、这里替换成旅游
                        
                    }else if(mViewType == ViewType.news){
                    	setBackgroundResource(R.drawable.application_news_mode_nine);
                    }
                }else{
                    if(mViewType == ViewType.health){
                    	setBackgroundResource(R.drawable.application_travel_mode_3);// 汪洋 修改：之前新闻健康右边显示的是健康图片、这里替换成旅游
                        
                    }else if(mViewType == ViewType.news){
                    	setBackgroundResource(R.drawable.application_news_mode_3);
                    }
                }
            }
            return;
        }else{
            bmp = BitmapFactory.decodeFile(mImagePath);
        }
        
        Log.i(TAG, "bmp:"+(bmp == null)+"    mImagePath:"+mImagePath);
        if(bmp == null){
            if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){ //added by wangyang
                if(mViewType == ViewType.finances){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_finances_mode_5_banner);
                }else if(mViewType == ViewType.travel){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_travel_mode_5_banner);
                }else if(mViewType == ViewType.health){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_health_mode_5_banner);
                }else if(mViewType == ViewType.news){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_news_mode_5_banner);
                }
            }else{
                if(mViewType == ViewType.finances){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_finances_mode_3_banner);
                }else if(mViewType == ViewType.travel){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_travel_mode_3_banner);
                }else if(mViewType == ViewType.health){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_health_mode_3_banner);
                }else if(mViewType == ViewType.news){
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.application_news_mode_3_banner);
                }
            }
            
        }
        if(bmp!=null){
         // 将bmp作为着色器，就是在指定区域内绘制bmp
            mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
            float scale = 1.0f;
            if (type == TYPE_SQUARE) {
                if (!(bmp.getWidth() == getWidth() && bmp.getHeight() == getHeight())) {
                    // 如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
                    scale = Math.max(getWidth() * 1.0f / bmp.getWidth(), getHeight() * 1.0f / bmp.getHeight());
                }
                //Log.e("hehangjun", "b'w = " + bmp.getWidth() + " , " + "b'h = " + bmp.getHeight()+"    scale:"+scale);
            }
            // shader的变换矩阵，我们这里主要用于放大或者缩小
            
            if(BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){//这里是防止线上版本图片显示不全的问题
                scale = Math.max(getWidth() * 1.0f / bmp.getWidth(), getHeight() * 1.0f / bmp.getHeight());
            }
            
            mMatrix.setScale(scale, scale);
            // 设置变换矩阵
            mBitmapShader.setLocalMatrix(mMatrix);
            // 设置shader
            mBitmapPaint.setShader(mBitmapShader);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	LauncherLog.v(TAG, "onDraw,jeff type="+type+",mBorderRadius"+mBorderRadius+",mImagePath="+mImagePath);
        if (mImagePath == null) {
            setUpShader();
            super.onDraw(canvas);
            return;
        }
        setUpShader();
        if (type == TYPE_ROUND) {
            if(mRoundRect == null){
                mRoundRect = new RectF(0, 0, getWidth(), getHeight());
            }
            if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
            	canvas.drawRoundRect(mRoundRect, 12, 12, mBitmapPaint);
            }else{
            	canvas.drawRoundRect(mRoundRect, mBorderRadius, mBorderRadius, mBitmapPaint);
            }
            
        } else if(type == TYPE_SQUARE) {
            if(mRoundRect == null){
                mRoundRect = new RectF(0, 0, getWidth(), getHeight());
            }
            canvas.drawRoundRect(mRoundRect, 0, 0, mBitmapPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 圆角图片的范围
        if (type == TYPE_ROUND)
            mRoundRect = new RectF(0, 0, w, h);
    }

    /**
     * drawable转bitmap
     * 
     * @param drawable
     * @return
     */
//    private Bitmap drawableToBitamp(Drawable drawable) {
//        if (drawable instanceof BitmapDrawable) {
//            BitmapDrawable bd = (BitmapDrawable) drawable;
//            return bd.getBitmap();
//        }
//        int w = drawable.getIntrinsicWidth();
//        int h = drawable.getIntrinsicHeight();
//        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, w, h);
//        drawable.draw(canvas);
//        return bitmap;
//    }
    
    /**
     * 设置imageview显示图片的形状是圆角形状还是直角形状
     *   TYPE_SQUARE = 0;  直角形状
     *   TYPE_ROUND = 1;   圆角形状
     * */
    public void setImageShapeType(int type){
        //Log.i("hehangjun", "setImageShapeType   type:"+type+"     "+"    mBitmapShader:"+(mBitmapShader!=null));
        if(this.type != type){
            this.type = type;
            requestLayout();
        }
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            this.type = TYPE_SQUARE;
        }
    }
    
    /**设置imageview显示图片地址的路径*/
    public void setImagePath(String imagePath,int viewType){
        Log.i(TAG, "type:"+type+"     "+"imagePath:"+imagePath);
        if(imagePath == null){
            return;
        }
        if(viewType > 0){
            mViewType = getViewType(viewType);
        }
        
        if(!imagePath.equals(mImagePath)){
            this.mImagePath = imagePath;
            requestLayout();
        }
        
        if(mBitmapShader == null){
            requestLayout();
        }
    }
    private ViewType getViewType(int index) {
        ViewType vt = null;
        switch (index) {
            case NetLookActivity.NEWS_INDEX:
                vt = ViewType.news;
                break;
            case NetLookActivity.HEALTH_INDEX:
                vt = ViewType.health;
                break;
            case NetLookActivity.TRAVEL_INDEX:
                vt = ViewType.travel;
                break;
            case NetLookActivity.FINANCES_INDEX:
                vt = ViewType.finances;
                break;
            default:
                break;
        }
        return vt;
    }
}