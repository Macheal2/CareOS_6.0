package com.cappu.launcherwin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.cappu.launcherwin.LauncherSettings.Favorites;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;
import com.cappu.launcherwin.downloadUI.celllayout.CellLyouatUtil;
import com.cappu.launcherwin.kookview.assembly.ClassRoomManager;
import com.cappu.launcherwin.nativejni.KookNativeChannel;

import com.cappu.launcherwin.tools.AppComponentNameReplace;
import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.I99ThemeToast;
import com.cappu.launcherwin.widget.LauncherLog;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.kook.exception.util.CrashHandler;

public class LauncherApplication extends Application {
    public static final String CappuDate = "data/data/com.cappu.launcherwin/cappu/";
    public static final String CappuThemes = "data/data/com.cappu.launcherwin/cappu/themes/";
    public static final String CappuPackage = "com.cappu.launcherwin";
    
    public LauncherModel mModel;
    public ThemeTools mThemeTools;
    
    public static final String TAG = "LauncherApplication";
    public Context mContext;
    
    /*天气*/
    private LauncherApplication mApplication;
    public LocationClient mLocationClient; 
    /*天气 end*/
    
    /**默认celllayout*/
    CellLyouatUtil mCellLyouatUtil;
    
    /**信息*/
    public static final int MSG_SHOW_TRANSIENTLY_FAILED_NOTIFICATION = 2;
    public static final int MSG_DONE = 4;
    public static final int EVENT_QUIT = 100;

    /**信息 end*/

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mApplication = this;
        //解析assets资源
        CPaseetsFile();
        ThemeManager.init(getApplicationContext());
        ThemeRes.init(getApplicationContext());
        ClassRoomManager.init(getApplicationContext());//hejianfeng add
        mModel = new LauncherModel(this,mThemeTools);
        
        // Register intent receivers
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);
        mCellLyouatUtil = new CellLyouatUtil(this);
        initSystemInfo();
        initData();
    }
    
    public final class ToastHandler extends Handler {
        public ToastHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Toast Handler handleMessage :" + msg);

            switch (msg.what) {
                case EVENT_QUIT: {
                    Log.d(TAG, "EVENT_QUIT");
                    getLooper().quit();
                    return;
                }

                case MSG_SHOW_TRANSIENTLY_FAILED_NOTIFICATION: {
                    I99ThemeToast.makeText(mApplication, R.string.transmission_transiently_failed, Toast.LENGTH_LONG);
                    break;
                }

                case MSG_DONE: {
                    I99ThemeToast.makeText(mApplication, R.string.care_sms_successed, Toast.LENGTH_SHORT);
                    break;
                }
            }
        }
    }
    /*信息 end*/
    
    /**让系统默认中文*/
    public void setdefaultLanguage() {
        String languageToLoad = "zh";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        config.locale = Locale.SIMPLIFIED_CHINESE;
        getResources().updateConfiguration(config, metrics);
    }
    
    public Context getContext(){
        if(mContext == null){
            mContext = mApplication;
        }
        return mContext;
    }

    public void initSystemInfo() {
        String channel = "8e1302d3bf043608e975e48504a3668b";//KookNativeChannel.GetChannel(mContext);
    }
    
    /**
     * There's no guarantee that this function is ever called.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e(TAG, "onTerminate");
    }
    
    LauncherModel setLauncher(Launcher launcher) {
        mModel.initialize(launcher);
        return mModel;
    }
    
    public ThemeTools getThemeTools(){
        return mThemeTools;
    }
    
    
    public CellLyouatUtil getCellLyouatUtil(){
        return mCellLyouatUtil;
    }

    LauncherModel getModel() {
        return mModel;
    }
    
    
    
    private void CPaseetsFile(){
        AssetManager mAssetManager = null;
        File dirFile = new File(CappuDate);
        if(!dirFile.exists()){
            dirFile.mkdir();
        }
        
        try {
            // 得到资源
            mAssetManager = getContext().getAssets();
            String[]  flLists= this.getAssets().list("");
            for (int i = 0; i < flLists.length; i++) {
                File jhPath = new File(CappuDate + flLists[i]);//
                if("calendardata".equals(flLists[i])){
                    CPFile(mAssetManager, flLists[i], jhPath);
                }else if(flLists[i].equals("theme")){
                    if(KookSharedPreferences.getString(getContext(), "theme") == null){
                        boolean isExist = CPFile(mAssetManager, flLists[i], jhPath);
                        if(isExist){
                            KookSharedPreferences.putString(getContext(), ThemeRes.THEMESEXIST, "theme");
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.i(TAG,"CPaseetsFile :"+ e.toString());
        }
    }
    
    private boolean CPFile(AssetManager mAssetManager,String FileName,File file){
        try {
            Log.i(TAG, "    FileName :" + FileName+"     LAUNCHER_VERSION:"+BasicKEY.LAUNCHER_VERSION);
            // 得到数据库的输入流
               InputStream is = mAssetManager.open(FileName);
               // 用输出流写到data/data/com.cappu.launcherwin/cappu/  应用下面
               FileOutputStream fos = new FileOutputStream(file);
               // 创建byte数组 用于1KB写一次
               byte[] buffer = new byte[1024];
               int count = 0;
               while ((count = is.read(buffer)) > 0) {
                   fos.write(buffer, 0, count);
               }
               // 最后关闭就可以了
               fos.flush();
               fos.close();
               is.close();
               return true;
        } catch (Exception e) {
            Log.i(TAG, "    CPFile :" + e.toString());
            return false;
        }
    }
    /*天气*/
    private LocationClientOption getLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1000);//设置发起定位请求的间隔时间为1000ms
	//option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setIsNeedAddress(true);//返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
        return option;
    }

    public void initData() {
        mLocationClient = new LocationClient(this.getApplicationContext());//声明LocationClient类
        mLocationClient.setLocOption(getLocationClientOption());

    }

 
    public synchronized LocationClient getLocationClient() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this.getApplicationContext()); // 声明LocationClient类
            mLocationClient.setLocOption(getLocationClientOption());
        }
        return mLocationClient;
    }
    /*天气 end*/
}
