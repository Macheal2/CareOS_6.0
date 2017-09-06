package com.cappu.launcherwin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import java.util.ArrayList;
import java.util.Collections;

import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.Indicator;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.SparseArrayMap;


/**
 * The workspace is a wide area with a wallpaper and a finite number of pages.
 * Each page contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends SmoothPagedView implements View.OnTouchListener, ViewGroup.OnHierarchyChangeListener {
    private static final String TAG = "Workspace";

    private int mDefaultPage = 1;

    // State variable that indicates whether the pages are small (ie when you're
    // in all apps or customize mode)

    enum State {
        NORMAL, SPRING_LOADED, SMALL
    };
   
    private State mState = State.NORMAL;
    private boolean mIsSwitchingState = false;

    boolean mChildrenLayersEnabled = true;
    public static final int DRAG_BITMAP_PADDING = 2;

    enum WallpaperVerticalOffset {
        TOP, MIDDLE, BOTTOM
    };

    boolean mUpdateWallpaperOffsetImmediately = false;
    // Variables relating to touch disambiguation (scrolling workspace vs.
    // scrolling a widget)
    private float mXDown;
    private float mYDown;
    final static float START_DAMPING_TOUCH_SLOP_ANGLE = (float) Math.PI / 6;
    final static float MAX_SWIPE_ANGLE = (float) Math.PI / 3;
    final static float TOUCH_SLOP_DAMPING_FACTOR = 4;

    // Relating to the animation of items being dropped externally
    public static final int ANIMATE_INTO_POSITION_AND_DISAPPEAR = 0;
    public static final int ANIMATE_INTO_POSITION_AND_REMAIN = 1;
    public static final int ANIMATE_INTO_POSITION_AND_RESIZE = 2;
    public static final int COMPLETE_TWO_STAGE_WIDGET_DROP_ANIMATION = 3;
    public static final int CANCEL_TWO_STAGE_WIDGET_DROP_ANIMATION = 4;

    private float mTransitionProgress;
    //hejianfeng add start
    private static final int DEFAULT_MAX_PAGE=6;
    //hejianfeng add end

    /**
     * Used to inflate the Workspace from XML.
     * 
     * @param context
     *            The application's context.
     * @param attrs
     *            The attributes set containing the Workspace's customization
     *            values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Used to inflate the Workspace from XML.
     * 
     * @param context
     *            The application's context.
     * @param attrs
     *            The attributes set containing the Workspace's customization
     *            values.
     * @param defStyle
     *            Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContentIsRefreshable = false;
        // With workspace, data is available straight from the get-go
        setDataIsReady();
        mFadeInAdjacentScreens = false;
        setOnHierarchyChangeListener(this);
        setHapticFeedbackEnabled(false);
        initWorkspace();
    }

    /**
     * Initializes various states for this workspace.
     */
    protected void initWorkspace() {
        mCurrentPage = mDefaultPage;
        setWillNotDraw(false);
        setChildrenDrawnWithCacheEnabled(true);
    }

    @Override
    protected int getScrollMode() {
        return SmoothPagedView.X_LARGE_MODE;
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    }

    boolean isTouchActive() {
        return mTouchState != TOUCH_STATE_REST;
    }
    
    boolean isTouchScrolling(){
        return mTouchState == TOUCH_STATE_SCROLLING;
    }
    
    int getTouchState(){
        return mTouchState;
    }
    
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.i(TAG, "进入dispatchTouchEvent");
        
        final int action = ev.getAction();
        if(mDragController == null){
            return super.dispatchTouchEvent(ev);
        }
        Log.i(TAG, "onTouchEvent -------- :"+mDragController.getDragMode());
        if(action == MotionEvent.ACTION_MOVE){
            if(mDragController.getDragMode() == DragController.Mode_Drag){
                Log.i(TAG, "onTouchEvent --------");
                return true;
            }else{
                return super.dispatchTouchEvent(ev);
            }
            
        }else {
            return super.dispatchTouchEvent(ev);
        }
        
    }

    /**
     * Called directly from a CellLayout (not by the framework), after we've
     * been added as a listener via setOnInterceptTouchEventListener(). This
     * allows us to tell the CellLayout that it should intercept touch events,
     * which is not something that is normally supported.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return (isSmall() || !isFinishedSwitchingState());
    }

    /**
     * This differs from isSwitchingState in that we take into account how far
     * the transition has completed.
     */
    public boolean isFinishedSwitchingState() {
        return !mIsSwitchingState || (mTransitionProgress > 0.5f);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (isSmall() || !isFinishedSwitchingState()) {
            // when the home screens are shrunken, shouldn't allow
            // side-scrolling
            return false;
        }
        return super.dispatchUnhandledMove(focused, direction);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (mLauncher.getPopupWindowMenu().isShowing()) {
//            return true;
//        }
       final int action = ev.getAction();
       if(action == MotionEvent.ACTION_UP){
            boolean r = super.onTouchEvent(ev);
            mLauncher.launcherSendBroadcastReceiver(false,null);
            return r;
        }else{
            return super.onTouchEvent(ev);
        }
        
    }
    
    public void setAllowOverScroll(boolean allowOverScroll){
        mAllowOverScroll = allowOverScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mXDown = ev.getX();
            mYDown = ev.getY();
            break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void determineScrollingStart(MotionEvent ev) {
        if (isSmall())
            return;
        if (!isFinishedSwitchingState())
            return;

        float deltaX = Math.abs(ev.getX() - mXDown);
        float deltaY = Math.abs(ev.getY() - mYDown);

        if (Float.compare(deltaX, 0f) == 0)
            return;

        float slope = deltaY / deltaX;
        float theta = (float) Math.atan(slope);

        if (deltaX > mTouchSlop || deltaY > mTouchSlop) {
            cancelCurrentPageLongPress();
        }

        if (theta > MAX_SWIPE_ANGLE) {
            // Above MAX_SWIPE_ANGLE, we don't want to ever start scrolling the
            // workspace
            return;
        } else if (theta > START_DAMPING_TOUCH_SLOP_ANGLE) {
            // Above START_DAMPING_TOUCH_SLOP_ANGLE and below MAX_SWIPE_ANGLE,
            // we want to
            // increase the touch slop to make it harder to begin scrolling the
            // workspace. This
            // results in vertically scrolling widgets to more easily. The
            // higher the angle, the
            // more we increase touch slop.
            theta -= START_DAMPING_TOUCH_SLOP_ANGLE;
            float extraRatio = (float) Math.sqrt((theta / (MAX_SWIPE_ANGLE - START_DAMPING_TOUCH_SLOP_ANGLE)));
            super.determineScrollingStart(ev, 1 + TOUCH_SLOP_DAMPING_FACTOR * extraRatio);
        } else {
            // Below START_DAMPING_TOUCH_SLOP_ANGLE, we don't do anything
            // special
            super.determineScrollingStart(ev);
        }
    }

    @Override
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        updateIndicatorPosition();
    };

    @Override
    protected void updateCurrentPageScroll() {
        super.updateCurrentPageScroll();
    }

    @Override
    protected void snapToPage(int whichPage) {
        super.snapToPage(whichPage);
    }

    @Override
    protected void snapToPage(int whichPage, int duration) {
        super.snapToPage(whichPage, duration);
    }
    
    protected void snapToPage(int whichPage, int delta, int duration){
        super.snapToPage(whichPage,delta, duration);
    }
    

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    @Override
    protected void screenScrolled(int screenCenter) {
        super.screenScrolled(screenCenter);
        enableHwLayersOnVisiblePages();
    }

    @Override
    protected void overScroll(float amount) {
        acceleratedOverScroll(amount);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mFirstLayout && mCurrentPage >= 0 && mCurrentPage < getChildCount()) {
            mUpdateWallpaperOffsetImmediately = true;
        }
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return false;
    }

    @Override
    public int getDescendantFocusability() {
        if (isSmall()) {
            return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
        }
        return super.getDescendantFocusability();
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        super.addFocusables(views, direction, focusableMode);
    }

    public boolean isSmall() {
        return mState == State.SMALL || mState == State.SPRING_LOADED;
    }

    private void enableHwLayersOnVisiblePages() {
        if (mChildrenLayersEnabled) {
            final int screenCount = getChildCount();
            getVisiblePages(mTempVisiblePagesRange);
            int leftScreen = mTempVisiblePagesRange[0];
            int rightScreen = mTempVisiblePagesRange[1];
            if (leftScreen == rightScreen) {
                // make sure we're caching at least two pages always
                if (rightScreen < screenCount - 1) {
                    rightScreen++;
                } else if (leftScreen > 0) {
                    leftScreen--;
                }
            }
        }
    }


    void moveToDefaultScreen(boolean animate) {
        if (!isSmall()) {
            if (animate) {
                snapToPage(getDefaultPage());
            } else {
                setCurrentPage(getDefaultPage());
            }
        }
        if(getChildAt(getDefaultPage()) !=null){
            getChildAt(getDefaultPage()).requestFocus();
        }
        
    }

    @Override
    public void syncPages() {
    }

    @Override
    public void syncPageItems(int page, boolean immediate) {
    }

    public boolean isSupportCycleSlidingScreen() {
        return mLauncher.isSupportCycleSlidingScreen();
    }
    
    /*hehangjun modify*/
    
    private Launcher mLauncher;
    private DragController mDragController;
    Indicator mIndicator;
    private long mTargetId = -1;
    // 默认的显示视图
    private int mDefaultScreen = 1;
    
    public void setLauncher(Launcher launcher){
        this.mLauncher = launcher;
    }
    
    void setDragController(DragController dragController){
        this.mDragController = dragController;
    }
    
    void setIndicator(Indicator indicator){
        mIndicator = indicator;
        setIndicatorCount(getChildCount());
        updateIndicatorPosition();
    }
    
    public void setIndicatorCount(int cout){
        mIndicator.setCount(cout);
    }
    
    public void updateIndicatorPosition(){
        if(getChildCount()<=0){
            return;
        }
        setIndicatorCount(getChildCount());
        mIndicator.setCurrentPosition(getCurScreen());
    }
    
    /**设置当前焦点ID*/
    public void setTargetId(long id){
        mTargetId = id;
    }
    
    /**获取当前焦点ID*/
    public long getTargetId(){
        return mTargetId;
    }
    
    public void setDefaultScreen(int c){
        mDefaultScreen = c;
        setModeChangeCurrentPage(mDefaultScreen);
        updateIndicatorPosition();
    }
    
    public Scroller getScroller(){
        return mScroller;
    }
    
    public int getCurScreen() {
        return mCurrentPage;
    }
    
    boolean isDefaultScreenShowing() {
        return mCurrentPage == mDefaultScreen;
    }
    //hejianfeng add start
    private SparseArrayMap<CellLayout> mWorkspaceScreens = new SparseArrayMap<CellLayout>();
    private ArrayList<Integer> mScreenOrder = new ArrayList<Integer>();
    public SparseArrayMap<CellLayout> getWorkspaceScreens(){
    	return mWorkspaceScreens;
    }
    public ArrayList<Integer> getScreenOrder(){
    	return mScreenOrder;
    }
	public boolean isNegativeScreen() {
		return Settings.Global.getInt(mLauncher.getContentResolver(),
				"add_contacts_screen", 0) == 1
				&& ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS;
	}
    public void removeAllWorkspaceScreens() {
    	removeAllViews();
    	mWorkspaceScreens.clear();
    	mScreenOrder.clear();
    }
    public int getDefaultMaxPage(){
    	return isNegativeScreen() ? DEFAULT_MAX_PAGE+1:DEFAULT_MAX_PAGE;
    }
    public int getDefaultPage(){
    	return isNegativeScreen() ? mDefaultPage+1:mDefaultPage;
    }
    //hejianfeng add end
    void addInScreen(View child, int screen, int cellX, int cellY, int spanX, int spanY, boolean insert) {
    	LauncherLog.v(TAG, "addInScreen,jeff screen="+screen+",cellX="+cellX+",cellY="+cellY);
		try {
			if (!mWorkspaceScreens.containsKey(screen)) {
				addCellLayoutInWorkSpace(screen);
			}
			CellLayout group = mWorkspaceScreens.get(screen);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child
					.getLayoutParams();
			if (lp == null) {
				lp = new CellLayout.LayoutParams(cellX, cellY, spanX, spanY,
						screen);
			} else {
				lp.cellX = cellX;
				lp.cellY = cellY;
				lp.cellHSpan = spanX;
				lp.cellVSpan = spanY;
				lp.screen = screen;
			}
			if (group != null) {
				group.addView(child, lp);
			}
		} catch (Exception e) {
			Log.v(TAG, "addInScreen Exception");
		}
    }
    
    private void addCellLayoutInWorkSpace(int screen){
        CellLayout cellLayout = new CellLayout(mLauncher);
        cellLayout.setLongClickable(true);
        mWorkspaceScreens.put(screen, cellLayout);
        mScreenOrder.add(screen);
    }

	public void updateWorkSpaceUI() {
		removeAllViews();
		Collections.sort(mScreenOrder);
		int i=0;
		if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS){
			i = isNegativeScreen() ? 0 : 1;
		}
		for (; i < mWorkspaceScreens.size(); i++) {
			LauncherLog.v(TAG, "updateWorkSpaceUI,cellLayout i="+i+"="+mWorkspaceScreens.get(i));
			addView(mWorkspaceScreens.get(mScreenOrder.get(i)));
		}
		setDefaultScreen(getDefaultPage());
		requestLayout();
	}
}
