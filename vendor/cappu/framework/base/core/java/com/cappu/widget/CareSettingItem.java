package com.cappu.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cappu.internal.R;
import com.cappu.util.Constant;

import com.cappu.theme.ThemeManager;
//hejianfeng add start
import android.graphics.Color;
import android.util.Log;
//hejianfeng add end
/**
 * Created by lenovo on 15-12-3.
 */
public class CareSettingItem extends LinearLayout implements View.OnClickListener {
    private static final String TAG = CareSettingItem.class.getSimpleName();

    private Context mContext;

    private static final int DEFAULT_TYPE = Constant.TYPE_MULE;
    private static final int DEFAULT_STYLE = Constant.STYLE_MID;
    private final Drawable mSingleDrawable_Class = Constant.getBgDrawable(getContext(), R.drawable.list_corner_round_class);
    private final Drawable mTopDrawable_Class = Constant.getBgDrawable(getContext(), R.drawable.list_corner_round_top_class);
    private final Drawable mMidDrawable_Class = Constant.getBgDrawable(getContext(), R.drawable.list_corner_round_mid_class);
    private final Drawable mBottomDrawable_Class = Constant.getBgDrawable(getContext(), R.drawable.list_corner_round_bottom_class);

    private Drawable mSingleDrawable, mTopDrawable, mMidDrawable, mBottomDrawable;

    private final int mPadding = Constant.getPaddings(getContext());
    private final int mTitleSize = Constant.getDefaultTextSize(getContext());
    private final int mTipViewSize = Constant.getTipViewSize(getContext());
    private final int mIconViewSize = Constant.getIconViewSize(getContext());

    private Drawable mIcon, mMore;
    private String mItemTitle;
    private ColorStateList mTitleColor;

    private ImageView mIconView, mTipView;
    private TextView mContentTitle, mSubContent;
    private CheckBox mSwitch;
    private CheckBox mCheckBox;
    private Boolean isChecked;

    private int mItemType;
    private int mItemStyle;

    private ThemeManager mThemeManager;
    private int mItemHeight;

    private SettingItemListener mListener;
    //hejianfeng add start
    private int mSwitchHeight=64; 
    private View dividing_h;
    //hejianfeng add end

    public CareSettingItem(Context context) {
        this(context, null);
    }

