package com.cappu.cleaner.context;

import android.graphics.drawable.Drawable;

public class fileCleanInfo {

    private int id;// auto increment
    private Drawable icon;//应用图片
    private String label = "";// 文件名
    private String sublabel = "";// 副名
    private String packageName = "";// 包名
    private String version = "";//版本号
    private String fileSavePath = "";// 文件保存路径
    private long filesize = 0;// 文件总大小kb
    private int type;
    private boolean checkstate = true;//选中状态

    public fileCleanInfo() {
    }
    public fileCleanInfo(fileCleanInfo info) {
        setId(info.getId());
        setIcon(info.getIcon());
        setLabel(info.getLabel());
        setSubLabel(info.getSubLabel());
        setPackageName(info.getPackageName());
        setVersion(info.getVersion());
        setFileSavePath(info.getFileSavePath());
        setFilesize(info.getFilesize());
        setType(info.getType());
        setState(info.getState());
    }

    public fileCleanInfo(int id, Drawable icon, String label, String sublabel, String fileSavePath, String packagename, long filesize, int type) {
        setId(id);
        setIcon(icon);
        setLabel(label);
        setSubLabel(sublabel);
        setPackageName(packagename);
        setVersion("0.0.0");
        setFileSavePath(fileSavePath);
        setFilesize(filesize);
        setType(type);
        setState(true);
    }

    /********
     * id
     **********/
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /********
     * icon
     **********/
    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    /********
     * filename
     **********/
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /********
     * subLabel
     **********/
    public String getSubLabel() {
        return sublabel;
    }

    public void setSubLabel(String sublabel) {
        this.sublabel = sublabel;
    }

    /********
     * FileSavePath
     **********/
    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    /********
     * package
     **********/
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /********
     * version
     **********/
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /********
     * filesize
     **********/
    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    /********
     * type
     **********/
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /********
     * status
     **********/
    public boolean getState() {
        return checkstate;
    }

    public void setState(boolean state) {
        this.checkstate = state;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof fileCleanInfo))
            return false;
        fileCleanInfo that = (fileCleanInfo) o;
        if (id != that.id)
            return false;
        return true;
    }

    public String toString() {
        return "DownloadInfo [id=" + id + ", filename=" + label + ", packageName=" + packageName + ", localname=" + fileSavePath + ", filesize=" + filesize
                + ", checkstate=" + checkstate + ", version="+version+", drawable=" + icon.toString() /*+ ", ishandler=" + ( handler != null ) */ + "]";
    }

    public String toStater() {
        return "DownloadInfo [id=" + id + ", filename=" + label + ", packageName=" + (packageName != null) + ", localname=" + (fileSavePath != null) + ", filesize="
                + filesize /* + ", ishandler=" + ( handler != null ) */ + "]";
    }

    public String toshortString() {
        return "DownloadInfo [id=" + id + ", filename=" + label + ", filesize=" + filesize + "]";
    }


}
