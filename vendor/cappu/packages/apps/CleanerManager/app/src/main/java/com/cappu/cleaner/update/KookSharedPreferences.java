package com.cappu.cleaner.update;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

import com.cappu.cleaner.Util;

public class KookSharedPreferences {
    private static final String FILE_NAME = "KookSharedPreferences";

    public static int getInt(Context context, String keyname) {
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        int v = shared.getInt(keyname, -1);
        if (v == -1) {
            return -1;
        } else {
            return v;
        }
    }

    public static String getString(Context context, String keyname) {
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        String str = shared.getString(keyname, null);
        if (str == null) {
            return null;
        } else {
            return str;
        }
    }
    
    public static long getLong(Context context, String keyname) {
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        long str = shared.getLong(keyname, -1);
        return str;
    }
    
    public static boolean getBoolean(Context context, String keyname) {
        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        boolean str = shared.getBoolean(keyname, false);
        return str;
    }

    public static void putString(Context context, String keyname, String values) {

        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        Editor e = shared.edit();
        e.putString(keyname, values);
        boolean b = e.commit();
        if (b) {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values);
        } else {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values + " save is fail");
        }
    }

    public static void putInt(Context context, String keyname, int values) {

        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        Editor e = shared.edit();
        e.putInt(keyname, values);
        boolean b = e.commit();
        if (b) {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values);
        } else {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values + " save is fail");
        }
    }
    
    public static void putLong(Context context, String keyname, long values) {

        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        Editor e = shared.edit();
        e.putLong(keyname, values);
        boolean b = e.commit();
        if (b) {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values);
        } else {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values + " save is fail");
        }
    }

    public static void putBoolean(Context context, String keyname, boolean values) {

        SharedPreferences shared = context.getSharedPreferences(FILE_NAME, 0);
        Editor e = shared.edit();
        e.putBoolean(keyname, values);
        boolean b = e.commit();
        if (b) {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values);
        } else {
            if(Util.DEBUG)Log.i(Util.TAG, "KookSharedPreferences ----keyname:" + keyname + "   values:" + values + " save is fail");
        }
    }
}
