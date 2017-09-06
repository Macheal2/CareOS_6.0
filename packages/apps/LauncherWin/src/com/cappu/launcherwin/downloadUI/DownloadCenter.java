package com.cappu.launcherwin.downloadUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.downloadapk.FileDownloader;
import com.cappu.launcherwin.downloadapk.services.FileService;
import com.cappu.launcherwin.downloadapk.services.KookLocalService;
import com.cappu.launcherwin.downloadapk.services.KookLocalService.DownloadTask;
import com.cappu.launcherwin.downloadapk.services.KookLocalService.LocalBinder;
import com.cappu.launcherwin.install.APKInstallTools;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.KookProgress;
import com.cappu.launcherwin.widget.TopBar;


public class DownloadCenter extends BasicActivity {
    /**这个日期格式存在是为里让每天最多刷新一次网络的xml文件*/
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Calendar mDummyDate;
    private static String ToDayIsDownload = "to_day_download";
    
    private File ReadmeFile;
    
    private ListView mListView;
    private KookProgress mKookProgress;
    private TextView mTipText;
    Context TextContext;
    int mTextSize = 34;
    
    AppAdapter mAppAdapter;
    
    /**这个是下载页面点击下载储存的应用*/
    private List<String> mDownloadAppName;
    /**这个是下载的services返回回来的下载应用名字*/
    private List<String> mDownloadAppNameServices = null;
    
    private List<AppInfo> mMapAppInfo;
    
    public static final String DOWNLOAD_HEAD = "http://app.careos.cn/download/";
    
    public static final String DOWNLOAD_DIR = "/.Mancomm";
    
    private String mAutoPackageName = null;
    private String mAutoClassName = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_center);
        mAutoPackageName = getIntent().getStringExtra("mAutoPackageName");
        mAutoClassName = getIntent().getStringExtra("mAutoClassName");
        init();
      //注册广播接收器
        mServiceDataReceiver=new ServiceDataReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
        registerReceiver(mServiceDataReceiver,filter);
        
