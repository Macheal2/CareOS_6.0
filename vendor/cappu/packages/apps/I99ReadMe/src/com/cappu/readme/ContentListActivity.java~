package com.cappu.readme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.cappu.widget.TopBar;
import com.cappu.widget.TopBar.onTopBarListener;


public class ContentListActivity extends Activity implements OnItemClickListener, OnClickListener{

    private static final String TAG = "ContentListActivity";
    private int mReadmeId;

    private ListView mListView;
    private TopBar mTopBar;

    private String[] mTitleData;
    private int[] mIconData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_list_activity);

        Intent intent = getIntent();
        mReadmeId = intent.getIntExtra("which_list", ReadMeConfigure.USER_MANUAL);
        Log.d(TAG,"mReadmeId = " + mReadmeId);

        mTopBar = (TopBar)findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(mTopBarListener);
        Resources res = getResources();

        if(0 == mReadmeId){
            mTopBar.setText(R.string.title_user_manual);
            mTitleData = res.getStringArray(R.array.user_manual_list);
            mIconData = getIcons(R.array.user_manual_icons);
        }else if(1 == mReadmeId){
            mTopBar.setText(R.string.title_weixin);
            mTitleData = res.getStringArray(R.array.weixin_list);
            mIconData = getIcons(R.array.weixin_icons);
        }else{
            mTopBar.setText(R.string.title_user_manual);
            mTitleData = res.getStringArray(R.array.user_manual_list);
            mIconData = getIcons(R.array.user_manual_icons);
        }

        mListView =(ListView) findViewById(R.id.list);
        mListView.setAdapter(new ContentAdapter());
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v){
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        Intent intent = new Intent();
        intent.putExtra("which_picture", position);
        intent.putExtra("which_group", mReadmeId);
        intent.setClass(ContentListActivity.this, FullscreenActivity.class);
        startActivity(intent);
    }

    private int[] getIcons(int resid){
        Resources res = getResources();
        final String [] iconsName = res.getStringArray(resid);
        int [] icons = new int[iconsName.length];
        for(int i=0 ; i< iconsName.length ; i++){
            icons[i] = res.getIdentifier(iconsName[i], "drawable", "com.cappu.readme");
        }
        return icons;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ContentEntry{
        String title;
        int icon;
    }

    private class ContentHolder{
        ImageView icon;
        TextView  title;
    }

    private class ContentAdapter extends BaseAdapter {

        final List<ContentEntry> mData = new ArrayList<ContentEntry>();

        public ContentAdapter(){
            for( int i=0; i < mTitleData.length; i++ ){
                ContentEntry entry = new ContentEntry();
                entry.title = mTitleData[i];
                entry.icon = mIconData[i];
                mData.add(entry);
            }
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {

            ContentHolder holder ;
            ContentEntry entry = mData.get(position);

            if(view == null){
                LayoutInflater infliter = LayoutInflater.from(ContentListActivity.this);
                view = infliter.inflate(R.layout.list_item,null);
                holder = new ContentHolder();
                holder.title = (TextView)view.findViewById(R.id.title);
                holder.icon = (ImageView)view.findViewById(R.id.icon);
                view.setTag(holder);
            }else{
                holder = (ContentHolder)view.getTag();
            }

            holder.title.setText(entry.title);
            holder.icon.setImageResource(entry.icon);

            final int count = getCount();

            if( count == 1 ){
                view.setBackgroundResource(R.drawable.item_bg);
            }else if ( position == 0){
                view.setBackgroundResource(R.drawable.item_top_bg);
            }else if ( position == count -1 ){
                view.setBackgroundResource(R.drawable.item_bottom_bg);
            }else{
                view.setBackgroundResource(R.drawable.item_middle_bg);
            }

            return view;
        }
    }

    private onTopBarListener mTopBarListener = new onTopBarListener(){

        public void onLeftClick(View v){
            ContentListActivity.this.finish();
        }
        public void onRightClick(View v){
        }    
        public void onTitleClick(View v){

        }
    };
}
