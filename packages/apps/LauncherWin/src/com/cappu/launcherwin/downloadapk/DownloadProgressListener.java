package com.cappu.launcherwin.downloadapk;

public interface DownloadProgressListener {
    /**下载的大小*/
    public void onDownloadSize(int size);
    
    /**下载成功*/
    public void downloadSucceed();

    /**下载失败*/
    public void downloadFailed();
    
    /**网络链接失败*/
    public void networkLinkFailure();

    /**下载更新*/
    public void downloadUpdate();
}
