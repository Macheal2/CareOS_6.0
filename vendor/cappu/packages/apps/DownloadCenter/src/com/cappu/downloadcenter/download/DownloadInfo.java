package com.cappu.downloadcenter.download;

import java.io.File;
import com.cappu.downloadcenter.common.Constants;
import com.joy.network.impl.ProtocalFactory;
import com.joy.network.util.SystemInfo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "download", onCreated = "CREATE UNIQUE INDEX index_name ON download(label,fileSavePath)")
public class DownloadInfo {

    @Column(name = "id", isId = true,  autoGen = false)
    private int id;// auto increment
    
    @Column(name = "label")
    private String label;// 文件名
    
    @Column(name = "packageName")
    private String packageName;// 包名
    
    @Column(name = "fileSavePath")
    private String fileSavePath;// 文件保存路径
    
    @Column(name = "url")
    private String url;// 链接地址
    
    @Column(name = "filesize")
    private long filesize;// 文件总大小
    
    @Column(name = "completesize")
    private long completesize;// 下载完成文件大小
    
    @Column(name = "state")
    private DownloadState state;// WAITING(0), STARTED(1), FINISHED(2),
                                 // STOPPED(3), ERROR(4)
    
    @Column(name = "installstate")
    private boolean installstate;
    
    @Column(name = "autoResume")
    private boolean autoResume;// 如果目标文件存在，接着未完成的部分继续下载。服务器不支持RANGE时将从新下载。
    
    @Column(name = "autoRename")
    private boolean autoRename;// 如果从请求返回信息中获取到文件名，下载完成后自动重命名。
    // @Transient
    // private HttpHandler<File> handler;//下载线程

    public DownloadInfo() {
    }

    public DownloadInfo(int id, String label, String fileSavePath, String packagename, String url, long filesize) {
        setId(id);
        setLabel(label);
        setFileSavePath(fileSavePath);
        setPackageName(packagename);
        setUrl(url);
        setFilesize(filesize);
        setCompletesize(0L);
        setState(DownloadState.STOPPED);
        setInstallStatus(false);
        setAutoRename(true);
        setAutoResume(true);
    }

    /******** id **********/
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /******** filename **********/
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    /******** FileSavePath **********/
    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        int ind = fileSavePath.indexOf(".apk");
        if (ind == -1) {
            this.fileSavePath = new File(Constants.DOWNLOAD_APK_DIR, fileSavePath + ".apk").getAbsolutePath();
        } else {
            this.fileSavePath = fileSavePath;
        }
    }

    /******** package **********/
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /******** url **********/
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        int ind = url.indexOf("&channel=");
        if (ind == -1) {
            this.url = ProtocalFactory.HOST_DOWN_APP + url + "&channel=" + SystemInfo.channel;
        } else {
            this.url = url;
        }
    }

    /******** filesize **********/
    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    /******** complete **********/
    public long getCompletesize() {
        return completesize;
    }

    public void setCompletesize(long completesize) {
        this.completesize = completesize;
    }

    /******** handler **********/
    // public HttpHandler<File> getHandler() {
    // return handler;
    // }
    //
    // public void setHandler(HttpHandler<File> handler) {
    // this.handler = handler;
    // }

    /******** status **********/
    public DownloadState getState() {
        return state;
    }

    public void setState(DownloadState state) {
        this.state = state;
    }

    /******** install **********/
    public boolean getInstallStatus() {
        return installstate;
    }

    public void setInstallStatus(boolean status) {
        this.installstate = status;
    }
    
    /******** autoresume **********/
    public boolean isAutoResume() {
        return autoResume;
    }

    public void setAutoResume(boolean autoResume) {
        this.autoResume = autoResume;
    }

    /******** autorename **********/
    public boolean isAutoRename() {
        return autoRename;
    }

    public void setAutoRename(boolean autoRename) {
        this.autoRename = autoRename;
    }
    
    
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DownloadInfo))
            return false;
        DownloadInfo that = (DownloadInfo) o;
        if (id != that.id)
            return false;
        return true;
    }

    public String toString() {
        return "DownloadInfo [id=" + id + ", filename=" + label + ", packageName=" + packageName + ", localname=" + fileSavePath + ", url=" + url + ", filesize=" + filesize + ", completesize="
                + completesize + ", status=" + state + ", installstate=" + installstate /*+ ", ishandler=" + ( handler != null ) */+ "]";
    }

    public String toStater() {
        return "DownloadInfo [id=" + id + ", filename=" + label + ", packageName=" + (packageName != null) + ", localname=" + (fileSavePath != null) + ", url=" + (url != null) + ", filesize="
                + filesize + ", completesize=" + completesize + ", status=" + state + ", installstate=" + installstate /* + ", ishandler=" + ( handler != null ) */+ "]";
    }

    public String toshortString() {
        return "DownloadInfo [id=" + id + ", filename=" + label + ", filesize=" + filesize + ", completesize=" + completesize + ", status=" + state + ", installstate=" + installstate
        /* + ", ishandler=" + (handler != null) + (handler != null ? (", (isCancelled="+ handler.isCancelled() +", State=" + handler.getState()+")") : "") */+ "]";
    }
}
