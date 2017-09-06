package com.joy.util;

import android.os.Environment;

public class Constants {

	public static final String BASE_URL = "http://192.168.164.134:8080/app/api.do";

	public static final int TIMEOUT = 120000;

//	public static final String DS_ROOT = "joy";

//	public static final String DOWNLOAD_IMAGE_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT
//			+ "/images";
//
//	public static final String DOWNLOAD_APK_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT
//			+ "/apk";
//
//	public static final String DOWNLOAD_JSON_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT
//			+ "/json";

//	public static final String FILENAME_APP_LIST = "applist.txt";
	public static final String SDCARD = Environment.getExternalStorageDirectory().getPath();

	public static final int SOFT_TYPE_VIRTUAL = 1;

	public static final int SOFT_TYPE_SECRETLY = 2;

	public static final int APK_LIST_NUM = 8;

	public final static int DOWNLOAD_APK_HTTP_OK = 206;

	// ---online wallpaper--add by huangming
	public final static String ONLINE = "online";

	public final static String THUMBNAIL = "_thumbnail";

	public final static String NATIVE = "native";

	public final static String POSITION = "position";

	public final static String CATEGORY_TYPE = "category_type";

	public final static String CATEGORY_NAME = "category_name";

	public final static String CATEGORY_URL = "category_url";

	public final static String CATEGORY_DESCRIPTION = "category_description";

	public final static int CATEGORY_TYPE_NATIVE = 1;

	public final static int CATEGORY_TYPE_ONLINE = 2;
	// end
}
