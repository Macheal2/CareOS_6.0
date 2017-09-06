package com.cappu.music.database;

import java.util.HashMap;
import java.util.List;

import com.cappu.music.MusicApplication;
import com.cappu.music.MusicApplication.QueryCompleteListener;
import com.cappu.music.PlayFragment;
import com.cappu.music.R;
import com.cappu.music.uitl.DocumentsContract;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;
import android.util.Log;

public class MusicProvider extends ContentProvider {
    private static final String TAG = "HHJ";
    private static final boolean LOGD = false;
    public static final String DATABASE_NAME = "music.db";
    
    private final static int IDCOLIDX = 0;

    private static int DATABASE_VERSION = -1;

    static final String AUTHORITY = "com.cappu.music";

    /** 歌曲清单列表表名 */
    public static final String INVENTORY_TABLE_NAME = "folder";
    /** 清单歌曲表名 */
    public static final String SONG_TABLE_NAME = "song";

    private static final String CREATE_INVENTORY_TABLE_SQL = " create table " + INVENTORY_TABLE_NAME
            + "(_id integer primary key autoincrement,songInventoryName text,icon INTEGER,type INTEGER) ";
    private static final String CREATE_SONG_TABLE_SQL = " create table " + SONG_TABLE_NAME
            + "(_id integer primary key autoincrement, songId INTEGER,songInventoryId INTEGER,title TEXT) ";

    private SQLiteOpenHelper mOpenHelper;
    
    public static interface BaseMusicColumns extends BaseColumns {
        static final String SONG_INVENTORY_NAME = "songInventoryName";
        static final String SONG_INVENTORY_ICON = "icon";
        static final String SONG_INVENTORY_TYPE = "type";
        static final String SONG_ID = "songId";
        static final String SONG_INVENTORY_ID = "songInventoryId";
        static final String SONG_TITLE="title";
        
        public static final Uri INVENTORY_URI = Uri.parse("content://" +MusicProvider.AUTHORITY + "/" + MusicProvider.INVENTORY_TABLE_NAME);
        public static final Uri SONG_URI = Uri.parse("content://" +MusicProvider.AUTHORITY + "/" + MusicProvider.SONG_TABLE_NAME);
        
    }
    public static final class BaseMusicUri implements BaseMusicColumns{

        public static Uri getInventoryUri(long id) {
            return Uri.parse("content://" + MusicProvider.AUTHORITY + "/" + MusicProvider.INVENTORY_TABLE_NAME + "/" + id + "?");
        }
        
        public static Uri getSongUri(long id){
            return Uri.parse("content://" + MusicProvider.AUTHORITY + "/" + MusicProvider.SONG_TABLE_NAME + "/" + id + "?");
        }
        
    }

    @Override
    public boolean onCreate() {
        if (DATABASE_VERSION == -1) {
            DATABASE_VERSION = 11;
            Log.i("HHJ", "数据库版本  " + DATABASE_VERSION);
        }
        mOpenHelper = new DatabaseHelper(getContext());
        
        
        return true;
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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

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
        if (rowId <= 0)
            return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        //sendNotify(uri);

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
                if (db.insert(args.table, null, values[i]) < 0)
                    return 0;
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
        if (count > 0)
            sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0)
            sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(INVENTORY_TABLE_NAME);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper implements QueryCompleteListener {

        private final Context mContext;
        MusicApplication mMusicApplication;

        private SQLiteDatabase mDefaultWritableDatabase = null;

        static final int APPWIDGET_HOST_ID = 1024;
        
        
        private HashMap<String,Long> mMapIdKey;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            
            mMapIdKey = new HashMap<String, Long>();
            mMusicApplication = (MusicApplication) mContext.getApplicationContext();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD)
                Log.d(TAG, "onUpgrade triggered");

