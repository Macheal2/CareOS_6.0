package com.cappu.launcherwin.downloadapk.services;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.downloadapk.DownloadProgressListener;
import com.cappu.launcherwin.downloadapk.FileDownloader;
import com.cappu.launcherwin.downloadapk.services.KookLocalService.DownloadTask;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KookLocalService extends Service { 
    
    DownloadTaskManagerThread mDownloadTaskManagerThread;
    
    // Binder given to clients 
    private final IBinder mBinder = new LocalBinder(); 
    // Random number generator 
    private final Random mGenerator = new Random();
    
    public enum DownloadType {
        xml, app, icon,themesZip;
    }
  
    /** 
     * Class used for the client Binder.  Because we know this service always 
     * runs in the same process as its clients, we don't need to deal with IPC. 
     */
    public class LocalBinder extends Binder {
        public KookLocalService getService() { 
            // Return this instance of LocalService so clients can call public methods 
            return KookLocalService.this; 
        }
    } 
    
/*    public class KookServiceImpl extends IKookService.Stub {

        @Override
        public String getValue() throws RemoteException {
            return "Android/OPhone开发讲义";
        }

    }*/
    
    
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (intent == null){
            return;
        }
        
       // 1.new一个线程管理队列
        DownloadTaskManager.getInstance();
        // 2.new一个线程池，并启动(这里只需要第一次执行启动线程即可，后续线程会一直在后台执行)
        if(mDownloadTaskManagerThread == null){
            mDownloadTaskManagerThread = new DownloadTaskManagerThread();
            new Thread(mDownloadTaskManagerThread).start();
        }else{
            Log.i("HHJ", "mDownloadTaskManagerThread 这个线程池一直存在，不需要在实例化了"+(mDownloadTaskManagerThread == null));
        }
        
        
        String types = intent.getStringExtra("types");
        DownloadType type = null;
        if(types.equals("xml")){
            type = DownloadType.xml;
        }else if(types.equals("app")){
            type = DownloadType.app;
        }else if(types.equals("icon")){
            type = DownloadType.icon;
        }else if(types.equals("themesZip")){
            type = DownloadType.themesZip;
        }
        String path = intent.getStringExtra("path");
        String SaveDirStr = intent.getStringExtra("saveDir");
        String appName = intent.getStringExtra("appName");
        
        
        if(types.equals("xml")){
            DownloadTask downloadTask = new DownloadTask(path, new File(SaveDirStr),type,appName);
            new Thread(downloadTask).start();
        }else if(types.equals("app")){
            /*if(mDownloadTaskMap.containsKey(appName)){
                Log.i("HHJ", "应用："+appName+" 已经在下载:"+(mDownloadTaskMap.get(appName) == null));
            }else{
                DownloadTask downloadTask = new DownloadTask(path, new File(SaveDirStr),type,appName);
                new Thread(downloadTask).start();
                mDownloadTaskMap.put(appName, downloadTask);
            }*/
            Log.i("HHJ", "types："+types+"   path:"+path+"  SaveDirStr:"+SaveDirStr);
            DownloadTaskManager downloadTaskMananger = DownloadTaskManager.getInstance();
            downloadTaskMananger.addDownloadTask(new DownloadTask(path, new File(SaveDirStr),type,appName));
            
        }else if(types.equals("icon")){
            /*DownloadTask downloadTask = new DownloadTask(path, new File(SaveDirStr),type,appName);
            new Thread(downloadTask).start();*/
            
            DownloadTaskManager downloadTaskMananger = DownloadTaskManager.getInstance();
            downloadTaskMananger.addDownloadTask(new DownloadTask(path, new File(SaveDirStr),type,appName));
        }else if(types.equals("themesZip")){
            DownloadTaskManager downloadTaskMananger = DownloadTaskManager.getInstance();
            downloadTaskMananger.addDownloadTask(new DownloadTask(path, new File(SaveDirStr),type,appName));
        }
        
        //mDownloadTaskManagerThread.StartThreadRun();
    }
    
    
    @Override
    public IBinder onBind(Intent intent) { 
        return mBinder; 
    } 
    
    public DownloadTask getListDownloadTask() {
        if (mDownloadTaskManagerThread != null) {
            return mDownloadTaskManagerThread.getCurrentDownloadTask();
        }else{
            return null;
        }
    }
  
    /** method for clients */
    public int getRandomNumber() { 
      return mGenerator.nextInt(100); 
    }
    
    /*
     * UI控件画面的重绘(更新)是由主线程负责处理的，如果在子线程中更新UI控件的值，更新后的值不会重绘到屏幕上
     * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
     */
    public final class DownloadTask implements Runnable {
        
        private DownloadType mDownloadType;
        private View mView;
        private String mPath;
        private String mAppName;
        private File mSaveDir;
        private FileDownloader mFileDownloader;
        
        public String getAppName() {
            return mAppName;
        }

        public FileDownloader getFileDownloader() {
            return mFileDownloader;
        }

        public DownloadTask(String path, File saveDir,DownloadType type,String appName) {
            this.mAppName = appName;
            this.mPath = path;
            this.mSaveDir = saveDir;
            this.mDownloadType = type;
        }

        /**
         * 退出下载
         */
        public void exit() {
            if (mFileDownloader != null)
                mFileDownloader.exit();
        }

        DownloadProgressListener mDownloadProgressListener = new DownloadProgressListener() {
            final Intent intent=new Intent();
            public void onDownloadSize(int size) {
                Log.i("HHJ", "下载大小  size："+size+"   mDownloadType:"+mDownloadType+"    mDownloadType ==app:"+(mDownloadType == DownloadType.app));
                if(mDownloadType == DownloadType.xml){
                    intent.putExtra("type", "xml");
                    intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    sendBroadcast(intent);
                } else if(mDownloadType == DownloadType.app) {
                    mDownloadTaskManagerThread.setCurrentDownloadTask(mAppName);
                    intent.putExtra("method", "onDownloadSize");
                    intent.putExtra("type", "app");
                    intent.putExtra("max", mFileDownloader.getFileSize());
                    intent.putExtra("size", size);
                    intent.putExtra("appName", mAppName);
                    intent.putExtra("appNames", mDownloadTaskManagerThread.getDownloadNames());
                    intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    sendBroadcast(intent);
                }
            }

            @Override
            public void downloadSucceed() {
                Log.i("HHJ", "下载成功 mDownloadType:"+mDownloadType);
                if(mDownloadType == DownloadType.xml){
                    intent.putExtra("type", "xml");
                    intent.putExtra("method", "downloadSucceed");
                    intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    sendBroadcast(intent);
                }else if(mDownloadType == DownloadType.app){
                    intent.putExtra("method", "downloadSucceed");
                    intent.putExtra("type", "app");
                    intent.putExtra("appName", mAppName);
                    intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    sendBroadcast(intent);
                    
                    Intent intentInstall = new Intent();
                    intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentInstall.setAction(android.content.Intent.ACTION_VIEW);
                    intentInstall.setDataAndType(Uri.fromFile(mFileDownloader.getSaveFile()), "application/vnd.android.package-archive");
                    startActivity(intentInstall);
                }else if(mDownloadType == DownloadType.icon){
                    intent.putExtra("appName", mAppName);
                    intent.putExtra("type", "icon");
                    intent.putExtra("method", "downloadSucceed");
                    intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    sendBroadcast(intent);
                }else if(mDownloadType == DownloadType.themesZip){
                    intent.putExtra("appName", mAppName);
                    intent.putExtra("type", "themesZip");
                    intent.putExtra("method", "downloadSucceed");
                    intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                    sendBroadcast(intent);
                }
                
                mDownloadTaskManagerThread.rmDownloadTask(mAppName);
                /*if(mDownloadTaskMap.containsKey(mAppName)){
                    mDownloadTaskMap.remove(mAppName);
                    Log.i("HHJ", "将当前的下载线程队列mDownloadTaskList中移掉"+mAppName);
                }*/
            }

            @Override
            public void downloadFailed() {
                if(mDownloadType == DownloadType.xml){
                    intent.putExtra("type", "xml");
                } else if(mDownloadType == DownloadType.app) {
                    intent.putExtra("type", "app");
                    mDownloadTaskManagerThread.rmDownloadTask(mAppName);
                } else if(mDownloadType == DownloadType.icon) {
                    intent.putExtra("type", "icon");
                }
                intent.putExtra("method", "downloadFailed");
                intent.putExtra("appName", mAppName);
                intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                sendBroadcast(intent);
            }

            @Override
            public void downloadUpdate() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void networkLinkFailure() {
                if(mDownloadType == DownloadType.xml){
                    intent.putExtra("type", "xml");
                } else if(mDownloadType == DownloadType.app) {
                    intent.putExtra("type", "app");
                    intent.putExtra("appName", mAppName);
                    mDownloadTaskManagerThread.rmDownloadTask(mAppName);
                }else if(mDownloadType == DownloadType.icon){
                    intent.putExtra("type", "icon");
                    intent.putExtra("appName", mAppName);
                }
                intent.putExtra("method", "networkLinkFailure");
                intent.setAction("com.cappu.launcherwin.downloadapk.services.KookLocalService");
                sendBroadcast(intent);
            }
        };
        public void run() {
            try {
                mFileDownloader = new FileDownloader(getApplicationContext(), mPath, mSaveDir, 1, mDownloadProgressListener);
                
                final Intent intent=new Intent();
                mFileDownloader.download(mDownloadProgressListener);
                
            } catch (Exception e) {
                e.printStackTrace();
                //mUIHander.sendMessage(mUIHander.obtainMessage(-1));
            }
        }
    }
}

