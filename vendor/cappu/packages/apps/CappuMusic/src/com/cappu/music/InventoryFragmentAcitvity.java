package com.cappu.music;

import java.util.ArrayList;
import java.util.List;

import com.cappu.music.MusicApplication.QueryCompleteListener;
import com.cappu.music.MusicUtils.ServiceToken;
import com.cappu.music.database.MusicProvider;
import com.cappu.music.entiy_gai.Songgengxin;
import com.cappu.music.widget.MenuPopupWindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class InventoryFragmentAcitvity extends Activity implements OnClickListener,OnItemClickListener,QueryCompleteListener,ServiceConnection{

    private long InventoryId;
    MusicInventory mMusicInventory;
    private TextView mInventoryTitleView;
    private TextView mSongBatch;
    MenuPopupWindow mMenuPopupWindow;
    
    private ListView mListView;
    private MusicApplication mMusicApplication;
    private TrackListAdapter mTrackListAdapter;
    
    Cursor mAllSongCursor;
    private IMediaPlaybackService mService = null;
    
    private ServiceToken mToken;
    
    List<Long> mInventorySongIDList;
    private Songgengxin songgengxin;
    private boolean fage=false;
    
    private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mListView.invalidateViews();
            if (mService != null) {
                MusicUtils.updateNowPlaying(InventoryFragmentAcitvity.this, getResources().getConfiguration().orientation,mAllSongCursor);
            }
        }
    };
    
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_fragment);
        InventoryId = getIntent().getLongExtra("InventoryId", -1);
        fage=getIntent().getBooleanExtra("songboolean", false);
        if(InventoryId == -1){
            finish();
        }
        songgengxin=new Songgengxin(this);//yuan tong qin add
        mMusicInventory = initInventory();
        mMusicApplication = (MusicApplication) getApplication();
       
	    
        initView();
        getSongList();
 //       Log.i("test", "=oncreate=inventoryfragment=进来了没有==");
        mToken = MusicUtils.bindToService(this, this);
        
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.META_CHANGED);
        f.addAction(MediaPlaybackService.QUEUE_CHANGED);
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        registerReceiver(mTrackListListener, new IntentFilter(f));
    }
    public void initView(){
        mInventoryTitleView = (TextView) findViewById(R.id.inventory_title);
        mInventoryTitleView.setText(mMusicInventory.getInventoryName());
        mListView = (ListView) findViewById(android.R.id.list);
        mSongBatch = (TextView) findViewById(R.id.song_Batch);
        mSongBatch.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        mInventoryTitleView.setOnClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if(mAllSongCursor != null){
            MediaPlaybackService.mNowPlayList = InventoryId;
            MusicUtils.playAll(this,mAllSongCursor, arg2);
        }
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	 //yuan tong qin add 
    	fage=false;
        getSongList();
    	
    }
    
    @Override
    protected void onResume() {
        super.onResume();
       
    }
 
    List<Long> mylistb=new ArrayList<Long>();
    @SuppressLint("NewApi")
    public void getSongList(){
	//yuan tong qin add 
    	Cursor getcursor = getcursor();
    	new Myasynctask().execute(getcursor);
//    	long aa=System.currentTimeMillis();
//        mylistb.clear();
//        if(mInventorySongIDList == null){
//            mInventorySongIDList = new ArrayList<Long>();
//        }else{
//            mInventorySongIDList.clear();
//        }
//        
//        Cursor cursor = getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ InventoryId +"'", null, null);
//        if(cursor!=null && cursor.getCount() == 0){
//            mListView.setVisibility(View.INVISIBLE);
//            cursor.close();
//            return;
//        }else{
//            mListView.setVisibility(View.VISIBLE);
//        }
//        StringBuilder mWhere = new StringBuilder();
//        if(cursor != null && cursor.getCount()>0){
//            mWhere.append(" AND (");
//        }
//        try {
//            final int IdIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
//            final int inventoryIdIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ID);
//            final int songIdIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_ID);
//            int i = 0;
//            long ID = -1;
//            long inventoryId = -1;
//            long songId = -1;
//            while (cursor.moveToNext()) {
//                ID = cursor.getLong(IdIndex);
//                inventoryId = cursor.getLong(inventoryIdIndex);
//                songId = cursor.getLong(songIdIndex);
//                Log.i("test", "getSongList cursor ID:"+ID+"   inventoryId:"+inventoryId+"    songId: "+songId);
//                mInventorySongIDList.add(songId);              
//                boolean fage=false;
//                for (int j = 0; j < mylistb.size(); j++) {
//                	if(songId!=0){
//	                    if(songId==mylistb.get(j))
//	                    {
//	                        fage=true;
//	                    }
//                	}
//                }
//                if(fage){
//                	//yuan tong qin add 如果歌曲的id相同就删除  因为全部为0了
//                   getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, "songInventoryId = '"+ InventoryId +"' and _id= "+ID, null);
//                }else{
//                    mylistb.add(songId);
//                    try {
//                        mWhere.append(" "+MediaStore.Audio.Media._ID + " = '"+songId+"' ");
//                        /*mWhere.append(" "+MediaStore.Audio.Playlists.Members.AUDIO_ID + " = '"+songId+"' ");*/
//                        if(!cursor.isLast()){
//                            mWhere.append(" or ");
//                        }
//                        
//                    } catch (IllegalArgumentException ex) {
//                        mWhere.append(MediaStore.Audio.Media._ID + "="+songId);
//                    }
//                }
//            }
//            if(cursor != null && cursor.getCount()>0){
//                mWhere.append(" ) ");
//            }
//            Log.i("HHJ", "mWhere:"+mWhere.toString());
//        } catch (Exception e) {
//        }finally{
//            if(cursor != null){
//                cursor.close();
//            }
//        }
//        StringBuilder select  = mMusicApplication.getWhere();
//        select.append(mWhere.toString());
//       long bb= System.currentTimeMillis();
//       Log.i("test", bb-aa+"==时间");
//        mMusicApplication.goDoQuery(this,select.toString());
    }
    
    //yuan tong qin add 
    
    public Cursor getcursor(){
//    	long a=System.currentTimeMillis();
    		mylistb.clear();
	        if(mInventorySongIDList == null){
	            mInventorySongIDList = new ArrayList<Long>();
	        }else{
	            mInventorySongIDList.clear();
	        }
	        
	        Cursor cursor = getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ InventoryId +"'", null, null);
	        if(cursor!=null && cursor.getCount() == 0){
	            mListView.setVisibility(View.INVISIBLE);
	            cursor.close();
	           return null;
	        }else{
	            mListView.setVisibility(View.VISIBLE);
	        }
