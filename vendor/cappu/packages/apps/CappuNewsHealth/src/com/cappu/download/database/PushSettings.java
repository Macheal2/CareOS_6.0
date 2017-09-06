package com.cappu.download.database;

import android.provider.BaseColumns;
import android.net.Uri;

/**
 * Settings related utilities.
 */
public class PushSettings {
    
    public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETED";
    
    /**
     * This download will be saved to the location given by the file URI in
     * 此下载将被保存在该文件的URI给定的位置
     */
    public static final int DESTINATION_FILE_URI = 4;
    
    /**
     * This download has started
     * 此下载已开始
     * @hide
     */
    public static final int STATUS_RUNNING = 192;
    
    /**
     * This download hasn't stated yet
     * 该下载尚未开始
     */
    public static final int STATUS_PENDING = 190;
    
    /**
     * This download was canceled
     * 此下载已取消
     * @hide
     */
    public static final int STATUS_CANCELED = 490;
    
    /**
     * This download encountered some network error and is waiting before
     * retrying the request.
     * 此下载遇到一些网络错误，重试前等待的请求。
     */
    public static final int STATUS_WAITING_TO_RETRY = 194;
    
    /**
     * This download has been paused by the owning app.
     * 此下载已暂停由所拥有的应用程序。
     */
    public static final int STATUS_PAUSED_BY_APP = 193;
    
    /**
     * This download is waiting for network connectivity to proceed.
     * 此下载正在等待网络连接进行。
     */
    public static final int STATUS_WAITING_FOR_NETWORK = 195;
    
    /**
     * This download exceeded a size limit for mobile networks and is waiting
     * for a Wi-Fi connection to proceed.
     * 此下载超过大小限制移动网络，正在等待WiFi连接进行。
     */
    public static final int STATUS_QUEUED_FOR_WIFI = 196;
    
    
    /**
     * 此下载已成功完成。警告：可能还有其他的状态值，表明未来的成功。用的是成功（）为*捕获整个类别。
     */
    public static final int STATUS_SUCCESS = 200;
    
    /**
     * This download can't be performed because the content type cannot be
     * handled.
     * 不能进行此下载，因为内容类型不能处理。
     */
    public static final int STATUS_NOT_ACCEPTABLE = 406;
    
    /**
     * The requested destination file already exists.
     * 所请求的目标文件已经存在。
     */
    public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;
    
