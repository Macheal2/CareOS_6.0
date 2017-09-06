package com.cappu.downloadcenter.common;

import android.os.Environment;

public class Constants {

    // public static final String BASE_URL =
    // "http://192.168.164.134:8080/app/api.do";

    public static final int TIMEOUT = 15000;

    public static final String DS_ROOT = ".folder";

    public static final String DOWNLOAD_IMAGE_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT + "/images";

    public static final String DOWNLOAD_APK_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT + "/apk";

    public static final String DOWNLOAD_JSON_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT + "/json";
    public static final String ICON_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT + "/icon";

    public static final String FILENAME_APP_LIST = "applist.txt";

    public static final int SOFT_TYPE_VIRTUAL = 1;

    public static final int SOFT_TYPE_SECRETLY = 2;

    public static final int APK_LIST_NUM = 8;

    public final static int DOWNLOAD_APK_HTTP_OK = 206;

    public final static String DEFAULT_CHANNL = "specialsoftwarefor55";
//    public final static String DEFAULT_CHANNL = "laorenji02a";
    //public final static String DEFAULT_CHANNL = "careosdlcappu";
    // public final static String DEFAULT_CHANNL = "ch001";

    //渠道号
    //laorenji02a 卡布老人机下载中心
    //careosdlcappu 卡布桌面公开下载中心
    //游戏圈 cxyxq0918
    //生活圈 cxshq0918
    //测试文件夹 onlinefoldertest
    

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

    public final static boolean IS_SMALL_FOLDERBUTTON = true;

    public final static boolean TURN_OFF_SECRETLY = true;

    // end
    // 未知软件类型
    public static final int UNKNOW_APP = 0x0;
    // 用户软件类型
    public static final int USER_APP = 0x1;
    // 系统软件
    public static final int SYSTEM_APP = 0x2;
    // 系统升级软件
    public static final int SYSTEM_UPDATE_APP = 0x4;
    // 系统+升级软件
    public static final int SYSTEM_REF_APP = SYSTEM_APP | SYSTEM_UPDATE_APP;

    public static final String ACTION_PROGRESS = "com.cappu.downloadcenter.progress.update";
	
}
