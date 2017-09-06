package com.cappu.launcherwin.netinfo.widget;

import java.util.ArrayList;
import java.util.List;

import com.cappu.launcherwin.Launcher;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.Launcher.LauncherType;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.widget.Indicator;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.RoundImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Gallery.LayoutParams;
import android.view.View.OnClickListener;

public class CappuHSView extends LinearLayout implements OnPageChangeListener{
    public String TAG = "CappuHSView";
    Context mContext;
    
    boolean isStop = false;
    
    public RelativeLayout mRelativeLayoutLeft;
    public BaseViewPager mBaseViewPagerLeft;
    public Indicator mIndicatorLeft;
    
    
    public RelativeLayout mRelativeLayoutRight;
    public BaseViewPager mBaseViewPagerRight;
    public Indicator mIndicatorRight;
    
    public boolean mIndicatorShow = false;
    
    public static final int ROUND_IMAGE_VIEW_LEFT = 0x7f010001;
    public static final int BASEVIEW_PAGER_LEFT = 0x7f010002;
    public static final int LINEARLAYOUT_VIEW_LEFT = 0x7f010003;
    
    public static final int ROUND_IMAGE_VIEW_RIGHT = 0x7f010004;
    public static final int BASEVIEW_PAGER_RIGHT = 0x7f010005;
    public static final int LINEARLAYOUT_VIEW_RIGHT = 0x7f010006;
    
    
    //private List<ImageView> imageViews = new ArrayList<ImageView>();
    private ImageView[] indicators;
    private FrameLayout viewPagerFragmentLayout;
    private ViewPagerAdapter adapter;
    private int time = 5000; // 默认轮播时间
    
    private int mCurrentCount = 0; // 当前轮播次数
    private int mLeftCurrentPosition;
    private int mRightCurrentPosition;
    
    /**滚动框是否滚动着*/
    private boolean isScrolling = true;
    /**是否轮播*/
    private boolean isWheel = true; // 是否轮播
    /**转动*/
    private int WHEEL = 100; // 转动
    /**等待*/
    private int WHEEL_WAIT = 101; // 等待
    private ImageCycleViewListener mImageCycleViewListener;
    private List<NetDateDao> mList;
    private ArrayList<NetDateDao> mListLeft;// = new ArrayList<NetDateDao>();
    private ArrayList<NetDateDao> mListRight;// = new ArrayList<NetDateDao>();
    
    ViewPagerAdapter mViewPagerAdapterLeft;
    ViewPagerAdapter mViewPagerAdapterRight;
    
    Handler mCycleHandler;
    
    Launcher mLauncher;
    
    public CappuHSView(Context context){
        super(context);
        mContext = context;
        if(context instanceof Launcher){
            mLauncher = (Launcher) context;
        }
        /*设置layout自身属性*/
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(lp);
        
        setOrientation(LinearLayout.HORIZONTAL);
        
        int margin = 0;
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            margin = DensityUtil.dip2px(getContext(), 0.5);
        }else{
            margin = DensityUtil.dip2px(mContext, mContext.getResources().getInteger(R.integer.cell_padding)+1);
        }
        
