/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardStatusView;
import com.android.systemui.DejankUtils;
import com.android.systemui.EventLogConstants;
import com.android.systemui.EventLogTags;
import com.android.systemui.R;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.StackStateAnimator;

import com.mediatek.keyguard.Clock.ClockView;

//added by yzs for MagcommLocker begin
import com.cappu.keyguard.CappuBootUnlockView;
import com.magcomm.keyguard.MagcommBootUnlockView;
import com.android.internal.widget.LockPatternUtils;
//added by yzs for MagcommLocker end
import android.util.Log;
/* begin: add by hmq 20160504状态栏分2页显示 */
import android.content.ComponentName;
import android.content.Intent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.systemui.statusbar.phone.StatusBarHeaderView.NotificationSwitchPageCallback;
/* end: add by hmq 20160504状态栏分2页显示 */
public class NotificationPanelView extends PanelView implements
        ExpandableView.OnHeightChangedListener, ObservableScrollView.Listener,
        View.OnClickListener, NotificationStackScrollLayout.OnOverscrollTopChangedListener,
        KeyguardAffordanceHelper.Callback, NotificationStackScrollLayout.OnEmptySpaceClickListener,
        HeadsUpManager.OnHeadsUpChangedListener {

    private static final boolean DEBUG = false;

    // Cap and total height of Roboto font. Needs to be adjusted when font for the big clock is
    // changed.
    private static final int CAP_HEIGHT = 1456;
    private static final int FONT_HEIGHT = 2163;

    private static final float HEADER_RUBBERBAND_FACTOR = 2.05f;
    private static final float LOCK_ICON_ACTIVE_SCALE = 1.2f;

    private static final String COUNTER_PANEL_OPEN = "panel_open";
    private static final String COUNTER_PANEL_OPEN_QS = "panel_open_qs";
    private static final String COUNTER_PANEL_OPEN_PEEK = "panel_open_peek";

    private static final Rect mDummyDirtyRect = new Rect(0, 0, 1, 1);

    public static final long DOZE_ANIMATION_DURATION = 700;

    // added by yzs for MagcommLocker begin
    private CappuBootUnlockView mCappuBootUnlockView;
    private MagcommBootUnlockView mMagcommBootUnlockView;
    // added by yzs for MagcommLocker end

    private KeyguardAffordanceHelper mAfforanceHelper;
    private StatusBarHeaderView mHeader;//hmq 标题VIEW
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private KeyguardStatusBarView mKeyguardStatusBar;
    private QSContainer mQsContainer;//hmq path 1里面的具体QS view
    private QSPanel mQsPanel;//hmq path 1里 mQsContainer具体QS view
    private KeyguardStatusView mKeyguardStatusView;//hmq 锁屏的view
    private ObservableScrollView mScrollView;//hmq path 1

    private TextView mClockView;
    private View mReserveNotificationSpace;
    private View mQsNavbarScrim;
    private NotificationsQuickSettingsContainer mNotificationContainerParent;
    private NotificationStackScrollLayout mNotificationStackScroller;//hmq path 2
    private int mNotificationTopPadding;//hmq 0dip 通知栏距上一个控件高度
    private boolean mAnimateNextTopPaddingChange;
    /* begin: add by hmq 20160504状态栏分2页显示 */
    private LinearLayout mSwitchToolBar;//switch_tools_bar
    private Button mBtnSwitchL;//switch_left
    private Button mBtnSwitchR;//switch_right
    private RelativeLayout mDragScrollBar;//drag_scroll_bar
    private ImageView mDragScrollLineImg;//drag_scroll_line_img
    private ImageView mDragScrollupImg;//drag_scroll_up_img
    private AlphaAnimation mDragScrollImgAlphaToMaxAnim;
    private AlphaAnimation mDragScrollImgAlphaToMinAnim;
    /* end: add by hmq 20160504状态栏分2页显示 */
    private int mTrackingPointer;
    private VelocityTracker mVelocityTracker;//跟踪滑动速度的一个类
    private boolean mQsTracking;

    /**
     * Handles launching the secure camera properly even when other applications may be using the
     * camera hardware.
     */
    private SecureCameraLaunchManager mSecureCameraLaunchManager;

    /**
     * If set, the ongoing touch gesture might both trigger the expansion in {@link PanelView} and
     * the expansion for quick settings.
     */
    private boolean mConflictingQsExpansionGesture;

    /**
     * Whether we are currently handling a motion gesture in #onInterceptTouchEvent, but haven't
     * intercepted yet.
     */
    private boolean mIntercepting;//hmq 是否还是初始状态，当有mQsExpansionAnimator和touch move的时候false
    private boolean mPanelExpanded;
    private boolean mQsExpanded;//hmq QS是否可被拉升的状态     false QS未被拉伸     true QS已经被拉伸
    private boolean mQsExpandedWhenExpandingStarted;
    private boolean mQsFullyExpanded;//是否已经拉升大最大
    private boolean mKeyguardShowing;
    private boolean mDozing;
    private boolean mDozingOnDown;
    private int mStatusBarState;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;//hmq touch down坐标X 初始
    private float mInitialTouchY;//hmq touch down坐标Y 初始
    private float mLastTouchX;
    private float mLastTouchY;
    private float mQsExpansionHeight;//hmq 当前QS设置的高度
    private int mQsMinExpansionHeight;//hmq 最小QS高度 一个相对固定的高度，值只有2种状态，一个锁屏，一个是其他。
    private int mQsMaxExpansionHeight;//hmq 最大QS高度
    private int mQsPeekHeight;//hmq 与header高度之间的间距 0dip
    private boolean mStackScrollerOverscrolling;//hmq 拖动notification 过量拉升，现象:notification上划的时候，不要放手，再往下滑，划出QS的时候状态为true,其余时间为false
    private boolean mQsExpansionFromOverscroll;
    private float mLastOverscroll;
    private boolean mQsExpansionEnabled = true;//hmq 控件是否可以活动 true可以点击事件，false不可响应点击事件
    private ValueAnimator mQsExpansionAnimator;//hmq 获得当前的动画类
    private FlingAnimationUtils mFlingAnimationUtils;
    private int mStatusBarMinHeight;// hmq 40dip 状态栏高度
    private boolean mUnlockIconActive;
    private int mNotificationsHeaderCollideDistance;
    private int mUnlockMoveDistance;
    private float mEmptyDragAmount;

    private Interpolator mFastOutSlowInInterpolator;
    private Interpolator mFastOutLinearInterpolator;
    private Interpolator mDozeAnimationInterpolator;
    private ObjectAnimator mClockAnimator;
    private int mClockAnimationTarget = -1;
    private int mTopPaddingAdjustment;
    private KeyguardClockPositionAlgorithm mClockPositionAlgorithm =
            new KeyguardClockPositionAlgorithm();
    private KeyguardClockPositionAlgorithm.Result mClockPositionResult =
            new KeyguardClockPositionAlgorithm.Result();
    private boolean mIsExpanding;

    private boolean mBlockTouches;
    private int mNotificationScrimWaitDistance;
    // Used for two finger gesture as well as accessibility shortcut to QS.
    private boolean mQsExpandImmediate;//hmq 立即展开
    private boolean mTwoFingerQsExpandPossible;

    /**
     * If we are in a panel collapsing motion, we reset scrollY of our scroll view but still
     * need to take this into account in our panel height calculation.
     */
    private int mScrollYOverride = -1;
    private boolean mQsAnimatorExpand;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private Runnable mLaunchAnimationEndRunnable;
    private boolean mOnlyAffordanceInThisMotion;
    private boolean mKeyguardStatusViewAnimating;
    private boolean mHeaderAnimating;
    private ObjectAnimator mQsContainerAnimator;
    private ValueAnimator mQsSizeChangeAnimator;//hmq qs 尺寸变化动画

    private boolean mShadeEmpty;

    private boolean mQsScrimEnabled = true;
    private boolean mLastAnnouncementWasQuickSettings;
    private boolean mQsTouchAboveFalsingThreshold;
    private int mQsFalsingThreshold;

    private float mKeyguardStatusBarAnimateAlpha = 1f;
    private int mOldLayoutDirection;
    private HeadsUpTouchHelper mHeadsUpTouchHelper;
    private boolean mIsExpansionFromHeadsUp;
    private boolean mListenForHeadsUp;
    private int mNavigationBarBottomHeight;
    private boolean mExpandingFromHeadsUp;
    private boolean mCollapsedOnDown;
    private int mPositionMinSideMargin;
    private int mLastOrientation = -1;
    private boolean mClosingWithAlphaFadeOut;
    private boolean mHeadsUpAnimatingAway;

    /// M: For customize clock
    private ClockView mMtkClockView;

    /// M: A1 support
    private static boolean bA1Support =
            SystemProperties.get("ro.mtk_a1_feature").equals("1");

    private Runnable mHeadsUpExistenceChangedRunnable = new Runnable() {
        @Override
        public void run() {
            mHeadsUpAnimatingAway = false;
            notifyBarPanelExpansionChanged();
        }
    };

    /** Interpolator to be used for animations that respond directly to a touch */
    private final Interpolator mTouchResponseInterpolator =
            new PathInterpolator(0.3f, 0f, 0.1f, 1f);

    private LockPatternUtils mLockPatternUtils;//added by yzs for MagcommLocker 
    private boolean isFullHeight = false;//add by hmq 20160504状态栏分2页显示 
    private boolean isHeaderClick = false;//add by hmq 20160504状态栏分2页显示 
    private int mScreenWidth;//add by hmq 20160504状态栏分2页显示 
    private int mScreenHeight;//add by hmq 20160504状态栏分2页显示 
    
    public NotificationPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLockPatternUtils = new LockPatternUtils(context);//added by yzs for MagcommLocker
        setWillNotDraw(!DEBUG);
    }

    public void setStatusBar(PhoneStatusBar bar) {
        mStatusBar = bar;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeader = (StatusBarHeaderView) findViewById(R.id.header);
//        mHeader.setOnClickListener(this);//hide by hmq 20160504状态栏分2页显示 header onClickListener
        mKeyguardStatusBar = (KeyguardStatusBarView) findViewById(R.id.keyguard_header);
        mKeyguardStatusView = (KeyguardStatusView) findViewById(R.id.keyguard_status_view);
        mQsContainer = (QSContainer) findViewById(R.id.quick_settings_container);
        mQsPanel = (QSPanel) findViewById(R.id.quick_settings_panel);

        if (bA1Support) {
            mClockView = (TextView) findViewById(R.id.clock_view);
        } else {
            mMtkClockView = (ClockView) findViewById(R.id.clock_view);
        }

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);//hmq path 1
        mScrollView.setListener(this);
        mScrollView.setFocusable(false);
        mReserveNotificationSpace = findViewById(R.id.reserve_notification_space);
        mNotificationContainerParent = (NotificationsQuickSettingsContainer)
                findViewById(R.id.notification_container_parent);
        mNotificationStackScroller = (NotificationStackScrollLayout)
                findViewById(R.id.notification_stack_scroller);//hmq path 2
        mNotificationStackScroller.setOnHeightChangedListener(this);
        mNotificationStackScroller.setOverscrollTopChangedListener(this);
        mNotificationStackScroller.setOnEmptySpaceClickListener(this);
        mNotificationStackScroller.setScrollView(mScrollView);
        mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_slow_in);
        mFastOutLinearInterpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_linear_in);
        mDozeAnimationInterpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.linear_out_slow_in);
        mKeyguardBottomArea = (KeyguardBottomAreaView) findViewById(R.id.keyguard_bottom_area);
        mQsNavbarScrim = findViewById(R.id.qs_navbar_scrim);
        mAfforanceHelper = new KeyguardAffordanceHelper(this, getContext());
        mSecureCameraLaunchManager =
                new SecureCameraLaunchManager(getContext(), mKeyguardBottomArea);
        
        /* begin: add by hmq 20160504状态栏分2页显示 */
        mSwitchToolBar = (LinearLayout) findViewById(R.id.switch_tools_bar);
        mBtnSwitchL = (Button) findViewById(R.id.switch_left);
        mBtnSwitchL.setOnClickListener(this);
        mBtnSwitchR = (Button) findViewById(R.id.switch_right);
        mBtnSwitchR.setOnClickListener(this);
        mDragScrollBar = (RelativeLayout) findViewById(R.id.drag_scroll_bar);
        mDragScrollLineImg = (ImageView) findViewById(R.id.drag_scroll_line_img);
        mDragScrollLineImg.setVisibility(View.VISIBLE);
        mDragScrollupImg = (ImageView) findViewById(R.id.drag_scroll_up_img);
        mDragScrollupImg.setVisibility(View.INVISIBLE);
        
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenHeight = wm.getDefaultDisplay().getHeight();
        /* end: add by hmq 20160504状态栏分2页显示 */
        mLastOrientation = getResources().getConfiguration().orientation;

        // recompute internal state when qspanel height changes
        mQsContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                final int height = bottom - top;
                final int oldHeight = oldBottom - oldTop;
                if (height != oldHeight) {
                    onScrollChanged();
                }
            }
        });
        /* begin: add by hmq 20160504状态栏分2页显示 */
        mHeader.setCallback(new NotificationSwitchPageCallback() {
            public void onNotificationSwitchPage() {
                if(!isFullyExpanded())
                    return;
                
                if(mHeader.getSwitchPageButton() != null){
                    if (mHeader.getSwitchPageButton().getText().equals(getResources().getString(R.string.status_bar_settings_settings_button))){
//                        startActivity(new Intent(this, LauncherSettingActivity.class));
                        ComponentName Comp = new ComponentName("com.cappu.launcherwin", "com.cappu.launcherwin.LauncherSettingActivity");
                        Intent intent = new Intent();
                        intent.setComponent(Comp);
                        intent.setAction("android.intent.action.MAIN");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        mStatusBar.animateCollapsePanels();//关闭下拉notification
                        isHeaderClick = true;
                    }else if (mHeader.getSwitchPageButton().getText().equals(getResources().getString(R.string.status_bar_clear_all_button))){
                        if (mStatusBar.hasActiveClearableNotifications()){
                            mStatusBar.activityClearAllNotifications();
                            isHeaderClick = true;
                        }
                    }
                }
            }
        });
        /* end: add by hmq 20160504状态栏分2页显示 */
    }

    @Override
    protected void loadDimens() {
        super.loadDimens();
        mNotificationTopPadding = getResources().getDimensionPixelSize(
                R.dimen.notifications_top_padding);//hmq 0dip 通知栏距上一个控件高度
        mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.4f);
        mStatusBarMinHeight = getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);//hmq 40dip 状态栏高度
        mQsPeekHeight = getResources().getDimensionPixelSize(R.dimen.qs_peek_height);//hmq 与header高度之间的间距 0dip
        mNotificationsHeaderCollideDistance =
                getResources().getDimensionPixelSize(R.dimen.header_notifications_collide_distance);
        mUnlockMoveDistance = getResources().getDimensionPixelOffset(R.dimen.unlock_move_distance);
        mClockPositionAlgorithm.loadDimens(getResources());
        mNotificationScrimWaitDistance =
                getResources().getDimensionPixelSize(R.dimen.notification_scrim_wait_distance);
        mQsFalsingThreshold = getResources().getDimensionPixelSize(
                R.dimen.qs_falsing_threshold);
        mPositionMinSideMargin = getResources().getDimensionPixelSize(
                R.dimen.notification_panel_min_side_margin);
    }

    public void updateResources() {
        int panelWidth = getResources().getDimensionPixelSize(R.dimen.notification_panel_width);//1ps
        int panelGravity = getResources().getInteger(R.integer.notification_panel_layout_gravity);//fill_horizontal|top
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeader.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mHeader.setLayoutParams(lp);
            mHeader.post(mUpdateHeader);
        }

        lp = (FrameLayout.LayoutParams) mNotificationStackScroller.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mNotificationStackScroller.setLayoutParams(lp);
        }

        lp = (FrameLayout.LayoutParams) mScrollView.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mScrollView.setLayoutParams(lp);
        }
        
        /* begin: add by hmq 20160504状态栏分2页显示 */
        lp = (FrameLayout.LayoutParams) mSwitchToolBar.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mSwitchToolBar.setLayoutParams(lp);
        }
        
        lp = (FrameLayout.LayoutParams) mDragScrollBar.getLayoutParams();
        if (lp.width != panelWidth) {
            lp.width = panelWidth;
            lp.gravity = panelGravity;
            mDragScrollBar.setLayoutParams(lp);
        }
        /* end: add by hmq 20160504状态栏分2页显示 */
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        /*edit by hmq 20160504状态栏分2页显示 横屏自动收缩下拉状态栏*/
        if (getOrientationScreen() && mExpandedHeight != 0){
            mExpandedHeight = 0;
            mStatusBar.onBackPressed();
        }
        /*edit by hmq 20160504状态栏分2页显示 横屏自动收缩下拉状态栏*/
        
        // Update Clock Pivot
        mKeyguardStatusView.setPivotX(getWidth() / 2);
        /// M: We use "getHeight()" instead of "getTextSize()" since the ClockView
        /// is implemented by our own and does not have "getTextSize()".
        /// Our own ClockView will display AM/PM by default.
        if (bA1Support) {
            mKeyguardStatusView.setPivotY(
                    (FONT_HEIGHT - CAP_HEIGHT) / 2048f * mClockView.getTextSize());
        } else {
            mKeyguardStatusView.setPivotY(
                    (FONT_HEIGHT - CAP_HEIGHT) / 2048f * mMtkClockView.getHeight());
        }

        // Calculate quick setting heights.
        int oldMaxHeight = mQsMaxExpansionHeight;
        mQsMinExpansionHeight = mKeyguardShowing ? 0 : mHeader.getCollapsedHeight() + mQsPeekHeight;//hmq 锁屏 ？ 0 ： header高度 + 高度间距
        mQsMaxExpansionHeight = mScreenHeight; /*edit by hmq 20160504状态栏分2页显示 QS最大Y值(整体高度)
        mQsMaxExpansionHeight =mHeader.getExpandedHeight() + mQsContainer.getDesiredHeight();//hmq header高度 + qs内容高度 hide 最大的QS高度 */
        positionClockAndNotifications();
        if (mQsExpanded && mQsFullyExpanded) {
            mQsExpansionHeight = mQsMaxExpansionHeight;
            requestScrollerTopPaddingUpdate(false /* animate */);
            requestPanelHeightUpdate();

            // Size has changed, start an animation.
            if (mQsMaxExpansionHeight != oldMaxHeight) {
                startQsSizeChangeAnimation(oldMaxHeight, mQsMaxExpansionHeight);
            }
        } else if (!mQsExpanded) {
            /*mQsExpanded false:这个标志位说明状态栏未被拉出来，那就把QS的X的高度设置为QS的最小高度+滚动冗余的长度*/
            //setQsExpansion(mQsMinExpansionHeight + mLastOverscroll); //edit by hmq 20160504状态栏分2页显示
        }
        updateStackHeight(getExpandedHeight());//hmq getExpandedHeight() notification的当前位置Y值
        updateHeader();
        /* begin: add by hmq 20160504状态栏分2页显示 */
        updateDragScrollBar();
        updateSwitchToolBar();
        /* end: add by hmq 20160504状态栏分2页显示 */
        mNotificationStackScroller.updateIsSmallScreen(
                mHeader.getCollapsedHeight() + mQsPeekHeight);
        mNotificationStackScroller.setNotificationHeightOffset(mSwitchToolBar.getHeight());//add by hmq 20160504状态栏分2页显示
        // If we are running a size change animation, the animation takes care of the height of
        // the container. However, if we are not animating, we always need to make the QS container
        // the desired height so when closing the QS detail, it stays smaller after the size change
        // animation is finished but the detail view is still being animated away (this animation
        // takes longer than the size change animation).
        if (mQsSizeChangeAnimator == null) {
            mQsContainer.setHeightOverride(getHeight()-mHeader.getHeight());/*edit by hmq 20160504状态栏分2页显示 修改QS的内容高度,QS的这个方法就是高度的重定义，如果不是-1，就读取传入的值作为高度
            mQsContainer.setHeightOverride(mQsContainer.getDesiredHeight());*/
        }
        updateMaxHeadsUpTranslation();
    }

    @Override
    public void onAttachedToWindow() {
        mSecureCameraLaunchManager.create();
    }

    @Override
    public void onDetachedFromWindow() {
        mSecureCameraLaunchManager.destroy();
    }

    private void startQsSizeChangeAnimation(int oldHeight, final int newHeight) {
        if (mQsSizeChangeAnimator != null) {
            oldHeight = (int) mQsSizeChangeAnimator.getAnimatedValue();
            mQsSizeChangeAnimator.cancel();
        }
        mQsSizeChangeAnimator = ValueAnimator.ofInt(oldHeight, newHeight);
        mQsSizeChangeAnimator.setDuration(300);
        mQsSizeChangeAnimator.setInterpolator(mFastOutSlowInInterpolator);
        mQsSizeChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                requestScrollerTopPaddingUpdate(false /* animate */);
                requestPanelHeightUpdate();
                int height = (int) mQsSizeChangeAnimator.getAnimatedValue();
                mQsContainer.setHeightOverride(getHeight()-mHeader.getHeight());/*edit by hmq 20160504状态栏分2页显示 修改QS的内容高度,QS的这个方法就是高度的重定义，如果不是-1，就读取传入的值作为高度
                mQsContainer.setHeightOverride(height - mHeader.getExpandedHeight());*/
            }
        });
        mQsSizeChangeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mQsSizeChangeAnimator = null;
            }
        });
        mQsSizeChangeAnimator.start();
    }

    /**
     * 位置的时钟和通知动态根据有多少通知显示
     * Positions the clock and notifications dynamically depending on how many notifications are
     * showing.
     */
    private void positionClockAndNotifications() {
        boolean animate = mNotificationStackScroller.isAddOrRemoveAnimationPending();
        int stackScrollerPadding;
        if (mStatusBarState != StatusBarState.KEYGUARD) {
            int bottom = mHeader.getCollapsedHeight();
            stackScrollerPadding = mStatusBarState == StatusBarState.SHADE
                    ? bottom + mQsPeekHeight + mNotificationTopPadding
                    : mKeyguardStatusBar.getHeight() + mNotificationTopPadding;
            mTopPaddingAdjustment = 0;
        } else {
            mClockPositionAlgorithm.setup(
                    mStatusBar.getMaxKeyguardNotifications(),
                    getMaxPanelHeight(),
                    getExpandedHeight(),
                    mNotificationStackScroller.getNotGoneChildCount(),
                    getHeight(),
                    mKeyguardStatusView.getHeight(),
                    mEmptyDragAmount);
            mClockPositionAlgorithm.run(mClockPositionResult);
            if (animate || mClockAnimator != null) {
                startClockAnimation(mClockPositionResult.clockY);
            } else {
                mKeyguardStatusView.setY(mClockPositionResult.clockY);
            }
            updateClock(mClockPositionResult.clockAlpha, mClockPositionResult.clockScale);
            stackScrollerPadding = mClockPositionResult.stackScrollerPadding;
            mTopPaddingAdjustment = mClockPositionResult.stackScrollerPaddingAdjustment;
        }
        mNotificationStackScroller.setIntrinsicPadding(stackScrollerPadding);
        requestScrollerTopPaddingUpdate(animate);
    }

    private void startClockAnimation(int y) {
        if (mClockAnimationTarget == y) {
            return;
        }
        mClockAnimationTarget = y;
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if (mClockAnimator != null) {
                    mClockAnimator.removeAllListeners();
                    mClockAnimator.cancel();
                }
                mClockAnimator = ObjectAnimator
                        .ofFloat(mKeyguardStatusView, View.Y, mClockAnimationTarget);
                mClockAnimator.setInterpolator(mFastOutSlowInInterpolator);
                mClockAnimator.setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD);
                mClockAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mClockAnimator = null;
                        mClockAnimationTarget = -1;
                    }
                });
                mClockAnimator.start();
                return true;
            }
        });
    }

    private void updateClock(float alpha, float scale) {
        if (!mKeyguardStatusViewAnimating) {
            mKeyguardStatusView.setAlpha(alpha);
        }
        mKeyguardStatusView.setScaleX(scale);
        mKeyguardStatusView.setScaleY(scale);
    }

    public void animateToFullShade(long delay) {
        mAnimateNextTopPaddingChange = true;
        mNotificationStackScroller.goToFullShade(delay);
        requestLayout();
    }

    /**
     * 
     * @param qsExpansionEnabled 布尔值mQsExpansionEnabled 是否激活活动QS & mHeader
     * 这个方法在PhoneStatusBar.java中调用
     */
    public void setQsExpansionEnabled(boolean qsExpansionEnabled) {
        mQsExpansionEnabled = qsExpansionEnabled;
        mHeader.setClickable(qsExpansionEnabled);
    }

    @Override
    public void resetViews() {
        mIsLaunchTransitionFinished = false;
        mBlockTouches = false;
        mUnlockIconActive = false;
        mAfforanceHelper.reset(true);
        closeQs();
        mStatusBar.dismissPopups();
        mNotificationStackScroller.setOverScrollAmount(0f, true /* onTop */, false /* animate */,
                true /* cancelAnimators */);
        mNotificationStackScroller.resetScrollPosition();
        resourceInit();//add by hmq 20160504状态栏分2页显示
    }

    public void closeQs() {
        cancelQsAnimation();
        setQsExpansion(mQsMinExpansionHeight);
    }

    public void animateCloseQs() {
        if (mQsExpansionAnimator != null) {
            if (!mQsAnimatorExpand) {
                return;
            }
            float height = mQsExpansionHeight;
            mQsExpansionAnimator.cancel();
            setQsExpansion(height);
        }
        flingSettings(0 /* vel */, false);
    }

    public void openQs() {
        cancelQsAnimation();
        if (mQsExpansionEnabled) {
            setQsExpansion(mQsMaxExpansionHeight);
        }
    }

    public void expandWithQs() {
        if (mQsExpansionEnabled) {
            mQsExpandImmediate = true;
        }
        expand();
    }

    @Override
    public void fling(float vel, boolean expand) {
        GestureRecorder gr = ((PhoneStatusBarView) mBar).mBar.getGestureRecorder();
        if (gr != null) {
            gr.tag("fling " + ((vel > 0) ? "open" : "closed"), "notifications,v=" + vel);
        }
        /* begin: add by hmq 20160504状态栏分2页显示 */
        if(expand){
            mQsExpandImmediate = expand;
        }
        if (vel == 0 && expand){
            setNotificationWhichPage();
        }
        /* end: add by hmq 20160504状态栏分2页显示 */
        super.fling(vel, expand);
    }

    @Override
    protected void flingToHeight(float vel, boolean expand, float target,
            float collapseSpeedUpFactor, boolean expandBecauseOfFalsing) {
        mHeadsUpTouchHelper.notifyFling(!expand);
        setClosingWithAlphaFadeout(!expand && getFadeoutAlpha() == 1.0f);
        super.flingToHeight(vel, expand, target, collapseSpeedUpFactor, expandBecauseOfFalsing);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add(getKeyguardOrLockScreenString());
            mLastAnnouncementWasQuickSettings = false;
            return true;
        }
        return super.dispatchPopulateAccessibilityEventInternal(event);
    }

    /**
     * android 自带是用于处理事件（类似于预处理，当然也可以不处理）并改变事件的传递方向，也就是决定是否允许Touch事件继续向下（子控件）传递。
     * 返回True（代表事件在当前的viewGroup中会被处理），则向下传递之路被截断（所有子控件将没有机会参与Touch事件），同时把事件传递给当前的控件的onTouchEvent()处理；
     * 返回false，则把事件交给子控件的onInterceptTouchEvent();
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mBlockTouches) {
            return false;
        }
        
        if (isHeaderClick)//add by hmq 20160504状态栏分2页显示 
            return false;
        
        if(getOrientationScreen())//add by hmq 20160504状态栏分2页显示 
            return false;
        
        if ((mCappuBootUnlockView != null || mMagcommBootUnlockView != null) && mKeyguardShowing){//added by yzs for MagcommLocker
            return false;//added by yzs for MagcommLocker
        }else {
            initDownStates(event);
            if (mHeadsUpTouchHelper.onInterceptTouchEvent(event)) {
                mIsExpansionFromHeadsUp = true;
                MetricsLogger.count(mContext, COUNTER_PANEL_OPEN, 1);
                MetricsLogger.count(mContext, COUNTER_PANEL_OPEN_PEEK, 1);
                return true;
            }
            if (!isFullyCollapsed() && onQsIntercept(event)) {
                return true;
            }
            return super.onInterceptTouchEvent(event);
        }//added by yzs for MagcommLocker
    }

    private boolean onQsIntercept(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mIntercepting = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                initVelocityTracker();
                trackMovement(event);
                if (shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, 0)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (mQsExpansionAnimator != null) {
                    onQsExpansionStarted();
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mQsTracking = true;
                    mIntercepting = false;
                    mNotificationStackScroller.removeLongPressCallback();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialTouchX = event.getX(newIndex);
                    mInitialTouchY = event.getY(newIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final float h = y - mInitialTouchY;
                trackMovement(event);
				    /* begin: add by hmq 20160504状态栏分2页显示 */
		            if (false){
//		            if (mQsTracking) {
				    /* end: add by hmq 20160504状态栏分2页显示 */

                    // Already tracking because onOverscrolled was called. We need to update here
                    // so we don't stop for a frame until the next touch event gets handled in
                    // onTouchEvent.
                    setQsExpansion(h + mInitialHeightOnTouch);
                    trackMovement(event);
                    mIntercepting = false;
                    return true;
                }
		        /* huangminqi隐藏这段代码目的是上划QS时，直接隐藏下拉状态栏:不隐藏这段代码上划QS结果是隐藏QS显示notification整个下拉状态栏 */
