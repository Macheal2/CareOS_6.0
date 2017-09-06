package com.cappu.launcherwin;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.SparseArrayMap;
import com.cappu.launcherwin.widget.TopBar;
import android.view.MotionEvent;


public class ScreenManagerActivity extends BasicActivity {
	private String TAG="ScreenManagerActivity";
    private SparseArrayMap<CellLayout> mWorkspaceScreens;
    private ViewPager viewPager;
    private ArrayList<View> views=new ArrayList<View>();
    private ImageView imgDelete;
    private LinearLayout mViewPagerContainer;
    private static final float MAX_SCALE = 1.1f;
    private static final float MIN_SCALE = 1.0f;//0.85f
    private ImageView imgDeleteNotClick;
    private int mSelectPage=0;
    private static final int MAX_PAGE_NUM=10;
    private boolean isNegativeScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_manager_layout);
        mWorkspaceScreens=ThemeManager.getInstance().getWorkspace().getWorkspaceScreens();
        LauncherLog.v("hejianfeng", "getWorkspaceScreens="+mWorkspaceScreens);
        View convertView =null;
        isNegativeScreen=ThemeManager.getInstance().getWorkspace().isNegativeScreen();
        if(mWorkspaceScreens!=null && mWorkspaceScreens.size()>0){
        	int i=isNegativeScreen?0:1;
        	for(;i<mWorkspaceScreens.size();i++){
				CellLayout cellLayout = mWorkspaceScreens.get(ThemeManager
						.getInstance().getWorkspace().getScreenOrder().get(i));
				if(cellLayout.getWidth()<=0||cellLayout.getHeight()<=0){
					continue;
				}
				Bitmap bitmap = Bitmap.createBitmap(cellLayout.getWidth(),
						cellLayout.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				cellLayout.draw(canvas);
				LayoutInflater mInflater = LayoutInflater
						.from(ScreenManagerActivity.this);
				convertView = mInflater
						.inflate(R.layout.screen_grid_item, null);
				RelativeLayout rlview = (RelativeLayout) convertView
						.findViewById(R.id.rl_screen);
				rlview.setBackground(new BitmapDrawable(bitmap));
				views.add(convertView);
        	}
			if (displayAddPage()) {
				initAddPage();
			}
        }
        imgDelete =(ImageView) findViewById(R.id.img_delete);
        imgDeleteNotClick =(ImageView) findViewById(R.id.img_delete_not_click);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(
        		mWorkspaceScreens.get(0).getWidth()*2/3,
        		mWorkspaceScreens.get(0).getHeight()*2/3);
        viewPager = (ViewPager) findViewById(R.id.viewPager_screen);
        viewPager.setClipChildren(false);
        viewPager.setLayoutParams(params);
        viewPager.setAdapter(new ScreenAdapter());
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        //设置预加载数量
        viewPager.setOffscreenPageLimit(2);
        //设置每页之间的左右间隔
        viewPager.setPageMargin(80);
      //将容器的触摸事件反馈给ViewPager
        mViewPagerContainer=(LinearLayout)findViewById(R.id.viewPagerContainer);
        mViewPagerContainer.setClipChildren(false);
        mViewPagerContainer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                return viewPager.dispatchTouchEvent(event);
            }

        });
        imgDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(ScreenManagerActivity.this,
						Launcher.class);
				intent.setAction(BasicKEY.THEME_DELETE_ACTION);
				intent.putExtra(
						"delete_select_page",
						ThemeManager
								.getInstance()
								.getWorkspace()
								.getScreenOrder()
								.get(isNegativeScreen ? mSelectPage
										: mSelectPage + 1));
				startActivity(intent);
				finish();
			}
		});
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {  
            
            @Override  
            public void onPageSelected(int arg0) {  
            	LauncherLog.v(TAG, "onPageSelected,jeff arg0="+arg0);
            	LauncherLog.v(TAG, "onPageSelected,jeff views.size()="+views.size());
            	mSelectPage=arg0;
				if (arg0 >= ThemeManager.getInstance().getWorkspace()
						.getDefaultMaxPage()
						&& (!displayAddPage() || displayAddPage()
								&& arg0 < (views.size() - 1))) {
					imgDelete.setVisibility(View.VISIBLE);
					imgDeleteNotClick.setVisibility(View.GONE);
				} else {
					imgDelete.setVisibility(View.GONE);
					imgDeleteNotClick.setVisibility(View.VISIBLE);
				}
            }  
              
            @Override  
            public void onPageScrolled(int arg0, float arg1, int arg2) {  
                  
            }
              
            @Override  
            public void onPageScrollStateChanged(int arg0) {  
                  
            }  
        }) ;
        (views.get(viewPager.getCurrentItem())).setScaleX(MAX_SCALE);
        (views.get(viewPager.getCurrentItem())).setScaleY(MAX_SCALE);
        
    }
    private boolean displayAddPage(){
    	return mWorkspaceScreens.size() < MAX_PAGE_NUM+1;
    }
    private void initAddPage(){
    	Bitmap workspaceBmp = ThemeManager.getInstance().getImagePiece().workspaceBmp;
		LayoutInflater mInflater = LayoutInflater
				.from(ScreenManagerActivity.this);
		View convertView = mInflater.inflate(R.layout.screen_grid_item,
				null);
		RelativeLayout rlview = (RelativeLayout) convertView
				.findViewById(R.id.rl_screen);
		rlview.setBackground(new BitmapDrawable(workspaceBmp));
		ImageView imgAdd=(ImageView) convertView
				.findViewById(R.id.img_add);
		imgAdd.setVisibility(View.VISIBLE);
		views.add(convertView);
		
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(ScreenManagerActivity.this,Launcher.class).setAction(BasicKEY.THEME_ADD_ACTION));
	        	finish();
			}
		});
    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		views.clear();
		views=null;
	}
    private class ScreenAdapter extends PagerAdapter {

	    @Override
	    public int getCount() {                                                           
	        // TODO Auto-generated method stub
	        return views.size();
	    }

	    @Override
	    public boolean isViewFromObject(View arg0, Object arg1) {                         
	        // TODO Auto-generated method stub
	        return arg0 == arg1;
	    }
	    
	    @Override
	    public void destroyItem(View view, int position, Object object)                
	    {
	        ((ViewPager) view).removeView(views.get(position));
	    }
	    
	    @Override
	    public Object instantiateItem(View view, int position)        
	    {
	        ((ViewPager) view).addView(views.get(position), 0);
	        return views.get(position);
	    }

	}
    class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            //setScaleY只支持api11以上
        	LauncherLog.v(TAG, "transformPage,jeff position="+position);
            if (position < -1){
                view.setScaleX(MIN_SCALE);
                view.setScaleY(MIN_SCALE);
            } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            { // [-1,1]
//              Log.e("TAG", view + " , " + position + "");
                float scaleFactor =  MIN_SCALE+(1-Math.abs(position))*(MAX_SCALE-MIN_SCALE);
                view.setScaleX(scaleFactor);
                //每次滑动后进行微小的移动目的是为了防止在三星的某些手机上出现两边的页面为显示的情况
                if(position>0){
                    view.setTranslationX(-scaleFactor*2);
                }else if(position<0){
                    view.setTranslationX(scaleFactor*2);
                }
                view.setScaleY(scaleFactor);

            } else
            { // (1,+Infinity]

                view.setScaleX(MIN_SCALE);
                view.setScaleY(MIN_SCALE);

            }
        }

    }
}