    /**
     * This download has completed with an error. Warning: there will be other
     * status values that indicate errors in the future. Use isStatusError() to
     * capture the entire category.
     * 此下载已完成，出现错误。警告：会有其他状态值，表明在未来的错误。采用的是状态错误（）来捕获整个类别。
     */
    public static final int STATUS_UNKNOWN_ERROR = 491;
    
    
    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full. Use the more
     * specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR} and
     * {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
     * 此下载不能因为某个存储问题的完成。通常情况下，这是因为文件系统丢失或完整。使用更具体的
     */
    public static final int STATUS_FILE_ERROR = 492;
    
    /**
     * This download couldn't be completed due to insufficient storage space.
     * Typically, this is because the SD card is full.
     * 此下载无法完成因存储空间不足。通常情况下，这是因为SD卡已满。
     */
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;
    
    /**
     * This download couldn't be completed because no external storage device
     * was found. Typically, this is because the SD card is not mounted.
     * This download couldn't be completed because no external storage device was found. Typically, this is because the SD card is not mounted.
     */
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;
    
    /**
     * Some possibly transient error occurred, but we can't resume the download.
     * 发生了一些可能瞬时错误，但我们不能恢复下载。
     */
    public static final int STATUS_CANNOT_RESUME = 489;
    
    /**
     * This download couldn't be completed because of an HTTP redirect response
     * that the download manager couldn't handle.
     * 此下载不能因为HTTP重定向响应的下载管理器无法处理的完成。
     */
    public static final int STATUS_UNHANDLED_REDIRECT = 493;
    
    /**
     * This download couldn't be completed because of an unspecified unhandled
     * HTTP code.
     * 此下载不能因为一个未指定未处理的HTTP代码来完成。
     */
    public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
    
    /**
     * This download couldn't be completed because of an error receiving or
     * processing data at the HTTP level.
     * 此下载不能因为在HTTP级别的错误接收或处理数据的完成。
     * 
     */
    public static final int STATUS_HTTP_DATA_ERROR = 495;
    
    /**
     * This download couldn't be completed because there were too many
     * redirects.
     * 此下载无法完成，因为有太多的重定向。
     */
    public static final int STATUS_TOO_MANY_REDIRECTS = 497;

    
    
    /**
     * This download is allowed to run.
     * 此下载允许运行。
     */
    public static final int CONTROL_RUN = 0;

    /**
     * This download must pause at the first opportunity.
     * 此下载必须一有机会暂停。
     */
    public static final int CONTROL_PAUSED = 1;
    
    
    /**
     * This download will be saved to the external storage. This is the default
     * behavior, and should be used for any file that the user can freely
     * access, copy, delete. Even with that destination, unencrypted DRM files
     * are saved in secure internal storage. Downloads to the external
     * destination only write files for which there is a registered handler. The
     * resulting files are accessible by filename to all applications.
     * 此下载将被保存到外部存储。这是默认的行为，应该用于用户可以自由访问任何文件，复制，删除。即使该目标，未加密的DRM文件保存在安全的内部存储。下载到外部目标只写其中有一个注册的处理程序文件。生成的文件名通过对所有应用程序进行访问。
     * @hide
     */
    public static final int DESTINATION_EXTERNAL = 0;
    
    /**
     * The name of the column containing the date at which some interesting
     * status changed in the download. Stored as a System.currentTimeMillis()
     * value.
     * <P>
     *  这个列会根据插入的时间（当前系统时间的毫秒）来改变下载状态状态，有时候储存名也用这个值来
     * </P>
     */
    public static final String COLUMN_LAST_MODIFICATION = "lastmod";
    
    /**
     * This download is visible and shows in the notifications while in progress
     * and after completion.
     * 此下载是可见的，并示出了在通知，而在进度和完成后。
     * @hide
     */
    public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
    
    
    
    /** The column that is used for the downloads's ETag
     * 即用于下载的ETag（分段下载结束时）列
     *  */
    public static final String ETAG = "etag";
    
    /**
     * The name of the column holding a bitmask of allowed network types. This
     * is only used for public API downloads.
     * 列保持允许的网络类型的一个位掩码的名称。这仅用于公共API的下载。
     */
    public static final String COLUMN_ALLOWED_NETWORK_TYPES = "allowed_network_types";
    
    /**
     * Returns whether the download has completed (either with success or
     * error).
     * 返回下载是否已完成（无论成功或错误）。
     * @hide
     */
    public static boolean isStatusCompleted(int status) {
        return (status >= 200 && status < 300) || (status >= 400 && status < 600);
    }
    
    /**
     * Returns whether the status is an error (i.e. 4xx or 5xx).
     * 返回状态是否为错误
     */
    public static boolean isStatusError(int status) {
        return (status >= 400 && status < 600);
    }
    
    public static interface BasePushColumns extends BaseColumns {
        /**下载通道号*/
        public static String DOWNLOAD_CHANNEL = "channel";
        public static String COLUMN_STATUS = "status";
        
        /**
         *     下载的URL ，既下载地址
         */
        public static final String COLUMN_URI = "uri";
        
        /**
         * 这个是下载文件在储存设备上的实际名字
         */
        public static final String _DATA = "_data";
        
        /**
         * 这个列的作用就是当下载没有文件名的时候，就用这个默认使用这个名字
         */
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        
        /**下面的类型是国际规定的 具体可以参考 http://tool.oschina.net/commons/*/
        /**获取的apk*/
        public static String PUSH_DOWNLOAD_TYPE_APK = "application/vnd.android.package-archive";
        /**获取的是图片*/
        public static String PUSH_DOWNLOAD_TYPE_PNG = "image/png";
        /**获取的是JSON*/
        public static String PUSH_DOWNLOAD_TYPE_TEXT = "text/xml";
        /**
         * 下载的数据类型，这个类型有可以确定你的文件后缀名
         */
        public static final String COLUMN_MIME_TYPE = "mimetype";
        
        /**
         * 用于计算重试列
         *  */
        public static final String FAILED_CONNECTIONS = "numfailed";
        
        /** 
         *  http 重试请求的次数
         *  */
        public static final String RETRY_AFTER_X_REDIRECT_COUNT = "method";
        
        /**
         * 下载文件的总大小
         */
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        
        /**
         * 当前已经下载的大小
         */
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";
        
        /**
         * 如果下载被删除，设置为true。
         */
        public static final String COLUMN_DELETED = "deleted";
        
        /**
         * 显示下载的名字
         */
        public static final String COLUMN_TITLE = "title";
        
        /**
         * 其中发起应用程序可以提供的名称此下载的说明。描述将被显示给用户在下载列表中。
         */
        public static final String COLUMN_DESCRIPTION = "description";
        
        
        /**
         * The name of the column containing the current control state of the
         * download. Applications can write to this to control (pause/resume) the
         * download. the CONTROL_* constants for a list of legal values.
         * 应用可以插入 值来 控制 （暂停/ 恢复） 下载
         */
        public static final String COLUMN_CONTROL = "control";
        
        /**这个是需要关联其他数据时候ID字段*/
        public static final String COLUMN_ATTACHED_ID = "Other_attached_id";
        
        public static final int TEXT_STATUS_FAIL = 0;
        public static final int TEXT_STATUS_SUCCESS = 1;
    }
    
    
    /**
     * Constants related to HTTP request headers associated with each download.
     */
    public static class RequestHeaders {
        public static final String HEADERS_DB_TABLE = "request_headers";
        public static final String COLUMN_DOWNLOAD_ID = "download_id";
        public static final String COLUMN_HEADER = "header";
        public static final String COLUMN_VALUE = "value";

        /**
         * Path segment to add to a download URI to retrieve request headers
         */
        public static final String URI_SEGMENT = "headers";

        /**
         * Prefix for ContentValues keys that contain HTTP header lines, to be
         * passed to DownloadProvider.insert().
         */
        public static final String INSERT_KEY_PREFIX = "http_header_";
    }
    
    public static final class PushType implements BasePushColumns{//这个只是负责下载的数据库，其他豆不用管
        static final String DOWNLOAD_TYPE_TABLE = "downloadType";
        
        public static final String PUSH_TYPE = "type";//推送的类型 //激活 设置 列表
        public static final String PUSH_TYPE_NAME = "typeNameCH";//推送的类型中文名（方便查看）
        public static final String PUSH_PROTOCOL = "protocol";//协议内容（比如获取内容ID ，以及其他之类的）
        
        public static final Uri PUSH_URI = Uri.parse("content://" +PushProvider.AUTHORITY + "/" + DOWNLOAD_TYPE_TABLE);
        
        public static Uri getDownloadUri(long id) {
            return Uri.parse("content://" + PushProvider.AUTHORITY + "/" + DOWNLOAD_TYPE_TABLE + "/" + id);
        }
    }

    public static final class PushAPKInfo implements BasePushColumns {
        static final String DOWNLOAD_APK_TABLE = "downloadApk";
        
        static final String APP_ID = "id";
        static final String APP_ICON = "icon";
        static final String APP_ICON_STATUS = "iconStatus";
        static final String APP_STATUS = "status";
        static final String APP_SIZE = "size";
        static final String APP_NAME = "name";
        static final String APP_PKG = "packagename";
        static final String APP_PI = "pi";
        static final String APP_PN = "pn";
        static final String APP_RN = "rn";
        static final String APP_PS = "ps";
         /**这个是apk内置应用的标志位*/
        static final String APP_INTERNAL = "internal";
        
        public static final Uri Download_URI = Uri.parse("content://" +PushProvider.AUTHORITY + "/" + DOWNLOAD_APK_TABLE);
        
        public static Uri getDownloadUri(long id) {
            return Uri.parse("content://" + PushProvider.AUTHORITY + "/" + DOWNLOAD_APK_TABLE + "/" + id);
        }
        
        /**这个表示后续下载的apk*/
        public static final int APP_DOWNLOAD_INDEX = 0;
        /**这个是apk内置应用的标志位*/
        public static final int APP_INTERNAL_INDEX = 1;
        
    }
    
    
    public static final class PushTextInfo implements BasePushColumns{
        static final String DOWNLOAD_TEXT_TABLE = "downloadText";
        
        
        /**服务器端传过来存在本地的一个唯一去服务器获取的ID值*/
        public static final String TEXT_ID = "id";
        public static final String TEXT_FLAG = "flag";
        public static final String TEXT_ICON = "icon";
        public static final String TEXT_ICON_PATH = "icon_path";
        /**这边这个状态表示icon下载成功与否 0表示失败，1表示成功*/
        public static final String TEXT_ICON_STATUS = "iconStatus";
        public static final String TEXT_BANNER = "banner";
        public static final String TEXT_BANNER_PATH = "banner_path";
        /**这边这个状态表示banner下载成功与否 0表示失败，1表示成功*/
        public static final String TEXT_BANNER_STATUS = "bannerStatus";
        public static final String TEXT_TYPE = "type";
        public static final String TEXT_TITLE = "title";
        public static final String TEXT_INTRODUCE = "introduce";
        public static final String TEXT_PACKAGENAME = "packageName";
        public static final String TEXT_URL = "url";
        public static final String TEXT_SIZE = "size";
        public static final String TEXT_SITE = "site";
        /**表示推送内容去服务器获取的时间*/
        public static final String TEXT_DATE = "date";
        /**这个是收藏的标志位*/
        public static final String TEXT_FAVORITES = "favorites";
        public static final String TEXT_STATUS = "pushStatus";
        
        
        public static final Uri PUSH_TEXT_URI = Uri.parse("content://" +PushProvider.AUTHORITY + "/" + DOWNLOAD_TEXT_TABLE);
        
        public static Uri getDownloadUri(long id) {
            return Uri.parse("content://" + PushProvider.AUTHORITY + "/" + DOWNLOAD_TEXT_TABLE + "/" + id);
        }
    }
}
