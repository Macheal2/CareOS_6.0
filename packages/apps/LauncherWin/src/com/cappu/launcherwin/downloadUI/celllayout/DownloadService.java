package com.cappu.launcherwin.downloadUI.celllayout;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service {
    public static final String TAG = "DownloadService";
    
    /**初始状态*/
    public static final int Flag_Init = 0;
    /**下载状态*/
    public static final int Flag_Down = 1;
    /**暂停状态*/
    public static final int Flag_Pause = 2;
    /**完成状态*/
    public static final int Flag_Done = 3;

    String url;

    // 下载进度
    private int progress = 0;

    public int getProgress() {
        return progress;
    }

    // 下载状态标志
    private int flag;

    public int getFlag() {
        return flag;
    }

    DownThread mThread;
    Downloader downloader;

    private static DownloadService instance;
    
    private DownloadServiceBinder mDownloadServiceBinder;
    
    public class DownloadServiceBinder extends Binder {
        
    }

    public static DownloadService getInstance() {
        return instance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDownloadServiceBinder;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "service.........onCreate");
        instance = this;
        mDownloadServiceBinder = new DownloadServiceBinder();
        flag = Flag_Init;
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Bundle bundle = intent.getExtras();
            if(bundle == null){
                return super.onStartCommand(intent, flags, startId); 
            }
            String msg = bundle.getString("flag");
            url = bundle.getString("url");
            Log.i(TAG, "service.........onStartCommand  msg:"+msg+"   url:"+url);
            
            mThread = new DownThread();
            downloader = new Downloader(this, url);
            
            downloader.setDownloadListener(downListener);

            if (msg.equals("start")) {
                flag = Flag_Init;
                startDownload();
            } else if (msg.equals("pause")) {
                downloader.pause();
            } else if (msg.equals("resume")) {
                downloader.resume();
            } else if (msg.equals("stop")) {
                downloader.cancel();
                stopSelf();
            }
            return super.onStartCommand(intent, flags, startId);
        } catch (Exception e) {
            return super.onStartCommand(intent, flags, startId);
        }

    }

    private void startDownload() {
        if (flag == Flag_Init || flag == Flag_Pause) {
            if (mThread != null && !mThread.isAlive()) {
                mThread = new DownThread();
            }
            mThread.start();
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "service...........onDestroy");
        try {
            flag = 0;
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mThread = null;
        super.onDestroy();
    }

    class DownThread extends Thread {

        @Override
        public void run() {
            if (flag == Flag_Init || flag == Flag_Done) {
                flag = Flag_Down;
            }
            downloader.start();
        }
    }

    private DownloadListener downListener = new DownloadListener() {

        int fileSize;
        Intent intent = new Intent();

        @Override
        public void onSuccess(File file) {
            Log.i("DownloadService", "DownloadListener   onSuccess");
            
            intent.setAction(CellLyouatUtil.ACTION_DOWNLOAD_SUCCESS);
            intent.putExtra("progress", 100);
            intent.putExtra("file", file);
            intent.putExtra("fileName", downloader.getFileName());
            sendBroadcast(intent);
        }

        @Override
        public void onStart(int fileByteSize) {
            Log.i("DownloadService", "DownloadListener   onStart");
            fileSize = fileByteSize;
            flag = Flag_Down;
        }

        @Override
        public void onResume() {
            Log.i("DownloadService", "DownloadListener   onResume");
            flag = Flag_Down;
        }

        @Override
        public void onProgress(int receivedBytes) {
            
            if (flag == Flag_Down) {
                progress = (int) ((receivedBytes / (float) fileSize) * 100);
                intent.setAction(CellLyouatUtil.ACTION_DOWNLOAD_PROGRESS);
                intent.putExtra("progress", progress);
                intent.putExtra("fileName", downloader.getFileName());
                sendBroadcast(intent);

                if (progress == 100) {
                    flag = Flag_Done;
                }
            }
            
            Log.i("DownloadService", "DownloadListener   onProgress  progress:"+progress);
        }

        @Override
        public void onPause() {
            Log.i("DownloadService", "DownloadListener   onPause");
            flag = Flag_Pause;
        }

        @Override
        public void onFail() {
            Log.i("DownloadService", "DownloadListener   onFail");
            intent.setAction(CellLyouatUtil.ACTION_DOWNLOAD_FAIL);
            intent.putExtra("fileName", downloader.getFileName());
            sendBroadcast(intent);
            flag = Flag_Init;
        }

        @Override
        public void onCancel() {
            Log.i("DownloadService", "DownloadListener   onCancel");
            progress = 0;
            flag = Flag_Init;
        }
    };
}
