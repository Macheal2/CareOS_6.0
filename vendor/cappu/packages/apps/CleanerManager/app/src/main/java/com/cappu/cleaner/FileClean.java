package com.cappu.cleaner;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.cappu.cleaner.context.fileCleanInfo;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hmq on 17-1-10.
 */

public class FileClean {
    private static FileClean mFileClean;
    private Context mContext;
    private String mStrPath;
    private PackageManager pm;
    CacheAsyncTask mCacheAsyncTask = new CacheAsyncTask();
    FileAsyncTask mFileAsyncTask = new FileAsyncTask();
    private ArrayList<fileCleanInfo> mFileList;
    private ArrayList<PackageStats> mCacheList;
    private ArrayList<fileCleanInfo> mWechatList;
    private int mMAXPackageSize;

    private static int current_cache_num;
    private long cacheS;
    private int mState;
    /** 每次只执行一个任务的线程池 */
    private static ExecutorService mSingleThreadExecutor = null;

    private int mSearchType;//1 all, 2 wechat, 3file&files
    private SearchListener mSearchListener;
    private CleanListener mCleanListener;
    public static final int NONE = 0;
    public static final int WORKING_CACHE = 1;
    public static final int WORKING_FILE = 2;
    public static final int FINISH = 3;

    public static final int SEARCH_CACHE = 1;
    public static final int SEARCH_INVALID_FILE_AND_FILES = 2;
    public static final int SEARCH_WECHAT = 3;
    public static final int SEARCH_ALL = 4;

    public static final int SAFE_FILE = 0;
    public static final int RESULT_CACHE = 1;
    public static final int RESULT_APK = 2;
    public static final int RESULT_AD = 3;
    public static final int RESULT_WECHAT = 4;

    public static final int CLEAR_CACHE = 1;
    public static final int CLEAR_FILE = 2;
    public static final int CLEAR_WECHAT = 3;
    public static final int CLEAR_ALL = 4;
    public static final int CLEAR_FINISH = 5;

    public FileClean(Context context) {
        mContext = context;
        initFileList();
    }

    public void initFileList() {
        if (mFileList == null) {
            mFileList = new ArrayList<fileCleanInfo>();
        }
        if (mCacheList == null) {
            mCacheList = new ArrayList<PackageStats>();
        }
        if (mWechatList == null) {
            mWechatList = new ArrayList<fileCleanInfo>();
        }
        mFileList.clear();
        mCacheList.clear();
        mWechatList.clear();
        mState = NONE;
        pm = mContext.getPackageManager();
//        initExecutorService();
    }

    public static synchronized FileClean getInstance(Context context) {
        if (mFileClean == null) {
            mFileClean = new FileClean(context);
            Log.e("hmq","not "+context.getClass().getName());
        }
        Log.e("hmq","has "+ mFileClean.mContext.getClass().getName() +"; "+context.getClass().getName());
        return mFileClean;
    }

    public int getState() {
        return mState;
    }

