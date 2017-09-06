package com.cappu.launcherwin.netinfo.widget;

import java.util.ArrayList;
import java.util.List;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.SmoothPagedView;

public class CappuHorizontalScrollView extends LinearLayout {

    private String TAG = "CappuHorizontalScrollView";
    private Context mContext;
    private CappuHSView mCappuHSView;

    
    public CappuHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    
    public int getCountView() {
        return getChildCount();
    }
    
    public void setData(List<NetDateDao> netDateDaoList) {
        if(mCappuHSView == null){
            mCappuHSView = new CappuHSView(mContext);
            mCappuHSView.setData(netDateDaoList);
            addView(mCappuHSView);
        }else{
            mCappuHSView.setData(netDateDaoList);
        }
    }
    
    
    @Override
    protected void onDetachedFromWindow() {
        Log.i(TAG, "view 销毁的时候执行这个");
        super.onDetachedFromWindow();
    }
    
    public void deleteView(){
        if(mCappuHSView != null){
            mCappuHSView.deleteView();
        }
        
        removeView(mCappuHSView);
        mCappuHSView = null;
        removeAllViewsInLayout();
    }
    
}
