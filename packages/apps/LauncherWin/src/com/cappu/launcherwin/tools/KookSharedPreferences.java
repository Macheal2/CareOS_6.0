
package com.cappu.launcherwin.tools;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class KookSharedPreferences {
    private Context mContext;
    private static final String FILE_NAME = "KookSharedPreferences";
    
    public KookSharedPreferences(Context context){
        this.mContext = context;
    }
    
    public static void putInt(Context context,String keyname,int values){
        
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        Editor e = shared.edit();
        e.putInt(keyname, values);
        boolean b = e.commit();
        if(b){
            Log.i("HHJ", "keyname:"+keyname+"   values:"+values);
        }else{
            Log.i("HHJ", "keyname:"+keyname+"   values:"+values+" save is fail");
        }
    }
    
    public static int getInt(Context context,String keyname,int defValues){
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        int v = shared.getInt(keyname, -1);
        if(v == -1){
            return defValues;
        }else{
            return v;
        }
    }
    
    public static int getInt(Context context,String keyname){
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        int v = shared.getInt(keyname, -1);
        if(v == -1){
            return -1;
        }else{
            return v;
        }
    }
    
    public static String getString(Context context,String keyname){
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        String str = shared.getString(keyname, null);
        if(str == null){
            return null;
        }else{
            return str;
        }
    }
    
    public static void putString(Context context,String keyname,String values){
        
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        Editor e = shared.edit();
        e.putString(keyname, values);
        boolean b = e.commit();
        if(b){
            Log.i("HHJ", "keyname:"+keyname+"   values:"+values);
        }else{
            Log.i("HHJ", "keyname:"+keyname+"   values:"+values+" save is fail");
        }
    }
}