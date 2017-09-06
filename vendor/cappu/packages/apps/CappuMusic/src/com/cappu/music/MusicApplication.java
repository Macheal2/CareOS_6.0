package com.cappu.music;

import java.util.ArrayList;
import java.util.List;

import com.cappu.music.database.MusicProvider;

import android.app.Application;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

public class MusicApplication extends Application {
    private String[] mCursorCols;
    private TrackQueryHandler mQueryHandler;// = new TrackQueryHandler(context.getContentResolver());
    
    private Cursor mCursor;
    
    private Uri mSongUri = null;
    private String mSortOrder;
    StringBuilder mWhere;
    
    /**歌单清单列表数据*/
    private List<MusicInventory> mInventoryList;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mQueryHandler = new TrackQueryHandler(getApplicationContext().getContentResolver());
        mCursorCols = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DURATION,
                /// M: add cursor drm columns
                /*MediaStore.Audio.Media.IS_DRM,
                MediaStore.Audio.Media.DRM_METHOD,*/
                /// M: add for chinese sorting
                /*MediaStore.Audio.Media.TITLE_PINYIN_KEY,*/
                MediaStore.Audio.Media.ALBUM_ID
        };
        
        Cursor ret = null;
        mSortOrder = "title_pinyin_key";//MediaStore.Audio.Media.TITLE_PINYIN_KEY;
        
        if(mSongUri == null){
            mSongUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        
        ret = mQueryHandler.doQuery(mSongUri, mCursorCols, getWhere().toString(), null, mSortOrder, true);

        if (ret != null) {
            init(ret, false);
        }
    }
    /**获取歌单数据列表*/
    public List<MusicInventory> getInventoryData(boolean Refresh){
        if(Refresh){
            mInventoryList = updateInventoryDate();
        }
        return mInventoryList;
    }
    /**获取歌单数据*/
    public List<MusicInventory> updateInventoryDate(){
        Cursor c = getContentResolver().query(MusicProvider.BaseMusicColumns.INVENTORY_URI,null,null, null, null);
        List<MusicInventory> inventoryList = new ArrayList<MusicInventory>();
        try {
            final int idIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
            final int songInventoryNameIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_NAME);
            final int songInventoryIconIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ICON);
            final int songInventoryIconType = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_TYPE);
            while (c.moveToNext()) {
                MusicInventory musicInventory = new MusicInventory();
                musicInventory.inventoryName = c.getString(songInventoryNameIndex);
                musicInventory.id = c.getLong(idIndex);
                musicInventory.iconRes = c.getInt(songInventoryIconIndex);
                musicInventory.type = c.getInt(songInventoryIconType);
                inventoryList.add(musicInventory);
            }
            
        } catch (Exception e) {
        }finally{
            c.close();
        }
        return inventoryList;
    }
    
    public Cursor getSongCursor(){
        /*while (mCursor.moveToNext()) {
            Log.i("HHJ", " ........moveToNext.............");
            for (int i = 0; i < mCursor.getColumnCount(); i++) {
                Log.i("HHJ", mCursor.getColumnName(i)+"   "+mCursor.getString(mCursor.getColumnIndex(mCursor.getColumnName(i))));
            }
        }*/
        
        return mCursor;
    }
    private  void init(Cursor newCursor, boolean isLimited) {
        mCursor = newCursor;
    }
    
    public StringBuilder getWhere(){
        if(mWhere == null){
            mWhere = new StringBuilder();
        }else{
            mWhere.delete(0, mWhere.length());
        }
        mWhere.append(MediaStore.Audio.Media.TITLE + " != ''");
        mWhere.append(" AND " + MediaStore.Audio.Media.IS_MUSIC + "=1");
        return mWhere;
    }
    
    public void goDoQuery(QueryCompleteListener callback,String selection){
        if(mQueryHandler == null){
            mQueryHandler = new TrackQueryHandler(getApplicationContext().getContentResolver());
        }
        Log.i("hehangjun", "开始查询 where.toString():"+mWhere.toString());
        mQueryHandler.doQuery(mSongUri, mCursorCols, mWhere.toString(), null, mSortOrder, true);
        this.mCallBack = callback;
    }
    
    
    class TrackQueryHandler extends AsyncQueryHandler {

        class QueryArgs {
            public Uri uri;
            public String [] projection;
            public String selection;
            public String [] selectionArgs;
            public String orderBy;
        }

        TrackQueryHandler(ContentResolver res) {
            super(res);
        }
        
        public Cursor doQuery(Uri uri, String[] projection,String selection, String[] selectionArgs,String orderBy, boolean async) {
            if (async) {
                // Get 100 results first, which is enough to allow the user to start scrolling,
                // while still being very fast.
                Uri limituri = uri.buildUpon().appendQueryParameter("limit", "100").build();
                QueryArgs args = new QueryArgs();
                args.uri = uri;
                args.projection = projection;
                args.selection = selection;
                args.selectionArgs = selectionArgs;
                args.orderBy = orderBy;

                startQuery(0, args, uri, projection, selection, selectionArgs, orderBy);
                return null;
            }
            return MusicUtils.query(getApplicationContext(),uri, projection, selection, selectionArgs, orderBy);
        }

        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.i("HHJ", "123  onQueryComplete  :"+(cursor!=null)+"    "+(mCallBack!=null));
            if(mCallBack!=null){
                mCallBack.onQueryConmplete(cursor);
            }else{
                init(cursor, cookie != null);
            }
            
            if (token == 0 && cookie != null && cursor != null && !cursor.isClosed() && cursor.getCount() >= 100) {
                QueryArgs args = (QueryArgs) cookie;
                startQuery(1, null, args.uri, args.projection, args.selection, args.selectionArgs, args.orderBy);
            }
        }
    }
    
    QueryCompleteListener mCallBack;
    public interface QueryCompleteListener{
        public void onQueryConmplete(Cursor cursor);
    }
    
    
    // and ytq  这里是关闭usb链接用到的变量
    int mCount=0;

	public int getCount() {
		return mCount;
	}
	public void setCount() {
	    mCount++;
	}
	public void setzoer()
	{
	    mCount=0;
	}
	//第一次判断是否有sd卡
	boolean mFage=true;
	public void setfage()
	{
	    mFage=false;
	}
	public boolean  getfage()
	{
		return mFage;
	}
   
    
}
