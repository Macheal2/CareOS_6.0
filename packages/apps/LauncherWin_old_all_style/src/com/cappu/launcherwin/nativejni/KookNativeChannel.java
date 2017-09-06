package com.cappu.launcherwin.nativejni;

import com.cappu.launcherwin.basic.BasicKEY;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

/**管理推送消息通道号*/
public class KookNativeChannel extends KookNative{
    
    /**这里是为了线上版本让第三方检测能否有通道号准备*/
    public boolean isExist(String s){
        if(CappuChannelNet() == null){
            return false;
        }else{
            return true;
        }
    }
    
    public static String GetChannel(Context context){
        /* 版本选择 1 晨想老人机    2晨想老人机线上版本    3 派信老人机版本*/
        if(BasicKEY.LAUNCHER_VERSION == 1){
            return CappuChannel();
        }else if(BasicKEY.LAUNCHER_VERSION == 2){
            return CappuChannelNet();
        }else if(BasicKEY.LAUNCHER_VERSION == 4){
            return CappuChannelNet();
        }else{
            return null;
        }
    }

    /**老人机机器版本*/
    public native static String CappuChannel();
    
    /**老人机线上版本*/
    public native static String CappuChannelNet();
    
    public native static String KLTChannel();
    
    public native static String[] Replace(String packageName,String className);

}