//              if (Math.abs(h) > mTouchSlop && Math.abs(h) > Math.abs(x - mInitialTouchX)
//                      && shouldQuickSettingsIntercept(mInitialTouchX, mInitialTouchY, h)) {
//                  mQsTracking = true;
//                  onQsExpansionStarted();
//                  mInitialHeightOnTouch = mQsExpansionHeight;
//                  mInitialTouchY = y;
//                  mInitialTouchX = x;
//                  mIntercepting = false;
//                  mNotificationStackScroller.removeLongPressCallback();
//                  return true;
//              }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                trackMovement(event);
                if (mQsTracking) {
                    flingQsWithCurrentVelocity(y,
                            event.getActionMasked() == MotionEvent.ACTION_CANCEL);
                    mQsTracking = false;
                }
                mIntercepting = false;
                break;
        }
        return false;
    }

    @Override
    protected boolean isInContentBounds(float x, float y) {
        float stackScrollerX = mNotificationStackScroller.getX();
        return !mNotificationStackScroller.isBelowLastNotification(x - stackScrollerX, y)
                && stackScrollerX < x && x < stackScrollerX + mNotificationStackScroller.getWidth();
    }

    private void initDownStates(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mOnlyAffordanceInThisMotion = false;
            mQsTouchAboveFalsingThreshold = mQsFullyExpanded;
            mDozingOnDown = isDozing();
            mCollapsedOnDown = isFullyCollapsed();
            mListenForHeadsUp = mCollapsedOnDown && mHeadsUpManager.hasPinnedHeadsUp();
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        // Block request when interacting with the scroll view so we can still intercept the
        // scrolling when QS is expanded.
        if (mScrollView.isHandlingTouchEvent()) {
            return;
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void flingQsWithCurrentVelocity(float y, boolean isCancelMotionEvent) {
        float vel = getCurrentVelocity();
        final boolean expandsQs = flingExpandsQs(vel);
        if (expandsQs) {
            logQsSwipeDown(y);
        }
        flingSettings(vel, expandsQs && !isCancelMotionEvent);
    }

    private void logQsSwipeDown(float y) {
        float vel = getCurrentVelocity();
        final int gesture = mStatusBarState == StatusBarState.KEYGUARD
                ? EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_SWIPE_DOWN_QS
                : EventLogConstants.SYSUI_SHADE_GESTURE_SWIPE_DOWN_QS;
        EventLogTags.writeSysuiLockscreenGesture(
                gesture,
                (int) ((y - mInitialTouchY) / mStatusBar.getDisplayDensity()),
                (int) (vel / mStatusBar.getDisplayDensity()));
    }

    private boolean flingExpandsQs(float vel) {
        if (isBelowFalsingThreshold()) {
            return false;
        }
        if (Math.abs(vel) < mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return getQsExpansionFraction() > 0.5f;
        } else {
            return vel > 0;
        }
    }

    private boolean isBelowFalsingThreshold() {
        return !mQsTouchAboveFalsingThreshold && mStatusBarState == StatusBarState.KEYGUARD;
    }

    private float getQsExpansionFraction() {
        return Math.min(1f, (mQsExpansionHeight - mQsMinExpansionHeight)
                / (getTempQsMaxExpansion() - mQsMinExpansionHeight));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(getOrientationScreen()){//add by hmq 20160504状态栏分2页显示 
            return false;
        }
        //added by yzs for MagcommLocker begin
        if ((mCappuBootUnlockView != null || mMagcommBootUnlockView != null) && mKeyguardShowing){
            return false;
        }else{
            if (mBlockTouches) {
                return false;
            }
            initDownStates(event);
            if (mListenForHeadsUp && !mHeadsUpTouchHelper.isTrackingHeadsUp()
                    && mHeadsUpTouchHelper.onInterceptTouchEvent(event)) {
                mIsExpansionFromHeadsUp = true;
                MetricsLogger.count(mContext, COUNTER_PANEL_OPEN_PEEK, 1);
            }
            if ((!mIsExpanding || mHintAnimationRunning)
                    && !mQsExpanded
                    && mStatusBar.getBarState() != StatusBarState.SHADE) {
                mAfforanceHelper.onTouchEvent(event);
            }
            if (mOnlyAffordanceInThisMotion) {
                return true;
            }
            mHeadsUpTouchHelper.onTouchEvent(event);
            if (!mHeadsUpTouchHelper.isTrackingHeadsUp() && handleQsTouch(event)) {
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && isFullyCollapsed()) {
                MetricsLogger.count(mContext, COUNTER_PANEL_OPEN, 1);
                updateVerticalPanelPosition(event.getX());
            }
            super.onTouchEvent(event);
            return true;
        }//added by yzs for MagcommLocker end
    }

    private boolean handleQsTouch(MotionEvent event) {
        final int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN && getExpandedFraction() == 1f
                && mStatusBar.getBarState() != StatusBarState.KEYGUARD && !mQsExpanded
                && mQsExpansionEnabled) {

            // Down in the empty area while fully expanded - go to QS.
            mQsTracking = true;
            mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            mInitialHeightOnTouch = mQsExpansionHeight;
            mInitialTouchY = event.getX();
            mInitialTouchX = event.getY();
        }
        if (!isFullyCollapsed()) {
            handleQsDown(event);
        }
        if (!mQsExpandImmediate && mQsTracking) {
            onQsTouch(event);
            /* begin: hide by hmq 20160504状态栏分2页显示 */
//          if (!mConflictingQsExpansionGesture) {
//              return true;
//          }
            /* end: hide by hmq 20160504状态栏分2页显示 */
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mConflictingQsExpansionGesture = false;
        }
        if (action == MotionEvent.ACTION_DOWN && isFullyCollapsed()
                && mQsExpansionEnabled) {
            mTwoFingerQsExpandPossible = true;
        }
        if (mTwoFingerQsExpandPossible /*&& isOpenQsEvent(event)*/// edit by hmq 20151111 Modify for 下拉状态栏完全展开
                && event.getY(event.getActionIndex()) < mStatusBarMinHeight) {
            MetricsLogger.count(mContext, COUNTER_PANEL_OPEN_QS, 1);
            mQsExpandImmediate = true;
            requestPanelHeightUpdate();

            // Normally, we start listening when the panel is expanded, but here we need to start
            // earlier so the state is already up to date when dragging down.
            setListening(true);
        }
        return false;
    }
    
    /* begin: add by hmq 20160504状态栏分2页显示 */
    private void resourceInit() {
        mQsContainer.setTranslationX(0);
        //mNotificationStackScroller.setTranslationX(mScreenWidth);
    }
    
    protected void setNotificationWhichPage(){
        //liukun 现在默认是开关
        //edit by hmq 隐藏 按条件切换默认下拉通知栏选项卡
//        if(mStatusBar.hasActiveClearableNotifications()){
//            mQsContainer.setTranslationX(-mScreenWidth);
//            mNotificationStackScroller.setTranslationX(0);
//        }else{
            mQsContainer.setTranslationX(0);
            mNotificationStackScroller.setTranslationX(mScreenWidth);
//        }
        
        if (mBtnSwitchL!= null && mBtnSwitchR != null){
            mBtnSwitchL.setSelected(mQsContainer.getTranslationX() == 0);
            mBtnSwitchR.setSelected(mNotificationStackScroller.getTranslationX() == 0);
        }
        
        if(mQsContainer.getTranslationX() == 0 && mHeader.getSwitchPageButton() != null){
            mHeader.getSwitchPageButton().setEnabled(true);
            mHeader.getSwitchPageButton().setText(getResources().getString(R.string.status_bar_settings_settings_button));
            mStackScrollerOverscrolling = false;
        }else if(mNotificationStackScroller.getTranslationX() == 0 && mHeader.getSwitchPageButton() != null) {
            mHeader.getSwitchPageButton().setEnabled(true);
            mHeader.getSwitchPageButton().setText(getResources().getString(R.string.status_bar_clear_all_button));
            mStackScrollerOverscrolling = true;
        }
        isHeaderClick = false;
    }
    /* end: add by hmq 20160504状态栏分2页显示 */
    
    private boolean isInQsArea(float x, float y) {
        return (x >= mScrollView.getX() && x <= mScrollView.getX() + mScrollView.getWidth()) &&
                (y <= mNotificationStackScroller.getBottomMostNotificationBottom()
                || y <= mQsContainer.getY() + mQsContainer.getHeight());
    }

    private boolean isOpenQsEvent(MotionEvent event) {
        final int pointerCount = event.getPointerCount();
        final int action = event.getActionMasked();

        final boolean twoFingerDrag = action == MotionEvent.ACTION_POINTER_DOWN
                && pointerCount == 2;

        final boolean stylusButtonClickDrag = action == MotionEvent.ACTION_DOWN
                && (event.isButtonPressed(MotionEvent.BUTTON_STYLUS_PRIMARY)
                        || event.isButtonPressed(MotionEvent.BUTTON_STYLUS_SECONDARY));

        final boolean mouseButtonClickDrag = action == MotionEvent.ACTION_DOWN
                && (event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)
                        || event.isButtonPressed(MotionEvent.BUTTON_TERTIARY));

        return twoFingerDrag || stylusButtonClickDrag || mouseButtonClickDrag;
    }

    private void handleQsDown(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && shouldQuickSettingsIntercept(event.getX(), event.getY(), -1)) {
            mQsTracking = true;
            onQsExpansionStarted();
            mInitialHeightOnTouch = mQsExpansionHeight;
            mInitialTouchY = event.getX();
            mInitialTouchX = event.getY();

            // If we interrupt an expansion gesture here, make sure to update the state correctly.
            notifyExpandingFinished();
        }
    }

    @Override
    protected boolean flingExpands(float vel, float vectorVel, float x, float y) {
        boolean expands = super.flingExpands(vel, vectorVel, x, y);

        // If we are already running a QS expansion, make sure that we keep the panel open.
        if (mQsExpansionAnimator != null) {
            expands = true;
        }
        return expands;
    }

    @Override
    protected boolean hasConflictingGestures() {
        return mStatusBar.getBarState() != StatusBarState.SHADE;
    }

    @Override
    protected boolean shouldGestureIgnoreXTouchSlop(float x, float y) {
        return !mAfforanceHelper.isOnAffordanceIcon(x, y);
    }

    private void onQsTouch(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            mTrackingPointer = event.getPointerId(pointerIndex);
        }
        final float y = event.getY(pointerIndex);
        final float x = event.getX(pointerIndex);
        final float h = y - mInitialTouchY;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mQsTracking = true;
                mInitialTouchY = y;
                mInitialTouchX = x;
                onQsExpansionStarted();
                mInitialHeightOnTouch = mQsExpansionHeight;
                initVelocityTracker();
                trackMovement(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                final int upPointer = event.getPointerId(event.getActionIndex());
                if (mTrackingPointer == upPointer) {
                    // gesture is ongoing, find a new pointer to track
                    final int newIndex = event.getPointerId(0) != upPointer ? 0 : 1;
                    final float newY = event.getY(newIndex);
                    final float newX = event.getX(newIndex);
                    mTrackingPointer = event.getPointerId(newIndex);
                    mInitialHeightOnTouch = mQsExpansionHeight;
                    mInitialTouchY = newY;
                    mInitialTouchX = newX;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                setQsExpansion(h + mInitialHeightOnTouch);
                if (h >= getFalsingThreshold()) {
                    mQsTouchAboveFalsingThreshold = true;
                }
                trackMovement(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mQsTracking = false;
                mTrackingPointer = -1;
                trackMovement(event);
                float fraction = getQsExpansionFraction();
                if ((fraction != 0f || y >= mInitialTouchY)
                        && (fraction != 1f || y <= mInitialTouchY)) {
                    flingQsWithCurrentVelocity(y,
                            event.getActionMasked() == MotionEvent.ACTION_CANCEL);
                } else {
                    logQsSwipeDown(y);
                    mScrollYOverride = -1;
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
    }

    private int getFalsingThreshold() {
        float factor = mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
        return (int) (mQsFalsingThreshold * factor);
    }

    @Override
    public void onOverscrolled(float lastTouchX, float lastTouchY, int amount) {
        if (mIntercepting && shouldQuickSettingsIntercept(lastTouchX, lastTouchY,
                -1 /* yDiff: Not relevant here */)) {
            /* begin: add by hmq 20160504状态栏分2页显示   隐藏这段代码就是当滑动越界会记录划出越界的偏移量，现在左右显示了，就不需要这个功能了*/
//            mQsTracking = true;
//            onQsExpansionStarted(amount);
//            mInitialHeightOnTouch = mQsExpansionHeight;
//            mInitialTouchY = mLastTouchY;
//            mInitialTouchX = mLastTouchX;
            /* end: add by hmq 20160504状态栏分2页显示 */
        }
    }

    /**
     * notification interface
     * Notifies a listener that the overscroll has changed.
     * 对于notification移动中超出自身高度，所触发的监听器
     * @param amount 对OverScroll量，以像素为单位 the amount of overscroll, in pixels
     * @param isRubberbanded true:这是一个rubberbanded OverScroll；  false:这是直接扩大OverScroll unrubberbanded运动（如扩大QS）
     *                      if true, this is a rubberbanded overscroll; if false, this is an
     *                     unrubberbanded motion to directly expand overscroll view (e.g expand QS) 
     */
    @Override
    public void onOverscrollTopChanged(float amount, boolean isRubberbanded) {
        cancelQsAnimation();
        if (!mQsExpansionEnabled) {
            amount = 0f;
        }
        amount = 0f;//edit by hmq 20160504状态栏分2页显示 强制赋值，该值表示在向下滑动的时候超出显示Notification的高度的偏移量，赋值给mLastOverscroll。该值用于，只有Notification显示的时候，向下滑动，会划出QS的界面
        float rounded = amount >= 1f ? amount : 0f;
        mStackScrollerOverscrolling = true;//rounded != 0f && isRubberbanded;
        mQsExpansionFromOverscroll = rounded != 0f;
        mLastOverscroll = rounded;
        updateQsState();
        setQsExpansion(mQsExpansionHeight);//edit by hmq 20160504状态栏分2页显示 修改意图:现在不要上下显示，改为左右，拉动notification不需要变化QS的Y值，其实这句代码可以不写,以防万一加一句，不会对结果造成变化
        //setQsExpansion(mQsMinExpansionHeight + rounded);//hide huangminqi 原设计就是上下QS和notification排列，当notification向下划越界后的插值就是rounded，设置QS的当前Y值的高度为最低高度+插值;隐藏越界后QS的滑动
    }

    /**
     * NotificationStackScrollLayout 的回调函数，当滑动到顶部触发
     */
    @Override
    public void flingTopOverscroll(float velocity, boolean open) {
        mLastOverscroll = 0f;
        setQsExpansion(mQsExpansionHeight);
        /* begin: hide by hmq 20160504状态栏分2页显示 隐藏这点代码的意图:原来当norification,上划到顶的时候，会改变QS的状态最后会走到setQsExpanded方法，
        里面会把mNotificationStackScroller.setInterceptDelegateEnabled(false)，隐藏后仍然notification可被TouchEvent */
//        flingSettings(!mQsExpansionEnabled && open ? 0f : velocity, open && mQsExpansionEnabled,
//                new Runnable() {
//            @Override
//            public void run() {
//
//                mStackScrollerOverscrolling = false;
//                mQsExpansionFromOverscroll = false;
//                updateQsState();
//            }
//        }, false /* isClick */);
         /* end: add by hmq 20160504状态栏分2页显示 */
    }

    private void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    private void onQsExpansionStarted(int overscrollAmount) {
        cancelQsAnimation();
        cancelHeightAnimator();

        // Reset scroll position and apply that position to the expanded height.
        float height = mQsExpansionHeight - mScrollView.getScrollY() - overscrollAmount;
        if (mScrollView.getScrollY() != 0) {
            mScrollYOverride = mScrollView.getScrollY();
        }
        mScrollView.scrollTo(0, 0);
        setQsExpansion(height);
        requestPanelHeightUpdate();
    }

    /**
     * 向下传QS和Notification 的拉伸状态
     * @param expanded 当前拉伸的状态 true已经被拉伸 false仍可以拉伸
     */
    private void setQsExpanded(boolean expanded) {
        boolean changed = mQsExpanded != expanded;
        if (changed) {
            mQsExpanded = expanded;//hmq 赋值(唯一)
            updateQsState();
            requestPanelHeightUpdate();
            mNotificationStackScroller.setInterceptDelegateEnabled(expanded);
            mStatusBar.setQsExpanded(expanded);
            mQsPanel.setExpanded(expanded);
            //liukun 连续点击状态栏 第三次的时候会把下拉栏里面的开关切换到通知 混乱 
            //mNotificationContainerParent.setQsExpanded(expanded);
        }
    }

    public void setBarState(int statusBarState, boolean keyguardFadingAway,
            boolean goingToFullShade) {
        int oldState = mStatusBarState;
        boolean keyguardShowing = statusBarState == StatusBarState.KEYGUARD;
        setKeyguardStatusViewVisibility(statusBarState, keyguardFadingAway, goingToFullShade);
        setKeyguardBottomAreaVisibility(statusBarState, goingToFullShade);

        mStatusBarState = statusBarState;
        mKeyguardShowing = keyguardShowing;

        if (goingToFullShade || (oldState == StatusBarState.KEYGUARD
                && statusBarState == StatusBarState.SHADE_LOCKED)) {
            animateKeyguardStatusBarOut();
            animateHeaderSlidingIn();
        } else if (oldState == StatusBarState.SHADE_LOCKED
                && statusBarState == StatusBarState.KEYGUARD) {
            animateKeyguardStatusBarIn(StackStateAnimator.ANIMATION_DURATION_STANDARD);
            animateHeaderSlidingOut();
        } else {
            mKeyguardStatusBar.setAlpha(1f);
            mKeyguardStatusBar.setVisibility(keyguardShowing ? View.VISIBLE : View.INVISIBLE);
            if (keyguardShowing && oldState != mStatusBarState) {
                mKeyguardBottomArea.updateLeftAffordance();
                mAfforanceHelper.updatePreviews();
            }
        }
        if (keyguardShowing) {
            updateDozingVisibilities(false /* animate */);
        }
        resetVerticalPanelPosition();
        updateQsState();
    }

    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable() {
        @Override
        public void run() {
            mKeyguardStatusViewAnimating = false;
            mKeyguardStatusView.setVisibility(View.GONE);
        }
    };

    private final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable() {
        @Override
        public void run() {
            mKeyguardStatusViewAnimating = false;
        }
    };

    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener
            = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            mHeaderAnimating = false;
            mQsContainerAnimator = null;
            mQsContainer.removeOnLayoutChangeListener(mQsContainerAnimatorUpdater);
        }
    };

    private final OnLayoutChangeListener mQsContainerAnimatorUpdater
            = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                int oldTop, int oldRight, int oldBottom) {
            int oldHeight = oldBottom - oldTop;
            int height = bottom - top;
            if (height != oldHeight && mQsContainerAnimator != null) {
                PropertyValuesHolder[] values = mQsContainerAnimator.getValues();
                float newEndValue = mHeader.getCollapsedHeight() + mQsPeekHeight - height - top;
                float newStartValue = -height - top;
                values[0].setFloatValues(newStartValue, newEndValue);
                mQsContainerAnimator.setCurrentPlayTime(mQsContainerAnimator.getCurrentPlayTime());
            }
        }
    };

    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn
            = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);
            long delay = mStatusBarState == StatusBarState.SHADE_LOCKED
                    ? 0
                    : mStatusBar.calculateGoingToFullShadeDelay();
            mHeader.setTranslationY(-mHeader.getCollapsedHeight() - mQsPeekHeight);
            mHeader.animate()
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE)
                    .setInterpolator(mFastOutSlowInInterpolator)
                    .start();
            mQsContainer.setY(-mQsContainer.getHeight());
            mQsContainerAnimator = ObjectAnimator.ofFloat(mQsContainer, View.TRANSLATION_Y,
                    mQsContainer.getTranslationY(),
                    mHeader.getCollapsedHeight() + mQsPeekHeight - mQsContainer.getHeight()
                            - mQsContainer.getTop());
            mQsContainerAnimator.setStartDelay(delay);
            mQsContainerAnimator.setDuration(StackStateAnimator.ANIMATION_DURATION_GO_TO_FULL_SHADE);
            mQsContainerAnimator.setInterpolator(mFastOutSlowInInterpolator);
            mQsContainerAnimator.addListener(mAnimateHeaderSlidingInListener);
            mQsContainerAnimator.start();
            mQsContainer.addOnLayoutChangeListener(mQsContainerAnimatorUpdater);
            return true;
        }
    };

    private void animateHeaderSlidingIn() {
        // If the QS is already expanded we don't need to slide in the header as it's already
        // visible.
        if (!mQsExpanded) {
            mHeaderAnimating = true;
            getViewTreeObserver().addOnPreDrawListener(mStartHeaderSlidingIn);
        }
    }

    private void animateHeaderSlidingOut() {
        mHeaderAnimating = true;
        mHeader.animate().y(-mHeader.getHeight())
                .setStartDelay(0)
                .setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD)
                .setInterpolator(mFastOutSlowInInterpolator)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mHeader.animate().setListener(null);
                        mHeaderAnimating = false;
                        updateQsState();
                    }
                })
                .start();
        mQsContainer.animate()
                .y(-mQsContainer.getHeight())
                .setStartDelay(0)
                .setDuration(StackStateAnimator.ANIMATION_DURATION_STANDARD)
                .setInterpolator(mFastOutSlowInInterpolator)
                .start();
    }

    private final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable() {
        @Override
        public void run() {
            mKeyguardStatusBar.setVisibility(View.INVISIBLE);
            mKeyguardStatusBar.setAlpha(1f);
            mKeyguardStatusBarAnimateAlpha = 1f;
        }
    };

    private void animateKeyguardStatusBarOut() {
        ValueAnimator anim = ValueAnimator.ofFloat(mKeyguardStatusBar.getAlpha(), 0f);
        anim.addUpdateListener(mStatusBarAnimateAlphaListener);
        anim.setStartDelay(mStatusBar.isKeyguardFadingAway()
                ? mStatusBar.getKeyguardFadingAwayDelay()
                : 0);
        anim.setDuration(mStatusBar.isKeyguardFadingAway()
                ? mStatusBar.getKeyguardFadingAwayDuration() / 2
                : StackStateAnimator.ANIMATION_DURATION_STANDARD);
        anim.setInterpolator(mDozeAnimationInterpolator);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimateKeyguardStatusBarInvisibleEndRunnable.run();
            }
        });
        anim.start();
    }

    private final ValueAnimator.AnimatorUpdateListener mStatusBarAnimateAlphaListener =
            new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mKeyguardStatusBarAnimateAlpha = (float) animation.getAnimatedValue();
            updateHeaderKeyguardAlpha();
        }
    };

    private void animateKeyguardStatusBarIn(long duration) {
        mKeyguardStatusBar.setVisibility(View.VISIBLE);
        mKeyguardStatusBar.setAlpha(0f);
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.addUpdateListener(mStatusBarAnimateAlphaListener);
        anim.setDuration(duration);
        anim.setInterpolator(mDozeAnimationInterpolator);
        anim.start();
    }

    private final Runnable mAnimateKeyguardBottomAreaInvisibleEndRunnable = new Runnable() {
        @Override
        public void run() {
            mKeyguardBottomArea.setVisibility(View.GONE);
        }
    };

    private void setKeyguardBottomAreaVisibility(int statusBarState,
            boolean goingToFullShade) {
        //added by yzs for MagcommLocker begin
        if (mMagcommBootUnlockView != null || mCappuBootUnlockView != null){
                mKeyguardBottomArea.animate().cancel();
                mKeyguardBottomArea.setVisibility(View.GONE);
                mKeyguardBottomArea.setAlpha(1f);
        }else{
            if (goingToFullShade) {
                mKeyguardBottomArea.animate().cancel();
                mKeyguardBottomArea.animate()
                        .alpha(0f)
                        .setStartDelay(mStatusBar.getKeyguardFadingAwayDelay())
                        .setDuration(mStatusBar.getKeyguardFadingAwayDuration() / 2)
                        .setInterpolator(PhoneStatusBar.ALPHA_OUT)
                        .withEndAction(mAnimateKeyguardBottomAreaInvisibleEndRunnable)
                        .start();
            } else if (statusBarState == StatusBarState.KEYGUARD
                    || statusBarState == StatusBarState.SHADE_LOCKED) {
                mKeyguardBottomArea.animate().cancel();
                if (!mDozing) {
                    mKeyguardBottomArea.setVisibility(View.VISIBLE);
                }
                mKeyguardBottomArea.setAlpha(1f);
            } else {
                mKeyguardBottomArea.animate().cancel();
                mKeyguardBottomArea.setVisibility(View.GONE);
                mKeyguardBottomArea.setAlpha(1f);
            }
        }//added by yzs for MagcommLocker end
    }

    private void setKeyguardStatusViewVisibility(int statusBarState, boolean keyguardFadingAway,
            boolean goingToFullShade) {
        // added by yzs for MagcommLocker begin
        if (mMagcommBootUnlockView != null || mCappuBootUnlockView != null) {
            mKeyguardStatusView.animate().cancel();
            mKeyguardStatusViewAnimating = false;
            mKeyguardStatusView.setVisibility(View.GONE);
            mKeyguardStatusView.setAlpha(1f);
        } else {
            if ((!keyguardFadingAway && mStatusBarState == StatusBarState.KEYGUARD
                    && statusBarState != StatusBarState.KEYGUARD) || goingToFullShade) {
                mKeyguardStatusView.animate().cancel();
                mKeyguardStatusViewAnimating = true;
                mKeyguardStatusView.animate()
                        .alpha(0f)
                        .setStartDelay(0)
                        .setDuration(160)
                        .setInterpolator(PhoneStatusBar.ALPHA_OUT)
                        .withEndAction(mAnimateKeyguardStatusViewInvisibleEndRunnable);
                if (keyguardFadingAway) {
                    mKeyguardStatusView.animate()
                            .setStartDelay(mStatusBar.getKeyguardFadingAwayDelay())
                            .setDuration(mStatusBar.getKeyguardFadingAwayDuration()/2)
                            .start();
                }
            } else if (mStatusBarState == StatusBarState.SHADE_LOCKED
                    && statusBarState == StatusBarState.KEYGUARD) {
                mKeyguardStatusView.animate().cancel();
                mKeyguardStatusView.setVisibility(View.VISIBLE);
                mKeyguardStatusViewAnimating = true;
                mKeyguardStatusView.setAlpha(0f);
                mKeyguardStatusView.animate()
                        .alpha(1f)
                        .setStartDelay(0)
                        .setDuration(320)
                        .setInterpolator(PhoneStatusBar.ALPHA_IN)
                        .withEndAction(mAnimateKeyguardStatusViewVisibleEndRunnable);
            } else if (statusBarState == StatusBarState.KEYGUARD) {
                mKeyguardStatusView.animate().cancel();
                mKeyguardStatusViewAnimating = false;
                mKeyguardStatusView.setVisibility(View.VISIBLE);
                mKeyguardStatusView.setAlpha(1f);
            } else {
                mKeyguardStatusView.animate().cancel();
                mKeyguardStatusViewAnimating = false;
                mKeyguardStatusView.setVisibility(View.GONE);
                mKeyguardStatusView.setAlpha(1f);
            }
        }//added by yzs for MagcommLocker end
    }

    /**
     * 更新QS的一些状态
     * 
     */
    private void updateQsState() {
        boolean expandVisually = mQsExpanded || mStackScrollerOverscrolling || mHeaderAnimating;
        mHeader.setVisibility((!mKeyguardShowing) ? View.VISIBLE : View.INVISIBLE);
        //mHeader.setVisibility((mQsExpanded || !mKeyguardShowing || mHeaderAnimating)
        //        ? View.VISIBLE
        //        : View.INVISIBLE);
        mHeader.setExpanded((mKeyguardShowing && !mHeaderAnimating)
                || (mQsExpanded && !mStackScrollerOverscrolling));
        mNotificationStackScroller.setScrollingEnabled(
                mStatusBarState != StatusBarState.KEYGUARD && (!mQsExpanded
                        || mQsExpansionFromOverscroll));
        mNotificationStackScroller.setVisibility((!mKeyguardShowing) ? View.VISIBLE : View.INVISIBLE);
        mQsPanel.setVisibility(expandVisually ? View.VISIBLE : View.INVISIBLE);
		/* begin: add by hmq 20160504状态栏分2页显示 */
        //mQsContainer.setVisibility(
        //        mKeyguardShowing && !expandVisually ? View.INVISIBLE : View.VISIBLE);

        mQsContainer.setVisibility(mKeyguardShowing ? View.GONE : View.VISIBLE);
        /* end: add by hmq 20160504状态栏分2页显示 */
        mScrollView.setTouchEnabled(mQsExpanded);
        updateEmptyShadeView();
        mQsNavbarScrim.setVisibility(mStatusBarState == StatusBarState.SHADE && mQsExpanded
                && !mStackScrollerOverscrolling && mQsScrimEnabled
                        ? View.VISIBLE
                        : View.INVISIBLE);
        if (mKeyguardUserSwitcher != null && mQsExpanded && !mStackScrollerOverscrolling) {
            mKeyguardUserSwitcher.hideIfNotSimple(true /* animate */);
        }
    }

    /**
     * mQsExpansionHeight赋值
     * @param height
     */
    private void setQsExpansion(float height) {
        height = Math.min(Math.max(height, mQsMinExpansionHeight), mQsMaxExpansionHeight);
        mQsFullyExpanded = height == mQsMaxExpansionHeight;
        //added by yzs for MagcommLocker begin
        if(mKeyguardShowing){
            setQsExpanded(false);
        }else{
            //added by yzs for MagcommLocker end
            /* begin: edit by hmq 20151025 Modify for 老人机下拉状态栏 */
            if (height > mQsMinExpansionHeight && !mQsExpanded /*&& !mStackScrollerOverscrolling*/) {//hide by hmq 目地是排除通知栏的属性修改mQsExpanded
                if(mNotificationStackScroller.getTranslationX() == 0){
                    setQsExpanded(false);
                }else{
                setQsExpanded(true);
                }
            /* end: edit by hmq 20151025 Modify for 老人机下拉状态栏 */
            } else if (height <= mQsMinExpansionHeight && mQsExpanded) {
                setQsExpanded(false);
                if (mLastAnnouncementWasQuickSettings && !mTracking && !isCollapsing()) {
                    announceForAccessibility(getKeyguardOrLockScreenString());
                    mLastAnnouncementWasQuickSettings = false;
                }
            }
            mQsExpansionHeight = height;
            //mHeader.setExpansion(getHeaderExpansionFraction());// edit by hmq 20151025 Modify for 老人机下拉状态栏
            setQsTranslation(height);
            requestScrollerTopPaddingUpdate(false /* animate */);//hmq 隐藏notification bar 的同步滑动
            updateNotificationScrim(height);
            if (mKeyguardShowing) {
                updateHeaderKeyguard();
            }
            if (mStatusBarState == StatusBarState.SHADE_LOCKED
                    || mStatusBarState == StatusBarState.KEYGUARD) {
                updateKeyguardBottomAreaAlpha();
            }
            if (mStatusBarState == StatusBarState.SHADE && mQsExpanded
                    && !mStackScrollerOverscrolling && mQsScrimEnabled) {
                mQsNavbarScrim.setAlpha(getQsExpansionFraction());
            }
    
            // Upon initialisation when we are not layouted yet we don't want to announce that we are
            // fully expanded, hence the != 0.0f check.
            if (height != 0.0f && mQsFullyExpanded && !mLastAnnouncementWasQuickSettings) {
                announceForAccessibility(getContext().getString(
                        R.string.accessibility_desc_quick_settings));
                mLastAnnouncementWasQuickSettings = true;
            }
            if (DEBUG) {
                invalidate();
            }
        }
    }

    private String getKeyguardOrLockScreenString() {
        if (mStatusBarState == StatusBarState.KEYGUARD) {
            return getContext().getString(R.string.accessibility_desc_lock_screen);
        } else {
            return getContext().getString(R.string.accessibility_desc_notification_shade);
        }
    }

    private void updateNotificationScrim(float height) {
        int startDistance = mQsMinExpansionHeight + mNotificationScrimWaitDistance;
        float progress = (height - startDistance) / (mQsMaxExpansionHeight - startDistance);
        progress = Math.max(0.0f, Math.min(progress, 1.0f));
    }

    private float getHeaderExpansionFraction() {
        if (!mKeyguardShowing) {
            return getQsExpansionFraction();
        } else {
            return 1f;
        }
    }

    /**
     * 设置mQsContainer && mHeader Y轴的位置
     * @param height
     */
    private void setQsTranslation(float height) {
        if (!mHeaderAnimating) {
//            mQsContainer.setY(height - mQsContainer.getDesiredHeight() + getHeaderTranslation());//huangminqi 这里是设置QS的位置Y 高度 - QS view的高度 + header高度
            mQsContainer.setY(height - (getHeight()-mHeader.getHeight()) + getHeaderTranslation());//huangminqi 这里是设置QS的位置Y 高度 - QS view的高度 + header当前Y值
            mQsContainer.setHeightOverride(getHeight()-mHeader.getHeight());//edit huangminqi 修改QS的内容高度,QS的这个方法就是高度的重定义，如果不是-1，就读取传入的值作为高度
        }
        if (mKeyguardShowing && !mHeaderAnimating) {
            //锁屏时header的高度是-header的高度
            mHeader.setY(interpolate(getQsExpansionFraction(), -mHeader.getHeight(), 0));
            mSwitchToolBar.setY(-mSwitchToolBar.getHeight());//add by hmq 20160504状态栏分2页显示
            mDragScrollBar.setY(-mDragScrollBar.getHeight());//add by hmq 20160504状态栏分2页显示
        }
    }

    /**
     * @return 计算QS下底线高度 (品红色)
     */
    private float calculateQsTopPadding() {
        if (mKeyguardShowing
                && (mQsExpandImmediate || mIsExpanding && mQsExpandedWhenExpandingStarted)) {

            // Either QS pushes the notifications down when fully expanded, or QS is fully above the
            // notifications (mostly on tablets). maxNotifications denotes the normal top padding
            // on Keyguard, maxQs denotes the top padding from the quick settings panel. We need to
            // take the maximum and linearly interpolate with the panel expansion for a nice motion.
            int maxNotifications = mClockPositionResult.stackScrollerPadding
                    - mClockPositionResult.stackScrollerPaddingAdjustment
                    - mNotificationTopPadding;
            int maxQs = getTempQsMaxExpansion();
            int max = mStatusBarState == StatusBarState.KEYGUARD
                    ? Math.max(maxNotifications, maxQs)
                    : maxQs;
            return (int) interpolate(getExpandedFraction(),
                    mQsMinExpansionHeight, max);
        } else if (mQsSizeChangeAnimator != null) {
            return (int) mQsSizeChangeAnimator.getAnimatedValue();
        } else if (mKeyguardShowing && mScrollYOverride == -1) {

            // We can only do the smoother transition on Keyguard when we also are not collapsing
            // from a scrolled quick settings.
            return interpolate(getQsExpansionFraction(),
                    mNotificationStackScroller.getIntrinsicPadding() - mNotificationTopPadding,
                    mQsMaxExpansionHeight);
        } else {
            return mQsExpansionHeight;
        }
    }

    /**
     * 
     * @param animate true可以滑动    false不可滑动
     */
    private void requestScrollerTopPaddingUpdate(boolean animate) {
        mNotificationStackScroller.updateTopPadding(0,    //edit by hmq 20160504状态栏分2页显示  <calculateQsTopPadding()> 这里控制它的移动高度。现在设置为0，不受QS的移动变换其实位置
                mScrollView.getScrollY(),
                mAnimateNextTopPaddingChange || animate,
                mKeyguardShowing
                        && (mQsExpandImmediate || mIsExpanding && mQsExpandedWhenExpandingStarted));
        mAnimateNextTopPaddingChange = false;
    }

    /**
     * 获得touch event跟踪触摸屏事件的速度
     * 初始化mLastTouchX,mLastTouchY
     */
    private void trackMovement(MotionEvent event) {
        if (mVelocityTracker != null) mVelocityTracker.addMovement(event);//hmq 将事件加入到VelocityTracker类实例中
        mLastTouchX = event.getX();
        mLastTouchY = event.getY();
    }

    /**
     * 初始化VelocityTracker
     * 用于跟踪触摸屏事件的速度
     */
    private void initVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = VelocityTracker.obtain();
    }

    /**
     * 
     * @return 获得当前VelocityTracker 运动速度
     */
    private float getCurrentVelocity() {
        if (mVelocityTracker == null) {
            return 0;
        }
        mVelocityTracker.computeCurrentVelocity(1000);
        return mVelocityTracker.getYVelocity();
    }

    private void cancelQsAnimation() {
        if (mQsExpansionAnimator != null) {
            mQsExpansionAnimator.cancel();
        }
    }

    /* begin: add by hmq 20160504状态栏分2页显示 */
    private void switchHorizontalFling(float vel, boolean expand) {
        switchHorizontalFling(vel, expand, null);
    }
    
    private void switchHorizontalFling(float vel, boolean expand, final Runnable onFinishRunnable) {
        mNotificationStackScroller.setScrollingEnabled(!expand);
        int target = expand ? 0 : -getWidth();
        if (target == getWidth()) {
            mScrollYOverride = -1;
            if (onFinishRunnable != null) {
                onFinishRunnable.run();
            }
            return;
        }
        boolean belowFalsingThreshold = isBelowFalsingThreshold();
        if (belowFalsingThreshold) {
            vel = 0;
        }
        mScrollView.setBlockFlinging(true);
        ValueAnimator animator = ValueAnimator.ofFloat(mQsContainer.getTranslationX(), target);
        mFlingAnimationUtils.apply(animator, mQsContainer.getTranslationX(), target, vel);
        if (belowFalsingThreshold) {
            animator.setDuration(350);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mQsContainer.setTranslationX((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollView.setBlockFlinging(false);
                mScrollYOverride = -1;
                mQsExpansionAnimator = null;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
        });
        animator.start();
        int target1 = expand ? getWidth() : 0;
        ValueAnimator animator1 = ValueAnimator.ofFloat(mNotificationStackScroller.getTranslationX(), target1);
        mFlingAnimationUtils.apply(animator1, mNotificationStackScroller.getTranslationX(), target1, vel);
        if (belowFalsingThreshold) {
            animator1.setDuration(350);
        }
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNotificationStackScroller.setTranslationX((Float) animation.getAnimatedValue());
            }
        });
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollView.setBlockFlinging(false);
                mScrollYOverride = -1;
                mQsExpansionAnimator = null;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
            
            @Override
            public void onAnimationStart(Animator animation) {
                mNotificationStackScroller.invalidate();
            }
        });
        animator1.start();
        mQsExpanded = expand;
        mQsAnimatorExpand = expand;
        mStatusBar.setQsExpanded(expand);
        mQsPanel.setExpanded(expand);
    }
    /* end: add by hmq 20160504状态栏分2页显示 */
    
    private void flingSettings(float vel, boolean expand) {
        flingSettings(vel, expand, null, false /* isClick */);
    }

    private void flingSettings(float vel, boolean expand, final Runnable onFinishRunnable,
            boolean isClick) {
        float target = expand ? mQsMaxExpansionHeight : mQsMinExpansionHeight;
        if (target == mQsExpansionHeight) {
            mScrollYOverride = -1;
            if (onFinishRunnable != null) {
                onFinishRunnable.run();
            }
            return;
        }
        boolean belowFalsingThreshold = isBelowFalsingThreshold();
        if (belowFalsingThreshold) {
            vel = 0;
        }
        mScrollView.setBlockFlinging(true);
        ValueAnimator animator = ValueAnimator.ofFloat(mQsExpansionHeight, target);
        if (isClick) {
            animator.setInterpolator(mTouchResponseInterpolator);
            animator.setDuration(368);
        } else {
            mFlingAnimationUtils.apply(animator, mQsExpansionHeight, target, vel);
        }
        if (belowFalsingThreshold) {
            animator.setDuration(350);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setQsExpansion((Float) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mScrollView.setBlockFlinging(false);
                mScrollYOverride = -1;
                mQsExpansionAnimator = null;
                if (onFinishRunnable != null) {
                    onFinishRunnable.run();
                }
            }
        });
        animator.start();
        mQsExpansionAnimator = animator;
        mQsAnimatorExpand = expand;
    }

    /**
     * @return Whether we should intercept a gesture to open Quick Settings.
     * 我们是否应该拦截一个手势打开快速设置。
     */
    private boolean shouldQuickSettingsIntercept(float x, float y, float yDiff) {
        if (!mQsExpansionEnabled || mCollapsedOnDown) {
            return false;
        }
        View header = mKeyguardShowing ? mKeyguardStatusBar : mHeader;
        boolean onHeader = x >= header.getX() && x <= header.getX() + header.getWidth()
                && y >= header.getTop() && y <= header.getBottom();
        if (mQsExpanded) {
            return onHeader || (mScrollView.isScrolledToBottom() && yDiff < 0) && isInQsArea(x, y);
        } else {
            return onHeader;
        }
    }

    @Override
    protected boolean isScrolledToBottom() {
        if (!isInSettings()) {
            return mStatusBar.getBarState() == StatusBarState.KEYGUARD
                    || mNotificationStackScroller.isScrolledToBottom();
        } else {
            return mScrollView.isScrolledToBottom();
        }
    }

    /**
     * 返回整个下拉状态栏的最大高度
     */
    @Override
    protected int getMaxPanelHeight() {
        int min = mStatusBarMinHeight;//hmq 状态栏高度
        if (mStatusBar.getBarState() != StatusBarState.KEYGUARD   //hmq不是锁屏状态
                && mNotificationStackScroller.getNotGoneChildCount() == 0) { //hmq 当前没有通知
            int minHeight = (int) ((mQsMinExpansionHeight + getOverExpansionAmount()) //hmq (最小QS高度 + ?)*2.05f
                    * HEADER_RUBBERBAND_FACTOR);
            min = Math.max(min, minHeight);
        }
        int maxHeight;
        if (mQsExpandImmediate || mQsExpanded || mIsExpanding && mQsExpandedWhenExpandingStarted) {//立即展开 + 是否可被拖动
            maxHeight = calculatePanelHeightQsExpanded();
        } else {
            maxHeight = calculatePanelHeightShade();
        }
        maxHeight = Math.max(maxHeight, min);
        return maxHeight;
    }

    private boolean isInSettings() {
        return mQsExpanded;
    }

    @Override
    protected void onHeightUpdated(float expandedHeight) {
        if (!mQsExpanded || mQsExpandImmediate || mIsExpanding && mQsExpandedWhenExpandingStarted) {
            positionClockAndNotifications();
        }
        if (mQsExpandImmediate || mQsExpanded && !mQsTracking && mQsExpansionAnimator == null
                && !mQsExpansionFromOverscroll) {
            float t;
            if (mKeyguardShowing) {

                // On Keyguard, interpolate the QS expansion linearly to the panel expansion
                t = expandedHeight / getMaxPanelHeight();
            } else {

                // In Shade, interpolate linearly such that QS is closed whenever panel height is
                // minimum QS expansion + minStackHeight
                float panelHeightQsCollapsed = mNotificationStackScroller.getIntrinsicPadding()
                        + mNotificationStackScroller.getMinStackHeight();
                float panelHeightQsExpanded = calculatePanelHeightQsExpanded();
                t = (expandedHeight - panelHeightQsCollapsed)
                        / (panelHeightQsExpanded - panelHeightQsCollapsed);
            }
            setQsExpansion(expandedHeight);/*edit by hmq 20160504状态栏分2页显示 修改原因初始状态栏下拉时QS和noitification同时下拉，QS获得notification位置值
            setQsExpansion(mQsMinExpansionHeight
                    + t * (getTempQsMaxExpansion() - mQsMinExpansionHeight)); */
        }
        
        if(mQsExpandImmediate){//edit by hmq 20160504状态栏分2页显示
            mNotificationStackScroller.setInterceptDelegateEnabled(true);//edit by hmq 20160504状态栏分2页显示
        }//edit by hmq 20160504状态栏分2页显示
        updateStackHeight(expandedHeight);//edit by hmq 20160504状态栏分2页显示 notification的当前位置Y值
        updateHeader();
        /* begin: add by hmq 20160504状态栏分2页显示 */
        updateDragScrollBar();
        updateSwitchToolBar();
        /* end: add by hmq 20160504状态栏分2页显示 */
        updateUnlockIcon();
        updateNotificationTranslucency();
        updatePanelExpanded();
        mNotificationStackScroller.setShadeExpanded(!isFullyCollapsed());
        if (DEBUG) {
            invalidate();
        }
    }

    private void updatePanelExpanded() {
        boolean isExpanded = !isFullyCollapsed();
        if (mPanelExpanded != isExpanded) {
            mHeadsUpManager.setIsExpanded(isExpanded);
            mStatusBar.setPanelExpanded(isExpanded);
            mPanelExpanded = isExpanded;
        }
    }

    /**
     * @return a temporary override of {@link #mQsMaxExpansionHeight}, which is needed when
     *         collapsing QS / the panel when QS was scrolled
     */
    private int getTempQsMaxExpansion() {
        int qsTempMaxExpansion = mQsMaxExpansionHeight;
        if (mScrollYOverride != -1) {
            qsTempMaxExpansion -= mScrollYOverride;
        }
        return qsTempMaxExpansion;
    }

    private int calculatePanelHeightShade() {
        int emptyBottomMargin = mNotificationStackScroller.getEmptyBottomMargin();
        int maxHeight = mNotificationStackScroller.getHeight() - emptyBottomMargin
                - mTopPaddingAdjustment;
        maxHeight += mNotificationStackScroller.getTopPaddingOverflow();
        maxHeight = getHeight();//edit by hmq 20160504状态栏分2页显示 临时设置下，全屏
        return maxHeight;
    }

    /**
     * 
     * @return 计算做大高度
     */
    private int calculatePanelHeightQsExpanded() {
        float notificationHeight = mNotificationStackScroller.getHeight()
                - mNotificationStackScroller.getEmptyBottomMargin()
                - mNotificationStackScroller.getTopPadding();

        // When only empty shade view is visible in QS collapsed state, simulate that we would have
        // it in expanded QS state as well so we don't run into troubles when fading the view in/out
        // and expanding/collapsing the whole panel from/to quick settings.
        if (mNotificationStackScroller.getNotGoneChildCount() == 0
                && mShadeEmpty) {
            notificationHeight = mNotificationStackScroller.getEmptyShadeViewHeight()
                    + mNotificationStackScroller.getBottomStackPeekSize()
                    + mNotificationStackScroller.getCollapseSecondCardPadding();
        }
        int maxQsHeight = mQsMaxExpansionHeight;

        // If an animation is changing the size of the QS panel, take the animated value.
        if (mQsSizeChangeAnimator != null) {
            maxQsHeight = (int) mQsSizeChangeAnimator.getAnimatedValue();
        }
        float totalHeight = Math.max(
                maxQsHeight + mNotificationStackScroller.getNotificationTopPadding(),
                mStatusBarState == StatusBarState.KEYGUARD
                        ? mClockPositionResult.stackScrollerPadding - mTopPaddingAdjustment
                        : 0)
                + notificationHeight;
        if (totalHeight > mNotificationStackScroller.getHeight()) {
            float fullyCollapsedHeight = maxQsHeight
                    + mNotificationStackScroller.getMinStackHeight()
                    + mNotificationStackScroller.getNotificationTopPadding()
                    - getScrollViewScrollY();
            totalHeight = Math.max(fullyCollapsedHeight, mNotificationStackScroller.getHeight());
        }
        totalHeight = mScreenHeight;//edit by hmq 20160504状态栏分2页显示 临时设置下，全屏
        return (int) totalHeight;
    }

    private int getScrollViewScrollY() {
        if (mScrollYOverride != -1 && !mQsTracking) {
            return mScrollYOverride;
        } else {
            return mScrollView.getScrollY();
        }
    }
    
    /**
     * set notification alpha
     */
    private void updateNotificationTranslucency() {
        float alpha = 1f;
        if (mClosingWithAlphaFadeOut && !mExpandingFromHeadsUp && !mHeadsUpManager.hasPinnedHeadsUp()) {
            alpha = getFadeoutAlpha();
        }
        mNotificationStackScroller.setAlpha(alpha);
    }

    private float getFadeoutAlpha() {
        float alpha = (getNotificationsTopY() + mNotificationStackScroller.getItemHeight())
                / (mQsMinExpansionHeight + mNotificationStackScroller.getBottomStackPeekSize()
                - mNotificationStackScroller.getCollapseSecondCardPadding());
        alpha = Math.max(0, Math.min(alpha, 1));
        alpha = (float) Math.pow(alpha, 0.75);
        return alpha;
    }

    @Override
    protected float getOverExpansionAmount() {
        return mNotificationStackScroller.getCurrentOverScrollAmount(true /* top */);
    }

    @Override
    protected float getOverExpansionPixels() {
        return mNotificationStackScroller.getCurrentOverScrolledPixels(true /* top */);
    }

    private void updateUnlockIcon() {
        if (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED) {
            boolean active = getMaxPanelHeight() - getExpandedHeight() > mUnlockMoveDistance;
            KeyguardAffordanceView lockIcon = mKeyguardBottomArea.getLockIcon();
            if (active && !mUnlockIconActive && mTracking) {
                lockIcon.setImageAlpha(1.0f, true, 150, mFastOutLinearInterpolator, null);
                lockIcon.setImageScale(LOCK_ICON_ACTIVE_SCALE, true, 150,
                        mFastOutLinearInterpolator);
            } else if (!active && mUnlockIconActive && mTracking) {
                lockIcon.setImageAlpha(lockIcon.getRestingAlpha(), true /* animate */,
                        150, mFastOutLinearInterpolator, null);
                lockIcon.setImageScale(1.0f, true, 150,
                        mFastOutLinearInterpolator);
            }
            mUnlockIconActive = active;
        }
    }

    /**
     * Hides the header when notifications are colliding with it.
     * 更新header在屏幕的位置
     */
    private void updateHeader() {
        if (mStatusBar.getBarState() == StatusBarState.KEYGUARD) {
            updateHeaderKeyguard();
        } else {
            updateHeaderShade();
        }

    }

    /**
     * 第一次下滑会走，QS的位置和header的位置
     */
    private void updateHeaderShade() {
        if (!mHeaderAnimating) {//edit by hmq 说明：这个永远是mHeaderAnimatingIn=false 一直会走到 
            mHeader.setTranslationY(getHeaderTranslation());//edit by hmq 划出header位置 
        }
        setQsTranslation(mQsExpansionHeight);
    }

    
    /**
     * return header Y值 
     * 返回的是负值，最大是0 默认的header的高度是负高，拖动中增加他的值，直到完全显示为0
     */
    private float getHeaderTranslation() {
        if (mStatusBar.getBarState() == StatusBarState.KEYGUARD) {
            return 0;
        }
        //hide by hmq 20160504状态栏分2页显示 hide 状态栏左右切换了，默认通知栏是不会显示的，所以这几句代码现在就不要了。
//        if (mNotificationStackScroller.getNotGoneChildCount() == 0) {
//            if (mExpandedHeight / HEADER_RUBBERBAND_FACTOR >= mQsMinExpansionHeight) {
//                return 0;
//            } else {
//                return mExpandedHeight / HEADER_RUBBERBAND_FACTOR - mQsMinExpansionHeight;
//            }
//        }
//
//        float stackTranslation = mNotificationStackScroller.getStackTranslation();
//        float translation = stackTranslation / HEADER_RUBBERBAND_FACTOR;
//        if (mHeadsUpManager.hasPinnedHeadsUp() || mIsExpansionFromHeadsUp) {
//            translation = mNotificationStackScroller.getTopPadding() + stackTranslation
//                    - mNotificationTopPadding - mQsMinExpansionHeight;
//        }
//        return Math.min(0, translation);
        /* end: add by hmq 20160504状态栏分2页显示 */
        return Math.min(0f, getExpandedHeight() - mDragScrollBar.getHeight() - mHeader.getCollapsedHeight());/*edit by hmq 20160504状态栏分2页显示 ExpandedHeight - 最下条拖动的高度 - header的高度*/

    }

    /**
     * @return the alpha to be used to fade out the contents on Keyguard (status bar, bottom area)
     *         during swiping up
     */
    private float getKeyguardContentsAlpha() {
        float alpha;
        if (mStatusBar.getBarState() == StatusBarState.KEYGUARD) {

            // When on Keyguard, we hide the header as soon as the top card of the notification
            // stack scroller is close enough (collision distance) to the bottom of the header.
            alpha = getNotificationsTopY()
                    /
                    (mKeyguardStatusBar.getHeight() + mNotificationsHeaderCollideDistance);
        } else {

            // In SHADE_LOCKED, the top card is already really close to the header. Hide it as
            // soon as we start translating the stack.
            alpha = getNotificationsTopY() / mKeyguardStatusBar.getHeight();
        }
        alpha = MathUtils.constrain(alpha, 0, 1);
        alpha = (float) Math.pow(alpha, 0.75);
        return alpha;
    }

    private void updateHeaderKeyguardAlpha() {
        float alphaQsExpansion = 1 - Math.min(1, getQsExpansionFraction() * 2);
        mKeyguardStatusBar.setAlpha(Math.min(getKeyguardContentsAlpha(), alphaQsExpansion)
                * mKeyguardStatusBarAnimateAlpha);
        mKeyguardStatusBar.setVisibility(mKeyguardStatusBar.getAlpha() != 0f
                && !mDozing ? VISIBLE : INVISIBLE);
    }

    private void updateHeaderKeyguard() {
        updateHeaderKeyguardAlpha();
        //added by yzs for MagcommLocker begin
        if (mMagcommBootUnlockView != null || mCappuBootUnlockView != null){
            mKeyguardBottomArea.setVisibility(View.GONE);
        }
        //added by yzs for MagcommLocker end
        setQsTranslation(mQsExpansionHeight);
    }

    private void updateKeyguardBottomAreaAlpha() {
        float alpha = Math.min(getKeyguardContentsAlpha(), 1 - getQsExpansionFraction());
        mKeyguardBottomArea.setAlpha(alpha);
        mKeyguardBottomArea.setImportantForAccessibility(alpha == 0f
                ? IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                : IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    }

    private float getNotificationsTopY() {
        if (mNotificationStackScroller.getNotGoneChildCount() == 0) {
            return getExpandedHeight();
        }
        return mNotificationStackScroller.getNotificationsTopY();
    }

    /**
     * 开始拉升
     * touch down 触发
     * 0状态下拉的时候会走
     * 1状态下拉 和 2状态上划不会走
     * 1状态上划会走
     */
    @Override
    protected void onExpandingStarted() {
        super.onExpandingStarted();
        mNotificationStackScroller.onExpansionStarted();
        mIsExpanding = true;
        mQsExpandedWhenExpandingStarted = mQsFullyExpanded;
        if (mQsExpanded) {
            onQsExpansionStarted();
        }
    }

    /**
     * 停止拉升
     * touch up c触发
     * onExpandingStarted()触发条件对应
     */
    @Override
    protected void onExpandingFinished() {
        super.onExpandingFinished();
        mNotificationStackScroller.onExpansionStopped();
        mHeadsUpManager.onExpandingFinished();
        mIsExpanding = false;
        mScrollYOverride = -1;
        if (isFullyCollapsed()) {
            DejankUtils.postAfterTraversal(new Runnable() {
                @Override
                public void run() {
                    setListening(false);
                }
            });

            // Workaround b/22639032: Make sure we invalidate something because else RenderThread
            // thinks we are actually drawing a frame put in reality we don't, so RT doesn't go
            // ahead with rendering and we jank.
            postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    getParent().invalidateChild(NotificationPanelView.this, mDummyDirtyRect);
                }
            });
        } else {
            setListening(true);
        }
        mQsExpandImmediate = false;
        mTwoFingerQsExpandPossible = false;
        mIsExpansionFromHeadsUp = false;
        mNotificationStackScroller.setTrackingHeadsUp(false);
        mExpandingFromHeadsUp = false;
        setPanelScrimMinFraction(0.0f);
    }

    private void setListening(boolean listening) {
        mHeader.setListening(listening);
        mKeyguardStatusBar.setListening(listening);
        mQsPanel.setListening(listening);
    }

    @Override
    public void instantExpand() {
        super.instantExpand();
        setListening(true);
    }

    @Override
    protected void setOverExpansion(float overExpansion, boolean isPixels) {
        if (mConflictingQsExpansionGesture || mQsExpandImmediate) {
            return;
        }
        if (mStatusBar.getBarState() != StatusBarState.KEYGUARD) {
            mNotificationStackScroller.setOnHeightChangedListener(null);
            if (isPixels) {
                mNotificationStackScroller.setOverScrolledPixels(
                        overExpansion, true /* onTop */, false /* animate */);
            } else {
                mNotificationStackScroller.setOverScrollAmount(
                        overExpansion, true /* onTop */, false /* animate */);
            }
            mNotificationStackScroller.setOnHeightChangedListener(this);
        }
    }

    @Override
    protected void onTrackingStarted() {
        super.onTrackingStarted();
        if (mQsFullyExpanded) {
            mQsExpandImmediate = true;
        }
        if (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED) {
            mAfforanceHelper.animateHideLeftRightIcon();
        }
        mNotificationStackScroller.onPanelTrackingStarted();
    }

    @Override
    protected void onTrackingStopped(boolean expand) {
        super.onTrackingStopped(expand);
        if (expand) {
            mNotificationStackScroller.setOverScrolledPixels(
                    0.0f, true /* onTop */, true /* animate */);
        }
        mNotificationStackScroller.onPanelTrackingStopped();
        if (expand && (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED)) {
            if (!mHintAnimationRunning) {
                mAfforanceHelper.reset(true);
            }
        }
        if (!expand && (mStatusBar.getBarState() == StatusBarState.KEYGUARD
                || mStatusBar.getBarState() == StatusBarState.SHADE_LOCKED)) {
            KeyguardAffordanceView lockIcon = mKeyguardBottomArea.getLockIcon();
            lockIcon.setImageAlpha(0.0f, true, 100, mFastOutLinearInterpolator, null);
            lockIcon.setImageScale(2.0f, true, 100, mFastOutLinearInterpolator);
        }
    }

    /**
     * notification interface
     */
    @Override
    public void onHeightChanged(ExpandableView view, boolean needsAnimation) {

        // Block update if we are in quick settings and just the top padding changed
        // (i.e. view == null).
        if (view == null && mQsExpanded) {
            return;
        }
        requestPanelHeightUpdate();
    }

    @Override
    public void onReset(ExpandableView view) {
    }

    @Override
    public void onScrollChanged() {
        if (mQsExpanded) {
            requestScrollerTopPaddingUpdate(false /* animate */);
            requestPanelHeightUpdate();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mAfforanceHelper.onConfigurationChanged();
        if (newConfig.orientation != mLastOrientation) {
            resetVerticalPanelPosition();
        }
        mLastOrientation = newConfig.orientation;
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        mNavigationBarBottomHeight = insets.getSystemWindowInsetBottom();
        updateMaxHeadsUpTranslation();
        return insets;
    }

    private void updateMaxHeadsUpTranslation() {
        mNotificationStackScroller.setHeadsUpBoundaries(getHeight(), mNavigationBarBottomHeight);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        if (layoutDirection != mOldLayoutDirection) {
            mAfforanceHelper.onRtlPropertiesChanged();
            mOldLayoutDirection = layoutDirection;
        }
    }

    @Override
    public void onClick(View v) {
//        if (v == mHeader) {
//            onQsExpansionStarted();
//            if (mQsExpanded) {
//                flingSettings(0 /* vel */, false /* expand */, null, true /* isClick */);
//            } else if (mQsExpansionEnabled) {
//                EventLogTags.writeSysuiLockscreenGesture(
//                        EventLogConstants.SYSUI_TAP_TO_OPEN_QS,
//                        0, 0);
//                flingSettings(0 /* vel */, true /* expand */, null, true /* isClick */);
//            }
//        }
        /* begin: add by hmq 20160504状态栏分2页显示 */
        if (v == mBtnSwitchL) {
            mBtnSwitchR.setSelected(false);
            mBtnSwitchL.setSelected(true);
            switchHorizontalFling(0 /* vel */, true /* expand */);
            if (mHeader.getSwitchPageButton() != null) {
                mHeader.getSwitchPageButton().setEnabled(true);
                mHeader.getSwitchPageButton().setText(getResources().getString(R.string.status_bar_settings_settings_button));
            }
            mStackScrollerOverscrolling = false;
            mQsExpansionFromOverscroll = false;
            updateQsState();
        } else if (v == mBtnSwitchR) {
            mBtnSwitchR.setSelected(true);
            mBtnSwitchL.setSelected(false);
            switchHorizontalFling(0 /* vel */, false /* expand */);
            
            boolean showDismissView = mStatusBarState != StatusBarState.KEYGUARD && mStatusBar.hasActiveClearableNotifications();
            if (mHeader.getSwitchPageButton() != null) {
                mHeader.getSwitchPageButton().setEnabled(showDismissView);
                mHeader.getSwitchPageButton().setText(getResources().getString(R.string.status_bar_clear_all_button));
            }
        }
        /* end: add by hmq 20160504状态栏分2页显示 */
    }

    @Override
    public void onAnimationToSideStarted(boolean rightPage, float translation, float vel) {
        boolean start = getLayoutDirection() == LAYOUT_DIRECTION_RTL ? rightPage : !rightPage;
        mIsLaunchTransitionRunning = true;
        mLaunchAnimationEndRunnable = null;
        float displayDensity = mStatusBar.getDisplayDensity();
        int lengthDp = Math.abs((int) (translation / displayDensity));
        int velocityDp = Math.abs((int) (vel / displayDensity));
        if (start) {
            EventLogTags.writeSysuiLockscreenGesture(
                    EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_SWIPE_DIALER, lengthDp, velocityDp);
            mKeyguardBottomArea.launchLeftAffordance();
        } else {
            EventLogTags.writeSysuiLockscreenGesture(
                    EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_SWIPE_CAMERA, lengthDp, velocityDp);
            mSecureCameraLaunchManager.startSecureCameraLaunch();
        }
        mStatusBar.startLaunchTransitionTimeout();
        mBlockTouches = true;
    }

    @Override
    public void onAnimationToSideEnded() {
        mIsLaunchTransitionRunning = false;
        mIsLaunchTransitionFinished = true;
        if (mLaunchAnimationEndRunnable != null) {
            mLaunchAnimationEndRunnable.run();
            mLaunchAnimationEndRunnable = null;
        }
    }

    @Override
    protected void startUnlockHintAnimation() {
        super.startUnlockHintAnimation();
        startHighlightIconAnimation(getCenterIcon());
    }

    /**
     * Starts the highlight (making it fully opaque) animation on an icon.
     */
    private void startHighlightIconAnimation(final KeyguardAffordanceView icon) {
        icon.setImageAlpha(1.0f, true, KeyguardAffordanceHelper.HINT_PHASE1_DURATION,
                mFastOutSlowInInterpolator, new Runnable() {
                    @Override
                    public void run() {
                        icon.setImageAlpha(icon.getRestingAlpha(),
                                true /* animate */, KeyguardAffordanceHelper.HINT_PHASE1_DURATION,
                                mFastOutSlowInInterpolator, null);
                    }
                });
    }

    @Override
    public float getMaxTranslationDistance() {
        return (float) Math.hypot(getWidth(), getHeight());
    }

    @Override
    public void onSwipingStarted(boolean rightIcon) {
        boolean camera = getLayoutDirection() == LAYOUT_DIRECTION_RTL ? !rightIcon
                : rightIcon;
        if (camera) {
            mSecureCameraLaunchManager.onSwipingStarted();
            mKeyguardBottomArea.bindCameraPrewarmService();
        }
        requestDisallowInterceptTouchEvent(true);
        mOnlyAffordanceInThisMotion = true;
        mQsTracking = false;
    }

    @Override
    public void onSwipingAborted() {
        mKeyguardBottomArea.unbindCameraPrewarmService(false /* launched */);
    }

    @Override
    public void onIconClicked(boolean rightIcon) {
        if (mHintAnimationRunning) {
            return;
        }
        mHintAnimationRunning = true;
        mAfforanceHelper.startHintAnimation(rightIcon, new Runnable() {
            @Override
            public void run() {
                mHintAnimationRunning = false;
                mStatusBar.onHintFinished();
            }
        });
        rightIcon = getLayoutDirection() == LAYOUT_DIRECTION_RTL ? !rightIcon : rightIcon;
        if (rightIcon) {
            mStatusBar.onCameraHintStarted();
        } else {
            if (mKeyguardBottomArea.isLeftVoiceAssist()) {
                mStatusBar.onVoiceAssistHintStarted();
            } else {
                mStatusBar.onPhoneHintStarted();
            }
        }
    }

    @Override
    public KeyguardAffordanceView getLeftIcon() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL
                ? mKeyguardBottomArea.getRightView()
                : mKeyguardBottomArea.getLeftView();
    }

    @Override
    public KeyguardAffordanceView getCenterIcon() {
        return mKeyguardBottomArea.getLockIcon();
    }

    @Override
    public KeyguardAffordanceView getRightIcon() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL
                ? mKeyguardBottomArea.getLeftView()
                : mKeyguardBottomArea.getRightView();
    }

    @Override
    public View getLeftPreview() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL
                ? mKeyguardBottomArea.getRightPreview()
                : mKeyguardBottomArea.getLeftPreview();
    }

    @Override
    public View getRightPreview() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL
                ? mKeyguardBottomArea.getLeftPreview()
                : mKeyguardBottomArea.getRightPreview();
    }

    @Override
    public float getAffordanceFalsingFactor() {
        return mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
    }

    /**
     * 返回 长按status bar后一瞬间出现控件的高度
     */
    @Override
    protected float getPeekHeight() {
        return mHeader.getCollapsedHeight()+mDragScrollBar.getHeight();
        //hide by hmq 20160504状态栏分2页显示
//        if (mNotificationStackScroller.getNotGoneChildCount() > 0) {
//            return mNotificationStackScroller.getPeekHeight();
//        } else {
//            return mQsMinExpansionHeight * HEADER_RUBBERBAND_FACTOR;
//        }
    }

    @Override
    protected float getCannedFlingDurationFactor() {
        if (mQsExpanded) {
            return 0.7f;
        } else {
            return 0.6f;
        }
    }

    @Override
    protected boolean fullyExpandedClearAllVisible() {
        return mNotificationStackScroller.isDismissViewNotGone()
                && mNotificationStackScroller.isScrolledToBottom() && !mQsExpandImmediate;
    }

    @Override
    protected boolean isClearAllVisible() {
        return mNotificationStackScroller.isDismissViewVisible();
    }

    /**
     * 获得清除的高度
     * 
     */
    @Override
    protected int getClearAllHeight() {
        return mNotificationStackScroller.getDismissViewHeight();
    }

    @Override
    protected boolean isTrackingBlocked() {
        return mConflictingQsExpansionGesture && mQsExpanded;
    }

    public void notifyVisibleChildrenChanged() {
        if (mNotificationStackScroller.getNotGoneChildCount() != 0) {
            mReserveNotificationSpace.setVisibility(View.VISIBLE);
        } else {
            mReserveNotificationSpace.setVisibility(View.GONE);
        }
    }

    public boolean isQsExpanded() {
        return mQsExpanded;
    }

    public boolean isQsDetailShowing() {
        return mQsPanel.isShowingDetail();
    }

    public void closeQsDetail() {
        mQsPanel.closeDetail();
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public boolean isLaunchTransitionFinished() {
        return mIsLaunchTransitionFinished;
    }

    public boolean isLaunchTransitionRunning() {
        return mIsLaunchTransitionRunning;
    }

    public void setLaunchTransitionEndRunnable(Runnable r) {
        mLaunchAnimationEndRunnable = r;
    }

    public void setEmptyDragAmount(float amount) {
        float factor = 0.8f;
        if (mNotificationStackScroller.getNotGoneChildCount() > 0) {
            factor = 0.4f;
        } else if (!mStatusBar.hasActiveNotifications()) {
            factor = 0.4f;
        }
        mEmptyDragAmount = amount * factor;
        positionClockAndNotifications();
    }

    private static float interpolate(float t, float start, float end) {
        return (1 - t) * start + t * end;
    }

    public void setDozing(boolean dozing, boolean animate) {
        if (dozing == mDozing) return;
        mDozing = dozing;
        if (mStatusBarState == StatusBarState.KEYGUARD) {
            updateDozingVisibilities(animate);
        }
    }

    private void updateDozingVisibilities(boolean animate) {
        if (mDozing) {
            mKeyguardStatusBar.setVisibility(View.INVISIBLE);
            mKeyguardBottomArea.setVisibility(View.INVISIBLE);
        } else {
            mKeyguardBottomArea.setVisibility(View.VISIBLE);
            mKeyguardStatusBar.setVisibility(View.VISIBLE);
            if (animate) {
                animateKeyguardStatusBarIn(DOZE_ANIMATION_DURATION);
                mKeyguardBottomArea.startFinishDozeAnimation();
            }
        }
    }

    @Override
    public boolean isDozing() {
        return mDozing;
    }

    public void setShadeEmpty(boolean shadeEmpty) {
        mShadeEmpty = shadeEmpty;
        updateEmptyShadeView();
    }

    private void updateEmptyShadeView() {

        // Hide "No notifications" in QS.
        mNotificationStackScroller.updateEmptyShadeView(mShadeEmpty/* && !mQsExpanded*/);//edit by hmq 20160504状态栏分2页显示显示隐藏 没有通知 
    }

    public void setQsScrimEnabled(boolean qsScrimEnabled) {
        boolean changed = mQsScrimEnabled != qsScrimEnabled;
        mQsScrimEnabled = qsScrimEnabled;
        if (changed) {
            updateQsState();
        }
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    private final Runnable mUpdateHeader = new Runnable() {
        @Override
        public void run() {
            mHeader.updateEverything();
        }
    };

    public void onScreenTurningOn() {
        mKeyguardStatusView.refreshTime();
    }

    @Override
    public void onEmptySpaceClicked(float x, float y) {
        onEmptySpaceClick(x);
    }

    protected boolean onMiddleClicked() {
        switch (mStatusBar.getBarState()) {
            case StatusBarState.KEYGUARD:
                if (!mDozingOnDown) {
                    EventLogTags.writeSysuiLockscreenGesture(
                            EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_TAP_UNLOCK_HINT,
                            0 /* lengthDp - N/A */, 0 /* velocityDp - N/A */);
                    startUnlockHintAnimation();
                }
                return true;
            case StatusBarState.SHADE_LOCKED:
                if (!mQsExpanded) {
                    mStatusBar.goToKeyguard();
                }
                return true;
            case StatusBarState.SHADE:

                // This gets called in the middle of the touch handling, where the state is still
                // that we are tracking the panel. Collapse the panel after this is done.
                post(mPostCollapseRunnable);
                return false;
            default:
                return true;
        }
    }

    /**
     * 绘制自己的孩子
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.RED);
            p.setStrokeWidth(2);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawLine(0, getMaxPanelHeight(), getWidth(), getMaxPanelHeight(), p);
            p.setColor(Color.BLUE);
            canvas.drawLine(0, getExpandedHeight(), getWidth(), getExpandedHeight(), p);
            p.setColor(Color.GREEN);
            canvas.drawLine(0, calculatePanelHeightQsExpanded(), getWidth(),
                    calculatePanelHeightQsExpanded(), p);
            p.setColor(Color.YELLOW);
            canvas.drawLine(0, calculatePanelHeightShade(), getWidth(),
                    calculatePanelHeightShade(), p);
            p.setColor(Color.MAGENTA);
            canvas.drawLine(0, calculateQsTopPadding(), getWidth(),
                    calculateQsTopPadding(), p);
            p.setColor(Color.CYAN);
            canvas.drawLine(0, mNotificationStackScroller.getTopPadding(), getWidth(),
                    mNotificationStackScroller.getTopPadding(), p);
        }
    }

    @Override
    public void onHeadsUpPinnedModeChanged(final boolean inPinnedMode) {
        if (inPinnedMode) {
            mHeadsUpExistenceChangedRunnable.run();
            updateNotificationTranslucency();
        } else {
            mHeadsUpAnimatingAway = true;
            mNotificationStackScroller.runAfterAnimationFinished(
                    mHeadsUpExistenceChangedRunnable);
        }
    }

    @Override
    public void onHeadsUpPinned(ExpandableNotificationRow headsUp) {
        mNotificationStackScroller.generateHeadsUpAnimation(headsUp, true);
    }

    @Override
    public void onHeadsUpUnPinned(ExpandableNotificationRow headsUp) {
    }

    @Override
    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean isHeadsUp) {
        mNotificationStackScroller.generateHeadsUpAnimation(entry.row, isHeadsUp);
    }

    @Override
    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        super.setHeadsUpManager(headsUpManager);
        mHeadsUpTouchHelper = new HeadsUpTouchHelper(headsUpManager, mNotificationStackScroller,
                this);
    }

    public void setTrackingHeadsUp(boolean tracking) {
        if (tracking) {
            mNotificationStackScroller.setTrackingHeadsUp(true);
            mExpandingFromHeadsUp = true;
        }
        // otherwise we update the state when the expansion is finished
    }

    @Override
    protected void onClosingFinished() {
        super.onClosingFinished();
        resetVerticalPanelPosition();
        setClosingWithAlphaFadeout(false);
    }

    private void setClosingWithAlphaFadeout(boolean closing) {
        mClosingWithAlphaFadeOut = closing;
        mNotificationStackScroller.forceNoOverlappingRendering(closing);
    }

    /**
     * Updates the vertical position of the panel so it is positioned closer to the touch
     * responsible for opening the panel.
     *
     * @param x the x-coordinate the touch event
     */
    private void updateVerticalPanelPosition(float x) {
        // M: Fix the display wrong issue when the width = 0 unexpected.
        // Step: set screen lock as none and reboot then
        if (mNotificationStackScroller.getWidth() <= 0
                || mNotificationStackScroller.getWidth() * 1.75f > getWidth()) {
            resetVerticalPanelPosition();
            return;
        }
        float leftMost = mPositionMinSideMargin + mNotificationStackScroller.getWidth() / 2;
        float rightMost = getWidth() - mPositionMinSideMargin
                - mNotificationStackScroller.getWidth() / 2;
        if (Math.abs(x - getWidth() / 2) < mNotificationStackScroller.getWidth() / 4) {
            x = getWidth() / 2;
        }
        x = Math.min(rightMost, Math.max(leftMost, x));
        setVerticalPanelTranslation(x -
                (mNotificationStackScroller.getLeft() + mNotificationStackScroller.getWidth() / 2));
     }

    private void resetVerticalPanelPosition() {
        setVerticalPanelTranslation(0f);
    }

    private void setVerticalPanelTranslation(float translation) {
        mNotificationStackScroller.setTranslationX(translation);
        mScrollView.setTranslationX(translation);
        mHeader.setTranslationX(translation);
    }

    private void updateStackHeight(float stackHeight) {
        mNotificationStackScroller.setStackHeight(stackHeight);
        updateKeyguardBottomAreaAlpha();
    }

    public void setPanelScrimMinFraction(float minFraction) {
        mBar.panelScrimMinFractionChanged(minFraction);
    }

    public void clearNotificattonEffects() {
        mStatusBar.clearNotificationEffects();
    }

    protected boolean isPanelVisibleBecauseOfHeadsUp() {
        return mHeadsUpManager.hasPinnedHeadsUp() || mHeadsUpAnimatingAway;
    }

    // added by yzs for MagcommLocker begin
    public void setCappuLockerView(CappuBootUnlockView cappuBootUnlockView) {
        mCappuBootUnlockView = cappuBootUnlockView;
    }

    public CappuBootUnlockView getCappuLockerView() {
        return mCappuBootUnlockView;
    }

    public void setMagcommLockerView(MagcommBootUnlockView magcommLockerView) {
        mMagcommBootUnlockView = magcommLockerView;
    }

    public MagcommBootUnlockView getMagcommLockerView() {
        return mMagcommBootUnlockView;
    }
    // added by yzs for MagcommLocker end
    
    /* begin: add by hmq 20160504状态栏分2页显示 */
    /**
     * 更改SwitchToolBar在屏幕的位置
     * Warning: 这个方法必须放在updateDragScrollBar()方法之后，因为是根据DragScrollBar来绘制其高度的
     */
    private void updateSwitchToolBar() {
        if (mStatusBar.getBarState() != StatusBarState.KEYGUARD && mStatusBar.getBarState() != StatusBarState.SHADE_LOCKED) {//非锁屏状态才会更新SwitchToolBar
            mSwitchToolBar.setTranslationY(mDragScrollBar.getTranslationY()-mSwitchToolBar.getHeight());
        }
    }
    
    /**
     * 更改DragScrollBar在屏幕的位置
     * Warning: 这个方法必须放在updateHeader()方法之后，因为是根据header来绘制其高度的
     */
    private void updateDragScrollBar(){
        if (mStatusBar.getBarState() != StatusBarState.KEYGUARD && mStatusBar.getBarState() != StatusBarState.SHADE_LOCKED) {//非锁屏状态才会更新SwitchToolBar
            if (getExpandedHeight() <= getPeekHeight()){
                mDragScrollBar.setTranslationY(Math.min(getPeekHeight()- mDragScrollBar.getHeight(), getExpandedHeight() - mDragScrollBar.getHeight()));
            }else{
                mDragScrollBar.setTranslationY(Math.min(getHeight() - mDragScrollBar.getHeight(), getExpandedHeight() - mDragScrollBar.getHeight()));
            }
            if (isFullyExpanded() && !isFullHeight) {
                updateDragScrollImgAnim(true);
                isFullHeight = true;
            } else if (!isFullyExpanded() && isFullHeight) {
                updateDragScrollImgAnim(false);
                isFullHeight = false;
            }
        }
    }
    
    private void updateDragScrollImgAnim(boolean isUpToLineImg) {
        if(mDragScrollImgAlphaToMaxAnim != null || mDragScrollImgAlphaToMinAnim != null){
            mDragScrollImgAlphaToMaxAnim.cancel();
            mDragScrollImgAlphaToMaxAnim = null;
            mDragScrollImgAlphaToMinAnim.cancel();
            mDragScrollImgAlphaToMinAnim = null;
        }
        mDragScrollLineImg.setVisibility(isUpToLineImg ? View.INVISIBLE : View.VISIBLE);
        mDragScrollupImg.setVisibility(isUpToLineImg ? View.VISIBLE : View.INVISIBLE);
        
        mDragScrollImgAlphaToMinAnim = new AlphaAnimation(1f, 0f);
        mDragScrollImgAlphaToMinAnim.setDuration(300);// 设置动画持续时间
        mDragScrollImgAlphaToMinAnim.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        
        mDragScrollImgAlphaToMaxAnim = new AlphaAnimation(0f, 1f);
        mDragScrollImgAlphaToMaxAnim.setDuration(220);// 设置动画持续时间
        mDragScrollImgAlphaToMaxAnim.setFillAfter(true);// 动画执行完后是否停留在执行完的状态
        mDragScrollImgAlphaToMaxAnim.setStartOffset(100);// 执行前的等待时间
        
        mDragScrollupImg.setAnimation(isUpToLineImg ? mDragScrollImgAlphaToMaxAnim : mDragScrollImgAlphaToMinAnim);
        mDragScrollLineImg.setAnimation(isUpToLineImg ? mDragScrollImgAlphaToMinAnim : mDragScrollImgAlphaToMaxAnim);

        mDragScrollImgAlphaToMinAnim.start();
        mDragScrollImgAlphaToMaxAnim.start();
    }
    
    public void updateDismissView(boolean isEnabled) {
        if (mNotificationStackScroller.getTranslationX() == 0 && mHeader.getSwitchPageButton() != null) {
            if(mNotificationStackScroller.isDismissViewNotGone()){
                mHeader.getSwitchPageButton().setEnabled(isEnabled);
                mNotificationStackScroller.updateDismissView(false);
            }else{
                mHeader.getSwitchPageButton().setEnabled(isEnabled);
            }
        }
    }
    
    /**
     * 判断是否横竖屏
     * @return true横屏 false竖屏
     */
    public boolean getOrientationScreen(){
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenHeight = wm.getDefaultDisplay().getHeight();
        
        if (mScreenWidth > mScreenHeight){
            return true;
        }
        return false;
    }
    /* end: add by hmq 20160504状态栏分2页显示 */
}
