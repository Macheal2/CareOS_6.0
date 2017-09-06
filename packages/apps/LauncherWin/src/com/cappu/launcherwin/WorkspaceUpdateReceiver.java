package com.cappu.launcherwin;

import java.util.Calendar;

import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.kookview.GPRSKookView;
import com.cappu.launcherwin.tools.TimeTools;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.kookview.AlbumsRelativeLayout;
import com.cappu.launcherwin.kookview.assembly.HealthLinearLayout;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class WorkspaceUpdateReceiver extends BroadcastReceiver {
    public static String TAG = "WorkspaceUpdateReceiver";
    
    WeatherTimeLayout mWeatherTimeLayout = null;
    
    private Context mLauncherContext;
    
    private final int VIEW_INIT = 0;
    private final int VIEW_REFRESH = 1;
    /**刷新新闻健康*/
    private final int VIEW_REFRESH_HORIZONTAL = 2;
    private final int VIEW_UPDATE_WEATHER = 3;
    private final int VIEW_UPDATE_NET_LOOK =4;
    private final int VIEW_DESTROY = 5;
    
    
    private final int CONNECTIVITY_CANCEL = 1;
    private final int CONNECTIVITY_CHANGEING = 2;
    private final int CONNECTIVITY_CHANGE = 3;
    private final int UPDATE_DATACONN_STATUS = 4;
    
    GPRSKookView mGPRSKookView;
    
    
    private long mCurrentMillis = 0;
    
    //hejianfeng add start
    private Launcher mLauncher;
    public void setLancher(Launcher launcher){
    	mLauncher=launcher;
    }
    //hejianfeng add end
    public CustomContentObserver mContentObserver = new CustomContentObserver();
    class CustomContentObserver extends ContentObserver {

        public CustomContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mCappuHandler.sendEmptyMessage(VIEW_UPDATE_NET_LOOK);
        }
    }
    
    
    public Handler mCappuHandler = new Handler(){
        @Override
            public void handleMessage(Message msg) {
            if(mWeatherTimeLayout == null){
                Log.i(TAG, " mCappuHandler mWeatherTimeLayout is null"+"    msg.what:"+msg.what);
                return;
            }
            
            Log.i(TAG, "    msg.what:"+msg.what);
            switch (msg.what) {
                case VIEW_INIT: 
                    break;
                case VIEW_REFRESH: 
                    viewRefresh();
                    break;
                case VIEW_REFRESH_HORIZONTAL: 
                    //mWeatherTimeLayout.mCappuHorizontalScrollView.refreshLayout(mWeatherTimeLayout.mNetDateDaoList);
                    break;
                case VIEW_UPDATE_WEATHER: 
//                    if(mWeatherTimeLayout.mWeatherInfo == null){
//                        mWeatherTimeLayout.updataWeathers(Calendar.getInstance());
//                    }
                    break;
                /**更新新闻健康*/
                case VIEW_UPDATE_NET_LOOK: 
                    if(mWeatherTimeLayout!=null){
                        mWeatherTimeLayout.updateNetLook();
                    }
                    break;
                    
                    //更新桌面轮播图 added by wangyang
                case VIEW_UPDATE_NET_ISHARE:
                    Log.i("wangwang","VIEW_UPDATE_NET_ISHARE             mAlbumsRelativeLayout = "+ (mAlbumsRelativeLayout == null));
				if (mAlbumsRelativeLayout != null) {
					if (APKInstallTools.checkApkInstall(mLauncherContext,
							"com.cappu.ishare",
							"com.cappu.ishare.ui.activitys.SplashActivity")) {
						mAlbumsRelativeLayout.updateNetLook(1);
						mAlbumsRelativeLayout.checkData();// hejianfeng add
					} else {
						mAlbumsRelativeLayout.updateNetLook(0);
					}
				}
                    break;
                case VIEW_DESTROY: 
                    break;
                
                default:
                    break;
            }
         }
     };
     
     private Handler mGprsViewHandler = new Handler(){
         @Override
             public void handleMessage(Message msg) {
             // 获得网络连接服务
             ConnectivityManager connManager = null;
             if(mLauncherContext != null){
                 connManager = (ConnectivityManager) mLauncherContext.getSystemService(Context.CONNECTIVITY_SERVICE);
             }else {
                 Log.i(TAG, "mLauncherContext is null");
                 return;
             }
             
             if(mGPRSKookView == null || mGPRSKookView.mImageView == null){
                 Log.i(TAG, "mGPRSKookView:"+(mGPRSKookView == null)+"    "+(mGPRSKookView.mImageView == null));
                 return;
             }
             
             boolean isGprs = mGPRSKookView.gprsIsOpenMethod("getMobileDataEnabled");
             
             boolean wifiConn = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();//dengying@20150408 wifi open gprs status update
             Log.i(TAG, "isGprs:"+isGprs+"    msg.what:"+msg.what+"  wifiConn="+wifiConn);
             if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
	             mLauncher.mSystemBarTintManager.setTintDrawable(new BitmapDrawable(
	 					ThemeManager.getInstance().getImagePiece().statusBarBmp));
             }
             switch (msg.what) {
                 case CONNECTIVITY_CANCEL:
                	 if(wifiConn){
                		 mGPRSKookView.updateDataConnStatus();
                	 }
                     break;
                 case CONNECTIVITY_CHANGEING: 
                     if (mGPRSKookView.mImageView != null) {
                             if(ThemeManager.getInstance().getCurrentThemeType(mLauncherContext) == ThemeManager.THEME_CHINESESTYLE){
                                 mGPRSKookView.mImageView.setImageResource(R.anim.gprs_animation_mode_five);
                             }else if(ThemeManager.getInstance().getCurrentThemeType(mLauncherContext) == ThemeManager.THEME_NINE_GRIDS){
                                 mGPRSKookView.mImageView.setImageResource(R.anim.gprs_animation_mode_nine);
                             }else{
                                 mGPRSKookView.mImageView.setImageResource(R.anim.gprs_animation_mode_four);
                             }
                             mGPRSKookView.mImageView.runImageViewAnimation();
                         
                         if(wifiConn){
                             Log.i(TAG,"MyThread start");
                             this.sendEmptyMessageDelayed(UPDATE_DATACONN_STATUS, 6000);
                         }
                     }
                     break;
                 case CONNECTIVITY_CHANGE:
                	 if(mGPRSKookView != null) {
                         if (mGPRSKookView.mImageView != null && isGprs) {
                             mGPRSKookView.mImageView.stopImageViewAnimation();
                         }
                         mGPRSKookView.updateDataConnStatus();
                     }
                     break;
                 case UPDATE_DATACONN_STATUS:
                     if(mGPRSKookView != null) {//msg.what==1
                         if (mGPRSKookView.mImageView != null) {
                             mGPRSKookView.mImageView.stopImageViewAnimation();
                         }
                         mGPRSKookView.updateDataConnStatus();
                     }
                 default:
                     break;
             }
          }
      };
    //hejianfeng add start
	private HealthLinearLayout mHealthLinearLayout;
	public void initHealthLayout(HealthLinearLayout healthLinearLayout){
		mHealthLinearLayout=healthLinearLayout;
		mHealthLinearLayout.updataHealthData();
	}
    //hejianfeng add end
     public void registerContentObserver(Context context){
         context.getContentResolver().registerContentObserver(getNetUri(), true, mContentObserver);
     }
     
     public void unRregisterContentObserver(Context context){
         context.getContentResolver().unregisterContentObserver(mContentObserver);
     }
    
    public void initWeatherTimeLayout(WeatherTimeLayout weatherTimeLayout){
        if(mWeatherTimeLayout != null){
            viewDestroy();
        }
        mWeatherTimeLayout = weatherTimeLayout;
        
        if(mWeatherTimeLayout.mTimeTools == null){
            mWeatherTimeLayout.mTimeTools = new TimeTools();
        }
        
        if(mWeatherTimeLayout!=null){
            viewRefresh();
            mWeatherTimeLayout.updateNetLook();
            mWeatherTimeLayout.updataWeathers();//hejianfeng add
        }
    }
    
    public void initGPRSKookView(Context context,GPRSKookView kookView){
        mLauncherContext = context;
        mGPRSKookView = kookView;
    }
    
    public Uri getNetUri(){
        return Uri.parse( "content://com.cappu.download/downloadText");
    }
    
    
    public void viewDestroy() {
        mWeatherTimeLayout.deleteAllView(mWeatherTimeLayout);
        mWeatherTimeLayout.removeAllViewsInLayout();
        mWeatherTimeLayout.removeAllViews();
//        mWeatherTimeLayout = null;
    }
    
    public void gprsViewDestroy(){
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive is :"+action);
        if(mGPRSKookView != null){
            mGPRSKookView.gprsIsOpenMethod("getMobileDataEnabled");
        }
        
        if (action.equals(Intent.ACTION_TIME_TICK) || action.equals(Intent.ACTION_TIME_CHANGED)) {
          //每过一分钟 触发
            mCappuHandler.sendEmptyMessage(VIEW_REFRESH);
//            mCappuHandler.sendEmptyMessage(VIEW_UPDATE_NET_LOOK);//hejianfeng delete
        } else if(action.equals(Intent.ACTION_TIME_CHANGED)){
            mCappuHandler.sendEmptyMessage(VIEW_REFRESH);
            mCappuHandler.sendEmptyMessage(VIEW_UPDATE_NET_LOOK);
        } 
		else if("android.net.conn.CONNECTIVITY_CANCEL".equals(action)){
            mGprsViewHandler.sendEmptyMessage(CONNECTIVITY_CANCEL);
			//Log.i("zhaogangzhu","CONNECTIVITY_CANCEL intent on--->off");
        }else if ("android.net.conn.CONNECTIVITY_CHANGEING".equals(action)) {
            mGprsViewHandler.sendEmptyMessage(CONNECTIVITY_CHANGEING);
        }else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            mGprsViewHandler.sendEmptyMessage(CONNECTIVITY_CHANGE);
        }else if (action.equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            Log.i("qqqqqqqqqqqqqqqqqq", "安装了:" +packageName + "包名的程序");
            mCappuHandler.sendEmptyMessage(VIEW_UPDATE_NET_ISHARE);
        }else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            Log.i("qqqqqqqqqqqqqqqqqq", "卸载了:" +packageName + "包名的程序");
            mCappuHandler.sendEmptyMessage(VIEW_UPDATE_NET_ISHARE);
        }
        //hejianfeng add start
        else if(action.equals("android.intent.action.WALLPAPER_CHANGED")){
        	int currentPosition=Settings.Global.getInt(context.getContentResolver(), "current_theme_bg", 0);
        	if(currentPosition==-1){
        		mLauncher.changeWallpaper();
        	}
        }else if(action.equals("com.cappu.weather.action.datarefresh")){
        	if(mWeatherTimeLayout!=null){
        		mWeatherTimeLayout.updataWeathers();//hejianfeng add
        	}
        }else if(action.equals("com.cappu.healthcenter.action.datarefresh")){
        	if(mHealthLinearLayout!=null){
        		mHealthLinearLayout.updataHealthData();
        	}
        }else if(action.equals("com.cappu.healthcenter.action.step_count_refresh")){
        	int step=intent.getIntExtra("step_count", 0);
        	if(mHealthLinearLayout!=null){
        		mHealthLinearLayout.setStepTxt(step+"");
        	}
        }
        //hejianfeng add end
    }
    
    
    public void viewRefresh(){
        Calendar calendar = Calendar.getInstance();
        if(mWeatherTimeLayout != null){
            if(mWeatherTimeLayout.mTimeTools == null){
                mWeatherTimeLayout.mTimeTools = new TimeTools();
            }
            Log.i(TAG, "calendar 当前毫秒数:"+calendar.getTimeInMillis() +"        mCurrentMillis:"+mCurrentMillis+"        (calendar.getTimeInMillis() - mCurrentMillis) > 60*1000   "+((calendar.getTimeInMillis() - mCurrentMillis) > 60*1000));
          //每过一分钟 触发
            mWeatherTimeLayout.setTimeLayout(calendar);
            if((calendar.getTimeInMillis() - mCurrentMillis) > 60*1000){//意思是当前时间与上一次播报时间相差一份钟以上才开始播报
//                mWeatherTimeLayout.weatherSpeech(calendar);
                mCurrentMillis =  calendar.getTimeInMillis();
            }
            
//            mWeatherTimeLayout.updataWeathers(calendar);
            mWeatherTimeLayout.updateTimeDate();
        }
    }
    
    //added by wangyang 2016.8.8 (started)
    public void registerIShareContentObserver(Context context){
        context.getContentResolver().registerContentObserver(getIShareUri(), true, mIShareContentObserver);
    }
    
    private Uri getIShareUri(){
        return Uri.parse("content://com.cappu.ishare.authority/ShareBean");
    }
    
    IShareContentObserver mIShareContentObserver = new IShareContentObserver();
    
    public final int VIEW_UPDATE_NET_ISHARE = 6;
    
    private class IShareContentObserver extends ContentObserver{

        public IShareContentObserver() {
            super(new Handler());
        }
        
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mCappuHandler.sendEmptyMessage(VIEW_UPDATE_NET_ISHARE);
            Log.i("wangwang","onChange            改变了");
        }
    }
    
    private AlbumsRelativeLayout mAlbumsRelativeLayout;
    
    public void initAlbumsRelativeLayout(AlbumsRelativeLayout mAlbumsRelativeLayout){
        this.mAlbumsRelativeLayout = mAlbumsRelativeLayout;
    }
    
    public void unRregisterIShareContentObserver(Context context){
        context.getContentResolver().unregisterContentObserver(mIShareContentObserver);
    }
    
  //added by wangyang 2016.8.8 (ended)
}
