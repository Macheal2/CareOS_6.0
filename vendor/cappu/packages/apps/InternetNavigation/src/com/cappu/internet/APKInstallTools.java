package com.cappu.internet;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class APKInstallTools {
    
    
    /**
     * �����
     * �ж��ֻ����Ƿ�װ��ĳ��Ӧ��*/
    public static boolean checkApkInstall(Context context,String packageName){
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for(int i = 0; i < packages.size(); i++){
            PackageInfo packageInfo = packages.get(i);
            //Log.i("HHJ", "PackageInfo  "+packageInfo.packageName+"   "+packageInfo.applicationInfo.className);
            if(packageInfo.packageName.equals(packageName)){
                return true;
            }else{
                continue;
            }
        }
        return false;
    }
    
    /**
     * ���������
     * �ж��ֻ����Ƿ�װ��ĳ��Ӧ��*/
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
     * �����
     * ��ȡӦ�õİ汾��*/
    public static String getVersionName(Context context, String packageName) {

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            // ��ǰӦ�õİ汾���
            String versionName = info.versionName;
            // ��ǰ�汾�İ汾��
            int versionCode = info.versionCode;
            // ��ǰ�汾�İ���
            String packageNames = info.packageName;
            return versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    /**
     * �ж�Care os
     * @throws NameNotFoundException 
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException */
    public static boolean checkCareOS(Context context) throws NameNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Context c = context.createPackageContext("com.android.cappu", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);  
        Class clazz = c.getClassLoader().loadClass("com.android.cappu.nativejni.KookNativiTools");  
        Object owner = clazz.newInstance();  
        Object obj = clazz.getMethod("isExist", String.class).invoke(owner,"");
        if((Boolean) obj){
            return true;
        }else{
            return false;
        }
    }

}
