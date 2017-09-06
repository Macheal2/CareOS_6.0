package com.cappu.keyguard;

import com.android.keyguard.R;
import android.os.Handler.Callback;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.app.Service;  
import android.os.Vibrator;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.InputStream;

public class CappuSlideView extends View {
    private static final String TAG = "CappuSlideView";
    CappuLockSreenType mCappuLockSreenType = null;

    public void setCappuLockSreenType(CappuLockSreenType listener) {
        mCappuLockSreenType = listener;
    }

    private static final int MSG_REDRAW = 1;
    private static final int MSG_SCREENON = 2;

    private static final long TIME_SCREEN_TIME = 8000;

    private static final int DRAW_INTERVAL = 50;
    private static final int STEP_LENGTH = 5;
    private Paint mPaint;
    private VelocityTracker mVelocityTracker;
    private int mMaxVelocity;
    private LinearGradient mGradient;
    private int[] mGradientColors;
    private int mGradientIndex;
    private Interpolator mInterpolator;
    private float mDensity;
    private Matrix mMatrix;
    private ValueAnimator mValueAnimator;
    private String mText;
    private int mTextSize;
    private int mTextLeft;
    /** 文字与顶部的距离 */
    private int mTextTop;
    private int mSlider;
    private Bitmap mSliderBitmap;
    private int mSliderLeft;
    private int mSliderTop;
    private Rect mSliderRect;
    private int mSlidableLength; // SlidableLength = BackgroundWidth -
                                 // LeftMagins - RightMagins - SliderWidth

    private int mEffectiveLength; // Suggested length is 20pixels shorter than
                                  // SlidableLength

    private float mEffectiveVelocity;
    private float mStartX;
    private float mStartY;
    private float mLastX;
    private float mLastY;
    private float mMoveX;
    private Context mContext;   

