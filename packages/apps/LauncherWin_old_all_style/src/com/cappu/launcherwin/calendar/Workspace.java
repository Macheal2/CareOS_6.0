package com.cappu.launcherwin.calendar;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class Workspace extends ViewGroup{
    private boolean type;
    
    /** 沿Y轴正方向看，数值减1时动画逆时针旋转。 */
    public static final boolean ROTATE_DECREASE = true;
    /** 沿Y轴正方向看，数值减1时动画顺时针旋转。 */
    public static final boolean ROTATE_INCREASE = false;
    
    /** 值为true时可明确查看动画的旋转方向。 */
    public static final boolean DEBUG = false;
    
    public static enum ProcessDate{
        SHOW,TOUCH,FINISH
    }
    
 // mOverScrollX is equal to getScrollX() when we're within the normal scroll range. Otherwise
    // it is equal to the scaled overscroll position. We use a separate value so as to prevent
    // the screens from continuing to translate beyond the normal bounds.
    protected int mOverScrollX;
    
    protected int mMaxScrollX;
    protected int[] mTempVisiblePagesRange = new int[2];
    protected boolean mForceDrawAllChildrenNextFrame;
    /**允许通过滚动*/
    protected boolean mAllowOverScroll = true;
    
    private static final float OVERSCROLL_DAMP_FACTOR = 0.14f;
    
    /**全点击操作*/
    private boolean FullClickOperation = false;
    
    protected static final int PAGE_SNAP_ANIMATION_DURATION = 550;
    
    private static final String TAG = "HHJ";
    
    protected static final int INVALID_PAGE = -1;
    
    protected int mNextPage = INVALID_PAGE;
    
    // 用于滑动的类
    private Scroller mScroller;
    // 用来跟踪触摸速度的类
    private VelocityTracker mVelocityTracker;
    // 当前的屏幕视图
    private int mCurScreen;
    // 默认的显示视图
    private int mDefaultScreen = 0;
    /**无事件的状态*/
    private static final int TOUCH_STATE_REST = 0;
    /**处于拖动的状态*/
    private static final int TOUCH_STATE_SCROLLING = 1;
    /**滑动到上一页*/
    protected final static int TOUCH_STATE_PREV_PAGE = 2;
    /**滑动到下一页*/
    protected final static int TOUCH_STATE_NEXT_PAGE = 3;
    protected final static float ALPHA_QUANTIZE_LEVEL = 0.0001f;
    // 滑动的速度
    private static final int SNAP_VELOCITY = 600;

    private static int mNum;
    
    private int mTouchState = TOUCH_STATE_REST;
    private int mTouchSlop;
    private float mLastMotionX;
    
    private boolean mOnTouchEvent = false;
    
    private int[] mChildOffsets;
    private int[] mChildRelativeOffsets;
    private int[] mChildOffsetsWithLayoutScale;
    
    protected int mUnboundedScrollX;
    private int mMinimumWidth;
    protected float mLayoutScale = 1.0f;
    
    protected int mPageSpacing;
    
    protected int mMinFlingVelocity;
    protected int mMinSnapVelocity;
    protected float mDensity;
    
    public static final int DEFAULT_START_YEAR = 1901;
    public static final int DEFAULT_END_YEAR = 2049;

    /*下列常数需要基于密度来进行缩放。缩放版本将分配到下面的相应成员变量。*/
    private static final int FLING_THRESHOLD_VELOCITY = 500;
    private static final int MIN_SNAP_VELOCITY = 1500;
    private static final int MIN_FLING_VELOCITY = 250;
    
    protected static final int MAX_PAGE_SNAP_DURATION = 750;

    
    int sdkVersion;
    
    private long mTargetId = -1;

    
    private Activity mActivity;
    
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    // 在构造器中初始化
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mScroller = new Scroller(context, new ScrollInterpolator());

        mCurScreen = mDefaultScreen;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        
        mDensity = getResources().getDisplayMetrics().density;
        
        mMinFlingVelocity = (int) (MIN_FLING_VELOCITY * mDensity);
        mMinSnapVelocity = (int) (MIN_SNAP_VELOCITY * mDensity);
        
        try { 
            sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK); 
        } catch (NumberFormatException e) { 
            sdkVersion = 0; 
        } 
    }
    
    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1;
        }
    }
    
    public void setActivity(Activity a){
        this.mActivity = a;
    }

    /*
     * 
     * 为子View指定位置
     */
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
            int childLeft = 0;
            final int childCount = getChildCount();

            for (int i = 0; i < childCount; i++) {
                final View cellLayoutView = getChildAt(i);
                if (cellLayoutView.getVisibility() != View.GONE) {
                    final int childWidth = cellLayoutView.getMeasuredWidth();
                    cellLayoutView.layout(childLeft, 0, childLeft + childWidth, cellLayoutView.getMeasuredHeight());
                    childLeft += childWidth;
                }
            }
        //}
    }
    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heigh = MeasureSpec.getSize(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
           /* throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");*/
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
       /*     throw new IllegalStateException(
                    "ScrollLayout only can run at EXACTLY mode!");*/
        }

        // The children are given the same width and height as the scrollLayout
        // 得到多少页(子View)并设置他们的宽和高
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        //Log.e(TAG, "moving to screen "+mCurScreen);
        //scrollTo(mCurScreen * width, 0);
        if (count > 0) {
            mMaxScrollX = getChildOffset(count - 1) - getRelativeChildOffset(count - 1);
            int offset = getRelativeChildOffset(0);
            int spacing = Math.max(offset, widthSize - offset - getChildAt(0).getMeasuredWidth());
            setPageSpacing(spacing);
        } else {
            mMaxScrollX = 0;
        }
        
        snapToPage(mCurScreen);
    }

    public int getCurScreen() {
        return mCurScreen;
    }
    
    public void setDefaultScreen(int c){
        mDefaultScreen = c;
    }
    
    boolean isDefaultScreenShowing() {
        return mCurScreen == mDefaultScreen;
    }
    

    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
        if (mScroller.computeScrollOffset()) {
            // scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

            if (getScrollX() != mScroller.getCurrX() || getScrollY() != mScroller.getCurrY() || mOverScrollX != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate();
        }
        computeScrollHelper();
    }
    
    // we moved this functionality to a helper function so SmoothPagedView can reuse it
    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
            // Don't bother scrolling if the page does not need to be moved
            if (getScrollX() != mScroller.getCurrX()
                || getScrollY() != mScroller.getCurrY()
                || mOverScrollX != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
            return true;
        } else if (mNextPage != INVALID_PAGE) {
            mCurScreen = Math.max(0, Math.min(mNextPage, getPageCount() - 1));
            mNextPage = INVALID_PAGE;
            return true;
        }
        return false;
    }


    @Override
    public void scrollBy(int x, int y) {
        
        scrollTo(mUnboundedScrollX + x, getScrollY() + y);
    }
    
    public void setTargetId(long id){
        mTargetId = id;
    }
    
    public long getTargetId(){
        return mTargetId;
    }

    @Override
    public void scrollTo(int x, int y) {
        mUnboundedScrollX = x;
        /*注释这段是不支持循环滑动
        if (x < 0) {
            super.scrollTo(0, y);
        } else if (x > mMaxScrollX) {
            super.scrollTo(mMaxScrollX, y);
        } else {
            mOverScrollX = x;
            super.scrollTo(x, y);
        }*/
        if (x < 0) {
            super.scrollTo(x, y);
/*            if (isSupportCycleSlidingScreen() && mAllowOverScroll) {
                Log.i("HHJ", "373 "+mAllowOverScroll);
                mOverScrollX = 0;
                super.scrollTo(x, y);
            } else {
                Log.i("HHJ", "377 "+mAllowOverScroll);
                super.scrollTo(0, y);
                if (mAllowOverScroll) {
                    overScroll(x);
                }
            }*/
        } else if (x > mMaxScrollX) {
            /// M: modify to cycle sliding screen.
            
            if (isSupportCycleSlidingScreen() && mAllowOverScroll) {
                mOverScrollX = mMaxScrollX;
                super.scrollTo(x, y);
            } else {
                super.scrollTo(mMaxScrollX, y);
                if (mAllowOverScroll) {
                    overScroll(x - mMaxScrollX);
                }
            }
        } else {
            mOverScrollX = x;
            super.scrollTo(x, y);
        }
        //super.scrollTo(x, y);
    }
    
    protected void overScroll(float amount) {
        dampedOverScroll(amount);
    }
    protected void dampedOverScroll(float amount) {
        int screenSize = getMeasuredWidth();

        float f = (amount / screenSize);

        if (f == 0) return;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        int overScrollAmount = (int) Math.round(OVERSCROLL_DAMP_FACTOR * f * screenSize);
        if (amount < 0) {
            mOverScrollX = overScrollAmount;
            super.scrollTo(0, getScrollY());
        } else {
            mOverScrollX = mMaxScrollX + overScrollAmount;
            super.scrollTo(mMaxScrollX, getScrollY());
        }
        invalidate();
    }
    
    private float overScrollInfluenceCurve(float f) {
        f -= 1.0f;
        return f * f * f + 1.0f;
    }
    
    public void setAllowOverScroll(boolean allowOverScroll){
        mAllowOverScroll = allowOverScroll;
    }
    boolean noScroll = false;
    float down_x = 0f;
    
    public boolean onTouchEvent(MotionEvent event) {

        if(!mScroller.isFinished()){
            return true;
        }
        
        if(mActivity instanceof DetailsCalendarActivity){
            if(!(((DetailsCalendarActivity) mActivity).getProcessDate() == ProcessDate.SHOW)){
                return true;
            }
        }
        
        if (mVelocityTracker == null) {
            // 使用obtain方法得到VelocityTracker的一个对象
            mVelocityTracker = VelocityTracker.obtain();
        }
        // 将当前的触摸事件传递给VelocityTracker对象
        mVelocityTracker.addMovement(event);
        // 得到触摸事件的类型
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            //Log.e(TAG, "event down!");
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            mLastMotionX = x;
            mOnTouchEvent = true;
            down_x = x;
            break;

        case MotionEvent.ACTION_MOVE:
            noScroll = false;
            int deltaX = (int) (mLastMotionX - x);
            if (CalendarActivity.mIsStop){
                if(CalendarActivity.mStopScreen == 1){
                    if(x < down_x){
                        noScroll = true;
                        break;
                    }else{
                        noScroll = false;
                    }
                }else if (CalendarActivity.mStopScreen == -1){
                    if(x > down_x){
                        noScroll = true;
                        break;
                    }else{
                        noScroll = false;
                    }
                }else{
                    Log.e("hmq","workspace calendar error stop screen");
                }
            }
            mLastMotionX = x;
            
            if (deltaX < 0) {
                if(mAllowOverScroll){
                    scrollBy(deltaX, 0);
                }else if (getScrollX() > 0) {//当手动滑动不能循环滑动是加上
                    scrollBy(deltaX, 0);
                }
            } else {
                final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - getScrollX() - getWidth();
                if(mAllowOverScroll){
                    scrollBy(deltaX, 0);
                }else if (availableToScroll > 0) {//当手动滑动不能循环滑动是加上
                    scrollBy(deltaX, 0);
                }
            }

            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if(noScroll){
                break;
            }
            //Log.e(TAG, "event : up");
            // if (mTouchState == TOUCH_STATE_SCROLLING) {
            final VelocityTracker velocityTracker = mVelocityTracker;
            // 计算当前的速度
            velocityTracker.computeCurrentVelocity(1000);
            // 获得当前的速度
            int velocityX = (int) velocityTracker.getXVelocity();
            
            final int deltaX1 = (int) (x - mLastMotionX);

            
            int finalPage = mCurScreen;
            int About = 0;
            
            
            /*isSupportCycleSlidingScreen();*/
            if (velocityX > SNAP_VELOCITY) {
                // Fling enough to move left
                //Log.i(TAG, "Fling enough to move left:");
                if (mCurScreen > 0) {
                    //Log.e("hmq", "上一页    :"+mCurScreen+"; getScrollX()="+getScrollX());
                    snapToPage(mCurScreen - 1);
                    About = -1;
                } else if (hitsPreviousPage(getScrollX(), y)) {
                    //Log.e("hmq", "上一页1    :" + velocityX +"; getScrollX()="+getScrollX());
                    snapToPageWithVelocity(getChildCount() - 1, velocityX);
                    About = -1;
                }
            } else if (velocityX < -SNAP_VELOCITY) {
                // Fling enough to move right
                //Log.i(TAG, "Fling enough to move right :"+mCurScreen);
                if(mCurScreen < getChildCount() - 1){
                    //Log.e("hmq", "下一页    :"+mCurScreen +"; getScrollX()="+getScrollX());
                    //Log.i(TAG, "Fling enough to move right:");
                    snapToPage(mCurScreen + 1);
                    About = 1;
                } else if (hitsNextPage(getScrollX(), y)) {
                    //Log.e("hmq", "下一页1   velocityX:"+velocityX +"; getScrollX()="+getScrollX());
                    snapToPageWithVelocity(0, velocityX);
                    About = 1;
                }
            } else {
//                if (hitsPreviousPage(getScrollX(), y) && mAllowOverScroll) {
//                    Log.e("hmq", "上一页    :"+velocityX);
//                    snapToPageWithVelocity(getChildCount() - 1, velocityX);
//                    About = -1;
//                } else if (hitsNextPage(getScrollX(), y) && mAllowOverScroll) {
//                    Log.e("hmq", "下一页   velocityX:"+velocityX);
//                    snapToPageWithVelocity(0, velocityX);
//                    
//                    About = 1;
//                } else {
                    //Log.e("hmq", "other a="+getPageNearestToCenterOfScreen());
                    snapToPage(mCurScreen);
                    About = 0;
//                }
            }
            
            final int v = About;

            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            mTouchState = TOUCH_STATE_REST;
            mOnTouchEvent = false;
            
            if(v!=0 && mActivity != null){
                ((DetailsCalendarActivity) mActivity).setProcessDate(ProcessDate.TOUCH);
            }
            new Thread(new Runnable() {
                public void run() {
                    if (v != 0) {
                        mHandler.sendEmptyMessageDelayed(v, PAGE_SNAP_ANIMATION_DURATION + 5);
                    }
                 }
             }).start();
            
            break;
        }

        return true;
    }
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTouchFinish.upDate(msg.what);
        }
    };
    
    
    public void snapToPageWithVelocity(int whichPage, int velocity) {
        whichPage = Math.max(0, Math.min(whichPage, getChildCount() - 1));
        int halfScreenSize = getMeasuredWidth() / 2;

        final int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);

        if (newX == mUnboundedScrollX) {
            //Log.i("HHJ","542  ");
            return;
        }

        if (isSupportCycleSlidingScreen()) {
            if (getScrollX() <= 0 && mCurScreen == 0 && whichPage == getChildCount() - 1) {
                mUnboundedScrollX = getChildCount() * getWidth() + getScrollX();
            } else if (getScrollX() >= mMaxScrollX && mCurScreen == getChildCount() - 1 && whichPage == 0) {
                mUnboundedScrollX -= mMaxScrollX + getWidth();
            }
        }
        
        int delta = newX - mUnboundedScrollX;
        int duration = 0;


        /// M: modify to cycle sliding screen.
        if (!isSupportCycleSlidingScreen() && Math.abs(velocity) < mMinFlingVelocity) {
            // If the velocity is low enough, then treat this more as an automatic page advance
            // as opposed to an apparent physical response to flinging
            /*Log.i(TAG, "snapToPageWithVelocity: velocity = " + velocity + ", whichPage = "
                    + whichPage + ", MIN_FLING_VELOCITY = " + MIN_FLING_VELOCITY + ", this = " + this);*/
            snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
            //Log.i("HHJ","564  ");
            return;
        }

        float distanceRatio = Math.min(1f, 1.0f * Math.abs(delta) / (2 * halfScreenSize));
        float distance = halfScreenSize + halfScreenSize * distanceInfluenceForSnapDuration(distanceRatio);

        velocity = Math.abs(velocity);
        velocity = Math.max(mMinSnapVelocity, velocity);

        // we want the page's snap velocity to approximately match the velocity at which the
        // user flings, so we scale the duration by a value near to the derivative of the scroll
        // interpolator at zero, ie. 5. We use 4 to make it a little slower.
        duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        duration = Math.min(duration, MAX_PAGE_SNAP_DURATION);

        snapToPage(whichPage, delta, duration);
    }

    int getPageNearestToCenterOfScreen() {
        int minDistanceFromScreenCenter = Integer.MAX_VALUE;
        int minDistanceFromScreenCenterIndex = -1;
        int screenCenter = getScrollX() + (getMeasuredWidth() / 2);
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View layout = (View) getPageAt(i);
            int childWidth = getScaledMeasuredWidth(layout);
            int halfChildWidth = (childWidth / 2);
            int childCenter = getChildOffset(i) + halfChildWidth;
            int distanceFromScreenCenter = Math.abs(childCenter - screenCenter);
            if (distanceFromScreenCenter < minDistanceFromScreenCenter) {
                minDistanceFromScreenCenter = distanceFromScreenCenter;
                minDistanceFromScreenCenterIndex = i;
            }
        }
        return minDistanceFromScreenCenterIndex;
    }
    
    protected void snapToPage(int whichPage, int duration) {
        whichPage = Math.max(0, Math.min(whichPage, getPageCount() - 1));

        int newX = getChildOffset(whichPage) - getRelativeChildOffset(whichPage);
        
        if (newX == mUnboundedScrollX) {
            return;
        }

        if (isSupportCycleSlidingScreen()) {
            if (getScrollX() <= 0 && mCurScreen == 0 && whichPage == getChildCount() - 1) {
                mUnboundedScrollX = getChildCount() * getWidth() + getScrollX();
            } else if (getScrollX() >= mMaxScrollX && mCurScreen == getChildCount() - 1 && whichPage == 0) {
                mUnboundedScrollX -= mMaxScrollX + getWidth();
            }
        }

        final int sX = mUnboundedScrollX;
        final int delta = newX - sX;
        
        snapToPage(whichPage, delta, duration);
    }
    
 // We want the duration of the page snap animation to be influenced by the distance that
    // the screen has to travel, however, we don't want this duration to be effected in a
    // purely linear fashion. Instead, we use this method to moderate the effect that the distance
    // of travel has on the overall snap duration.
    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }
    
    
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(mLastMotionX - x);
            if (xDiff > mTouchSlop) {
                mTouchState = TOUCH_STATE_SCROLLING;

            }
            break;

        case MotionEvent.ACTION_DOWN:
            down_x = x;
            mLastMotionX = x;
            mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
            final int xDist = Math.abs(mScroller.getFinalX() - mScroller.getCurrX());
            final boolean finishedScrolling = (mScroller.isFinished() || xDist < mTouchSlop);
            if (finishedScrolling) {
                mTouchState = TOUCH_STATE_REST;
                mScroller.abortAnimation();
            } else {
                mTouchState = TOUCH_STATE_SCROLLING;
            }
            setAllowOverScroll(true);
            break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mTouchState = TOUCH_STATE_REST;
            break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }
    
    /**
     * Return true if a tap at (x, y) should trigger a flip to the previous page.
     */
    protected boolean hitsPreviousPage(float x, float y) {
        //Log.i(TAG, "getRelativeChildOffset(mCurScreen)   "+getRelativeChildOffset(mCurScreen)+"     x:"+x);
        return (x < getRelativeChildOffset(mCurScreen) - mPageSpacing);
    }

    /**
     * Return true if a tap at (x, y) should trigger a flip to the next page.
     */
    protected boolean hitsNextPage(float x, float y) {
        //Log.i(TAG, "getMeasuredWidth()  "+getMeasuredWidth()+"     getRelativeChildOffset(mCurScreen):"+getRelativeChildOffset(mCurScreen)+"   x:"+x);
        return  (x > (getMeasuredWidth() - getRelativeChildOffset(mCurScreen) + mPageSpacing));
    }
    
    
    protected void snapToPage(int whichPage) {
        snapToPage(whichPage, PAGE_SNAP_ANIMATION_DURATION);
    }
    
    protected void snapToPage(int whichPage, int delta, int duration) {
        mNextPage = whichPage;

        View focusedChild = getFocusedChild();
        if (focusedChild != null && whichPage != mCurScreen &&
                focusedChild == getPageAt(mCurScreen)) {
            focusedChild.clearFocus();
        }

        awakenScrollBars(duration);
        if (duration == 0) {
            duration = Math.abs(delta);
        }

        if (!mScroller.isFinished()) mScroller.abortAnimation();
        mScroller.startScroll(mUnboundedScrollX, 0, delta, 0, duration);
        mCurScreen = whichPage;
        invalidate();
        
    }
    
    public Scroller getScroller(){
        return mScroller;
    }
    
    protected int getChildOffset(int index) {
        int[] childOffsets = Float.compare(mLayoutScale, 1f) == 0 ?
                mChildOffsets : mChildOffsetsWithLayoutScale;

        if (childOffsets != null && childOffsets[index] != -1) {
            return childOffsets[index];
        } else {
            if (getChildCount() == 0)
                return 0;

            int offset = getRelativeChildOffset(0);
            for (int i = 0; i < index; ++i) {
                offset += getScaledMeasuredWidth(getPageAt(i)) + mPageSpacing;
            }
            if (childOffsets != null) {
                childOffsets[index] = offset;
            }
            return offset;
        }
    }
    
    public void setPageSpacing(int pageSpacing) {
        mPageSpacing = pageSpacing;
    }

    protected int getRelativeChildOffset(int index) {
        if (mChildRelativeOffsets != null && mChildRelativeOffsets[index] != -1) {
            return mChildRelativeOffsets[index];
        } else {
            final int padding = getPaddingLeft() + getPaddingRight();
            final int offset = getPaddingLeft() +
                    (getMeasuredWidth() - padding - getChildWidth(index)) / 2;
            if (mChildRelativeOffsets != null) {
                mChildRelativeOffsets[index] = offset;
            }
            return offset;
        }
    }
    
    View getPageAt(int index) {
        return getChildAt(index);
    }

    int getPageCount() {
        return getChildCount();
    }
    
    protected int getChildWidth(int index) {
        // This functions are called enough times that it actually makes a
        // difference in the
        // profiler -- so just inline the max() here
        if (getChildCount() > 0) {
            int measuredWidth;
            if(getPageAt(index) != null){
                measuredWidth  = getPageAt(index).getMeasuredWidth();
            }else{
                return 0;
            }
            final int minWidth = mMinimumWidth;
            return (minWidth > measuredWidth) ? minWidth : measuredWidth;
        }else{
            return 0;
        }
    }
    
    protected int getScaledMeasuredWidth(View child) {
        // This functions are called enough times that it actually makes a difference in the
        // profiler -- so just inline the max() here
        final int measuredWidth = child.getMeasuredWidth();
        final int minWidth = mMinimumWidth;
        final int maxWidth = (minWidth > measuredWidth) ? minWidth : measuredWidth;
        return (int) (maxWidth * mLayoutScale + 0.5f);
    }
    
    /**
     * hehangjun modify
     * 这个里面主要是绘制app与widget特效*/
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int halfScreenSize = getMeasuredWidth() / 2;
        int screenCenter = mOverScrollX + halfScreenSize;

        final int pageCount = getChildCount();

        if (pageCount > 0) {
            getVisiblePages(mTempVisiblePagesRange);
            final int leftScreen = mTempVisiblePagesRange[0];
            final int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen != -1 && rightScreen != -1) {
                final long drawingTime = getDrawingTime();
                // / M: modify to cycle sliding screen.
                if (isSupportCycleSlidingScreen() && rightScreen < leftScreen) {
                    canvas.save();
                    int width = this.getWidth();
                    int offset = pageCount * width;
                    if (getScrollX() > mMaxScrollX) {
                        drawChild(canvas, getPageAt(leftScreen), drawingTime);
                        canvas.translate(+offset, 0);
                        drawChild(canvas, getPageAt(rightScreen), drawingTime);
                        // canvas.translate(-offset, 0);
                    } else if (getScrollX() < 0) {
                        drawChild(canvas, getPageAt(rightScreen), drawingTime);
                        canvas.translate(-offset, 0);
                        drawChild(canvas, getPageAt(leftScreen), drawingTime);
                        // canvas.translate(+offset, 0);
                    }
                    canvas.restore();
                } else {
                    // Clip to the bounds
                    canvas.save();
                    canvas.clipRect(getScrollX(), getScrollY(), getScrollX() + getRight() - getLeft(), getScrollY() + getBottom() - getTop());

                    for (int i = getChildCount() - 1; i >= 0; i--) {
                        final View v = getPageAt(i);
                        if (mForceDrawAllChildrenNextFrame || (leftScreen <= i && i <= rightScreen && shouldDrawChild(v))) {
                            drawChild(canvas, v, drawingTime);
                        }
                    }
                    mForceDrawAllChildrenNextFrame = false;
                    canvas.restore();
                }
            }
        }
    }
    
    
    protected boolean shouldDrawChild(View child) {
        
        if(sdkVersion>14){
            return child.getAlpha() > 0;
        }else{
            return true;
        }
        
    }
    
    protected void getVisiblePages(int[] range) {
        final int pageCount = getChildCount();

        if (pageCount > 0) {
            // / M: modify to cycle sliding screen.
            if (isSupportCycleSlidingScreen() && (getScrollX() < 0 || getScrollX() > mMaxScrollX)) {
                range[0] = pageCount - 1;
                range[1] = 0;
            } else {
                final int screenWidth = getMeasuredWidth();
                int leftScreen = 0;
                int rightScreen = 0;
                View currPage = getPageAt(leftScreen);
                float currPageGetX=0;
                if(sdkVersion >= 14) {
                    currPageGetX = currPage.getX();
                } else {
                    currPageGetX = 0;
                }
                while (leftScreen < pageCount - 1 && currPageGetX + currPage.getWidth() - currPage.getPaddingRight() < getScrollX()) {
                    leftScreen++;
                    currPage = getPageAt(leftScreen);
                }
                rightScreen = leftScreen;
                currPage = getPageAt(rightScreen + 1);
                while (rightScreen < pageCount - 1 && currPageGetX - currPage.getPaddingLeft() < getScrollX() + screenWidth) {
                    rightScreen++;
                    currPage = getPageAt(rightScreen + 1);
                }
                range[0] = leftScreen;
                range[1] = rightScreen;
            }
        } else {
            range[0] = -1;
            range[1] = -1;
        }
    }
    
    
    public boolean isSupportCycleSlidingScreen() {
        return true;
    }
    
    public interface TouchFinish{
        public void upDate(int about);
    }
    
    public TouchFinish mTouchFinish;
    
    public void setTouchFinish(TouchFinish touchFinish){
        this.mTouchFinish = touchFinish;
    }
}