    public File initSDcard() {
        File path = null;
        //检测SD卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            path = Util.getExternalFile();
        } else {
            Toast.makeText(mContext, "没有SD卡", Toast.LENGTH_LONG).show();
        }
        return path;
    }

    public int getSearchType(){
        return mFileClean.mSearchType;
    }

    private void initExecutorService() {
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();// 每次只执行一个线程任务的线程池
        mSingleThreadExecutor.shutdown();
    }

    public void searchListFiles(String strPath, int type) {
        mStrPath = strPath;
        mSearchType = type;
        switch (type){
            case SEARCH_ALL:
            case SEARCH_CACHE:
//                getCachesThread();
                Log.e("hmq","SEARCH_CACHE");
                if (mCacheAsyncTask != null){
                    mCacheAsyncTask.cancel(true);
                    mCacheAsyncTask = null;
                }
                mCacheAsyncTask = new CacheAsyncTask();
                mCacheAsyncTask.execute();
                break;
            case SEARCH_INVALID_FILE_AND_FILES:
            case SEARCH_WECHAT:
//                startFileThread();
                if(mFileAsyncTask.isCancelled()){
                    mFileAsyncTask.execute();
                }else{
                    mFileAsyncTask.cancel(true);
                    mFileAsyncTask.execute();
                }
                break;
            default:
                Log.e("hmq","FileClean searchListFiles error type code:"+type);
                break;
        }
    }

    private void refreshFileList(String strPath, int type) {
        File dir = new File(strPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {//文件夹
                //MyLog("e", "文件夹 = " + files[i].getAbsolutePath());
                //搜索无效文件夹
                if (type == SEARCH_INVALID_FILE_AND_FILES || type == SEARCH_ALL) {
                    for (int j = 0; j < NameList.ADFileList.length; j++){
                        if (files[i].getAbsolutePath().indexOf(NameList.ADFileList[j]) != -1){
                            Log.e("hmq","################# ADFiles #############"+files[i].getName());
//                            Message msg = new Message();
//                            msg.what = MSG_SEARCH_FILE;//搜索状态
//                            msg.obj = files[i].getAbsolutePath();//搜索到的路径
//                            msg.arg1 = RESULT_AD;//搜索到的类型
//                            msg.arg2 = j;//搜索到的文件名称
//                            handler.sendMessage(msg);
                            fileCleanInfo info = Util.getFileInfo(mContext, RESULT_AD, j, files[i].getAbsolutePath());
                            mFileList.add(info);
                            mSearchListener.onCatchSearchFile(files[i].getAbsolutePath(), RESULT_AD, j);
                            mSearchListener.onSearching(files[i].getAbsolutePath());
                            break;
                        }
                    }
                }
                //微信文件
                if (type == SEARCH_WECHAT || type == SEARCH_ALL){
                    if (files[i].getAbsolutePath().indexOf("tencent/MicroMsg") != -1){
                        for (int j = 0; j < NameList.WechatFileList.length; j++){
                            if (files[i].getName().indexOf(NameList.WechatFileList[j]) != -1){
                                Log.e("hmq","################# wechat #############"+files[i].getName());
//                                Message msg = new Message();
//                                msg.what = MSG_SEARCH_FILE;//搜索状态
//                                msg.obj = files[i].getAbsolutePath();//搜索到的路径
//                                msg.arg1 = RESULT_WECHAT;//搜索到的类型
//                                msg.arg2 = j;//搜索到的文件名称
//                                handler.sendMessage(msg);
                                fileCleanInfo info = Util.getFileInfo(mContext, RESULT_WECHAT, j, files[i].getAbsolutePath());
                                mWechatList.add(info);
                                mSearchListener.onCatchSearchFile(files[i].getAbsolutePath(), RESULT_WECHAT, j);
                                mSearchListener.onSearching(files[i].getAbsolutePath());
                                break;
                            }
                        }
                    }
                }
                refreshFileList(files[i].getAbsolutePath(), type);
            } else {//文件
//                Message msg = new Message();
//                msg.what = MSG_SEARCH_FILE;//搜索状态
//                msg.obj = files[i].getAbsolutePath();//搜索到的路径
//                msg.arg1 = SAFE_FILE;//搜索到的类型
//                if (type != SEARCH_WECHAT) {
//                    if (files[i].getName().toLowerCase().endsWith("apk")) {
//                        mFileList.add(files[i]);
//                        msg.arg1 = RESULT_APK;//搜索到的类型
//                    }
//                }
//                handler.sendMessage(msg);
                if (type != SEARCH_WECHAT) {
                    if (files[i].getName().toLowerCase().endsWith("apk")) {
                        fileCleanInfo info = Util.getPackageInfo(mContext, files[i].getAbsolutePath());
                        mFileList.add(info);
                        mSearchListener.onCatchSearchFile(files[i].getAbsolutePath(), RESULT_APK, 0);
                    }
                }
                mSearchListener.onSearching(files[i].getAbsolutePath());
                try {
                    Thread.sleep(60);
                } catch (Exception e) {

                    e.printStackTrace();
                }

            }
        }
    }

