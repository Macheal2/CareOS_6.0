/** 
 * Copyright (C) 2014 The Magcomm Android Source Project
 *
 * Licensed under the Magcomm License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.magcomm.cn
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2014年10月11日 下午4:59:16 
 * @author: y.haiyang@qq.com
 * @company: Magcomm Co.,Ltd. 
 */

package com.cappu.cleaner.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cappu.cleaner.R;
/**
 * @hide
 */
public class TopBar extends ViewGroup implements OnClickListener{

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
    private int mBackGround;
    private int mClickCount;

    private long mFirstTime;

    private boolean mTitleClickable;
    private boolean isSecretCode;

    private View mLeftLine, mRightLine;
    private ImageButton mLeftImage, mRightImage;
    private MagcommTextView mTitle;
    private MagcommTextView mSubTitle;
    private LinearLayout mTitleContent;

    private onTopBarListener mTopBarListener;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopBar,0, 0);

        mLeftDraw = a.getDrawable(R.styleable.TopBar_leftImage);
        mLeftVisibility = a.getInt(R.styleable.TopBar_leftVisibility, View.VISIBLE);

        mRightDraw = a.getDrawable(R.styleable.TopBar_rightImage);
        mRightVisibility = a.getInt(R.styleable.TopBar_rightVisibility, View.VISIBLE);

        mTitleStr = a.getString(R.styleable.TopBar_titleText);
        mTitleSize = a.getDimensionPixelSize(R.styleable.TopBar_titleTextSize, getDefaultTextSize(context));
        mTitleColor = a.getColorStateList(R.styleable.TopBar_titleColor);
        mTitleClickable = a.getBoolean(R.styleable.TopBar_titleClickable, false);

        mSubTitleStr = a.getString(R.styleable.TopBar_subText);
        mSubTitleSize = a.getDimensionPixelSize(R.styleable.TopBar_subTextSize, getDefaultSubTitleSize(context));

        mBackGround = a.getResourceId(R.styleable.TopBar_background, R.drawable.care_topbar_bg);

        isSecretCode = a.getBoolean(R.styleable.TopBar_secretable, false);

        if(isSecretCode){
            mTitleClickable = true;
        }

        if(mLeftDraw == null){
            mLeftDraw = context.getResources().getDrawable(R.drawable.care_ic_back);
        }

        if(mRightDraw == null){
            mRightDraw = context.getResources().getDrawable(R.drawable.care_ic_option);
        }

        if(mTitleColor == null){
            mTitleColor =  ColorStateList.valueOf(Color.WHITE);
        }

        a.recycle();

        mTopBarHeight = getResources().getDimensionPixelOffset(R.dimen.care_topbar_height);//CareConfigure.getTopBarHeight(context);

        // just set left and right button width = height
        mFeatureWidth = mTopBarHeight;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getTitle();
        getSubTitle();
        getLeftButton();
        getRightButton();
        setBackgroundResource(mBackGround);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int width = r - l;
        final int height = b -t;
        final int lineTop = (height - mLineHeight)/2;
        int left = 0;
        int right = width;

        if(mLeftVisibility == View.VISIBLE){
            mLeftImage.layout(0, 0, mFeatureWidth, mTopBarHeight);
            left = mTopBarHeight;
            mLeftLine.layout(left, lineTop, left + mLineWidth , lineTop + mLineHeight);
        }else if(mLeftVisibility == View.INVISIBLE){
            left = mTopBarHeight;
        }


        if(mRightVisibility == View.VISIBLE){
            mRightImage.layout(width - mFeatureWidth, 0, width, mTopBarHeight);
            right = width - mTopBarHeight;
            mRightLine.layout(right - mLineWidth, lineTop, right , lineTop + mLineHeight);
        }else if(mRightVisibility == View.INVISIBLE){
            right = width - mTopBarHeight;
        }

        mTitleContent.layout(left, 0, right, mTopBarHeight);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int titleW = getMeasuredWidth();

        if(mLeftVisibility != View.GONE){
            mLeftLine.measure(measureSpec, measureSpec);
            mLineWidth = mLeftLine.getMeasuredWidth();
            mLineHeight = mLeftLine.getMeasuredHeight();
            titleW = titleW - mLineWidth - mFeatureWidth;
        }

        if(mRightVisibility != View.GONE){
            mRightLine.measure(measureSpec, measureSpec);
            mLineWidth = mRightLine.getMeasuredWidth();
            mLineHeight = mRightLine.getMeasuredHeight();
            titleW = titleW - mLineWidth - mFeatureWidth;
        }

        mTitleContent.measure(
                MeasureSpec.makeMeasureSpec(titleW, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mTopBarHeight, MeasureSpec.EXACTLY));

        setMeasuredDimension(widthMeasureSpec, mTopBarHeight);
    }

    public LinearLayout getTitleContent(){
        if(mTitleContent == null){
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

    public TextView getTitle(){
        // Must be have parent
        getTitleContent();

        if(mTitle == null){
            mTitle = new MagcommTextView(getContext());
            mTitle.setText(mTitleStr);
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX , mTitleSize);
            mTitle.setIncludeFontPadding(false);
            mTitle.setGravity(Gravity.CENTER);

            if(mTitleColor != null){
                mTitle.setTextColor(mTitleColor);
            }
            mTitle.setId(TOP_TITLE_ID);
            mTitleContent.addView(mTitle);
        }
        return mTitle;
    }

    public TextView getSubTitle(){
        // Must be have parent
        getTitleContent();

        if(TextUtils.isEmpty(mSubTitleStr)){
            return null;
        }

        if(mSubTitle == null){
            mSubTitle = new MagcommTextView(getContext());
            mSubTitle.setText(mSubTitleStr);
            mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX , mSubTitleSize);
            mSubTitle.setIncludeFontPadding(false);
            mSubTitle.setGravity(Gravity.CENTER);
            if(mTitleColor != null){
                mSubTitle.setTextColor(mTitleColor);
            }
            mTitleContent.addView(mSubTitle);
        }
        return mSubTitle;
    }

    public ImageButton getLeftButton(){

        if(mLeftImage == null && mLeftVisibility != View.GONE){
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

    public void setLeftVisibilty(int visibility){
        if(mLeftImage != null){
            mLeftImage.setVisibility(visibility);
            mLeftLine.setVisibility(visibility);
        }
    }
    
    
    public ImageButton getRightButton(){

        if(mRightImage ==null && mRightVisibility != View.GONE){
            mRightImage = new ImageButton(getContext());
            mRightImage.setImageDrawable(mRightDraw);
            mRightImage.setBackgroundResource(mBackGround);
            mRightImage.setOnClickListener(this);
            mRightImage.setId(RIGHT_ID);
            addView(mRightImage);
            mRightLine = new View(getContext());
            mRightLine.setBackgroundResource(R.drawable.care_topbar_line);
            addView(mRightLine);
        }
        return mRightImage;
    }

    //added by yzs begin
    public void setSubTitleVisibility(int visible){
        if(mSubTitle != null){
            mSubTitle.setVisibility(visible);
        }
    }

    public void setTitleClickable(boolean clickable){
        if(mTitleContent != null){
            mTitleClickable = clickable;
            mTitleContent.setEnabled(clickable);
            mTitleContent.setClickable(clickable);
        }
    }

    public void setLeftDraw(Drawable drawable){
        if(mLeftImage != null){
            mLeftImage.setImageDrawable(drawable);
        }
    }

    public void setRightDraw(Drawable drawable){
        if(mRightImage != null){
            mRightImage.setImageDrawable(drawable);
        }
    }
    //added by yzs end
    
    public void setRightVisibilty(int visibility){
        if(mRightImage != null){
            mRightImage.setVisibility(visibility);
            mRightLine.setVisibility(visibility);
        }
    }
    
    public void setText(String title){
        mTitle.setText(title);
    }

    public void setText(int resId){
        mTitle.setText(resId);
    }

    public void setSubText(String title){
        // if title is null just return
        if(TextUtils.isEmpty(title)){
            return;
        }
        mSubTitleStr = title;
        getSubTitle();
        mSubTitle.setText(mSubTitleStr);
    }

    public void setSubText(int resId){
        mSubTitleStr = getContext().getString(resId);
        getSubTitle();
        mSubTitle.setText(mSubTitleStr);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case LEFT_ID:
                if(mTopBarListener != null){
                    mTopBarListener.onLeftClick(v);
                }
                break;

            case RIGHT_ID:
                if(mTopBarListener != null){
                    mTopBarListener.onRightClick(v);
                }
                break;
            case TITLE_ID:

                if(isSecretCode){
                    long second = System.currentTimeMillis();
                    if(second - mFirstTime < 400){
                        ++mClickCount;
                    }else{
                        mClickCount = 1;
                    }
                    if(mClickCount == EFFECT_BATTER){
                        //TODO
                    }else{
                        mHandler.removeMessages(MESSAGE_BATTER);
                        mHandler.sendEmptyMessageDelayed(MESSAGE_BATTER,400);
                    }
                    mFirstTime = second;
                }else{
                    if(mTopBarListener != null){
                        mTopBarListener.onTitleClick(v);
                    }
                }
                break;
        }
    }

    public void setOnTopBarListener(onTopBarListener listener){
        mTopBarListener = listener;
    }


    public int getDefaultTextSize(Context context){
        return context.getResources().getDimensionPixelOffset(R.dimen.care_normal_topbar);
    }

    public int getDefaultSubTitleSize(Context context){
        return context.getResources().getDimensionPixelOffset(R.dimen.care_normal_topbar_sub);
    }

    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MESSAGE_BATTER:
                if(mClickCount == 1){
                    if(mTopBarListener != null){
                        mTopBarListener.onTitleClick(mTitleContent);
                    }
                }
                break;

            default:
                break;
            }
        }

    };

    public interface onTopBarListener{
        void onLeftClick(View v);
        void onRightClick(View v);
        void onTitleClick(View v);
    }

}