    public CareSettingItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CareSettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = getContext();
        if (mThemeManager == null) {
            ThemeManager.init(context);
            mThemeManager = ThemeManager.getInstance();
        }

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CareSettingItem);
        mIcon = ta.getDrawable(R.styleable.CareSettingItem_item_icon);
        mItemTitle = ta.getString(R.styleable.CareSettingItem_item_title);
        mTitleColor = ta.getColorStateList(R.styleable.CareSettingItem_item_textcolor);
        isChecked = ta.getBoolean(R.styleable.CareSettingItem_item_checked, false);
        mItemType = ta.getInt(R.styleable.CareSettingItem_item_type, DEFAULT_TYPE);
        mItemStyle = ta.getInt(R.styleable.CareSettingItem_item_style, DEFAULT_STYLE);
        ta.recycle();

		mSingleDrawable = mSingleDrawable_Class;
		mTopDrawable = mTopDrawable_Class;
		mMidDrawable = mMidDrawable_Class;
		mBottomDrawable = mBottomDrawable_Class;

        mItemHeight = getResources().getDimensionPixelOffset(R.dimen.item_height_check);

        if (mTitleColor == null) {
            mTitleColor = Constant.getDefaultTextColor(mContext);
        }

        if (mItemTitle == null) {
            mItemTitle = Constant.getDefaultTitle(mContext);
        }
        mMore = getResources().getDrawable(R.mipmap.setting_switch_icon_tip);
        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getIconView();
        getContentTitle();
        getTipView();
        getSubContent();
        getSwitch();
        getCheckBox();
        //hejianfeng add start
        dividing_h=new View(mContext);
        dividing_h.setBackgroundResource(R.drawable.care_line_v);
        addView(dividing_h);
        //hejianfeng add end
        setTag(this);
        setItemStyle(mItemStyle);
    }
    public void setItemStyle(int itemStyle){
    	switch (itemStyle) {
        case Constant.STYLE_SINGLE:
            setBackground(mSingleDrawable);
            dividing_h.setVisibility(View.GONE);
            break;
        case Constant.STYLE_TOP:
            setBackground(mTopDrawable);
            break;
        case Constant.STYLE_MID:
            setBackground(mMidDrawable);
            break;
        case Constant.STYLE_BOTTOM:
            setBackground(mBottomDrawable);
            dividing_h.setVisibility(View.GONE);
            break;
        default:
            setBackground(mMidDrawable);
    }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int width = r - l;
        int height = b - t;
        ViewGroup.LayoutParams icon = mIconView.getLayoutParams();
        icon.width = icon.height = mIconViewSize;
        mIconView.setLayoutParams(icon);
        if (mIcon != null) {
            mIconView.setImageDrawable(mIcon);
            mIconView.layout(mPadding, (height - mIconView.getHeight()) / 2, mIconView.getWidth() + mPadding,
                    (height + mIconView.getHeight()) / 2);

            mContentTitle.layout(mIconView.getWidth() + 2 * mPadding, (height - mContentTitle.getHeight()) / 2, mIconView.getWidth() +
                    2 * mPadding + mContentTitle.getWidth() + mPadding, (height + mContentTitle.getHeight()) / 2);
        } else {
            mContentTitle.layout(mPadding, (height - mContentTitle.getHeight()) / 2,
                    mContentTitle.getWidth() + mPadding, (height + mContentTitle.getHeight()) / 2);
        }

        ViewGroup.LayoutParams lp = mTipView.getLayoutParams();
        lp.width = lp.height = mTipViewSize;
        mTipView.setLayoutParams(lp);

        mTipView.layout(width - mTipView.getWidth(), 0, width, mItemHeight);
        mSubContent.layout(width - mTipView.getWidth() - mSubContent.getWidth(), (height - mSubContent.getHeight()) / 2,
                width - mTipView.getWidth(), (height + mSubContent.getHeight()) / 2);


        mCheckBox.layout(width - mCheckBox.getWidth() - mPadding, (height - mCheckBox.getHeight()) / 2,
                width - mPadding, (height + mCheckBox.getHeight()) / 2);

        mSwitch.layout(width - 2*mSwitchHeight - mPadding, (height - mSwitchHeight) / 2,
                width - mPadding, (height + mSwitchHeight) / 2);
        //hejianfeng add
        dividing_h.layout(0,height-2,width,height);

        switch (mItemType) {
            case Constant.TYPE_CBOX:
                mTipView.setVisibility(View.GONE);
                mSubContent.setVisibility(View.GONE);
                mSwitch.setVisibility(View.GONE);
                break;
            case Constant.TYPE_MORE:
                mSubContent.setVisibility(View.GONE);
                mCheckBox.setVisibility(View.GONE);
                mSwitch.setVisibility(View.GONE);
                break;
            case Constant.TYPE_MULE:
                mCheckBox.setVisibility(View.GONE);
                mSwitch.setVisibility(View.GONE);
                break;
            case Constant.TYPE_SWIT:
                mTipView.setVisibility(View.GONE);
                mSubContent.setVisibility(View.GONE);
                mCheckBox.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        int height = MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY);

        setClickable(true);
        setEnabled(true);
        setMeasuredDimension(width, height);
    }

    private ImageView getIconView() {
        if (mIconView == null) {
            mIconView = new ImageView(mContext);
        }
        addView(mIconView);
        return mIconView;
    }

    private TextView getContentTitle() {
        if (mContentTitle == null) {
            mContentTitle = new TextView(mContext);
        }
        mContentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
        mContentTitle.setIncludeFontPadding(true);
        mContentTitle.setGravity(Gravity.CENTER);
        mContentTitle.setText(mItemTitle);
        if (mTitleColor != null) {
            mContentTitle.setTextColor(mTitleColor);
        }
        addView(mContentTitle);
        return mContentTitle;
    }

    private ImageView getTipView() {
        if (mTipView == null) {
            mTipView = new ImageView(mContext);
        }

        mTipView.setImageDrawable(mMore);
        addView(mTipView);
        return mTipView;
    }

    private TextView getSubContent() {
        if (mSubContent == null) {
            mSubContent = new TextView(mContext);
        }
        mSubContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTitleSize);
        mSubContent.setIncludeFontPadding(true);
        mSubContent.setGravity(Gravity.CENTER);
        mSubContent.setText(mItemTitle);
        if (mTitleColor != null) {
            mSubContent.setTextColor(mTitleColor);
        }
        addView(mSubContent);
        return mSubContent;
    }

    private CheckBox getSwitch() {
        if (mSwitch == null) {
            mSwitch = new CheckBox(mContext);
        }
        mSwitch.setClickable(false);
        mSwitch.setChecked(isChecked);
        mSwitch.setBackgroundResource(R.drawable.list_switch_style);
        mSwitch.setButtonDrawable(0);
        addView(mSwitch);
        return mSwitch;
    }

    private CheckBox getCheckBox() {
        if (mCheckBox == null) {
            mCheckBox = new CheckBox(mContext);
        }
        mCheckBox.setClickable(false);
        mCheckBox.setChecked(isChecked);
        mCheckBox.setButtonDrawable(R.drawable.checkbox_bg);
        addView(mCheckBox);
        return mCheckBox;
    }

    @Override
    public void onClick(View v) {
    	Log.v(TAG,"onClick,jeff mItemType="+mItemType);
        switch (mItemType) {
            case Constant.TYPE_MULE:
                break;
            case Constant.TYPE_SWIT:
                if (mSwitch != null) {
                    mSwitch.setChecked(!mSwitch.isChecked());
                }
                break;
            case Constant.TYPE_CBOX:
                if (mCheckBox != null)
                    mCheckBox.setChecked(!mCheckBox.isChecked());
                break;
        }
        if (mListener != null)
            mListener.onItemClick((CareSettingItem) (v.getTag()));
    }


    public void setSubContent(String subContent) {
        if (mSubContent != null) {
            mSubContent.setText(subContent);
        }
    }
    public void setSubContent(int resid) {
        if (mSubContent != null) {
            mSubContent.setText(resid);
        }
    }
    public boolean getChecked() {
        if (mCheckBox != null) {
            return mCheckBox.isChecked();
        }
        return false;
    }

    public boolean getSelected() {
        if (mSwitch != null) {
            return mSwitch.isChecked();
        }
        return false;
    }
    //hejianfeng add start
    public void setSelected(boolean isCheck){
    	if (mSwitch != null) {
    		mSwitch.setChecked(isCheck);
        }
    }
    public void setChecked(boolean isCheck){
    	if (mCheckBox != null) {
            mCheckBox.setChecked(isCheck);
        }
    }
    //hejianfeng add end
    public void setItemListener(SettingItemListener listener) {
        mListener = listener;
    }

    public interface SettingItemListener {
        void onItemClick(CareSettingItem item);
    }
}