        View leftView = onCreartLeftView();
        LayoutParams lpChildLeft = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpChildLeft.weight = 1;
        lpChildLeft.rightMargin = margin;
        if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_COLORFUL){//modify by wangyang
            lpChildLeft.bottomMargin = DensityUtil.dip2px(mContext, 4);
        }
        addView(leftView, lpChildLeft);
        
        View rightView = onCreartRightView();
        LayoutParams lpChildRight = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lpChildRight.weight = 1;
        lpChildRight.leftMargin = margin;
        if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_COLORFUL){//modify by wangyang
            lpChildRight.bottomMargin = DensityUtil.dip2px(mContext, 4);
        }
        addView(rightView, lpChildRight);
        
        mCycleHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(isStop || mCycleHandler == null){
                    return;
                }
                if(mList != null){
                    Log.i(TAG, "1  mLauncher.getLauncherType():"+mLauncher.getLauncherType()+"    isStop:"+isStop);
                    if(mList.size() > 0){
                        if(mCurrentCount % 2 == 0){
                            int max = mBaseViewPagerLeft.getChildCount();
                            if(mLeftCurrentPosition == max){
                                mBaseViewPagerLeft.setCurrentItem(0, false);
                                mLeftCurrentPosition = 0;
                            }else{
                                mBaseViewPagerLeft.setCurrentItem(mLeftCurrentPosition, true);
                            }
                            mLeftCurrentPosition += 1;
                            
                            
                        }else{
                            int max = mBaseViewPagerRight.getChildCount();
                            if(mRightCurrentPosition == max){
                                mBaseViewPagerRight.setCurrentItem(0, false);
                                mRightCurrentPosition = 0;
                            }else{
                                mBaseViewPagerRight.setCurrentItem(mRightCurrentPosition, true);
                            }
                            
                            mRightCurrentPosition += 1;
                        }
                    }
                    mCurrentCount += 1;
                    if(mList != null && mCurrentCount >= mList.size()){
                        mCurrentCount = 0;
                    }
                    this.sendEmptyMessageDelayed(0, 5000);
                }else{
                    Log.i(TAG, "2 mLauncher.getLauncherType():"+mLauncher.getLauncherType()+"   (mListLeft == null):"+(mListLeft == null)+"   "+(mViewPagerAdapterLeft == null));
                    
                    
                    if(mListLeft == null){
                        mListLeft = new ArrayList<NetDateDao>();
                    }
                    if(mViewPagerAdapterLeft == null){
                        setDateLeft(mListLeft);
                    }
                    
                    
                    if(mListRight == null){
                        mListRight = new ArrayList<NetDateDao>();
                    }
                    if(mViewPagerAdapterRight == null){
                        setDateRight(mListRight);
                    }
                    
                    this.sendEmptyMessageDelayed(0, 10000);
                    return;
                }
            }
        };
        mCycleHandler.sendEmptyMessage(0);
        setTypeface();
    }
    //用于更换字体 added by wangyang 2016 06 29
    private Typeface mTypeface;
    private void setTypeface() {
        mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/minilishu.ttf");
    }

    public View onCreartLeftView(){
        mRelativeLayoutLeft = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mRelativeLayoutLeft.setLayoutParams(relativeLayoutParams);
        
        mBaseViewPagerLeft = new BaseViewPager(mContext);
        mRelativeLayoutLeft.addView(mBaseViewPagerLeft,0);
        mBaseViewPagerLeft.setId(BASEVIEW_PAGER_LEFT);
        
        if(mIndicatorShow){
            mIndicatorLeft = new Indicator(mContext);
            RelativeLayout.LayoutParams mLinearLayoutParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(mContext, 48), DensityUtil.dip2px(mContext, 16));
            mLinearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mLinearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mIndicatorLeft.setPadding(DensityUtil.dip2px(mContext, 2), DensityUtil.dip2px(mContext, 2), 0, 0);
            mIndicatorLeft.setLayoutParams(mLinearLayoutParams);
            mRelativeLayoutLeft.addView(mIndicatorLeft,1);
        }
        
        return mRelativeLayoutLeft;
    }
    
    public View onCreartRightView(){
        mRelativeLayoutRight = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mRelativeLayoutRight.setLayoutParams(relativeLayoutParams);
        
        mBaseViewPagerRight = new BaseViewPager(mContext);
        mRelativeLayoutRight.addView(mBaseViewPagerRight,0);
        mBaseViewPagerLeft.setId(BASEVIEW_PAGER_RIGHT);
        
        if(mIndicatorShow){
            mIndicatorRight = new Indicator(mContext);
            RelativeLayout.LayoutParams mLinearLayoutParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(mContext, 48), DensityUtil.dip2px(mContext, 16));
            mLinearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mLinearLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mIndicatorRight.setLayoutParams(mLinearLayoutParams);
            mIndicatorRight.setPadding(0, DensityUtil.dip2px(mContext, 2), DensityUtil.dip2px(mContext, 2), 0);
            mRelativeLayoutRight.addView(mIndicatorRight,1);
        }
        //hejianfeng add start
