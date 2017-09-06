package com.cappu.music;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cappu.music.MusicApplication.QueryCompleteListener;
import com.cappu.music.MusicUtils.ServiceToken;
import com.cappu.music.animation.ExpandAnimation;
import com.cappu.music.database.MusicProvider;
import com.cappu.music.entiy_gai.ClassifySong;
import com.cappu.music.entiy_gai.Song;
import com.cappu.music.entiy_gai.Songgengxin;
import com.cappu.music.mediatek.MusicFeatureOption;
import com.cappu.music.mediatek.OmaDrmStore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PlayFragment extends ListFragment implements ServiceConnection,OnClickListener,OnScrollListener,QueryCompleteListener {
    private MusicApplication mMusicApplication;
    
    private TextView mFootViewCount;
    private ListView mTrackList;
    SyncImageLoader mSyncImageLoader;
    private TrackListAdapter mAdapter;
    //private ListBaseAdapter mAdapter;
    
    private int mLastListPosCourse = -1;
    private int mLastListPosFine = -1;
    
    private String mPlaylist;
    
    private String[] mCursorCols;
    
    private Cursor mTrackCursor;
    private int mVisibleItemsCount = 7;
    
  /// M: track position which is selected by user. 当前选中播放的位置
    private int mCurTrackPos = -1;
    
    private ServiceToken mToken;
    private IMediaPlaybackService mService = null;
    
  /// M: record the screen orientation for nowplaying update.
    private int mOrientation;
    
    private static final int REFRESH = 1;
    private static final int LIST_VIEW_UPDATE_STATUS = 2;
    
    IntentFilter mTrackListListenerIntentFilter = null;
    IntentFilter mScanListenerIntentFilter = null;
    
    private ColorDrawable mColorDrawable = new ColorDrawable(0xb0000000); 
    private PopupWindow mPopupWindowOption;
    private View mOptionViewDialogView;
    private View mOptionLayout;
    private TextView mDialogTitle;
    private TextView mDialogAddToButton;
    private TextView mDialogSettingRingButton;
    private TextView mDialogDeleteButton;
    private LayoutInflater mInflater;
    private TextView mCancelButton;//yuan tong qin add 
    private ListView mInventoryList;
    
    public  View mNowPlayingView;
    public TextView trackNameView;
    public TextView artistNameView;
    public ImageView miniPlayerThumbnail;
    public ImageView preBtn;
    public ImageView playBtn;
    public ImageView nextBtn;
    
    private String mCurrentTrackName;
    private long mSelectedId;
    private String mSongname;
    
    private List<MusicInventory> mInventoryListdate;
    private InventoryAdapter mInventoryAdapter;
    
    private Activity mActivity;
    View mRootView;
   private  MusicApplication myapplication;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.play_picker_fragment, container, false);
        if(mActivity == null){
            this.mActivity = getActivity();
            songgengxin = new Songgengxin(mActivity);
   			myapplication = (MusicApplication) mActivity.getApplication();
            if(mActivity instanceof MusicBrowserActivity){
                Log.i("hehangjun", "PlayFragment mActivity instanceof MusicBrowserActivity ");
                MusicBrowserActivity mba = (MusicBrowserActivity) mActivity;
                mba.setPlayFragment(this);
                if(mTrackListListenerIntentFilter == null){
                    mTrackListListenerIntentFilter = new IntentFilter();
                    mTrackListListenerIntentFilter.addAction(MediaPlaybackService.META_CHANGED);
                    mTrackListListenerIntentFilter.addAction(MediaPlaybackService.QUEUE_CHANGED);
                    mTrackListListenerIntentFilter.addAction("samsung_music_update_list_current");
                    mTrackListListenerIntentFilter.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
                    //musicmaggcom_wodeyueyin_delete  yuan tong qin add 
                    mTrackListListenerIntentFilter.addAction("musicmaggcom_wodeyueyin_delete");
                    
                }
                mActivity.registerReceiver(mTrackListListener, new IntentFilter(mTrackListListenerIntentFilter));
                
                if(mScanListenerIntentFilter == null){
                    mScanListenerIntentFilter = new IntentFilter();
                    mScanListenerIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
                    mScanListenerIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
                    mScanListenerIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
                    mScanListenerIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                    mScanListenerIntentFilter.addDataScheme("file");
                }
                
                mActivity.registerReceiver(mScanListener, new IntentFilter(
                        mScanListenerIntentFilter));
            }
            
        }
        
        mInflater = inflater;
        mToken = MusicUtils.bindToService(mActivity, this);
        mMusicApplication = (MusicApplication) mActivity.getApplication();
        mFootViewCount = new TextView(mActivity);
        mFootViewCount.setBackgroundColor(Color.parseColor("#e8e8e8"));
        mFootViewCount.setTextSize(14);
        mFootViewCount.setTextColor(Color.parseColor("#384653"));
        mFootViewCount.setGravity(Gravity.CENTER);
        mFootViewCount.setPadding(0, 5, 0, 5);
        mFootViewCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
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
        mOrientation = getResources().getConfiguration().orientation;
        mTrackList = (ListView) mRootView.findViewById(android.R.id.list);//getListView();
        mTrackList.addFooterView(mFootViewCount);
        mTrackList.setOnCreateContextMenuListener(this);
        mTrackList.setOnScrollListener(this);
        
        mTrackList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                
                if(mPopupWindowOption == null){
                    initPopupWindow();
                }
                if(viewHolder.ID != -1){
                    mSelectedId = viewHolder.ID;
                }
				//yuan tong qin add 
                if(viewHolder.line1.getText()!=null){
                	mSongname =viewHolder.line1.getText().toString();
                }
                mPopupWindowOption.setContentView(mOptionViewDialogView);
                mDialogTitle.setText(viewHolder.buffer1.data, 0, viewHolder.buffer1.sizeCopied);
                mCurrentTrackName = mDialogTitle.getText().toString();
                mPopupWindowOption.showAtLocation(mActivity.findViewById(android.R.id.tabhost), Gravity.BOTTOM, 0, 0);
                mActivity.findViewById(android.R.id.tabhost).setOnKeyListener(new OnKeyListener() {
                    
                    @Override
                    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                        Log.i("HHJ", "-------------++++++++++++++++++++++++++++++-------------------");
                        return false;
                    }
                });
                Log.i("hehangjun", "mCurrentTrackName:"+mCurrentTrackName+"    mSelectedId:"+mSelectedId);
                return true;
            }
        });
        
        initAdapter();
        initPopupWindow();
		// add ytq shanchuneikashang meiyoudegequ
        if (myapplication.getfage()) {
            songgengxin.selectzongsong();
            songgengxin.selectfenzongsong();
            songgengxin.deletefensong();
            myapplication.setfage();
 //           Log.i("test", "===这个是进来就用==");
        }
        return mRootView;
    }



    @Override
    public void onResume() {
        super.onResume();
        if(mActivity != null && mTrackCursor!=null){
            updateNowPlaying(mActivity, mOrientation, mTrackCursor);
        }

    }
    
    private void initPopupWindow(){
        mOptionViewDialogView = mInflater.inflate(R.layout.playlist_option_layout, null);
        mOptionLayout = mOptionViewDialogView.findViewById(R.id.option_layout);
        mDialogTitle = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_title);
        mDialogAddToButton = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_add_to_button);
        mDialogSettingRingButton = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_setting_ring_button);
        mDialogDeleteButton = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_delete_button);
        mInventoryList = (ListView) mOptionViewDialogView.findViewById(R.id.inventory_list);
        mCancelButton = (TextView) mOptionViewDialogView.findViewById(R.id.cancel_button);
        mDialogAddToButton.setOnClickListener(this);
        mDialogSettingRingButton.setOnClickListener(this);
        mDialogDeleteButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        
        
        mInventoryList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                InventoryAdapter.InventoryViewHolder inVentory = (InventoryAdapter.InventoryViewHolder) view.getTag();
                
                Cursor c = mActivity.getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ inVentory.mInventoryId +"' and songId ='"+mSelectedId+"'", null, null);
                long ID = -1;
                long inventoryId = -1;
                long songId = -1;
                try {
                    final int IdIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
                    final int inventoryIdIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ID);
                    final int songIdIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_ID);
                    while (c.moveToNext()) {
                        ID = c.getLong(IdIndex);
                        inventoryId = c.getLong(inventoryIdIndex);
                        songId = c.getLong(songIdIndex);
                    }
                } catch (Exception e) {
                }
                
                Log.i("hehangjun", "inVentory.mInventoryI:"+inVentory.mInventoryId+"   inventoryId:"+inventoryId+"  songId:"+songId+"   mSelectedId:"+mSelectedId+"   c:"+c.getCount());
                if(inVentory.mInventoryId == inventoryId){
                    //Toast.makeText(getActivity(), "该歌曲已在此歌单里", Toast.LENGTH_SHORT).show();
                    mActivity.getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, " _id = '"+ID+"'", null);
                }else {
                    ContentValues values = new ContentValues();
                    values.put("songId", mSelectedId);
                    values.put("songInventoryId", inVentory.mInventoryId);
                    values.put("title", mSongname);//yuan tong qin add 
                    Uri index = mActivity.getContentResolver().insert(MusicProvider.BaseMusicColumns.SONG_URI, values);
                    Log.i("hehangjun", "index "+index.toString());
                    Toast.makeText(mActivity, "歌曲成功添加到 "+inVentory.nName.getText().toString()+" 歌单", Toast.LENGTH_SHORT).show();
                }
                
                mInventoryListdate = ((MusicApplication)mActivity.getApplication()).getInventoryData(true);
                if(mInventoryAdapter == null){
                    mInventoryAdapter = new InventoryAdapter(mActivity, mInventoryListdate);
                    mInventoryList.setAdapter(mInventoryAdapter);
                }
                mInventoryAdapter.setInventoryDate(mInventoryListdate);
                mInventoryAdapter.notifyDataSetChanged();
                
            }
        });
        
        mPopupWindowOption = new PopupWindow(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mPopupWindowOption.setBackgroundDrawable(mColorDrawable);
        mPopupWindowOption.setFocusable(true);
        mPopupWindowOption.setAnimationStyle(R.style.popdialogshow);
        mPopupWindowOption.update();
        
        mPopupWindowOption.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
        mPopupWindowOption.setOnDismissListener(new OnDismissListener() {
            
            @Override
            public void onDismiss() {
                mOptionLayout.setVisibility(View.VISIBLE);
                mInventoryList.setVisibility(View.GONE);
                mCancelButton.setText(R.string.cancel);
            }
        });
        
        mOptionViewDialogView.setFocusableInTouchMode(true);
        mOptionViewDialogView.setOnKeyListener(new android.view.View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindowOption.isShowing())) {
                    mPopupWindowOption.dismiss();
                    return true;
                }
                return false;
            }
        });
    }
    private void initAdapter() {
        if (mAdapter == null) {
            mAdapter = new TrackListAdapter(mActivity, R.layout.track_list_item, null, new String[] {}, new int[] {});
            mTrackList.setAdapter(mAdapter);
            init(null);
            mSyncImageLoader = new SyncImageLoader(mActivity);
        }
        mMusicApplication.goDoQuery(PlayFragment.this, mMusicApplication.getWhere().toString());
    }
    
    
    public void Refresh() {
        if (mTrackCursor != null) {
            updateNowPlaying(mActivity, mOrientation, mTrackCursor);
        }else{
            Log.i("hehangjun", "playfragment Refresh mTrackCursor is null");
        }
    }
    
    
    public void init(Cursor newCursor) {

        if (mAdapter == null) {
            return;
        }
        Log.i("hehangjun", "playfragment init changeCursor");
        mAdapter.changeCursor(newCursor); // also sets mTrackCursor
        if (mTrackCursor == null) {
            MusicUtils.displayDatabaseError(getActivity(),mRootView, true);
            mReScanHandler.sendEmptyMessageDelayed(0, 10);
            return;
        }
        MusicUtils.emptyShow(mTrackList, mActivity);
        MusicUtils.hideDatabaseError(mActivity);
        
        mLastListPosCourse = mTrackList.getFirstVisiblePosition();
        View cv = mTrackList.getChildAt(0);
        if (cv != null) {
            mLastListPosFine = cv.getTop();
        }
        
        if (mLastListPosCourse >= 0 ) {
            mTrackList.setSelectionFromTop(mLastListPosCourse, mLastListPosFine);
        }
        Log.i("hehangjun", "playfragment init mTrackCursor mLastListPosCourse:"+(mLastListPosCourse)+"   mLastListPosFine:"+(mLastListPosFine));
        setFootView();
    }
    
    public void onDestroyunregisterReceiver() {
        if(mTrackListListener!=null && mActivity!=null){
            mActivity.unregisterReceiver(mTrackListListener);
        }
        
        if(mScanListener!=null && mActivity!=null){
            mActivity.unregisterReceiver(mScanListener);
        }
        
        if(mToken!=null){
            MusicUtils.unbindFromService(mToken);
        }
    }
    
    public void setFootView() {
        //Log.i("hehangjun", "........................................................  setFootView mAdapter:"+mAdapter.getCount());
        if (mFootViewCount == null) {
            mFootViewCount = new TextView(mActivity);
            mFootViewCount.setBackgroundColor(Color.parseColor("#19222a"));
            mFootViewCount.setTextSize(14);
            mFootViewCount.setTextColor(Color.parseColor("#92a0ad"));
            mFootViewCount.setGravity(Gravity.CENTER);
            mFootViewCount.setPadding(0, 5, 0, 5);
        }
        
        if (mAdapter != null && mAdapter.getCount() > 0) {
//        	mAdapter.notifyDataSetChanged();//yuan tong qin add 
        	
            setfootviewtext();
            
            mFootViewCount.setVisibility(View.VISIBLE);
            int viewCount = mTrackList.getFooterViewsCount();
            //Log.i("hehangjun", "........................................................  setFootView mAdapter viewCount:"+viewCount);
            if (viewCount == 0) {
                mTrackList.addFooterView(mFootViewCount);//yuan tong qin add 追加到最后的一个view
            }
            
            View v = mActivity.findViewById(R.id.nowplaying);
            View parent = (View) v.getParent();
            if (v != null && (parent.getVisibility() == View.GONE || parent.getVisibility() == View.INVISIBLE)) {
                parent.setVisibility(View.VISIBLE);
            }
            
        } else {
            int viewCount = mTrackList.getFooterViewsCount();
            if (viewCount != 0) {
                mTrackList.removeFooterView(mFootViewCount);
            }
        }
    }
    //yuan tong qin add 
	private void setfootviewtext() {
		mAdapter.notifyDataSetChanged();
		int count = mAdapter.getCount();
		String f;
		try {
		    f = getResources().getQuantityText(R.plurals.Nsongs, count).toString();
		} catch (Exception e) {
		    f = mActivity.getResources().getQuantityText(R.plurals.Nsongs, count).toString();
		    //Log.e("test", "....................................................................................................");
		}
		
		mFootViewCount.setText(String.format(f, count));
	}

    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long selectid) {
        Log.i("hehangjun", "mTrackCursor position:" + (position));
        if (mTrackCursor.getCount() == 0) {
            return;
        }
        
        if( listView != null){
            MediaPlaybackService.mNowPlayList = -1;
            int visible = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
            for (int i = 0; i < visible; i++) {
                ViewHolder viewHolder = (ViewHolder) listView.getChildAt(i).getTag();
                
                if (viewHolder.position == position) {
                    viewHolder.play_indicator.setVisibility(View.VISIBLE);
                    viewHolder.line1.setTextColor(Color.parseColor("#e55b09"));//Color.parseColor("#40afff")
                    viewHolder.play_indicator.setBackgroundResource(R.anim.list_nowplaying_ani);
                    AnimationDrawable mAnimPlay = (AnimationDrawable) viewHolder.play_indicator.getBackground();
                    try {
                        if (mService.isPlaying()) {
                            mAnimPlay.start();
                        } else {
                            mAnimPlay.stop();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    viewHolder.play_indicator.setVisibility(View.GONE);
                    viewHolder.line1.setTextColor(Color.parseColor("#384653"));
                }
            }
        }
        
        MusicUtils.playAll(mActivity, mTrackCursor, position);
    }
    
    
    private BroadcastReceiver mTrackListListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mUpdateUIHandler.sendEmptyMessageDelayed(0, 100);
            //yuan tong qin add 
            String action= intent.getAction();
            if(action.equals("musicmaggcom_wodeyueyin_delete")){
            	if(mAdapter!=null){
            		setfootviewtext();
            	}
            }
            
        }
    };
    
    private Handler mUpdateUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTrackList.invalidateViews();
            if (mService != null) {
                //MusicUtils.updateNowPlaying(mActivity, mOrientation,mTrackCursor);
                updateNowPlaying(mActivity, mOrientation, mTrackCursor);
            }
        }
    };


    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("hehangjun", "641 mScanListener action:" + (action));
            // String status = Environment.getExternalStorageState();
            if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                songgengxin.selectzongsong();
                songgengxin.selectfenzongsong();
                mMusicApplication.setCount();
   //             Log.i("test", "action_media_scanner_finished====广播==1111111");
                 if (mMusicApplication.getCount() >= 2) {
                     songgengxin.deletefensong();
   //              Log.i("test", "现在应该可以删除了==" + mMusicApplication.getCount());
                 mMusicApplication.setzoer();
                 }
                 songgengxin.update();
            }
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(mActivity);
                mReScanHandler.sendEmptyMessage(0);
