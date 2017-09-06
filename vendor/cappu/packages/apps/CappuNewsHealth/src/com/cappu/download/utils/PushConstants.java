package com.cappu.download.utils;

import java.io.File;

import android.os.Environment;

public class PushConstants {
    
    public static String TAG = "pushDownload";
    public static boolean LOGVV = true;

    /**链接超时设置*/
    public static final int TIMEOUT = 10000;//120000

    public static final String DS_ROOT = ".push";
    public static final String DOWNLOAD_IMAGE_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT + "/images";
    public static final String DOWNLOAD_APK_DIR = Environment.getExternalStorageDirectory().getPath() + "/" + DS_ROOT + "/apk";
    public static final String CHANNEL_PATH = Environment.getExternalStorageDirectory() + File.separator + ".warning" + File.separator;// 用于存放渠道号，防止升级的安装包里面没有渠道号
    public static final String ICON_PATH = Environment.getExternalStorageDirectory() + File.separator + ".icon" + File.separator;
    public final static int DOWNLOAD_APK_HTTP_OK = 206;

    public final static boolean DEBUG = true;

    public final static int VERSION = 4;

    public final static boolean TURN_OFF_SECRETLY = false;// 是否关闭静默安装
    public final static boolean IS_UPDATE_APP = false;// 当这个APP是更新包时为true,否则为false

    // public final static int DEBUG_FRIST_RECEIVER_TIME = 5000;//调试用，首次push间隔时间
    public final static int DEBUG_FRIST_RECEIVER_TIME = -1;// 从后台获取时间

    /**免激活和获取设置，快速弹出   默认false*/
    public static boolean QUICK_PUSH = false;// 免激活和获取设置，快速弹出
    
    
    /**action 推送的后台服务主要做两件事1.下载apk 2.获取推送显示*/
    public final static String PUSH_ACTION = "com.kook.cappu.push";
    public final static String PUSH_DOWNLOAD_ACTION = "com.kook.cappu.download";
    
    
    
    
    
    /**0 是获取推送的服务端设置*/
    public final static int PUSH_SETTINGS_TYPE = 0x0;

    /**1 是获取推送一条推送*/
    public final static int PUSH_ONE_MESSAGE_TYPE = 0x1;

    /**2 是获取推送列表*/
    public final static int PUSH_LIST_TYPE = 0x2;
    
    public final static String PUSH_DETAIL_INFO = "info";

    public final static String PUSH_DETAIL_TYPE = "type";

    public final static String PUSH_DETAIL_TITLE = "title";

    public final static String PUSH_DETAIL_DESCRIPTION = "description";

    public final static String PUSH_DETAIL_ICON = "icon";

    public final static String PUSH_DETAIL_ID = "id";

    public final static String PUSH_DETAIL_SIZE = "size";

    public final static String PUSH_DETAIL_PACKAGE_NAME = "packageName";

    public final static String PUSH_DETAIL_URL = "url";
    
    
    
    
    /** A magic filename that is allowed to exist within the system cache 
     *  缓存文件名是允许系统缓存中存在
     * */
    public static final String RECOVERY_DIRECTORY = "recovery";
    
    /** Where we store downloaded files on the external storage
     * 我们存储在外部存储下载的文件 表示默认下载的路径
     *  */
    public static final String DEFAULT_DL_SUBDIR = "/.Cappu/pushPIC";
    
    /** The default base name for downloaded files if we can't get one at the HTTP level 
     *   如果从http这里没有获取到文件名，我们就给一个默认名字
     * */
    public static final String DEFAULT_DL_FILENAME = "downloadfile";
    
    /** The default extension for html files if we can't get one at the HTTP level
     * 如果获取的是html文件，如果不能获取到名字就给一个默认后缀
     *  */
    public static final String DEFAULT_DL_HTML_EXTENSION = ".html";
    
    /** The default extension for text files if we can't get one at the HTTP level 
     * 如果获取的是text文件，如果不能获取到名字就给一个默认后缀
     * */
    public static final String DEFAULT_DL_TEXT_EXTENSION = ".txt";

    /** The default extension for binary files if we can't get one at the HTTP level 
     * 如果获取的是二进制 文件，如果不能获取到名字就给一个默认后缀
     * */
    public static final String DEFAULT_DL_BINARY_EXTENSION = ".bin";
    
