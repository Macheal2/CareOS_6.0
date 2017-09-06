package com.cappu.readme.view;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.cappu.readme.ContentListActivity;
import com.cappu.readme.FullscreenActivity;
import com.cappu.readme.R;
import com.cappu.readme.ReadMeConfigure;
import com.cappu.readme.ReadMeMainActivity;
import com.cappu.readme.util.DensityUtil;


public class LancherLayout extends LinearLayout implements ShadowCallBack, View.OnClickListener{

	private final static String TAG = "LancherLayout";

	private Context mContext;

	private ImageView[] mItemViews = new ImageView[5];
    private OnClickListener mClickListener;

	public LancherLayout(Context context) {
		this(context, null);

	}

    public LancherLayout(Context context , AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lancher, null);
        view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(view);
		initView();
    }

	public void initListener(OnClickListener listener) {
		this.mClickListener = listener;
	}

	/**
	 * init views
	 */
	public void initView() {

		this.mItemViews[0] = ((ImageView) findViewById(R.id.user_manual));
		this.mItemViews[1] = ((ImageView) findViewById(R.id.weixin));
		this.mItemViews[2] = ((ImageView) findViewById(R.id.media_manager));
		this.mItemViews[3] = ((ImageView) findViewById(R.id.faq));
		//this.mItemViews[4] = ((ImageView) findViewById(R.id.game1));

		for (int j = 0; j < 4; j++) {
			mItemViews[j].setOnClickListener(this);
		}
	}

	public void onClick(View paramView) {

		if (mClickListener != null) {
			mClickListener.onClick(paramView);
        }

        Log.d(TAG,"OnClickListener");
        Intent intent=new Intent();

        switch (paramView.getId()) {
            case R.id.user_manual:
                intent.putExtra("which_list", ReadMeConfigure.USER_MANUAL);
                intent.setClass(mContext,ContentListActivity.class);
                mContext.startActivity(intent);

                break;
            case R.id.weixin: //weixin
                intent.putExtra("which_list", ReadMeConfigure.WEI_XIN);
                intent.setClass(mContext,ContentListActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.media_manager: //copy songs
                intent.putExtra("which_list", ReadMeConfigure.MEDIA_MANAGER);
                intent.setClass(mContext,FullscreenActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.faq:
                intent.putExtra("which_list", ReadMeConfigure.FAQ);
                intent.setClass(mContext,FullscreenActivity.class);
                mContext.startActivity(intent);
                break;
            /*
            case R.id.game1://common issues
                i = 3;
                intent.putExtra("which_list", 3);
                intent.setClass(mContext,FullscreenActivity.class);
                mContext.startActivity(intent);
                break;*/
        }

	}

    @Override
    public void initListener() {
        // TODO Auto-generated method stub
	}

    @Override
    public void destroy(){
        // TODO Auto-generated method stub
    }

	@Override
	public void updateData() {
		// TODO Auto-generated method stub
	}

}