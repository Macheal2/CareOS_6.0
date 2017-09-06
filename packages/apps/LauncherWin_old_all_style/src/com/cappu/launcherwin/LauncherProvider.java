package com.cappu.launcherwin;

import com.cappu.launcherwin.LauncherSettings.Favorites;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.tools.AppComponentNameReplace;
import com.cappu.launcherwin.widget.LauncherLog;

import android.app.SearchManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.TypedArray;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Xml;
import android.util.AttributeSet;
import android.widget.Toast;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.basic.theme.ThemeRes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "LauncherProvider";
    private static final boolean LOGD = false;

    private static final String DATABASE_NAME = "launcher.db";
    
    private static int DATABASE_VERSION = -1;
    
    static final String AUTHORITY = "com.cappu.launcherwin";
    
    /**添加常用列表表*/
    static final String TABLE_FAVORITES = "favorites";
    static final String PARAMETER_NOTIFY = "notify";
    
    static final String TABLE_BACKUP = "backup";
    /**联系人表*/
    static final String TABLE_CONTACTS = "Contacts";
    
    public static final String TABLE_NETINFO = "netinfo";
    
    public static final String TAG_FAVORITES = "favorites";//这个是开始的标签
    
    public static final String TAG_FAVORITE = "favorite";
    public static final String TAG_CONTACTS = "contacts";
    public static final String TAG_APP = "AppComponentName";
    
    private SQLiteOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        if(DATABASE_VERSION == -1){
            DATABASE_VERSION = getContext().getResources().getInteger(R.integer.app_database_version);
            Log.i("HHJ", "数据库版本  "+DATABASE_VERSION);
        }
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;//多条数据
        } else {
            return "vnd.android.cursor.item/" + args.table;//单条数据
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);
        if (rowId <= 0) return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(args.table, null, values[i]) < 0) return 0;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        try {
            count = db.update(args.table, values, args.where, args.args);
        } catch (Exception e) {
            Log.i(TAG, "LauncherProvider Exception    "+e.toString());
        }
        if (count > 0) sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;

        private AppComponentNameReplace mAppComponentNameReplace;
        
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            mAppComponentNameReplace = new AppComponentNameReplace(context);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD) Log.d(TAG, "onUpgrade triggered");
            
            int version = oldVersion;
            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                onCreate(db);
            }
        }
        
        

        @Override
        public void onCreate(SQLiteDatabase db) {
            
            db.execSQL("CREATE TABLE if not exists favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "modeSelect TEXT," +                                      // 模式选择的类型
                    "title TEXT," +                                      // 应用名字
                    "intent TEXT," +                                     //必须的intent
                    "background TEXT," +                                 //磁块背景
                    "container INTEGER," +                               //占时没用的字段
                    "screen INTEGER," +                                  //第几屏
                    "cellX INTEGER," +                                   //第几列
                    "cellY INTEGER," +                                   //第几行
                    "spanX INTEGER," +                                   //夸几列
                    "spanY INTEGER," +                                   //夸几行
                    "itemType INTEGER," +                                //磁块类型  有widget（小部件） shortcut（快捷启动） contact（联系人）
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +         //widget 小部件的ID
                    "phoneNumber TEXT," +                                //联系人电话号码
                    "contactName TEXT," +                                //联系人名字
                    "cellDefImage TEXT," +                               //当磁块为联系人的时候 默认的联系人头像
                    "aliasTitle TEXT," +                                 //别名，比如日历改名为黄历
                    "aliasTitleBackground TEXT," +                       //显示文字的背景
                    "picUri TEXT," +                                     //当是联系人的时候的头像地址
                    "displayMode INTEGER" +
                    ");");
            //hejianfeng add start
            db.execSQL("CREATE TABLE if not exists backup (" +
                    "_id INTEGER PRIMARY KEY," +
                    "modeSelect TEXT," +                                      // 模式选择的类型
                    "title TEXT," +                                      // 应用名字
                    "intent TEXT," +                                     //必须的intent
                    "background TEXT," +                                 //磁块背景
                    "container INTEGER," +                               //占时没用的字段
                    "screen INTEGER," +                                  //第几屏
                    "cellX INTEGER," +                                   //第几列
                    "cellY INTEGER," +                                   //第几行
                    "spanX INTEGER," +                                   //夸几列
                    "spanY INTEGER," +                                   //夸几行
                    "itemType INTEGER," +                                //磁块类型  有widget（小部件） shortcut（快捷启动） contact（联系人）
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +         //widget 小部件的ID
                    "phoneNumber TEXT," +                                //联系人电话号码
                    "contactName TEXT," +                                //联系人名字
                    "cellDefImage TEXT," +                               //当磁块为联系人的时候 默认的联系人头像
                    "aliasTitle TEXT," +                                 //别名，比如日历改名为黄历
                    "aliasTitleBackground TEXT," +                       //显示文字的背景
                    "picUri TEXT," +                                     //当是联系人的时候的头像地址
                    "displayMode INTEGER" +
                    ");");
            //hejianfeng add end
            db.execSQL("CREATE TABLE if not exists netinfo (" +
                    "_id INTEGER PRIMARY KEY," +
                    "type INTEGER," +
                    "date TEXT," +
                    "title TEXT," +
                    "introduce TEXT," +
                    "address TEXT," +
                    "icon TEXT," +
                    "banner TEXT," +
                    "favorites TEXT" +
                    ");");
            
            /*联系人的数据库*/
            db.execSQL("CREATE TABLE if not exists Contacts  (" +
                    "_id INTEGER PRIMARY KEY," +
                    "name TEXT," +
                    "contact_id TEXT," +
                    "number TEXT," +
                    "groups TEXT," +
                    "head TEXT,"+
                    "headPhoto BLOB" +
                    ");");
            
            init();
            //hejianfeng add start
            if(backupList.size()>0){
            	backupList.clear();
            }
            loadFavorites(db);//读书快捷方式，跟联系人view
            //将内置的主题风格布局导入数据库
            File[] files=ThemeManager.getInstance().getDefaultThemeListFile();
            LauncherLog.v(TAG,
					"onCreate, jeff files="+files);
			if (files != null) {
				for (File file : files) {
					LauncherLog.v(TAG,
							"onCreate, jeff file.getPath()=" + file.getPath());
					File layoutFile = new File(file.getPath()
							+ "/default_workspace.xml");
					loadFavorites(db, ThemeManager.getInstance()
							.getThemeWorkspace(layoutFile));
				}
			}
            //hejianfeng add end
        }
        //hejianfeng add start
        private Map<String,ContentValues> backupList=new HashMap<String,ContentValues>();
        
        private void loadDefaultBackupList(TypedArray a,ContentValues values,String tagName){
        	String cellX=a.getString(R.styleable.Favorite_x);
        	String cellY=a.getString(R.styleable.Favorite_y);
        	String screen=a.getString(R.styleable.Favorite_screen);
        	String background=a.getString(R.styleable.Favorite_background);
        	String modeSelect=a.getString(R.styleable.Favorite_modeSelect);
        	String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            ContentValues contentvalues=new ContentValues(values);
            LauncherLog.v(TAG, "loadDefaultBackupList,jeff values="+packageName+"/"+className+"/"+background);
            if (TAG_FAVORITE.equals(tagName)) {
            	backupList.put(TAG_FAVORITE+"/"+screen+"/"+cellX+"/"+cellY+"/"+modeSelect, contentvalues);
            }else if(TAG_CONTACTS.equals(tagName)){
            	backupList.put(TAG_CONTACTS+"/"+screen+"/"+cellX+"/"+cellY+"/"+modeSelect, contentvalues);
            }else if(TAG_APP.equals(tagName)){
            	backupList.put(TAG_APP+"/"+screen+"/"+cellX+"/"+cellY+"/"+modeSelect, contentvalues);
            }
        }
        private void loadBackupList(Element element,ContentValues values,String tagName){
        	String cellX=element.getAttribute("x");
        	String cellY=element.getAttribute("y");
        	String screen=element.getAttribute(LauncherSettings.Favorites.SCREEN);
        	String background=element.getAttribute(LauncherSettings.Favorites.BACKGROUND);
        	String modeSelect=element.getAttribute(LauncherSettings.Favorites.MODE);
        	String packageName = element.getAttribute("packageName");
            String className = element.getAttribute("className");
            ContentValues contentvalues=new ContentValues(values);
            LauncherLog.v(TAG, "loadBackupList,jeff values="+packageName+"/"+className+"/"+background);
            if (TAG_FAVORITE.equals(tagName)) {
            	backupList.put(TAG_FAVORITE+"/"+screen+"/"+cellX+"/"+cellY+"/"+modeSelect, contentvalues);
            }else if(TAG_CONTACTS.equals(tagName)){
            	backupList.put(TAG_CONTACTS+"/"+screen+"/"+cellX+"/"+cellY+"/"+modeSelect, contentvalues);
            }else if(TAG_APP.equals(tagName)){
            	backupList.put(TAG_APP+"/"+screen+"/"+cellX+"/"+cellY+"/"+modeSelect, contentvalues);
            }
        }
        //hejianfeng add end
        private void init(){
            try {
                Settings.Global.putInt(mContext.getContentResolver(), "mms_speech_status",mContext.getResources().getInteger(R.integer.mms_speech_status));
                Settings.Global.putInt(mContext.getContentResolver(), "launcher_speech_status",mContext.getResources().getInteger(R.integer.launcher_speech_status));
                Settings.Global.putInt(mContext.getContentResolver(), "contacts_speech_status",mContext.getResources().getInteger(R.integer.contacts_speech_status));
                Settings.Global.putInt(mContext.getContentResolver(), "dialpad_speech_status",mContext.getResources().getInteger(R.integer.dialpad_speech_status));
                Settings.Global.putInt(mContext.getContentResolver(), "netinfo_speech_status",mContext.getResources().getInteger(R.integer.netinfo_speech_status));
                Settings.Global.putInt(mContext.getContentResolver(), "back_tuoch",mContext.getResources().getInteger(R.integer.back_tuoch));
                Settings.Global.putInt(mContext.getContentResolver(), "workspace_tuoch",mContext.getResources().getInteger(R.integer.workspace_tuoch));
                Settings.Global.putInt(mContext.getContentResolver(), "netinfo_speech_status",mContext.getResources().getInteger(R.integer.netinfo_speech_status));
                /*launcher 模式选择  1表示默认模式  2 便是简单模式   3 表示极简模式*/
                Settings.Global.putInt(mContext.getContentResolver(), BasicKEY.MODE_KEY,mContext.getResources().getInteger(R.integer.launcher_workspace_mode));
                Settings.Global.putInt(mContext.getContentResolver(), "launcher_version",BasicKEY.LAUNCHER_VERSION);
                Settings.Global.putInt(mContext.getContentResolver(), "textSize",mContext.getResources().getDimensionPixelSize(R.dimen.xl_text_size));
                Settings.Global.putInt(mContext.getContentResolver(), "netinfo_speech_status",mContext.getResources().getInteger(R.integer.netinfo_speech_status));
                /*桌面锁*/
                Settings.Global.putInt(mContext.getContentResolver(), "workspace_lock",mContext.getResources().getInteger(R.integer.workspace_lock));
                /* 铃声开关  jojo.zhou*/
               Settings.Global.putInt(mContext.getContentResolver(), /*Settings.Global.BOOTAUDIO_SETTING*/BasicKEY.BOOTAUDIO_SETTING,mContext.getResources().getInteger(R.integer.bootaudio_setting));
               Settings.Global.putInt(mContext.getContentResolver(), BasicKEY.LAUNCHER_BANCKGROUND,Color.parseColor(mContext.getResources().getString(R.string.launcher_background)));
               Settings.Global.putString(mContext.getContentResolver(), BasicKEY.LAUNCHER_WEATHER_SPEECH,mContext.getResources().getString(R.string.launcher_weather_speech));
               Settings.Global.putInt(mContext.getContentResolver(), BasicKEY.WEATHER_SPEECH_STATUS,mContext.getResources().getInteger(R.integer.weather_speech_status));
            } catch (Exception e) {
            }
        }
        //hejianfeng add start for load theme default_workspace.xml
        /**
         * author hejianfeng
         * 主题中的布局风格
         * @param db
         * @return 
         */
        private void loadFavorites(SQLiteDatabase db, Document doc){
			if (doc != null) {
				Intent intent = new Intent(Intent.ACTION_MAIN, null);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ContentValues values = new ContentValues();
				loadFavorites(db, doc,values, intent, TAG_FAVORITE);
				loadFavorites(db, doc, values,intent, TAG_CONTACTS);
				loadFavorites(db, doc, values,intent, TAG_APP);
			}
        }
        /**
         * author hejianfeng
         * 主题中的布局风格
         * @param db
         * @return 
         */
        private void loadFavorites(SQLiteDatabase db, Document doc,ContentValues values,Intent intent,String tagName){
             NodeList mNodeList = doc.getElementsByTagName(tagName);
             for(int i = 0; i < mNodeList.getLength(); i++){
            	 Element element=(Element) mNodeList.item(i);
            	 loadSingleNode(db,element,values,intent,tagName);
             }
        }
        /**
         * author hejianfeng
         * 导入单个节点
         * @param db
         * @return 
         */
		private void loadSingleNode(SQLiteDatabase db, Element element,
				ContentValues values, Intent intent, String tagName) {
			PackageManager packageManager = mContext.getPackageManager();
			try {
				long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
				LauncherLog.v(TAG, "loadSingleNode, jeff container= "+element
						.getAttribute(LauncherSettings.Favorites.CONTAINER));
				container = Long.valueOf(element
						.getAttribute(LauncherSettings.Favorites.CONTAINER));
				values.clear();
				values.put(LauncherSettings.Favorites.CONTAINER, container);
				values.put(LauncherSettings.Favorites.BACKGROUND, element
						.getAttribute(LauncherSettings.Favorites.BACKGROUND));
				values.put(LauncherSettings.Favorites.SCREEN,
						element.getAttribute(LauncherSettings.Favorites.SCREEN));
				values.put(LauncherSettings.Favorites.CELLX,
						element.getAttribute("x"));
				values.put(LauncherSettings.Favorites.CELLY,
						element.getAttribute("y"));
				values.put(LauncherSettings.Favorites.SPANX,
						element.getAttribute(LauncherSettings.Favorites.SPANX));
				values.put(LauncherSettings.Favorites.SPANY,
						element.getAttribute(LauncherSettings.Favorites.SPANY));
				values.put(LauncherSettings.Favorites.MODE,
						element.getAttribute(LauncherSettings.Favorites.MODE));
				if (TAG_FAVORITE.equals(tagName)) {
					LauncherLog.v(TAG, "loadSingleNode, jeff TAG_FAVORITE Favorites.MODE="+element.getAttribute(LauncherSettings.Favorites.MODE)
							+",className="+element.getAttribute("className"));
					addThemeAppShortcut(db, values, element, packageManager,
							intent, container);
				} else if (TAG_CONTACTS.equals(tagName)) {
					LauncherLog.v(TAG, "loadSingleNode, jeff TAG_CONTACTS Favorites.MODE="+element.getAttribute(LauncherSettings.Favorites.MODE)
							+",className="+element.getAttribute("className"));
					addThemeContact(db, values, element, packageManager,
							intent);
				} else if (TAG_APP.equals(tagName)) {
					String packageName = element.getAttribute("packageName");
					String className = element.getAttribute("className");
					LauncherLog.v(TAG, "loadSingleNode, jeff TAG_APP Favorites.MODE="+element.getAttribute(LauncherSettings.Favorites.MODE)
							+",className="+className);
					values.put(LauncherSettings.Favorites.INTENT, packageName
							+ "/" + className);
					values.put(LauncherSettings.Favorites.ALIAS_TITLE,
							element.getAttribute("aliasTitle"));
					values.put(Favorites.ITEM_TYPE,
							Favorites.ITEM_TYPE_APPWIDGET);
					db.insert(TABLE_FAVORITES, null, values);
					db.insert(TABLE_BACKUP, null, values);//hejianfeng add 
				}
				loadBackupList(element,values,tagName);//hejianfeng add
			} catch (Exception e) {
				Log.w(TAG, "Got exception parsing favorites.", e);
			}
        }
        private boolean addThemeContact(SQLiteDatabase db, ContentValues values, Element element,PackageManager packageManager, Intent intent) {

            ActivityInfo info = null;
            String packageName = element.getAttribute("packageName");
            String className = element.getAttribute("className");
            ComponentName cn = null;
            try {
                cn = new ComponentName(packageName, className);
                ComponentName componentNameR = mAppComponentNameReplace.Replace(packageName, className);
                if(componentNameR != null){
                    cn = componentNameR;
                }
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                Log.i("HHJ", "PackageManager.NameNotFoundException:"+nnfe.toString());
            }

            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites.INTENT, intent.toUri(0));
            String aliasTitle = element.getAttribute("aliasTitle");
            if(info != null){
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
            }else{
                values.put(Favorites.TITLE, aliasTitle);
            }
            
            if(aliasTitle != null && !"".equals(aliasTitle)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
            }
            
            String cellDefImage = element.getAttribute("cellDefImage");
            if(cellDefImage != null && !"".equals(cellDefImage)){
                values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,cellDefImage);
            }else{
                values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,"");
            }
            
            String aliasTitleBackground = element.getAttribute("aliasTitleBackground");
            if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
            }
            //hejianfeng add start for add phoneNumber
            values.put(LauncherSettings.Favorites.PHONENUMBER,element.getAttribute("phoneNumber"));
            values.put(LauncherSettings.Favorites.CONTACTNAME,element.getAttribute("contactName"));
            //hejianfeng add end 
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_CONTACTS);
            db.insert(TABLE_FAVORITES, null, values);
            db.insert(TABLE_BACKUP, null, values);//hejianfeng add 
            return true;
        }
        private boolean addThemeAppShortcut(SQLiteDatabase db, ContentValues values, Element element,
                PackageManager packageManager, Intent intent,long container) {

            ActivityInfo info = null;
            String packageName = element.getAttribute("packageName");
            String className = element.getAttribute("className");
            ComponentName cn = null;
            try {
                cn = new ComponentName(packageName, className);

                ComponentName componentNameR = mAppComponentNameReplace.Replace(packageName, className);
                if(componentNameR != null){
                    cn = componentNameR;
                }
                
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                Log.i("HHJ", "PackageManager.NameNotFoundException:"+nnfe.toString());
            }

            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites.INTENT, intent.toUri(0));
            String aliasTitle = element.getAttribute("aliasTitle");
            if(info != null){
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
            }else{
                values.put(Favorites.TITLE, aliasTitle);//getAliasTitle(aliasTitle)
            }
            
            if(aliasTitle != null && !"".equals(aliasTitle)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
            }
         
            String aliasTitleBackground = element.getAttribute("aliasTitleBackground");
            if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
            }
            
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            db.insert(TABLE_FAVORITES, null, values);
            db.insert(TABLE_BACKUP, null, values);//hejianfeng add 
            return true;
        }
        //hejianfeng add end
        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         */
        private int loadFavorites(SQLiteDatabase db) {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            PackageManager packageManager = mContext.getPackageManager();
            int i = 0;
            try {
                XmlResourceParser parser = null;
                
                    parser = mContext.getResources().getXml(R.xml.default_workspace);
                
                AttributeSet attrs = Xml.asAttributeSet(parser);
                XmlUtils.beginDocument(parser, TAG_FAVORITES);

                final int depth = parser.getDepth();
                ContentValues values = new ContentValues();
                int type;
                while (((type = parser.next()) != XmlPullParser.END_TAG ||
                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }

                    boolean added = false;
                    final String name = parser.getName();

                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
                    long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                    if (a.hasValue(R.styleable.Favorite_container)) {
                        container = Long.valueOf(a.getString(R.styleable.Favorite_container));
                    }
                    values.clear();
                    values.put(LauncherSettings.Favorites.CONTAINER, container); 
                    values.put(LauncherSettings.Favorites.BACKGROUND, a.getString(R.styleable.Favorite_background)); 
                    values.put(LauncherSettings.Favorites.SCREEN, a.getString(R.styleable.Favorite_screen));
                    values.put(LauncherSettings.Favorites.CELLX, a.getString(R.styleable.Favorite_x));
                    values.put(LauncherSettings.Favorites.CELLY, a.getString(R.styleable.Favorite_y));
                    values.put(LauncherSettings.Favorites.SPANX, a.getString(R.styleable.Favorite_spanX));
                    values.put(LauncherSettings.Favorites.SPANY, a.getString(R.styleable.Favorite_spanY));
                    values.put(LauncherSettings.Favorites.MODE, a.getString(R.styleable.Favorite_modeSelect));
                    if (TAG_FAVORITE.equals(name)) {
                        added = addAppShortcut(db, values, a, packageManager, intent,container);
                    }else if(TAG_CONTACTS.equals(name)){
                        added = addContact(db, values, a, packageManager,intent);
                    }else if(TAG_APP.equals(name)){
                        String packageName = a.getString(R.styleable.Favorite_packageName);
                        String className = a.getString(R.styleable.Favorite_className);
                        values.put(LauncherSettings.Favorites.INTENT, packageName+"/"+className);
                        values.put(LauncherSettings.Favorites.ALIAS_TITLE, a.getString(R.styleable.Favorite_aliasTitle));
                        values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
                        db.insert(TABLE_FAVORITES, null, values);
                        db.insert(TABLE_BACKUP, null, values);//hejianfeng add 
                    }
                    loadDefaultBackupList(a,values,name);//hejianfeng add
                    if (added) i++;
                    a.recycle();
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            } catch (IOException e) {
                Log.w(TAG, "Got exception parsing favorites.", e);
            }

            return i;
        }
        
        private boolean addContact(SQLiteDatabase db, ContentValues values, TypedArray a,PackageManager packageManager, Intent intent) {

            ActivityInfo info = null;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            ComponentName cn = null;
            try {
                cn = new ComponentName(packageName, className);
                ComponentName componentNameR = mAppComponentNameReplace.Replace(packageName, className);
                if(componentNameR != null){
                    cn = componentNameR;
                }
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                Log.i("HHJ", "PackageManager.NameNotFoundException:"+nnfe.toString());
            }

            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites.INTENT, intent.toUri(0));
            String aliasTitle = a.getString(R.styleable.Favorite_aliasTitle);
            if(info != null){
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
            }else{
                values.put(Favorites.TITLE, aliasTitle/*getAliasTitle(aliasTitle)*/);
            }
            
            if(aliasTitle != null && !"".equals(aliasTitle)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle/*getAliasTitle(aliasTitle)*/);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
            }
            
            String cellDefImage = a.getString(R.styleable.Favorite_cellDefImage);
            if(cellDefImage != null && !"".equals(cellDefImage)){
                values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,cellDefImage);
            }else{
                values.put(LauncherSettings.Favorites.CELL_DEF_IMAGE,"");
            }
            
            String aliasTitleBackground = a.getString(R.styleable.Favorite_aliasTitleBackground);
            if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
            }
            //hejianfeng add start for add phoneNumber
            values.put(LauncherSettings.Favorites.PHONENUMBER,a.getString(R.styleable.Favorite_phoneNumber));
            values.put(LauncherSettings.Favorites.CONTACTNAME,a.getString(R.styleable.Favorite_contactName));
            //hejianfeng add end 
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_CONTACTS);
            db.insert(TABLE_FAVORITES, null, values);
            db.insert(TABLE_BACKUP, null, values);//hejianfeng add 
            return true;
        }
        

        private boolean addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent,long container) {

            ActivityInfo info = null;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);
            ComponentName cn = null;
            try {
                cn = new ComponentName(packageName, className);

                ComponentName componentNameR = mAppComponentNameReplace.Replace(packageName, className);
                if(componentNameR != null){
                    cn = componentNameR;
                }
                
                info = packageManager.getActivityInfo(cn, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                Log.i("HHJ", "PackageManager.NameNotFoundException:"+nnfe.toString());
            }

            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites.INTENT, intent.toUri(0));
            String aliasTitle = a.getString(R.styleable.Favorite_aliasTitle);
            if(info != null){
                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
            }else{
                values.put(Favorites.TITLE, aliasTitle);//getAliasTitle(aliasTitle)
            }
            
            if(aliasTitle != null && !"".equals(aliasTitle)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,aliasTitle);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE,"");
            }
         
            String aliasTitleBackground = a.getString(R.styleable.Favorite_aliasTitleBackground);
            if(aliasTitleBackground != null && !"".equals(aliasTitleBackground)){
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,aliasTitleBackground);
            }else{
                values.put(LauncherSettings.Favorites.ALIAS_TITLE_BACKGROUND,"");
            }
            
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
            db.insert(TABLE_FAVORITES, null, values);
            db.insert(TABLE_BACKUP, null, values);//hejianfeng add 
            return true;
        }
    }
    
    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);                
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}