package com.cappu.launcherwin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.cappu.launcherwin.BubbleView.OnChildViewDrag;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.widget.Indicator;
import com.cappu.launcherwin.widget.LauncherLog;

public class DragLayer extends RelativeLayout implements OnChildViewDrag{

    private String TAG = "DragLayer";
    private Hotseat mHotseat;
    private Indicator mIndicator;
    private Context mContext;
    private Launcher mLauncher;
    
    private Workspace mWorkspace;
    DragController mDragController;
    boolean isDrag = false;
    
    int mBackgroundRes = 0;
    public DragLayer(Context context) {
    	super(context);
    }
    public DragLayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }    
    
    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
    }

    @SuppressLint("NewApi")
    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        if(mContext == null){
            mContext = getContext();
        }    
        LauncherLog.v(TAG, "onFinishInflate,jeff");
        initWorkspace();
        initIndicatorHotseat();
        setBackgroundInMode();
        setHotsetBackgroud();           
    }

    private void initWorkspace(){
    	mWorkspace=(Workspace)findViewById(R.id.pagedview_workspace);
    }
    
    public Workspace getWorkspace(){
        return mWorkspace;
    }
    
    private void initIndicatorHotseat(){
    	mHotseat=(Hotseat)findViewById(R.id.ly_hotseat);
    	mHotseat.updateHotseatIcon();
    	mIndicator=(Indicator)findViewById(R.id.view_indicator);
    	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
    		BitmapDrawable drawable = new BitmapDrawable(ThemeManager.getInstance().getImagePiece().indicatorBmp);
    		mIndicator.setBackground(drawable);
    	}else{
    		mIndicator.setBackgroundResource(R.drawable.shape_transparent);
    	}
    	mIndicator.updateIndicator();
    }
    
    //added by yzs for unreadView begin
	public void refreshPhoneState(int unreadPhone) {
		if (unreadPhone > 0) {
			mHotseat.imgvMiddle.setImageBitmap(ThemeManager.getInstance()
					.getUnreadIcon(
							ThemeManager.getInstance().getHotSeatIcon(
									(String) mHotseat.imgvMiddle.getTag()),
							unreadPhone));
		} else {
			mHotseat.imgvMiddle.setImageBitmap(ThemeManager.getInstance()
					.getHotSeatIcon((String) mHotseat.imgvMiddle.getTag()));
		}
	}

	public void refrshMmsState(int unreadMms) {
		if (unreadMms > 0) {
			mHotseat.imgvLeft
					.setImageBitmap(ThemeManager.getInstance().getUnreadIcon(
							ThemeManager.getInstance().getHotSeatIcon(
									(String) mHotseat.imgvLeft.getTag()), unreadMms));
		} else {
			mHotseat.imgvLeft.setImageBitmap(ThemeManager.getInstance()
					.getHotSeatIcon((String) mHotseat.imgvLeft.getTag()));
		}
	}
    //added by yzs for unreadView end
   
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException( "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
        }
        //width 当前的DragLayer 的宽度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //heigh 当前的DragLayer 的高度
        int heigh = MeasureSpec.getSize(heightMeasureSpec);
        //由于当前dragLayer的高度包含了状态栏 这里除去
        heigh = heigh -getStatusBarHeight();
        Log.i(TAG, "onMeasure    width"+width+"     heigh"+heigh);
        mDragController.setScreenWidth(width);
        mDragController.setScreenHeight(heigh);

    }
    
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG, "getStatusBarHeight-----"+resourceId);
        return result;
    }


    @SuppressLint("NewApi")
    public void themeChange() {
    	LauncherLog.v(TAG, "themeChange,jeff");
        initWorkspace();
        initIndicatorHotseat();
        setHotsetBackgroud();
        setBackgroundInMode();  
        
        refreshPhoneState(mLauncher.getUnreceivedCallCount());
        refrshMmsState(mLauncher.getNewMmsCount() + mLauncher.getNewSmsCount());
    }

    /**设置launcher 的整体 背景颜色,默认是米色 #dddddd*/
	public void setBackgroundInMode(){
    	String launcherBg=ThemeRes.getInstance().getThemeBgName("launcher");
    	LauncherLog.v(TAG, "setBackgroundInMode,jeff launcherBg="+launcherBg);
    	if(launcherBg==null || launcherBg==""){
    		LauncherLog.v(TAG, "setBackgroundInMode,jeff ThemeType="+ThemeManager.getInstance().getCurrentThemeType());
    		if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS ){
    			setBackground(ThemeManager.getInstance().getCurrentWallPaper());
    			return;
    		}else{
    			launcherBg="#dddddd";
    		}
    	}
    	setBackgroundColor(Color.parseColor(launcherBg));
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDragController.dispatchTouchEvent(ev);
        
        return super.dispatchTouchEvent(ev);
    }
    
    /**设置hotset的背景颜色*/
	public void setHotsetBackgroud() {
		Bitmap bm = ThemeRes.getInstance().getThemeBackground("hotseat");
		if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
			bm=ThemeManager.getInstance().getImagePiece().hotseatBmp;
		}
		BitmapDrawable drawable = new BitmapDrawable(bm);
		mHotseat.setBackground(drawable);
	}

    @Override
    public void onDragView(View view) {
        
        boolean isTouchScrolling = mWorkspace.isTouchScrolling();
        
        Log.i(TAG, "isTouchScrolling:"+isTouchScrolling+"        mTouchState::"+mWorkspace.getTouchState());
        if(!isDrag){//判断 当前 桌面不是做滑动状态
            isDrag = true;
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(true);
            Bitmap bm = Bitmap.createBitmap(view.getDrawingCache());
            view.setVisibility(View.GONE);
            mDragController.setWorkspace(mWorkspace);
            mDragController.startDrag(bm, (int) (mDragController.getLastMotionX()), (int) (mDragController.getLastMotionY()), view);
        }
    }

}
