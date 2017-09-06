package com.cappu.launcherwin.downloadUI.celllayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.cappu.launcherwin.LauncherSettings;
import com.cappu.launcherwin.LauncherSettings.Favorites;
import com.cappu.launcherwin.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class CellLyouatUtil{
    
    public static final String TAG = "DownloadService";

    public static final String ACTION_DOWNLOAD_PROGRESS = "download_progress";
    public static final String ACTION_DOWNLOAD_SUCCESS = "download_success";
    public static final String ACTION_DOWNLOAD_FAIL = "download_fail";
    
    
    public static final int ACTION_DOWNLOAD_PROGRESS_TYPE = 1;
    public static final int ACTION_DOWNLOAD_SUCCESS_TYPE = 2;
    public static final int ACTION_DOWNLOAD_FAIL_TYPE = 3;

    public static final String DOWNLOAD_HEAD = "http://app.careos.cn/download/";

    public static final String CellLayoutExpandDIR = "/.Cappu/cellLayout";
    
    public static final String DownloadURL = DOWNLOAD_HEAD+"cellLayout/expand_default_workspace.xml";
    
    public static final Map<String,ContentValues> mList = new HashMap<String,ContentValues>();
    
    Context mContext;

    public CellLyouatUtil(Context context){
        this.mContext = context;
    }
    
    public void checkCellLayout(){
        File path = Environment.getExternalStoragePublicDirectory(CellLayoutExpandDIR);
        if(path.exists()){
            boolean check = checkComplete(path+"/expand_default_workspace.xml");//判断当前文件是否完整
            if(check){
                Log.i(TAG, "当前文件完整");
                if(mList.size() == 0){
                    readConfig(path+"/expand_default_workspace.xml");
                }
            }else{
                Log.i(TAG, "当前文件不完整  开始下载新文件 配置文件");
                startDownloadService(DownloadURL);
            }
        }else{
            startDownloadService(DownloadURL);
        }
        
    }

    /**开始线程下载*/
    public void startDownloadService(String url) {
        if (DownloadService.getInstance() != null && (DownloadService.getInstance().getFlag() != DownloadService.Flag_Init && DownloadService.getInstance().getFlag() != DownloadService.Flag_Done)) {
            Log.i(TAG, " Flag_Init："+DownloadService.getInstance().getFlag());
            return;
        }
        Intent it = new Intent(mContext, DownloadService.class);
        it.putExtra("flag", "start");
        it.putExtra("url", url);
        mContext.startService(it);
    }

    /**暂停线程下载*/
    public void pauseDownloadService() {
        String flag = null;
        int f = DownloadService.getInstance().getFlag();
        if (DownloadService.getInstance() != null) {
            // 如果当前已经暂停，则恢复
            if (f == DownloadService.Flag_Pause) {
                flag = "resume";
            } else if (f == DownloadService.Flag_Down) {
                flag = "pause";
            } else {
                return;
            }
        }
        Intent it = new Intent(mContext, DownloadService.class);
        it.putExtra("flag", flag);
        mContext.startService(it);
    }

    /**停止线程下载*/
    public void stopDownloadService() {
        Intent it = new Intent(mContext, DownloadService.class);
        it.putExtra("flag", "stop");
        mContext.startService(it);
    }

    private void openFile(File f) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String type = "audio";
        intent.setDataAndType(Uri.fromFile(f), type);
        mContext.startActivity(intent);
    }
    
    public Map<String,ContentValues> getExpandConfig(){
        mList.clear();
        if(mList.size() == 0){
            checkCellLayout();
        }
        Log.i(TAG, "扩展的配置文件大小 mList："+mList.size());
        return mList;
    }
    
    /** 
     * 
     * fileName 读取文件的路径地址 path + name
     * 
     * */
    private void readConfig(String fileName) {
        try {
            //InputStream in = getResources().getAssets().open("theme/icon_config.xml");
            //InputStream in = new FileInputStream(new File(getFilesDir()+"readme.xml"));
            InputStream in = new FileInputStream(new File(fileName));
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(in, "UTF-8");
            final int depth = xpp.getDepth();
            int type = xpp.getEventType();
            ContentValues values = new ContentValues();
            int i = 0;
            while (((type = xpp.next()) != XmlPullParser.END_TAG || xpp.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if (type != XmlPullParser.START_TAG) {
                    continue;
                }
                values = new ContentValues();
                final String name = xpp.getName();//parser.getName();
                Log.i(TAG, "readConfig name： "+name);
                int itemType = 0;
                if ("favorite".equals(name)) {
                    itemType = Favorites.ITEM_TYPE_SHORTCUT;
                }else if("contacts".equals(name)){
                    itemType = Favorites.ITEM_TYPE_CONTACTS;
                }else if("AppComponentName".equals(name)){
                    itemType = Favorites.ITEM_TYPE_APPWIDGET;
                }else if("favorites".equals(name)){
                    continue;
                }else{
                    continue;
                }
                
                String modeSelect = xpp.getAttributeValue(null, LauncherSettings.Favorites.MODE);
                if(TextUtils.isEmpty(modeSelect)){
                    modeSelect = null;
                }
                values.put(Favorites.ITEM_TYPE, itemType);
                values.put(LauncherSettings.Favorites.MODE, modeSelect); 
                values.put(LauncherSettings.Favorites.CONTAINER, xpp.getAttributeValue(null, LauncherSettings.Favorites.CONTAINER)); 
                values.put(LauncherSettings.Favorites.BACKGROUND, xpp.getAttributeValue(null, LauncherSettings.Favorites.BACKGROUND));
                values.put(LauncherSettings.Favorites.SCREEN, xpp.getAttributeValue(null, LauncherSettings.Favorites.SCREEN));
                values.put(LauncherSettings.Favorites.CELLX, xpp.getAttributeValue(null, "x"));
                values.put(LauncherSettings.Favorites.CELLY, xpp.getAttributeValue(null, "y"));
                
                
                /** 以下是做网络版 后整合 添加的属性*/
                values.put("packageName", xpp.getAttributeValue(null, "packageName"));
                values.put("className", xpp.getAttributeValue(null, "className"));
                
                values.put(LauncherSettings.Favorites.APP_NAME, xpp.getAttributeValue(null, "AppName"));
                values.put(LauncherSettings.Favorites.APP_NAME_CN, xpp.getAttributeValue(null, "AppNameCN"));
                values.put(LauncherSettings.Favorites.APP_ICON_NAME, xpp.getAttributeValue(null, "AppIconName"));
                values.put(LauncherSettings.Favorites.APP_ICON_URL, xpp.getAttributeValue(null, "AppIconUrl"));
                values.put(LauncherSettings.Favorites.APP_DOWANLOAD_URL, xpp.getAttributeValue(null, "DownloadUrl"));
                values.put(LauncherSettings.Favorites.VERSION, xpp.getAttributeValue(null, "Version"));
                /** 以下是做网络版 后整合 添加的属性  end*/
                
                String packageName = xpp.getAttributeValue(null, "packageName");
                String className = xpp.getAttributeValue(null, "className");
                if(itemType != Favorites.ITEM_TYPE_APPWIDGET){ // 说明是 不 插件
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    ComponentName cn = new ComponentName(packageName, className);
                    intent.setComponent(cn);
                    values.put(LauncherSettings.Favorites.INTENT, intent.toUri(0));
                    values.put(LauncherSettings.Favorites.SPANX, xpp.getAttributeValue(null,LauncherSettings.Favorites.SPANX));
                    values.put(LauncherSettings.Favorites.SPANY, xpp.getAttributeValue(null,LauncherSettings.Favorites.SPANY));
                }else{ // 说明是插件
                    values.put(LauncherSettings.Favorites.INTENT, packageName+"/"+className);
                    values.put(LauncherSettings.Favorites.SPANX, xpp.getAttributeValue(null,LauncherSettings.Favorites.SPANX));
                    values.put(LauncherSettings.Favorites.SPANY, xpp.getAttributeValue(null,LauncherSettings.Favorites.SPANY));
                }
                
                String aliasTitle = xpp.getAttributeValue(null, LauncherSettings.Favorites.ALIAS_TITLE);
                if(aliasTitle != null && !"".equals(aliasTitle)){
                    values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle);
                }else{
                    values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
                }
                
                String cellDefImage = xpp.getAttributeValue(null, LauncherSettings.Favorites.CELL_DEF_IMAGE);
                if(cellDefImage != null && !"".equals(cellDefImage)){
                    values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,cellDefImage);
                }else{
                    values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,"");
                }
                
                String aliasTitleBackground = xpp.getAttributeValue(null, LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND);
                if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
                    values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
                }else{
                    values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
                }
                
                if(itemType == Favorites.ITEM_TYPE_CONTACTS){
                    mList.put(packageName+"/"+className+"/"+modeSelect+"/"+i, values);
                }else{
                    if(modeSelect == null){
                        mList.put(packageName+"/"+className, values);
                    }else{
                        mList.put(packageName+"/"+className+"/"+modeSelect, values);
                    }
                    
                }
                i++;
            }
            in.close();

        } catch (XmlPullParserException e) {
            Log.e(TAG, "Got XmlPullParserException parsing toppackage.", e);
            DeleteFile(fileName);
        } catch (IOException e) {
            Log.e(TAG, "Got IOException parsing toppackage.", e);
            DeleteFile(fileName);
        }
    }
    
    /** 
     * 
     * fileName 读取文件的路径地址 path + name
     * 
     * */
    public void DeleteFile(String fileName){
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 判断文件是否完整 
     * @param src
     *            文件路径
     */
    public static boolean checkComplete(String src) {
        int c;
        String line; // 读取新文件
        RandomAccessFile rf = null;
        boolean isComplete = true;
        try {
            rf = new RandomAccessFile(src, "r");
            long len = rf.length();
            long start = rf.getFilePointer();
            long nextend = start + len - 1;
            rf.seek(start + len - 1);
            while (nextend > start) {
                c = rf.read();
                line = rf.readLine();
                nextend--;
                rf.seek(nextend);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isComplete = false;
        } catch (IOException e) {
            e.printStackTrace();
            isComplete = false;
        } finally {
            try {
                if(rf != null){
                    rf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isComplete;
    }
    
    /**
     * 判断一个文件是否完整而且最后一行是否为某字符串
     * @param src
     *            文件路径
     * @param key
     *            判断是否最后一行的字符串
     *     是 则 返回 true 不是 则 返回 false
     */
    public static boolean checkLastLine(String src, String key) {
        int c;
        String line; // 读取新文件
        RandomAccessFile rf = null;
        boolean isEnd = false;
        try {
            rf = new RandomAccessFile(src, "r");
            long len = rf.length();
            long start = rf.getFilePointer();
            long nextend = start + len - 1;
            rf.seek(start + len - 1);
            while (nextend > start) {
                c = rf.read();
                line = rf.readLine();
                // 判断读到的最后一行
                if ((c == '\n' || c == '\r') && line != null && !line.equals("")) {
                    if (line.equals(key)) {
                        isEnd = true;
                    }
                    break;
                }
                nextend--;
                rf.seek(nextend);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isEnd;
    }


}
