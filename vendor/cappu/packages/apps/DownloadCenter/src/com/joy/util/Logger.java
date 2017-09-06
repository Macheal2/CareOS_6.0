package com.joy.util;

import android.util.Log;

public class Logger {

	static final boolean isDebug = false;
	public static void debug(Object context, String msg) {
		if (isDebug) {
			Log.d(getObjectTag(context), msg);
		}
	}

	public static void info(Object context, String msg) {
		if (isDebug) {
			Log.i(getObjectTag(context), msg);
		}
	}

	public static void warn(Object context, String msg) {
		if (isDebug) {
			Log.w(getObjectTag(context), msg);
		}
	}

	public static void error(Object context, String msg) {
		if (isDebug) {
			Log.e(getObjectTag(context), msg);
		}
	}

	private static String getObjectTag(Object o) {
		return "Launcher -- " + o.getClass().getSimpleName();
	}
}
