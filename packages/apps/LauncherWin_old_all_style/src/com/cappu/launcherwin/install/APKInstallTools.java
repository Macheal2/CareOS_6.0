package com.cappu.launcherwin.install;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class APKInstallTools {
    
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
     * 获取 一个 apk 的版本号
     * archiveFilePath 安装包路径
     * */
    public static String getAppVersionName(Context context,String archiveFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = info.applicationInfo;
            String version = info.versionName; // 得到版本信息
            return version;
        } catch (Exception e) {
            return null;
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
	public static boolean checkApkInstall(Context context, String packageName,String className) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0); 
			if (packageInfo == null) {  
			    return false;  
			} else {  
			    return true;  
			}
		} catch (Exception e) {
			return false;
		}
	}
    
//    /**
//     * 给定包名类名
//     * 判断手机中是否安装了某个应用*/
//    public static boolean checkApkInstall(Context context,String packageName,String className){
//        try {
//            ComponentName cn = new ComponentName(packageName, className);
//            ActivityInfo info = context.getPackageManager().getActivityInfo(cn, 0);
//            if (info !=null){
//                return true;
//            }else{
//                return false;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//        
//    }
    
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
        Context c = context.createPackageContext("com.cappu.launcherwin", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);  
        //载入这个类  
        Class clazz = c.getClassLoader().loadClass("com.cappu.launcherwin.nativejni.KookNativiTools");  
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

}
