
package com.cappu.launcherwin;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.tools.DensityUtil;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RingtoneActivity extends BasicActivity implements View.OnClickListener,Runnable{
    
    private String TAG = "RingtoneActivity";
    
    Button i99_cancel;
    Button i99_ok;
    
    int mTextSize = 34;
    //Context TextContext;
    
    ListView ring_tone_list;
    RingToneAdapter mRingToneAdapter;
    private TextView mFootViewCount;
    
    private Ringtone mRingtone;
    
    private RingtoneManager mRingtoneManager;
    private Cursor mCursor;
    private Handler mHandler;
    
    /** M: The position in the list of the 'More Ringtongs' item. */
    private int mMoreRingtonesPos = -1;
    
  /// M: Request codes to MusicPicker for add more ringtone
    private static final int ADD_MORE_RINGTONES = 1;
    
    /** The position in the list of the last clicked item. */
    private int mClickedPos = -1;
    
    private boolean isInit = true;
    
    
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.ringtone_picker);
        
        mTextSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
        
        mRingtoneManager = new RingtoneManager(this);
        mCursor = mRingtoneManager.getCursor();
        mClickedPos = mRingtoneManager.getRingtonePosition(mRingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE));
        init();
    }
    
    private void init() {
        isInit = true;
        ring_tone_list = (ListView) findViewById(R.id.ring_tone_list);
        mHandler = new Handler();
        
        i99_cancel = (Button) findViewById(R.id.i99_cancel);
        i99_ok = (Button) findViewById(R.id.i99_ok);
        
        i99_cancel.setOnClickListener(this);
        i99_ok.setOnClickListener(this);
        
        setFootView();
        
        mRingToneAdapter = new RingToneAdapter(this, mCursor);
        ring_tone_list.setAdapter(mRingToneAdapter);
        ring_tone_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                for (int i = 0; i < adapter.getChildCount(); i++) {
                    View rl = adapter.getChildAt(i);
                    ImageView iv = (ImageView) rl.findViewById(R.id.check);
                    if(iv != null){
                        iv.setImageResource(R.drawable.ring_uncheck);
                    }
                }
                RelativeLayout mrl = (RelativeLayout)view;
                if(mrl!=null){
                    ImageView iv = (ImageView) mrl.findViewById(R.id.check);
                    if(iv!=null){
                        iv.setImageResource(R.drawable.ring_check);
                        mMoreRingtonesPos = position;//position-ring_tone_list.getCheckedItemPosition();
                        playRingtone(position, 0);
                    }
                    
                }
                
                
            }
        });
        
        if(mClickedPos != -1){
            mMoreRingtonesPos = mClickedPos;
            ring_tone_list.setSelection(mClickedPos);
        }
    }
    
    public void setFootView() {
        if (mFootViewCount == null) {
            mFootViewCount = new TextView(this);
            mFootViewCount.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mFootViewCount.setTextSize(27);
            mFootViewCount.setTextColor(Color.parseColor("#000000"));
            mFootViewCount.setGravity(Gravity.CENTER);
            mFootViewCount.setPadding(0, 5, 0, 5);
        }

        mFootViewCount.setText("更多音乐...");
        mFootViewCount.setVisibility(View.VISIBLE);
        ring_tone_list.addFooterView(mFootViewCount);
        mFootViewCount.setOnClickListener(this);
        
        Log.i(TAG, "ring_tone_list:"+ring_tone_list.getFooterViewsCount());
    }
    

    class RingToneAdapter extends CursorAdapter{
        public final LayoutInflater mInflater;
        public final Context mContext;
        public final int mLabelIndex;

        public RingToneAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mLabelIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
        }
        
        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView text = (TextView) view.findViewById(R.id.song_name);
            
            String ringName = cursor.getString(mLabelIndex);
            Log.i(TAG, "ringName:"+ringName);
            text.setText(ringName);
            if(cursor.getPosition() == mClickedPos && isInit){
                RelativeLayout mrl = (RelativeLayout)view;
                ImageView iv = (ImageView) mrl.findViewById(R.id.check);
                iv.setImageResource(R.drawable.ring_check);
            }else if(mMoreRingtonesPos ==cursor.getPosition()){
                RelativeLayout mrl = (RelativeLayout)view;
                ImageView iv = (ImageView) mrl.findViewById(R.id.check);
                iv.setImageResource(R.drawable.ring_check);
            }else {
                RelativeLayout mrl = (RelativeLayout)view;
                ImageView iv = (ImageView) mrl.findViewById(R.id.check);
                iv.setImageResource(R.drawable.ring_uncheck);
            }
            isInit = false;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.ring_tone_item, parent, false);
        }

    };
    
    @Override
    protected void onPause() {
        isInit = false;
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mRingtone != null && mRingtone.isPlaying()) {
            mRingtone.stop();
        }
    }
    
    
    @Override
    public void onClick(View v) {
        if(v == mCancel || v == i99_cancel){
            finish();
        }else if(v == i99_ok){
            if(mMoreRingtonesPos == -1){
                return;
            }
            try {
                // 得到我们选择的铃声
                Uri pickedUri = mRingtoneManager.getRingtoneUri(mMoreRingtonesPos);
                // 将我们选择的铃声设置成为默认
                if (pickedUri != null) {
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, pickedUri);
                }
            } catch (Exception e) {
                finish();
            }
            finish();
        }else if(v == mFootViewCount){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            
            //加下面两行是android L版本有多个选择
            intent.setPackage("com.cappu.music");
            intent.setClassName("com.cappu.music", "com.cappu.music.MusicPicker");
            intent.setType("audio/*");
            intent.setType("application/ogg");
            intent.setType("application/x-ogg");
            startActivityForResult(intent, ADD_MORE_RINGTONES);
        }
    }
    /// M: Add to handle user choose a ringtone from MusicPicker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case ADD_MORE_RINGTONES:
                if (resultCode == RESULT_OK) {
                    Uri uri = (null == intent ? null : intent.getData());
                    if (uri != null ) {
                        
                        setRingtone(this.getContentResolver(), uri);
                        
                        //Log.v(TAG, "onActivityResult: RESULT_OK, so set to be ringtone! " + uri+"    mClickedPos:"+mClickedPos+"    ring_tone_list:"+ring_tone_list.getCount());
                    }
                } else {
                    Log.v(TAG, "onActivityResult: Cancel to choose more ringtones, so do nothing!");
                }
                break;
        }
    }
    
    private void playRingtone(int position, int delayMs) {
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, delayMs);
    }

    @Override
    public void run() {
        mRingtone = mRingtoneManager.getRingtone(mMoreRingtonesPos);
        if (mRingtone != null) {
            mRingtone.play();
        }
    }
    
    
    
    @Override
    protected void onDestroy() {
        if (mRingtone != null) {
            mRingtone.stop();
        }
        super.onDestroy();
    }
    
    /**
     * M: Set the given uri to be ringtone
     * 
     * @param resolver content resolver
     * @param uri the given uri to set to be ringtones
     */
    private void setRingtone(ContentResolver resolver, Uri uri) {
        // / Set the flag in the database to mark this as a ringtone
        try {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
//            if ((RingtoneManager.TYPE_RINGTONE == mType) || (RingtoneManager.TYPE_VIDEO_CALL == mType) || (RingtoneManager.TYPE_SIP_CALL == mType)) {
//                values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
//            } else if (RingtoneManager.TYPE_ALARM == mType) {
//                values.put(MediaStore.Audio.Media.IS_ALARM, "1");
//            } else if (RingtoneManager.TYPE_NOTIFICATION == mType) {
//                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, "1");
//            } else {
//                return;
//            }
            resolver.update(uri, values, null, null);
            
            mClickedPos = mRingtoneManager.getRingtonePosition(uri);
            if(mClickedPos != -1){
                mMoreRingtonesPos = mClickedPos;
            }
            
            mCursor = mRingtoneManager.getCursor();
            //mCursor = mRingtoneManager.getNewCursor();
            mRingToneAdapter.changeCursor(mCursor);
            mRingToneAdapter.notifyDataSetChanged();
            
            
            if(mClickedPos != -1){
                ring_tone_list.setSelection(mClickedPos);
            }
            
            
          
        } catch (UnsupportedOperationException ex) {
            // / most likely the card just got unmounted
            Log.e(TAG, "couldn't set ringtone flag for uri " + uri);
        }
    }
    

}
