package com.cappu.launcherwin;

import android.provider.BaseColumns;
import android.net.Uri;

/**
 * Settings related utilities.
 */
public class LauncherSettings {
    static interface BaseLauncherColumns extends BaseColumns {
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>Type: TEXT</P>
         */
        static final String TITLE = "title";

        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to {@link android.content.Intent#parseUri(String, int)} to create
         * an Intent that can be launched.
         * <P>Type: TEXT</P>
         */
        static final String INTENT = "intent";

        /**
         * The type of the gesture
         *
         * <P>Type: INTEGER</P>
         */
        static final String ITEM_TYPE = "itemType";

    }

    /**
     * Favorites.
     */
    public static final class Favorites implements BaseLauncherColumns {
        
        
        public static final Uri NET_URI = Uri.parse("content://" +LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_NETINFO);
        
        public static final Uri CONTACTS_URI = Uri.parse("content://" +LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_CONTACTS);
        public static final Uri BACKUP_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_BACKUP +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");
        public static Uri getContentBackupUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_BACKUP + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
        /**
         * The content:// style URL for this table
         */
        static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");
        
        static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
        
        public static Uri getContactUri(long id){
            return Uri.parse("content://"+LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_CONTACTS+"/"+id);
        }

        /**
         * The container holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String CONTAINER = "container";

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        public static final int CONTAINER_DESKTOP = -100;
        public static final int CONTAINER_HOTSEAT = -101;
        
        /**
         * 这个表示快捷方式
         */
        public static final int ITEM_TYPE_SHORTCUT = 2;
        
        /**
         * 这个表示联系人
         */
        public static final int ITEM_TYPE_CONTACTS = 3;
        
        /**
         * 这个表示widget
         */
        public static final int ITEM_TYPE_APPWIDGET = 4;
        
        /**第几屏*/
        public static final String SCREEN = "screen";
        
        /**模式选择*/
        public static final String MODE = "modeSelect";

        public static final String CELLX = "cellX";
        public static final String CELLY = "cellY";
        
        public static final String SPANX = "spanX";
        public static final String SPANY = "spanY";
        
        public static final String APPWIDGET_ID = "appWidgetId";

        public static final String BACKGROUND = "background";
        
        public static final String PHONENUMBER = "phoneNumber";
        
        public static final String CONTACTNAME = "contactName";
        
        //public static final String ITEM_TYPE = "itemType";
        
        public static final String ALIAS_TITLE = "aliasTitle";
        
        public static final String CELL_DEF_IMAGE = "cellDefImage";
        
        public static final String ALIAS_TITLE_BACKGROUND = "aliasTitleBackground";
        
        public static final String PIC_URI = "picUri";
        
        
        
        /** 以下是做网络版添加的属性*/
        
        /**apk名*/
        public static final String APP_NAME = "AppName";
        
        /**中文名*/
        public static final String APP_NAME_CN = "AppNameCN";
        
        /**apk显示的icon名字*/
        public static final String APP_ICON_NAME = "AppIconName";
        
        /**下载apk显示的icon地址*/
        public static final String APP_ICON_URL = "AppIconUrl";
        
        /**下载apk地址*/
        public static final String APP_DOWANLOAD_URL = "DownloadUrl";
        
        /**当前APK的版本号*/
        public static final String VERSION = "Version";
        /** 以下是做网络版添加的属性  end*/

    }
}
