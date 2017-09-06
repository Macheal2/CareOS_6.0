package com.cappu.cleaner;

import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.cappu.cleaner.CleanerManager;
import com.cappu.cleaner.R;
import com.cappu.cleaner.Util;
import com.cappu.cleaner.base.BaseActivity;
import com.cappu.cleaner.context.SharedPreferencesUtils;
import com.cappu.cleaner.service.CleanerService;
import com.cappu.cleaner.service.CoreService;

import java.io.File;
import java.util.Random;


public class SplishActivity extends BaseActivity {

    /**
     * 三个切换的动画
     */
    private Animation mFadeIn;
    private Animation mFadeInScale;
    private Animation mFadeOut;

    ImageView mImageView;
    private long mCurrentTempTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splish);
        mImageView = (ImageView) findViewById(R.id.image);
        int index = new Random().nextInt(2);
        if (index == 1) {
            mImageView.setImageResource(R.drawable.entrance3);
        } else {
            mImageView.setImageResource(R.drawable.entrance2);
        }
        startService(new Intent(this, CoreService.class));
        startService(new Intent(this, CleanerService.class));


        if (!SharedPreferencesUtils.isShortCut(mContext)) {
            createShortCut();
        }

        initAnim();
        setListener();
        initData();

    }

    private void createShortCut() {
        // TODO Auto-generated method stub
        Intent intent = new Intent();
        intent.setAction(Util.ACTION_INSTALL_SHORTCUT);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "一键加速");
        intent.putExtra("duplicate", false);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.short_cut_icon));
        Intent i = new Intent();
        i.setAction("com.yzy.shortcut");
        i.addCategory("android.intent.category.DEFAULT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
        sendBroadcast(intent);

        SharedPreferencesUtils.setIsShortCut(mContext, true);
    }

    private void initAnim() {
        mCurrentTempTime = System.currentTimeMillis();
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_in);
        mFadeIn.setDuration(50);
        mFadeInScale = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_in_scale);
        mFadeInScale.setDuration(20);
        mFadeInScale.setFillAfter(true);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.welcome_fade_out);
        mFadeOut.setDuration(50);
        mFadeOut.setFillAfter(true);
        mImageView.startAnimation(mFadeIn);
    }


    /**
     * 监听事件
     */
    public void setListener() {
        /**
         * 动画切换原理:开始时是用第一个渐现动画,当第一个动画结束时开始第二个放大动画,当第二个动画结束时调用第三个渐隐动画,
         * 第三个动画结束时修改显示的内容并且重新调用第一个动画,从而达到循环效果
         */
        mFadeIn.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(mFadeInScale);
                Log.e("hmq","1="+(System.currentTimeMillis() - mCurrentTempTime));
                mCurrentTempTime = System.currentTimeMillis();
            }
        });
        mFadeInScale.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                mImageView.startAnimation(mFadeOut);
                Log.e("hmq","2="+(System.currentTimeMillis() - mCurrentTempTime));
                mCurrentTempTime = System.currentTimeMillis();
            }
        });
        mFadeOut.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationRepeat(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                startActivity(CleanerManager.class);
                Log.e("hmq","3="+(System.currentTimeMillis() - mCurrentTempTime));
                finish();
            }
        });
    }

    private void initData(){
        FileClean.getInstance(this);
    }
}