    private boolean mAttached;
    private boolean mToUnLock = false;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SCREENON:
                    if(mCappuLockSreenType != null){
                        mCappuLockSreenType.keepScreenOn();
                    }
                    break;
                case MSG_REDRAW:
                    mMatrix.setTranslate(mGradientIndex, 0);
                    mGradient.setLocalMatrix(mMatrix);//dengying@20160822 lockscreen slide text
                    invalidate();
                    mGradientIndex += STEP_LENGTH * mDensity;
                    if (mGradientIndex > mSlidableLength) {
                        mGradientIndex = 0;
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                    break;
            }
        }
    };

    public int getSlidableLength(){
        return mSlidableLength;        
    }

    public CappuSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        mInterpolator = new AccelerateDecelerateInterpolator();
        mDensity = getResources().getDisplayMetrics().density;
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

	    mContext = context;

        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.SlideView);
        mText = typeArray.getString(R.styleable.SlideView_maskText);
        mTextSize = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextSize, R.dimen.cappu_mask_text_size);
        mTextLeft = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginLeft, R.dimen.cappu_mask_text_margin_left);
        mTextTop = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginTop, R.dimen.cappu_mask_text_margin_top);

        mSlider = typeArray.getResourceId(R.styleable.SlideView_slider, R.drawable.cappu_jog_tab_left_normal);
        mSliderLeft = typeArray.getDimensionPixelSize(R.styleable.SlideView_sliderMarginLeft, R.dimen.cappu_slider_margin_left);
        mSliderTop = typeArray.getDimensionPixelSize(R.styleable.SlideView_sliderMarginTop, R.dimen.cappu_slider_margin_top);
        mSliderBitmap = BitmapFactory.decodeResource(getResources(), mSlider);//readBitMap(mContext, mSlider);

        mSliderRect = new Rect(mSliderLeft, mSliderTop, mSliderLeft + mSliderBitmap.getWidth(), mSliderTop + mSliderBitmap.getHeight());

        mSlidableLength = typeArray.getDimensionPixelSize(R.styleable.SlideView_slidableLength, R.dimen.cappu_slidable_length);
        mEffectiveLength = typeArray.getDimensionPixelSize(R.styleable.SlideView_effectiveLength, R.dimen.cappu_effective_length);
        mEffectiveVelocity = typeArray.getDimensionPixelSize(R.styleable.SlideView_effectiveVelocity, R.dimen.cappu_effective_velocity);
        typeArray.recycle();

        mGradientColors = new int[] {
                Color.argb(255, 177, 177, 177), Color.argb(255, 177, 177, 177), Color.argb(255, 255, 255, 255)
        };
        mGradient = new LinearGradient(0, 0, 100 * mDensity, 0, mGradientColors, new float[] {
                0, 0.7f, 1
        }, TileMode.MIRROR);
        mGradientIndex = 0;
        mPaint = new Paint();
        mMatrix = new Matrix();
        mPaint.setTextSize(mTextSize);
        mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
    }

    /*private Bitmap readBitMap(Context context, int resId){     
        BitmapFactory.Options opt = new BitmapFactory.Options();     
        opt.inPreferredConfig = Bitmap.Config.RGB_565;      
        opt.inPurgeable = true;     
        opt.inInputShareable = true;    
        InputStream is = context.getResources().openRawResource(resId);        
        return BitmapFactory.decodeStream(is,null,opt);        
    }*/ 

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setShader(mGradient);
        if(WidgetUtil.isZh(mContext)){
            canvas.drawText(mText, mTextLeft, mTextTop, mPaint);
        }else{
            canvas.drawText(mText, mTextLeft - 20, mTextTop, mPaint);
        }
        canvas.drawBitmap(mSliderBitmap, mSliderLeft + mMoveX, mSliderTop, null);
    }

    private void reset() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mMoveX = 0;
        mPaint.setAlpha(255);
        mHandler.removeMessages(MSG_REDRAW);
        mHandler.sendEmptyMessage(MSG_REDRAW);
    }

    /*public void removeThread(){
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
        mMoveX = 0;
        mPaint.setAlpha(255);
        mHandler.removeMessages(MSG_SCREENON);
        mHandler.removeMessages(MSG_REDRAW);
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN && !mSliderRect.contains((int) mStartX, (int) mStartY)) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                mSliderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cappu_jog_tab_left_normal);
            }
            return super.onTouchEvent(event);
        }
        acquireVelocityTrackerAndAddMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
    	 	    Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);   
	            vib.vibrate(20);
                mStartX = event.getX();
                mStartY = event.getY();
                mLastX = mStartX;
                mLastY = mStartY;
                mHandler.removeMessages(MSG_REDRAW);
                mSliderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cappu_jog_tab_left_pressed);               
                break;

            case MotionEvent.ACTION_MOVE:
                mLastX = event.getX();
                mLastY = event.getY();
                mHandler.sendEmptyMessage(MSG_SCREENON);
                if (mLastX > mStartX) {
                    int alpha = (int) (255 - (mLastX - mStartX) * 3 / mDensity);
                    if (alpha > 1) {
                        mPaint.setAlpha(alpha);
                    } else {
                        mPaint.setAlpha(0);
                    }
                    if (mLastX - mStartX > mSlidableLength) {
                        mLastX = mStartX + mSlidableLength;
                        mMoveX = mSlidableLength;
                    } else {
                        mMoveX = (int) (mLastX - mStartX);
                    }
                } else {
                    mLastX = mStartX;
                    mLastY = mStartY;
                    mMoveX = 0;
                }

                if(mLastY < 0 || mLastY > 126){
                    mHandler.removeMessages(MSG_SCREENON);
                    //mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                    mSliderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cappu_jog_tab_left_normal);
                    //startAnimator(mLastX - mStartX, 0, 6000f, false);
                    mMoveX = 0;
                    mLastX = mStartX;
                    invalidate();
                    return super.onTouchEvent(event);
                }

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                float velocityX = mVelocityTracker.getXVelocity();
                if (mLastX - mStartX > mEffectiveLength || velocityX > mEffectiveVelocity) {
                    mToUnLock = true;
                    startAnimator(mLastX - mStartX, mSlidableLength, velocityX, true);
                } else {
                    startAnimator(mLastX - mStartX, 0, velocityX, false);
                    mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                }
                mHandler.removeMessages(MSG_SCREENON);
                mSliderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cappu_jog_tab_left_normal);
                releaseVelocityTracker();
                break;
        }
        return super.onTouchEvent(event);
    }

    public void startAnimator(float start, float end, float velocity, boolean isRightMoving) {
        if (velocity < mEffectiveVelocity) {
            velocity = mEffectiveVelocity;
        }
        int duration = (int) (Math.abs(end - start) * 1000 / velocity);
        mValueAnimator = ValueAnimator.ofFloat(start, end);
        mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMoveX = (Float) animation.getAnimatedValue();
                int alpha = (int) (255 - (mMoveX) * 3 / mDensity);
                if (alpha > 1) {
                    mPaint.setAlpha(alpha);
                } else {
                    mPaint.setAlpha(0);
                }
                invalidate();
            }
        });
        mValueAnimator.setDuration(duration);
        mValueAnimator.setInterpolator(mInterpolator);
        if (isRightMoving) {
            mValueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCappuLockSreenType != null) {
                        mCappuLockSreenType.UnLock();
                    }else{
                        reset();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        mValueAnimator.start();
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    //注册广播
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //android.util.Log.i("yzs1015", "attachis called ");
        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
    }

    //销毁广播
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
         mToUnLock = false;
        //android.util.Log.i("yzs1015", "onDetachedFromWindow called ");
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }

        if(mSliderBitmap != null && !mSliderBitmap.isRecycled()){
            mSliderBitmap.recycle();
        }
    }

    //创建广播
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {                
                reset();
            }else if(Intent.ACTION_SCREEN_ON.equals(action)){
                if(mToUnLock){
                    reset();
                }
            }
        }
    };
}
