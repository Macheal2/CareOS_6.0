package com.cappu.readme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FullscreenActivity extends Activity implements OnClickListener, OnPageChangeListener{

    private static final String TAG = "FullscreenActivity";

    private int mCurrentIndex;
    private int mDotsNum = 0;
    private int mGroup;
    private int mItemPicture;
    private int mReadmeId;

    private ImageView[] mDots;
    private ViewPager mViewPager;
    private ViewPagerAdapter mPagerAdapter;

    private List<View> mViews;
    private List<View> mMediaViews;
    private List<View> mFaqViews;
    private LinearLayout mDotsContent;

    private static final int[] mMediaPics = {
        R.drawable.c1,
        R.drawable.c2,
        R.drawable.c3,
        R.drawable.c4
        // R.drawable.c5,
        //R.drawable.c6,
        //R.drawable.c7
    };

    private static final int[] mFaqPics = {
        R.drawable.common_issue1,
        R.drawable.common_issue2,
        R.drawable.common_issue3
    };

    private static final int[][][] mAllPics = {
            ////////GROUP0////////////////////////////
            {
                {R.drawable.a11, R.drawable.a12, R.drawable.a13, R.drawable.a14 },
                {R.drawable.a21, R.drawable.a22, R.drawable.a23, R.drawable.a24, R.drawable.a25, R.drawable.a26, R.drawable.a27 },
                {R.drawable.a31, R.drawable.a32, R.drawable.a33, R.drawable.a34 },
                {R.drawable.a41, R.drawable.a42, R.drawable.a43, R.drawable.a44 },
                {R.drawable.a51 },
                {R.drawable.a61 }
            },
            ////////GROUP1////////////////////////////
            {
                {R.drawable.b11},
                {R.drawable.b21},
                {R.drawable.b31},
                {R.drawable.b41},
                {R.drawable.b51},
                {R.drawable.b61},
                {R.drawable.b71},
                {R.drawable.b81},
                {R.drawable.b91},
                {R.drawable.b101},
                {R.drawable.b111},
                {R.drawable.b121},
            },
            ////////GROUP2////////////////////////////
        };

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mDotsContent = (LinearLayout) findViewById(R.id.dots_content);

        Intent intent1 = getIntent();
        mReadmeId = intent1.getIntExtra("which_list", 0);
        mItemPicture = intent1.getIntExtra("which_picture", 0);
        mGroup = intent1.getIntExtra("which_group", 0);
        Log.d("EVEN7799","mItemPicture = " + mItemPicture +"; mGroup = " + mGroup +"; mReadmeId = " + mReadmeId);

        mViews = new ArrayList<View>();
        mMediaViews = new ArrayList<View>();
        mFaqViews = new ArrayList<View>();


        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,

        LinearLayout.LayoutParams.WRAP_CONTENT);

        if( ReadMeConfigure.MEDIA_MANAGER == mReadmeId ){
            for(int i=0; i<mMediaPics.length; i++) {
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(mParams);
                iv.setBackgroundResource(mMediaPics[i]);
                mMediaViews.add(iv);
            }
        }else if(ReadMeConfigure.FAQ == mReadmeId){
            for(int i=0; i<mFaqPics.length; i++) {
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(mParams);
                iv.setBackgroundResource(mFaqPics[i]);
                mFaqViews.add(iv);
            }
        } else {
            for(int i=0; i<mAllPics[mGroup][mItemPicture].length; i++) {
                ImageView iv = new ImageView(this);
                iv.setLayoutParams(mParams);
                iv.setBackgroundResource(mAllPics[mGroup][mItemPicture][i]);
                mViews.add(iv);
            }
        }

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new ViewPagerAdapter(mViews);

        if( ReadMeConfigure.MEDIA_MANAGER == mReadmeId ){
            mPagerAdapter = new ViewPagerAdapter(mMediaViews);
        }else if( ReadMeConfigure.FAQ == mReadmeId ){
            mPagerAdapter = new ViewPagerAdapter(mFaqViews);
        }else{
            mPagerAdapter = new ViewPagerAdapter(mViews);
        }
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(this);

        initDots();
    }

    private void initDots() {

        if( ReadMeConfigure.MEDIA_MANAGER == mReadmeId ) {
            mDotsNum = mMediaPics.length;
        } else if (ReadMeConfigure.FAQ == mReadmeId ){
            mDotsNum = mFaqPics.length;
        } else {
            mDotsNum = mAllPics[mGroup][mItemPicture].length;
        }

        mDots = new ImageView[mDotsNum];
        Log.d( TAG,"mItemPicture = " + mItemPicture +"; mGroup = " + mGroup + "; mAllPics[mGroup][mItemPicture].length =" +mAllPics[mGroup][mItemPicture].length);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < mDotsNum; i++) {
            mDots[i] = new ImageView(this);
            mDots[i].setLayoutParams(params);
            mDots[i].setImageResource(R.drawable.dot);
            mDots[i].setEnabled(true);
            mDots[i].setOnClickListener(this);
            mDots[i].setVisibility(View.VISIBLE);
            mDots[i].setTag(i);
            mDotsContent.addView(mDots[i]);
        }

        mCurrentIndex = 0;
        mDots[mCurrentIndex].setEnabled(false);
    }

    private void setCurView(int position){

        if (position < 0 || position >= mDotsNum) {
            return;
        }
        mViewPager.setCurrentItem(position);
    }

    /**
     * dots is selects
     */
    private void setCurDot(int positon){

        if (positon < 0 || positon > mDotsNum || mCurrentIndex == positon) {
            return;
        }

        mDots[positon].setEnabled(false);
        mDots[mCurrentIndex].setEnabled(true);
        mCurrentIndex = positon;
    }


    @Override
    public void onPageScrollStateChanged(int arg0) {

        if(mDotsContent == null){
            return ;
        }

        if(1 == arg0){
            mDotsContent.setVisibility(View.VISIBLE);
        }else{
            mDotsContent.setVisibility(View.GONE);
        }
    }


    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurDot(arg0);
    }

    @Override
    public void onClick(View v) {
        int position = (Integer)v.getTag();
        setCurView(position);
        setCurDot(position);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

