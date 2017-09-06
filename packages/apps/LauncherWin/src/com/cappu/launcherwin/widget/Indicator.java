package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class Indicator extends View {
   private static final String TAG = "Indicator";
   private static final boolean DBG = false;
   
   public static final int INDICATOR_ID = 0x7f040000;

   private int mXPos = 0;

   private int mYPos = 0;

   private final int mScreenWidth;
   private final int mScreenHeight;
   private int mPageCount = 1;
   private boolean IDEL = false;
   public int mCurrentPosition=3;
   
   private Paint mTextPaint;
   private Paint mBitmapPaint;
// 字的长度
   private float mTxtWidth;
   // 字的高度
   private float mTxtHeight;
   
   
// 画实心圆的画笔
   private Paint mCirclePaint;
// 圆环半径
   private float mRingRadius;
   // 圆环宽度
   private float mStrokeWidth;
   // 圆心x坐标
   private int mXCenter;
   // 圆心y坐标
   private int mYCenter;
 // 半径
   private float mRadius;
   
   private int TextSize;
   private int TextMargin;
   
   private Bitmap mIndicatorPressed;
   private Bitmap mIndicatorNormal;
   
   public Indicator(Context context) {
       this(context, null);
   }
   
   public Indicator(Context context, AttributeSet attrs) {
       this(context, attrs, 0);
   }

   public Indicator(Context context, AttributeSet attrs, int defStyle) {
       super(context, attrs, defStyle);
       mScreenWidth = getResources().getDisplayMetrics().widthPixels;
       mScreenHeight = getResources().getDisplayMetrics().heightPixels;
       
       TextSize = getResources().getDimensionPixelSize(R.dimen.launcher_indicator_text_size);
       TextMargin = getResources().getDimensionPixelSize(R.dimen.launcher_indicator_text_margin);
       mRadius = TextSize/2;
       
       mTextPaint = new Paint();
       mTextPaint.setAntiAlias(true);
       mTextPaint.setStyle(Paint.Style.FILL);
       mTextPaint.setARGB(0, 0, 0, 0);
       mTextPaint.setTextSize(TextSize / 2);
       
       mBitmapPaint = new Paint();
       mBitmapPaint.setFlags(Paint.ANTI_ALIAS_FLAG); 
       mBitmapPaint.setAntiAlias(true);
       
       
       FontMetrics fm = mTextPaint.getFontMetrics();
       mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);
       
       mCirclePaint = new Paint();
       mCirclePaint.setAntiAlias(true);
       mCirclePaint.setStyle(Paint.Style.FILL);
       updateIndicator();
   }
   public void updateIndicator(){
	 //hejianfeng add start
       mIndicatorNormal=ThemeRes.getInstance().getThemeBackground("indicator_normal");
       mIndicatorPressed=ThemeRes.getInstance().getThemeBackground("indicator_pressed");
       LauncherLog.v(TAG, "Indicator,jeff indicator_normal="+mIndicatorNormal);
       LauncherLog.v(TAG, "Indicator,jeff indicator_pressed="+mIndicatorPressed);
       //hejianfeng add end
   }
   
   @Override
   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mXPos = mScreenWidth/2-mPageCount/2*TextMargin;
        
   }

   @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        if(mXPos > getWidth()){
            mXPos = getWidth()/2-mPageCount/2*TextMargin;
        }
        mXCenter = getWidth() / 2;
        mYCenter = getHeight() / 2;
        
        Log.i(TAG, "mXPos:"+mXPos+"    mXCenter:"+mXCenter+"    mYCenter:"+mYCenter);

        mTextPaint.setColor(Color.WHITE);
        
        mTextPaint.setTextSize(TextSize);

		for (int i = 0; i < mPageCount; i++) {
			if(mIndicatorPressed==null||mIndicatorNormal==null){
				continue;
			}
			float startPosX;
			if (mPageCount % 2 == 1) {
				startPosX = mXPos - mIndicatorPressed.getWidth() / 2 + i
						* TextMargin;
			} else {
				startPosX = mXPos - mIndicatorPressed.getWidth() / 2 + i
						* TextMargin + TextMargin / 2;
			}
			float startPosY = mYCenter - mIndicatorPressed.getHeight() / 2;
			if (mPageCount % 2 == 1) {
				if (mCurrentPosition == i) {
					canvas.drawBitmap(mIndicatorPressed, startPosX, startPosY,
							mBitmapPaint);
				} else {
					canvas.drawBitmap(mIndicatorNormal, startPosX, startPosY,
							mBitmapPaint);
				}
				if (mCurrentPosition == i) {
					canvas.drawBitmap(mIndicatorPressed, startPosX, startPosY,
							mBitmapPaint);
				}
			}
			if (mPageCount % 2 == 0) {
				if (mCurrentPosition == i) {
					canvas.drawBitmap(mIndicatorPressed, startPosX, startPosY,
							mBitmapPaint);
				} else {
					canvas.drawBitmap(mIndicatorNormal, startPosX, startPosY,
							mBitmapPaint);
				}
				if (mCurrentPosition == i) {
					canvas.drawBitmap(mIndicatorPressed, startPosX, startPosY,
							mBitmapPaint);
				}
			}
		}
        //canvas.restore();
    }
   
   public static Bitmap drawableToBitmap(Drawable drawable) {

       Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
               drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(bitmap);
       
       drawable.setBounds(0, 0,16,16);

       drawable.draw(canvas);
       return bitmap;
   }

   public void setPosition(float scrollPercent) {
       final int totalSpace = getWidth();
       final int scrollSpace = totalSpace;
       invalidate();
   }
   
   public void setCurrentPosition(int cp){
       mCurrentPosition = cp;
       invalidate();
   }
   
   public void setCount(int count){
       if(count == 0){
           return;
       }
       mPageCount = count;
       invalidate();
   }
   
   public int getCount(){
       return mPageCount;
   }
   
   public int getCurrentPosition(){
       return mCurrentPosition;
   }
}
