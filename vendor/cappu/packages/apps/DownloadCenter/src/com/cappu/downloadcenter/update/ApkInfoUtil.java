package com.cappu.downloadcenter.update;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

public class ApkInfoUtil {
    
    /**
     * 
     * 获取 一个 apk 的包名
     * archiveFilePath 安装包路径
     * */
    public static String getAppPackName(Context context,String archiveFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            String packageName = appInfo.packageName; // 得到安装包名称
            return packageName;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取 一个 apk 的版本号对应的vsernam
     * archiveFilePath 安装包路径
     * */
    public static String getAppVersionName(Context context,String archiveFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            String version;
            if(TextUtils.isEmpty(archiveFilePath)){
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
                version = info.versionName;
            }else{
                PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = info.applicationInfo;
                version = info.versionName; // 得到版本信息
            }
            return version;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取 一个 apk 的版本号
     * archiveFilePath 安装包路径 如果为空/就检测当前的软件
     * */
    public static int getAppVersionCode(Context context,String archiveFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            int code;
            if(TextUtils.isEmpty(archiveFilePath)){
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
                code = info.versionCode;
            }else{
                PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = info.applicationInfo;
                code = info.versionCode; // 得到版本信息
            }
            return code;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取 一个 apk 的icon
     * archiveFilePath 安装包路径
     * */
    public static Drawable getAppIcon(Context context,String archiveFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            Drawable icon = pm.getApplicationIcon(appInfo);// 得到图标信息
            return icon;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 
     * 获取 一个 apk 的名字
     * archiveFilePath 安装包路径
     * */
    public static String getAppName(Context context,String archiveFilePath) {
        
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            String appName = pm.getApplicationLabel(appInfo).toString();
            return appName;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 给定包名
     * 判断手机中是否安装了某个应用*/
    public static boolean checkApkInstall(Context context,String packageName){
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for(int i = 0; i < packages.size(); i++){
            PackageInfo packageInfo = packages.get(i);
            if(packageInfo.packageName.equals(packageName)){
                return true;
            }else{
                continue;
            }
        }
        return false;
    }
    
    /**
     * 给定包名类名
     * 判断手机中是否安装了某个应用*/
    public static boolean checkApkInstall(Context context,String packageName,String className){
        try {
            ComponentName cn = new ComponentName(packageName, className);
            ActivityInfo info = context.getPackageManager().getActivityInfo(cn, 0);
            if (info !=null){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
    }
    
    /**
     * 给定包名
     * 获取应用的版本号*/
    public static String getVersionName(Context context, String packageName) {

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            // 当前应用的版本名称
            String versionName = info.versionName;
            // 当前版本的版本号
            int versionCode = info.versionCode;
            // 当前版本的包名
            String packageNames = info.packageName;
            return versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    
    /**
     * 判断Care os
     * @throws NameNotFoundException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException */
    public static boolean checkCareOS(Context context) throws NameNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Context c = context.createPackageContext("com.android.magcomm", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);  
        //载入这个类  
        Class clazz = c.getClassLoader().loadClass("com.android.magcomm.nativejni.KookNativiTools");  
        //新建一个实例  
        Object owner = clazz.newInstance();
        //获取print方法，传入参数并执行
        /*Object obj = clazz.getMethod("print", String.class).invoke(owner, "Hello");*/
        Object obj = clazz.getMethod("isExist", String.class).invoke(owner);
        if((Boolean) obj){
            return true;
        }else{
            return false;
        }
    }
    
     /** 
          * 安装 
          *  
          * @param context 
          *            接收外部传进来的context 
          */  
    public static void install(Context context,String apkAbsPath) {
        // 核心是下面几句代码
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkAbsPath)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
