package com.cappu.music;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import com.cappu.music.database.MusicProvider;

public class MusicBrowserActivity extends Activity implements OnTabChangeListener{

    private static final String ARTIST = "Artist";
    private static final String Folder = "Folder";
    private static final String PLAYLIST = "Playlist";
    
    static final int FOLDER_INDEX = 1;
    static final int PLAYLIST_INDEX = 0;
    
    private TabHost mTabHost;
    private int mTabCount;
    private int mCurrentTab;
    
    
    FolderFragment mFolderFragment;
    PlayFragment mPlayFragment;
    
    /*FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;*/
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_browser);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        initTab();
        mTabHost.setOnTabChangedListener(this);
    }
    
    public void setFolderFragment(FolderFragment folderFragment){
        this.mFolderFragment = folderFragment;
    }
    
    public void setPlayFragment(PlayFragment playFragment){
        this.mPlayFragment = playFragment;
    }
    
    private void initTab() {
        final TabWidget tabWidget = (TabWidget) getLayoutInflater().inflate(R.layout.buttonbar, null);
        mTabCount = tabWidget.getChildCount();
        View tabView;
        for (int i = 0; i < mTabCount; i++) {
            tabView = tabWidget.getChildAt(0);
            if (tabView != null) {//这里是将 tabView 的父类去掉
                tabWidget.removeView(tabView);
            }
            if(tabView != null){
                if(tabView.getId() == R.id.play_list){
                    mTabHost.addTab(mTabHost.newTabSpec(getStringId(i)).setIndicator(tabView).setContent(R.id.play_fragment));
                }else if(tabView.getId() == R.id.play_folder){
                    mTabHost.addTab(mTabHost.newTabSpec(getStringId(i)).setIndicator(tabView).setContent(R.id.folder_fragment));
                }
            }
        }
    }
    
    /**
     * get current tab id though index
     * 
     * @param index
     * @return
     */
    private String getStringId(int index) {
        String tabStr = ARTIST;
        switch (index) {
            case PLAYLIST_INDEX:
                tabStr = PLAYLIST;
                break;
            case FOLDER_INDEX:
                tabStr = Folder;
                break;
            default:
                break;
        }
        return tabStr;
    }

    @SuppressLint("NewApi")
    @Override
    public void onTabChanged(String tag) {
        mTabHost.setCurrentTabByTag(tag);
        if(tag.equals(Folder)){
            //Log.i("hehangjun", "文件夹  mFolderFragment"+(mFolderFragment.getView() == null)+"   mTabHost.getTabContentView():"+mTabHost.getTabContentView().toString());
            mFolderFragment.Refresh(mFolderFragment);
            
        }else if(tag.equals(PLAYLIST)){
            mPlayFragment.Refresh();
            //Log.i("hehangjun", "播放  mPlayFragment.isHidden():"+mPlayFragment.isHidden()+"    "+mPlayFragment.isDetached());
        }
        
    }
    
    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        if(mFolderFragment!=null){
            mFolderFragment.Refresh(mFolderFragment);
        }
        
        if(mPlayFragment!=null){
            mPlayFragment.onResume();
        }
    }
    @SuppressLint("NewApi")
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        if(mPlayFragment!=null){
            mPlayFragment.onDestroyunregisterReceiver();
        }
    }
    
/*    @Override
    public void onBackPressed() {
        Log.i("HHJ", "----------------------");
    }*/
}
