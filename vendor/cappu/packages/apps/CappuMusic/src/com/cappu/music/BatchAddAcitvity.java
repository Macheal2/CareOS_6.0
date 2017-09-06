package com.cappu.music;

import java.util.ArrayList;
import java.util.List;

import com.cappu.music.MusicApplication.QueryCompleteListener;
import com.cappu.music.MusicUtils.ServiceToken;
import com.cappu.music.database.MusicProvider;
import com.cappu.music.widget.EasingType;
import com.cappu.music.widget.ElasticInterpolator;
import com.cappu.music.widget.LoadingDialog;
import com.cappu.music.widget.Panel;
import com.cappu.music.widget.Panel.OnPanelListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.graphics.drawable.ColorDrawable;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**这个是添加歌曲界面*/
public class BatchAddAcitvity extends Activity implements OnClickListener,OnItemClickListener,QueryCompleteListener,OnCheckedChangeListener,OnPanelListener{

    private long InventoryId;
    private String InventoryName;
    MusicInventory mMusicInventory;
    private TextView mInventoryTitleView;
    //private ImageView mInventoryBack;
    
    private CheckBox mAllSelect;
    boolean isItemCheck = false;
    
    private ListView mListView;
    private MusicApplication mMusicApplication;
    private TrackListAdapter mTrackListAdapter;
    
    private List<Long> mSongListID;
    private List<String> mystitle;// add ytq tianjiaziduan
    private List<String> mArtist;//add ytq tianjiayishujia
    Cursor mAllSongCursor;
    
    Panel mOptionLayout;
    
    
    TextView mMoveTo;
    TextView mDelete;
    TextView mAdd;
    
