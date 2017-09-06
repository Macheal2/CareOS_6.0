package com.cappu.drugsteward.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
    
    private ConnectivityManager manager;

    public Network(Activity activity){
     manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);   
    }

    //判断有没有wifi
    public boolean getwifi(){
        NetworkInfo info=manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(info!=null){
            return info.isAvailable();
        }
        return false;
    }
    
    //判断数据链接
    public boolean getmoblie(){
       NetworkInfo info= manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
       if(info!=null){
           return info.isAvailable();
       }
       return false;
    }
    
    //判断是否有网
    public boolean  getconnect(){
        if(getmoblie()==true||getwifi()==true){
            return true;
        }
        return false;
    }
    
}
