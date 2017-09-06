package com.cappu.download.push;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.cappu.download.PushReceiver;
import com.cappu.download.PushTypeSharedPreferences;
import com.cappu.download.database.PushSettings;
import com.cappu.download.utils.PushConstants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RealSystemFacade;
import android.os.SystemFacade;
import android.util.Log;

/**
 * Performs the background downloads requested by applications that use the
 * Downloads provider.
 */
public class PushService extends Service {
    public static final String TAG = "pushService";
    
    /** Observer to get notified when the content observer's data changes */
    private PushManagerContentObserver mObserver;
    
    private PushTypeSharedPreferences mPushTypeSharedPreferences;


    /**
     * The Service's view of the list of downloads, mapping download IDs to the
     * corresponding info object. This is kept independently from the content
     * provider, and the Service only initiates downloads based on this data, so
     * that it can deal with situation where the data in the content provider
     * changes or disappears.
     * 
     * 这里是根据下载 ID 保存着下载的所有对象 这个对象可以处理或改变 provider 的数据
     */
    private Map<Long, PushInfo> mDownloads = new HashMap<Long, PushInfo>();

    /**
     * The thread that updates the internal download list from the content
     * provider.
     */
    UpdateThread mUpdateThread;

    /**
     * Whether the internal download list should be updated from the content
     * provider.
     * 是否内部下载列表应该从内容提供者进行更新。
     */
    private boolean mPendingUpdate;

    SystemFacade mSystemFacade;

    /**
     * Receives notifications when the data in the content provider changes
     */
    private class PushManagerContentObserver extends ContentObserver {

        public PushManagerContentObserver() {
            super(new Handler());
        }

        /**
         * Receives notification when the data in the observed content provider
         * changes.
         */
        public void onChange(final boolean selfChange) {
            if (PushConstants.LOGVV) {
                Log.v(TAG, "PushManagerContentObserver Service ContentObserver received notification");
            }
            
            updateFromProvider();
        }
    }

    private class PushDetailContentObserver extends ContentObserver{

        public PushDetailContentObserver() {
            super(new Handler());
        }
        
        public void onChange(final boolean selfChange) {
            if (PushConstants.LOGVV) {
                //Log.v(TAG, "Service ContentObserver received notification");
            }
            updateFromProvider();
        }
    }
    /**
     * Returns an IBinder instance when someone wants to connect to this
     * service. Binding to this service is not allowed.
     * 
     * @throws UnsupportedOperationException
     */
    public IBinder onBind(Intent i) {
        throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
    }

    /**
     * Initializes the service when it is first created
     */
    public void onCreate() {
        super.onCreate();
        if (PushConstants.LOGVV) {
            Log.v(TAG, "Service onCreate");
        }

        if (mSystemFacade == null) {
            mSystemFacade = new RealSystemFacade(this);
        }

        mObserver = new PushManagerContentObserver();
        
        if(mPushTypeSharedPreferences == null){
            mPushTypeSharedPreferences = new PushTypeSharedPreferences(getApplicationContext());
        }
        
        //getContentResolver().registerContentObserver(Downloads.ALL_DOWNLOADS_CONTENT_URI, true, mObserver);

        //mNotifier = new DownloadNotification(this, mSystemFacade);
        //mSystemFacade.cancelAllNotifications();

        //updateFromProvider();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int returnValue = super.onStartCommand(intent, flags, startId);
        if (PushConstants.LOGVV) {
            Log.v(TAG, "Service onStart");
        }
        if(intent == null){
            Log.v(TAG, "intent is null ");
            return returnValue;
        }
        getContentResolver().registerContentObserver(PushSettings.PushType.PUSH_URI, true, mObserver);
        updateFromProvider();
        return returnValue;
    }