//	        long b=System.currentTimeMillis();
//	        Log.i("test",cursor.getCount()+"==数据长度==时间=="+(b-a));
	        return cursor;
    }
    
    private class Myasynctask extends AsyncTask{

		@Override
		protected Object doInBackground(Object... params) {

			Cursor cursor=(Cursor) params[0];
			StringBuilder mWhere = new StringBuilder();
	        if(cursor != null && cursor.getCount()>0){
	            mWhere.append(" AND (");
	        }
	        try {
		        if(cursor!=null){
		            final int IdIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
		            final int inventoryIdIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ID);
		            final int songIdIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_ID);
		            int i = 0;
		            long ID = -1;
		            long inventoryId = -1;
		            long songId = -1;
		            while (cursor.moveToNext()) {
		                ID = cursor.getLong(IdIndex);
		                inventoryId = cursor.getLong(inventoryIdIndex);
		                songId = cursor.getLong(songIdIndex);
		                Log.i("test", "getSongList cursor ID:"+ID+"   inventoryId:"+inventoryId+"    songId: "+songId);
		                mInventorySongIDList.add(songId);              
		                boolean fage=false;
		                for (int j = 0; j < mylistb.size(); j++) {
		                	if(songId!=0){
			                    if(songId==mylistb.get(j))
			                    {
			                        fage=true;
			                    }
		                	}
		                }
		                if(fage){
		                	//yuan tong qin add 如果歌曲的id相同就删除  因为全部为0了
		                   getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, "songInventoryId = '"+ InventoryId +"' and _id= "+ID, null);
		                }else{
		                    mylistb.add(songId);
		                    try {
		                        mWhere.append(" "+MediaStore.Audio.Media._ID + " = '"+songId+"' ");
		                        /*mWhere.append(" "+MediaStore.Audio.Playlists.Members.AUDIO_ID + " = '"+songId+"' ");*/
		                        if(!cursor.isLast()){
		                            mWhere.append(" or ");
		                        }
		                        
		                    } catch (IllegalArgumentException ex) {
		                        mWhere.append(MediaStore.Audio.Media._ID + "="+songId);
		                    }
		                }
		            }
		            if(cursor != null && cursor.getCount()>0){
		                mWhere.append(" ) ");
		            }
		            Log.i("HHJ", "mWhere:"+mWhere.toString());
		        }
	        } catch (Exception e) {
	        }finally{
	            if(cursor != null){
	                cursor.close();
	            }
	        }
	        StringBuilder select  = mMusicApplication.getWhere();
	        select.append(mWhere.toString());
			return select;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if(result!=null){
				StringBuilder select=(StringBuilder) result;
				mMusicApplication.goDoQuery(InventoryFragmentAcitvity.this,select.toString());
				if(fage){
					new TwoAsyncTask().execute(select.toString());
				}
			}
		}
    	
    }
    //yuan tong qin add 
    class TwoAsyncTask extends AsyncTask{

		@Override
		protected Object doInBackground(Object... params) {
			String select = (String) params[0];
			if(fage){
		        songgengxin.selectzongsong();
			    songgengxin.selectfenzongsong();
			    songgengxin.update();
			    songgengxin.deletefensong();
			    mMusicApplication.goDoQuery(InventoryFragmentAcitvity.this,select);
			}
			return null;
		}
    	
    }
    
    
    
    
    
    public MusicInventory initInventory(){
        MusicInventory musicInventory = null;
        Cursor cursor = getContentResolver().query(MusicProvider.BaseMusicColumns.INVENTORY_URI, null, "_id = '"+ InventoryId +"'", null, null);
        try {
            final int idIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
            final int songInventoryNameIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_NAME);
            final int songInventoryIconIndex = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ICON);
            final int songInventoryIconType = cursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_TYPE);
            while (cursor.moveToNext()) {
                musicInventory = new MusicInventory();
                musicInventory.inventoryName = cursor.getString(songInventoryNameIndex);
                musicInventory.id = cursor.getLong(idIndex);
                musicInventory.iconRes = cursor.getInt(songInventoryIconIndex);
                musicInventory.type = cursor.getInt(songInventoryIconType);
            }
        } catch (Exception e) {
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }
        if(musicInventory != null){
            Log.i("HHJ", "musicInventory:"+musicInventory.toString());
            return musicInventory;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTrackListListener != null){
            unregisterReceiver(mTrackListListener);
        }
        if(mToken!=null){
            MusicUtils.unbindFromService(mToken);
        }
    }

    static class TrackListAdapter extends SimpleCursorAdapter implements SectionIndexer {
        boolean mIsNowPlaying;
        boolean mDisableNowPlayingIndicator;

        int mTitleIdx;
        int mArtistIdx;
        int mDurationIdx;
        int mAudioIdIdx;

        private final StringBuilder mBuilder = new StringBuilder();
        private String mUnknownArtist;
        private String mUnknownAlbum;
        
        private AlphabetIndexer mIndexer;
        
        private Activity mActivity = null;
        private String mConstraint = null;
        private boolean mConstraintIsValid = false;
        
        private final BitmapDrawable mDefaultAlbumIcon;
        private final BitmapDrawable mBackgroundAlbumIcon;
        private static final int REFRESH_ALBUM_ART_DELAY_TIME = 100;
        private AnimationDrawable mAnimPlay = null;
        private ImageView mPlayPause = null;
        int mIsDrmIdx = -1;
        int mDrmMethodIdx = -1;
        /// M: add for chinese sorting, title pinyin index.
        int mTitlePinyinIdx;

        public static class ViewHolder {
            long ID = -1;
            /**歌曲名*/
            TextView line1;
            TextView line2;
            TextView duration;
            ImageView play_indicator;
            CharArrayBuffer buffer1;
            char [] buffer2;
            ImageView drmLock;
            ImageView editIcon;
        }

        
        @Override
        public int getCount() {
            return super.getCount();
        }

        TrackListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to,
                boolean isnowplaying, boolean disablenowplayingindicator) {
            super(context, layout, cursor, from, to);
            mActivity = (Activity) context;
            getColumnIndices(cursor);
            mIsNowPlaying = isnowplaying;
            mDisableNowPlayingIndicator = disablenowplayingindicator;
            mUnknownArtist = context.getString(R.string.unknown_artist_name);
            mUnknownAlbum = context.getString(R.string.unknown_album_name);
            

            Resources r = context.getResources();
            Bitmap b = BitmapFactory.decodeResource(r, R.drawable.albumart_mp_unknown_list);
            mDefaultAlbumIcon = new BitmapDrawable(context.getResources(), b);
            mDefaultAlbumIcon.setFilterBitmap(false);
            mDefaultAlbumIcon.setDither(false);
            Bitmap backgroud = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
            mBackgroundAlbumIcon = new BitmapDrawable(context.getResources(), backgroud);
            mBackgroundAlbumIcon.setFilterBitmap(false);
            mBackgroundAlbumIcon.setDither(false);
        }
        
        private void getColumnIndices(Cursor cursor) {
            if (cursor != null) {
                mTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                mDurationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                try {
                    mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                } catch (IllegalArgumentException ex) {
                    mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                }

            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            ViewHolder vh = new ViewHolder();
            vh.line1 = (TextView) v.findViewById(R.id.line1);
            vh.line1.setTextColor(Color.parseColor("#384653"));
            vh.line2 = (TextView) v.findViewById(R.id.line2);
            vh.line2.setTextColor(Color.parseColor("#bbbbbb"));//yuan tong qin add color
            vh.duration = (TextView) v.findViewById(R.id.duration);
            vh.duration.setTextColor(Color.parseColor("#384653"));
            vh.duration.setVisibility(View.VISIBLE);
            vh.play_indicator = (ImageView) v.findViewById(R.id.play_indicator);
            vh.buffer1 = new CharArrayBuffer(100);
            vh.buffer2 = new char[200];
            vh.drmLock = (ImageView) v.findViewById(R.id.drm_lock);
            vh.editIcon = (ImageView) v.findViewById(R.id.icon);
            v.setTag(vh);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            //Log.i("HHJ", "bindView      ------------------------");
            ViewHolder vh = (ViewHolder) view.getTag();
            
            cursor.copyStringToBuffer(mTitleIdx, vh.buffer1);
            vh.line1.setText(vh.buffer1.data, 0, vh.buffer1.sizeCopied);
            
            int secs = cursor.getInt(mDurationIdx) / 1000;
            vh.duration.setText(MusicUtils.makeTimeString(context, secs));
            final StringBuilder builder = mBuilder;
            builder.delete(0, builder.length());

            vh.ID = cursor.getLong(mAudioIdIdx);
            String name = cursor.getString(mArtistIdx);
            if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                builder.append(mUnknownArtist);
            } else {
                builder.append(name);
            }
            int len = builder.length();
            if (vh.buffer2.length < len) {
                vh.buffer2 = new char[len];
            }
            builder.getChars(0, len, vh.buffer2, 0);
            vh.line2.setText(vh.buffer2, 0, len);
            
            long album = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            ImageView ivEdit = vh.editIcon;
            ivEdit.setVisibility(View.GONE);

            ImageView iv = vh.play_indicator;
            long id = -1;
            if (MusicUtils.sService != null) {
                try {
                    if (mIsNowPlaying) {
                        id = MusicUtils.sService.getQueuePosition();
                    } else {
                        id = MusicUtils.sService.getAudioId();
                    }
                } catch (RemoteException ex) {
                }
            }
            
            if ( (mIsNowPlaying && cursor.getPosition() == id) || (!mIsNowPlaying && !mDisableNowPlayingIndicator && cursor.getLong(mAudioIdIdx) == id)) {
                iv.setVisibility(View.VISIBLE);
                vh.line1.setTextColor(Color.parseColor("#e55b09"));
                iv.setBackgroundResource(R.anim.list_nowplaying_ani);
                mAnimPlay = (AnimationDrawable) iv.getBackground();
                if (MusicUtils.isPlaying()) {
                    mAnimPlay.start();
                } else {
                    mAnimPlay.stop();
                }
                mPlayPause = ivEdit;
            } else {
                iv.setVisibility(View.GONE);
                vh.line1.setTextColor(Color.parseColor("#384653"));
            }
        }
        
        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor != null) {
                super.changeCursor(cursor);
                getColumnIndices(cursor);
            }
        }
        
        public Object[] getSections() {
            if (mIndexer != null) { 
                return mIndexer.getSections();
            } else {
                return new String [] { " " };
            }
        }
        
        public int getPositionForSection(int section) {
            if (mIndexer != null) {
                return mIndexer.getPositionForSection(section);
            }
            return 0;
        }
        
        public int getSectionForPosition(int position) {
            if (mIndexer != null) {
                return mIndexer.getSectionForPosition(position);
            }
            return 0;
        }
    }

    @Override
    public void onQueryConmplete(Cursor cursor) {
//    	long aa=System.currentTimeMillis();
        if(cursor!=null)
        {
            mAllSongCursor =cursor;
            mTrackListAdapter = new TrackListAdapter(this,R.layout.track_list_item,cursor, new String[] {},  new int[] {}, false,false);
   //       long bb=System.currentTimeMillis();
  //        Log.i("test",bb-aa+"===第二时间==");
            mListView.setAdapter(mTrackListAdapter);
            MusicUtils.updateNowPlaying(InventoryFragmentAcitvity.this, getResources().getConfiguration().orientation,mAllSongCursor);
            
    //        if(cursor!=null && cursor.getCount()>0){
                mSongBatch.setVisibility(View.VISIBLE);
    //        }else{
    //            mSongBatch.setVisibility(View.GONE);
    //        }
            
            try {
                //CorrectInventoryCount(cursor);
            } catch (Exception e) {
                Log.i("HHJ", " 清理歌单中没有的歌曲 异常:"+e.toString());
            }
        }
        
    }
    
    /**
     * 清理歌单中没有的歌曲
     * 防止歌单中歌曲被意外删除后没有了/数据不对
     * */
    public void CorrectInventoryCount(Cursor SongCursor){
        while (SongCursor.moveToNext()) {
            long songId = SongCursor.getLong(SongCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID /*MediaStore.Audio.Media.ARTIST*/));
            if(mInventorySongIDList.contains(songId)){
                mInventorySongIDList.remove(songId);
            }
        }
        
        for (long id:mInventorySongIDList) {
            getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, " songId = '"+ id+"' ", null);
        }
        
    }

    @Override
    public void onServiceConnected(ComponentName arg0, IBinder service) {
        mService = IMediaPlaybackService.Stub.asInterface(service);
        MusicUtils.updateNowPlaying(this, getResources().getConfiguration().orientation,mAllSongCursor);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mService = null;
    }
    @Override
    public void onClick(View view) {
        if(view == mInventoryTitleView){
            this.finish();
        }else if(view == mSongBatch){
            shaoMenu(this);
        }
    }
    
    public void shaoMenu(final Activity context) {
        if(mMenuPopupWindow == null){
            mMenuPopupWindow = new MenuPopupWindow(this,mMusicInventory,InventoryId);
        }
        // 显示窗口
        mMenuPopupWindow.showAtLocation(this.findViewById(R.id.inventory_layout),
                 Gravity.RIGHT | Gravity.TOP, 0, DensityUtil.dip2px(this,90)); // 设置layout在PopupWindow中显示的位置
    }

}