//    Handler handler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_SEARCH_CACHE:
//                    PackageStats pStats = (PackageStats) msg.obj;
//                    if (pStats.cacheSize > 0) {
//                        mSearchListener.onCatchSearchCache(pStats, msg.arg1);
//                    }
//                    String phonecache = mContext.getResources().getString(R.string.str_phone_cache);
//                    mSearchListener.onSearching(phonecache+ " " + pStats.packageName);
//                    break;
//                case MSG_SEARCH_FILE:
//                    if (msg.arg1 != SAFE_FILE) {
//                        mSearchListener.onCatchSearchFile((String) msg.obj, msg.arg1, msg.arg2);
//                    }
//                    break;
//                case MSG_SEARCH_FINISH:
//                    mState = FINISH;
//                    mSearchListener.onSearchState(mState);
//                    break;
//            }
//        }
//    };

    class CacheAsyncTask extends AsyncTask<Integer, Integer, List<PackageStats>> {
        List<PackageStats> list = null;

        // 该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
        @Override
        protected void onPreExecute() {
            if(isCancelled())
                return;
            mState = WORKING_CACHE;
            mSearchListener.onSearchState(mState);
            current_cache_num = 0;
        }

        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
        // 主要用于进行异步操作
        @Override
        protected List<PackageStats> doInBackground(Integer... params) {
            if(isCancelled())
                return null;
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            mMAXPackageSize = packages.size();//共安装多少应用

            MyPackageStateObserver current = new FileClean.MyPackageStateObserver();
            for (PackageInfo pinfo : packages) {
                if(isCancelled())
                    return null;
                String packageName = pinfo.packageName;
                try {
                    Method getPackageSizeInfo = PackageManager.class
                            .getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(pm, packageName, current);

                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            list = Service.getInstance().getRecommendTitles();
            return list;
        }

        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
        @Override
        protected void onProgressUpdate(Integer... values) {
            // pb.setProgress(value);
        }

        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(List<PackageStats> list) {
            if(isCancelled())
                return;
            if (getSearchType() == SEARCH_ALL){
                if (mFileAsyncTask != null){
                    mFileAsyncTask.cancel(true);
                    mFileAsyncTask = null;
                }
                mFileAsyncTask = new FileAsyncTask();
                mFileAsyncTask.execute();
            }else{
//                Message msg = new Message();
//                msg.what = MSG_SEARCH_FINISH;
//                handler.sendMessage(msg);
                mState = FINISH;
                mSearchListener.onSearchState(mState);
            }
        }
    }

    class FileAsyncTask extends AsyncTask<Object, Object, Void> {
        List<PackageStats> list = null;
        int max;

        // 该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
        @Override
        protected void onPreExecute() {
            if(isCancelled())
                return;
            mState = WORKING_FILE;
            mSearchListener.onSearchState(mState);
        }

        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
        // 主要用于进行异步操作
        @Override
        protected Void doInBackground(Object... params) {
            if(isCancelled())
                return null;
            refreshFileList(mStrPath, mSearchType);
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
        @Override
        protected void onProgressUpdate(Object... values) {
            // pb.setProgress(value);
        }

        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(Void aVoid) {
            if(isCancelled())
                return;
            super.onPostExecute(aVoid);
            mState = FINISH;
            mSearchListener.onSearchState(mState);
        }
    }

    public long getCacheSize(){
        return cacheS;
    }

    public int getCacheNum(){
        return current_cache_num;
    }
    public ArrayList<fileCleanInfo> getFileInfo(){
        return mFileList;
    }
    public ArrayList<fileCleanInfo> getWechatInfo(){
        return mWechatList;
    }

    public void clear(int state) {
        boolean c_chche = state == CLEAR_CACHE ? true : (state == CLEAR_ALL ? true : false);
        boolean c_file = state == CLEAR_FILE ? true : (state == CLEAR_ALL ? true : false);
        boolean c_wechat = state == CLEAR_WECHAT ? true : (state == CLEAR_ALL ? true : false);

        if (c_chche) {
            Method[] methods = PackageManager.class.getMethods();
            for (Method method : methods) {
                if ("freeStorageAndNotify".equals(method.getName())) {
                    try {
                        Log.e("hmq", method.toString());
                        method.invoke(pm, Long.MAX_VALUE, new ClearCacheObj());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
            mCacheList.clear();
            mCleanListener.onClearState(CLEAR_CACHE, 0, 0);
        }
        if (c_file) {
            for (int i = 0; i < mFileList.size(); i++) {
                String path = mFileList.get(i).getFileSavePath();
                File dir = new File(path);
                Util.deleteDirWihtFile(dir);
            }
            mFileList.clear();
            mCleanListener.onClearState(CLEAR_FILE, 0, 0);
        }
        if (c_wechat) {
            for (int i = 0; i < mWechatList.size(); i++) {
                String path = mWechatList.get(i).getFileSavePath();
                File dir = new File(path);
                Util.deleteDirWihtFile(dir);
            }
            mWechatList.clear();
            mCleanListener.onClearState(CLEAR_WECHAT, 0, 0);
        }
        mCleanListener.onClearState(CLEAR_FINISH, 0, 0);
    }

    public void cancelScanning(){
        if(mCacheAsyncTask != null && mCacheAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mCacheAsyncTask.cancel(true);
            mCacheAsyncTask = null;
        }

        if(mFileAsyncTask != null && mFileAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mFileAsyncTask.cancel(true);
            mFileAsyncTask = null;
        }
        mState = FINISH;
        mSearchListener.onSearchState(mState);
    }

    class MyPackageStateObserver extends IPackageStatsObserver.Stub {
        public int max;

        @Override
        public void onGetStatsCompleted(PackageStats result, boolean succeeded) throws RemoteException {
            long cacheSize = result.cacheSize;
            String phonecache = mContext.getResources().getString(R.string.str_phone_cache);
            String applicationName = null;
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = pm.getApplicationInfo(result.packageName, 0);
                applicationName = (String) pm.getApplicationLabel(applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                applicationName = result.packageName;
                Log.e("hmq","ERROR MyPackageStateObserver "+applicationName+"; NameNotFoundException="+e.getMessage());
                e.printStackTrace();
            }
            if (applicationInfo == null){
                return;
            }

            mSearchListener.onSearching(phonecache+ " " + applicationName);
            if (result.cacheSize > 0) {
                current_cache_num ++;
                float percent = 1.0f - ((float)current_cache_num / mMAXPackageSize);
                Log.e("hmq","percent" + percent + "; max="+mMAXPackageSize+"; num="+current_cache_num);
                mCacheList.add(result);
                cacheS += cacheSize;
                mSearchListener.onCatchSearchCache(result, percent);
            }
        }
    }

    class ClearCacheObj extends IPackageDataObserver.Stub {

        @Override
        public void onRemoveCompleted(String packageName, final boolean succeeded) throws RemoteException {
            Log.e("hmq","onRemoveCompleted "+packageName+"; 清楚状态： " + succeeded);
//            mHadler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(mContext.getApplicationContext(), "清楚状态： " + succeeded, Toast.LENGTH_SHORT).show();
//                }
//            });
        }
    }

    public static interface SearchListener {
        public void onSearching(String result);

        public void onCatchSearchCache(PackageStats result, float percent);

        public void onCatchSearchFile(String result, int type, int name);

        public void onSearchState(int state);
    }

    public void setOnSearchListener(SearchListener listener) {
        mSearchListener = listener;
    }

    public static interface CleanListener {
        public void onClearState(int type, long size, long Total_size);
    }

    public void setOnCleanListener(CleanListener listener) { mCleanListener = listener; }
}