//                Log.i("test", "action_media_scanner_started====广播==2222222");
                
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                mReScanHandler.sendEmptyMessageDelayed(0, 1000);
//                Log.i("test", "action_media_unmounted====广播==333333333333");
            } 
			
		  else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
//                Log.i("test", "action_media_mounted====广播==444444444");

                mReScanHandler.sendEmptyMessage(0);

            }
        }
    };
    
    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i("hehangjun", "668 mReScanHandler");
            if (mAdapter != null) {
                //getTrackCursor(mAdapter.getQueryHandler(), null, true);
                mMusicApplication.goDoQuery(PlayFragment.this, mMusicApplication.getWhere().toString());
                
                //init(null);
            }
        }
    };
    
    
    public class ViewHolder {
        //位置
        int position;
        //专辑ID
        long albumId =-1;
        //歌曲ID
        long ID = -1;
        TextView line1;
        TextView line2;
        TextView duration;
        ImageView play_indicator;
        CharArrayBuffer buffer1;
        char [] buffer2;
        ImageView drmLock;
        ImageView editIcon;
    }
    
    //yuan tong qin 歌曲的适配器
   class TrackListAdapter extends SimpleCursorAdapter {

        int mTitleIdx;
        int mArtistIdx;
        int mDurationIdx;
        int mAudioIdIdx;

        private final StringBuilder mBuilder = new StringBuilder();
        private String mUnknownArtist;
        private String mUnknownAlbum;
        
        private AnimationDrawable mAnimPlay = null;
        int mIsDrmIdx = -1;
        int mDrmMethodIdx = -1;
        int mTitlePinyinIdx;

        TrackListAdapter(Context context,int layout, Cursor cursor, String[] from, int[] to) {
            super(context, layout, cursor, from, to);
            getColumnIndices(cursor);
            mUnknownArtist = context.getString(R.string.unknown_artist_name);
            mUnknownAlbum = context.getString(R.string.unknown_album_name);
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
            vh.line2.setTextColor(Color.parseColor("#c6c6c6"));
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
            
            ViewHolder vh = (ViewHolder) view.getTag();
            
            vh.position = cursor.getPosition();
            cursor.copyStringToBuffer(mTitleIdx, vh.buffer1);
            vh.line1.setText(vh.buffer1.data, 0, vh.buffer1.sizeCopied);
            
            int secs = cursor.getInt(mDurationIdx) / 1000;
            if(secs == 0){
                int durationColIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                if (!cursor.isNull(durationColIdx)) {
                    MusicLogUtils.i("hehangjun", "duration from database is " + cursor.getLong(durationColIdx)+"    "+vh.line1.getText().toString());
                }
            }
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
            vh.albumId = album;
            ImageView ivEdit = vh.editIcon;
            //mSyncImageLoader.loadImage(vh.editIcon,vh.ID,album);
            
            mSyncImageLoader.beginloadImage(vh.position,imageLoadListener,vh.ID,album);//这里开始绑定专辑图片
            //Log.i("hehangjun", "-----------这里开始绑定专辑图片-----------");
            vh.editIcon.setImageResource(R.drawable.albumart_mp_unknown_list);
            

            ImageView iv = vh.play_indicator;
            long id = -1;
            if (mService != null) {
                try {
                    id = mService.getAudioId();
                } catch (RemoteException ex) {
                }
            }
            
            //Log.i("hehangjun", "------------bindView-------------vh.ID:"+vh.ID+"   id:"+id+"     vh.line1:"+vh.line1.getText().toString());
            if (vh.ID == id) {
                iv.setVisibility(View.VISIBLE);
                vh.line1.setTextColor(Color.parseColor("#e55b09"));//Color.parseColor("#40afff")
                iv.setBackgroundResource(R.anim.list_nowplaying_ani);
                mAnimPlay = (AnimationDrawable) iv.getBackground();
                try {
                    if (mService.isPlaying()) {
                        mAnimPlay.start();
                    } else {
                        mAnimPlay.stop();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                iv.setVisibility(View.GONE);
                vh.line1.setTextColor(Color.parseColor("#384653"));
            }
        }
        
        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor != mTrackCursor) {
                mTrackCursor = cursor;
                getColumnIndices(cursor);
            }
            super.changeCursor(cursor);
        }
        
        SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener() {

            @Override
            public void onImageLoad(Integer position, Bitmap bitmap) {
                if(bitmap != null){
                    View view = null;
                    Log.i("hehangjun", "回调 onImageLoad position："+position+"    First:"+(mTrackList.getFirstVisiblePosition())+"    Last:"+(mTrackList.getLastVisiblePosition()));
                    if(mTrackList.getFirstVisiblePosition() < position && mTrackList.getLastVisiblePosition() == -1){
                        view = mTrackList.getChildAt(position-mTrackList.getFirstVisiblePosition());
                    }else if(mTrackList.getFirstVisiblePosition() <= position && position <= mTrackList.getLastVisiblePosition()){
                        view = mTrackList.getChildAt(position-mTrackList.getFirstVisiblePosition());
                    }
                    if (view != null) {
                        ImageView iv = (ImageView) view.findViewById(R.id.icon);
                        iv.setImageBitmap(bitmap);
                    }
                }
                
            }

            @Override
            public void onError(Integer position) {
                Log.i("hehangjun", "回调 onError position："+position+"    读取图片失败");
                View view = mTrackList.findViewById(position);
                /*if (view != null) {
                    ImageView iv = (ImageView) view.findViewById(R.id.icon);
                    iv.setImageResource(R.drawable.albumart_mp_unknown_list);
                }*/
            }

        };

    }

    @Override
    public void onServiceConnected(ComponentName arg0, IBinder service) {
        mService = IMediaPlaybackService.Stub.asInterface(service);
        updateNowPlaying(mActivity, mOrientation, mTrackCursor);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mService = null;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mOrientation = newConfig.orientation;
    }

    @Override
    public void onClick(View view) {
        if(view == mDialogDeleteButton){//删除歌曲
            Log.i("hehangjun", "mSelectedId  :"+mSelectedId+"   mCurrentTrackName:"+mCurrentTrackName);
            long [] list = new long[1];
            list[0] = (int) mSelectedId;
            Bundle b = new Bundle();
            b.putInt(MusicUtils.DELETE_DESC_STRING_ID, R.string.delete_song_desc);
            b.putString(MusicUtils.DELETE_DESC_TRACK_INFO, mCurrentTrackName);
            b.putLongArray("items", list);
            Intent intent = new Intent();
            intent.setClass(mActivity, DeleteItems.class);
            intent.putExtras(b);
            startActivityForResult(intent, -1);
            
            if(mPopupWindowOption.isShowing()){
                mPopupWindowOption.dismiss();
            }
            
            
        }else if(view == mDialogSettingRingButton){
            if (mSelectedId != -1) {
                MusicUtils.setRingtone(mActivity,mSelectedId);
            }
            if(mPopupWindowOption.isShowing()){
                mPopupWindowOption.dismiss();
            }
        }else if(view == mDialogAddToButton){//移动到
            mOptionLayout.setVisibility(View.GONE);
            mInventoryListdate =  ((MusicApplication)mActivity.getApplication()).updateInventoryDate();
            if(mInventoryAdapter == null){
                mInventoryAdapter = new InventoryAdapter(mActivity, mInventoryListdate);
                mInventoryAdapter.setSelectedID(mSelectedId);
                mInventoryList.setAdapter(mInventoryAdapter);
            }else{
                mInventoryAdapter.setInventoryDate(mInventoryListdate);
                mInventoryAdapter.setSelectedID(mSelectedId);
                mInventoryAdapter.notifyDataSetChanged();
            }
            mInventoryList.setVisibility(View.VISIBLE);
            mCancelButton.setText(R.string.back);
            
        }else if(view == mCancelButton){
            if(mPopupWindowOption.isShowing() && mOptionLayout.getVisibility() == View.VISIBLE){
                mPopupWindowOption.dismiss();
            }else if(mPopupWindowOption.isShowing() && mOptionLayout.getVisibility() == View.GONE){
                mOptionLayout.setVisibility(View.VISIBLE);
                mInventoryList.setVisibility(View.GONE);
                mCancelButton.setText(R.string.cancel);
            }
        }
        
    }
    
    public class InventoryItem {
        private String inventoryName;
        public InventoryItem(String name) {
            this.inventoryName = name;
        }
        public String getName() {
            return inventoryName;
        }
        public void setName(String name) {
            this.inventoryName = name;
        }
    }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        switch (scrollState) {
        case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
            mSyncImageLoader.lock();
            break;
        case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
            loadImage();
            break;
        case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            mSyncImageLoader.lock();
            break;

        default:
            break;
        }

    }
    public void loadImage() {
        int start = mTrackList.getFirstVisiblePosition();
        int end = mTrackList.getLastVisiblePosition();
        if (end >= mTrackList.getCount()) {
            end = mTrackList.getCount() - 1;
        }
        mSyncImageLoader.setLoadLimit(start, end);
        mSyncImageLoader.unlock();
    }
    
     
     public View getNowPlayingView(Activity activity){
         if(mNowPlayingView == null || mActivity!=activity){
             mNowPlayingView = activity.findViewById(R.id.nowplaying_parent);
             trackNameView = (TextView) mNowPlayingView.findViewById(R.id.trackName);
             artistNameView = (TextView) mNowPlayingView.findViewById(R.id.artistName);
             miniPlayerThumbnail = (ImageView) mNowPlayingView.findViewById(R.id.mini_player_thumbnail);
             preBtn = (ImageView) mNowPlayingView.findViewById(R.id.mini_player_prev_btn);//上一首歌
             playBtn = (ImageView) mNowPlayingView.findViewById(R.id.mini_player_play_pause_btn);
             nextBtn = (ImageView) mNowPlayingView.findViewById(R.id.mini_player_next_btn);
             playBtn.setOnClickListener(mAllListener);
             preBtn.setOnClickListener(mAllListener);
             nextBtn.setOnClickListener(mAllListener);
             miniPlayerThumbnail.setOnClickListener(mAllListener);
         }
         return mNowPlayingView;
     }
     
    View.OnClickListener mAllListener = new View .OnClickListener() {
        @Override
        public void onClick(View view) {
            
            Log.i("hehangjun", "OnClickListener   mTrackCursor:"+(mTrackCursor==null)+"   mService:"+(mService==null));
            
            try {
                if(mService != null && mService.getAudioId() == -1){
                    Log.i("hehangjun", "sService is null   ");
                    MusicUtils.playAll(mActivity, mTrackCursor, 0);
                }
                
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            
            
            try {
                if(view == preBtn){
                    mService.prev();
                }else if(view == nextBtn){
                    mService.next();
                }else if(view == playBtn){
                    Log.i("hehangjun", "updateNowPlaying with view == playBtn: ");
                    try {
                        if (mService.isPlaying()) {
                            mService.pause();
                            playBtn.setImageResource(R.drawable.music_mini_player_play);
                        } else {
                            mService.play();
                            playBtn.setImageResource(R.drawable.music_mini_player_pause);
                        }
                    } catch (RemoteException ex) {
                        Log.i("hehangjun", "updateNowPlaying with RemoteException: " + ex.toString());
                    }
                }else if(view == miniPlayerThumbnail){
                    Context c = view.getContext();
                    c.startActivity(new Intent(c, MediaPlaybackActivity.class));
                }
                
            } catch (RemoteException ex) {
                Log.d("hehangjun", "updateNowPlaying with RemoteException: " + ex);
            }
        }
    };

    private Songgengxin songgengxin;
    
    void updateNowPlaying(Activity activity, int orientation,Cursor cursor) {
        
        if(mNowPlayingView == null || mActivity!=activity){
            getNowPlayingView(activity);
            if(mTrackCursor != null && mTrackCursor.getCount() <= 1){
                preBtn.setEnabled(false);
                nextBtn.setEnabled(false);
            }else{
                preBtn.setEnabled(true);
                nextBtn.setEnabled(true);
            }
        }
        if (mNowPlayingView == null) {
            Log.d("hehangjun", "mNowPlayingView is null start return     ");
            return;
        }
        try {
            if (MusicUtils.sService != null && MusicUtils.sService.getAudioId() != -1) {
                String artistName = MusicUtils.sService.getArtistName();
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = activity.getString(R.string.unknown_artist_name);
                }
                trackNameView.setText(MusicUtils.sService.getTrackName());
                artistNameView.setText(artistName);
                
                long albumid = mService.getAlbumId();
                long songid = mService.getAudioId();
                Bitmap mArtBitmap = MusicUtils.getArtwork(activity,songid, albumid, false);
                if (mArtBitmap == null) {
                    mArtBitmap = MusicUtils.getDefaultArtwork(activity,songid);
                }
                if (mArtBitmap != null) {
                    miniPlayerThumbnail.setImageBitmap(mArtBitmap);
                    miniPlayerThumbnail.getDrawable().setDither(true);
                }
                
                if (!mService.isPlaying()) {
                    playBtn.setImageResource(R.drawable.music_mini_player_play);
                } else {
                    playBtn.setImageResource(R.drawable.music_mini_player_pause);
                }
                final Activity sendBroadActivity = activity;
            }else{
                Log.d("hehangjun", "更新 "+(MusicUtils.sService != null)+"   ");
            }
        } catch (RemoteException ex) {
            Log.d("hehangjun", "1406  Exception: " + ex.toString());
        }
    }

    @Override
    public void onQueryConmplete(Cursor cursor) {
        Log.i("hehangjun", "1098 onQueryConmplete");
        init(cursor);
    }


}