    /**
     * Cleans up when the service is destroyed
     */
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        if (PushConstants.LOGVV) {
            Log.v(TAG, "Service onDestroy");
        }
        super.onDestroy();
    }

    /**
     * Parses data from the content provider into private array
     */
    private void updateFromProvider() {
        synchronized (this) {
            mPendingUpdate = true;
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread();
                mSystemFacade.startThread(mUpdateThread);
            }
        }
    }

    private class UpdateThread extends Thread {

        public UpdateThread() {
            super("Download Service");
        }

        public void run() {
            Log.v(TAG, "\n\n\n\n\n\nUpdateThread  start");
            /*
             * int THREAD_PRIORITY_AUDIO //标准音乐播放使用的线程优先级 int
             * THREAD_PRIORITY_BACKGROUND //标准后台程序 int THREAD_PRIORITY_DEFAULT
             * // 默认应用的优先级 int THREAD_PRIORITY_DISPLAY //标准显示系统优先级，主要是改善UI的刷新
             * int THREAD_PRIORITY_FOREGROUND //标准前台线程优先级 int
             * THREAD_PRIORITY_LESS_FAVORABLE //低于favorable int
             * THREAD_PRIORITY_LOWEST //有效的线程最低的优先级 int
             * THREAD_PRIORITY_MORE_FAVORABLE //高于favorable int
             * THREAD_PRIORITY_URGENT_AUDIO //标准较重要音频播放优先级 int
             * THREAD_PRIORITY_URGENT_DISPLAY //标准较重要显示优先级，对于输入事件同样适用。
             */
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);// //设置线程优先级为后台，这样当多个线程并发后很多无关紧要的线程分配的CPU时间将会减少，有利于主线程的处理

            trimDatabase(PushSettings.PushType.PUSH_URI);// 修剪数据，防止数据库越来越大
            removeSpuriousFiles(PushSettings.PushType.PUSH_URI);// 删除虚假文件

            /**keepService 这个值为false后服务就会挂掉，true服务会一直远行*/
            boolean keepService = false;
            // for each update from the database, remember which download is
            // supposed to get restarted soonest in the future
            // 从数据库每次更新，请记住哪下载应该接受重新启动在未来最快
            long wakeUp = Long.MAX_VALUE;
            for (;;) {
                synchronized (PushService.this) {
                    if (mUpdateThread != this) {
                        throw new IllegalStateException("multiple UpdateThreads in DownloadService");
                    }
                    if (!mPendingUpdate) {
                        mUpdateThread = null;
                        if (!keepService) {
                            stopSelf();
                        }

                        Log.i(TAG, "多少时间后是否重启这个服务" + ((wakeUp != Long.MAX_VALUE)) + "    wakeUp:" + wakeUp);
                        if (wakeUp != Long.MAX_VALUE) {
                            wakeUp = wakeUp > 30000?30000:wakeUp;//自定义三十秒钟重启服务
                            scheduleAlarm(wakeUp);
                        }
                        return;
                    }
                    mPendingUpdate = false;
                }

                long now = mSystemFacade.currentTimeMillis();
                keepService = false;
                wakeUp = Long.MAX_VALUE;
                Set<Long> idsNoLongerInDatabase = new HashSet<Long>(mDownloads.keySet());

                Cursor cursor = getContentResolver().query(PushSettings.PushType.PUSH_URI, null, PushSettings.BasePushColumns.COLUMN_STATUS + " <> '200'", null, null);
                if (cursor == null) {
                    continue;
                }
                try {
                    PushInfo.Reader reader = new PushInfo.Reader(getContentResolver(), cursor);// 获得下载的一切信息
                    int idColumn = cursor.getColumnIndexOrThrow(PushSettings.BasePushColumns._ID);// 得到下载
                                                                                                  // 列表中
                                                                                                  // 列ID的引擎字段位置

                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {// 对下载
                        long id = cursor.getLong(idColumn);// 获取当前下载的ID
                        idsNoLongerInDatabase.remove(id);// 将正在下载的列 标注
                                                         // 在移除的IDS里面(这里是防止同一个下载数据出现同时请求的情况)
                        PushInfo info = mDownloads.get(id);

                        /*if(info != null){
                            Log.v(TAG, "开始下载前 UpdateThread  updateDownload info:" + info.mPushNameCH+"   info is status:"+info.mStatus);
                        }else{
                            Log.v(TAG, "开始下载前 info is null ");
                        }*/
                        
                        if (info != null) {// 这里这个if else 里面将是文件下载更新的重要部分
                            updateDownload(reader, info, now);
                        } else {
                            info = insertDownload(reader, now);
                        }
                        
                        if (info.hasCompletionNotification()) {
                            keepService = true;
                        }
                        //Log.v(TAG, "开始下载后 UpdateThread  updateDownload info:" + info.mPushNameCH+"   info is status:"+info.mStatus);
                        long next = info.nextAction(now);// 查询当前的下载情况下/如果下载失败将重启此服务/next是启动时间
                        if (next == 0) {//让服务保持不死
                            keepService = true;
                        } else if (next > 0 && next < wakeUp) {
                            wakeUp = next;
                        }
                        
                    }
                } finally {
                    cursor.close();
                }

                for (Long id : idsNoLongerInDatabase) {
                    deleteDownload(id);
                }

                // is there a need to start the DownloadService? yes, if there
                // are rows to be deleted.是否有必要启动下载服务？是，如果有要被删除的行。
                for (PushInfo info : mDownloads.values()) {
                    if (info.mDeleted) {
                        keepService = true;
                        break;
                    }
                }

                // mNotifier.updateNotification(mDownloads.values());//更新通知栏数据

                // look for all rows with deleted flag set and delete the rows
                // from the database
                // permanently
                for (PushInfo info : mDownloads.values()) {
                    if (info.mDeleted) {
                        PushHelpers.deleteFile(getContentResolver(), PushSettings.PushType.PUSH_URI, info.mId, info.mFileName, info.mMimeType);
                    }
                }
            }
        }

        private void scheduleAlarm(long wakeUp) {
            AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarms == null) {
                Log.e(TAG, "couldn't get alarm manager");
                return;
            }

            if (PushConstants.LOGVV) {
                Log.v(TAG, "scheduling retry in " + wakeUp + "ms");
            }

            Intent intent = new Intent(PushConstants.ACTION_RETRY);
            intent.setClassName(getPackageName(), PushReceiver.class.getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(PushService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            alarms.set(AlarmManager.RTC_WAKEUP, mSystemFacade.currentTimeMillis() + wakeUp, pendingIntent);
        }
    }

    /**
     * Removes files that may have been left behind in the cache directory
     * 删除可能已经在缓存目录中被抛在后面的文件
     */
    private void removeSpuriousFiles(Uri mUri) {
        File[] files = Environment.getDownloadCacheDirectory().listFiles();
        if (files == null) {
            // The cache folder doesn't appear to exist (this is likely the case
            // when running the simulator).
            return;
        }
        HashSet<String> fileSet = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(PushConstants.KNOWN_SPURIOUS_FILENAME)) {
                continue;
            }
            if (files[i].getName().equalsIgnoreCase(PushConstants.RECOVERY_DIRECTORY)) {
                continue;
            }
            fileSet.add(files[i].getPath());
        }

        Cursor cursor = getContentResolver().query(mUri, new String[] { PushSettings.BasePushColumns._DATA }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    fileSet.remove(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Iterator<String> iterator = fileSet.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            if (PushConstants.LOGVV) {
                Log.v(TAG, "deleting spurious file " + filename);
            }
            Log.i("hehangjun", "5 ------------------------------------------state.mFilename:"+filename);
            new File(filename).delete();
        }
    }

    /**
     * Drops old rows from the database to prevent it from growing too large
     * 从数据库中删除旧行，以防止过大,当数据库数据超过一千行的时候将自动瘦身数据库
     * 
     */
    private void trimDatabase(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, new String[] { PushSettings.BasePushColumns._ID },
                PushSettings.BasePushColumns.COLUMN_STATUS + " >= '200'", null, PushSettings.COLUMN_LAST_MODIFICATION);
        if (cursor == null) {
            // This isn't good - if we can't do basic queries in our database,
            // nothing's gonna work
            Log.e(TAG, "null cursor in trimDatabase");
            return;
        }
        if (cursor.moveToFirst()) {
            int numDelete = cursor.getCount() - PushConstants.MAX_DOWNLOADS;
            int columnId = cursor.getColumnIndexOrThrow(PushSettings.BasePushColumns._ID);
            while (numDelete > 0) {
                Uri downloadUri = ContentUris.withAppendedId(uri, cursor.getLong(columnId));
                getContentResolver().delete(downloadUri, null, null);
                if (!cursor.moveToNext()) {
                    break;
                }
                numDelete--;
            }
        }
        cursor.close();
    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     * 保留有关下载的信息的本地副本，并在适当的启动下载。
     */
    private PushInfo insertDownload(PushInfo.Reader reader, long now) {
        PushInfo info = reader.newDownloadInfo(this,PushSettings.PushType.PUSH_URI, mSystemFacade,mPushTypeSharedPreferences);
        mDownloads.put(info.mId, info);

        if (PushConstants.LOGVV) {
            info.logVerboseInfo();
        }

        info.startIfReady(now);
        return info;
    }

    /**
     * Updates the local copy of the info about a download.
     * 更新有关下载信息的本地副本。
     * 
     * 里面注释部分是关于刷新通知栏部分/由于现在不需要/固然注释掉
     */
    private void updateDownload(PushInfo.Reader reader, PushInfo info, long now) {
        //int oldVisibility = info.mVisibility;
        //int oldStatus = info.mStatus;

        reader.updateFromDatabase(info);

        /*boolean lostVisibility = oldVisibility == PushSettings.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && info.mVisibility != PushSettings.VISIBILITY_VISIBLE_NOTIFY_COMPLETED && PushSettings.isStatusCompleted(info.mStatus);
        boolean justCompleted = !PushSettings.isStatusCompleted(oldStatus) && PushSettings.isStatusCompleted(info.mStatus);
        if (lostVisibility || justCompleted) {
            mSystemFacade.cancelNotification(info.mId);
        }*/

        info.startIfReady(now);
    }

    /**
     * Removes the local copy of the info about a download.
     * 删除有关下载信息的本地副本。
     */
    private void deleteDownload(long id) {
        PushInfo info = mDownloads.get(id);
        if (info.mStatus == PushSettings.STATUS_RUNNING) {
            info.mStatus = PushSettings.STATUS_CANCELED;
        }
        //if (info.mDestination != PushSettings.DESTINATION_EXTERNAL && info.mFileName != null) {
            //Log.i("DownloadProvider", "delete File:"+info.mFileName+"    info.mDestination:"+info.mDestination+"    389");
            //new File(info.mFileName).delete();
        //}
        //mSystemFacade.cancelNotification(info.mId);
        mDownloads.remove(info.mId);
    }
}