    private LoadingDialog mLoadingDialog;
    
    
    private PopupWindow mPopupWindowOption;
    private ColorDrawable mColorDrawable = new ColorDrawable(0xb0000000);
    private View mOptionViewDialogView;
    private TextView mDialogTitle;
    private TextView mDialogSettingRingButton;
    private LayoutInflater mInflater;
    private TextView mCancelButton;//yuan tong qin add textview
    private ListView mInventoryList;
    private List<MusicInventory> mInventoryListdate;
    private InventoryAdapter mInventoryAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_batch_edit);
        InventoryId = getIntent().getLongExtra("InventoryId", -1);
        InventoryName = getIntent().getStringExtra("InventoryName");
        if(mInflater == null){
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mLoadingDialog = new LoadingDialog(BatchAddAcitvity.this);
        mSongListID = new ArrayList<Long>();
        mystitle=new ArrayList<String>();
        mArtist=new ArrayList<String>();
        mMusicInventory = initInventory();
        mMusicApplication = (MusicApplication) getApplication();
        initView();
        getSongList();
        
    }
    public void initView(){
        mInventoryTitleView = (TextView) findViewById(R.id.inventory_batch_title);
        mInventoryTitleView.setText(R.string.batch_all);
        //mInventoryBack = (ImageView) findViewById(R.id.inventory_batch_back);
        mAllSelect = (CheckBox) findViewById(R.id.all_select);
        mAllSelect.setOnCheckedChangeListener(this);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        mInventoryTitleView.setOnClickListener(this);
        
        mInventoryTitleView.setText("    "+getString(R.string.add_song)+"->"+InventoryName);
        
        mOptionLayout = (Panel) findViewById(R.id.option_layout);
        mOptionLayout.setOnPanelListener(this);
        mOptionLayout.setInterpolator(new ElasticInterpolator(EasingType.OUT, 1.0f, 0.3f));
        mDelete = (TextView) findViewById(R.id.delete_button);
        mMoveTo = (TextView) findViewById(R.id.add_to_button);
        
        mDelete.setOnClickListener(this);
        mMoveTo.setOnClickListener(this);
        
        mDelete.setVisibility(View.INVISIBLE);
        mMoveTo.setVisibility(View.INVISIBLE);
        mAdd = (TextView) findViewById(R.id.add_button);
        mAdd.setVisibility(View.VISIBLE);
        mAdd.setOnClickListener(this);
        initPopupWindow();
    }
    
    private void initPopupWindow(){
        mOptionViewDialogView = mInflater.inflate(R.layout.playlist_option_layout, null);
        mOptionViewDialogView.findViewById(R.id.option_layout).setVisibility(View.GONE);
        mDialogTitle = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_title);
        //mDialogAddToButton = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_add_to_button);
        mDialogSettingRingButton = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_setting_ring_button);
        //mDialogDeleteButton = (TextView) mOptionViewDialogView.findViewById(R.id.dialog_delete_button);
        mInventoryList = (ListView) mOptionViewDialogView.findViewById(R.id.inventory_list);
        mCancelButton = (TextView) mOptionViewDialogView.findViewById(R.id.cancel_button);
        //mDialogAddToButton.setOnClickListener(this);
        mDialogSettingRingButton.setOnClickListener(this);
        //mDialogDeleteButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        
        
        mInventoryList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                InventoryAdapter.InventoryViewHolder inVentory = (InventoryAdapter.InventoryViewHolder) view.getTag();
                for (int i = 0; i < mSongListID.size(); i++) {
                    Cursor c = getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ inVentory.mInventoryId +"' and songId ='"+mSongListID.get(i)+"'", null, null);
                    long ID = -1;
                    try {
                        final int IdIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
                        final int inventoryIdIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ID);
                        final int songIdIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_ID);
                        while (c.moveToNext()) {
                            ID = c.getLong(IdIndex);
                            //inventoryId = c.getLong(inventoryIdIndex);
                            //songId = c.getLong(songIdIndex);
                        }
                    } catch (Exception e) {
                    }finally{
                        c.close();
                    }
                    int index = getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, "songInventoryId = '"+ InventoryId +"' and songId ='"+mSongListID.get(i)+"'", null);
                    if(c!=null && c.getCount()>0){
                        Log.i("HHJ", "说明"+mSongListID.get(i)+"这首歌存在   "+inVentory.nName.getText().toString()+" 歌单里面   那么这里就该将删除"+"   index:"+index);
                    }else {
                        ContentValues values = new ContentValues();
                        values.put("songId", mSongListID.get(i));
                        values.put("songInventoryId", inVentory.mInventoryId);
                        getContentResolver().insert(MusicProvider.BaseMusicColumns.SONG_URI, values);
                        //getContentResolver().update(MusicProvider.BaseMusicUri.getSongUri(InventoryId), values, null, null);
                        
                        Log.i("HHJ", "说明"+mSongListID.get(i)+"这首歌 不在   "+inVentory.nName.getText().toString()+" 歌单里面  歌单ID "+inVentory.mInventoryId +"  当前歌单ID:"+InventoryId+"  歌单名:"+InventoryName+"     index:"+index);
                    }
                }
                getSongList();
                if(mPopupWindowOption.isShowing()){
                    mPopupWindowOption.dismiss();
                }
                
                
            }
        });
        
        mPopupWindowOption = new PopupWindow(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mPopupWindowOption.setBackgroundDrawable(mColorDrawable);
        mPopupWindowOption.setFocusable(true);
        mPopupWindowOption.setAnimationStyle(R.style.popdialogshow);
        mPopupWindowOption.update();

        
        mPopupWindowOption.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        
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
    
    
    @SuppressLint("NewApi")
    public void getSongList(){
        Cursor cursor = getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, "songInventoryId = '"+ InventoryId +"'", null, null);
        /*if(cursor!=null && cursor.getCount() == 0){
            mListView.setVisibility(View.INVISIBLE);
            return;
        }*/
        StringBuilder mWhere = new StringBuilder();
        if(cursor != null && cursor.getCount()>0){
            mWhere.append(" AND (");
        }
        try {
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
                //Log.i("HHJ", "getSongList cursor ID:"+ID+"   inventoryId:"+inventoryId+"    songId: "+songId);
                
                try {
                    mWhere.append(" "+MediaStore.Audio.Media._ID + " != '"+songId+"' ");
                    /*mWhere.append(" "+MediaStore.Audio.Playlists.Members.AUDIO_ID + " = '"+songId+"' ");*/
                    if(!cursor.isLast()){
                        mWhere.append(" AND ");
                    }
                } catch (IllegalArgumentException ex) {
                    mWhere.append(MediaStore.Audio.Media._ID + "="+songId);
                }
            }
            if(cursor != null && cursor.getCount()>0){
                mWhere.append(" ) ");
            }
            
            //Log.i("HHJ", "mWhere:"+mWhere.toString());
            //Log.i("HHJ", "----------------------------------------------------------------");
        } catch (Exception e) {
        }finally{
            if(cursor != null){
                cursor.close();
            }
        }
        StringBuilder select  = mMusicApplication.getWhere();
        select.append(mWhere.toString());
        
        mMusicApplication.goDoQuery(this,select.toString());
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

    class TrackListAdapter extends SimpleCursorAdapter implements SectionIndexer {
        boolean mIsNowPlaying;
        boolean mDisableNowPlayingIndicator;

        int mTitleIdx;
        int mArtistIdx;
        int mDurationIdx;
        int mAudioIdIdx;
        int mystitleid;

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
        int mTitlePinyinIdx;

        public class ViewHolder {
            long ID = -1;
            String stitle;
            String sartist;
            /**歌曲名*/
            TextView line1;
            TextView line2;
            CheckBox itemSelect;
            CharArrayBuffer buffer1;
            char [] buffer2;
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
                	mystitleid=cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.TITLE);
                    mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                } catch (IllegalArgumentException ex) {
                    mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    mystitleid=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
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
            vh.itemSelect = (CheckBox) v.findViewById(R.id.item_select);
            vh.itemSelect.setFocusable(false);
            vh.line2.setTextColor(Color.parseColor("#c6c6c6"));
            vh.buffer1 = new CharArrayBuffer(100);
            vh.buffer2 = new char[200];
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
            final StringBuilder builder = mBuilder;
            builder.delete(0, builder.length());

            vh.ID = cursor.getLong(mAudioIdIdx);
            vh.stitle=cursor.getString(mystitleid);
            String name = cursor.getString(mArtistIdx);
            vh.sartist=name;
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
            
            if(mAllSelect.isChecked() || mSongListID.contains(vh.ID)){
                vh.itemSelect.setChecked(true);
            }else{
                vh.itemSelect.setChecked(false);
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
//        Log.i("HHJ", " 回调函数 cursor:"+cursor.getCount());
        mAllSongCursor =cursor;
        if(mTrackListAdapter == null){
            mTrackListAdapter = new TrackListAdapter(this,R.layout.track_list_item_edit,cursor, new String[] {},  new int[] {}, false,false);
            mListView.setAdapter(mTrackListAdapter);
            mListView.setOnItemClickListener(this);
        }else{
            mTrackListAdapter.changeCursor(cursor);
            mTrackListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        if(view == mInventoryTitleView){
            this.finish();
        }else if(view == mDelete){
            int count = 0;
            for (int i = 0; i < mSongListID.size(); i++) {
                int index = getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, " songInventoryId = '"+InventoryId +"' AND songId = '"+ mSongListID.get(i)+"' ", null);
                if(index>0){
                    count +=1; 
                }
            }
            
            KookToast.toast(this, getString(R.string.delete_inventory_song,InventoryName,count), "s", Color.parseColor("#FFFFFF"));
            if(count>0){
                getSongList();
                //mAllSelect.setClickable(false);
                mAllSelect.setChecked(false);
            }
            
        }else if(view == mMoveTo){
            mInventoryListdate =  mMusicApplication.getInventoryData(true);
            if(mInventoryAdapter == null){
                mInventoryAdapter = new InventoryAdapter(this, mInventoryListdate);
                mInventoryList.setAdapter(mInventoryAdapter);
            }else{
                mInventoryAdapter.setInventoryDate(mInventoryListdate);
            }
            mInventoryAdapter.notifyDataSetChanged();
            
            mPopupWindowOption.setContentView(mOptionViewDialogView);
            mDialogTitle.setText(getString(R.string.move_song_to_inventory, mSongListID.size()));
            mPopupWindowOption.showAtLocation(findViewById(R.id.option_layout), Gravity.BOTTOM, 0, 0);
        }else if(view == mCancelButton){
            if(mPopupWindowOption.isShowing()){
                mPopupWindowOption.dismiss();
            }
        }else if(view == mAdd){
        	
        	new MusicaddTask().execute();//添加歌曲
        	//yuan tong qin add 
            //Log.i("HHJ", " view == mAdd添加:"+mSongListID.size()+" 首歌 ");
//            for (int i = 0; i < mSongListID.size(); i++) {
//                ContentValues values = new ContentValues();
//                long songId = mSongListID.get(i);
//                
//              String stitlea=mystitle.get(i);
//              Log.i("ytq", stitlea+"----添加歌曲的id=============="+songId);
//                values.put("songId", songId);
//                values.put("songInventoryId", InventoryId);
//                values.put("title", stitlea);
//                getContentResolver().insert(MusicProvider.BaseMusicColumns.SONG_URI, values);
//                //mSongListID.remove(songId);
//                //Log.i("HHJ", "添加   songId:"+songId+"      i:"+i);
//            }
//            mSongListID.clear();
//            mystitle.clear();
//            mArtist.clear();
            //dialogHandler.sendEmptyMessageAtTime(0, 100*mSongListID.size());
//            Cursor query = getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, " songInventoryId = "+InventoryId, null, null);
//            if(query!=null){
//            	  Log.i("test",query.getCount()+"==添加之后的长度=");
//            }else{
//            	  Log.i("test","没有长度==添加之后的长度");
//            }
//            //getSongList();
//            if(mPopupWindowOption.isShowing()){
//                mPopupWindowOption.dismiss();
//            }
//            setOptionLayoutType();
//            Message message = new Message();
//            message.what = 0;
//            dialogHandler.sendMessageDelayed(message, 50);
//            dialogHandler.sendMessage(message);
//            startActivity(new Intent(BatchAddAcitvity.this,InventoryFragmentAcitvity.class));
        }
    }
    
    //数据添加
    private class MusicaddTask extends AsyncTask{

		@Override
		protected Object doInBackground(Object... params) {
			
			  for (int i = 0; i < mSongListID.size(); i++) {
	                ContentValues values = new ContentValues();
	                long songId = mSongListID.get(i);
	                
	              String stitlea=mystitle.get(i);
	              Log.i("ytq", stitlea+"----添加歌曲的id=============="+songId);
	                values.put("songId", songId);
	                values.put("songInventoryId", InventoryId);
	                values.put("title", stitlea);
	                getContentResolver().insert(MusicProvider.BaseMusicColumns.SONG_URI, values);
	            }
	            mSongListID.clear();
	            mystitle.clear();
	            mArtist.clear();
			
			return null;
		}
	
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);

	            //getSongList();
	            if(mPopupWindowOption.isShowing()){
	                mPopupWindowOption.dismiss();
	            }
	           setOptionLayoutType();
			   //yuan tong qin add 
//	           Message message = new Message();
//	            message.what = 0;
//	            dialogHandler.sendMessageDelayed(message, 50);
//	            dialogHandler.sendMessage(message);
			 startActivity(new Intent(BatchAddAcitvity.this,InventoryFragmentAcitvity.class));
		}
    	
    }
    
    private Handler dialogHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i("HHJ", " --------------------- 刷新一次  -----------------------       ");
            getSongList();
        }
    };
    
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean change) {
        if(mTrackListAdapter == null){
            return;
        }
        if(isItemCheck){
            isItemCheck = false;
            return;
        }
        
        if(compoundButton==mAllSelect){
            if (change) {

                try {
                    final int IdIndex = mAllSongCursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
                    final int stitleid=mAllSongCursor.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_TITLE);
                    final int Iartist=mAllSongCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                    /* int index = 0;
                    while (mAllSongCursor.moveToFirst() || mAllSongCursor.moveToNext()) {
                        Log.i("HHJ", " 当前执行到"+index+"  位置");
                        long ID =mAllSongCursor.getLong(IdIndex);
                        if(!mSongListID.contains(ID)){
                            boolean add = mSongListID.add(ID);
                            //Log.i("HHJ", " 添加 ID:"+ID+"  "+add);
                        }
                        index +=1;x
                    }*/
                    for (int i = 0; i < mAllSongCursor.getCount(); i++) {
                        if(mAllSongCursor.moveToPosition(i)){
                            long ID =mAllSongCursor.getLong(IdIndex);
                            String stitle=mAllSongCursor.getString(stitleid);
                            String sartist=mAllSongCursor.getString(Iartist);
                            Log.i("ytq", "没有东西==="+stitle);
                            if(!mSongListID.contains(ID)){
                                boolean add = mSongListID.add(ID);
                                Log.i("ytq","有没有stitle=="+stitle);
                                mystitle.add(stitle);
                                mArtist.add(sartist);
                                //Log.i("HHJ", " 添加 ID:"+ID+"  "+add);
                            }
                        }
                    }
                    Log.i("HHJ", " 添加:"+mAllSongCursor.getCount()+" 首歌 "+"     mSongListID:"+mSongListID.size());
                } catch (Exception e) {
                    Log.i("HHJ", " 动态改变 ID:"+e.toString());
                }
                mTrackListAdapter.notifyDataSetChanged();
            }else{
                mSongListID.clear();
                mystitle.clear();
                mArtist.clear();
                mTrackListAdapter.notifyDataSetChanged();
            }
            
            setOptionLayoutType();
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        if(mAllSongCursor != null){
            com.cappu.music.BatchAddAcitvity.TrackListAdapter.ViewHolder viewHolder = (com.cappu.music.BatchAddAcitvity.TrackListAdapter.ViewHolder)view.getTag();
            if(viewHolder.itemSelect.isChecked()){
                viewHolder.itemSelect.setChecked(false);
                mSongListID.remove(viewHolder.ID);
            
                mystitle.remove(viewHolder.stitle);//ytq
                mArtist.remove(viewHolder.sartist);
            }else {
                viewHolder.itemSelect.setChecked(true);
                boolean fage=true;
                for (int i = 0; i < mystitle.size(); i++) {
                    if((mystitle.get(i)+"").equals(viewHolder.stitle)&&(mArtist.get(i)+"").equals(viewHolder.sartist))
                    {
                        fage=false;
                        break;
                    }
                }
                if(fage)
                {
                    mSongListID.add(viewHolder.ID);
                    mystitle.add(viewHolder.stitle);
                    mArtist.add(viewHolder.sartist);//ytq
                }
               
                Log.i("test", viewHolder.sartist+"==是不是添加=="+viewHolder.ID+"==名字=="+viewHolder.stitle);
		    }
            //Log.i("HHJ", "mSongListID.size():"+mSongListID.size()+"        "+mTrackListAdapter.getCount());
            if(mSongListID.size() == mTrackListAdapter.getCount()){
                mAllSelect.setChecked(true);
            }else{
                if(mAllSelect.isChecked()){
                    isItemCheck = true;
                    mAllSelect.setChecked(false);
                }
                
            }
            setOptionLayoutType();
        }
    }
    
    public void setOptionLayoutType(){
        
        Log.i("HHJ", "setOptionLayoutType  mSongListID.size():"+mSongListID.size());
        if(mSongListID.size()>0){
            mOptionLayout.setOpen(true, false);
        }else{
            mOptionLayout.setOpen(false, false);
        }
    }
    @Override
    public void onPanelClosed(Panel panel) {
        
    }
    @Override
    public void onPanelOpened(Panel panel) {
        
    }

}
