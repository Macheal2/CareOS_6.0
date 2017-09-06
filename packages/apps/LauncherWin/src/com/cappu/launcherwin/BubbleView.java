package com.cappu.launcherwin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.speech.SpeechTools;

// add by y.haiyang for speech more (start)
import com.cappu.launcherwin.speech.LauncherSpeechTools;
// add by y.haiyang for speech more (end)
import com.cappu.launcherwin.tools.ImageHelper;
import com.cappu.launcherwin.widget.LauncherLog;

//START: added by Yar @20170824
import android.database.ContentObserver;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import java.util.Map;
//END: added by Yar @20170824

public class BubbleView extends RelativeLayout {
    
    private String TAG = "BubbleView";
    private LayoutInflater mInflater;	
    private Context mContext;
    private Launcher mLauncher;

    public ImageView mImageView;
    public TextView mTextView;
    private RelativeLayout mRelativeLayout;
    private RelativeLayout mRelativeLayoutHead;
    public RelativeLayout mRelativeLayoutInner;
    
    ThemeTools mThemeTools;
    Drawable mDrawable;
    String mTitle;

    // add by y.haiyang for speech more (start)
    /**
     * private SpeechTools mSpeechTools = null;
     */
    private LauncherSpeechTools mSpeechTools;
    // modify by y.haiyang for speech more (end)

    private ItemInfo mItemInfo;
    Context mTContext = null;
    
    Paint mTextPaint = new Paint();
    private ScaleAnimation animationBegin;
    private ScaleAnimation animationEnd;
    
