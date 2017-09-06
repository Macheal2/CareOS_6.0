package com.cappu.launcherwin.downloadUI.celllayout;

import java.io.File;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.LauncherSettings;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.downloadUI.DownloadCenter;
import com.cappu.launcherwin.downloadUI.DownloadCenter.AppInfo;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.widget.KookProgress;

public class DownloadCellLayoutMainActivity extends Activity implements View.OnClickListener{

    public static final String TAG = "DownloadService";
    
    private String mAutoPackageName = null;
    private String mAutoClassName = null;

    DownloadReceiver mDownloadReceiver;
    LauncherApplication mLauncherApplication;

    CellLyouatUtil mCellLyouatUtil;

    public TextView mAppName;
    public KookProgress mProgressBar;
    public Button mButton;
    public ImageView mImageView;
    
    Button mCancelButton;

    public static Map<String, ContentValues> mMap;
    
    String AppName;
    String AppNameCN;
    String AppIconName;
    String AppIconUrl;
    String DownloadUrl;
    
    String PackageName;
    String ClassName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_celllayout);
        mLauncherApplication = (LauncherApplication) getApplication();
        mCellLyouatUtil = mLauncherApplication.getCellLyouatUtil();
        mAutoPackageName = getIntent().getStringExtra("mAutoPackageName");
        mAutoClassName = getIntent().getStringExtra("mAutoClassName");
        init();
    }

    private void init(){
        mAppName = (TextView) findViewById(R.id.app_name);
        mProgressBar = (KookProgress) findViewById(R.id.progressBar);
        mButton = (Button) findViewById(R.id.option);
        mImageView = (ImageView) findViewById(R.id.icon);
        
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        
        mDownloadReceiver = new DownloadReceiver();
        
        mMap = mCellLyouatUtil.getExpandConfig();
        
        int modeSelect = ThemeManager.getInstance().getCurrentThemeType(mLauncherApplication);
        if(modeSelect == 2 || modeSelect == 3){
            modeSelect = 2;
        }
        ContentValues cv = mMap.get(mAutoPackageName+"/"+mAutoClassName+"/"+modeSelect);
        if(cv == null){
            cv = mMap.get(mAutoPackageName+"/"+mAutoClassName);
        }
        
        if(cv == null){
            finish();
        }
        
        try {
            AppName=cv.getAsString(LauncherSettings.Favorites.APP_NAME);
            AppNameCN=cv.getAsString(LauncherSettings.Favorites.APP_NAME_CN);
            AppIconName=cv.getAsString(LauncherSettings.Favorites.APP_ICON_NAME);
            AppIconUrl=cv.getAsString(LauncherSettings.Favorites.APP_ICON_URL);
            DownloadUrl=cv.getAsString(LauncherSettings.Favorites.APP_DOWANLOAD_URL);
            
            PackageName=cv.getAsString(LauncherSettings.Favorites.APP_ICON_URL);
            ClassName=cv.getAsString(LauncherSettings.Favorites.APP_DOWANLOAD_URL);
            
            mAppName.setText(AppNameCN);
            if(APKInstallTools.checkApkInstall(DownloadCellLayoutMainActivity.this, PackageName, ClassName)){
                File ApkFile = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/"+AppName);
                Log.i(TAG, "已存在  CellLyouatUtil.CellLayoutExpandDIR："+CellLyouatUtil.CellLayoutExpandDIR+"/"+AppName);
                if(ApkFile.exists()){
                    installAPK(ApkFile);
                }
            }else{
                File ApkFile = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/"+AppName);
                
                if(ApkFile.exists()){
                    Log.i(TAG, "已存在  CellLyouatUtil.CellLayoutExpandDIR："+CellLyouatUtil.CellLayoutExpandDIR+"/"+AppName);
                    installAPK(ApkFile);
                }else{
                    Log.i(TAG, " 未安装");
                    mCellLyouatUtil.startDownloadService(DownloadUrl);
                }
                
                
            }
        } catch (Exception e) {
            finish();
        }
    }
    
    public static boolean getUninatllApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            Log.e("archiveFilePath", filePath);
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            String packageName = null;
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
            Log.i(TAG, " 147 文件不完整");
        }
        return result;
    }
    
    private void installAPK(File apkFile){
        if(getUninatllApkInfo(this,apkFile.getAbsolutePath())){
            Log.i(TAG, " 文件完整");
        }else{
            Log.i(TAG, " 文件不完整");
            mCellLyouatUtil.DeleteFile(apkFile.getAbsolutePath());
            return;
        }
        
        
        Intent installIintent = new Intent();
        installIintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIintent.setAction(android.content.Intent.ACTION_VIEW);
        installIintent.addCategory("android.intent.category.DEFAULT");
        installIintent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        startActivity(installIintent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CellLyouatUtil.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(CellLyouatUtil.ACTION_DOWNLOAD_SUCCESS);
        filter.addAction(CellLyouatUtil.ACTION_DOWNLOAD_FAIL);
        registerReceiver(mDownloadReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mDownloadReceiver!=null){
            unregisterReceiver(mDownloadReceiver);
        }
        
        if(mCellLyouatUtil != null){
            mCellLyouatUtil.stopDownloadService();
        }
        
        finish();
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCellLyouatUtil != null){
            mCellLyouatUtil.stopDownloadService();
        }
        finish();
    }

    class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(CellLyouatUtil.ACTION_DOWNLOAD_PROGRESS)) {
                int pro = intent.getExtras().getInt("progress");
                mProgressBar.setProgress(pro);
                //Log.i(TAG, "当前下载  pro："+pro);
                mButton.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
            } else if (action.equals(CellLyouatUtil.ACTION_DOWNLOAD_SUCCESS)) {
                Toast.makeText(DownloadCellLayoutMainActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                File ApkFile = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/"+AppName);
                //Log.i(TAG, "下载成功  CellLyouatUtil.CellLayoutExpandDIR："+CellLyouatUtil.CellLayoutExpandDIR+"/"+AppName);
                if(ApkFile.exists()){
                    installAPK(ApkFile);
                }
            } else if (action.equals(CellLyouatUtil.ACTION_DOWNLOAD_FAIL)) {
                Toast.makeText(DownloadCellLayoutMainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mCancelButton){
            if(mCellLyouatUtil != null){
                mCellLyouatUtil.stopDownloadService();
                finish();
            }
        }
    }

}