/*        Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);*/
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAppAdapter != null){
            mAppAdapter.releaseBitmapCache();
        }
        
        unregisterReceiver(mServiceDataReceiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        File file = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/readme.xml");
        if(file.exists()){
            readConfig();
            Message msg = new Message();
            mUIConfigHander.sendMessage(msg);
        }
    }
    
    private ServiceDataReceiver mServiceDataReceiver;
    /**
     * 获取广播数据
     */
    public class ServiceDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String type = bundle.getString("type");
            String method = bundle.getString("method");
            if("xml".equals(type)){
                readConfig();
                Message msg = new Message();
                
                if("downloadFailed".equals(method)){
                    Toast.makeText(DownloadCenter.this, "亲，网络不给力，请检测网络", Toast.LENGTH_LONG).show();
                    msg.what = 5;
                    mUIHander.sendMessage(msg);
                }else{
                    msg.what = 2;
                    mUIHander.sendMessage(msg);
                }
            }else if("app".equals(type)){
                String appName = bundle.getString("appName");
                if(method.equals("onDownloadSize")){
                    int max = bundle.getInt("max");
                    int size = bundle.getInt("size");
                    String[] appNames = bundle.getStringArray("appNames");
                    for (int i = 0; i < appNames.length; i++) {
                        if(!mDownloadAppNameServices.contains(appNames[i])){//这里只负责向下载服务名里面添加
                            mDownloadAppNameServices.add(appNames[i]);
                        }
                    }
                    
                    Message msg = new Message();
                    msg.what = 1;
                    msg.getData().putInt("max", max);
                    msg.getData().putInt("size", size);
                    msg.getData().putString("appName", appName);
                    msg.getData().putStringArray("appNames", appNames);
                    mUIHander.sendMessage(msg);
                }else if(method.equals("downloadSucceed")){
                    if(mDownloadAppName.contains(appName)){//这里只负责删除名字
                        mDownloadAppName.remove(appName);
                    }
                    if(mDownloadAppNameServices.contains(appName)){//这里只负责删除名字
                        mDownloadAppNameServices.remove(appName);
                    }
                    Message msg = new Message();
                    msg.what = 3;
                    msg.getData().putString("appName", appName);
                    mUIHander.sendMessage(msg);
                    
                }else if(method.equals("downloadFailed")){
                    if(mDownloadAppName.contains(appName)){//这里只负责删除名字
                        mDownloadAppName.remove(appName);
                    }
                    if(mDownloadAppNameServices.contains(appName)){//这里只负责删除名字
                        mDownloadAppNameServices.remove(appName);
                    }
                    Message msg = new Message();
                    msg.what = 4;
                    msg.getData().putString("appName", appName);
                    mUIHander.sendMessage(msg);
                    Toast.makeText(DownloadCenter.this, "亲，下载失败、请更换网络环境", Toast.LENGTH_LONG).show();
                }else if(method.equals("networkLinkFailure")){
                    if(mDownloadAppName.contains(appName)){//这里只负责删除名字
                        mDownloadAppName.remove(appName);
                    }
                    if(mDownloadAppNameServices.contains(appName)){//这里只负责删除名字
                        mDownloadAppNameServices.remove(appName);
                    }
                    Message msg = new Message();
                    msg.what = 4;
                    msg.getData().putString("appName", appName);
                    mUIHander.sendMessage(msg);
                    Toast.makeText(DownloadCenter.this, "亲，网络不给力，请检测网络", Toast.LENGTH_LONG).show();
                    //mUIConfigHander.sendMessage(msg);
                }
                
            }else if("icon".equals(type)){
                String appName = bundle.getString("appName");
                Log.i("HHJ", "******************************************************** method:"+method+"    appName:"+appName);
                if("downloadSucceed".equals(method)){
                    mAppAdapter.notifyDataSetChanged();
                }
            }
        }
    }
    
    private UIHander mUIHander = new UIHander();
    private final class UIHander extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int size = msg.getData().getInt("size");
                    int max = msg.getData().getInt("max");
                    String appName = msg.getData().getString("appName");
                    AppAdapter appAdapter = (AppAdapter)mListView.getAdapter();
                    for (int i = 0; i < mListView.getChildCount(); i++) {
                        View view = mListView.getChildAt(i);
                        final ViewHolder viewHolder = (ViewHolder) view.getTag();
                        
                        if(appName.equals(viewHolder.mAppInfo.mAppName)){
                            if(viewHolder.mProgressBar.getProgress() <= 0 ){
                                viewHolder.mProgressBar.setMax(max);
                            }
                            viewHolder.mProgressBar.setProgress(size);
                            viewHolder.mAppInfo.setMaxProgress(max);
                            viewHolder.mAppInfo.setCurrentProgress(size);
                            
                            viewHolder.mButton.setVisibility(View.INVISIBLE);
                            viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                            viewHolder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                        }else if(mDownloadAppNameServices.contains(viewHolder.mAppInfo.mAppName)){
                            viewHolder.mButton.setVisibility(View.VISIBLE);
                            viewHolder.mButton.setText(R.string.wait);
                            viewHolder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                            viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                        }else{
                            viewHolder.mButton.setVisibility(View.VISIBLE);
                            viewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                    
                    break;
                    
                case 2:
                    if(mMapAppInfo == null){
                        return;
                    }
                    
                    if(mMapAppInfo.size()>0){
                        mListView.setVisibility(View.VISIBLE);
                    }
                    mListView.setAdapter(mAppAdapter);
                    mAppAdapter.setData(mMapAppInfo);
                    mAppAdapter.notifyDataSetChanged();
                    mKookProgress.setVisibility(View.GONE);
                    mTipText.setVisibility(View.GONE);
                    
                    if(mAutoPackageName != null && mAutoClassName !=null){//这里是进行自动下载/当进入下载中心有下载对象时
                        for (int i = 0; i < mMapAppInfo.size(); i++) {
                            AppInfo appInfo = mMapAppInfo.get(i);
                            if(mAutoPackageName.equals(appInfo.mPackageName) && mAutoClassName.equals(appInfo.mClassName)){
                                File file = new File(Environment.getExternalStorageDirectory()+DownloadCenter.DOWNLOAD_DIR+"/"+appInfo.mAppName);
                                if(file.exists()){
                                    Intent intent = new Intent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/"+appInfo.mAppName)), "application/vnd.android.package-archive");
                                    startActivity(intent);
                                }else{
                                    Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                                    intent.putExtra("types", "app");
                                    intent.putExtra("path", appInfo.mDownloadUrl);
                                    intent.putExtra("appName", appInfo.mAppName);
                                    intent.putExtra("saveDir", Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                                    startService(intent);
                                    mDownloadAppName.add(appInfo.mAppName);
                                    mListView.setSelection(i);
                                }
                                
                            }
                        }
                    }
                    break;

                case 3:
                    String appNames = msg.getData().getString("appName");
                    Log.i("HHJ", "下载完成appNames："+appNames);
                    if(mListView.getChildCount()>0){
                        mKookProgress.setVisibility(View.GONE);
                    }else {
                        mKookProgress.setVisibility(View.VISIBLE);
                    }
                    for (int i = 0; i < mListView.getChildCount(); i++) {
                        
                        View view = mListView.getChildAt(i);
                        final ViewHolder viewHolder = (ViewHolder) view.getTag();
                        
                        if(appNames.equals(viewHolder.mAppInfo.mAppName)){
                            viewHolder.mAppInfo.setCurrentProgress(0);
                            viewHolder.mAppInfo.setMaxProgress(0);
                            
                            viewHolder.mButton.setBackgroundResource(R.drawable.download_btn_selector);
                            viewHolder.mButton.setEnabled(true);
                            viewHolder.mButton.setTextColor(Color.parseColor("#FFFFFF"));
                            viewHolder.mButton.setText(getString(R.string.install));
                            viewHolder.mButton.setOnClickListener(new OnClickListener() {
                                
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction(android.content.Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/"+viewHolder.mAppInfo.mAppName)), "application/vnd.android.package-archive");
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                    break;
                case 4:
                    String appNamesF = msg.getData().getString("appName");
                    Log.i("HHJ", "更新一次  appNamesF:"+appNamesF);
                    if(appNamesF == null){
                        break;
                    }
                    for (int i = 0; i < mListView.getChildCount(); i++) {
                        View view = mListView.getChildAt(i);
                        final ViewHolder viewHolder = (ViewHolder) view.getTag();
    
                        if (appNamesF.equals(viewHolder.mAppInfo.mAppName)) {
    
                            viewHolder.mButton.setEnabled(true);
                            viewHolder.mButton.setBackgroundResource(R.drawable.download_btn_selector);
                            viewHolder.mButton.setTextColor(Color.parseColor("#FFFFFF"));
                            viewHolder.mButton.setText(getString(R.string.download));
                            viewHolder.mButton.setOnClickListener(new OnClickListener() {
    
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                                    intent.putExtra("types", "app");
                                    intent.putExtra("path", DOWNLOAD_HEAD + viewHolder.mAppInfo.mAppName);
                                    intent.putExtra("appName", viewHolder.mAppInfo.mAppName);
                                    intent.putExtra("saveDir", Environment.getExternalStorageDirectory() + DOWNLOAD_DIR);
                                    startService(intent);
                                    viewHolder.mButton.setEnabled(false);
                                    viewHolder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                                }
    
                            });
                        }
                    }
                    break;
                case 5:
                    if(mListView.getChildCount()>0){
                        break;
                    }
                    mKookProgress.setVisibility(View.GONE);
                    mTipText.setVisibility(View.VISIBLE);
                    mTipText.setText(getString(R.string.onclick_refresh));
                    mTipText.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            mTipText.setText(getString(R.string.get_net_config));
                            DownloadConfig();
                            mKookProgress.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
            }
        }
    }
    
    private void init(){
        mListView = (ListView) findViewById(R.id.download_list);
        mTipText = (TextView) findViewById(R.id.RefreshTxt);
        
        mKookProgress = (KookProgress) findViewById(R.id.RefreshTip);
        ReadmeFile = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/readme.xml");
        DownloadConfig();
        mDownloadAppName  = new ArrayList<String>();
        mDownloadAppNameServices  = new ArrayList<String>();
        mAppAdapter = new AppAdapter(this, mMapAppInfo, getPackageManager());
        if(ReadmeFile.exists()){
            readConfig();
            Message msg = new Message();
            mUIConfigHander.sendMessage(msg);
        }
    }
    
    
    private Handler mUIConfigHander = new UIConfigHander();

    private final class UIConfigHander extends Handler {
        public void handleMessage(Message msg) {
            if(mMapAppInfo.size()>0){
                mListView.setVisibility(View.VISIBLE);
            }
            mListView.setAdapter(mAppAdapter);
            mAppAdapter.setData(mMapAppInfo);
            mAppAdapter.notifyDataSetChanged();
            
            
            mKookProgress.setVisibility(View.GONE);
            mTipText.setVisibility(View.GONE);
        }
    }
    
    
    private void DownloadConfig(){
        String downloadpath = DOWNLOAD_HEAD+"/readme.xml";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            
            Calendar nowDate = Calendar.getInstance();
            mDummyDate = Calendar.getInstance();
            mDummyDate.setTimeZone(nowDate.getTimeZone());
            Date date = mDummyDate.getTime();
            String dateStr = dateFormat.format(date);
            
            //String dateExist = KookSharedPreferences.getString(this, ToDayIsDownload);
            String dateExist = Settings.Global.getString(getContentResolver(), ToDayIsDownload);
            if(dateExist == null || (!dateStr.equals(dateExist)) || !ReadmeFile.exists()){
              //File saveDir = getFilesDir();
                File saveDir = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                //Log.i("HHJ", "saveDir  1:"+saveDir.getPath() +"  2:"+Environment.getExternalStorageDirectory().getPath());
                download(downloadpath, saveDir);
                
                Settings.Global.putString(getContentResolver(), ToDayIsDownload, dateStr);
                //KookSharedPreferences.putString(this, ToDayIsDownload, dateStr);
            }
            
            Message msg = new Message();
            msg.what = 2;
            mUIHander.sendMessage(msg);
        } else {
            Toast.makeText(getApplicationContext(), R.string.sdcarderror, 1).show();
        }
    }
    
    KookLocalService mKookLocalService;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {//IBinder这个是KookLocalService里面的LocalBinder
            LocalBinder localBinder = (LocalBinder) service;
            mKookLocalService = localBinder.getService();
            //Log.i("HHJ", " onServiceConnected  onServiceConnected "+(mKookLocalService == null));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    
    /**
     * 
     * 1 下载URL
     * 2 下载保存路径
     * 
     * */
    private void download(String path, File saveDir) {// 运行在主线程
        
        Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
        intent.putExtra("types", "xml");
        intent.putExtra("path", path);
        intent.putExtra("saveDir", saveDir.getPath());
        //bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
        
    }
    public void readConfig() {
        if (mMapAppInfo == null) {
            mMapAppInfo = new ArrayList<AppInfo>();
        } else {
            mMapAppInfo.clear();
            // return true;
        }
        try {
            //InputStream in = getResources().getAssets().open("theme/icon_config.xml");
            File saveDir = getFilesDir();
            //InputStream in = new FileInputStream(new File(getFilesDir()+"readme.xml"));
            InputStream in = new FileInputStream(new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/readme.xml"));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(in, "UTF-8");
            final int depth = xpp.getDepth();
            int type = xpp.getEventType();
            while (((type = xpp.next()) != XmlPullParser.END_TAG || xpp.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                
                if("com.cappu.launcherwin".equals(xpp.getAttributeValue(null, "PackageName"))){
                    continue;
                }
                //mMapAppInfo.put(xpp.getAttributeValue(null, "AppName"), new AppInfo(xpp.getAttributeValue(null, "PackageName"), xpp.getAttributeValue(null, "ClassName"), xpp.getAttributeValue(null, "AppNameCN"), xpp.getAttributeValue(null, "AppName"), xpp.getAttributeValue(null, "Version")));
                if(xpp.getAttributeValue(null, "AppName") != null && xpp.getAttributeValue(null, "AppNameCN") != null && xpp.getAttributeValue(null, "Version") != null && xpp.getAttributeValue(null, "DownloadUrl") != null){
                    String PackageName = xpp.getAttributeValue(null, "PackageName");
                    String ClassName = xpp.getAttributeValue(null, "ClassName");
                    String AppNameCN = xpp.getAttributeValue(null, "AppNameCN");
                    String AppName = xpp.getAttributeValue(null, "AppName");
                    String AppIconName = xpp.getAttributeValue(null, "AppIconName");
                    String AppIconUrl = xpp.getAttributeValue(null, "AppIconUrl");
                    String DownloadUrl = xpp.getAttributeValue(null, "DownloadUrl");
                    String Version = xpp.getAttributeValue(null, "Version");
                    mMapAppInfo.add(new AppInfo(PackageName,ClassName,AppNameCN,AppName,AppIconName,AppIconUrl,DownloadUrl, Version));
                    
                }
                
                //Log.i("HHJ", "AppName and Version :"+xpp.getAttributeValue(null, "AppNameCN")+"    "+xpp.getAttributeValue(null, "AppName")+"    "+xpp.getAttributeValue(null, "Version"));
            }
            in.close();

        } catch (XmlPullParserException e) {
            Log.e("HHJ", "Got XmlPullParserException parsing toppackage.", e);
            DeleteConfig();
        } catch (IOException e) {
            Log.e("HHJ", "Got IOException parsing toppackage.", e);
            DeleteConfig();
        }
    }
    
    public void DeleteConfig(){
        File file = new File(Environment.getExternalStorageDirectory()+DownloadCenter.DOWNLOAD_DIR+"/readme.xml");
        if(file.exists()){
            file.delete();
        }
    }
    
    public static class AppInfo {
        public AppInfo(String packagename, String classname,String appNameCN, String appName,String appIconName,String appIconUrl,String downloadUrl, String version) {
            mPackageName = packagename;
            mClassName = classname;
            mAppNameCN = appNameCN;
            mAppName = appName;
            mAppIconName = appIconName;
            mAppIconUrl = appIconUrl;
            mDownloadUrl = downloadUrl;
            mVersion = version;
            MaxProgress = 0;
            CurrentProgress = 0;
            putAppName(mAppName,mPackageName+"/"+mClassName);
        }

        public String getmAppIconName() {
            return mAppIconName;
        }

        public void setmAppIconName(String mAppIconName) {
            this.mAppIconName = mAppIconName;
        }

        public int getCurrentProgress() {
            return CurrentProgress;
        }

        public int getMaxProgress() {
            return MaxProgress;
        }

        public void setMaxProgress(int maxProgress) {
            MaxProgress = maxProgress;
        }

        public void setCurrentProgress(int currentProgress) {
            CurrentProgress = currentProgress;
        }

        private Map<String, String> RESOURCE_MAP = new HashMap<String, String>();

        public void putAppName(String appName,String cn) {
            RESOURCE_MAP.put(appName,cn);
        }
        
        public String getAppName(String appName){
            if(RESOURCE_MAP.containsKey(appName)){
                return RESOURCE_MAP.get(appName);
            }else{
                return null;
            }
        }

        public String getString(String classname) {
            return RESOURCE_MAP.get(mClassName);
        }
        
        public String mPackageName;
        public String mClassName;
        public String mAppNameCN;
        public String mAppName;
        public String mAppIconName;
        public String mAppIconUrl;
        public String mDownloadUrl;
        public String mVersion;
        private int MaxProgress;
        private int CurrentProgress;
    }
    
    private class AppAdapter extends BaseAdapter {
        private AsynImageLoader mImageAsynLoader;
        
        LayoutInflater vi = null;
        Context context;
        List<AppInfo> mApps;
        PackageManager mPackageManager;
        Handler mHandler;
        
        AppAdapter(Context context,List<AppInfo> apps,PackageManager mPackageManager){
            this.context = context;
            this.mPackageManager = mPackageManager;
            this.mHandler = new Handler();
            this.mImageAsynLoader = new AsynImageLoader(mHandler);
            setData(apps);
        }
        
        public void releaseBitmapCache(){
            mImageAsynLoader.releaseBitmapCache();
        }
        
        
        public void setData(List<AppInfo> mApps){
            if(mApps == null){
                return;
            }
            List<AppInfo> delList = new ArrayList<AppInfo>();
            for (AppInfo ai:mApps) {
                if(!(ai.mClassName.equals(mAutoClassName) && ai.mPackageName.equals(mAutoPackageName))){
                    delList.add(ai);
                }
            }
            mApps.removeAll(delList);
            this.mApps = mApps;
        }
        
        @Override
        public int getCount() {
            if(mApps != null){
                return mApps.size();
            }else{
                return 0;
            }
            
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            final ViewHolder holder;
            final AppInfo mAppInfo = mApps.get(position);
            if (convertView == null) {
                vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.download_center_list_item, parent,false);
                holder = new ViewHolder();
                holder.mAppName = (TextView) convertView.findViewById(R.id.app_name);
                holder.mProgressBar = (KookProgress) convertView.findViewById(R.id.progressBar);
                holder.mButton = (Button) convertView.findViewById(R.id.option);
                holder.mImageView = (ImageView) convertView.findViewById(R.id.icon);
                holder.mAppInfo = mAppInfo;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.mAppInfo = mAppInfo;
            }
            if(mApps == null){
                return null;
            }
            
            Log.i("HHJ", " mAppInfo CurrentProgress:"+mAppInfo.getCurrentProgress()+"    "+mAppInfo.getMaxProgress()+"   "+mAppInfo.mAppName);
            
            if(mDownloadAppName.contains(mAppInfo.mAppName) || mDownloadAppNameServices.contains(mAppInfo.mAppName)){
                holder.mProgressBar.setVisibility(View.VISIBLE);
                holder.mButton.setVisibility(View.INVISIBLE);
                holder.mProgressBar.setMax(mAppInfo.getMaxProgress());
                holder.mProgressBar.setProgress(mAppInfo.getCurrentProgress());
            }else{
                holder.mButton.setVisibility(View.VISIBLE);
                holder.mProgressBar.setVisibility(View.INVISIBLE);
            }
            
            
            
            File file = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/"+mAppInfo.mAppIconName);
            
            //Log.i("HHJ", " mAppInfo CurrentProgress:"+mAppInfo.getCurrentProgress()+"    "+mAppInfo.getMaxProgress()+"   "+mAppInfo.mAppName+"     file.exists():"+file.exists());
            /*if(!file.exists()){
                Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                intent.putExtra("types", "icon");
                intent.putExtra("path", mAppInfo.mAppIconUrl);
                intent.putExtra("appName", mAppInfo.mAppIconName);
                intent.putExtra("saveDir", Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                startService(intent);
            }
            holder.mImageView.setTag(mAppInfo.mAppIconName);
            mImageAsynLoader.loadBitmap(holder.mImageView, R.drawable.download_icon);*/
            try {
                //File file = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/"+mAppInfo.mAppIconName);
                if(file.exists()){
                    Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/"+mAppInfo.mAppIconName);
                    holder.mImageView.setImageBitmap(bm);
                    holder.mImageView.setTag(mAppInfo.mAppIconName);
                    
                    mImageAsynLoader.loadBitmap(holder.mImageView, R.drawable.download_icon);
                }else{
                    Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    intent.putExtra("types", "icon");
                    intent.putExtra("path", mAppInfo.mAppIconUrl);
                    intent.putExtra("appName", mAppInfo.mAppIconName);
                    intent.putExtra("saveDir", Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                    startService(intent);
                    Log.i("HHJ", "这里开启下载icon");
                    holder.mImageView.setImageResource(R.drawable.download_icon);
                }
            } catch (Exception e) {
                Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                intent.putExtra("types", "icon");
                intent.putExtra("path", mAppInfo.mAppIconUrl);
                intent.putExtra("appName", mAppInfo.mAppIconName);
                intent.putExtra("saveDir", Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                startService(intent);
                
                Log.i("HHJ", "这里开启下载icon  e:"+e.toString());
                holder.mImageView.setImageResource(R.drawable.download_icon);
            }
            
            holder.mButton.setEnabled(true);
            holder.mButton.setBackgroundResource(R.drawable.download_btn_selector);
            holder.mButton.setTextColor(Color.parseColor("#FFFFFF"));
            holder.mProgressBar.setMax(0);
            holder.mProgressBar.setProgress(0);
            //Log.i("HHJ", "mAppInfo.mPackageName:"+mAppInfo.mPackageName+"    "+APKInstallTools.checkApkInstall(DownloadCenter.this, mAppInfo.mPackageName, mAppInfo.mClassName));
            if(APKInstallTools.checkApkInstall(DownloadCenter.this, mAppInfo.mPackageName, mAppInfo.mClassName)){
                APKInstallTools.getVersionName(DownloadCenter.this, mAppInfo.mPackageName);
                //Log.i("HHJ", "当前软件版本号   "+APKInstallTools.getVersionName(DownloadCenter.this, mAppInfo.mPackageName));
                String currentVersion = APKInstallTools.getVersionName(DownloadCenter.this, mAppInfo.mPackageName);
                holder.mAppName.setText(mAppInfo.mAppNameCN);
                //Log.i("HHJ", " "+mDownloadAppName.contains(mAppInfo.mAppName) +""+""+ mDownloadAppNameServices.contains(mAppInfo.mAppName)+"  mAppInfo.mAppName:"+mAppInfo.mAppName);
                if(mDownloadAppName.contains(mAppInfo.mAppName) || mDownloadAppNameServices.contains(mAppInfo.mAppName)){
                    Integer[] size = getDownloaddata(mAppInfo.mAppName);
                    holder.mButton.setText(R.string.wait);
                    holder.mButton.setEnabled(false);
                    holder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                    if(size != null && size[0]!=null && size[1]!=null){
                        
                        Log.i("HHJ", "  727  ");
                        holder.mProgressBar.setMax(size[0]);
                        holder.mProgressBar.setProgress(size[1]);
                    }
                }else if(currentVersion !=null && mAppInfo.mVersion != null && mAppInfo.mVersion.equals(currentVersion)){
                    holder.mButton.setText(getString(R.string.open_app));
                    holder.mButton.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent();
                            ComponentName cn = new ComponentName(mAppInfo.mPackageName,mAppInfo.mClassName);
                            i.setComponent(cn);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            i.setAction(Intent.ACTION_MAIN);
                            i.addCategory("android.intent.category.LAUNCHER");
                            startActivity(i);
                        }
                    });
                }else{
                    holder.mButton.setText(getString(R.string.update));
                    holder.mButton.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                            intent.putExtra("types", "app");
                            intent.putExtra("path", mAppInfo.mDownloadUrl);
                            intent.putExtra("appName", mAppInfo.mAppName);
                            intent.putExtra("saveDir", Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                            startService(intent);
                            holder.mButton.setEnabled(false);
                            holder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                            mDownloadAppName.add(mAppInfo.mAppName);
                        }
                    });
                }
                
            }else{
                final File ApkFile = new File(Environment.getExternalStorageDirectory()+DOWNLOAD_DIR+"/"+mAppInfo.mAppName);
                holder.mAppName.setText(mAppInfo.mAppNameCN);
                if(mDownloadAppName.contains(mAppInfo.mAppName) || mDownloadAppNameServices.contains(mAppInfo.mAppName)){
                    Integer[] size = getDownloaddata(mAppInfo.mAppName);
                    holder.mButton.setEnabled(false);
                    holder.mButton.setText(R.string.wait);
                    holder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                    if(size != null && size[0]!=null && size[1]!=null){
                        
                        holder.mProgressBar.setMax(size[0]);
                        holder.mProgressBar.setProgress(size[1]);
                    }
                }else if(ApkFile.exists()){
                    holder.mButton.setText(getString(R.string.install));
                    holder.mButton.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(ApkFile), "application/vnd.android.package-archive");
                            startActivity(intent);
                        }
                    });
                }else{
                    holder.mButton.setText(getString(R.string.download));
                    holder.mButton.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                            intent.putExtra("types", "app");
                            intent.putExtra("path", mAppInfo.mDownloadUrl);
                            intent.putExtra("appName", mAppInfo.mAppName);
                            intent.putExtra("saveDir", Environment.getExternalStorageDirectory()+DOWNLOAD_DIR);
                            startService(intent);
                            holder.mButton.setEnabled(false);
                            holder.mButton.setBackgroundResource(R.drawable.download_button_pressed);
                            mDownloadAppName.add(mAppInfo.mAppName);
                        }
                    });
                }
            }
            
            return convertView;
        }


    }
    
    /**获取下载的线程*/
    public Integer[] getDownloaddata(String appName){
        Integer[] fileSize = new Integer[2];
        FileDownloader fileDownloader = null;
        DownloadTask downloadTask = null;
        if(mKookLocalService!=null){
            downloadTask = mKookLocalService.getListDownloadTask();
        }
        
        if(downloadTask != null){
            fileDownloader = downloadTask.getFileDownloader();
            if(fileDownloader!=null){
                fileSize[0] = fileDownloader.getFileSize();
                fileSize[1] = fileDownloader.getDownloadSize();
            }
            
        }
        return fileSize;
    }
    
    static class ViewHolder{
        public TextView mAppName;
        public KookProgress mProgressBar;
        public Button mButton;
        public AppInfo mAppInfo;
        public ImageView mImageView;
    }
}
