package com.cappu.launcherwin.netinfo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.cappu.launcherwin.widget.TopBar;

public class TabTopBar extends TopBar{
    
    protected TabWidget mTabWidget;

    public TabTopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getTitle();
        getTitleTabWidget();
        getLeftButton();
        getRightButton();
        setBackgroundResource(mBackGround);
    }
    
    public LinearLayout getTitleContent() {
        if (mTitleContent == null) {
            mTitleContent = new LinearLayout(getContext());
            mTitleContent.setId(TITLE_ID);
            mTitleContent.setGravity(Gravity.CENTER);
            mTitleContent.setBackgroundResource(mBackGround);
            mTitleContent.setOrientation(LinearLayout.VERTICAL);
            addView(mTitleContent);
        }
        return mTitleContent;
    }
    
    
    public TabWidget getTitleTabWidget() {
        if(mTitleContent == null){
            getTitleContent();
        }
        if (mTabWidget == null) {
            mTabWidget = new TabWidget(getContext());
            mTabWidget.setId(android.R.id.tabs);
            mTabWidget.setVisibility(View.GONE);
            mTitleContent.addView(mTabWidget);
            
        }
        return null;
    }
    
    public void setTabWidget(boolean isTab){
        if(isTab){
            mTabWidget.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.GONE);
        }else{
            mTabWidget.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
        }
    }
}
