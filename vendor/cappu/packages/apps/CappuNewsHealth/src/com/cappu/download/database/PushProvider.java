
package com.cappu.download.database;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;

import com.cappu.download.PushReceiver;
import com.cappu.download.PushTypeSharedPreferences;
import com.cappu.download.push.PushService;
import com.cappu.download.utils.PushConstants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.RealSystemFacade;
import android.os.SystemFacade;
import android.text.TextUtils;
import android.util.Log;

public final class PushProvider extends ContentProvider {
    private static String TAG = "pushProvider";
    
    private static final String DATABASE_NAME = "downloadInfo.db";
    public static final String AUTHORITY = "com.cappu.download";
    private static int DATABASE_VERSION = 10;
    private SQLiteOpenHelper mOpenHelper;
    PushTypeSharedPreferences mPushTypeSharedPreferences;
    SystemFacade mSystemFacade;
    
    /** URI matcher used to recognize URIs sent by applications */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    //SimpleDateFormat sDateFormat = new SimpleDateFormat("yy/MM/dd");
    
    private static final int REQUEST_HEADERS_URI = 0x10;
    private static final int PUSH_TYPE_URI = 0x20;
    private static final int PUSH_TYPE_URI_ID = 0x40;
    private static final int PUSH_TYPE_TEXT_URI = 0x30;
    private static final int PUSH_TYPE_APK_URI = 0x50;
    static {
        sURIMatcher.addURI(AUTHORITY, PushSettings.PushAPKInfo.DOWNLOAD_APK_TABLE, PUSH_TYPE_APK_URI);
        sURIMatcher.addURI(AUTHORITY, PushSettings.PushTextInfo.DOWNLOAD_TEXT_TABLE, PUSH_TYPE_TEXT_URI);
        sURIMatcher.addURI(AUTHORITY, PushSettings.PushType.DOWNLOAD_TYPE_TABLE, PUSH_TYPE_URI);
        sURIMatcher.addURI(AUTHORITY, PushSettings.PushType.DOWNLOAD_TYPE_TABLE+"/#", PUSH_TYPE_URI_ID);
        sURIMatcher.addURI(AUTHORITY, PushSettings.PushType.DOWNLOAD_TYPE_TABLE+"/#/" + PushSettings.RequestHeaders.URI_SEGMENT, REQUEST_HEADERS_URI);
    }

    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            try {
                /**下载apk的表*/
                db.execSQL("DROP TABLE IF EXISTS " + PushSettings.PushAPKInfo.DOWNLOAD_APK_TABLE);
                db.execSQL("CREATE TABLE " + PushSettings.PushAPKInfo.DOWNLOAD_APK_TABLE + "( _id" + " INTEGER PRIMARY KEY AUTOINCREMENT," + PushSettings.PushAPKInfo.APP_ID + " TEXT,"
                        + PushSettings.PushAPKInfo.DOWNLOAD_CHANNEL+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_NAME+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_PKG+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_SIZE+" TEXT,"
                        + PushSettings.PushAPKInfo.COLUMN_URI+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_PN+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_PI+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_RN+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_PS+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_INTERNAL+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_STATUS+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_ICON_STATUS+" TEXT,"
                        + PushSettings.PushAPKInfo.APP_ICON + " TEXT); ");
                
                /**下载表，可以 推送设置，推送列表，激活等*/
                db.execSQL("DROP TABLE IF EXISTS " + PushSettings.PushType.DOWNLOAD_TYPE_TABLE);
                db.execSQL("CREATE TABLE " + PushSettings.PushType.DOWNLOAD_TYPE_TABLE + "( _id" + " INTEGER PRIMARY KEY AUTOINCREMENT," + PushSettings.PushAPKInfo.APP_ID + " TEXT,"
                        + PushSettings.PushType.DOWNLOAD_CHANNEL+" TEXT,"
                        + PushSettings.PushType.COLUMN_ATTACHED_ID+" TEXT,"
                        + PushSettings.PushType.COLUMN_URI+" TEXT,"
                        + PushSettings.PushType.PUSH_PROTOCOL+" TEXT,"
                        + PushSettings.PushType.COLUMN_STATUS+" TEXT,"
                        + PushSettings.PushType.PUSH_TYPE_NAME+" TEXT,"
                        + PushSettings.BasePushColumns._DATA +" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_FILE_NAME_HINT +" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_MIME_TYPE+" TEXT,"
                        + PushSettings.BasePushColumns.FAILED_CONNECTIONS+" TEXT,"
                        + PushSettings.BasePushColumns.RETRY_AFTER_X_REDIRECT_COUNT+" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_TOTAL_BYTES+" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_CURRENT_BYTES+" TEXT,"
                        + PushSettings.ETAG+" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_DELETED+" TEXT,"
                        + PushSettings.COLUMN_ALLOWED_NETWORK_TYPES+" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_TITLE+" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_DESCRIPTION+" TEXT,"
                        + PushSettings.BasePushColumns.COLUMN_CONTROL+" TEXT,"
                        + PushSettings.COLUMN_LAST_MODIFICATION +" TEXT,"
                        + PushSettings.PushType.PUSH_TYPE + " TEXT); ");
                
                /**这个是下载的时候 http请求 头属性 的表*/
                db.execSQL("DROP TABLE IF EXISTS " + PushSettings.RequestHeaders.HEADERS_DB_TABLE);
                db.execSQL("CREATE TABLE " + PushSettings.RequestHeaders.HEADERS_DB_TABLE + "(" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + PushSettings.RequestHeaders.COLUMN_DOWNLOAD_ID + " INTEGER NOT NULL," + PushSettings.RequestHeaders.COLUMN_HEADER + " TEXT NOT NULL,"
                        + PushSettings.RequestHeaders.COLUMN_VALUE + " TEXT NOT NULL" + ");");
                
                

                db.execSQL("DROP TABLE IF EXISTS " + PushSettings.PushTextInfo.DOWNLOAD_TEXT_TABLE);
                db.execSQL("CREATE TABLE " + PushSettings.PushTextInfo.DOWNLOAD_TEXT_TABLE + "( _id" + " INTEGER PRIMARY KEY AUTOINCREMENT," 
                        + PushSettings.PushTextInfo.TEXT_ID + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_FLAG + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_ICON + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_ICON_PATH + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_ICON_STATUS + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_BANNER + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_BANNER_PATH + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_BANNER_STATUS + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_TYPE + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_TITLE + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_INTRODUCE + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_PACKAGENAME + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_URL + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_SIZE + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_STATUS + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_DATE + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_FAVORITES + " TEXT,"
                        + PushSettings.PushTextInfo.TEXT_SITE + " TEXT); ");
                
            } catch (SQLException ex) {
                throw ex;
            }
        }
        

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int version = oldVersion;
            if (version != DATABASE_VERSION) {
                onCreate(db);
            }
        }
    }

    @Override
    public boolean onCreate() {
        if (mSystemFacade == null) {
            mSystemFacade = new RealSystemFacade(getContext());
        }
        
        if(mPushTypeSharedPreferences == null){
            mPushTypeSharedPreferences = new PushTypeSharedPreferences(getContext());
        }
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        Log.i(TAG, " insert  uri:"+uri);
        SqlArguments args = new SqlArguments(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        Context context = getContext();
        long rowID = 0;
        int match = sURIMatcher.match(uri);
        if (match == PUSH_TYPE_URI) {
            Cursor cursor = null;
            boolean isExist;
            try {
                String[] projection = new String[]{PushSettings.BasePushColumns._ID,PushSettings.BasePushColumns.COLUMN_STATUS,PushSettings.BasePushColumns.COLUMN_TITLE};
                cursor = queryDataExist(db,args.table,projection,PushSettings.PushType.PUSH_TYPE, values.getAsString(PushSettings.PushType.PUSH_TYPE));
                isExist = cursor.getCount() != 0 ;
                int typeValues = Integer.parseInt(values.getAsString(PushSettings.PushType.PUSH_TYPE));
                Log.i("pushProvider"," 数据在表:"+args.table+"存在   isExist: "+isExist+"    "+values.getAsString(PushSettings.PushType.PUSH_TYPE_NAME)+"    typeValues:"+typeValues);
                if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES && isExist){//激活
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long upID = cursor.getLong(0);
                        ContentValues upCV = new ContentValues(); 
                        upCV.put(PushSettings.PushType.COLUMN_URI, values.getAsString(PushSettings.PushType.COLUMN_URI));
                        
                        upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                        upCV.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
                        Intent intent = new Intent(context, PushService.class);
                        intent.setData(uri);
                        insertRequestHeaders(db, upID, values);
                        db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                        
                        Log.v(TAG, "再次去激活");
                        ComponentName cn = context.startService(intent);
                        
                        /*int status = cursor.getInt(1);
                        if(status != PushSettings.STATUS_SUCCESS){
                            context = getContext();
                            Intent intent = new Intent(context, PushService.class);
                            intent.setData(uri);
                            ComponentName cn = context.startService(intent);
                            Log.i("pushProvider"," 激活更新 防止未激活成功 status:"+status+"   uri:"+uri);
                        }else{
                            String activationValues = cursor.getString(2);
                            if(activationValues != null && activationValues.indexOf("1")!=-1){
                                Log.v(TAG, "激活");
                                mPushTypeSharedPreferences.setBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, true);
                            }else{
                                Log.v(TAG, "激活失败");
                                mPushTypeSharedPreferences.setBooleanToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_KEY, false);                            
                            }
                            mSystemFacade.scheduleAlarm(1000, context,PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES);//激活以后执行每天的设置
                        }*/
                        return uri;
                    }
                }else if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES && isExist){//设置
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long upID = cursor.getLong(0);
                        Log.i("pushProvider","设置 更新 upID:"+upID);
                        ContentValues upCV = new ContentValues(); 
                        
                        upCV.put(PushSettings.PushType.COLUMN_URI, values.getAsString(PushSettings.PushType.COLUMN_URI));
                        
                        upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                        upCV.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
                        
                        int status = cursor.getInt(1);
                        //if(status != PushSettings.STATUS_SUCCESS){
                            Intent intent = new Intent(context, PushService.class);
                            intent.setData(uri);
                            insertRequestHeaders(db, upID, values);
                            db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                            ComponentName cn = context.startService(intent);
                            Log.i("pushProvider"," 推送设置 未更新成功 再次更新status:"+status+"   uri:"+uri);
                        //}
                        
                        //Log.i("pushProvider"," 推送设置 未更新成功 再次更新status:"+status+"   uri:"+uri);
                        return uri;
                    }
                }else if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES && isExist){/**推送的列表*/
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long upID = cursor.getLong(0);
                        Log.i("pushProvider","推送 列表 更新 upID:"+upID);
                        ContentValues upCV = new ContentValues(); 
                        upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                        upCV.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
                        Intent intent = new Intent(context, PushService.class);
                        intent.setData(uri);
                        
                        upCV.put(PushSettings.PushType.COLUMN_URI, values.getAsString(PushSettings.PushType.COLUMN_URI));
                        
                        insertRequestHeaders(db, upID, values);
                        db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                        ComponentName cn = context.startService(intent);
                        return uri;
                    }
                }else if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES && isExist){
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long upID = cursor.getLong(0);
                        Log.i("pushProvider","单条推送详情 upID:"+upID+"    表:"+args.table+"    URI:"+values.getAsString(PushSettings.PushType.COLUMN_URI));
                        ContentValues upCV = new ContentValues(); 
                        upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                        upCV.put(PushSettings.BasePushColumns.COLUMN_URI, values.getAsString(PushSettings.PushType.COLUMN_URI));
                        upCV.put(PushSettings.PushType.PUSH_PROTOCOL, values.getAsString(PushSettings.PushType.PUSH_PROTOCOL));
                        upCV.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
                        upCV.put(PushSettings.BasePushColumns.COLUMN_ATTACHED_ID, values.getAsString(PushSettings.PushType.COLUMN_ATTACHED_ID));
                        Intent intent = new Intent(context, PushService.class);
                        intent.setData(uri);
                        insertRequestHeaders(db, upID, values);
                        db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                        ComponentName cn = context.startService(intent);
                        return uri;
                    }
                }else if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES && isExist){
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long upID = cursor.getLong(0);
                        Log.i("pushProvider","这个是获取的 icon图片 upID:"+upID+"    表:"+args.table+"    URI:"+values.getAsString(PushSettings.PushType.COLUMN_URI));
                        ContentValues upCV = new ContentValues(); 
                        upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                        upCV.put(PushSettings.BasePushColumns.COLUMN_URI, values.getAsString(PushSettings.PushType.COLUMN_URI));
                        upCV.put(PushSettings.PushType.PUSH_PROTOCOL, values.getAsString(PushSettings.PushType.PUSH_PROTOCOL));
                        upCV.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
                        upCV.put(PushSettings.BasePushColumns.COLUMN_ATTACHED_ID, values.getAsString(PushSettings.PushType.COLUMN_ATTACHED_ID));
                        upCV.put(PushSettings.PushType.COLUMN_FILE_NAME_HINT, values.getAsString(PushSettings.PushType.COLUMN_FILE_NAME_HINT));
                        upCV.put(PushSettings.PushType._DATA, "");
                        Intent intent = new Intent(context, PushService.class);
                        intent.setData(uri);
                        insertRequestHeaders(db, upID, values);
                        db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                        ComponentName cn = context.startService(intent);
                        return uri;
                    }
                }else if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES && isExist){
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long upID = cursor.getLong(0);
                        Log.i("pushProvider","这个是获取的 icon图片 upID:"+upID+"    表:"+args.table+"    URI:"+values.getAsString(PushSettings.PushType.COLUMN_URI));
                        ContentValues upCV = new ContentValues(); 
                        upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                        upCV.put(PushSettings.BasePushColumns.COLUMN_URI, values.getAsString(PushSettings.PushType.COLUMN_URI));
                        upCV.put(PushSettings.PushType.PUSH_PROTOCOL, values.getAsString(PushSettings.PushType.PUSH_PROTOCOL));
                        upCV.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
                        upCV.put(PushSettings.BasePushColumns.COLUMN_ATTACHED_ID, values.getAsString(PushSettings.PushType.COLUMN_ATTACHED_ID));
                        upCV.put(PushSettings.PushType.COLUMN_FILE_NAME_HINT, values.getAsString(PushSettings.PushType.COLUMN_FILE_NAME_HINT));
                        upCV.put(PushSettings.PushType._DATA, "");
                        Intent intent = new Intent(context, PushService.class);
                        intent.setData(uri);
                        insertRequestHeaders(db, upID, values);
                        db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                        ComponentName cn = context.startService(intent);
                        return uri;
                    }
                }
            } catch (Exception e) {
                Log.e("pushProvider","Exception:" + e.toString());
            }finally{
                if(cursor != null){
                    cursor.close();
                }
            }
        }else if(match == PUSH_TYPE_TEXT_URI){
            Cursor cursor = null;
            boolean isExist;
            try {
                String[] projection = new String[]{PushSettings.BasePushColumns._ID};
                cursor = queryDataExist(db,args.table,projection,PushSettings.PushTextInfo.TEXT_ID, values.getAsString(PushSettings.PushTextInfo.TEXT_ID));
                isExist = cursor.getCount() != 0 ;
                Log.e("pushProvider","防止 更新列表 时候 重复的数据提交到数据库     查询单条信息 cursor:" + cursor.getCount()+"     isExist:"+isExist);
                if(isExist){//防止 更新列表 时候 重复的数据提交到数据库
                    return uri;
                }else{
                    rowID = db.insert(args.table, null, values);
                }
            }catch(Exception e){
                
            }
            
            return uri = ContentUris.withAppendedId(uri, rowID);
            
            /*else if(typeValues == PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES && isExist){*//**推送的列表*//*
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    long upID = cursor.getLong(0);
                    Log.i("pushProvider"," 单条 推送 更新 upID:"+upID);
                    ContentValues upCV = new ContentValues(); 
                    upCV.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
                    upCV.put(PushSettings.BasePushColumns.COLUMN_URI, values.getAsString(PushSettings.BasePushColumns.COLUMN_URI));
                    
                    Context context = getContext();
                    Intent intent = new Intent(context, PushService.class);
                    intent.setData(uri);
                    db.update(args.table, upCV, PushSettings.BasePushColumns._ID + " = " +upID, null);
                    ComponentName cn = context.startService(intent);
                    return uri;
                }
            }*/
        }else if(match == PUSH_TYPE_APK_URI){
            rowID = db.insert(args.table, null, values);
            uri = ContentUris.withAppendedId(uri, rowID);
            Log.i(TAG, " insert apk list uri:"+uri);
            return uri;
        }
        
        
        ContentValues filteredValues = new ContentValues();
        copyString(PushSettings.BasePushColumns.COLUMN_URI, values, filteredValues);//下载地址
        copyString(PushSettings.BasePushColumns.DOWNLOAD_CHANNEL, values, filteredValues);//下载通道号
        copyString(PushSettings.PushType.PUSH_PROTOCOL, values, filteredValues);//下载 协议
        copyString(PushSettings.BasePushColumns.COLUMN_STATUS, values, filteredValues);//下载的状态 （暂停，开始，正在下载 等。。。。）
        copyString(PushSettings.PushType.PUSH_TYPE_NAME, values, filteredValues);//下push的类型中文名 这个与 PushSettings.PushType.PUSH_TYPE 里面的值对应
        copyString(PushSettings.BasePushColumns.COLUMN_MIME_TYPE, values, filteredValues);//下载的类型/是text/还是文件等。。。。
        copyString(PushSettings.BasePushColumns.FAILED_CONNECTIONS, values, filteredValues);//下载失败以后重试的次数
        copyString(PushSettings.BasePushColumns.COLUMN_TOTAL_BYTES, values, filteredValues);//下载总数据大小
        copyString(PushSettings.BasePushColumns.COLUMN_CURRENT_BYTES, values, filteredValues);//当前已下载数据
        copyString(PushSettings.ETAG, values, filteredValues);//下载标志位
        copyString(PushSettings.BasePushColumns.COLUMN_DELETED, values, filteredValues);//已下载的数据是否被删除
        
        copyString(PushSettings.COLUMN_ALLOWED_NETWORK_TYPES, values, filteredValues);//下载数据时所使用的数据类型
        copyString(PushSettings.BasePushColumns.COLUMN_TITLE, values, filteredValues);//显示下载的名字
        copyString(PushSettings.BasePushColumns.COLUMN_DESCRIPTION, values, filteredValues);//下载说明
        copyString(PushSettings.BasePushColumns.COLUMN_CONTROL, values, filteredValues);//暂停或在恢复下载的一个标志位
        copyString(PushSettings.PushType.PUSH_TYPE, values, filteredValues);//下载的数据类型
        copyString(PushSettings.PushType.COLUMN_ATTACHED_ID, values, filteredValues);//下载的数据附带一个其他表需要的id字段
        
        copyString(PushSettings.PushType.COLUMN_FILE_NAME_HINT, values, filteredValues);//下载的数据附带一个其他表需要的id字段
        
        filteredValues.put(PushSettings.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
        filteredValues.put(PushSettings.BasePushColumns.COLUMN_STATUS, PushSettings.STATUS_PENDING);
        
        
        rowID = db.insert(args.table, null, filteredValues);
        
        //Log.i("pushProvider", " insert  rowID:"+rowID);
        if (rowID <= 0) return null;
        
        Intent intent = new Intent(context, PushService.class);
        intent.setData(uri);
        ComponentName cn = context.startService(intent);
        
        uri = ContentUris.withAppendedId(uri, rowID);
        getContext().getContentResolver().notifyChange(uri, null);
        
        insertRequestHeaders(db, rowID, values);
        //Log.i("pushProvider", "insert end :"+cn.toString());
        return uri;
    }
    
    private static final void copyBoolean(String key, ContentValues from, ContentValues to) {
        Boolean b = from.getAsBoolean(key);
        if (b != null) {
            to.put(key, b);
        }
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

    private static final void copyStringWithDefault(String key, ContentValues from, ContentValues to, String defaultValue) {
        copyString(key, from, to);
        if (!to.containsKey(key)) {
            to.put(key, defaultValue);
        }
    }
    
    /**
     * Insert request headers for a download into the DB.
     */
    private void insertRequestHeaders(SQLiteDatabase db, long downloadId, ContentValues values) {
        String[] projection = new String[]{"id"};
        Cursor cursor = queryDataExist(db,PushSettings.RequestHeaders.HEADERS_DB_TABLE,projection,PushSettings.RequestHeaders.COLUMN_DOWNLOAD_ID, String.valueOf(downloadId));
        boolean isExist = false;
        if(cursor != null && cursor.getCount() != 0){
            isExist = true;
        }
        if(cursor != null){
            cursor.close();
        }
        
        ContentValues rowValues = new ContentValues();
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            rowValues.clear();
            String key = entry.getKey();
            if (key.startsWith(PushSettings.RequestHeaders.INSERT_KEY_PREFIX)) {
                String headerLine = entry.getValue().toString();
                if (!headerLine.contains(":")) {
                    throw new IllegalArgumentException("Invalid HTTP header line: " + headerLine);
                }
                String[] parts = headerLine.split(":", 2);
                rowValues.put(PushSettings.RequestHeaders.COLUMN_HEADER, parts[0].trim());
                rowValues.put(PushSettings.RequestHeaders.COLUMN_VALUE, parts[1].trim());
                Log.i("pushProvider", "downloadId:"+downloadId+"    header:"+parts[0].trim()+"   values:"+parts[1].trim()+"    isExist:"+isExist);
                if(isExist){
                    db.update(PushSettings.RequestHeaders.HEADERS_DB_TABLE, rowValues,PushSettings.RequestHeaders.COLUMN_HEADER +" =?  and "+PushSettings.RequestHeaders.COLUMN_DOWNLOAD_ID +" =? " ,new String[]{parts[0].trim(),String.valueOf(downloadId)});//(PushSettings.RequestHeaders.HEADERS_DB_TABLE, null, rowValues);
                }else{
                    rowValues.put(PushSettings.RequestHeaders.COLUMN_DOWNLOAD_ID, downloadId);
                    db.insert(PushSettings.RequestHeaders.HEADERS_DB_TABLE, null, rowValues);
                }
            }
        }
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        Context context = getContext();
        
        //Log.i(TAG, " update  uri:"+uri);
        
        
        long rowID = 0;
        int match = sURIMatcher.match(uri);
        if(match == PUSH_TYPE_URI_ID){//说明是更新了类型表里面的数据
            if(mPushTypeSharedPreferences == null){
                mPushTypeSharedPreferences = new PushTypeSharedPreferences(context);
            }
            int status = 0;
            if(values.containsKey(PushSettings.BasePushColumns.COLUMN_STATUS)){
                status = values.getAsInteger(PushSettings.BasePushColumns.COLUMN_STATUS);
            }
            long defTime = 1000;// 1000 毫秒 既 一秒
            if (status == PushSettings.STATUS_SUCCESS) {
                int type = values.getAsInteger(PushSettings.PushType.PUSH_TYPE);
                switch (type) {
                case PushTypeSharedPreferences.PUSH_TYPE_ACTIVATION_VALUES:// 激活类型
                    mSystemFacade.scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES);//激活以后执行每天的设置
                    break;
                case PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_VALUES:// 设置的类型
                    defTime = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_FIRST_INTERVAL, 30 * defTime)*1000;//设置 后半个小时开始获取列表
                    mSystemFacade.scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES);//获取到设置以后就开始每天的列表
                    break;
                case PushTypeSharedPreferences.PUSH_TYPE_LIST_VALUES:// 列表 类型
                    defTime = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, 3 * defTime)*1000;//列表获取以后3分钟推送每一条内容
                    mSystemFacade.scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
                    break;
                case PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES:// 详情 类型  详情列表推送完成以后立马拉取 图片资源
                    mSystemFacade.scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES);//去获取图片资源
                    
                    //defTime = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, 3 * defTime)*1000;//定时去获取下一条详情
                    //scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
                    break;
                case PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES:// 详情 icon 类型
                case PushTypeSharedPreferences.PUSH_TYPE_DETAIL_BANNER_VALUES:// 详情 banner 类型    图片资源拉取完以后 在执行下一条详情获取
                    //scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_ICON_VALUES);
                    
                    defTime = mPushTypeSharedPreferences.getLongToSettingDate(PushTypeSharedPreferences.PUSH_TYPE_SETTINGS_DETAIL_TIME_INTERVAL, 3 * defTime)*1000;//定时去获取下一条详情
                    mSystemFacade.scheduleAlarm(defTime, context,PushTypeSharedPreferences.PUSH_TYPE_DETAIL_VALUES);
                    break;
                default:
                    break;
                }
            }
        }
        
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        /*for (Entry<String, Object> item : values.valueSet()) {
            String str =  "更新数据列 "+item.getKey();
            if(item.getValue() != null){
                str = str + "    值是:"+item.getValue().toString()+"     表名:"+args.table+"    args.where:"+args.where;
            }else{
                str = str + "    值是null"+"     表名:"+args.table+"    args.where:"+args.where;
            }
            Log.i(TAG, str);
        }*/
        
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }
    
    private void sendNotify(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
    
    @Override
    public int delete(final Uri uri, final String where, final String[] whereArgs) {
        SqlArguments args = new SqlArguments(uri, where, whereArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) sendNotify(uri);
        return count;
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

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        //Log.i(TAG, " query  selection:"+selection);
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
        int match = sURIMatcher.match(uri);
        //Log.e("pushProvider", " query  match:"+match);
        if (match == REQUEST_HEADERS_URI) {
            if (projection != null || selection != null || sortOrder != null) {
                throw new UnsupportedOperationException("Request header queries do not support " + "projections, selections or sorting");
            }
            return queryRequestHeaders(db, uri);
        }
        
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }
    
    /**
     * Handle a query for the custom request headers registered for a download.
     */
    private Cursor queryRequestHeaders(SQLiteDatabase db, Uri uri) {
        String where = PushSettings.RequestHeaders.COLUMN_DOWNLOAD_ID + "=" + getDownloadIdFromUri(uri);
        String[] projection = new String[] { PushSettings.RequestHeaders.COLUMN_HEADER, PushSettings.RequestHeaders.COLUMN_VALUE };
        Cursor cursor = db.query(PushSettings.RequestHeaders.HEADERS_DB_TABLE, projection, where, null, null, null, null);
        return new ReadOnlyCursorWrapper(cursor);
    }
    private String getDownloadIdFromUri(final Uri uri) {
        return uri.getPathSegments().get(1);
    }
    
    /**
     * 查询某个uri是否存在记录
     * 第2个参数 是 表名
     * 第3个参数 是 要查询的列
     * 第4个参数 关键列
     * 第5个参数 关键列 的值
     * */
    private Cursor queryDataExist(SQLiteDatabase db,String table,String[] projection,String key, String values){
        Cursor cursor = null;
        try {
            String where = key + "=" + values;//getDataNameFromUri(uri);
            //String[] projection;// = new String[] { PushSettings.RequestHeaders.COLUMN_HEADER, Downloads.RequestHeaders.COLUMN_VALUE };
            cursor = db.query(table, projection, where, null, null, null, null);
            //cursor = db.query(table, null, null, null, null, null, null);
            //Log.i("pushProvider"," table:"+table+"    projection:"+projection+"   where: "+where+"    key:"+key+"     values:"+values+"   count:"+cursor.getCount());
            return cursor;
        } catch (Exception e) {
            Log.e("pushProvider", "query data exist exception :"+e.toString());
            return cursor;
        }
    }
    
    private String getDataNameFromUri(final Uri uri){
        return uri.getPathSegments().get(1);
    }
    
    private class ReadOnlyCursorWrapper extends CursorWrapper implements CrossProcessCursor {
        public ReadOnlyCursorWrapper(Cursor cursor) {
            super(cursor);
            mCursor = (CrossProcessCursor) cursor;
        }

        @SuppressWarnings("unused")
        public boolean deleteRow() {
            throw new SecurityException("Download manager cursors are read-only");
        }

        @SuppressWarnings("unused")
        public boolean commitUpdates() {
            throw new SecurityException("Download manager cursors are read-only");
        }

        public void fillWindow(int pos, CursorWindow window) {
            mCursor.fillWindow(pos, window);
        }

        public CursorWindow getWindow() {
            return mCursor.getWindow();
        }

        public boolean onMove(int oldPosition, int newPosition) {
            return mCursor.onMove(oldPosition, newPosition);
        }

        private CrossProcessCursor mCursor;
    }
}
