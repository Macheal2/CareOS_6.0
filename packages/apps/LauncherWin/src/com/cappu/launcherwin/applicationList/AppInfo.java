package com.cappu.launcherwin.applicationList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class AppInfo {

    private String app_name = null;
    //private Drawable app_icon = null;
    private Bitmap app_icon = null;
    private String package_name = null;
    private String activity_name = null;
    private int type = -1;
    private Intent mIntent;

    public AppInfo(String app_name, Bitmap icon, String pkg,String activity_name,int type) {
        super();
        this.app_name = app_name;
        this.app_icon = icon;
        this.package_name = pkg;
        this.activity_name = activity_name;
        this.type = type;
    }

    public AppInfo(String app_name, Bitmap icon, String pkg,String activity_name) {
        super();
        this.app_name = app_name;
        this.app_icon = icon;
        this.package_name = pkg;
        this.activity_name = activity_name;
    }    
    
    public AppInfo(String name, Bitmap icon, String pkg) {
        super();
        this.app_name = name;
        this.app_icon = icon;
        this.package_name = pkg;
    }    
    
    public void setIntent(Intent intent){
        mIntent = intent;
    }   
    
    public Intent getAppInfoIntent(){
        return mIntent;
    }
    
    @Override
    public String toString() {
        return "AppInfo [app_name=" + app_name + ", app_icon=" + app_icon + ", package_name=" + package_name + ", activity_name=" + activity_name+ ", type=" + type + "]";
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public Bitmap getApp_icon() {
        return app_icon;
    }

    public void setApp_icon(Bitmap app_icon) {
        this.app_icon = app_icon;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }
    
    public String getActivity_name() {
        return activity_name;
    }

    public void setActivity_name(String activity_name) {
        this.activity_name = activity_name;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }    
}