//        RelativeLayout mRelativeLayoutShadow = new RelativeLayout(mContext);
//        RelativeLayout.LayoutParams relativeLayoutParamsShadow = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        mRelativeLayoutShadow.setLayoutParams(relativeLayoutParamsShadow);
//        mRelativeLayoutShadow.setBackgroundResource(R.drawable.weather_background);
//        mRelativeLayoutRight.addView(mRelativeLayoutShadow);
        //hejianfeng add end
        return mRelativeLayoutRight;
    }
    
    
    public void setData(List<NetDateDao> list) {
//        if(mLauncher.getLauncherType() != LauncherType.onResume){
//            return;
//        }
        this.mList = list;
        if(mListLeft == null){
            mListLeft = new ArrayList<NetDateDao>();
        }else{
            mListLeft.clear();
        }
       
        if(mListRight == null){
            mListRight = new ArrayList<NetDateDao>();
        }else{
            mListRight.clear();
        }
        for (NetDateDao netDateDao:list) {
            if(netDateDao.type  == 6 || netDateDao.type  == 7){
                mListLeft.add(netDateDao);
            }else if(netDateDao.type  == 8 || netDateDao.type  == 9){
                mListRight.add(netDateDao);
            }
        }
        
        if(mListLeft != null || mListLeft.size() == 0){
            setDateLeft(mListLeft);
        }
        
        if(mListRight != null || mListRight.size() == 0){
            setDateRight(mListRight);
        }
        Log.i(TAG, "mListLeft:"+mListLeft.size()+"    mListRight:"+mListRight.size());
    }
    
    public int getCountView(){
        return getChildCount();
    }
    
    private void setDateLeft(ArrayList<NetDateDao> listLeft) {
        if(mViewPagerAdapterLeft == null){
            mViewPagerAdapterLeft = new ViewPagerAdapter(listLeft,"Left");
        }else{
            if(listLeft != mViewPagerAdapterLeft.getDate()){
                mViewPagerAdapterLeft.setDate(listLeft);
            }
        }
        
        if(mIndicatorShow){
            setIndicatorCount(mIndicatorLeft, listLeft.size());
            setIndicatorCurrentPosition(mIndicatorLeft, 0);
            if(listLeft.size() == 0){
                mIndicatorLeft.setVisibility(View.INVISIBLE);
            }else{
                mIndicatorLeft.setVisibility(View.VISIBLE);
            }
        }
        
        if(mBaseViewPagerLeft.getAdapter() == null){
            mBaseViewPagerLeft.setAdapter(mViewPagerAdapterLeft);
            mBaseViewPagerLeft.setOffscreenPageLimit(3);
            mBaseViewPagerLeft.setOnPageChangeListener(this);
        }
        
        if(listLeft != null){
            Log.i(TAG, "listLeft :"+listLeft.size());
        }else{
            Log.i(TAG, "listLeft is null");
        }
        mViewPagerAdapterLeft.notifyDataSetChanged();
    }
    
    private void setDateRight(ArrayList<NetDateDao> listRight) {
        if(mViewPagerAdapterRight == null){
            mViewPagerAdapterRight  = new ViewPagerAdapter(listRight,"Right");
        }else{
            if(listRight != mViewPagerAdapterRight.getDate()){
                mViewPagerAdapterRight.setDate(listRight);
            }
        }
        if(mIndicatorShow){
            setIndicatorCount(mIndicatorRight, listRight.size());
            setIndicatorCurrentPosition(mIndicatorRight, 0);
            
            if(listRight.size() == 0){
                mIndicatorRight.setVisibility(View.INVISIBLE);
            }else{
                mIndicatorRight.setVisibility(View.VISIBLE);
            }
        }
        
        if(mBaseViewPagerRight.getAdapter() == null){
            mBaseViewPagerRight.setAdapter(mViewPagerAdapterRight);
            mBaseViewPagerRight.setOffscreenPageLimit(3);
            mBaseViewPagerRight.setOnPageChangeListener(this);
        }
        
        if(listRight != null){
            Log.i(TAG, "listRight :"+listRight.size());
        }else{
            Log.i(TAG, "listRight is null");
        }
        mViewPagerAdapterRight.notifyDataSetChanged();
    }
    
    public void setIndicatorCurrentPosition(Indicator indicator,int position){
        Log.i(TAG, "setIndicatorCurrentPosition  indicator getCount:"+indicator.getCount()+"    CurrentPosition:"+indicator.getCurrentPosition());
        indicator.setCurrentPosition(position);
    }
    
    public void setIndicatorCount(Indicator indicator,int count){
        indicator.setCount(count);
    }




    /**
     * 设置轮播暂停时间，即没多少秒切换到下一张视图.默认5000ms
     * 
     * @param time
     *            毫秒为单位
     */
    public void setTime(int time) {
        this.time = time;
    }

    /**
     * 隐藏CycleViewPager
     */
    public void hide() {
        viewPagerFragmentLayout.setVisibility(View.GONE);
    }


    /**
     * 页面适配器 返回对应的view
     * 
     * @author Yuedong Li
     * 
     */
    private class ViewPagerAdapter extends PagerAdapter {
        
        public ImageView mFountImageView;
        
        private static final int ROUND_IMAGE_VIEW = 0x7f010001;
        private static final int TEXT_VIEW = 0x7f010002;
        private String position = "";
        
        private ArrayList<NetDateDao> mListDate;
        private boolean isViewRefreash = false;
        
        public ViewPagerAdapter(ArrayList<NetDateDao> listDate,String position){
            this.mListDate = listDate;
            this.position = position;
        }
        @Override
        public int getCount() {
            
            if(mListDate == null || mListDate.size() == 0){
                return 1;
            } if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){ //added by wangyang 2016.8.25
                return 1;
            }else{
                return mListDate.size();
            }
        }
        
        public void setDate(ArrayList<NetDateDao> listDate){
            this.mListDate = listDate;
        }
        
        public ArrayList<NetDateDao> getDate(){
            return this.mListDate;
        }
        
        public boolean isExistView(){
            return isViewRefreash;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.i(TAG, "  destroyItem                                position  =" + position);
            container.removeView((View) object);
        }

        @SuppressLint("NewApi")
        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            Log.i(TAG, "           position="+position);
            ViewHolder viewHolder = null;
            if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){  // 中国风设置为默认的图片 added by wangyang 2016.8.25
                if("Right".equals(this.position)){
                    viewHolder = onCreartView(2,null);
                }else if("Left".equals(this.position)){
                    viewHolder = onCreartView(1,null);
                }
                viewHolder.mRoundImageView.setImagePath(null,0);
                viewHolder.mRoundImageView.setImageShapeType(1);
                viewHolder.mTitleTextView.setBackground(null);
                container.addView(viewHolder.mRelativeLayout);
                
                //修改中国风只有一个topbar的问题 modify by wangyang 2016.10.17 start
                if(mListDate != null && mListDate.size() !=0){
                    NetDateDao nd = mListDate.get(position);
                    viewHolder.mRelativeLayout.setTag(nd);
                }
              //modify by wangyang 2016.10.17 end
                
                isViewRefreash = true;
                return viewHolder.mRelativeLayout;
            } else{
            	LauncherLog.v(TAG, "instantiateItem,jeff mListDate="+mListDate);
                if(mListDate == null || mListDate.size() == 0){
                    if("Right".equals(this.position)){
                        viewHolder = onCreartView(2,null);
                    }else if("Left".equals(this.position)){
                        viewHolder = onCreartView(1,null);
                    }
                    viewHolder.mRoundImageView.setImagePath(null,0);
                    viewHolder.mRoundImageView.setImageShapeType(1);
                    viewHolder.mTitleTextView.setBackground(null);
                    container.addView(viewHolder.mRelativeLayout);
                }else{
                    NetDateDao nd = mListDate.get(position);
                    viewHolder = onCreartView(0,nd);
                    Log.i(RoundImageView.TAG, "nd.banner:"+nd.banner+"     "+"nd.title:"+nd.title);
                    viewHolder.mRoundImageView.setImagePath(nd.banner,nd.type);
                    viewHolder.mRoundImageView.setImageShapeType(1);
                    viewHolder.mTitleTextView.setText(nd.title);

                    container.addView(viewHolder.mRelativeLayout);
                    viewHolder.mRelativeLayout.setTag(nd);
                }
                isViewRefreash = true;
                return viewHolder.mRelativeLayout;
            }
            
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        
        /**type 表示当前没有数据的时候 默认显示的图片类型 ， 1 表示新闻  2 表示健康*/
        private ViewHolder onCreartView(int type,NetDateDao nd){
            RelativeLayout mRelativeLayout = new RelativeLayout(mContext);
            RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            mRelativeLayout.setLayoutParams(relativeLayoutParams);
            
            RoundImageView mRoundImageView;
            if(type == 0){
                mRoundImageView = new RoundImageView(mContext,0);
            }else{
                mRoundImageView = new RoundImageView(mContext,type);
            }
            
            RelativeLayout.LayoutParams roundImageViewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            roundImageViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            roundImageViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            mRoundImageView.setLayoutParams(roundImageViewParams);
            mRoundImageView.setId(ROUND_IMAGE_VIEW_LEFT);
            mRelativeLayout.addView(mRoundImageView);
                   
            //当没有推送条目时，显示新闻和旅游 started by wangyang 2016.6.17
                TextView mFountTextView = new TextView(mContext);
                RelativeLayout.LayoutParams fountTextViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                if("Right".equals(this.position)){
                    fountTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    fountTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    fountTextViewParams.rightMargin = DensityUtil.dip2px(mContext, 3);
                    Log.i(TAG, "Right              "+DensityUtil.dip2px(mContext, 3));
                    Log.i(TAG, "Right              "+DensityUtil.dip2px(mContext, 6));
                }else if("Left".equals(this.position)){
                    fountTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    fountTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    fountTextViewParams.rightMargin = DensityUtil.dip2px(mContext, 3);
                }
                if(nd!=null){
                    mFountTextView.setText(getStringToType(nd.type));
                }else{
                    if("Right".equals(this.position)){
                        mFountTextView.setText(R.string.travel);
                    }else{
                        mFountTextView.setText(R.string.news);
                    }
                }
                
                mFountTextView.setLayoutParams(fountTextViewParams);
				
                //modify by wangyang 2016.10.18 start
                if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                    mFountTextView.setTextColor(Color.BLACK);
                    mFountTextView.setTextSize(DensityUtil.dip2px(mContext, 12));
                } else {
                    mFountTextView.getPaint().setFakeBoldText(true);// 加粗
                    mFountTextView.setTextColor(Color.WHITE);
                    mFountTextView.setShadowLayer(1, 0, 1, Color.BLACK);
                    mFountTextView.setTextSize(DensityUtil.dip2px(mContext, 10));
                }
              //modify by wangyang 2016.10.18 end
                
                
                mRelativeLayout.addView(mFountTextView);
            
            TextView mTitleTextView = new TextView(mContext);
            RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, DensityUtil.dip2px(mContext, 32));
            textViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mTitleTextView.setGravity(Gravity.CENTER);
            mTitleTextView.setMaxLines(1);
            if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
                mTitleTextView.setBackgroundResource(R.drawable.shape_blck_transparent);
                mFountTextView.setTypeface(mTypeface);
                mFountTextView.setTextColor(Color.parseColor("#000000"));
                mTitleTextView.setTextColor(Color.parseColor("#000000"));
                mTitleTextView.setTypeface(mTypeface);
            }else if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
            	mTitleTextView.setBackgroundResource(R.drawable.shape_circle_blck_transparent_nine);
            	mTitleTextView.setTextColor(Color.parseColor("#FFFFFF"));
            }
            else{
            	mTitleTextView.setBackgroundResource(R.drawable.shape_circle_blck_transparent);
                mTitleTextView.setTextColor(Color.parseColor("#FFFFFF"));
            }
            mTitleTextView.setTextSize(DensityUtil.dip2px(mContext, 10));
            mTitleTextView.setLayoutParams(textViewParams);
            mTitleTextView.setId(TEXT_VIEW);
            mRelativeLayout.addView(mTitleTextView);
            mRelativeLayout.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View view) {
                    NetDateDao netDateDao = (NetDateDao) view.getTag();
                    Intent intent = new Intent("com.cappu.launcherwin.PUSH");
                    intent.setPackage(mContext.getPackageName());
                    intent.putExtra("position", position);
                    intent.putExtra("TYPE", netDateDao);
                    intent.putParcelableArrayListExtra("TYPES", mListDate);
                    
                    if(netDateDao != null){
                        Log.i(TAG, "mNetDateDao:"+netDateDao.toString());
                    }
                    
                    try{
                        mContext.startActivity(intent);
                    }catch(Exception e){
                        Log.i(TAG, "startActivity exception "+e.toString());
                    }
                    
                    
                    if(mContext instanceof Launcher){
                        Log.i(TAG, "mContext is launcher");
                    }
                }
            });
            
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mRelativeLayout = mRelativeLayout;
            viewHolder.mTitleTextView = mTitleTextView;
            viewHolder.mRoundImageView = mRoundImageView;
            return viewHolder;
        }
        
        public String getStringToType(int type){
            String str = null;
            if(type == 1 || type == 6){
                str = mContext.getString(R.string.news);
            }else if(type == 2 || type == 7){
                str = mContext.getString(R.string.health);
            }else if(type == 3 || type == 8){
                str =  mContext.getString(R.string.travel);
            }else if(type == 4 || type == 9){
                str = mContext.getString(R.string.finances);
            }
            return str;
        }
    }

    private class ViewHolder{
        public RelativeLayout mRelativeLayout;
        public TextView mTitleTextView;
        public RoundImageView mRoundImageView;
    }
    
    @Override
    public void onPageScrollStateChanged(int arg0) {
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        if(mIndicatorShow){
            if(mCurrentCount % 2 == 0){
                mLeftCurrentPosition = position;
                setIndicatorCurrentPosition(mIndicatorLeft, mLeftCurrentPosition);
            }else{
                mRightCurrentPosition = position;
                setIndicatorCurrentPosition(mIndicatorRight, mRightCurrentPosition);
            }
        }
        
    }

    /**
     * 如果当前页面嵌套在另一个viewPager中，为了在进行滚动时阻断父ViewPager滚动，可以 阻止父ViewPager滑动事件
     * 父ViewPager需要实现ParentViewPager中的setScrollable方法
     */
    public void disableParentViewPagerTouchEvent(BaseViewPager parentViewPager) {
        if (parentViewPager != null)
            parentViewPager.setScrollable(false);
    }

    /**
     * 轮播控件的监听事件
     * 
     * @author minking
     */
    public static interface ImageCycleViewListener {

        /**
         * 单击图片事件
         * 
         * @param position
         * @param imageView
         */
        public void onImageClick(NetDateDao info, int postion, View imageView);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
    
    @Override
    protected void onAttachedToWindow() {
        Log.i(TAG, "view 添加的时候执行这个");
        super.onAttachedToWindow();
    }


    public void deleteView() {
        isStop = true;
        mCycleHandler = null;
        
        mRelativeLayoutLeft.removeAllViewsInLayout();
        removeView(mRelativeLayoutLeft);
        
        mRelativeLayoutRight.removeAllViewsInLayout();
        removeView(mRelativeLayoutRight);
        
        mBaseViewPagerRight.removeAllViewsInLayout();
        removeView(mBaseViewPagerRight);
        
        mBaseViewPagerLeft.removeAllViewsInLayout();
        removeView(mBaseViewPagerLeft);
        
        removeView(mIndicatorRight);
        removeView(mIndicatorLeft);
        removeView(viewPagerFragmentLayout);
        mViewPagerAdapterLeft = null;
        mViewPagerAdapterRight = null;
        
        if(viewPagerFragmentLayout != null){
            viewPagerFragmentLayout.removeAllViewsInLayout();
        }
        
        
        mRelativeLayoutLeft = null;
        mRelativeLayoutRight = null;
        mBaseViewPagerRight = null;
        mBaseViewPagerLeft = null;
        mIndicatorRight = null;
        mIndicatorLeft = null;
        viewPagerFragmentLayout = null;
        if(mListLeft != null){
            mListLeft.clear();
            mListLeft = null;
        }
        
        if(mListRight != null){
            mListRight.clear();
            mListRight = null;
        }
        
        removeAllViewsInLayout();
        Log.i(TAG, "view 销毁的时候执行这个 isStop:"+isStop);
        
    }
    
}
