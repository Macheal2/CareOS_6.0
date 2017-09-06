/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.micode.fileexplorer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.android.kookutil.tabutil.TabHostUtile;

public class FileExplorerTabActivity extends Activity {
    static String TAG = "FileExplorerTabActivity";
    
    private static final String INSTANCESTATE_TAB = "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES = 2;
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    ActionMode mActionMode;
    
    TabHost mTabHost;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pager);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);
        
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mTabsAdapter = new TabsAdapter(this,mTabHost, mViewPager);
        mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.tab_category)).setIndicator(onCreatTabView(getString(R.string.tab_category))).setContent(android.R.id.tabcontent), FileCategoryActivity.class, null);
        mTabsAdapter.addTab(mTabHost.newTabSpec(getString(R.string.tab_sd)).setIndicator(onCreatTabView(getString(R.string.tab_sd))).setContent(android.R.id.tabcontent), FileViewActivity.class, null);
        //mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_remote), ServerControlActivity.class, null);
        
        initTab();
    }

    @SuppressLint("ResourceAsColor")
    public View onCreatTabView(String title){
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.leftMargin = 0;
        lp.rightMargin = 0;
        
        tv.setBackgroundResource(R.drawable.action_bar_background);
        tv.setText(title);
        
        tv.setSingleLine();
        tv.setGravity(Gravity.CENTER);
        tv.setEllipsize(TruncateAt.MARQUEE);
        tv.setFocusable(true);
        tv.setTextSize(16);
        tv.setTextAppearance(this, android.R.attr.textAppearanceSmall);
        tv.setLayoutParams(lp);
        
        tv.setTextColor(createColorStateList() );
        return tv;
    }
    
    public TabHost getTabHost() {
        return mTabHost;
    }
    

    /** 对TextView设置不同状态时其文字颜色。 */
        private ColorStateList createColorStateList() {
            Resources resource=(Resources)getBaseContext().getResources();   
            ColorStateList csl=(ColorStateList)resource.getColorStateList(R.color.tab_indicator_text);  
            return csl;
        }
    
        
    private void initTab(){
        int index = PreferenceManager.getDefaultSharedPreferences(this).getInt(INSTANCESTATE_TAB, Util.CATEGORY_TAB_INDEX);
        boolean isSuccess = TabHostUtile.initTab(this,mTabHost, 0);
        Log.i(TAG, "isSuccess: "+isSuccess+"    index:"+index);
    }
     
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences != null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(editor != null){
                editor.putInt(INSTANCESTATE_TAB, mTabHost.getCurrentTab());
                editor.commit();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged ");
        
        if (mTabHost != null && mTabHost.getCurrentTab()  == Util.CATEGORY_TAB_INDEX) {//getActionBar().getSelectedNavigationIndex() == Util.CATEGORY_TAB_INDEX
            FileCategoryActivity categoryFragement = (FileCategoryActivity) mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) {
                reInstantiateCategoryTab();
            } else {
                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void reInstantiateCategoryTab() {
        mTabsAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX, mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mTabsAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mTabsAdapter.getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    public interface IBackPressedListener {
        /**
         * 处理back事件。
         * 
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }

    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public Fragment getFragment(int tabIndex) {
        return mTabsAdapter.getItem(tabIndex);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost. It relies on a
     * trick. Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show. This is not sufficient for switching
     * between pages. So instead we make the content part of the tab host 0dp
     * high (it is not shown) and the TabsAdapter supplies its own dummy view to
     * show as the tab content. It listens to changes in tabs, and takes care of
     * switch to the correct paged in the ViewPager whenever the selected tab
     * changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter implements OnTabChangeListener/*ActionBar.TabListener*/, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ViewPager mViewPager;
        
        TabHost mTabHost;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private final TabSpec mTabSpec;
            private Fragment fragment;

            TabInfo(TabSpec tab,Class<?> _class, Bundle _args) {
                mTabSpec = tab;
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity,TabHost tabHost,ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            
            mViewPager.setOnPageChangeListener(this);
            mTabHost.setOnTabChangedListener(this);
            
            mViewPager.setAdapter(this);
        }

        public void addTab(TabSpec tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(tab,clss, args);
            mTabs.add(info);
            mTabHost.addTab(tab);
            notifyDataSetChanged();
            
        }

        @Override
        public int getCount() {
            Log.i(TAG, "getCount mTabs.size():"+mTabs.size());
            return mTabs.size();
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            Log.i(TAG, "onPageSelected arg0:"+arg0);
            mTabHost.setCurrentTab(arg0);
        }

        @Override
        public void onTabChanged(String tabId) {
            Log.i(TAG, "onTabChanged tabId:"+tabId+"    mTabs:"+mTabs.size() +"   mViewPager is null:"+(mViewPager == null));
            for (int i = 0; i < mTabs.size(); i++) {
                if (mTabs.get(i).mTabSpec.getTag().equals(tabId)) {
                    Log.i(TAG, "------------------------------:"+mViewPager.getChildCount());
                    mViewPager.setCurrentItem(i);
                }
            }
            if (!tabId.equals(mContext.getString(R.string.tab_sd))) {
                ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
                if (actionMode != null) {
                    actionMode.finish();
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(TAG, "getItem position:"+position);
            TabInfo info = mTabs.get(position);
            if (info.fragment == null) {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
        }
    }
}
