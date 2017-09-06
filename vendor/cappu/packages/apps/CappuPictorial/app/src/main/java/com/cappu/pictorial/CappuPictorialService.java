/**
 *  
 * Copyright (C) 2016 The Cappu Android Source Project
 * <p>
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.cappu.cn
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2016年12月1日
 * @author: huangminqi@cappu.cn
 * @company: Cappu Co.,Ltd. 
 */
package com.cappu.pictorial;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;

import com.cappu.pictorial.network.CappuNetworkConnect;
import com.cappu.pictorial.network.ParseJsonUtils;
import com.cappu.pictorial.network.ProtocalFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cappu.pictorial.Util.CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL;
import static com.cappu.pictorial.Util.CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_OFF;
import static com.cappu.pictorial.Util.CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_ON;
import static com.cappu.pictorial.Util.CAPPU_SP;
import static com.cappu.pictorial.Util.CAPPU_SP_COUNT_KEY;
import static com.cappu.pictorial.Util.CAPPU_SP_JSON_KEY;
import static com.cappu.pictorial.Util.CAPPU_SP_TIME_KEY;
import static com.cappu.pictorial.Util.CAPPU_SP_VERSION_KEY;
import static com.cappu.pictorial.Util.MyLog;

/**
 * Created by hmq on 16-12-1.
 */

public class CappuPictorialService extends Service {
    private static final String LOG_MSG = "CappuPictorialService  ";
    private static final int DEFAULT_PICTORIAL_COUNT = 5;

    private int mCurrentCount = DEFAULT_PICTORIAL_COUNT;//锁屏画报当前循环图片的总数
    private int mCurrentVersion;//锁屏画报当前版本号
    private List<PictorialInfo> mCurrentList;//锁屏画报当前循环显示图片列表信息

    private int mTempCount = 0;//服务器下载图片的总数
    private int mTempVersion;//服务器下载的版本号
    private List<PictorialInfo> mTempList;//服务器下载图片列表信息,一个临时的列表，为空代表没有需要更新的
    private int mTempIndex = 0;//完成下载任务数，与mTempCount相同，表示下载完成

    private static boolean mSynchServerUpdate = false;//是否与服务器同步最新
    private Long mUpdateLastTime;//最后更新的时间
    private Long mDownloadStartTime;//开始更新，但未在规定时间完成下载任务
    private int mCurrentPictorialIndex;//当前循环的目标指针
    private SharedPreferences mSp;//保存信息 版本号，时间

    private List<Integer> mDefaultDrawableList = new ArrayList<Integer>();//系统默认的画报数组
    GetInfoAsyncTask mAsyncTask;//下载资源的异步线程

//    private Map<String, Bitmap> mHashRefs;//把下载的图片放到MAP中，增加调用速度
//    private Map<String, MySoftRef> mHashRefs;//软引用 已经不使用
//    private ReferenceQueue<Bitmap> queue;//软引用 已经不使用
//    HashMapAsyncTask mHashAsyncTask;//对于mHashRefs的异步显线程用来更新mHashRefs