/**下载队列的类 负责给出要下载的线程，保存新来的任务*/
class DownloadTaskManager {
    private static final String TAG="DownloadTaskManager";
    /**UI请求下载的队列*/
    private LinkedList<DownloadTask> mLinkedListDownloadTasks;
    /**任务不能重复*/
    private Set<String> taskIdSet;

    private static DownloadTaskManager mDownloadTaskMananger;

    private DownloadTaskManager() {

        mLinkedListDownloadTasks = new LinkedList<DownloadTask>();
        taskIdSet = new HashSet<String>();
        
    }

    public static synchronized DownloadTaskManager getInstance() {
        if (mDownloadTaskMananger == null) {
            mDownloadTaskMananger = new DownloadTaskManager();
        }
        return mDownloadTaskMananger;
    }
    

    /**添加执行任务*/
    public void addDownloadTask(DownloadTask downloadTask) {
        synchronized (mLinkedListDownloadTasks) {
            if (!isTaskRepeat(downloadTask.getAppName())) {
                // 增加下载任务
                mLinkedListDownloadTasks.addLast(downloadTask); 
            }
        }
    }
    /**判断是否有重复的任务*/
    public boolean isTaskRepeat(String fileId) {
        synchronized (taskIdSet) {
            if (taskIdSet.contains(fileId)) {
                return true;
            } else {
                Log.i("HHJ", "下载管理器增加下载任务：");
                taskIdSet.add(fileId);
                return false;
            }
        }
    }
    /**获取要下载的对象*/
    public DownloadTask getDownloadTask() {
        synchronized (mLinkedListDownloadTasks) {
            if (mLinkedListDownloadTasks.size() > 0) {
                Log.i("HHJ", "下载管理器增加下载任务："+"取出任务");
                DownloadTask downloadTask = mLinkedListDownloadTasks.removeFirst();
                taskIdSet.remove(downloadTask.getAppName());
                return downloadTask;
            }
        }
        return null;
    }
}

