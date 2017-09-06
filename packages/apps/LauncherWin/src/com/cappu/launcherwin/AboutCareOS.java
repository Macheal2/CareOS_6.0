
package com.cappu.launcherwin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.downloadUI.DownloadCenter;
import com.cappu.launcherwin.downloadUI.DownloadCenter.AppInfo;
import com.cappu.launcherwin.downloadUI.celllayout.CellLyouatUtil;
import com.cappu.launcherwin.install.APKInstallTools;

public class AboutCareOS extends Activity implements View.OnClickListener{
    
    public String mCurrentVersion;

    TextView text_version;
    TextView text_loading;
    TextView btn_action;
    ImageView image_loading;
    Matrix mMatrix;
    FlingRunnable mFlingRunnable;
    
    LinearLayout layout_checking;
    
    private float image_loading_centerY;
    private float image_loading_centerX;
    
    //private List<AppInfo> mMapAppInfo;
    public static Map<String, ContentValues> mMap;
    public ContentValues mContentValues;
    private Handler mUIConfigHander = new UIConfigHander();
    
    CellLyouatUtil mCellLyouatUtil;
    private class UIConfigHander extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                image_loading.setVisibility(View.INVISIBLE);
                text_loading.setText(R.string.about_unexpected);//检查失败
                break;
            case 3:
                image_loading.setVisibility(View.INVISIBLE);
                readConfig();
                try {
                    if(mContentValues == null){
                        Log.i("DownloadService", "AboutCareOs mContentValues is null");
                        return;
                    }
                    final ContentValues contentValues= mContentValues;
                    
                    final String appName=contentValues.getAsString(LauncherSettings.Favorites.APP_NAME);
                    final String appNameCN=contentValues.getAsString(LauncherSettings.Favorites.APP_NAME_CN);
                    final String appIconName=contentValues.getAsString(LauncherSettings.Favorites.APP_ICON_NAME);
                    final String appIconUrl=contentValues.getAsString(LauncherSettings.Favorites.APP_ICON_URL);
                    final String downloadUrl=contentValues.getAsString(LauncherSettings.Favorites.APP_DOWANLOAD_URL);
                    final String currentVersion=contentValues.getAsString(LauncherSettings.Favorites.VERSION);
                    
                    final String PackageName=contentValues.getAsString(LauncherSettings.Favorites.APP_ICON_URL);
                    final String ClassName=contentValues.getAsString(LauncherSettings.Favorites.APP_DOWANLOAD_URL);
                    
                    final File file = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/"+appName);
                    String existsApkVersion = null;
                    if(file.exists()){
                        existsApkVersion = APKInstallTools.getAppVersionName(AboutCareOS.this, file.getAbsolutePath());
                    }
                    
                    if(mCurrentVersion !=null && currentVersion != null && currentVersion.equals(mCurrentVersion)){
                        text_loading.setText(R.string.about_newest);
                        btn_action.setText(R.string.about_ok);
                        btn_action.setOnClickListener(new OnClickListener() {
                            
                            @Override
                            public void onClick(View arg0) {
                                finish();
                            }
                        });
                    }else if(existsApkVersion!=null && existsApkVersion.equals(currentVersion)){//这里是检测已存在的apk是否为最新下载过的apk
                        image_loading.setVisibility(View.INVISIBLE);
                        text_loading.setText(R.string.about_downloaded_hint);
                        btn_action.setText(R.string.about_install);
                        btn_action.setOnClickListener(new OnClickListener() {
                            
                            @Override
                            public void onClick(View arg0) {
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                startActivity(intent);
                            }
                        });
                    }else{
                        String book=getResources().getString(R.string.about_have_a_newer_version);//有新版本 需要下载
                        String bookTest=String.format(book,currentVersion);
                        text_loading.setText(bookTest);
                        btn_action.setText(R.string.about_download);
                        btn_action.setOnClickListener(new OnClickListener() {
                            
                            @Override
                            public void onClick(View arg0) {
                                if(mContentValues == null){
                                    Log.i("DownloadService", "AboutCareOs mContentValues is null");
                                    return;
                                }
                                final ContentValues contentValues= mContentValues;
                                
                                final String downloadUrl=contentValues.getAsString(LauncherSettings.Favorites.APP_DOWANLOAD_URL);
                                mCellLyouatUtil.startDownloadService(downloadUrl);
                                text_loading.setText(R.string.about_downloading_hint);
                                image_loading.post(mFlingRunnable);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.i("DownloadService", "有时候网络异常下载空文件报错:"+e.toString());
                }
                
                break;
            case 4:
                image_loading.setVisibility(View.INVISIBLE);
                text_loading.setText(R.string.about_downloaded_hint);
                btn_action.setText(R.string.about_install);
                btn_action.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View arg0) {
                        if(mContentValues == null){
                            Log.i("DownloadService", "AboutCareOs mContentValues is null");
                            return;
                        }
                        final String appName=mContentValues.getAsString(LauncherSettings.Favorites.APP_NAME);
                        final File file = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/"+appName);
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        startActivity(intent);
                    }
                });
                break;
                
            case 5:
                int pro = msg.arg1;
                String text = String.valueOf(pro) + "%";
                btn_action.setText(text);
                btn_action.setOnClickListener(null);
                text_loading.setText(R.string.about_downloading_hint);//正在下载..
                
                image_loading.setVisibility(View.VISIBLE);
                break;
            }
        }
    }
    private ServiceDataReceiver mServiceDataReceiver;
    public class ServiceDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            
            String action = intent.getAction();
            String fileName = intent.getStringExtra("fileName");
            Log.i("DownloadService", "fileName:"+fileName+"   action:"+action);
            Message msg = new Message();
            if(CellLyouatUtil.ACTION_DOWNLOAD_PROGRESS.equals(action)){
                if(!"expand_default_workspace.xml".equals(fileName)){
                    int pro = intent.getExtras().getInt("progress");
                    msg.what = 5;
                    msg.arg1 = pro;
                    mUIConfigHander.sendMessage(msg);
                }
            }else if(CellLyouatUtil.ACTION_DOWNLOAD_SUCCESS.equals(action)){
                if("expand_default_workspace.xml".equals(fileName)){
                    mCellLyouatUtil.checkCellLayout();
                    msg.what = 3;
                    mUIConfigHander.sendMessage(msg);
                }else{
                    /*msg.what = 4;
                    mUIConfigHander.sendMessage(msg);*/
                    if(mContentValues == null){
                        Log.i("DownloadService", "AboutCareOs mContentValues is null");
                        return;
                    }
                    final String appName=mContentValues.getAsString(LauncherSettings.Favorites.APP_NAME);
                    final File file = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/"+appName);
                    Intent installIntent = new Intent();
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    installIntent.setAction(android.content.Intent.ACTION_VIEW);
                    installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    startActivity(installIntent);
                    finish();
                }
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_careos);
        mCurrentVersion = APKInstallTools.getVersionName(AboutCareOS.this,"com.cappu.launcherwin");
        //注册广播接收器
        mServiceDataReceiver=new ServiceDataReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(CellLyouatUtil.ACTION_DOWNLOAD_PROGRESS);
        filter.addAction(CellLyouatUtil.ACTION_DOWNLOAD_SUCCESS);
        filter.addAction(CellLyouatUtil.ACTION_DOWNLOAD_FAIL);
        filter.addAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
        registerReceiver(mServiceDataReceiver,filter);
        init();
    }
    
    public void init() {
        layout_checking = (LinearLayout) findViewById(R.id.layout_checking);
        image_loading = (ImageView) findViewById(R.id.image_loading);
        text_loading = (TextView) findViewById(R.id.text_loading);
        btn_action = (TextView) findViewById(R.id.btn_action);
        btn_action.setText(R.string.about_ok);
        
        if (mMatrix == null) {
            mMatrix = new Matrix();
        } else {
            mMatrix.reset();
        }
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.about_ico_loading);
        image_loading_centerX = mBitmap.getWidth() / 2 ;
        image_loading_centerY = mBitmap.getHeight() / 2;
        
        mFlingRunnable = new FlingRunnable();
        image_loading.post(mFlingRunnable);
        
        text_version = (TextView) findViewById(R.id.text_version);
        String book=getResources().getString(R.string.about_version_format);
        String bookTest=String.format(book,mCurrentVersion);
        text_version.setText(bookTest);
        
        File file = new File(Environment.getExternalStoragePublicDirectory(CellLyouatUtil.CellLayoutExpandDIR)+"/expand_default_workspace.xml");
        if(file.exists()){
            readConfig();
        }
    }
    
    /**
     * A {@link Runnable} for animating the the dialer's fling.
     */
    private class FlingRunnable implements Runnable {

        @Override
        public void run() {
            rotateDialer(5);
            image_loading.post(this);
        }
    }
    
    /**
     * Rotate the dialer.
     * 
     * @param degrees The degrees, the dialer should get rotated.
     */
    private void rotateDialer(float degrees) {
        mMatrix.postRotate(degrees, image_loading_centerX, image_loading_centerY);
        
        image_loading.setImageMatrix(mMatrix);
    }
    
    public void readConfig() {
        if(mCellLyouatUtil == null){
            LauncherApplication la = (LauncherApplication) getApplication();
            mCellLyouatUtil = la.getCellLyouatUtil();
        }
        mMap = mCellLyouatUtil.getExpandConfig();
        mContentValues= mMap.get("com.cappu.launcherwin/com.cappu.launcherwin.Launcher");
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mServiceDataReceiver);
        if(mMap != null){
            mMap.clear();
            mMap = null;
        }
    }

    @Override
    public void onClick(View v) {}
    
    
    
}
