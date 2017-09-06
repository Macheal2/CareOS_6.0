package com.cappu.launcherwin;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.cappu.launcherwin.R;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.netinfo.widget.BaseViewPager;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.TopBar;

//add by wangyang 2016.9.1
public class ThemeSelectActivity extends BasicActivity {

    private ListView mListView;
    private int mCurrentTheme;
    
    private Context mContext;
    private ArrayList<ThemeSelectItem> mSelectItems=new ArrayList<ThemeSelectItem>();
    private final String TAG = "ThemeSelectActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cappu_theme_select);
        mContext = getApplicationContext();
        init();
    }
    private void initSelectItems(){
    	File[] files=ThemeManager.getInstance().getDefaultThemeListFile();
    	for (File file:files){
    		ThemeSelectItem mThemeSelectItem=new ThemeSelectItem(mContext,mCurrentTheme);
    		int modeSelect=ThemeRes.getInstance().getThemeModeSelect(file.getName());
    		LauncherLog.v(TAG, "initSelectItems,jeff file.getName()="+file.getName()+",modeSelect="+modeSelect);
    		mThemeSelectItem.setOwnTheme(modeSelect);
    		mThemeSelectItem.setmThemeBmp1(ThemeRes.getInstance().getThemeBackground("theme_select_Model_1",modeSelect,file.getName()));
    		mThemeSelectItem.setmThemeTitle(ThemeRes.getInstance().getThemeBgName("theme_select_title", modeSelect));
    		mSelectItems.add(mThemeSelectItem);
    	}
    	Comparator comp = new SortComparator(); 
    	Collections.sort(mSelectItems,comp); 
    }
    //hejianfeng add start
   class SortComparator implements Comparator {  
        @Override  
        public int compare(Object lhs, Object rhs) {  
        	ThemeSelectItem a = (ThemeSelectItem) lhs;  
        	ThemeSelectItem b = (ThemeSelectItem) rhs;  
      
            return (a.getOwnTheme() - b.getOwnTheme());  
        }  
    } 
   //hejianfeng add end
    private void init() {
        mCurrentTheme = Settings.Global.getInt(getContentResolver(), ThemeManager.ACTION_THEME_KEY, ThemeManager.THEME_DEFUALT);
        if(mSelectItems.size()==0){
        	initSelectItems();
        }
        mListView = (ListView)findViewById(R.id.theme_list_view);
        MyBaseAdapter mMyBaseAdapter = new MyBaseAdapter();
        mListView.setAdapter(mMyBaseAdapter);
    }
    
    private class MyBaseAdapter extends BaseAdapter {
        
        private LayoutInflater mInflater;
        public MyBaseAdapter(){
            mInflater = LayoutInflater.from(getApplicationContext());
        }
        
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mSelectItems.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        /**
         * author hejianfeng 
         * 初始化AdapterView
         * @param mViewPagerHolder
         * @param convertView
         */
        private void initAdapterView(ViewPagerHolder mViewPagerHolder,View convertView){
            mViewPagerHolder.mTextTitle = (TextView)convertView.findViewById(R.id.text_title);
            mViewPagerHolder.mImageView1 = (ImageView)convertView.findViewById(R.id.image_view_1);
            mViewPagerHolder.mViewTwoSpit = (ImageView)convertView.findViewById(R.id.view_two_spit);
            mViewPagerHolder.mThemeChoice = (RelativeLayout)convertView.findViewById(R.id.theme_choice);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewPagerHolder mViewPagerHolder = null;
			if (convertView == null) {
				mViewPagerHolder = new ViewPagerHolder();
				convertView = mInflater.inflate(
						R.layout.cappu_theme_viewpager, null);
				initAdapterView(mViewPagerHolder, convertView);
				convertView.setTag(mViewPagerHolder);
			} else {
				mViewPagerHolder = (ViewPagerHolder) convertView.getTag();
			}
			loadAdapterUI(mViewPagerHolder,position);
            return convertView;
        }
        /**
         * 加载AdapterUI
         * @param mViewPagerHolder
         * @param position
         */
		private void loadAdapterUI(ViewPagerHolder mViewPagerHolder,
				final int position) {
			LauncherLog.v(TAG,"loadAdapterUI,jeff position="+position +",mCurrentTheme="+mCurrentTheme);
			final ThemeSelectItem mThemeSelectItem=mSelectItems.get(position);
			mViewPagerHolder.mTextTitle
					.setText(mThemeSelectItem.getmThemeTitle());
			mViewPagerHolder.mImageView1.setImageBitmap(mThemeSelectItem
					.getmThemeBmp1());
			mViewPagerHolder.mThemeChoice
			.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Settings.Global.putInt(getContentResolver(),
							ThemeManager.ACTION_THEME_KEY,
							mThemeSelectItem.getOwnTheme());
				}
			});
			if (mThemeSelectItem.isUseTheme()) {
				mViewPagerHolder.mThemeChoice
						.setBackgroundResource(R.drawable.theme_text_used_ing);
			} else {
				mViewPagerHolder.mThemeChoice
						.setBackgroundResource(R.drawable.theme_text_used);
			}
			if(position ==(getCount()-1)){
				
				mViewPagerHolder.mViewTwoSpit.setVisibility(View.GONE);
			}else{
				mViewPagerHolder.mViewTwoSpit.setVisibility(View.VISIBLE);
			}
		}

        private class ViewPagerHolder{
            HorizontalScrollView mViewPager;
            ImageView mImageView1;
            ImageView mViewTwoSpit;
            RelativeLayout mThemeChoice;
            TextView mTextTitle;
        }
    }

    @Override
    public void onThemeChanged(int theme) {
    	LauncherLog.v(TAG, "onThemeChanged,jeff theme="+theme);
        startActivity(new Intent(this,Launcher.class).setAction(BasicKEY.THEME_CHANGE_ACTION));
        finish();
    }
}
