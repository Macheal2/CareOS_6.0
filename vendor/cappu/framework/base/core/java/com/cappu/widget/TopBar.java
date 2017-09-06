/** 
 * Copyright (C) 2014 The Cappu Android Source Project
 *
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.cappu.cn
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2014年10月11日 下午4:59:16 
 * @author: y.haiyang@qq.com
 * @company: Cappu Co.,Ltd. 
 */

package com.cappu.widget;

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

import com.cappu.internal.R;

import com.cappu.util.CareConfigure;
import com.cappu.widget.MarqueeTextView;

import com.cappu.theme.ThemeManager;

/**
 * @hide
 */
public class TopBar extends ViewGroup implements OnClickListener{

    public static final String TAG = "TopBar";
    private static final int MESSAGE_BATTER = 1;
    private static final int EFFECT_BATTER = 6;

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
    private MarqueeTextView mTitle;
    private MarqueeTextView mSubTitle;
    private LinearLayout mTitleContent;

    private onTopBarListener mTopBarListener;
    
    ThemeManager mThemeManager;

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopBar, R.attr.topbarStyle, 0);
        
        if(mThemeManager == null){
            ThemeManager.init(context);
            mThemeManager = ThemeManager.getInstance();
        }
        

        mLeftDraw = a.getDrawable(R.styleable.TopBar_leftImage);
        mLeftVisibility = a.getInt(R.styleable.TopBar_leftVisibility, View.VISIBLE);

        mRightDraw = a.getDrawable(R.styleable.TopBar_rightImage);
        mRightVisibility = a.getInt(R.styleable.TopBar_rightVisibility, View.VISIBLE);

        mTitleStr = a.getString(R.styleable.TopBar_text);
        mTitleSize = a.getDimensionPixelSize(R.styleable.TopBar_textSize, getDefaultTextSize(context));
        mTitleColor = a.getColorStateList(R.styleable.TopBar_textColor);
        mTitleClickable = a.getBoolean(R.styleable.TopBar_clickable, false);

        mSubTitleStr = a.getString(R.styleable.TopBar_subText);
        mSubTitleSize = a.getDimensionPixelSize(R.styleable.TopBar_subTextSize, getDefaultSubTitleSize(context));

        Log.i(TAG,"TAG themeType:"+mThemeManager.getThemeType(context) );
        if(mThemeManager.getThemeType(context) == ThemeManager.THEME_CLASSICAL){
            mBackGround = R.drawable.shape_green;//a.getResourceId(R.styleable.TopBar_background, R.drawable.shape_green);
        }else{
            mBackGround = a.getResourceId(R.styleable.TopBar_background, R.drawable.topbar_default_bg);
        }

        isSecretCode = a.getBoolean(R.styleable.TopBar_secretable, false);

        if(isSecretCode){
            mTitleClickable = true;
        }

        if(mLeftDraw == null){
            mLeftDraw = context.getResources().getDrawable(R.drawable.ic_back);
        }

        if(mRightDraw == null){
            mRightDraw = context.getResources().getDrawable(R.drawable.ic_option);
        }

        if(mTitleColor == null){
            mTitleColor =  ColorStateList.valueOf(Color.WHITE);
        }

        a.recycle();

        mTopBarHeight = CareConfigure.getTopBarHeight(context);

        // just set left and right button width = height
        mFeatureWidth = mTopBarHeight;
        Log.i(TAG,"mTitleSize = " + mTitleSize);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(mThemeManager == null){
            ThemeManager.init(getContext());
            mThemeManager = ThemeManager.getInstance();
        }
        
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
            mTitleContent.setId(R.id.topbar_title);
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
            mTitle = new MarqueeTextView(getContext());
            mTitle.setText(mTitleStr);
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX , mTitleSize);
            mTitle.setIncludeFontPadding(false);
            mTitle.setGravity(Gravity.CENTER);

            if(mTitleColor != null){
                mTitle.setTextColor(mTitleColor);
            }
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
            mSubTitle = new MarqueeTextView(getContext());
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
            mLeftImage.setId(R.id.topbar_left_button);
            addView(mLeftImage);
            mLeftLine = new View(getContext());
            mLeftLine.setBackgroundResource(R.drawable.topbar_line);
            addView(mLeftLine);
        }
        return mLeftImage;
    }

    public ImageButton getRightButton(){

        if(mRightImage ==null && mRightVisibility != View.GONE){
            mRightImage = new ImageButton(getContext());
            mRightImage.setImageDrawable(mRightDraw);
            mRightImage.setBackgroundResource(mBackGround);
            mRightImage.setOnClickListener(this);
            mRightImage.setId(R.id.topbar_right_button);
            addView(mRightImage);
            mRightLine = new View(getContext());
            mRightLine.setBackgroundResource(R.drawable.topbar_line);
            addView(mRightLine);
        }
        return mRightImage;
    }

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

    public void setLeftDrawable(Drawable drawable){
        if(mLeftImage != null){
            mLeftImage.setImageDrawable(drawable);
        }
    }

    public void setRightDrawable(Drawable drawable){
        if(mRightImage != null){
            mRightImage.setImageDrawable(drawable);
        }
    }

    public void setLeftVisibilty(int visibility){
        if(mLeftImage != null){
            mLeftImage.setVisibility(visibility);
            mLeftLine.setVisibility(visibility);
        }
    }

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
            case R.id.topbar_left_button:
                if(mTopBarListener != null){
                    mTopBarListener.onLeftClick(v);
                }
                break;

            case R.id.topbar_right_button:
                if(mTopBarListener != null){
                    mTopBarListener.onRightClick(v);
                }
                break;
            case R.id.topbar_title:

                if(isSecretCode){
                    long second = System.currentTimeMillis();
                    if(second - mFirstTime < 400){
                        ++mClickCount;
                    }else{
                        mClickCount = 1;
                    }
                    if(mClickCount == EFFECT_BATTER){
                        //TODO
                        Log.i(TAG, "Runing SecretCode!");
                    }else{
                        mHandler.removeMessages(MESSAGE_BATTER);
                        mHandler.sendEmptyMessageDelayed(MESSAGE_BATTER, 400);
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
        return context.getResources().getDimensionPixelOffset(R.dimen.topbar_default_text_size);
    }

    public int getDefaultSubTitleSize(Context context){
        return context.getResources().getDimensionPixelOffset(R.dimen.topbar_default_sub_text_size);
    }

    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_BATTER:
                    if(mClickCount == 1){
                        Log.i(TAG,"sec click");
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
