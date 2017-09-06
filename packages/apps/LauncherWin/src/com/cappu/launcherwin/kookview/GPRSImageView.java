package com.cappu.launcherwin.kookview;

import java.lang.reflect.Method;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.I99ThemeToast;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.content.Intent;

public class GPRSImageView extends ImageView implements OnChildViewClick {

    private ConnectivityManager mCM;

    AnimationDrawable mAnimationDrawable;
    private TelephonyManager mTelephonyManager;// zhaogangzhu
    // dengying@20150408 wifi open gprs status update begin
    private MyHandler myHandler = new MyHandler();

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            if (msg.what == 1) {
                stopImageViewAnimation();
                updateDataConnStatus();
            }
        }
    }

    private class MyThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(6000);

                // 发送消息到handler
                Message mMsg = new Message();
                mMsg.what = 1;
                myHandler.sendMessage(mMsg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateDataConnStatus() {
        boolean isGprs = gprsIsOpenMethod("getMobileDataEnabled");// ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getMobileDataEnabled();
		if (ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE) {
			if (isGprs) {
				Log.i("GPRSImageView", "isGprs------THEME_CHINESESTYLE-----"
						+ isGprs);
				setImageResource(R.drawable.icon_3g_on_mode_5);
			} else {
				setImageResource(R.drawable.icon_3g_off_mode_5);
			}
		} else if (ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_NINE_GRIDS) {
			if (isGprs) {
				Log.i("GPRSImageView", "isGprs------THEME_NINE_GRIDS-----"
						+ isGprs);
				setImageResource(R.drawable.icon_3g_on_mode_9);
			} else {
				setImageResource(R.drawable.icon_3g_off_mode_9);
			}
		}else {
			Log.i("GPRSImageView", "isGprs------THEME-----" + isGprs);
			if (isGprs) {
				setImageResource(R.drawable.icon_3g_on_mode_4);
			} else {
				setImageResource(R.drawable.icon_3g_off_mode_4);
			}
		}
    }

    // dengying@20150408 wifi open gprs status update end

    public GPRSImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);// zhaogagnzhu
    }

    @Override
    public void onClick(Context context) {
        if (getPhoneNumber(getContext()) == null) {
            I99ThemeToast.toast(getContext(), getContext().getString(R.string.No_SIM_card), "l", Color.parseColor("#FFFFFF"));

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
                //modify by wangyang 2016.10.19 start
			if (ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE) {
				setImageResource(R.drawable.icon_3g_off_mode_5);
			}else if (ThemeManager.getInstance().getCurrentThemeType(
					getContext()) == ThemeManager.THEME_NINE_GRIDS) {
				setImageResource(R.drawable.icon_3g_off_mode_9);
			}else {
				setImageResource(R.drawable.icon_3g_off_mode_4);
			}
              //modify by wangyang 2016.10.19 end

            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
		if (ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE) {
			setImageResource(R.anim.gprs_animation_mode_five);
		}else if (ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_NINE_GRIDS) {
			setImageResource(R.anim.gprs_animation_mode_nine);
		} else {
			setImageResource(R.anim.gprs_animation_mode_four);
		}

        // dengying@20150408 wifi open gprs status update begin
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifiConn = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        // dengying@20150408 wifi open gprs status update end

        /* 版本选择 1 晨想老人机 2晨想老人机线上版本 3 派信老人机版本 */
        if (BasicKEY.LAUNCHER_VERSION == 2) {
            runImageViewAnimation();

            // dengying@20150408 wifi open gprs status update begin
            if (wifiConn) {
                new Thread(new MyThread()).start();
            }
            // dengying@20150408 wifi open gprs status update end
        }

        if (gprsIsOpenMethod("getMobileDataEnabled")) {
            // setGprsEnabled("setMobileDataEnabled", false);
            if (mTelephonyManager != null) {
                // mTelephonyManager.setDataEnabled(false);//zhaogagnzhu
                Intent intent = new Intent("android.net.conn.SETDATA_OFF");// zhaogangzhu@20150929
                context.sendBroadcast(intent);
            }
            runImageViewAnimation();

            // dengying@20150408 wifi open gprs status update begin
            if (wifiConn) {
                new Thread(new MyThread()).start();
            }
            // dengying@20150408 wifi open gprs status update end
        } else {
            // setGprsEnabled("setMobileDataEnabled", true);
            if (mTelephonyManager != null) {
                // mTelephonyManager.setDataEnabled(true);//zhaogagnzhu
                Intent intent = new Intent("android.net.conn.SETDATA_ON");// zhaogangzhu@20150929
                context.sendBroadcast(intent);
            }

        }
    }

    public void runImageViewAnimation() {
        /** 这里是状态是打开状态 要关闭的时候跑一个 */
        mAnimationDrawable = (AnimationDrawable) getDrawable();
        mAnimationDrawable.start();
    }

    // dengying@20150408 wifi open gprs status update begin
    public void stopImageViewAnimation() {
        if (mAnimationDrawable == null) {
             Drawable drawable = getDrawable();
            if(drawable instanceof AnimationDrawable){
                Log.i("GPRSImageView", "stopImageViewAnimation is AnimationDrawable");
                mAnimationDrawable = (AnimationDrawable) drawable;
            }else{
                Log.i("GPRSImageView", "stopImageViewAnimation is not  AnimationDrawable");
            }
        }
        if(mAnimationDrawable != null){
            mAnimationDrawable.stop();
        }
        
    }

    // dengying@20150408 wifi open gprs status update end

    /** 检测GPRS是否打开 */
    private boolean gprsIsOpenMethod(String methodName) {
        if (mCM == null) {
            mCM = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
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
        if (mCM == null) {
            mCM = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
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
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        String tel = tm.getLine1Number();
        return tel;
    }

}
