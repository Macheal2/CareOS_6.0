package com.cappu.downloadcenter.download;

import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.common.task.PriorityExecutor;
import org.xutils.common.util.LogUtil;
import org.xutils.db.converter.ColumnConverterFactory;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import android.util.Log;

import com.cappu.downloadcenter.common.utils.Util;
import com.joy.network.util.SystemInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * xutils3
 */
public final class DownloadManager {

    static {
        // 注册DownloadState在数据库中的值类型映射
        ColumnConverterFactory.registerColumnConverter(DownloadState.class, new DownloadStateConverter());
    }

    private static volatile DownloadManager instance;

    private final static int MAX_DOWNLOAD_THREAD = 3; // 有效的值范围[1, 3], 设置为3时,
                                                      // 可能阻塞图片加载.

    private final DbManager db;
    private final Executor executor = new PriorityExecutor(MAX_DOWNLOAD_THREAD, true);
    private final List<DownloadInfo> downloadInfoList = new ArrayList<DownloadInfo>();
    private final ConcurrentHashMap<DownloadInfo, DownloadCallback> callbackMap = new ConcurrentHashMap<DownloadInfo, DownloadCallback>(5);

    private DownloadManager() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig().setDbName("download").setDbVersion(1);
        db = x.getDb(daoConfig);
        try {
            List<DownloadInfo> infoList = db.selector(DownloadInfo.class).findAll();
            if (infoList != null) {
                for (DownloadInfo info : infoList) {
                    if (info.getState().value() < DownloadState.FINISHED.value()) {
                        info.setState(DownloadState.STOPPED);
                    }
                    downloadInfoList.add(info);
                }
            }
        } catch (DbException ex) {
            LogUtil.e(ex.getMessage(), ex);
        }
    }

    /* package */
    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 更新数据库信息
     * @param info
     * @throws DbException
     */
    public void updateDownloadInfo(DownloadInfo info) throws DbException {
        db.update(info);
    }

    /**
     * 
     * @return当前下载列表数量
     */
    public int getDownloadListCount() {
        return downloadInfoList.size();
    }

    /**
     * 查找下载列表中是否有下载信息的数据
     * @param index 从第0项开始
     * @return 返回下载信息，如果列表中没有下载数据返回空
     */
    public DownloadInfo getDownloadInfo(int index) {
        for (int i = 0; i < downloadInfoList.size(); i++){
            if (downloadInfoList.get(i).getId() == index){
                return downloadInfoList.get(i);
            }
        }
        return null;
//        return downloadInfoList.get(index);
    }
    
    /**
     * 通过列表索引获得下载数据
     * @return
     */
    public DownloadInfo getDownloadInfoIndex(int index) {
        return downloadInfoList.get(index);
    }
    
    //开始下载
    public synchronized void startDownload(DownloadViewHolder viewHolder) throws DbException {
        String url = viewHolder.getDownloadInfo().getUrl();
        String label = viewHolder.getDownloadInfo().getLabel();
        String savePath = viewHolder.getDownloadInfo().getFileSavePath();
        boolean autoResume = viewHolder.getDownloadInfo().isAutoResume();
        boolean autoRename = viewHolder.getDownloadInfo().isAutoRename();
                
        String fileSavePath = new File(savePath).getAbsolutePath();
        DownloadInfo downloadInfo = db.selector(DownloadInfo.class).where("label", "=", label).and("fileSavePath", "=", fileSavePath).findFirst();
        if (downloadInfo != null) {
            DownloadCallback callback = callbackMap.get(downloadInfo);
            if (callback != null) {
                if (viewHolder == null) {
                    viewHolder = new DefaultDownloadViewHolder(null, downloadInfo);
                }
                if (callback.switchViewHolder(viewHolder)) {
                    return;
                } else {
                    callback.cancel();
                }
            }
        }

        // create download info
        if (downloadInfo == null) {
//            downloadInfo = new DownloadInfo();
//            downloadInfo.setUrl(url);
//            downloadInfo.setAutoRename(autoRename);
//            downloadInfo.setAutoResume(autoResume);
//            downloadInfo.setLabel(label);
//            downloadInfo.setFileSavePath(fileSavePath);
//            downloadInfo.setState(dInfo.getState());
            downloadInfo = viewHolder.getDownloadInfo();
            db.saveBindingId(downloadInfo);
        }

        // start downloading
        if (viewHolder == null) {
            viewHolder = new DefaultDownloadViewHolder(null, downloadInfo);
        } else {
            viewHolder.update(downloadInfo);
        }
        DownloadCallback callback = new DownloadCallback(viewHolder);
        callback.setDownloadManager(this);
        callback.switchViewHolder(viewHolder);
        RequestParams params = new RequestParams(url);
        params.setAutoResume(downloadInfo.isAutoResume());
        params.setAutoRename(downloadInfo.isAutoRename());
        params.setSaveFilePath(downloadInfo.getFileSavePath());
        params.setExecutor(executor);
        params.setCancelFast(false);
        params.addHeader("ts", Util.getTS());
        params.addHeader("deviceId", SystemInfo.deviceid);
        params.addHeader("Accept-Encoding", "gzip");
        params.addHeader("Content-Type", "text/json;charset=UTF-8");
        Callback.Cancelable cancelable = x.http().get(params, callback);//开始下载
        callback.setCancelable(cancelable);
        callbackMap.put(downloadInfo, callback);

        if (downloadInfoList.contains(downloadInfo)) {
            int index = downloadInfoList.indexOf(downloadInfo);
            downloadInfoList.remove(downloadInfo);
            downloadInfoList.add(index, downloadInfo);
        } else {
            downloadInfoList.add(downloadInfo);
        }
    }

    public void stopDownload(int index) {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        stopDownload(downloadInfo);
    }

    public void stopDownload(DownloadInfo downloadInfo) {
        for (Entry<DownloadInfo, DownloadCallback> e : callbackMap.entrySet()) {
            if(downloadInfo.getId() == e.getKey().getId()){
                e.getValue().cancel();
            }
            
        }
        
//        if (cancelable != null) {
//            cancelable.cancel();
//        }
    }

    public void stopAllDownload() {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            Callback.Cancelable cancelable = callbackMap.get(downloadInfo);
            if (cancelable != null) {
                cancelable.cancel();
            }
        }
    }

    public void removeDownload(int index) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(index);
    }

    public void removeDownload(DownloadInfo downloadInfo) throws DbException {
        db.delete(downloadInfo);
        stopDownload(downloadInfo);
        downloadInfoList.remove(downloadInfo);
    }
    
    public void removeAllDownload() throws DbException {
        stopAllDownload();
        for (DownloadInfo downloadInfo : downloadInfoList) {
            db.delete(downloadInfo);
        }
    }
}