    /**
     * When a number has to be appended to the filename, this string is used to separate the
     * base filename from the sequence number
     * 
     * 当一个文件存在的时候，我们就将文件名设置为 “文件-数字”组合
     */
    public static final String FILENAME_SEQUENCE_SEPARATOR = "-";
    
    /** The column that is used to count retries
     * 用于计算重试列
     *  */
    public static final String FAILED_CONNECTIONS = "numfailed";
    
    /** The column that used to be used for the HTTP method of the request
     * 这个列将用于请求http 方法
     *  */
    public static final String RETRY_AFTER_X_REDIRECT_COUNT = "method";
    
    /** The column that is used for the downloads's ETag
     * 即用于下载的ETag（分段下载结束时）列
     *  */
    public static final String ETAG = "etag";
    
    /**
     * The time between a failure and the first retry after an IOException.
     * Each subsequent retry grows exponentially, doubling each time.
     * The time is in seconds.
     */
    public static final int RETRY_FIRST_DELAY = 30;
    
    
    /** The intent that gets sent when the service must wake up for a retry 
     *  唤醒一个服务
     * */
    public static final String ACTION_RETRY = "android.intent.action.DOWNLOAD_WAKEUP";

    /** the intent that gets sent when clicking a successful download 
     * 成功下载时发送的广播
     * */
    public static final String ACTION_OPEN = "android.intent.action.DOWNLOAD_OPEN";

    /** the intent that gets sent when clicking an incomplete/failed download 
     *  下载失败或下载不完全是发送的广播
     *  */
    public static final String ACTION_LIST = "android.intent.action.DOWNLOAD_LIST";

    /** the intent that gets sent when deleting the notification of a completed download 
     *  删除下载完成的通知时被发送意图
     * */
    public static final String ACTION_HIDE = "android.intent.action.DOWNLOAD_HIDE";
    
    /** The default user agent used for downloads */
    public static final String DEFAULT_USER_AGENT = "AndroidDownloadManager";
    
    /** A magic filename that is allowed to exist within the system cache 
     *  魔术文件名是允许系统缓存中存在
     * */
    public static final String KNOWN_SPURIOUS_FILENAME = "lost+found";
    
    
    /** The maximum number of rows in the database (FIFO) 
     * 数据库里面允许存储的最大行数
     * */
    public static final int MAX_DOWNLOADS = 500;
    
    /** The buffer size used to stream the data 
     *  用于流式传输的数据缓冲区的大小
     * */
    public static final int BUFFER_SIZE = 4096;
    
    
    /** The minimum amount of progress that has to be done before the progress bar gets updated 
     *  每次更新数据的最小数据量
     * */
    public static final int MIN_PROGRESS_STEP = 4096;

    /** The minimum amount of time that has to elapse before the progress bar gets updated, in ms 
     * 有进度条之前的等待时间的最低金额被更新，以毫秒为单位
     * */
    public static final long MIN_PROGRESS_TIME = 1500;
    
    /**
     * The number of times that the download manager will retry its network
     * operations when no progress is happening before it gives up.
     * 当下在没有进展时让网络重试最多次数
     * 
     */
    public static final int MAX_RETRIES = 5;
    
    
    /**
     * The maximum number of redirects.
     * 重定向的最大数目。
     */
    public static final int MAX_REDIRECTS = 5; // can't be more than 7.

    /**
     * The minimum amount of time that the download manager accepts for
     * a Retry-After response header with a parameter in delta-seconds.
     * 时间的最低金额的下载管理器接受与增量秒参数的重发后响应头。
     */
    public static final int MIN_RETRY_AFTER = 30; // 30s
    
    /**
     * The maximum amount of time that the download manager accepts for
     * a Retry-After response header with a parameter in delta-seconds.
     * 该下载管理器接受与增量秒参数的重发后响应头的最大时间量。
     */
    public static final int MAX_RETRY_AFTER = 24 * 60 * 60; // 24h
    
    /** The column that is used for the initiating app's UID 
     *  初始插入时给的一个uid
     * */
    public static final String UID = "uid";
    
    /** The column that used to be used to reject system filetypes
     * 拒绝系统的文件类型
     *  */
    public static final String NO_SYSTEM_FILES = "no_system";
    
    /** The column that used to be used for the magic OTA update filename 
     *  使用无线升级更新以后的文件名字
     *  */
    public static final String OTA_UPDATE = "otaupdate";
}
