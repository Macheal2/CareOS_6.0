
package com.cappu.launcherwin;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.widget.CareDialog;
import com.cappu.launcherwin.widget.LauncherLog;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

public class Hotseat extends LinearLayout implements OnClickListener,OnTouchListener{
    private static final String TAG = "Hotseat";
    private Context mContext;
    private Launcher mLauncher;
    public ImageView imgvLeft;
    public ImageView imgvMiddle;
    private ImageView imgvRight;
    private LinearLayout lyLeft;
    private LinearLayout lyMiddle;
    private LinearLayout lyRight;
    
    private int unreadPhone=0;
    private int unreadMms=0;
    private Bitmap bmpPhone;
    private Bitmap bmpPressPhone;
    private Bitmap bmpMms;
    private Bitmap bmpPressMms;
    
    private Bitmap bmpPhoneTag;
    private Bitmap bmpPressPhoneTag;
    private Bitmap bmpMmsTag;
    private Bitmap bmpPressMmsTag;
    public Hotseat(Context context) {
    	super(context);
    }

    public Hotseat(Context context, AttributeSet attrs) {
    	super(context, attrs);
        this.mContext = context;
        if(context instanceof Launcher){
            this.mLauncher = (Launcher) context;
        }
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        if(this.mContext == null){
            this.mContext = getContext();
        }
    }
    private void updateImgv(LinearLayout mLinearLayout,int gravity,ImageView mImageView, int size){
    	mLinearLayout.setGravity(gravity);
    	LinearLayout.LayoutParams imgvLayoutParams=new LinearLayout.LayoutParams(size, size);
    	imgvLayoutParams.height=imgvLayoutParams.width=size;
    	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_COLORFUL){
    		if(mImageView==imgvLeft){
    			imgvLayoutParams.leftMargin=12;
    			imgvLayoutParams.bottomMargin=16;
    		}else if(mImageView==imgvRight){
    			imgvLayoutParams.rightMargin=12;
    			imgvLayoutParams.bottomMargin=16;
    		}
    	}
    	mImageView.setLayoutParams(imgvLayoutParams);
    }
    public void updateHotseatIcon(){
    	imgvLeft=(ImageView)findViewById(R.id.imgv_left);
    	imgvMiddle=(ImageView)findViewById(R.id.imgv_middle);
    	imgvRight=(ImageView)findViewById(R.id.imgv_right);
    	lyLeft=(LinearLayout)findViewById(R.id.ly_left);
    	lyLeft.setOnClickListener(this);
    	lyMiddle=(LinearLayout)findViewById(R.id.ly_middle);
    	lyMiddle.setOnClickListener(this);
    	lyRight=(LinearLayout)findViewById(R.id.ly_right);
    	lyRight.setOnClickListener(this);
    	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
    		lyLeft.setOnTouchListener(this);
    		lyMiddle.setOnTouchListener(this);
    		lyRight.setOnTouchListener(this);
    		updateImgv(lyLeft,Gravity.CENTER,imgvLeft,150);
    		updateImgv(lyMiddle,Gravity.CENTER,imgvMiddle,150);
    		updateImgv(lyRight,Gravity.CENTER,imgvRight,150);
    		
    	}else if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_COLORFUL){
    		updateImgv(lyLeft,Gravity.BOTTOM,imgvLeft,LayoutParams.WRAP_CONTENT);
    		updateImgv(lyMiddle,Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,imgvMiddle,LayoutParams.WRAP_CONTENT);
    		updateImgv(lyRight,Gravity.RIGHT|Gravity.BOTTOM,imgvRight,LayoutParams.WRAP_CONTENT);
    		
    	}else{
    		lyLeft.setBackgroundResource(R.drawable.shape_transparent);
    		lyMiddle.setBackgroundResource(R.drawable.shape_transparent);
    		lyRight.setBackgroundResource(R.drawable.shape_transparent);
    		updateImgv(lyLeft,Gravity.CENTER,imgvLeft,LayoutParams.WRAP_CONTENT);
    		updateImgv(lyMiddle,Gravity.CENTER,imgvMiddle,LayoutParams.WRAP_CONTENT);
    		updateImgv(lyRight,Gravity.CENTER,imgvRight,LayoutParams.WRAP_CONTENT);
    	}
    	imgvLeft.setImageBitmap(ThemeManager.getInstance().getHotSeatIcon((String)imgvLeft.getTag()));
    	imgvMiddle.setImageBitmap(ThemeManager.getInstance().getHotSeatIcon((String)imgvMiddle.getTag()));
    	imgvRight.setImageBitmap(ThemeManager.getInstance().getHotSeatIcon((String)imgvRight.getTag()));
    	
    	unreadMms=mLauncher.getNewSmsCount()+mLauncher.getNewMmsCount();
    	unreadPhone=mLauncher.getUnreceivedCallCount();
    	bmpMms = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.hotseat_mms);
    	bmpPressMms = ThemeManager.getInstance().getUnreadIcon(
				BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.hotseat_mms), unreadMms);
    	bmpPhone = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.hotseat_iphone);
		bmpPressPhone = ThemeManager.getInstance().getUnreadIcon(
				BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.hotseat_iphone), unreadPhone);
		
		bmpMmsTag = ThemeManager.getInstance().getHotSeatIcon((String)imgvLeft.getTag());
    	bmpPressMmsTag = ThemeManager.getInstance().getUnreadIcon(
    			ThemeManager.getInstance().getHotSeatIcon((String)imgvLeft.getTag()), unreadMms);
    	bmpPhoneTag = ThemeManager.getInstance().getHotSeatIcon((String)imgvMiddle.getTag());
		bmpPressPhoneTag = ThemeManager.getInstance().getUnreadIcon(
				ThemeManager.getInstance().getHotSeatIcon((String)imgvMiddle.getTag()), unreadPhone);
    	
    }
    @Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
    	LauncherLog.v(TAG, "onTouch,jeff event.getAction()="+event.getAction());
    	if(ThemeManager.getInstance().getCurrentThemeType()!=ThemeManager.THEME_NINE_GRIDS){
    		return false;
    	}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(lyLeft==v){
				if(unreadMms>0){
					imgvLeft.setImageBitmap(bmpPressMms);
				}else{
					imgvLeft.setImageBitmap(bmpMms);
				}
			}else if(lyMiddle==v){
				if(unreadPhone>0){
					imgvMiddle.setImageBitmap(bmpPressPhone);
				}else{
					imgvMiddle.setImageBitmap(bmpPhone);
				}
			}else if(lyRight==v){
				imgvRight.setImageBitmap(BitmapFactory.decodeResource (mContext.getResources(), R.drawable.hotseat_camera));
			}
			break;
		case MotionEvent.ACTION_UP:
			if(lyLeft==v){
				if(unreadMms>0){
					imgvLeft.setImageBitmap(bmpPressMmsTag);
				}else{
					imgvLeft.setImageBitmap(bmpMmsTag);
				}
			}else if(lyMiddle==v){
				if(unreadPhone>0){
					imgvMiddle.setImageBitmap(bmpPressPhoneTag);
				}else{
					imgvMiddle.setImageBitmap(bmpPhoneTag);
				}
			}else if(lyRight==v){
				imgvRight.setImageBitmap(ThemeManager.getInstance().getHotSeatIcon((String)imgvRight.getTag()));
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if(lyLeft==v){
				if(unreadMms>0){
					imgvLeft.setImageBitmap(bmpPressMmsTag);
				}else{
					imgvLeft.setImageBitmap(bmpMmsTag);
				}
			}else if(lyMiddle==v){
				if(unreadPhone>0){
					imgvMiddle.setImageBitmap(bmpPressPhoneTag);
				}else{
					imgvMiddle.setImageBitmap(bmpPhoneTag);
				}
			}else if(lyRight==v){
				imgvRight.setImageBitmap(ThemeManager.getInstance().getHotSeatIcon((String)imgvRight.getTag()));
			}
			break;
		}

		return false;
	}
    @Override
    public void onClick(View v) {
            if(v==lyLeft){
				try {
					if (APKInstallTools.checkApkInstall(mContext,
							"com.android.mms",
							"com.android.mms.ui.BootActivity")) {
						Intent intent = new Intent();
						ComponentName cn = new ComponentName("com.android.mms",
								"com.android.mms.ui.BootActivity");
						intent.setComponent(cn);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_NO_ANIMATION);
						mLauncher.getSpeechTools().startSpeech(
								mContext.getString(R.string.launcher_mms),
								mLauncher.getSpeechStatus());
						mLauncher.startActivity(intent);
					} else {
						Intent it = new Intent(Intent.ACTION_MAIN);
						it.setType("vnd.android-dir/mms-sms");
						mLauncher.getSpeechTools().startSpeech(
								mContext.getString(R.string.launcher_mms),
								mLauncher.getSpeechStatus());
						mLauncher.startActivity(it);
					}
				} catch (Exception e) {
					Toast.makeText(mContext,
							mContext.getString(R.string.activity_not_found),
							Toast.LENGTH_LONG).show();
				}
			} else if (v==lyMiddle) {
				IntoDialog();
			} else if(v==lyRight){
                try {
                    Intent cameraIntent = new Intent();
                    cameraIntent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    mContext.startActivity(cameraIntent);
                    //START: added by Yar @20170815
                    mLauncher.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    //END: added by Yar @20170815
                } catch (Exception e) {
                    Log.i("Launcher", "cameraIntent Exception:"+e.toString());
                }
                mLauncher.getSpeechTools().startSpeech(mContext.getString(R.string.app_name_camera),mLauncher.getSpeechStatus());            
            } 
    }
    
	private void IntoDialog() {
		try {
			Intent intent = new Intent();
			ComponentName cn = new ComponentName("com.android.dialer",
					"com.android.dialer.calllog.CallLogActivity");
			intent.setComponent(cn);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			mContext.startActivity(intent);
			mLauncher.getSpeechTools().startSpeech(
					mContext.getString(R.string.launcher_phone),
					mLauncher.getSpeechStatus());
		} catch (Exception e) {
			Toast.makeText(mContext,
					mContext.getString(R.string.activity_not_found),
					Toast.LENGTH_LONG).show();
		}
	}

}