    public BubbleView(Context context) {
        this(context, null);
    }
    public BubbleView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
        setTypeface();
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
        	animationBegin=new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, 
    				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationEnd=new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, 
    				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationBegin.setAnimationListener(new AnimationListener() {
    			
    			@Override
    			public void onAnimationStart(Animation animation) {
    				// TODO Auto-generated method stub
    				LauncherLog.v(TAG, "onAnimationStart,jeff animationBegin");
    				mRelativeLayoutHead.setBackground(new BitmapDrawable(createIconBgBitmap(mItemInfo.pieceBg)));
    			}
    			
    			@Override
    			public void onAnimationRepeat(Animation animation) {
    				// TODO Auto-generated method stub
    				LauncherLog.v(TAG, "onAnimationRepeat,jeff animationBegin");
    			}
    			
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				// TODO Auto-generated method stub
    				LauncherLog.v(TAG, "onAnimationEnd,jeff animationBegin");
    			}
    		});
            animationEnd.setAnimationListener(new AnimationListener() {
    			
    			@Override
    			public void onAnimationStart(Animation animation) {
    				// TODO Auto-generated method stub
    				LauncherLog.v(TAG, "onAnimationStart,jeff animationEnd");
    			}
    			
    			@Override
    			public void onAnimationRepeat(Animation animation) {
    				// TODO Auto-generated method stub
    				LauncherLog.v(TAG, "onAnimationRepeat,jeff animationEnd");
    			}
    			
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				// TODO Auto-generated method stub
    				LauncherLog.v(TAG, "onAnimationEnd,jeff animationEnd");
    				mRelativeLayoutHead.setBackground(new BitmapDrawable(mItemInfo.pieceBg));
    			}
    		});
        }else{
        	animationBegin=new ScaleAnimation(1.0f, 0.95f, 1.0f, 0.95f, 
    				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationEnd=new ScaleAnimation(0.95f, 1.0f, 0.95f, 1.0f, 
    				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        animationBegin.setDuration(300);
        animationBegin.setFillAfter(true);
        animationEnd.setDuration(300);
        animationEnd.setFillAfter(true);
    }
    
    private void setTypeface() {
        mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/minilishu.ttf");
    }

    @Override
    protected void onFinishInflate() {
    	LauncherLog.v(TAG, "onFinishInflate, jeff");
        if(mContext == null){
            mContext = getContext();
        }
        if (mInflater == null) {
            mInflater = ((Activity) mContext).getLayoutInflater();
        }

        mImageView = (ImageView) findViewById(R.id.bubbleView_icon);
        mTextView = (TextView) findViewById(R.id.bubbleView_text);
        mRelativeLayout=(RelativeLayout)findViewById(R.id.ry_bubbleView);
        mRelativeLayoutHead=(RelativeLayout)findViewById(R.id.ry_bubbleView_head);
        mRelativeLayoutInner=(RelativeLayout)findViewById(R.id.ry_bubbleView_inner);
        if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
            mTextView.setTypeface(mTypeface);
            mTextView.setTextColor(Color.BLACK);
        }
        registerBoradcastReceiver();
        super.onFinishInflate();
    }
    private boolean isReceiver=false;
    private void registerBoradcastReceiver() {
    	if(!isReceiver){
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("com.cappu.bubbleview.refresh");
		
		mContext.registerReceiver(mBroadcastReceiver, myIntentFilter);
		isReceiver=true;
    	}
	}
    public void unRegisterBoradcastReceiver(){
    	if(isReceiver){
    	mContext.unregisterReceiver(mBroadcastReceiver);
    	isReceiver=false;
    	}
    }
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LauncherLog.v(TAG, "onReceive,jeff action="+action);
			if (action.equals("com.cappu.bubbleview.refresh")) {
				int textsize=Settings.Global.getInt(mContext.getContentResolver(),
						"textSize", mContext.getResources()
								.getDimensionPixelSize(R.dimen.xl_text_size));
				mTextView.setTextSize(textsize);
				int fontHeight = getFontHeight(textsize);
				if ((fontHeight * density) >= textViewHeight) {// 当字体高度大于TextView的高度时候做异常处理
					LauncherLog.v(TAG, "onReceive,jeff fontHeight * density="+fontHeight * density);
					int ts = textsize;
					LauncherLog.v(TAG, "onReceive,jeff ts="+ts);
					while ((fontHeight * density) >= textViewHeight) {
						ts -= 4;
						fontHeight = getFontHeight(ts);
					}
					LauncherLog.v(TAG, "onReceive,jeff ts="+ts);
					mTextView.setTextSize(ts);
				} else {
					mTextView.setTextSize(textsize);
				}
			}
			
		}
	};	
    @Override
    public void draw(Canvas canvas) {
    	LauncherLog.v(TAG, "draw, jeff");
        super.draw(canvas);
    }

    /**自定义Bubbleview的单击事件*/
    public interface OnChildViewClick {
        public void onClick(Context c);
    }
    
    /**自定义Bubbleview的长按事件*/
    public interface OnChildViewLongClick {
        public void onLongClick(Context c,View view);
    }
    
    /**自定义Bubbleview的拖拽事件*/
    public interface OnChildViewDrag {
        public void onDragView(View view);
    }

    OnChildViewClick mOnChildViewClick=null;
    public void setOnClickIntent(OnChildViewClick onclick){
        this.mOnChildViewClick=onclick;
    }

    OnChildViewLongClick mOnChildViewLongClick=null;
    public void setOnClickIntent(OnChildViewLongClick onLongclick){
        this.mOnChildViewLongClick=onLongclick;
    }
    
    OnChildViewDrag mOnChildViewDrag=null;
    public void setOnChildViewDrag(OnChildViewDrag onDrag){
        this.mOnChildViewDrag=onDrag;
    }
    private Bitmap createIconBgBitmap(Bitmap bitmap) {
		Bitmap mBitmap=bitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas=new Canvas(mBitmap);
		Drawable drawable = mContext.getResources().getDrawable(R.drawable.cell_nine_grids_middle_inner);
		drawable.setBounds(0, 0,bitmap.getWidth(),bitmap.getHeight());
		drawable.draw(canvas);
		return mBitmap;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

	private boolean onTouchEventDefault(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			downTime = event.getEventTime();
			if(mItemInfo.spanX == 1 && mItemInfo.spanY == 1){
				startAnimation(animationBegin);
			}
			mActionFinish = false;
			break;
		case MotionEvent.ACTION_UP:
			currentTime = event.getEventTime();
			if((currentTime - downTime)<500 && mOnChildViewClick != null && mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET){
				LauncherLog.v(TAG, "onTouchEventDefault,jeff up click");
				bubbleViewClick();
            }
			if (!mActionFinish&&mItemInfo.spanX == 1 && mItemInfo.spanY == 1) {
				startAnimation(animationEnd);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if (!mActionFinish &&mItemInfo.spanX == 1 && mItemInfo.spanY == 1) {
				startAnimation(animationEnd);
			}
			break;
		}
		return true;
	}

	private boolean onTouchEventNine(MotionEvent event) {
		if (mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
				&& (mItemInfo.spanX > 1 || mItemInfo.spanY > 1)&&mItemInfo.screen!=2) {
			return true;
		}
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			downTime = event.getEventTime();
			mActionFinish = true;
			break;
		case MotionEvent.ACTION_MOVE:
			currentTime = event.getEventTime();
			if (currentTime - downTime > 400 && currentTime - downTime < 600
					&& mActionFinish) {
				LauncherLog.v(TAG, "onTouchEvent,jeff move");
				//hejianfeng add start
				if(mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET&& mItemInfo.screen==2){
					return true;
				}
				//hejianfeng add end
				mRelativeLayoutHead.startAnimation(animationBegin);
				mActionFinish = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			currentTime = event.getEventTime();
			if (currentTime - downTime < 400 && mOnChildViewClick != null && mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
				LauncherLog.v(TAG, "onTouchEventNine,jeff up click");
				bubbleViewClick();
				return true;
			}
			if (!mActionFinish) {
				mRelativeLayoutHead.startAnimation(animationEnd);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			mRelativeLayoutInner
					.setBackgroundResource(R.drawable.shape_transparent);
			if (!mActionFinish) {
				mRelativeLayoutHead.startAnimation(animationEnd);
			}
			break;
		}
		return true;
	}
    private long downTime;
    private long currentTime;
    private boolean mActionFinish =true;
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		LauncherLog.v(TAG,
				"onTouchEvent,jeff event.getAction()=" + event.getAction());
		if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
			return onTouchEventNine(event);
		}else{
			return onTouchEventDefault(event);
		}
	}
    
    public void bubbleViewClick(){
        if(mSpeechTools!=null){
        	if(mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
				&& (mItemInfo.spanX > 1 && mItemInfo.spanY > 1)&&mItemInfo.screen==2){
        		mSpeechTools.startSpeech(mContext.getString(R.string.app_name_music),getLauncherStatus());
        	}else{
        		mSpeechTools.startSpeech(mTextView.getText().toString(),getLauncherStatus());
        	}
        }
        if(mOnChildViewClick!=null){
            mOnChildViewClick.onClick(getContext());
        }
    }
    public boolean getLauncherStatus() {
        return Settings.Global.getInt(getContext().getContentResolver(), "launcher_speech_status",
                getResources().getInteger(R.integer.launcher_speech_status)) == 1 ? true : false;
    }
    
    //hejianfeng add start
    /**
     * 设置联系人icon图标
     * @param itemInfo
     */
    private void setContactsIcon(ItemInfo itemInfo,float density,int textViewHeight){
		ItemContacts ic = (ItemContacts) itemInfo;
		if (ic.getCustomHeader()) {
			if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
				mImageView.setImageBitmap(ImageHelper.GetRoundedCornerBitmap(ic.getHeadBitmap()));
				mRelativeLayoutHead.setBackground(new BitmapDrawable(ic.pieceBg));
			}else{
				mImageView.setImageBitmap(null);
				setBackground(new BitmapDrawable(
						ImageHelper.GetRoundedCornerBitmap(ic.getHeadBitmap())));
			}
		} else {
			mImageView.setScaleType(ScaleType.CENTER_INSIDE);
			mImageView.setImageBitmap(ic.getHeadBitmap());
			if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
				mRelativeLayoutHead.setBackground(new BitmapDrawable(ic.pieceBg));
			}else{
				gradualChangeBackgroundResource(ic.getBackgroundRes());
			}
		}
		mTextView.setText(ic.getTitle());
		int fontHeight = getFontHeight(ic.textSize);
		if ((fontHeight * density) >= textViewHeight) {// 当字体高度大于TextView的高度时候做异常处理
			int ts = ic.textSize;
			while ((fontHeight * density) >= textViewHeight) {
				ts -= 4;
				fontHeight = getFontHeight(ts);
			}
			mTextView.setTextSize(ts);
		} else {
			mTextView.setTextSize(ic.textSize);
		}
		int res = ic.getTextBackgroundRes();
		if (res == -1 && !ic.getCustomHeader()) {
			mTextView.setBackground(null);
		}