    @Override
    public IBinder onBind(Intent arg0) {  //这是Service必须要实现的方法，目前这里面什么都没有做
        MyLog("i", LOG_MSG + " -- onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        MyLog("i", LOG_MSG + " -- onRebind");
    }

    @Override
    public void onCreate() {//只调用一次，如果已经启动不会调用
        super.onCreate();
        MyLog("i", LOG_MSG + " -- onCreate");

//        mHashRefs = new Hashtable<String, Bitmap>();
//        queue = new ReferenceQueue<Bitmap>();
        initSharedPreferences();
        initTemp();
//        initHashMap();
        //系统默认添加画报资源
//        mDefaultDrawableList.add(R.drawable.cappu_default_keyguard_pictorial_1);
//        mDefaultDrawableList.add(R.drawable.cappu_default_keyguard_pictorial_2);
//        mDefaultDrawableList.add(R.drawable.cappu_default_keyguard_pictorial_3);
//        mDefaultDrawableList.add(R.drawable.cappu_default_keyguard_pictorial_4);
//        mDefaultDrawableList.add(R.drawable.cappu_default_keyguard_pictorial_5);
    }

    private void initSharedPreferences() {
        mSp = this.getSharedPreferences(CAPPU_SP, MODE_WORLD_WRITEABLE);
        mCurrentCount = mSp.getInt(CAPPU_SP_COUNT_KEY, 0);
        mCurrentVersion = mSp.getInt(CAPPU_SP_VERSION_KEY, 0);
        mUpdateLastTime = mSp.getLong(CAPPU_SP_TIME_KEY, 0);
        MyLog("e","初始化sp 数量="+mCurrentCount+"; 版本="+mCurrentVersion);
        mCurrentList = readPictorialSP();//初始化list
    }

    private void initTemp() {
        mTempCount = 0;
        mTempIndex = 0;
        mTempVersion = 0;
        mTempList = new ArrayList<PictorialInfo>();
        mDownloadStartTime = 0L;
    }

//    private void initHashMap() {
//        if (mCurrentList != null) {
//            updateHashMap();
//        }
//    }

    @Override
    public void onStart(Intent intent, int startId) {//每次启动都会调用
        super.onStart(intent, startId);
        MyLog("i", LOG_MSG + " -- onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog("i", LOG_MSG + " -- onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        MyLog("i", LOG_MSG + " -- onDestroy");
        super.onDestroy();
        mDefaultDrawableList = null;
    }

    WallpaperManager mWallManager = null;

    public void showWallpaper() {
//        MyLog("i", LOG_MSG + " -- showWallpaper  mWallManager is null " + (mWallManager == null));
//        Bitmap wallpaper = null;
//        String path;
//        if (mWallManager == null) {
//            mWallManager = mWallManager = WallpaperManager.getInstance(this);
//        }
//        //WallpaperManager mWallManager = WallpaperManager.getInstance(this);
//        Long currentTime = Util.currentTimeLong();
//
//        if (mCurrentList == null) {
//            MyLog("i", LOG_MSG + " --showWallpaper mCurrentList == null 等于没有获得历史的图片数据");
//            Drawable drawable = null;
//
//            if (Settings.System.getInt(this.getContentResolver(), CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL, CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_OFF) == CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_ON) {
//                java.util.Random random = new java.util.Random();// 定义随机类
//                int result = random.nextInt(mDefaultDrawableList.size());
//                drawable = this.getDrawable(mDefaultDrawableList.get(result));
//            } else {
//                if (mCurrentPictorialIndex <= (mDefaultDrawableList.size() - 1)) {
//                    drawable = this.getDrawable(mDefaultDrawableList.get(mCurrentPictorialIndex));
//                } else {
//                    drawable = this.getDrawable(mDefaultDrawableList.get(0));
//                }
//                mCurrentPictorialIndex += 1;
//            }
//            try {
//                mWallManager.setBitmap(Util.drawableToBitamp(drawable));
//            } catch (IOException e) {
//                MyLog("e", LOG_MSG + "ERROR -- showWallpaper 当前列表为空 IOException msg:" + e.getMessage());
//            }
//        } else {
//            PictorialInfo item;
//
//            if (mCurrentPictorialIndex >= mCurrentCount) {
//                mCurrentPictorialIndex = 0;
//            }
//
//            if (Settings.System.getInt(this.getContentResolver(), CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL, CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_OFF) == CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_ON) {
//                java.util.Random random = new java.util.Random();// 定义随机类
//                int result = random.nextInt(mCurrentList.size());
//                item = mCurrentList.get(result);
//                MyLog("i", LOG_MSG + " -- ## path0=" + item.getFileSavePath());
//            } else {
//                if (mCurrentPictorialIndex <= mCurrentList.size()) {
//                    item = mCurrentList.get(mCurrentPictorialIndex);
//                    MyLog("i", LOG_MSG + " -- ## intex=" + mCurrentPictorialIndex + "; lenght=" + mCurrentList.size() + "; path1=" + item.getFileSavePath());
//                } else {
//                    item = mCurrentList.get(0);
//                    MyLog("i", LOG_MSG + " -- ## path1=" + item.getFileSavePath());
//                }
//                mCurrentPictorialIndex += 1;
//            }
//
//            try {
//                long startTime = System.currentTimeMillis();
//                Bitmap bm = null;//getCachBitmap(item.getId());
//                if (bm != null) {
//                    mWallManager.setBitmap(bm);
//                } else {
//                    bm = BitmapFactory.decodeFile(item.getFileSavePath());
////                    addCacheBitmap(item.getMd5(), bm);
//                    mWallManager.setBitmap(bm);//(Util.getImageDrawable(path));
//                }
//
//
//                MyLog("i", LOG_MSG + " -- showwallpaper over" + "     " + (System.currentTimeMillis() - startTime));
//            } catch (IOException e) {
//                e.printStackTrace();
//                MyLog("e", LOG_MSG + "ERROR --showWallpaper 当前列表包含内容 IOException msg:" + e.getMessage());
//            }
//        }
//        if (!Util.isNetworkConnected(this)) {
//            return;
//        }
//
//        boolean isToday = Util.isSameDate(currentTime, mUpdateLastTime);
//        boolean isDownloadFail = !mSynchServerUpdate && Util.isHeartbeatTimeH(mDownloadStartTime, 1);
//        MyLog("i", LOG_MSG + " -- isToday=" + isToday + "; isDownloadFail=" + isDownloadFail);
//        if (!isToday || isDownloadFail) {
//            detectVersion();
//        }
    }

    public String getWallpaperPath() {
        String path = null;
        Long currentTime = Util.currentTimeLong();
        MyLog("i", LOG_MSG + " 获得墙纸地址 -- mCurrentList=null?" + (mCurrentList == null));
        if (mCurrentList != null) {
            PictorialInfo item;

            if (Settings.System.getInt(this.getContentResolver(), CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL, CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_OFF) == CAPPU_SETTINGS_SYSTEM_RANDOM_PICTORIAL_ON) {
                java.util.Random random = new java.util.Random();// 定义随机类
                int result = random.nextInt(mCurrentList.size());//0到size-1
                item = mCurrentList.get(result);
                path = item.getFileSavePath();
                MyLog("i", LOG_MSG + " -- ## 随机 intex=" + mCurrentPictorialIndex + "; lenght=" + mCurrentList.size() + "; path1=" + item.getFileSavePath());
            } else {
                if (mCurrentPictorialIndex > (mCurrentList.size() - 1)) {
                    mCurrentPictorialIndex = 0;
                }
                item = mCurrentList.get(mCurrentPictorialIndex);
                path = item.getFileSavePath();
                MyLog("i", LOG_MSG + " -- ## 序列 intex=" + mCurrentPictorialIndex + "; lenght=" + mCurrentList.size() + "; path1=" + item.getFileSavePath());
                mCurrentPictorialIndex += 1;
            }
        }
        if (!Util.isNetworkConnected(this)) {
            MyLog("i", LOG_MSG + " -- 没有链接网络");
            return path;
        }

        boolean isToday = Util.isSameDate(currentTime, mUpdateLastTime);
        boolean isDownloadFail = !mSynchServerUpdate && Util.isHeartbeatTimeH(mDownloadStartTime, 1);
        MyLog("i", LOG_MSG + " -- isToday=" + isToday + "; isDownloadFail=" + isDownloadFail);
        if (!isToday || isDownloadFail) {
            detectVersion();
        }
        return path;
    }

    /**
     * 开始异步添加Hashmap Bitmap
     */
//    public void updateHashMap() {
//        AsyncTask.Status status = null;
//
//        if (mHashAsyncTask != null) {
//            status = mHashAsyncTask.getStatus();
//            if (AsyncTask.Status.FINISHED == status) {
//                mHashAsyncTask = null;
//            }
//        }
//        if (mHashAsyncTask == null) {
//            mHashAsyncTask = new HashMapAsyncTask();
//        }
//
//        if (status != AsyncTask.Status.RUNNING) {
//            mHashAsyncTask.execute();
//        }
//    }

//    class HashMapAsyncTask extends AsyncTask<Object, Object, Void> {// 继承AsyncTask
//
//        // 该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
//        @Override
//        protected void onPreExecute() {
//        }
//
//        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
//        // 主要用于进行异步操作
//        @Override
//        protected Void doInBackground(Object... params) {
//            if (mCurrentList != null && mCurrentList.size() > 1) {
//                for (PictorialInfo info : mCurrentList) {
//                    Bitmap bm = getCachBitmap(info.getMd5());
//                    if (bm != null) {
//                        continue;
//                    }
//
//                    if ((info.getFileSavePath() != null) && hasFile(info.getFileSavePath())) {
//                        bm = BitmapFactory.decodeFile(info.getFileSavePath());
//                        addCacheBitmap(info.getMd5(), bm);
//                    }
//                }
//            }
//            return null;
//        }
//
//        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
//        @Override
//        protected void onProgressUpdate(Object... values) {
//            Object value = values[0];
//        }
//
//        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
//        // 主要用于将异步操作任务执行的结果展示给用户
//        @Override
//        protected void onPostExecute(Void a) {
//        }
//    }

    /**
     * 开始异步下载图片资源
     */
    public void detectVersion() {
        AsyncTask.Status status = null;
        if (mAsyncTask != null) {
            status = mAsyncTask.getStatus();
            if (AsyncTask.Status.RUNNING == status) {
                mAsyncTask.cancel(true);
            }
            mAsyncTask = null;
        }
        mAsyncTask = new GetInfoAsyncTask();
        mDownloadStartTime = Util.currentTimeLong();
        mAsyncTask.execute(ProtocalFactory.getServerURL(mCurrentVersion));
    }

    /**
     * 获得握手的信息
     * @author hmq
     *
     */
    class GetInfoAsyncTask extends AsyncTask<String, Integer, List<PictorialInfo>> {// 继承AsyncTask

        // 该方法运行在Ui线程内,初始化进来,可以对UI线程内的控件设置和修改其属性
        @Override
        protected void onPreExecute() {
        }

        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
        // 主要用于进行异步操作
        @Override
        protected List<PictorialInfo> doInBackground(String... params) {
            String postUrl = params[0];

            if(isCancelled())
                return null;
            
            if (TextUtils.isEmpty(postUrl)) {
                return null;
            }
            String result = CappuNetworkConnect.requestByHttpGet(postUrl);
            return getPictorialInfo(result);
        }

        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
        @Override
        protected void onProgressUpdate(Integer... values) {
            int value = values[0];
            if(isCancelled())
                return;
        }

        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(List<PictorialInfo> list) {
            if(isCancelled()) return;
            
            if (list != null) {
                mTempList = list;
                for (int i = 0; i < mTempList.size(); i++) {
                    updatePictorialInfo(mTempList.get(i));
                }
//                        Glide.with(getApplicationContext())
//                                .load(list.get(i).getUrl())
//                                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                .into(new target(list.get(i)));
            }
        }
    }

    private List<PictorialInfo> getPictorialInfo(String result) {
        List<PictorialInfo> list = null;
        int rt = ParseJsonUtils.ParseToItemInt(result, ParseJsonUtils.RT);//josn状态
        mTempCount = ParseJsonUtils.ParseToItemInt(result, ParseJsonUtils.COUNT);//josn 图片总数
        mTempVersion = ParseJsonUtils.ParseToItemInt(result, ParseJsonUtils.VERSION);

        MyLog("i", LOG_MSG + " --握手获得的数据 rt= " + rt + "; 服务器版本号=" + mTempVersion + "; 数量="+ mTempCount + "; 当前使用版本号=" + mCurrentVersion);
        if (rt == 1) {//有完整数据需要更新
            if (mTempVersion > mCurrentVersion) {//比当前的版本号新才升级
                list = ParseJsonUtils.ParseToArray(result, ParseJsonUtils.ITEM, mTempVersion);
//                mCurrentVersion = 0;//mTempVersion;
                mSynchServerUpdate = false;
            }
        } else {
            mSynchServerUpdate = true;
        }
        return list;
    }

    /**
     * 更新画报信息
     *
     * @param item
     */
    private void updatePictorialInfo(PictorialInfo item) {
        if (item == null) {
            return;
        }
//        String existPathFile = Util.searchFile(item.getMd5(), new File(Util.DOWNLOAD_IMAGE_DIR));
//        MyLog("i", LOG_MSG + "获取下载列表 id="+item.getIndex()+"; url="+item.getUrl() + "; existPathFile=" + existPathFile);
//        if (existPathFile == null) {
        GetRedirect302AsyncTask mTask = new GetRedirect302AsyncTask();
        mTask.execute(item);
//        } else {
//            String hasNewPathFile = Util.savePath(item.getVersion() + "-" + item.getMd5() + "-" + item.getIndex(), item.getSuffixes());
//            boolean status = Util.renameFile(existPathFile, hasNewPathFile);
//            MyLog("i", LOG_MSG + "存在文件 重命名完成？"+status+"; oldpath:"+existPathFile+"; newpath="+hasNewPathFile);
//            item.setFileSavePath(hasNewPathFile);
//            item.setState(PictorialInfo.ISFINISH);
//            finishUpdate();
//        }
    }

    class GetRedirect302AsyncTask extends AsyncTask<PictorialInfo, Integer, byte[]> {// 继承AsyncTask
        PictorialInfo info;

        // 该方法运行在Ui线程内，可以对UI线程内的控件设置和修改其属性
        @Override
        protected void onPreExecute() {
        }

        // 该方法并不运行在UI线程内，所以在方法内不能对UI当中的控件进行设置和修改
        // 主要用于进行异步操作
        @Override
        protected byte[] doInBackground(PictorialInfo... params) {
            info = params[0];
            String postUrl = info.getUrl();

            if (TextUtils.isEmpty(postUrl)) {
                return null;
            }
            String result = CappuNetworkConnect.requestByRedirect302Http(postUrl);
            byte[] data = CappuNetworkConnect.requestByDownloadHttp(result);
            return data;
        }

        // 在doInBackground方法当中，每次调用publishProgrogress()方法之后，都会触发该方法
        @Override
        protected void onProgressUpdate(Integer... values) {
            int value = values[0];
        }

        // 在doInBackground方法执行结束后再运行，并且运行在UI线程当中
        // 主要用于将异步操作任务执行的结果展示给用户
        @Override
        protected void onPostExecute(byte[] data) {
            if (data != null) {
                Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                MyLog("i", LOG_MSG + "获得下载图片流数据完成 bitmap是否为空？="+(mBitmap == null));
                String key = info.getVersion() + "-" + info.getMd5() + "-" + info.getIndex();
                String filePath = Util.saveBitmapToSD(key, mBitmap, info.getSuffixes());
                //addCacheBitmap(info.getMd5(), mBitmap);
                if (filePath != null) {
                    mTempIndex += 1;
                    info.setFileSavePath(filePath);
                    info.setState(PictorialInfo.ISFINISH);
                }
                MyLog("i", LOG_MSG + " -- onResourceReady=" + info.toString());
                finishUpdate();
            } else {
                MyLog("e", LOG_MSG + "ERROR 异步下载文件后但是没有获得数据流");
            }
//            Glide.with(getApplicationContext())
//                    .load(newUrl)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(new target(info));
        }
    }

    /**
     * 回调类 glide下载图片的回调类
     */
//    class target extends SimpleTarget {
//        PictorialInfo info;
//
//        public target(PictorialInfo info) {
//            this.info = info;
//        }
//
//        @Override
//        public void onResourceReady(Object resource, GlideAnimation glideAnimation) {
//            GlideBitmapDrawable glidebt = (GlideBitmapDrawable) resource;
//            String filePath = Util.saveBitmapToSD(mCurrentVersion + "-" + info.getMd5() + "-" + info.getIndex(), glidebt.getBitmap(), info.getSuffixes());
//            addCacheBitmap(info.getMd5(), glidebt.getBitmap());
//            info.setFileSavePath(filePath);
//            info.setState(PictorialInfo.ISFINISH);
//            MyLog("i", LOG_MSG + " -- onResourceReady=" + info.toString());
//            updatePictorialInfo(info);
//        }
//    }
    private void finishUpdate() {
        if (mTempList == null || mTempList.size() < 1) {
            return;
        }
        if (mSynchServerUpdate || mTempIndex != mTempCount) {
            return;
        }
        for (int i = 0; i < mTempList.size(); i++) {
            if (mTempList.get(i).getState() == PictorialInfo.ISREADY) {
                return;
            }
        }
        MyLog("i", LOG_MSG + "画报更新完毕！");
        mCurrentCount = mTempCount;
        mCurrentVersion = mTempVersion;
        mCurrentList = new ArrayList<PictorialInfo>(mTempList);//mTempList;
        mSynchServerUpdate = true;
        mUpdateLastTime = Util.currentTimeLong();
        savePictorialSP(mCurrentList);
        initTemp();
        deleteFolderOtherFile();
    }

    private void deleteFolderOtherFile() {
        Util.deleteFolderOtherFile(mCurrentList, new File(Util.DOWNLOAD_IMAGE_DIR));
    }

    public void savePictorialSP(List<PictorialInfo> infoList) {
        JSONArray arr = new JSONArray();
        for (PictorialInfo info : infoList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", info.getId());
                obj.put("index", info.getIndex());
                obj.put("version", info.getVersion());
                obj.put("md5", info.getMd5());
                obj.put("resolution", info.getResolution());
                obj.put("suffixes", info.getSuffixes());
                obj.put("summary", info.getSummary());
                obj.put("time", info.getTime());
                obj.put("url", info.getUrl());
                obj.put("fileSavePath", info.getFileSavePath());
                obj.put("state", info.getState());
            } catch (JSONException e) {
                e.printStackTrace();
                MyLog("e", LOG_MSG + " ERROR --saveSP save error! msg:" + e.getMessage());
            }
            arr.put(obj);
        }
        mSp.edit().putString(CAPPU_SP_JSON_KEY, arr.toString()).commit();
        mSp.edit().putInt(CAPPU_SP_VERSION_KEY, mCurrentVersion).commit();//更新版本号
        mSp.edit().putInt(CAPPU_SP_COUNT_KEY, mCurrentCount).commit();
        mSp.edit().putLong(CAPPU_SP_TIME_KEY, mUpdateLastTime).commit();
    }

    private ArrayList<PictorialInfo> readPictorialSP() {
        String json = mSp.getString(CAPPU_SP_JSON_KEY, "");
        ArrayList<PictorialInfo> infoList = new ArrayList<PictorialInfo>();
        if (json != "") {
            try {
                JSONArray arr = new JSONArray(json);
                int lenth = arr.length();
                for (int i = 0; i < lenth; i++) {
                    PictorialInfo info = new PictorialInfo();
                    JSONObject obj = arr.getJSONObject(i);
                    info.setId(obj.optString("id"));
                    info.setIndex(obj.optInt("index"));
                    info.setVersion(obj.optInt("version"));
                    info.setMd5(obj.optString("md5"));
                    info.setResolution(obj.optString("resolution"));
                    info.setSuffixes(obj.optString("suffixes"));
                    info.setSummary(obj.optString("summary"));
                    info.setTime(obj.optLong("time"));
                    info.setUrl(obj.optString("url"));
                    info.setFileSavePath(obj.optString("fileSavePath"));
                    info.setState(obj.optBoolean("state"));
                    infoList.add(info);
                    MyLog("e","读取SP数据 "+info.toShortString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                MyLog("e", LOG_MSG + "ERROR --readPictorialSP JSONException msg:" + e);
            }
        }
        if (infoList.size() == 0)
            infoList = null;
        return infoList;
    }

//    private Bitmap getCachBitmap(String key) {
//        if (mHashRefs.containsKey(key)) {
//            MySoftRef ref = mHashRefs.get(key);
//            Bitmap bm = ref.get();
//            return mHashRefs.get(key);
//        }
//        return null;
//    }

//    private void addCacheBitmap(String key, Bitmap bmp) {
//        cleanCache();
//        MySoftRef ref = new MySoftRef(bmp, queue, key);
//        mHashRefs.put(key, ref);
//        mHashRefs.put(key, bmp);
//    }

//    private void cleanCache() {
//        MySoftRef ref = null;
//        while ((ref = (MySoftRef) queue.poll()) != null) {
//            mHashRefs.remove(ref.key);
//        }
//    }

//    private class MySoftRef extends SoftReference<Bitmap> {
//        public String key;
//
//        public MySoftRef(Bitmap bmp, ReferenceQueue<Bitmap> queue, String key) {
//            super(bmp, queue);
//            this.key = key;
//        }
//    }

    ICappuPictorial.Stub mBinder = new ICappuPictorial.Stub() {
        @Override
        public void showCappuPictorialImg() throws RemoteException {
            showWallpaper();
        }

        @Override
        public String getCappuPictorialPath() throws RemoteException {
            return getWallpaperPath();
        }


    };


}