/**这个类是控制下载的类*/
class DownloadTaskManagerThread implements Runnable {

    private DownloadTaskManager downloadTaskManager;

    /**创建一个可重用固定线程数的线程池*/
    private ExecutorService mExecutorService;
    /**线程池大小*/
    private final int POOL_SIZE = 1;
    /**轮询时间*/
    private final int SLEEP_TIME = 1000;
    /**是否停止*/
    private boolean isStop = false;
    
    /**当前正在下载的线程*/
    private DownloadTask mCurrentDownloadTask;
    
    /**当前线程池里有的线程*/
    private Map<String, DownloadTask> mDownloadTaskMap  = new HashMap<String, DownloadTask>();
    
    /**当前下载队列的所有线程名字*/
    private String[] mAppNames;

    public DownloadTaskManagerThread() {
        downloadTaskManager = DownloadTaskManager.getInstance();
        mExecutorService = Executors.newFixedThreadPool(POOL_SIZE);

    }

    /**获取当前正则下载的线程*/
    public DownloadTask getCurrentDownloadTask(){
        return mCurrentDownloadTask;
    }
    
    /**获取当前正则下载的线程*/
    public void setCurrentDownloadTask(String name){
        if(mDownloadTaskMap.containsKey(name)){
            mCurrentDownloadTask = mDownloadTaskMap.get(name);
        }
    }
    
    public void UnwantedCurrentDownloadTask(){
    }
    
    /**获取当前下载队列的所有线程*/
    public Map<String, DownloadTask> getAllDownloadTask(){
        return mDownloadTaskMap;
    }
    
    /**获取当前下载队列的所有线程名字*/
    public String[] getDownloadNames(){
        if(mAppNames == null || mAppNames.length != mDownloadTaskMap.size()){
            mAppNames = new String[mDownloadTaskMap.size()];
        }
        
        java.util.Iterator it = mDownloadTaskMap.entrySet().iterator();
        int i=0;
        while (it.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            mAppNames[i] = (String) entry.getKey();
            i++;
        }
        
        return mAppNames;
    }
    
    public void rmDownloadTask(String name){
        if(mDownloadTaskMap.containsKey(name)){
            mDownloadTaskMap.remove(name);
            Log.i("HHJ", "下载管理器删除下载任务成功");
        }else{
            Log.i("HHJ", "下载管理器删除下载任务失败");
        }
        
/*        if(mDownloadTaskMap.isEmpty() || mDownloadTaskMap.size()==0){
            try {
                synchronized (this) {
                    Log.i("HHJ", "下载管理器 没有任务,进入等待状态");
                    this.wait();// 保用线程进入等待状态，直到有新的任务被加入时通知唤醒
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
    
    public void StartThreadRun(){
        synchronized (this) {
            Log.i("HHJ", "唤醒通知");
            this.notify();//通知唤醒
        }
    }
    
    @Override
    public void run() {
        // TODO Auto-generated method stub
        while (!isStop) {
            mCurrentDownloadTask = downloadTaskManager.getDownloadTask();
            if (mCurrentDownloadTask != null) {
                /**执行某个线程*/
                mExecutorService.execute(mCurrentDownloadTask);
                /**这里是不可能重复放的,在上面取出来的时候就移除了,所以这里是唯一的一个值*/
                mDownloadTaskMap.put(mCurrentDownloadTask.getAppName(), mCurrentDownloadTask);
            } else {  //如果当前未有downloadTask在任务队列中
                try {
                    // 查询任务完成失败的,重新加载任务队列
                    // 轮询,
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
        if (isStop) {
            mExecutorService.shutdown();
        }

    }

    /**
     * @param isStop
     *            the isStop to set
     */
    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }

}
