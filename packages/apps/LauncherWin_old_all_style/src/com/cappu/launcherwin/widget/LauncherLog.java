package com.cappu.launcherwin.widget;

import android.util.Log;

/**
 * 
 * @author hejianfeng
 *
 */
public final class LauncherLog {
	private static final String MODULE_NAME = "LauncherWin";
	private static final boolean DEBUG = false;
	
	public static void e(String tag, String msg){
		if(DEBUG)
		Log.e(MODULE_NAME+"_"+tag, msg);
	}
	
	public static void v(String tag, String msg){
		if(DEBUG)
		Log.v(MODULE_NAME+"_"+tag, msg);
	}
}
