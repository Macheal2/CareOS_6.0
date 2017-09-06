package com.cappu.keyguard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.keyguard.R;
import android.os.Handler.Callback;
import android.provider.Settings;

import com.android.keyguard.KeyguardUpdateMonitor;
import com.cappu.pictorial.CappuPictorialTool;
import com.cappu.pictorial.ICappuPictorial;

public class CappuLockScreen extends CappuLockBaseView {
    public RelativeLayout subRootView;
    private Callback mainHandler = null;
    private Context mContext;
    private CappuLockScreenView mCappuLockScreenView;
    private ICappuPictorial mIsService;//by hmq 20161108 Modify for cappu pictorial
    
    public CappuLockScreen(Context context) {
        super(context);
        mContext = context;
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View viewRoot = inflater.inflate(R.layout.cappu_lock_screen, this, true);//dengying@20160822 lockscreen
        mCappuLockScreenView = (CappuLockScreenView) viewRoot.findViewById(R.id.cappu_lock_layout);
		if(mCappuLockScreenView != null){
		    mCappuLockScreenView.setSubHandler(mViewSubCallback);
		}
        
        /* begin: edit by hmq 20161108 Modify for cappu pictorial */
        if (SystemProperties.getInt("ro.com.cappu.keyguard.pictorial", 0) == 1) {
            Log.e("hmq", "CappuLockscreen ro.com.cappu.keyguard.pictorial= 1");
            mIsService = CappuPictorialTool.getCappuPictorialService();
            if (mIsService == null) {
                mIsService = CappuPictorialTool.setBindService(mContext);
            }
            Log.e("hmq", "CappuLockscreen mIsService?=null " + (mIsService == null));
            int mode = Settings.System.getInt(context.getContentResolver(), Settings.System.CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL, Settings.System.CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL_ON);
            
            if (mode == Settings.System.CAPPU_SETTINGS_SYSTEM_AUTO_PICTORIAL_ON) {
                try {
                    Log.e("hmq", "CappuLockscreen connection mIsService != null");
                    if (mIsService != null) {
                        // 调用远程服务中的方法
                        long currentTime = System.currentTimeMillis();
                        String path = mIsService.getCappuPictorialPath();
                        Log.e("hmq", "CappuLockscreen connection path=" + path);
                        Bitmap bp = CappuPictorialTool.getDiskBitmap(path);
                        Log.e("hmq", "CappuLockscreen connection bp?=null" + (bp == null));
                        if (bp != null) {
                            mCappuLockScreenView.setBackground(CappuPictorialTool.bitmap2Drawable(bp));
                        }
                        Log.e("hmq", "CappuLockscreen connection overtime=" + (System.currentTimeMillis() - currentTime));
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    mCappuLockScreenView.setBackgroundResource(R.drawable.cappu_default_wallpaper2);
                    e.printStackTrace();
                    Log.e("hmq", "CappuLockscreen connection error=" + e.getMessage());
                }
            }else{
                mCappuLockScreenView.setBackgroundResource(R.drawable.cappu_default_wallpaper2);
            }
        }else{
            Log.e("hmq", "CappuLockscreen  ro.com.cappu.keyguard.pictorial= 0");
            mCappuLockScreenView.setBackgroundResource(R.drawable.cappu_default_wallpaper2);
            
        }
        /* end: edit by hmq 20161108 Modify for cappu pictorial */
    }

    @Override
    protected void onTimeChanged(String dateFormat){
        // TODO Auto-generated method stub
        super.onTimeChanged(dateFormat);

		if(mCappuLockScreenView != null){
		    mCappuLockScreenView.setChangedTime(dateFormat);
		}
    }    
    
    @Override
    protected void onRefreshCarrierInfo(int maxSimId, int simId, String simInfo, String defaultInfo) {
        super.onRefreshCarrierInfo(maxSimId, simId, simInfo, defaultInfo);       
    }

    @Override
    protected void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status, int level) {
        // TODO Auto-generated method stub
        super.onRefreshBatteryInfo(status, level);  
        if(mCappuLockScreenView != null){
		    mCappuLockScreenView.setBatteryChange(status, level);
		}     
    }  
    
    @Override
    protected void onResume(int reason) {
        // TODO Auto-generated method stub
        super.onResume(reason);
    }   

    public Handler mViewSubCallback =new Handler (){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Intent intent = null;
            switch (msg.what) {
            case CAPPU_STATUS_UNLOCK:
            case CAPPU_STATUS_DIAL:
            case CAPPU_STATUS_MSG:
            case CAPPU_STATUS_SCREENON:
                gotoIntent(msg.what);
                break;
            case CAPPU_STATUS_HIDE_VIEW:
                //hideDispInfo();
                break;
            case CAPPU_STATUS_SHOW_VIEW:
                //showDispInfo();
                break;
            default:
                break;
            }
        }
    };

    public void gotoIntent(int msg){
        Message m = new Message();
        m.what = msg;
        mainHandler.handleMessage(m);
    } 
    
    public void setMainHandler(Callback handler){
        mainHandler = handler;
    } 

    public void reflashPhoneUnread(boolean visible, boolean savedMode){
        if(mCappuLockScreenView != null){
		    mCappuLockScreenView.reflashPhoneUnread(visible, savedMode);
		} 
    }

    public void reflashUnreadMessage(boolean visible, boolean savedMode){
        if(mCappuLockScreenView != null){
		    mCappuLockScreenView.reflashUnreadMessage(visible, savedMode);
		}
    }
}
