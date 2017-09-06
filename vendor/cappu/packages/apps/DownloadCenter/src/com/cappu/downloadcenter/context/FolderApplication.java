package com.cappu.downloadcenter.context;

import org.xutils.x;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.joy.network.impl.Service;
import com.joy.network.util.SystemInfo;
import com.cappu.downloadcenter.common.Constants;
import com.cappu.downloadcenter.common.utils.Util;
import com.cappu.downloadcenter.download.DownloadManager;

public class FolderApplication extends Application {
    public static Context mContext;
    public static String mConnectedType;
    public static final String NETWORK_TYPE_WIFI = "net_type_wifi";
    public static final String NETWORK_TYPE_MOBILE = "net_type_mobile";
    public static final String NETWORK_TYPE_NULL = "net_type_null";
    public static boolean onlyOneRefresh = false;
    public static int mIsChannel;
    public static final int LOCAL_CHANNEL = 0;
    public static final int NETWORK_CHANNEL = 1;
    public static final boolean XUTILS_DEBUG = false;

    
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);//xutils3 初始化
        x.Ext.setDebug(XUTILS_DEBUG); // 是否输出debug日志, 开启debug会影响性能.
        
        mContext = this;
        Service.getInstance(this, false);
        DownloadManager.getInstance();//下载数据库初始化
        
        SystemInfo.getInstance(Constants.DEFAULT_CHANNL, 0, false);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        setConnectedType(Util.getConnectedType(mContext));
        if (sp.getBoolean("isActive", false))
            new Thread() {
                public void run() {
                    if (!NETWORK_TYPE_NULL.equals(getConnectedType())) {
                        boolean isActive = Service.getInstance().activateLauncher();
                        if (isActive) {
                            sp.edit().putBoolean("isActive", true).commit();
                        }
                    }
                };
            }.start();
            
            if (Util.checkAppType(mContext, mContext.getPackageName()) == Constants.USER_APP){
                mIsChannel = NETWORK_CHANNEL;
            }else{
                mIsChannel = LOCAL_CHANNEL;
            }
            if(Util.DEBUG)Log.e(Util.TAG,"mIsChannel="+mIsChannel);
    }
    
    public static void setConnectedType(int type){
        mConnectedType = NETWORK_TYPE_NULL;
        if (type == ConnectivityManager.TYPE_MOBILE) {
            mConnectedType = NETWORK_TYPE_MOBILE;
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            mConnectedType = NETWORK_TYPE_WIFI;
            
        } else if (type == ConnectivityManager.TYPE_NONE) {
            mConnectedType = NETWORK_TYPE_NULL;
        }
        if(Util.DEBUG)Log.e(Util.TAG, "setConnectedType = "+mConnectedType+"; type="+type);
    }
    
    public String getConnectedType(){
        return mConnectedType;
    }
    
    public void changeNetwork(String type){
        if (!type.equals(mConnectedType)) {
            mConnectedType = type;
        }
    }
    
}
