package com.cappu.launcherwin.kookview;

import java.lang.reflect.Method;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.WorkspaceUpdateReceiver;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.I99ThemeToast;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GPRSKookView {
    private ConnectivityManager mCM;
    private Context mContext;
    public GPRSImageView mImageView;

    AnimationDrawable mAnimationDrawable;

    public void updateDataConnStatus() {
        boolean isGprs = gprsIsOpenMethod("getMobileDataEnabled");// ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getMobileDataEnabled();

		if (ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE) {
			if (isGprs) {
				mImageView.setImageResource(R.drawable.icon_3g_on_mode_5);
			} else {
				mImageView.setImageResource(R.drawable.icon_3g_off_mode_5);
			}
		} else if (ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_NINE_GRIDS) {
			if (isGprs) {
				mImageView.setImageResource(R.drawable.icon_3g_on_mode_9);
			} else {
				mImageView.setImageResource(R.drawable.icon_3g_off_mode_9);
			}
		}else {
			if (isGprs) {
				mImageView.setImageResource(R.drawable.icon_3g_on_mode_4);
			} else {
				mImageView.setImageResource(R.drawable.icon_3g_off_mode_4);
			}
		}
    }

    public View getView(final Context context, WorkspaceUpdateReceiver mWorkspaceUpdateReceiver) {
        Log.i("wangyangshousiba", "laorenjilaorenjilaorenjilaorenjilaorenjilaorenji      ");
        this.mContext = context;
        if (mCM == null) {
            mCM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.gprs_widget_layout, null);

        mImageView = (GPRSImageView) view.findViewById(R.id.img);

            if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){
                Log.i("GPRSKookView", "gprs_animation_mode_five------------"+ThemeManager.getInstance().getCurrentThemeType(mContext));
                mImageView.setScaleType(ImageView.ScaleType.CENTER);//hejianfeng add
                mImageView.setImageResource(R.anim.gprs_animation_mode_five);
                if (gprsIsOpenMethod("getMobileDataEnabled")) {
                    mImageView.setImageResource(R.drawable.icon_3g_on_mode_5);
                } else {
                    mImageView.setImageResource(R.drawable.icon_3g_off_mode_5);
                }
            }else if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_NINE_GRIDS){
                mImageView.setImageResource(R.anim.gprs_animation_mode_nine);
                if (gprsIsOpenMethod("getMobileDataEnabled")) {
                    mImageView.setImageResource(R.drawable.icon_3g_on_mode_9);
                } else {
                    mImageView.setImageResource(R.drawable.icon_3g_off_mode_9);
                }
            }else{
                Log.i("GPRSKookView", "gprs_animation_mode_four------------"+ThemeManager.getInstance().getCurrentThemeType(mContext));
                mImageView.setImageResource(R.anim.gprs_animation_mode_four);
                if (gprsIsOpenMethod("getMobileDataEnabled")) {
                    mImageView.setImageResource(R.drawable.icon_3g_on_mode_4);
                } else {
                    mImageView.setImageResource(R.drawable.icon_3g_off_mode_4);
                }
            }

        mWorkspaceUpdateReceiver.initGPRSKookView(context, this);
        Log.i("wangyangshousiba", "laorenjilaorenjilaorenjilaorenjilaorenjilaorenji1111111111111");
        return mImageView;
    }

    /** 检测GPRS是否打开 */
    public boolean gprsIsOpenMethod(String methodName) {
        Class cmClass = mCM.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;

        Boolean isOpen = false;
        try {
            Method method = cmClass.getMethod(methodName, argClasses);

            isOpen = (Boolean) method.invoke(mCM, argObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOpen;
    }

    // 打开或关闭GPRS
    private boolean gprsEnabled(boolean bEnable) {
        Object[] argObjects = null;

        boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
        if (isOpen == !bEnable) {
            setGprsEnabled("setMobileDataEnabled", bEnable);
        }

        return isOpen;
    }

    // 开启/关闭GPRS
    private void setGprsEnabled(String methodName, boolean isEnable) {
        Class cmClass = mCM.getClass();
        Class[] argClasses = new Class[1];
        argClasses[0] = boolean.class;

        try {
            Method method = cmClass.getMethod(methodName, argClasses);
            method.invoke(mCM, isEnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取手机号 */
    public String getPhoneNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        String tel = tm.getLine1Number();
        return tel;
    }
}