            int version = oldVersion;
            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + CREATE_INVENTORY_TABLE_SQL);
                db.execSQL("DROP TABLE IF EXISTS " + CREATE_SONG_TABLE_SQL);
                onCreate(db);
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_INVENTORY_TABLE_SQL);
            db.execSQL(CREATE_SONG_TABLE_SQL);
            initData(db);
            classification();
        }
        
        public void initData(SQLiteDatabase db) {
            if(false){//平台的分类
                insertMoudule(db,mContext.getString(R.string.classical_music),R.drawable.classical_music,1);
                insertMoudule(db,mContext.getString(R.string.famous_music),R.drawable.famous_music,1);
                insertMoudule(db,mContext.getString(R.string.classic_music),R.drawable.classic_music,1);
                insertMoudule(db,mContext.getString(R.string.pop_music),R.drawable.pop_music,1);
                insertMoudule(db,mContext.getString(R.string.drama_music),R.drawable.drama_music,1);
            }else{//老人机上的分类
                insertMoudule(db,mContext.getString(R.string.square_dance_music),R.drawable.classical_music,1);
                insertMoudule(db,mContext.getString(R.string.oldies_music),R.drawable.famous_music,1);
                insertMoudule(db,mContext.getString(R.string.storytelling_music),R.drawable.classic_music,1);
                insertMoudule(db,mContext.getString(R.string.famous_music),R.drawable.pop_music,1);
                insertMoudule(db,mContext.getString(R.string.drama_music),R.drawable.drama_music,1);
                insertMoudule(db,mContext.getString(R.string.shanghai_opera_music),R.drawable.drama_music,1);
                
                //<!-- added by jiangyan for music start-->
                insertMoudule(db,mContext.getString(R.string.pure_music_txt),R.drawable.pure_music,1);
                insertMoudule(db,mContext.getString(R.string.children_music_txt),R.drawable.children_music,1);
                insertMoudule(db,mContext.getString(R.string.huangmei_opera_music_txt),R.drawable.huangmei_opera,1);
                insertMoudule(db,mContext.getString(R.string.kunqu_opera_music_txt),R.drawable.kunqu_music,1);
                insertMoudule(db,mContext.getString(R.string.qinqiang_opera_music_txt),R.drawable.qinqiang_music,1);
                insertMoudule(db,mContext.getString(R.string.bedtime_stories_txt),R.drawable.bedtime_stories,1);
               // <!-- added by jiangyan for music end-->
            }
            
        }

        private boolean insertMoudule(SQLiteDatabase db,String inventoryName, int icon, int type) {
            try {
                ContentValues values = new ContentValues();
                values.put("songInventoryName", inventoryName);
                values.put("icon", icon);
                values.put("type", type);
                long id = db.insert(INVENTORY_TABLE_NAME, null, values);
                mMapIdKey.put(inventoryName, id);
                return true;
            } catch (SQLException ex) {
                Log.d("HHJ", "insert table failure");
                return false;
            }
        }
        
        public void classification(){
            mMusicApplication.goDoQuery(this, mMusicApplication.getWhere().toString());
        }
        
        @Override
        public void onQueryConmplete(Cursor cursor) {
            if(cursor != null){
                ContentValues values = new ContentValues();
                
                int audioIdIdx;
                int snameId;// add ytq  change table
                try {
                    audioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                    snameId=cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.TITLE);
                } catch (IllegalArgumentException ex) {
                    audioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    snameId=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                }
                
                boolean insert = false;
                long inventoryId = -1;
                String sTitle ="";
                long songId=-1;
                
                while (cursor.moveToNext()) {
                    values.clear();
                    String path = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + cursor.getLong(IDCOLIDX);
                    String storagePath = getBeteenPath(Uri.parse(path), null);
                    
                    if(storagePath!=null){
                        insert = false;
                        if(storagePath.indexOf(mContext.getString(R.string.square_dance_music)) != -1 ){
                            //Log.i("hhj", "广场舞:"+storagePath);
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.square_dance_music));
                        }else if(storagePath.indexOf(mContext.getString(R.string.oldies_music)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.oldies_music));
                        }else if(storagePath.indexOf(mContext.getString(R.string.storytelling_music)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.storytelling_music));
                        }else if(storagePath.indexOf(mContext.getString(R.string.famous_music)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.famous_music));
                            Log.i("hhj", "红歌 :"+storagePath+ "   songId:"+songId+"     inventoryId:"+inventoryId);
                        }else if(storagePath.indexOf(mContext.getString(R.string.drama_music)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.drama_music));
                        }else if(storagePath.indexOf(mContext.getString(R.string.shanghai_opera_music)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.shanghai_opera_music));
                        }   // <!-- added by jiangyan for music start-->
                        else if(storagePath.indexOf(mContext.getString(R.string.pure_music_txt)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.pure_music_txt));
                            Log.i("jiangyan", "chunyinyue :"+storagePath+ "   songId:"+songId+"     inventoryId:"+inventoryId);
                        } else if(storagePath.indexOf(mContext.getString(R.string.children_music_txt)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.children_music_txt));
                        } else if(storagePath.indexOf(mContext.getString(R.string.huangmei_opera_music_txt)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.huangmei_opera_music_txt));
                        } else if(storagePath.indexOf(mContext.getString(R.string.kunqu_opera_music_txt)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.kunqu_opera_music_txt));
                        }else if(storagePath.indexOf(mContext.getString(R.string.qinqiang_opera_music_txt)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.qinqiang_opera_music_txt));
                        }else if(storagePath.indexOf(mContext.getString(R.string.bedtime_stories_txt)) != -1 ){
                            insert = true;
                            songId = cursor.getLong(audioIdIdx);
                            sTitle=cursor.getString(snameId);
                            inventoryId = mMapIdKey.get(mContext.getString(R.string.bedtime_stories_txt));
                        }
                        if(insert){
                            Cursor c = mContext.getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ inventoryId +"' and songId ='"+songId+"'", null, null);
                            if (c.getCount() == 0) {
                                values.put("songId", songId);
                                values.put("songInventoryId", inventoryId);
                                values.put("title", sTitle);
                                Uri index = mContext.getContentResolver().insert(MusicProvider.BaseMusicColumns.SONG_URI, values);
                            }
                            c.close();
                        }
                    }
                }
            }
            
        }
        
        public String getBeteenPath(Uri data, String filename) {
            Cursor cursor;
            if (data.getScheme().toString().compareTo("content") == 0) {
                cursor = mContext.getContentResolver().query(data, new String[] { Audio.Media.DATA }, null, null, null);
                if (cursor.moveToFirst()) {
                    filename = cursor.getString(0);
                }
            } else if (data.getScheme().toString().compareTo("file") == 0) // file:///开头的uri
            {
                filename = data.toString();
                filename = data.toString().replace("file://", "");
                // 替换file://
                if (!filename.startsWith("/mnt")) {
                    // 加上"/mnt"头
                    filename += "/mnt";
                }
            }
            return filename;
        }
        
        /**
         * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
         */
        @SuppressLint("NewApi")
        public static String getPath(final Context context, final Uri uri) {
     
            final boolean isKitKat = Build.VERSION.SDK_INT >= 19/*Build.VERSION_CODES.KITKAT*/;
            Log.i("hhjun", "28  paths isKitKat :"+isKitKat);
     
            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
     
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
     
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
     
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
     
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
     
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
     
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] { split[1] };
     
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
     
            return null;
        }
        
        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context
         *            The context.
         * @param uri
         *            The Uri to query.
         * @param selection
         *            (Optional) Filter used in the query.
         * @param selectionArgs
         *            (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        public static String getDataColumn(Context context, Uri uri, String selection,
                String[] selectionArgs) {
     
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = { column };
     
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }
     
        /**
         * @param uri
         *            The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        public static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }
     
        /**
         * @param uri
         *            The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        public static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }
     
        /**
         * @param uri
         *            The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        public static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
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