//		if (ic.screen == 0) {
//			mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//		}
		
		//START: added by Yar @20170824
		if (!isRegisterContacts && ic.mRawId != -1 && ic.mRawId != 0 && ic.phoneNumber != null && !ic.phoneNumber.equals("")) {
			mObserver = new ContactsObserver(mLauncher.mHandler);
			mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.android.contacts/contacts/"), true, mObserver);
			isRegisterContacts = true;
			android.util.Log.i("Yar", " registerContentObserver() mObserver isRegisterContacts = " + isRegisterContacts);
		}
		//END: added by Yar @20170824
    }
    /**
     * 设置快捷方式icon图标
     * @param itemInfo
     */
    private void setShortcutIcon(ItemInfo itemInfo,float density,int textViewHeight){
    	LauncherLog.v(TAG, "setShortcutIcon,jeff mRelativeLayout.getChildCount()="+mRelativeLayout.getChildCount());
    	for (int i = 0; i < mRelativeLayout.getChildCount(); i++) {
			if (mRelativeLayout.getChildAt(i) != mImageView && mRelativeLayout.getChildAt(i) != mTextView) {
				mRelativeLayout.removeViewAt(i);
				mImageView.setVisibility(View.VISIBLE);
			}
		}
		ItemShortcut is = (ItemShortcut) itemInfo;
		LauncherLog.v(TAG, "setShortcutIcon,jeff title="+is.getTitle()+",textSize="+is.textSize);
		mImageView.setScaleType(ScaleType.CENTER_INSIDE);
		mImageView.setImageBitmap(is.getIconDrawable());
		mTextView.setText(is.getTitle());

		int fontHeight = getFontHeight(is.textSize);
		if ((fontHeight * density) >= textViewHeight) {// 当字体高度大于TextView的高度时候做异常处理
			int ts = is.textSize;
			while ((fontHeight * density) >= textViewHeight) {
				ts -= 4;
				fontHeight = getFontHeight(ts);
			}
			mTextView.setTextSize(ts);
		} else {
			mTextView.setTextSize(is.textSize);
		}
		if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
			mRelativeLayoutHead.setBackground(new BitmapDrawable(is.pieceBg));
		}else{
			gradualChangeBackgroundResource(is.getBackgroundRes());
		}
    }
    /**
     * 设置小组件icon图标
     * @param itemInfo
     */
    private void setAppWidgetIcon(ItemInfo itemInfo,float density,int textViewHeight){
		for (int i = 0; i < mRelativeLayout.getChildCount(); i++) {
			if (mRelativeLayout.getChildAt(i).getId() == R.id.bubbleView_icon) {
				mRelativeLayout.getChildAt(i).setVisibility(View.GONE);
			}
		}
		ItemWidget iw = (ItemWidget) itemInfo;
		LauncherLog.v(TAG, "setAppWidgetIcon,jeff title="+iw.getTitle()+",textSize="+iw.textSize);
		if (ThemeManager.getInstance().isExistWidget(iw.intent, "com.cappu.launcherwin.kookview.AlbumsKookView")) {
			mTextView.setText(R.string.aifenxiang);
			//hejianfeng add start
			if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_CHINESESTYLE){
				RelativeLayout.LayoutParams viewLayoutParams=(RelativeLayout.LayoutParams)mTextView.getLayoutParams();
				viewLayoutParams.setMargins(0, 0, 75, 40);
				mTextView.setLayoutParams(viewLayoutParams);
				mTextView.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
			}else{
				mTextView.setGravity(Gravity.RIGHT | Gravity.CENTER_HORIZONTAL);
			}
			//hejianfeng add end
		} else {
			mTextView.setText(iw.getTitle());
		}
		//hejianfeng add start
		if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS
				&&(ThemeManager.getInstance().isExistWidget(iw.intent, "com.cappu.launcherwin.kookview.CappuKookView"))){
			mRelativeLayoutHead.setPadding(10, 0, 10, 60);
		} else if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS
				&& ThemeManager
						.getInstance()
						.isExistWidget(iw.intent,
								"com.cappu.launcherwin.kookview.assembly.AssemblyJeffView")) {
			mRelativeLayoutHead.setPadding(0, 0, 0, 40);
		} else if (ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS
				&& ThemeManager
				.getInstance()
				.isExistWidget(iw.intent,
						"com.cappu.launcherwin.kookview.MusicKookView")) {
			mRelativeLayoutHead.setPadding(0, 0, 0, 40);
		}
		//hejianfeng add end
		int fontHeight = getFontHeight(iw.textSize);
		if ((fontHeight * density) >= textViewHeight) {// 当字体高度大于TextView的高度时候做异常处理
			int ts = iw.textSize;
			while ((fontHeight * density) >= textViewHeight) {
				ts -= 4;
				fontHeight = getFontHeight(ts);
			}
			mTextView.setTextSize(ts);
		} else {
			mTextView.setTextSize(iw.textSize);
		}
		if(ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.MusicKookView") 
				||ThemeManager.getInstance().isExistWidget(iw.intent,"com.cappu.launcherwin.kookview.assembly.AssemblyJeffView")){
			mTextView.setGravity(Gravity.RIGHT | Gravity.CENTER_HORIZONTAL);
		}
		if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
			mRelativeLayoutHead.setBackground(new BitmapDrawable(iw.pieceBg));
		}else{
			gradualChangeBackgroundResource(iw.getBackgroundRes());
		}
    }
    //hejianfeng add end
    private Typeface mTypeface;
    private void updateBubbleView(int textViewHeight,int size){
    	RelativeLayout.LayoutParams viewLayoutParams=(RelativeLayout.LayoutParams)mTextView.getLayoutParams();
    	viewLayoutParams.height=textViewHeight;
    	mTextView.setLayoutParams(viewLayoutParams);
    	
    	RelativeLayout.LayoutParams imgvLayoutParams=(RelativeLayout.LayoutParams)mImageView.getLayoutParams();
    	imgvLayoutParams.width=size;
		if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS){
			imgvLayoutParams.topMargin=30;
			setBackground(new BitmapDrawable(mItemInfo.pieceHalfBg));
		}else{
			setBackgroundResource(R.drawable.shape_transparent);
		}
    	mImageView.setLayoutParams(imgvLayoutParams);
    	
    }
    //hejianfeng add start
    private int textViewHeight;
    private float density;
    //hejianfeng add end
    // add by y.haiyang for speech (start)
    @SuppressLint("NewApi")
    /**
     * public void setItemInfo(ItemInfo itemInfo,SpeechTools speechTools){
     */    
    public void setItemInfo(Launcher launcher,ItemInfo itemInfo,LauncherSpeechTools speechTools){
    // add by y.haiyang for speech (end)    
        mLauncher = launcher;
        density = mLauncher.getDisplayMetrics().density;
        textViewHeight = (int) (40 * density);
        
        this.mItemInfo = itemInfo;
        this.mSpeechTools = speechTools;
        LauncherLog.v(TAG, "setItemInfo,jeff itemInfo.screen="+itemInfo.screen);
        //hejianfeng add start
        if(ThemeManager.getInstance().getCurrentThemeType() == ThemeManager.THEME_NINE_GRIDS){
        	if(itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_CONTACTS){
        		updateBubbleView(90,165);
        	}else{
        		updateBubbleView(90,150);
        	}
			if (itemInfo.screen != 1 && itemInfo.screen != 2 && itemInfo.screen != 3) {
				RelativeLayout.LayoutParams ryLayoutParams = (RelativeLayout.LayoutParams) mRelativeLayoutHead
						.getLayoutParams();
				if(mItemInfo.cellY==0){
					ryLayoutParams.setMargins(1, 2, 1, 1);
				}else if(mItemInfo.cellY==2){
					ryLayoutParams.setMargins(1, 1, 1, 2);
				}else{
					ryLayoutParams.setMargins(1, 1, 1, 1);
				}
				mRelativeLayoutHead.setLayoutParams(ryLayoutParams);
			}else if(mItemInfo.spanX==1 && mItemInfo.spanY==1){
				RelativeLayout.LayoutParams ryLayoutParams = (RelativeLayout.LayoutParams) mRelativeLayoutHead
						.getLayoutParams();
				ryLayoutParams.setMargins(0, 0, 0, 2);
				mRelativeLayoutHead.setLayoutParams(ryLayoutParams);
			}else{
				RelativeLayout.LayoutParams ryLayoutParams = (RelativeLayout.LayoutParams) mRelativeLayoutHead
						.getLayoutParams();
				ryLayoutParams.setMargins(0, 0, 0, 0);
				mRelativeLayoutHead.setLayoutParams(ryLayoutParams);
			}
        }else{
        	RelativeLayout.LayoutParams ryLayoutParams = (RelativeLayout.LayoutParams) mRelativeLayoutHead
					.getLayoutParams();
			ryLayoutParams.setMargins(0, 0, 0, 0);
			mRelativeLayoutHead.setLayoutParams(ryLayoutParams);
        	updateBubbleView(textViewHeight,LayoutParams.MATCH_PARENT);
        }
        //hejianfeng add end
        if(itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_CONTACTS){
        	setContactsIcon(itemInfo,density,textViewHeight);
        }else if(itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT){
            setShortcutIcon(itemInfo,density,textViewHeight);
        }else if(itemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET){
           setAppWidgetIcon(itemInfo,density,textViewHeight);
        }
    }
    
    /**让背景颜色由一个颜色渐变成另一个颜色  由于某个问题使得渐变暂时没使用*/
	private void gradualChangeBackgroundResource(int res) {
		mRelativeLayoutHead.setBackgroundColor(Color.TRANSPARENT);
		setBackgroundResource(res);
	}
	
	//START: added by Yar @20170824
    private ContactsObserver mObserver;
    private boolean isRegisterContacts = false;
	
	class ContactsObserver extends ContentObserver {
		public ContactsObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// 当联系人表发生变化时进行相应的操作
			ItemContacts ic = (ItemContacts)mItemInfo;
			ContentResolver cr = mContext.getContentResolver();
			Log.i("Yar", " onChange() mItemInfo.mRawId = " + ic.mRawId);
			long id = ic.id;
			String photoKey;
			Uri uri = null;
			String cellDefName = null;
			String phoneNumber = "";
			
			final Map<String, String> info = ic.getQuickInfo(cr, ic.mRawId);
			//final String number = info.get(Phone.CONTENT_ITEM_TYPE);
		    if (info != null) {
			    photoKey = info.get(Photo.CONTENT_ITEM_TYPE);
	            if (TextUtils.isEmpty(photoKey)) {
	        	    uri = ic.parsePhotoUri(cr, ic.mRawId);
	            } else {
	        	    cellDefName = photoKey;
	            }
	            phoneNumber = info.get(Phone.CONTENT_ITEM_TYPE);
	        }
		    
			String contactName = ic.getDisplayName(cr, ic.mRawId);
			String headName = null;
			int raw_id = ic.mRawId;
			
			mLauncher.updateContactsItem(uri, id, phoneNumber, contactName, headName, raw_id, cellDefName);
			
			if (isRegisterContacts && (phoneNumber == null || "".equals(phoneNumber)) && (contactName == null || "".equals(contactName))) {
				mContext.getContentResolver().unregisterContentObserver(mObserver);
	        	isRegisterContacts = false;
	        	android.util.Log.i("Yar", "1. onChanged unregister !!! ");
			} else if (isRegisterContacts && (raw_id == -1 || raw_id == 0)) {
				mContext.getContentResolver().unregisterContentObserver(mObserver);
	        	isRegisterContacts = false;
	        	android.util.Log.i("Yar", "2. onChanged unregister !!! raw_id = " + raw_id);
			}
		}
	}
    //END: added by Yar @20170824
    
    /**传入一个文字大小 单位可以是dpi dp 等
     * 返回字体的像素高度
     * */
    public int getFontHeight(int textSize){
        mTextPaint.setTextSize(textSize);
        FontMetrics fm = mTextPaint.getFontMetrics();
        int fontHeight = (int) (Math.ceil(fm.descent - fm.ascent) + 2);
        return fontHeight;
    }
    
    @Override
    protected void onAttachedToWindow() {
        //Log.i("HHJ", "view 添加的时候执行这个");
        super.onAttachedToWindow();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LauncherLog.v(TAG,"onDetachedFromWindow,jeff");
        unRegisterBoradcastReceiver();
        if (isRegisterContacts) {
        	mContext.getContentResolver().unregisterContentObserver(mObserver);
        	isRegisterContacts = false;
        }
        android.util.Log.i("Yar", " onDetachedFromWindow isRegisterContacts = " + isRegisterContacts);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
    }
}
