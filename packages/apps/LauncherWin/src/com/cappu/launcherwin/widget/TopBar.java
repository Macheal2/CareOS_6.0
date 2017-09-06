package com.cappu.launcherwin.widget;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.theme.ThemeManager;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TopBar extends ViewGroup implements OnClickListener {

    private static final String TAG = "TopBar";
    private static final int MESSAGE_BATTER = 1;
    private static final int EFFECT_BATTER = 6;
    public static final int LEFT_ID = R.id.topBar_Left_ImageButton;
    public static final int RIGHT_ID = R.id.topBar_Right_ImageButton;
    public static final int TITLE_ID = R.id.topBar_title;
    public static final int TOP_TITLE_ID = R.id.topBar_Top_Title;
    

    private ColorStateList mTitleColor;

    private Drawable mLeftDraw, mRightDraw;

    private String mTitleStr;
    private String mSubTitleStr;

    private int mTitleSize;
    private int mSubTitleSize;
    private int mLeftVisibility, mRightVisibility;
    private int mLineWidth, mLineHeight;
    private int mTopBarHeight;
    private int mFeatureWidth;
    protected int mBackGround;
    private int mClickCount;

    private long mFirstTime;

    protected boolean mTitleClickable;
    protected boolean isSecretCode;

    /**左右的分割线*/
    private View mLeftLine, mRightLine;
    private ImageButton mLeftImage, mRightImage;
    protected KookTextView mTitle;
    private KookTextView mSubTitle;
    protected LinearLayout mTitleContent;

    private onTopBarListener mTopBarListener;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        /*下面这个说明 是从 R.styleable.CareStyle 主题样式里面获取  R.attr.topbarStyle 这个样式*/
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CareStyle, R.attr.topbarStyle, 0);
        
        
/*        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyView);
        context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)*/

        mLeftDraw = a.getDrawable(R.styleable.CareStyle_leftImage);
        mLeftVisibility = a.getInt(R.styleable.CareStyle_leftVisibility, View.VISIBLE);

        mRightDraw = a.getDrawable(R.styleable.CareStyle_rightImage);
        mRightVisibility = a.getInt(R.styleable.CareStyle_rightVisibility, View.VISIBLE);

        mTitleStr = a.getString(R.styleable.CareStyle_titleText);
        mTitleSize = a.getDimensionPixelSize(R.styleable.CareStyle_titleTextSize, getDefaultTextSize(context));
        mTitleColor = a.getColorStateList(R.styleable.CareStyle_titleColor);
        mTitleClickable = a.getBoolean(R.styleable.CareStyle_titleClickable, false);

        mSubTitleStr = a.getString(R.styleable.CareStyle_subText);
        mSubTitleSize = a.getDimensionPixelSize(R.styleable.CareStyle_subTextSize, getDefaultSubTitleSize(context));

        mBackGround = a.getResourceId(R.styleable.CareStyle_titleBackground, R.drawable.shape_blue);

        isSecretCode = a.getBoolean(R.styleable.CareStyle_secretable, false);

        if (isSecretCode) {
            mTitleClickable = true;
        }
        
        if (mLeftDraw == null) {
            mLeftDraw = context.getResources().getDrawable(
                    R.drawable.icon_back);
        }

        if (mRightDraw == null) {
            mRightDraw = context.getResources().getDrawable(
                    R.drawable.care_ic_option);
        }

        if (mTitleColor == null) {
            mTitleColor = ColorStateList.valueOf(Color.WHITE);
        }

        a.recycle();
        
        Log.i("HHJ", "TopBar init mLeftDraw:"+(mLeftDraw==null)+"   mRightDraw:"+(mRightDraw==null)+"   ThemeManager.getInstance():"+ThemeManager.getInstance().getThemeId());

        mTopBarHeight = getResources().getDimensionPixelOffset(R.dimen.topbar_height);

        // just set left and right button width = height
        mFeatureWidth = mTopBarHeight;
        Log.i("HHJ", "TopBar init end:");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i("HHJ", "TopBar jeff onFinishInflate:");
        getTitle();
        getSubTitle();
        getLeftButton();
        getRightButton();
        setBackgroundResource(mBackGround);
        Log.i("HHJ", "TopBar onFinishInflate end");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int width = r - l;
        final int height = b - t;
        final int lineTop = (height - mLineHeight) / 2;
        int left = 0;
        int right = width;

        if (mLeftVisibility == View.VISIBLE) {
            mLeftImage.layout(0, 0, mFeatureWidth, mTopBarHeight);
            left = mTopBarHeight;
            
            //Log.i("HHJ", "mLeftLine  lineTop:"+lineTop+"    mLineHeight:"+mLineHeight);
            mLeftLine.layout(left, lineTop, left + mLineWidth, lineTop + mLineHeight);
        } else if (mLeftVisibility == View.INVISIBLE) {
            left = mTopBarHeight;
        }

        if (mRightVisibility == View.VISIBLE) {
            mRightImage.layout(width - mFeatureWidth, 0, width, mTopBarHeight);
            right = width - mTopBarHeight;
            mRightLine.layout(right - mLineWidth, lineTop, right, lineTop + mLineHeight);
        } else if (mRightVisibility == View.INVISIBLE) {
            right = width - mTopBarHeight;
        }

        mTitleContent.layout(left, 0, right, mTopBarHeight);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int titleW = getMeasuredWidth();

        if (mLeftVisibility != View.GONE) {
            mLeftLine.measure(measureSpec, measureSpec);
            mLineWidth = mLeftLine.getMeasuredWidth();
            mLineHeight = mLeftLine.getMeasuredHeight();
            titleW = titleW - mLineWidth - mFeatureWidth;
        }

        if (mRightVisibility != View.GONE) {
            mRightLine.measure(measureSpec, measureSpec);
            mLineWidth = mRightLine.getMeasuredWidth();
            mLineHeight = mRightLine.getMeasuredHeight();
            titleW = titleW - mLineWidth - mFeatureWidth;
        }

        mTitleContent.measure(MeasureSpec.makeMeasureSpec(titleW, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mTopBarHeight, MeasureSpec.EXACTLY));

        setMeasuredDimension(widthMeasureSpec, mTopBarHeight);
    }

    public LinearLayout getTitleContent() {
        if (mTitleContent == null) {
            mTitleContent = new LinearLayout(getContext());
            mTitleContent.setId(TITLE_ID);
            mTitleContent.setGravity(Gravity.CENTER);
            mTitleContent.setBackgroundResource(mBackGround);
            mTitleContent.setOrientation(LinearLayout.VERTICAL);
            mTitleContent.setEnabled(mTitleClickable);
            mTitleContent.setClickable(mTitleClickable);
            mTitleContent.setOnClickListener(this);
            addView(mTitleContent);
        }
        return mTitleContent;
    }

    public TextView getTitle() {
        // Must be have parent
        getTitleContent();
        if (mTitle == null) {
            mTitle = new KookTextView(getContext());
            mTitle.setText(mTitleStr);
            if(mTitleColor !=null ){
                mTitle.setTextColor(mTitleColor);
            }
            
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
            mTitle.setIncludeFontPadding(false);
            mTitle.setGravity(Gravity.CENTER);
            mTitle.setId(TOP_TITLE_ID);
            mTitleContent.addView(mTitle);
        }
        return mTitle;
    }

    public TextView getSubTitle() {
        // Must be have parent
        getTitleContent();

        if (TextUtils.isEmpty(mSubTitleStr)) {
            return null;
        }

        if (mSubTitle == null) {
            mSubTitle = new KookTextView(getContext());
            mSubTitle.setText(mSubTitleStr);
            if(mTitleColor !=null ){
                mSubTitle.setTextColor(mTitleColor);
            }
            mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSubTitleSize);
            mSubTitle.setIncludeFontPadding(false);
            mSubTitle.setGravity(Gravity.CENTER);
            mTitleContent.addView(mSubTitle);
        }
        return mSubTitle;
	}

	public void setLeftDraw(Drawable drawable) {
		if (mLeftImage != null) {
			mLeftImage.setImageDrawable(drawable);
		}
	}

	public void setRightDraw(Drawable drawable) {
		if (mRightImage != null) {
			mRightImage.setImageDrawable(drawable);
		}
	}

    public ImageButton getLeftButton() {

        if (mLeftImage == null && mLeftVisibility != View.GONE) {
            mLeftImage = new ImageButton(getContext());
            mLeftImage.setImageDrawable(mLeftDraw);
            mLeftImage.setBackgroundResource(mBackGround);
            mLeftImage.setOnClickListener(this);
            mLeftImage.setId(LEFT_ID);
            addView(mLeftImage);
            mLeftLine = new View(getContext());
            mLeftLine.setBackgroundResource(R.drawable.care_topbar_line);
            addView(mLeftLine);
        }
        return mLeftImage;
    }

    public ImageButton getRightButton() {

        if (mRightImage == null && mRightVisibility != View.GONE) {
            mRightImage = new ImageButton(getContext()){
                @Override
                public void setVisibility(int visibility) {
                    super.setVisibility(visibility);
                    if(visibility == View.VISIBLE){
                        mRightLine.setVisibility(visibility);
                    }else if(visibility == View.INVISIBLE){
                        mRightLine.setVisibility(visibility);
                    }else if(visibility == View.GONE){
                        mRightLine.setVisibility(visibility);
                    }
                }
            };
            mRightImage.setImageDrawable(mRightDraw);
            mRightImage.setBackgroundResource(mBackGround);
            mRightImage.setOnClickListener(this);
            mRightImage.setId(RIGHT_ID);
            addView(mRightImage);
            mRightLine = new View(getContext());
            mRightLine.setBackgroundResource(R.drawable.care_topbar_line);
            addView(mRightLine);
        }

        if(mRightImage != null)
        {
            mRightImage.setVisibility(View.GONE);
        }

        return mRightImage;
    }

    public void setText(String title) {
        mTitle.setText(title);
    }

    public void setText(int resId) {
        mTitle.setText(resId);
    }

    public void setSubText(String title) {
        // if title is null just return
        if (TextUtils.isEmpty(title)) {
            return;
        }
        mSubTitleStr = title;
        getSubTitle();
        mSubTitle.setText(mSubTitleStr);
    }

    public void setSubTitleVisible(boolean visible){
		if(mSubTitle != null){
			mSubTitle.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}
    public void setSubText(int resId) {
        mSubTitleStr = getContext().getString(resId);
        getSubTitle();
        mSubTitle.setText(mSubTitleStr);
    }
    
    public void setLeftVisibilty(int visibility) {
        if (mLeftImage != null) {
            mLeftImage.setVisibility(visibility);
            mLeftLine.setVisibility(visibility);
        }
    }
    
    public void setRightVisibilty(int visibility) {
        if (mRightImage != null) {
            mRightImage.setVisibility(visibility);
            mRightLine.setVisibility(visibility);
        }
    }
    

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case LEFT_ID:
            if (mTopBarListener != null) {
                mTopBarListener.onLeftClick(v);
            }
            break;

        case RIGHT_ID:
            if (mTopBarListener != null) {
                mTopBarListener.onRightClick(v);
            }
            break;
        case TITLE_ID:

            if (isSecretCode) {
                long second = System.currentTimeMillis();
                if (second - mFirstTime < 400) {
                    ++mClickCount;
                } else {
                    mClickCount = 1;
                }
                if (mClickCount == EFFECT_BATTER) {
                    /*Intent intent = new Intent(getContext(), Test.class);
                    getContext().startActivity(intent);*/
                } else {
                    mHandler.removeMessages(MESSAGE_BATTER);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_BATTER, 400);
                }
                mFirstTime = second;
            } else {
                if (mTopBarListener != null) {
                    mTopBarListener.onTitleClick(v);
                }
            }
            break;
        }
    }

    public void setOnTopBarListener(onTopBarListener listener) {
        mTopBarListener = listener;
    }

    public int getDefaultTextSize(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.normal_topbar);
    }

    public int getDefaultSubTitleSize(Context context) {
        return context.getResources().getDimensionPixelOffset(R.dimen.normal_topbar_sub);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MESSAGE_BATTER:
                if (mClickCount == 1) {
                    Log.i(TAG, "sec click");
                    if (mTopBarListener != null) {
                        mTopBarListener.onTitleClick(mTitleContent);
                    }
                }
                break;

            default:
                break;
            }
        }

    };

    public interface onTopBarListener {
        void onLeftClick(View v);

        void onRightClick(View v);

        void onTitleClick(View v);
    }

}
