package com.cappu.launcherwin.downloadUI.celllayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class Downloader {

    public static final String TAG = "DownloadService";
    
    String mDownloadUrl;
    /**默认保存的位置*/
    String mFilePath = CellLyouatUtil.CellLayoutExpandDIR;
    /**下载的文件名字*/
    String mFileName;

    DownloadListener downloadListener;
    
    private boolean isPause;
    private boolean isCancel;

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
    }

    public Downloader(String url) {
        this.mDownloadUrl = url;
    }

    public Downloader(Context context, String url) {
        this(url);
    }

    public void start() {
        URL url = null;
        try {
            url = new URL(mDownloadUrl);
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
            urlCon.setDoInput(true);
            urlCon.setRequestMethod("GET");
            urlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            mFileName = getFileName(urlCon);
            // 建立连接
            urlCon.connect();
            int length = urlCon.getContentLength();
            downloadListener.onStart(length);

            if (urlCon.getResponseCode() == 200) {

                File path = Environment.getExternalStoragePublicDirectory(mFilePath);
                if(!path.exists()){
                    path.mkdirs();
                    Log.e(TAG, "创建下载的文件目录  path:" + path.getPath());
                }
                File file = new File(path, mFileName);
                BufferedInputStream is = new BufferedInputStream(urlCon.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buffer = new byte[10240];
                int len = 0;
                int receivedBytes = 0;
                label: while (true) {
                    // 这里如果暂停下载，并没有真正的销毁线程，而是处于等待状态
                    // 但如果这时候用户退出了，要做处理，比如取消任务；或做其他处理

                    if (isPause)
                        downloadListener.onPause();
                    if (isCancel) {
                        downloadListener.onCancel();
                        break label;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while (!isPause && (len = is.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                        receivedBytes += len;
                        downloadListener.onProgress(receivedBytes);
                        if (receivedBytes == length) {
                            downloadListener.onSuccess(file);
                            break label;
                        }
                        if (isCancel) {
                            downloadListener.onCancel();
                            file.delete();
                            break label;
                        }
                    }
                }

                is.close();
                out.close();
            } else {
                Log.e(TAG, "ResponseCode:" + urlCon.getResponseCode() + ", msg:" + urlCon.getResponseMessage());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "MalformedURLException  e:" + e.toString());
            downloadListener.onFail();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException  e:" + e.toString());
            downloadListener.onFail();
        }
    }

    public void pause() {
        isPause = true;
    }

    public void resume() {
        isPause = false;
        isCancel = false;
        downloadListener.onResume();
    }

    public void cancel() {
        isCancel = true;
    }
    
    /**
     * 获取文件名
     */
    private String getFileName(HttpURLConnection conn) {
        String filename = this.mDownloadUrl.substring(this.mDownloadUrl.lastIndexOf('/') + 1);
        if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
            for (int i = 0;; i++) {
                String mine = conn.getHeaderField(i);
                if (mine == null)
                    break;
                if ("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())) {
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if (m.find())
                        return m.group(1);
                }
            }
            filename = UUID.randomUUID() + ".tmp";// 默认取一个文件名
        }
        mFileName = filename;
        return filename;
    }
    
    public String getFileName(){
        return mFileName;
    }
}
