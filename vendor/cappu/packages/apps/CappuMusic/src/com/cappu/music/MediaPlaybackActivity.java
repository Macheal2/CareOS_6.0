package com.cappu.music;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SectionIndexer;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.music.MusicUtils.ServiceToken;
import com.cappu.music.mediatek.IVoiceCommandManager;
import com.cappu.music.mediatek.MusicFeatureOption;
import com.cappu.music.mediatek.VoiceCommandListener;
/*import com.mediatek.drm.OmaDrmStore;
import com.mediatek.drm.OmaDrmClient;
import com.mediatek.drm.OmaDrmUiUtils;*/

import java.util.Arrays;

/**播放详情界面*/
@SuppressLint("NewApi")
public class MediaPlaybackActivity extends Activity implements MusicUtils.Defs,View.OnTouchListener, View.OnLongClickListener, CreateBeamUrisCallback{
    private static final String TAG = "MediaPlayback";

    private static final int USE_AS_RINGTONE = CHILD_MENU_BASE;

    private boolean mSeeking = false;
    private boolean mDeviceHasDpad;
    private long mStartSeekPos = 0;
    private long mLastSeekEventTime;
    private IMediaPlaybackService mService = null;
    private RepeatingImageButton mPrevButton;
    private ImageButton mPauseButton;
    private RepeatingImageButton mNextButton;
    private ImageView mRepeatButton;
    /*private ImageButton mShuffleButton;*/
    private ImageButton mQueueButton;
    private Worker mAlbumArtWorker;
    private AlbumArtHandler mAlbumArtHandler;
    private Toast mToast;
    private int mTouchSlop;
    private ServiceToken mToken;

    /// M: specific performace test case.
    private static final String PLAY_TEST = "play song";
    private static final String NEXT_TEST = "next song";
    private static final String PREV_TEST = "prev song";

    /// M: FM Tx package and activity information.
    private static final String FM_TX_PACKAGE = "com.mediatek.FMTransmitter";
    private static final String FM_TX_ACTIVITY = FM_TX_PACKAGE + ".FMTransmitterActivity";

    /// M: The command id of voice command ui.
    private static final int VOICE_COMMAND_PLAY = 13;
    private static final int VOICE_COMMAND_PAUSE = 12;
    private static final int VOICE_COMMAND_NEXT = 7;
    private static final int VOICE_COMMAND_PREV = 8;
    private static final int VOICE_COMMAND_SHFFLE = 9;
    private static final int VOICE_COMMAND_HIGHER = 11;
    private static final int VOICE_COMMAND_LOWER = 10;
    
    private static final int VOICE_COMMAND_INDICATOR = 110;
    /// M: show album art again when configuration change
    private boolean mIsShowAlbumArt = false;
    private Bitmap mArtBitmap = null;
    private long mArtSongId = -1;

    /// M: Add queue, repeat and shuffle to action bar when in landscape
    private boolean mIsLandscape;
    /*private MenuItem mQueueMenuItem;
    private MenuItem mRepeatMenuItem;
    private MenuItem mShuffleMenuItem;*/
    /// M: Add search button in actionbar when nowplaying not exist
    MenuItem mSearchItem;

    /// M: Add playlist sub menu to music
    /*private SubMenu mAddToPlaylistSubmenu;*/

    /// M: Music performance test string which is current runing
    private String mPerformanceTestString = null;

    /// M: use to make current playing time aways showing when seeking
    private int mRepeatCount = -1;

    /// M: Some music's durations can only be obtained when playing the media.
    // As a result we must know whether to update the durations.
    private boolean mNeedUpdateDuration = true;

    /// M: aviod Navigation button respond JE if Activity is background
    private boolean mIsInBackgroud = false;

    private boolean mIsCallOnStop = false;

    private NotificationManager mNotificationManager;
    
    private  AudioManager mAudioManager;
    NfcAdapter mNfcAdapter;

    private TextView mCurrentPlayIndex;
    private ImageView mBackListView;
    private final static int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private AudioManager audioMgr = null;
    private ListView mPlayList;
    private View mListParent;
    private Cursor mTrackCursor;
    private ViewGroup mContainer;
    private TextView mListHeadRight;
    private TextView mListHeadLeft;
    private boolean mRunAnim = false;
    private String mSortOrder;
    private int mCurTrackPos = -1;
    //private OmaDrmClient mDrmClient = null;
    private TrackListAdapter mAdapter = null;
    private String[] mCursorCols = new String[] {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            /*MediaStore.Audio.Media.IS_DRM,
            MediaStore.Audio.Media.DRM_METHOD,
            MediaStore.Audio.Media.TITLE_PINYIN_KEY*/
    };
    public MediaPlaybackActivity() {
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mAlbumArtWorker = new Worker("album art worker");
        mAlbumArtHandler = new AlbumArtHandler(mAlbumArtWorker.getLooper());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mIsLandscape = (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
        updateUI();
        registerVoiceUiListener();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (mNfcAdapter == null) {
            MusicLogUtils.e(TAG, "NFC not available!");
            return;
        }
    }
    /**
     * M:execute the task which the voice command.
     * @param commandId
     */
    public void voiceUiCommand(int commandId) {
        try {
            switch (commandId) {
                case VOICE_COMMAND_PLAY :
                    if (!mService.isPlaying()) {
                        doPauseResume();
                    }
                    break;
                case VOICE_COMMAND_PAUSE :
                    if (mService.isPlaying()) {
                        doPauseResume();
                    }
                    break;
                case VOICE_COMMAND_NEXT :
                    Message msgnext = mHandler.obtainMessage(NEXT_BUTTON, null);
                    mHandler.removeMessages(NEXT_BUTTON);
                    mHandler.sendMessage(msgnext);
                    break;
                case VOICE_COMMAND_PREV :
                    Message msgprev = mHandler.obtainMessage(PREV_BUTTON, null);
                    mHandler.removeMessages(PREV_BUTTON);
                    mHandler.sendMessage(msgprev);
                    break;
                case VOICE_COMMAND_SHFFLE :
                    int shuffle = mService.getShuffleMode();
                    if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                        mService.next();
                    } else {
                        MusicUtils.togglePartyShuffle();
                        setShuffleButtonImage();
                    }
                    break;
                case VOICE_COMMAND_LOWER :
                    int lOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    lOldVolume = lOldVolume > 0 ? (lOldVolume - 1) : 0;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lOldVolume, AudioManager.FLAG_SHOW_UI);
                    break;
                case VOICE_COMMAND_HIGHER :
                    int hOldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int maxMusicVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    hOldVolume = (hOldVolume < maxMusicVolume) ? (hOldVolume + 1) : maxMusicVolume;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, hOldVolume, AudioManager.FLAG_SHOW_UI);
                default :
                    break;
            }
        } catch (RemoteException e) {
            MusicLogUtils.e(TAG, "RemoteException:" + e);
        }
    }
    
    int mInitialX = -1;
    int mLastX = -1;
    int mTextWidth = 0;
    int mViewWidth = 0;
    boolean mDraggingLabel = false;
    
    TextView textViewForContainer(View v) {
        View vv;/* = v.findViewById(R.id.artistname);
        if (vv != null)
            return (TextView) vv;*/
        vv = v.findViewById(R.id.albumname);
        if (vv != null)
            return (TextView) vv;
        vv = v.findViewById(R.id.trackname);
        if (vv != null)
            return (TextView) vv;
        return null;
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        TextView tv = textViewForContainer(v);
        if (tv == null) {
            return false;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            /// M: For ICS style and support for theme manager
            v.setBackgroundColor(getBackgroundColor());
            mInitialX = mLastX = (int) event.getX();
            mDraggingLabel = false;
            /// M: Because only when the text has ellipzised we need scroll the text view to show ellipsis
            /// text to user, We should get the non-ellipsized text width to determine whether need to scroll
            /// the text view. {@
            mTextWidth = (int)tv.getPaint().measureText(tv.getText().toString());
            mViewWidth = tv.getWidth();
            /// @}

            /// M: when text width large than view width, we need turn off ellipsize to show total text.
            if (mTextWidth > mViewWidth) {
                tv.setEllipsize(null);
            }
        } else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundColor(0);
            if (mDraggingLabel) {
                Message msg = mLabelScroller.obtainMessage(0, tv);
                mLabelScroller.sendMessageDelayed(msg, 1000);
            }
            /// M: When touch finished, turn on ellipsize.
            tv.setEllipsize(TruncateAt.END);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mDraggingLabel) {
                int scrollx = tv.getScrollX();
                int x = (int) event.getX();
                int delta = mLastX - x;
                if (delta != 0) {
                    mLastX = x;
                    scrollx += delta;
                    if (scrollx > mTextWidth) {
                        // scrolled the text completely off the view to the left
                        scrollx -= mTextWidth;
                        scrollx -= mViewWidth;
                    }
                    if (scrollx < -mViewWidth) {
                        // scrolled the text completely off the view to the right
                        scrollx += mViewWidth;
                        scrollx += mTextWidth;
                    }
                    tv.scrollTo(scrollx, 0);
                }
                return true;
            }
            int delta = mInitialX - (int) event.getX();
            if (Math.abs(delta) > mTouchSlop) {
                mLabelScroller.removeMessages(0, tv);
                
                if (mViewWidth > mTextWidth) {
                    // tv.setEllipsize(TruncateAt.END);
                    v.cancelLongPress();
                    return false;
                }
                
                mDraggingLabel = true;
                tv.setHorizontalFadingEdgeEnabled(true);
                v.cancelLongPress();
                return true;
            }
        }
        return false; 
    }

    Handler mLabelScroller = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView tv = (TextView) msg.obj;
            int x = tv.getScrollX();
            x = x * 3 / 4;
            tv.scrollTo(x, 0);
            if (x == 0) {
                tv.setEllipsize(TruncateAt.END);
            } else {
                Message newmsg = obtainMessage(0, tv);
                mLabelScroller.sendMessageDelayed(newmsg, 15);
            }
        }
    };
    
    public boolean onLongClick(View view) {

        CharSequence title = null;
        String mime = null;
        String query = null;
        String artist;
        String album;
        String song;
        long audioid;
        
        try {
            artist = mService.getArtistName();
            album = mService.getAlbumName();
            song = mService.getTrackName();
            audioid = mService.getAudioId();
        } catch (RemoteException ex) {
            return true;
        } catch (NullPointerException ex) {
            // we might not actually have the service yet
            return true;
        }

        if (MediaStore.UNKNOWN_STRING.equals(album) &&
                MediaStore.UNKNOWN_STRING.equals(artist) &&
                song != null &&
                song.startsWith("recording")) {
            // not music
            return false;
        }

        if (audioid < 0) {
            return false;
        }

        Cursor c = MusicUtils.query(this,
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioid),
                new String[] {MediaStore.Audio.Media.IS_MUSIC}, null, null, null);
        boolean ismusic = true;
        if (c != null) {
            if (c.moveToFirst()) {
                ismusic = c.getInt(0) != 0;
            }
            c.close();
        }
        if (!ismusic) {
            return false;
        }

        boolean knownartist =
            (artist != null) && !MediaStore.UNKNOWN_STRING.equals(artist);

        boolean knownalbum =
            (album != null) && !MediaStore.UNKNOWN_STRING.equals(album);
        
        if (knownartist /*&& view.equals(mArtistName.getParent())*/) {
            title = artist;
            query = artist;
            mime = MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE;
        } else if (knownalbum && view.equals(mAlbumName.getParent())) {
            title = album;
            if (knownartist) {
                query = artist + " " + album;
            } else {
                query = album;
            }
            mime = MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE;
        } else if (view.equals(mTrackName.getParent()) || !knownartist || !knownalbum) {
            if ((song == null) || MediaStore.UNKNOWN_STRING.equals(song)) {
                // A popup of the form "Search for null/'' using ..." is pretty
                // unhelpful, plus, we won't find any way to buy it anyway.
                return true;
            }

            title = song;
            if (knownartist) {
                query = artist + " " + song;
            } else {
                query = song;
            }
            mime = "audio/*"; // the specific type doesn't matter, so don't bother retrieving it
        } else {
            throw new RuntimeException("shouldn't be here");
        }
        title = getString(R.string.mediasearch, title);

        Intent i = new Intent();
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
        i.putExtra(SearchManager.QUERY, query);
        if(knownartist) {
            i.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
        }
        if(knownalbum) {
            i.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album);
        }
        i.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song);
        i.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, mime);

        startActivity(Intent.createChooser(i, title));
        return true;
    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mFromTouch = true;
        }
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser || (mService == null)) return;
            if (!mFromTouch) {
                mPosOverride = mDuration * progress / 1000;
                try {
                    mService.seek(mPosOverride);
                } catch (RemoteException ex) {
                    MusicLogUtils.e(TAG, "Error:" + ex);
                }
                refreshNow();
                mPosOverride = -1;
            }
        }
        public void onStopTrackingTouch(SeekBar bar) {
           if (mService != null) {
                try {
                    mPosOverride = bar.getProgress() * mDuration / 1000;
                    mService.seek(mPosOverride);
                    refreshNow();
                } catch (RemoteException ex) {
                    MusicLogUtils.e(TAG, "Error:" + ex);
                }
           }
            mPosOverride = -1;
            mFromTouch = false;
        }
    };
    
    private View.OnClickListener mQueueListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mListParent.getVisibility() != View.VISIBLE) {
                applyRotation(0, 0, 90);
                mBackListView.setImageBitmap(toRoundCorner(((BitmapDrawable)mAlbum.getDrawable()).getBitmap(), 50));
                
            } else {
                applyRotation(-1, 0, -90);
                mBackListView.setImageResource(R.drawable.music_play_control_list_btn);
            }
        }
    };

    private Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        if(bitmap != null){
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = pixels;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }else{
            return null;
        }
        
        
    }

    private View.OnClickListener mShuffleListener = new View.OnClickListener() {
        public void onClick(View v) {
            toggleShuffle();
        }
    };

    private View.OnClickListener mRepeatListener = new View.OnClickListener() {
        public void onClick(View v) {
            cycleRepeat();
        }
    };

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
        }
    };

    private View.OnClickListener mPrevListener = new View.OnClickListener() {
        public void onClick(View v) {
            /// M: performance test, response time for Prev button
            MusicLogUtils.i("MusicPerformanceTest", "[Performance test][Music] prev song start ["
                                + System.currentTimeMillis() + "]");
            mPerformanceTestString = PREV_TEST;

            /// M: Handle click event in handler to avoid ANR for continuous
            // press @{
            MusicLogUtils.d(TAG, "Prev Button onClick,Send Msg");
            Message msg = mHandler.obtainMessage(PREV_BUTTON, null);
            mHandler.removeMessages(PREV_BUTTON);
            mHandler.sendMessage(msg);
            /// @}
        }
    };

    private View.OnClickListener mNextListener = new View.OnClickListener() {
        public void onClick(View v) {
            /// M: performance test, response time for Next button
            MusicLogUtils.i("MusicPerformanceTest", "[Performance test][Music] next song start ["
                                + System.currentTimeMillis() + "]");
            mPerformanceTestString = NEXT_TEST;

            /// M: Handle click event in handler to avoid ANR for continuous
            // press @{
            MusicLogUtils.d(TAG, "Next Button onClick,Send Msg");
            Message msg = mHandler.obtainMessage(NEXT_BUTTON, null);
            mHandler.removeMessages(NEXT_BUTTON);
            mHandler.sendMessage(msg);
            /// @}
        }
    };

    private View.OnClickListener mSoundAliveListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                Intent intent;
                Intent i = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mService.getAudioSessionId());
                startActivityForResult(i, EFFECTS_PANEL);
            } catch (RemoteException ex) {
            }
        }
    };

    private View.OnClickListener mBackListListener = new View.OnClickListener() {
        public void onClick(View v) {
            //onBackPressed();
            long playlist = MediaPlaybackService.mNowPlayList;
            
            Intent intent = new Intent();
            if(playlist == -1){
                intent.setClass(getBaseContext(), MusicBrowserActivity.class);
            }else{
                intent.setClass(getBaseContext(), InventoryFragmentAcitvity.class);
                intent.putExtra("InventoryId", playlist);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(0, R.anim.slide_down_out);
            finish();
        }
    };
    private RepeatingImageButton.RepeatListener mRewListener =
        new RepeatingImageButton.RepeatListener() {
        public void onRepeat(View v, long howlong, int repcnt) {
            MusicLogUtils.d(TAG, "music backward");
            mRepeatCount = repcnt;
            scanBackward(repcnt, howlong);
        }
    };
    
    private RepeatingImageButton.RepeatListener mFfwdListener =
        new RepeatingImageButton.RepeatListener() {
        public void onRepeat(View v, long howlong, int repcnt) {
            MusicLogUtils.d(TAG, "music forward");
            /// M: use to make current playing time aways showing when seeking
            mRepeatCount = repcnt;
            scanForward(repcnt, howlong);
        }
    };
   
    @Override
    public void onStop() {
        paused = true;
        MusicLogUtils.d(TAG, "onStop()");
        /// M: so mark mIsCallOnStop is true
        mIsCallOnStop = true;
        mHandler.removeMessages(REFRESH);
        unregisterReceiver(mStatusListener);
        MusicUtils.unbindFromService(mToken);
        mService = null;
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        paused = false;

        mToken = MusicUtils.bindToService(this, osc);
        if (mToken == null) {
            // something went wrong
            mHandler.sendEmptyMessage(QUIT);
        }
        
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        /// M: listen more status to update UI @{
        f.addAction(MediaPlaybackService.QUIT_PLAYBACK);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        /// @}
        registerReceiver(mStatusListener, new IntentFilter(f));
        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
    }
    
    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        /// M: when it launch from status bar, collapse status ba first. @{
        Intent intent = getIntent();
        boolean collapseStatusBar = intent.getBooleanExtra("collapse_statusbar", false);
        if (MusicFeatureOption.IS_SUPPORT_VOICE_COMMAND_UI /*&& (mVoiceCmdManager != null)*/) {
            MusicLogUtils.i(TAG, "onResume: register voice commond listenr and send start commond");
        }
        updateTrackInfo();
        if (!mIsCallOnStop) {
            setPauseButtonImage();
        }
        mIsCallOnStop = false;

        mPosOverride = -1;
        invalidateOptionsMenu();
        
        mPerformanceTestString = PLAY_TEST;
        mIsInBackgroud = false;
    }
    
    @Override
    public void onDestroy()
    {
        mAlbumArtWorker.quit();
        super.onDestroy();
        unregisterReceiverSafe(mNowPlayingListener);
        mAdapter = null;
        mPlayList.setAdapter(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        long currentAudioId = MusicUtils.getCurrentAudioId();
        if (currentAudioId >= 0) {
            menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu_short).setIcon(R.drawable.ic_menu_set_as_ringtone);
            menu.add(1, DELETE_ITEM, 0, R.string.delete_item).setIcon(R.drawable.ic_menu_delete);
            return true;
        }
        return false;
    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mService == null) return false;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        try {
            switch (item.getItemId()) {
                case GOTO_START:
                    intent = new Intent();
                    intent.setClass(this, MusicBrowserActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case USE_AS_RINGTONE: {
                    // Set the system setting to make this the current ringtone
                    if (mService != null) {
                        MusicUtils.setRingtone(this, mService.getAudioId());
                    }
                    return true;
                }
                /*case PARTY_SHUFFLE:
                    MusicUtils.togglePartyShuffle();
                    setShuffleButtonImage();
                    /// M: Update repeat button because will set repeat current to repeat all when open party shuffle.
                    setRepeatButtonImage();
                    break;*/
                    
                case NEW_PLAYLIST: {
                    intent = new Intent();
                    //intent.setClass(this, CreatePlaylist.class);
                    startActivityForResult(intent, NEW_PLAYLIST);
                    return true;
                }

                case PLAYLIST_SELECTED: {
                    long [] list = new long[1];
                    list[0] = MusicUtils.getCurrentAudioId();
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(this, list, playlist);
                    return true;
                }
                
                case DELETE_ITEM: {
                    if (mService != null) {
                        long [] list = new long[1];
                        list[0] = MusicUtils.getCurrentAudioId();
                        Bundle b = new Bundle();
                        String f;
                        b.putInt(MusicUtils.DELETE_DESC_STRING_ID, R.string.delete_song_desc);
                        b.putString(MusicUtils.DELETE_DESC_TRACK_INFO, mService.getTrackName());
                        b.putLongArray("items", list);
                        intent = new Intent();
                        intent.setClass(this, DeleteItems.class);
                        intent.putExtras(b);
                        startActivityForResult(intent, -1);
                    }
                    return true;
                }

                /// M: Show effect panel and call the same method as other activities.
                case EFFECTS_PANEL:
                    return MusicUtils.startEffectPanel(this);

                /// M: Open FMTransmitter and Search view. {@
                case FM_TRANSMITTER:
                    Intent intentFMTx = new Intent(FM_TX_ACTIVITY);
                    intentFMTx.setClassName(FM_TX_PACKAGE, FM_TX_ACTIVITY);

                    try {
                        startActivity(intentFMTx);
                    } catch (ActivityNotFoundException anfe) {
                        MusicLogUtils.e(TAG, "FMTx activity isn't found!!");
                    }
                
                    return true;

                case R.id.search:
                    onSearchRequested();
                    return true;
                /// @}

                /// M: handle action bar and navigation up button. {@
                case android.R.id.home:
                    /// M: Navigation button press back,
                    /// aviod Navigation button respond JE if Activity is background
                    if (!mIsInBackgroud) {
                        Intent parentIntent = new Intent(this, MusicBrowserActivity.class);
                        parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        parentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        parentIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        finish();
                        startActivity(parentIntent);
                    }
                    return true;

                case R.id.current_playlist_menu_item:
                    /// M: Current playlist(queue) button
                    mQueueListener.onClick(null);
                    break;

                case R.id.shuffle_menu_item:
                    /// M: Shuffle button
                    toggleShuffle();
                    break;

                case R.id.repeat_menu_item:
                    /// M: Repeat button
                    cycleRepeat();
                    break;

                default:
                    return true;
                /// @}
            }
        } catch (RemoteException ex) {
            MusicLogUtils.e(TAG, "onOptionsItemSelected with RemoteException " + ex);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case NEW_PLAYLIST:
                Uri uri = intent.getData();
                if (uri != null) {
                    long [] list = new long[1];
                    list[0] = MusicUtils.getCurrentAudioId();
                    int playlist = Integer.parseInt(uri.getLastPathSegment());
                    MusicUtils.addToPlaylist(this, list, playlist);
                }
                break;
        }
    }
    private final int keyboard[][] = {
        {
            KeyEvent.KEYCODE_Q,
            KeyEvent.KEYCODE_W,
            KeyEvent.KEYCODE_E,
            KeyEvent.KEYCODE_R,
            KeyEvent.KEYCODE_T,
            KeyEvent.KEYCODE_Y,
            KeyEvent.KEYCODE_U,
            KeyEvent.KEYCODE_I,
            KeyEvent.KEYCODE_O,
            KeyEvent.KEYCODE_P,
        },
        {
            KeyEvent.KEYCODE_A,
            KeyEvent.KEYCODE_S,
            KeyEvent.KEYCODE_D,
            KeyEvent.KEYCODE_F,
            KeyEvent.KEYCODE_G,
            KeyEvent.KEYCODE_H,
            KeyEvent.KEYCODE_J,
            KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_L,
            KeyEvent.KEYCODE_DEL,
        },
        {
            KeyEvent.KEYCODE_Z,
            KeyEvent.KEYCODE_X,
            KeyEvent.KEYCODE_C,
            KeyEvent.KEYCODE_V,
            KeyEvent.KEYCODE_B,
            KeyEvent.KEYCODE_N,
            KeyEvent.KEYCODE_M,
            KeyEvent.KEYCODE_COMMA,
            KeyEvent.KEYCODE_PERIOD,
            KeyEvent.KEYCODE_ENTER
        }

    };

    private int lastX;
    private int lastY;

    private boolean seekMethod1(int keyCode)
    {
        if (mService == null) return false;
        for(int x=0;x<10;x++) {
            for(int y=0;y<3;y++) {
                if(keyboard[y][x] == keyCode) {
                    int dir = 0;
                    // top row
                    if(x == lastX && y == lastY) dir = 0;
                    else if (y == 0 && lastY == 0 && x > lastX) dir = 1;
                    else if (y == 0 && lastY == 0 && x < lastX) dir = -1;
                    // bottom row
                    else if (y == 2 && lastY == 2 && x > lastX) dir = -1;
                    else if (y == 2 && lastY == 2 && x < lastX) dir = 1;
                    // moving up
                    else if (y < lastY && x <= 4) dir = 1; 
                    else if (y < lastY && x >= 5) dir = -1; 
                    // moving down
                    else if (y > lastY && x <= 4) dir = -1; 
                    else if (y > lastY && x >= 5) dir = 1; 
                    lastX = x;
                    lastY = y;
                    try {
                        mService.seek(mService.position() + dir * 5);
                    } catch (RemoteException ex) {
                    }
                    refreshNow();
                    return true;
                }
            }
        }
        lastX = -1;
        lastY = -1;
        return false;
    }

    private boolean seekMethod2(int keyCode)
    {
        if (mService == null) return false;
        for(int i=0;i<10;i++) {
            if(keyboard[0][i] == keyCode) {
                int seekpercentage = 100*i/10;
                try {
                    mService.seek(mService.duration() * seekpercentage / 100);
                } catch (RemoteException ex) {
                }
                refreshNow();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        try {
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (!useDpadMusicControl()) {
                        break;
                    }
                    if (mService != null) {
                        if (!mSeeking && mStartSeekPos >= 0) {
                            mPauseButton.requestFocus();
                            if (mStartSeekPos < 1000) {
                                mService.prev();
                            } else {
                                mService.seek(0);
                            }
                        } else {
                            scanBackward(-1, event.getEventTime() - event.getDownTime());
                            mPauseButton.requestFocus();
                            mStartSeekPos = -1;
                        }
                    }
                    mSeeking = false;
                    mPosOverride = -1;
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (!useDpadMusicControl()) {
                        break;
                    }
                    if (mService != null) {
                        if (!mSeeking && mStartSeekPos >= 0) {
                            mPauseButton.requestFocus();
                            mService.next();
                        } else {
                            scanForward(-1, event.getEventTime() - event.getDownTime());
                            mPauseButton.requestFocus();
                            mStartSeekPos = -1;
                        }
                    }
                    mSeeking = false;
                    mPosOverride = -1;
                    return true;
                    
                /// M: handle key code center. {@
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    View curSel = getCurrentFocus();
                    if ((curSel != null && R.id.pause == curSel.getId()) || 
                            (curSel == null)) {
                        doPauseResume();
                    }
                    return true;
                /// @}
            }
        } catch (RemoteException ex) {
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean useDpadMusicControl() {
        if (mDeviceHasDpad && (mPrevButton.isFocused() ||
                mNextButton.isFocused() ||
                mPauseButton.isFocused())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        int direction = -1;
        int repcnt = event.getRepeatCount();

        if((seekmethod==0)?seekMethod1(keyCode):seekMethod2(keyCode))
            return true;

        switch(keyCode) {
            case KeyEvent.KEYCODE_SLASH:
                seekmethod = 1 - seekmethod;
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!useDpadMusicControl()) {
                    break;
                }
                if (!mPrevButton.hasFocus()) {
                    mPrevButton.requestFocus();
                }
                scanBackward(repcnt, event.getEventTime() - event.getDownTime());
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!useDpadMusicControl()) {
                    break;
                }
                if (!mNextButton.hasFocus()) {
                    mNextButton.requestFocus();
                }
                scanForward(repcnt, event.getEventTime() - event.getDownTime());
                return true;

            case KeyEvent.KEYCODE_S:
                toggleShuffle();
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
                /// M: handle key code center.
                 return true;
                 
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
                doPauseResume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //audioMgr.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_UP:
                //audioMgr.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void scanBackward(int repcnt, long delta) {
        if(mService == null) return;
        try {
            if(repcnt == 0) {
                mStartSeekPos = mService.position();
                mLastSeekEventTime = 0;
                mSeeking = false;
            } else {
                mSeeking = true;
                if (delta < 5000) {
                    // seek at 10x speed for the first 5 seconds
                    delta = delta * 10; 
                } else {
                    // seek at 40x after that
                    delta = 50000 + (delta - 5000) * 40;
                }
                long newpos = mStartSeekPos - delta;
                if (newpos < 0) {
                    // move to previous track
                    mService.prev();
                    long duration = mService.duration();
                    mStartSeekPos += duration;
                    newpos += duration;
                }
                if (((delta - mLastSeekEventTime) > 250) || repcnt < 0){
                    mService.seek(newpos);
                    mLastSeekEventTime = delta;
                }
                if (repcnt >= 0) {
                    mPosOverride = newpos;
                } else {
                    mPosOverride = -1;
                }
                refreshNow();
            }
        } catch (RemoteException ex) {
        }
    }

    private void scanForward(int repcnt, long delta) {
        if(mService == null) return;
        try {
            if(repcnt == 0) {
                mStartSeekPos = mService.position();
                mLastSeekEventTime = 0;
                mSeeking = false;
            } else {
                mSeeking = true;
                if (delta < 5000) {
                    // seek at 10x speed for the first 5 seconds
                    delta = delta * 10; 
                } else {
                    // seek at 40x after that
                    delta = 50000 + (delta - 5000) * 40;
                }
                long newpos = mStartSeekPos + delta;
                long duration = mService.duration();
                if (newpos >= duration) {
                    // move to next track
                    mService.next();
                    mStartSeekPos -= duration; // is OK to go negative
                    newpos -= duration;
                }
                if (((delta - mLastSeekEventTime) > 250) || repcnt < 0){
                    mService.seek(newpos);
                    mLastSeekEventTime = delta;
                }
                if (repcnt >= 0) {
                    mPosOverride = newpos;
                } else {
                    mPosOverride = -1;
                }
                refreshNow();
            }
        } catch (RemoteException ex) {
        }
    }
    
    private void doPauseResume() {
        try {
            if(mService != null) {
                Boolean isPlaying = mService.isPlaying();
                MusicLogUtils.d(TAG, "doPauseResume: isPlaying=" + isPlaying);
                /// M: AVRCP and Android Music AP supports the FF/REWIND
                //   aways get position from service if user press pause button
                mPosOverride = -1;
                if (isPlaying) {
                    mService.pause();
                } else {
                    mService.play();
                }
                refreshNow();
                setPauseButtonImage();
                if (mAdapter != null) {
                    mAdapter.changeCurrentItemStauts(!isPlaying);
                }
            }
        } catch (RemoteException ex) {
        }
    }
    
    private void toggleShuffle() {
        if (mService == null) {
            return;
        }
        try {
            int shuffle = mService.getShuffleMode();
            if (shuffle == MediaPlaybackService.SHUFFLE_NONE) {
                mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
                if (mService.getRepeatMode() == MediaPlaybackService.REPEAT_CURRENT) {
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                }
                /// M: need to refresh repeat button when we modify rpeate mode.
                setRepeatButtonImage();
                showToast(R.string.shuffle_on_notif);
            } else if (shuffle == MediaPlaybackService.SHUFFLE_NORMAL ||
                    shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                /// M: After turn off party shuffle, we should to refresh option menu to avoid user click fast to show
                /// party shuffle off when has turned off.
                invalidateOptionsMenu();
                showToast(R.string.shuffle_off_notif);
            } else {
                MusicLogUtils.w(TAG, "Invalid shuffle mode: " + shuffle);
            }
            setShuffleButtonImage();
        } catch (RemoteException ex) {
        }
    }
    
    private void cycleRepeat() {
        if (mService == null) {
            return;
        }
        try {
            int mode = mService.getRepeatMode();
            if (mode == MediaPlaybackService.REPEAT_NONE) {
                mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                showToast(R.string.repeat_all_notif);
            } else if (mode == MediaPlaybackService.REPEAT_ALL) {
                mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
                if (mService.getShuffleMode() != MediaPlaybackService.SHUFFLE_NONE) {
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                    /// M: After turn off party shuffle, we should to refresh option menu to avoid user click fast to show
                    /// party shuffle off when has turned off.
                    invalidateOptionsMenu();
                    setShuffleButtonImage();
                }
                showToast(R.string.repeat_current_notif);//重复播放当前歌曲
            } else {
                mService.setRepeatMode(MediaPlaybackService.REPEAT_NONE);
                showToast(R.string.play_shuffle);//重复播放已关闭
            }
            setRepeatButtonImage();
        } catch (RemoteException ex) {
        }
        
    }
    
    private void showToast(int resid) {
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(resid);
        mToast.show();
    }

    private void startPlayback() {

        if(mService == null)
            return;
        Intent intent = getIntent();
        String filename = "";
        Uri uri = intent.getData();
        if (uri != null && uri.toString().length() > 0) {
            // If this is a file:// URI, just use the path directly instead
            // of going through the open-from-filedescriptor codepath.
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                filename = uri.getPath();
            } else {
                filename = uri.toString();
            }
            try {
                mService.stop();
                mService.openFile(filename);
                mService.play();
                setIntent(new Intent());
            } catch (Exception ex) {
                MusicLogUtils.d(TAG, "couldn't start playback: " + ex);
            }
        }

        updateTrackInfo();
        initAdapter();
        long next = refreshNow();
        queueNextRefresh(next);
    }

    private ServiceConnection osc = new ServiceConnection() {
            public void onServiceConnected(ComponentName classname, IBinder obj) {
                mService = IMediaPlaybackService.Stub.asInterface(obj);
                /// M: Call this to invalidate option menu to install action bar
                invalidateOptionsMenu();
                startPlayback();
                try {
                    // Assume something is playing when the service says it is,
                    // but also if the audio ID is valid but the service is paused.
                    if (mService.getAudioId() >= 0 || mService.isPlaying() ||
                            mService.getPath() != null) {
                        // something is playing now, we're done
                        /// M: Only in portrait we need to set them to be 
                        // visible {@
                        if (!mIsLandscape) {
                            mRepeatButton.setVisibility(View.VISIBLE);
                            /*mShuffleButton.setVisibility(View.VISIBLE);*/
                        }
                        /// @}
                        setRepeatButtonImage();
                        setShuffleButtonImage();
                        setPauseButtonImage();
                        return;
                    }
                } catch (RemoteException ex) {
                }
                
                Log.i("HHJ", "finish 1490 ");
                finish();
            }
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
                Log.i("HHJ", "finish 1495 ");
                finish();
            }
    };

    private void setRepeatButtonImage() {
        if (mService == null) return;
        try {
            int drawable;
            switch (mService.getRepeatMode()) {
                case MediaPlaybackService.REPEAT_ALL:
                    drawable = R.drawable.music_play_menu_repeat_all;
                    break;
                    
                case MediaPlaybackService.REPEAT_CURRENT:
                    drawable = R.drawable.music_play_menu_repeat_one;
                    break;
                    
                default:
                    drawable = R.drawable.music_play_menu_repeat_off;
                    break;
                    
            }
            if (mIsLandscape) {
                /*if (mRepeatMenuItem != null) {   
                    mRepeatMenuItem.setIcon(drawable);
                }*/
            } else {
                mRepeatButton.setImageResource(drawable);
            }
            /// @}
        } catch (RemoteException ex) {
        }
    }
    
    private void setShuffleButtonImage() {
        if (mService == null) return;
        try {
            /// M: Set drawable to action bar in landscape and set it to button in 
            // portrait  {@
            int drawable = 0;
            switch (mService.getShuffleMode()) {
                case MediaPlaybackService.SHUFFLE_NONE:
                    //drawable = R.drawable.music_play_menu_shuffle_off;
                    break;
                    
                case MediaPlaybackService.SHUFFLE_AUTO:
                    break;
                    
                default:
                    //drawable = R.drawable.music_play_menu_shuffle_on;
                    break;
                    
            }
            if (mIsLandscape) {
                /*if (mShuffleMenuItem != null) {
                    mShuffleMenuItem.setIcon(drawable);
                }*/
            } else {
                /*mShuffleButton.setImageResource(drawable);*/
            }
            /// @}
        } catch (RemoteException ex) {
        }
    }
    
    private void setPauseButtonImage() {
        try {
            if (mService != null && mService.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.music_play_control_pause);
                if (!mSeeking) {
                    mPosOverride = -1;
                }
            } else {
                mPauseButton.setImageResource(R.drawable.music_play_control_play);
            }
        } catch (RemoteException ex) {
        }
    }
    
    private ImageView mAlbum;
    private ImageView mMenuOption;
    private ImageView mPlayerBack;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    //private TextView mArtistName;
    private TextView mAlbumName;
    private TextView mTrackName;
    private ProgressBar mProgress;
    private long mPosOverride = -1;
    private boolean mFromTouch = false;
    private long mDuration;
    private int seekmethod;
    private boolean paused;

    private static final int REFRESH = 1;
    private static final int QUIT = 2;
    private static final int GET_ALBUM_ART = 3;
    private static final int ALBUM_ART_DECODED = 4;

    /// M: Define next and prev button.
    private static final int NEXT_BUTTON = 6;
    private static final int PREV_BUTTON = 7;

    private void queueNextRefresh(long delay) {
        if (!paused) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    private long refreshNow() {
        /// M: duration for position correction for play complete
        final int positionCorrection = 300;
        if(mService == null)
            return 500;
        try {
            long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
            /// M: position correction for play complete @{
            if (pos + positionCorrection > mDuration) {
                MusicLogUtils.d(TAG, "refreshNow, do a workaround for position");
                pos = mDuration;
            }
            /// @}
            if ((pos >= 0) && (mDuration > 0)) {
                mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
                /// M: Don't need to update from touch @{
                if (!mFromTouch) {
                    int progress = (int) (1000 * pos / mDuration);
                    mProgress.setProgress(progress);
                }
                /// @}
                /// M: use to make current playing time aways showing when seeking
                if (mService.isPlaying() || mRepeatCount > -1) {
                    mCurrentTime.setVisibility(View.VISIBLE);
                } else {
                    // blink the counter
                    int vis = mCurrentTime.getVisibility();
                    return 500;
                }
            } else {
                /// M: adjust the UI for error file  @{
                mCurrentTime.setVisibility(View.VISIBLE);
                mCurrentTime.setText("0:00");
                mTotalTime.setText("--:--");
                if (!mFromTouch) {
                    mProgress.setProgress(0);
                }
                /// @}
            }
            /// M: update duration for specific formats
            updateDuration(pos);
            // calculate the number of milliseconds until the next full second, so
            // the counter can be updated at just the right time
            long remaining = 1000 - (pos % 1000);

            // approximate how often we would need to refresh the slider to
            // move it smoothly
            int width = mProgress.getWidth();
            if (width == 0) width = 320;
            long smoothrefreshtime = mDuration / width;

            if (smoothrefreshtime > remaining) return remaining;
            if (smoothrefreshtime < 20) return 20;
            return smoothrefreshtime;
        } catch (RemoteException ex) {
        }
        return 500;
    }
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALBUM_ART_DECODED:
                    mAlbum.setImageBitmap((Bitmap)msg.obj);
                    mAlbum.getDrawable().setDither(true);
                    if (mListParent.getVisibility() == View.VISIBLE) {
                        mBackListView.setImageBitmap(toRoundCorner((Bitmap)msg.obj, 50));
                    }
                    break;

                case REFRESH:
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
                    
                case QUIT:
                    // This can be moved back to onCreate once the bug that prevents
                    // Dialogs from being started from onCreate/onResume is fixed.
                    new AlertDialog.Builder(MediaPlaybackActivity.this)
                            .setTitle(R.string.service_start_error_title)
                            .setMessage(R.string.service_start_error_msg)
                            .setPositiveButton(R.string.service_start_error_button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Log.i("HHJ", "finish 1705 ");
                                            finish();
                                        }
                                    })
                            .setCancelable(false)
                            .show();
                    break;

                /// M: Handle next and prev button. {@
                case NEXT_BUTTON:
                    MusicLogUtils.d(TAG, "Next Handle");
                    if (mService == null) {
                        return;
                    }
                    mNextButton.setEnabled(false);
                    mNextButton.setFocusable(false);
                    try {
                        mService.next();
                        mPosOverride = -1;
                    } catch (RemoteException ex) {
                        MusicLogUtils.e(TAG, "Error:" + ex);
                    }                
                    mNextButton.setEnabled(true);
                    mNextButton.setFocusable(true);
                    break;
                    
                case PREV_BUTTON:
                    MusicLogUtils.d(TAG, "Prev Handle");
                    if (mService == null) {
                        return;
                    }
                    mPrevButton.setEnabled(false);
                    mPrevButton.setFocusable(false);
                    try {
                        mPosOverride = -1;
                        mService.prev();
                    } catch (RemoteException ex) {
                        MusicLogUtils.e(TAG, "Error:" + ex);
                    }
                    mPrevButton.setEnabled(true);
                    mPrevButton.setFocusable(true);
                    break;
                /// @}

                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MusicLogUtils.d(TAG, "mStatusListener: " + action);
            if (action.equals(MediaPlaybackService.META_CHANGED)) {
                /// M: Refresh option menu when meta change
                invalidateOptionsMenu();

                // redraw the artist/title info and
                // set new max for progress bar
                updateTrackInfo();
                setPauseButtonImage();

                MusicLogUtils.v("MusicPerformanceTest", "[Performance test][Music] "
                        + mPerformanceTestString + " end [" + System.currentTimeMillis() 
                        + "]");
                
                queueNextRefresh(1);
            } else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
                setPauseButtonImage();
            /// M: Handle more status. {@
            } else if (action.equals(MediaPlaybackService.QUIT_PLAYBACK)) {
                mHandler.removeMessages(REFRESH);
                Log.i("HHJ", "finish 1779 ");
                finish();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                /// M: stop refreshing
                MusicLogUtils.d(TAG, "onReceive, stop refreshing ...");
                mHandler.removeMessages(REFRESH);
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                /// M: restore refreshing
                MusicLogUtils.d(TAG, "onReceive, restore refreshing ...");
                long next = refreshNow();
                queueNextRefresh(next);
            }
            /// @}
        }
    };

    private static class AlbumSongIdWrapper {
        public long albumid;
        public long songid;
        AlbumSongIdWrapper(long aid, long sid) {
            albumid = aid;
            songid = sid;
        }
    }
    
    private void updateTrackInfo() {
        if (mService == null) {
            return;
        }
        try {
            String path = mService.getPath();
            if (path == null) {
                Log.i("HHJ", "finish 1811 "+mService.getAudioId());
                finish();
                return;
            }
            
            long songid = mService.getAudioId(); 
            if (songid < 0 && path.toLowerCase().startsWith("http://")) {
                // Once we can get album art and meta data from MediaPlayer, we
                // can show that info again when streaming.
                //((View) mArtistName.getParent()).setVisibility(View.INVISIBLE);
                ((View) mAlbumName.getParent()).setVisibility(View.INVISIBLE);
                mAlbum.setVisibility(View.GONE);
                mTrackName.setText(path);
                mAlbumArtHandler.removeMessages(GET_ALBUM_ART);
                mAlbumArtHandler.obtainMessage(GET_ALBUM_ART, new AlbumSongIdWrapper(-1, -1)).sendToTarget();
            } else {
                //((View) mArtistName.getParent()).setVisibility(View.VISIBLE);
                ((View) mAlbumName.getParent()).setVisibility(View.VISIBLE);
                String artistName = mService.getArtistName();
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = getString(R.string.unknown_artist_name);
                }
                //mArtistName.setText(artistName);
                String albumName = mService.getAlbumName();
                long albumid = mService.getAlbumId();
                if (MediaStore.UNKNOWN_STRING.equals(albumName)) {
                    albumName = getString(R.string.unknown_album_name);
                    albumid = -1;
                }
                mAlbumName.setText(albumName+"-"+artistName);
                mTrackName.setText(mService.getTrackName());
                mAlbumArtHandler.removeMessages(GET_ALBUM_ART);
                mAlbumArtHandler.obtainMessage(GET_ALBUM_ART, new AlbumSongIdWrapper(albumid, songid)).sendToTarget();
                if (mListParent != null && mListParent.getVisibility() != View.VISIBLE) {
                    mAlbum.setVisibility(View.VISIBLE);
                }
            }
            mDuration = mService.duration();
            mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
            recordDurationUpdateStatus();
            int currentindex = mService.getQueuePosition()+1;
            int totalNum = mService.getPlayListSize();
            mCurrentPlayIndex.setText(""+currentindex+"/"+totalNum);
            mListHeadRight.setText(currentindex + "/" + totalNum);
            String f = getResources().getQuantityText(R.plurals.Nsongs, totalNum).toString();
            mListHeadLeft.setText(String.format(f, totalNum));
        } catch (RemoteException ex) {
            Log.i("HHJ", "finish 1858 ");
            finish();
        }
    }

    public class AlbumArtHandler extends Handler {
        private long mAlbumId = -1;
        
        public AlbumArtHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg)
        {
            /// M: Keep album art in mArtBitmap to improve loading speed when config changed.
            long albumid = ((AlbumSongIdWrapper) msg.obj).albumid;
            long songid = ((AlbumSongIdWrapper) msg.obj).songid;
            if (msg.what == GET_ALBUM_ART && (mAlbumId != albumid || albumid < 0 || mIsShowAlbumArt)) {
                Message numsg = null;
                // while decoding the new image, show the default album art
                if (mArtBitmap == null || mArtSongId != songid) {
                    numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
                    mHandler.removeMessages(ALBUM_ART_DECODED);
                    mHandler.sendMessageDelayed(numsg, 300);

                    // Don't allow default artwork here, because we want to fall back to song-specific
                    // album art if we can't find anything for the album.
                    /// M: if don't get album art from file,or the album art is not the same 
                    /// as the song ,we should get the album art again
                    mArtBitmap = MusicUtils.getArtwork(MediaPlaybackActivity.this,
                                                        songid, albumid, false);
                    MusicLogUtils.d(TAG, "get art. mArtSongId = " + mArtSongId 
                                            + " ,songid = " + songid + " ");
                    mArtSongId = songid;
                }
                
                if (mArtBitmap == null) {
                    mArtBitmap = MusicUtils.getDefaultArtwork(MediaPlaybackActivity.this,songid);
                    albumid = -1;
                }
                if (mArtBitmap != null) {
                    numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, mArtBitmap);
                    mHandler.removeMessages(ALBUM_ART_DECODED);
                    mHandler.sendMessage(numsg);
                }
                mAlbumId = albumid;
                mIsShowAlbumArt = false;
            }
        }
    }
    
    private static class Worker implements Runnable {
        private final Object mLock = new Object();
        private Looper mLooper;
        
        /**
         * Creates a worker thread with the given name. The thread
         * then runs a {@link android.os.Looper}.
         * @param name A name for the new thread
         */
        Worker(String name) {
            Thread t = new Thread(null, this, name);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            synchronized (mLock) {
                while (mLooper == null) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        
        public Looper getLooper() {
            return mLooper;
        }
        
        public void run() {
            synchronized (mLock) {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mLock.notifyAll();
            }
            Looper.loop();
        }
        
        public void quit() {
            mLooper.quit();
        }
    }

    /**
     * M: move from onCreat, Update media playback activity ui. call this method
     * when activity oncreate or on configuration changed.
     */
    private void updateUI() {
        setContentView(R.layout.audio_player);
        
        mPlayerBack = (ImageView) findViewById(R.id.audio_player_back);
        mPlayerBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mCurrentTime = (TextView) findViewById(R.id.currenttime);
        mTotalTime = (TextView) findViewById(R.id.totaltime);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        
        mMenuOption = (ImageView) findViewById(R.id.menu_option);
        mMenuOption.setOnCreateContextMenuListener(this);

        mAlbum = (ImageView) findViewById(R.id.album);
        //mArtistName = (TextView) findViewById(R.id.artistname);
        mAlbumName = (TextView) findViewById(R.id.albumname);
        mTrackName = (TextView) findViewById(R.id.trackname);

        mCurrentPlayIndex = (TextView) findViewById(R.id.currentPlayIndex);
        mContainer = (ViewGroup) findViewById(R.id.container);
        mPlayList = (ListView) findViewById(R.id.playList);
        mListParent = findViewById(R.id.list_parent);
        mListHeadLeft = (TextView) findViewById(R.id.list_head_left);
        mListHeadRight = (TextView) findViewById(R.id.list_head_right);
        mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
        mPlayList.setOnItemClickListener(mPlayListListener);

        /*View v = (View)mArtistName.getParent();
        v.setOnTouchListener(this);
        v.setOnLongClickListener(this);

        v = (View)mAlbumName.getParent();
        v.setOnTouchListener(this);
        v.setOnLongClickListener(this);

        v = (View)mTrackName.getParent();
        v.setOnTouchListener(this);
        v.setOnLongClickListener(this);*/

        mPrevButton = (RepeatingImageButton) findViewById(R.id.prev);
        mPrevButton.setOnClickListener(mPrevListener);
        mPrevButton.setRepeatListener(mRewListener, 260);
        mPauseButton = (ImageButton) findViewById(R.id.pause);
        mPauseButton.requestFocus();
        mPauseButton.setOnClickListener(mPauseListener);
        mNextButton = (RepeatingImageButton) findViewById(R.id.next);
        mNextButton.setOnClickListener(mNextListener);
        mNextButton.setRepeatListener(mFfwdListener, 260);
        seekmethod = 1;

        mDeviceHasDpad = (getResources().getConfiguration().navigation == Configuration.NAVIGATION_DPAD);

        if (!mIsLandscape) {
            mQueueButton = (ImageButton) findViewById(R.id.curplaylist);
            mQueueButton.setOnClickListener(mQueueListener);
            mRepeatButton = ((ImageView) findViewById(R.id.repeat));
            mRepeatButton.setOnClickListener(mRepeatListener);
        }
        
        mBackListView = (ImageView)findViewById(R.id.list_btn);
        mBackListView.setOnClickListener(mBackListListener);
        /*mBackListView.setOnClickListener(mQueueListener);*/

        if (mProgress instanceof SeekBar) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        mProgress.setMax(1000);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
    }

    /**
     *  M: save the activity is in background.
     */
    @Override
    protected void onPause() {
        /// M: aviod Navigation button respond JE if Activity is background
        mIsInBackgroud = true;
        /// M :unregister voice command listener and cancel voice command indicator. @{
        /*if (MusicFeatureOption.IS_SUPPORT_VOICE_COMMAND_UI && (mVoiceCmdManager != null)) {
            MusicLogUtils.i(TAG, "onPause: unregister voice command listener and send stop command");
            try {
                mVoiceCmdManager.sendCommand(this, VoiceCommandListener.ACTION_MAIN_VOICE_UI,
                        VoiceCommandListener.ACTION_VOICE_UI_STOP, null);
                mVoiceCmdManager.unRegisterListener(mVoiceCmdListener);
                mNotificationManager.cancel(VOICE_COMMAND_INDICATOR);
            } catch (RemoteException e) {
                MusicLogUtils.i(TAG, "unregister voice listener and send stop command with RemoteException: " + e);
            } catch (IllegalAccessException e) {
                MusicLogUtils.i(TAG, "unregister voice listener and send stop command with IllegalAccessException: " + e);
            }
        }*/
        ///@}
        super.onPause();
    }

    /**
     *  M: handle config change.
     *
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /// M: When configuration change, get the current orientation
        mIsLandscape = (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
        /// M: when configuration changed ,set mIsShowAlbumArt = true to update album art
        mIsShowAlbumArt = true;
        updateUI();
        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
        setRepeatButtonImage();
        setPauseButtonImage();
        setShuffleButtonImage();
        /// M: When back to this activity, ask service for right position
        mPosOverride = -1;
        
        /// M: Refresh action bar menu item
        invalidateOptionsMenu();
    }

    /**
     * M: Call when search request and expand search action view.
     */
    @Override
    public boolean onSearchRequested() {
        if (mSearchItem != null) {
            mSearchItem.expandActionView();
        }
        return true;
    }

    /**
     * M: Search view query text listener.
     */
    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        public boolean onQueryTextSubmit(String query) {
            Intent intent = new Intent();
            //intent.setClass(MediaPlaybackActivity.this, QueryBrowserActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
            mSearchItem.collapseActionView();
            return true;
        }

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    /**
     * M: get the background color when touched, it may get from thememager.
     * 
     * @return Return background color
     */
    private int getBackgroundColor() {
        /// M: default background color for ICS.
        final int defaultBackgroundColor = 0xcc0099cc;
        /// M: For ICS style and support for theme manager {@
        int ret = defaultBackgroundColor;
        if (MusicFeatureOption.IS_SUPPORT_THEMEMANAGER) {
            Resources res = getResources();
            /*ret = res.getThemeMainColor();
            if (ret == 0) {
                ret = defaultBackgroundColor;
            }*/
        }
        return ret;
    }

    /**
     * M: update duration for MP3/AMR/AWB/AAC/FLAC formats.
     *
     * @param position The current positon for error check.
     */
    private void updateDuration(long position) {
        final int soundToMs = 1000;
        try {
            if (mNeedUpdateDuration && mService.isPlaying()) {
                long newDuration = mService.duration();

                if (newDuration > 0L && newDuration != mDuration) {
                    mDuration = newDuration;
                    mNeedUpdateDuration = false;
                    /// M: Update UI with new duration.
                    mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / soundToMs));
                    MusicLogUtils.i(TAG, "new duration updated!!");
                }
            } else if (position < 0 || position >= mDuration) {
                mNeedUpdateDuration = false;
            }
        } catch (RemoteException ex) {
            MusicLogUtils.e(TAG, "Error:" + ex);
        }
    }

    /**
     * M: record duration update status when playing,
     * if play mp3/aac/amr/awb/flac file, set mNeedUpdateDuration to update
     * layter in updateDuration().
     */
    private void recordDurationUpdateStatus() {
        final String mimeTypeMpeg = "audio/mpeg";
        final String mimeTypeAmr = "audio/amr";
        final String mimeTypeAmrWb = "audio/amr-wb";
        final String mimeTypeAac = "audio/aac";
        final String mimeTypeFlac = "audio/flac";
        String mimeType;
        mNeedUpdateDuration = false;
        try {
            mimeType = mService.getMIMEType();
        } catch (RemoteException ex) {
            MusicLogUtils.e(TAG, "Error:" + ex);
            mimeType = null;
        }
        if (mimeType != null) {
            MusicLogUtils.i(TAG, "mimeType=" + mimeType);
            if (mimeType.equals(mimeTypeMpeg) 
                || mimeType.equals(mimeTypeAmr) 
                || mimeType.equals(mimeTypeAmrWb) 
                || mimeType.equals(mimeTypeAac)
                || mimeType.equals(mimeTypeFlac)) {
                mNeedUpdateDuration = true;
            }
        }
    }

    /**
     * M: Add NFC callback to provide the uri.
     */
    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        Uri currentUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicUtils.getCurrentAudioId());;
        MusicLogUtils.i(TAG, "NFC call for uri " + currentUri);
        return new Uri[] {currentUri};
    }

    /**
     * M: Register voice command listener.
     */
    private void registerVoiceUiListener() {
        if (!MusicFeatureOption.IS_SUPPORT_VOICE_COMMAND_UI) {
            MusicLogUtils.w(TAG, "registerVoiceUiListener when not support voice ui feature, return!");
            return;
        }
        /// M: Get voice command manager
       /* mVoiceCmdManager = (IVoiceCommandManager) getSystemService(VoiceCommandListener.VOICE_COMMAND_SERVICE);
        if (mVoiceCmdManager == null) {
            MusicLogUtils.e(TAG, "registerVoiceUiListener with null vocie command manager!");
            return;
        }*/
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        /// M: New a VoiceCommandListener
        /*mVoiceCmdListener = new VoiceCommandListener(this) {
            @Override
            public void onVoiceCommandNotified(int mainAction, int subAction, Bundle extraData) {
                int actionExtraResult = extraData.getInt(ACTION_EXTRA_RESULT);
                int actionExtraResultInfo = extraData.getInt(ACTION_EXTRA_RESULT_INFO);
                String actionExtraResultInfoString = extraData.getString(ACTION_EXTRA_RESULT_INFO1);
                MusicLogUtils.d(TAG, "mainAction = " + mainAction + ",subAction = " + subAction
                        + " ,result = " + actionExtraResult + " ,info = " + actionExtraResultInfo + " ,infoString = "
                        + actionExtraResultInfoString);
                if (actionExtraResult != ACTION_EXTRA_RESULT_SUCCESS) {
                    MusicLogUtils.w(TAG, "onVoiceCommandNotified with failed result, just return");
                    return;
                } 
                if (mainAction == VoiceCommandListener.ACTION_MAIN_VOICE_UI) {
                    switch (subAction) {
                        ///M : voice command start,send command about show the indicator.
                        case ACTION_VOICE_UI_START:
                            try {
                                mVoiceCmdManager.sendCommand(MediaPlaybackActivity.this,
                                        VoiceCommandListener.ACTION_MAIN_VOICE_COMMON,
                                        VoiceCommandListener.ACTION_VOICE_COMMON_KEYWORD, null);
                            } catch (RemoteException e) {
                                MusicLogUtils.e(TAG, "Send voice ui start command with RemoteException: " + e);
                            } catch (IllegalAccessException e) {
                                MusicLogUtils.e(TAG, "Send voice ui start command with IllegalAccessException: " + e);
                            }
                            break;
                        /// M: the voice command action.Such as "play","pause","next" and so on.
                        case ACTION_VOICE_UI_NOTIFY:
                            voiceUiCommand(actionExtraResultInfo);
                            break;
                        /// M: TODO voice command stop May use if need.
                        case ACTION_VOICE_UI_STOP:
                            break;
                        default:
                            MusicLogUtils.e(TAG, "Undefined voice ui sub action!");
                            break;
                    }
                } else if (mainAction == VoiceCommandListener.ACTION_MAIN_VOICE_COMMON) {
                    /// M: show the indicator in notification bar.
                    if (subAction == VoiceCommandListener.ACTION_VOICE_COMMON_KEYWORD) {
                        String[] stringCommonInfo = extraData.getStringArray(ACTION_EXTRA_RESULT_INFO);
                        MusicLogUtils.i(TAG, "onVoiceCommandNotified with " + stringCommonInfo);
                        if (stringCommonInfo != null) {
                            showVoiceCommandIndicator(stringCommonInfo);
                        }
                    }
                }
            }
        };*/
    }

    /**
     * M: Show voice command indicator to notify user.
     * 
     * @param stringCommonInfo The voice command stirng info
     */
    private void showVoiceCommandIndicator(String[] stringCommonInfo) {
        int commandLength = stringCommonInfo.length;
        StringBuffer keywords = new StringBuffer(commandLength);
        String lastWord = "\"" + stringCommonInfo[commandLength - 1] + "\"";

        for (int i = 0; i < commandLength - 1; i++) {
            keywords.append("\"").append(stringCommonInfo[i]).append("\"");
            if (i != commandLength - 2) {
                keywords.append(",");
            }
        }
        String indicatorTicker = getString(R.string.voice_command_indicator_ticker, keywords.toString(), lastWord);
        Notification indicatorNotify = new Notification.Builder(getApplicationContext())
                        .setTicker(indicatorTicker)
                        .setContentTitle(getString(R.string.voice_command_indicator_content_title))
                        .setContentText(getString(R.string.voice_command_indicator_content_text))
                        .setSmallIcon(R.drawable.stat_voice).build();
        mNotificationManager.notify(VOICE_COMMAND_INDICATOR, indicatorNotify);
        MusicLogUtils.i(TAG, "showVoiceCommandIndicator with " + indicatorTicker);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_down_out);
    }

    private final class DisplayNextView implements Animation.AnimationListener {
        private final int mPosition;

        private DisplayNextView(int position) {
            mPosition = position;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            mRunAnim = false;
            mContainer.post(new SwapViews(mPosition));
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private void applyRotation(int position, float start, float end) {
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(200);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(position));

        if (mRunAnim) {
            return;
        }
        mRunAnim = true;
        if (position == 0) {
            mAlbum.startAnimation(rotation);
        } else {
            mListParent.startAnimation(rotation);
        }
    }

    private final class SwapViews implements Runnable {
        private final int mPosition;

        public SwapViews(int position) {
            mPosition = position;
        }

        public void run() {
            final float centerX = mContainer.getWidth() / 2.0f;
            final float centerY = mContainer.getHeight() / 2.0f;
            Rotate3dAnimation rotation;
            if (mPosition == 0) {
                mAlbum.setVisibility(View.GONE);
                mListParent.setVisibility(View.VISIBLE);
                mListParent.requestFocus();
                rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 310.0f, false);
            } else {
                mListParent.setVisibility(View.GONE);
                mAlbum.setVisibility(View.VISIBLE);
                mAlbum.requestFocus();
                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 310.0f, false);
            }
            rotation.setDuration(200);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());
            rotation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mAlbum.getAnimation() != null) {
                        mAlbum.clearAnimation();
                    }
                    if (mListParent.getAnimation() != null) {
                        mListParent.clearAnimation();
                    }
                }
            });
            if (mPosition == 0) {
                mListParent.startAnimation(rotation);
            } else {
                mAlbum.startAnimation(rotation);
            }
        }
    }

    public class TrackListAdapter extends SimpleCursorAdapter {
        int mTitleIdx;
        int mArtistIdx;
        int mDurationIdx;
        int mAudioIdIdx;
        AnimationDrawable mAnimPlay = null;

        private final StringBuilder mBuilder = new StringBuilder();
        private String mUnknownArtist;
        private String mUnknownAlbum;

        private MediaPlaybackActivity mActivity = null;
        private String mConstraint = null;
        private boolean mConstraintIsValid = false;
        private TrackQueryHandler mQueryHandler;
        int mIsDrmIdx = -1;
        int mDrmMethodIdx = -1;
        int mTitlePinyinIdx;

        public class ViewHolder {
            TextView text1;
            TextView text2;
            ImageView list_pause_animation;
            CharArrayBuffer buffer1;
            char[] buffer2;
            ImageView list_animation;
        }

        class TrackQueryHandler extends AsyncQueryHandler {

            class QueryArgs {
                public Uri uri;
                public String[] projection;
                public String selection;
                public String[] selectionArgs;
                public String orderBy;
            }

            TrackQueryHandler(ContentResolver res) {
                super(res);
            }

            public Cursor doQuery(Uri uri, String[] projection,
                    String selection, String[] selectionArgs,
                    String orderBy, boolean async) {
                if (async) {
                    Uri limituri = uri.buildUpon().appendQueryParameter("limit", "100").build();
                    QueryArgs args = new QueryArgs();
                    args.uri = uri;
                    args.projection = projection;
                    args.selection = selection;
                    args.selectionArgs = selectionArgs;
                    args.orderBy = orderBy;

                    startQuery(0, args, limituri, projection, selection, selectionArgs, orderBy);
                    return null;
                }
                return MusicUtils.query(mActivity,
                        uri, projection, selection, selectionArgs, orderBy);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                mActivity.init(cursor, cookie != null);
                if (token == 0 && cookie != null && cursor != null && !cursor.isClosed()
                        && cursor.getCount() >= 100) {
                    QueryArgs args = (QueryArgs) cookie;
                    startQuery(1, null, args.uri, args.projection, args.selection,
                            args.selectionArgs, args.orderBy);
                }
            }
        }

        public TrackListAdapter(Context context, MediaPlaybackActivity currentactivity,
                int layout, Cursor cursor, String[] from, int[] to) {
            super(context, layout, cursor, from, to);
            mActivity = currentactivity;
            getColumnIndices(cursor);
            mUnknownArtist = context.getString(R.string.unknown_artist_name);
            mUnknownAlbum = context.getString(R.string.unknown_album_name);
            mQueryHandler = new TrackQueryHandler(context.getContentResolver());
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            ViewHolder vh = new ViewHolder();
            vh.text1 = (TextView) v.findViewById(R.id.text1);
            vh.text2 = (TextView) v.findViewById(R.id.text2);
            vh.list_pause_animation = (ImageView) v.findViewById(R.id.list_pause_animation);
            vh.list_animation = (ImageView) v.findViewById(R.id.list_animation);
            vh.buffer1 = new CharArrayBuffer(100);
            vh.buffer2 = new char[200];
            v.setTag(vh);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder vh = (ViewHolder) view.getTag();
            cursor.copyStringToBuffer(mTitleIdx, vh.buffer1);
            vh.text1.setText(vh.buffer1.data, 0, vh.buffer1.sizeCopied);
            final StringBuilder builder = mBuilder;
            builder.delete(0, builder.length());

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
            vh.text2.setText(vh.buffer2, 0, len);
            ImageView iv = vh.list_animation;
            long id = -1;
            if (MusicUtils.sService != null) {
                try {
                    id = MusicUtils.sService.getQueuePosition();
                } catch (RemoteException ex) {
                }
            }
            if (cursor.getPosition() == id ||
                    (cursor.getLong(mAudioIdIdx) == id)) {
                iv.setVisibility(View.VISIBLE);
                vh.text1.setTextColor(Color.parseColor("#40afff"));
                if (mAnimPlay != null) {
                    mAnimPlay.stop();
                    mAnimPlay = null;
                }
                mAnimPlay = (AnimationDrawable) iv.getBackground();
                if (MusicUtils.isPlaying()) {
                    mAnimPlay.start();
                } else {
                    mAnimPlay.stop();
                }
            } else {
                iv.setVisibility(View.GONE);
                vh.text1.setTextColor(Color.WHITE);
            }
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (mActivity.isFinishing() && cursor != null) {
                cursor.close();
                cursor = null;
            }
            if (cursor != mActivity.mPlayList) {
                mActivity.mTrackCursor = cursor;
                super.changeCursor(cursor);
                getColumnIndices(cursor);
            }
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String s = constraint.toString();
            if (mConstraintIsValid && (
                    (s == null && mConstraint == null) ||
                    (s != null && s.equals(mConstraint)))) {
                return getCursor();
            }
            Cursor c = mActivity.getTrackCursor(mQueryHandler, s, false);
            mConstraint = s;
            mConstraintIsValid = true;
            return c;
        }

        public void setActivity(MediaPlaybackActivity newactivity) {
            mActivity = newactivity;
        }

        public TrackQueryHandler getQueryHandler() {
            return mQueryHandler;
        }

        public void changeCurrentItemStauts(boolean isPlay) {
            if (mAnimPlay != null) {
                if (isPlay) {
                    mAnimPlay.start();
                } else {
                    mAnimPlay.stop();
                }
            }
        }

        public void reloadStringOnLocaleChanges() {
            String sUnknownArtist = mActivity.getString(R.string.unknown_artist_name);
            String sUnknownAlbum = mActivity.getString(R.string.unknown_album_name);
            if (mUnknownArtist != null && !mUnknownArtist.equals(sUnknownArtist)) {
                mUnknownArtist = sUnknownArtist;
            }
            if (mUnknownAlbum != null && !mUnknownAlbum.equals(sUnknownAlbum)) {
                mUnknownAlbum = sUnknownAlbum;
            }
        }

        private void getColumnIndices(Cursor cursor) {
            if (cursor != null) {
                mTitleIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                mDurationIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                try {
                    mAudioIdIdx = cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Playlists.Members.AUDIO_ID);
                } catch (IllegalArgumentException ex) {
                    mAudioIdIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                }

                /*mTitlePinyinIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE_PINYIN_KEY);
                if (MusicFeatureOption.IS_SUPPORT_DRM) {
                    mIsDrmIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_DRM);
                    mDrmMethodIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DRM_METHOD);
                }*/
            }
        }
    }

    public void init(Cursor newCursor, boolean isLimited) {
        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(newCursor);
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.META_CHANGED);
        f.addAction(MediaPlaybackService.QUEUE_CHANGED);
        registerReceiver(mNowPlayingListener, new IntentFilter(f));
        mNowPlayingListener.onReceive(this, new Intent(MediaPlaybackService.META_CHANGED));
    }

    private void initAdapter() {
        mAdapter = (TrackListAdapter) getLastNonConfigurationInstance();
        if (mAdapter == null) {
            mAdapter = new TrackListAdapter(
                    getApplication(),
                    this,
                    R.layout.music_player_nowplaying_list_item,
                    null,
                    new String[] {},
                    new int[] {});
            mPlayList.setAdapter(mAdapter);
            getTrackCursor(mAdapter.getQueryHandler(), null, true);
        } else {
            mAdapter.setActivity(this);
            mAdapter.reloadStringOnLocaleChanges();
            mPlayList.setAdapter(mAdapter);
            mTrackCursor = mAdapter.getCursor();
            if (mTrackCursor != null) {
                init(mTrackCursor, false);
            } else {
                getTrackCursor(mAdapter.getQueryHandler(), null, true);
            }
        }
    }

    private boolean checkDrmRightsForPlay(Cursor cursor, int position, boolean isNowplaying) {
        if (!MusicFeatureOption.IS_SUPPORT_DRM) {
            return true;
        }
        int oldPos = cursor.getPosition();
        cursor.moveToPosition(position);
        int isDrm = 0;//cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_DRM));
        try {
            if (isDrm == 0) {
                return true;
            }
            mCurTrackPos = position;
            //int drmMethod = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DRM_METHOD));

            int rightsStatus = getDrmRightsStatus(cursor, true);
            switch (rightsStatus) {/*
                case OmaDrmStore.RightsStatus.RIGHTS_VALID:
                    if (drmMethod == OmaDrmStore.DrmMethod.METHOD_FL) {
                        return true;
                    }
                    if (!isNowplaying && MusicUtils.sService != null) {
                        long[] list = MusicUtils.getSongListForCursor(cursor);
                        long[] curPlaylist = MusicUtils.sService.getQueue();
                        int curPos = MusicUtils.sService.getQueuePosition();
                        if (Arrays.equals(list, curPlaylist) && position == curPos
                                && !MediaPlaybackService.mTrackCompleted) {
                            return true;
                        }
                    }
                    OmaDrmUiUtils.showConsumeDialog(this, mDrmDialogListener, null);
                    break;
                case OmaDrmStore.RightsStatus.RIGHTS_INVALID:
                    MusicLogUtils.d(TAG, "OmaDrmStore.RightsStatus.RIGHTS_INVALID=");
                    if (drmMethod == OmaDrmStore.DrmMethod.METHOD_FL) {
                        showToast(R.string.fl_invalid);
                        return false;
                    }
                    OmaDrmUiUtils.showRefreshLicenseDialog(mDrmClient, this,
                            getUri(cursor), null);
                    break;
                case OmaDrmStore.RightsStatus.SECURE_TIMER_INVALID:
                    OmaDrmUiUtils.showSecureTimerInvalidDialog(this, null, null);
                    break;
                default:
                    MusicLogUtils.e(TAG, "No such rights status for current DRM file!!");
                    break;
            */}
            return false;
        } catch (Exception re) {//RemoteException
            MusicLogUtils.e(TAG, "RemoteException in service call!");
            return false;
        } finally {
            cursor.moveToPosition(oldPos);
        }
    }

    private Uri getUri(Cursor cursor) {
        int colIdx = -1;
        try {
            colIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (IllegalArgumentException ex) {
            colIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        }
        return ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(colIdx));
    }

    private int getDrmRightsStatus(Cursor cursor, boolean isForTap) {
        Uri uri = null;
        int rightsStatus = -1;
        uri = getUri(cursor);
        try {
            if (isForTap) {
                //rightsStatus = mDrmClient.checkRightsStatusForTap(uri, OmaDrmStore.Action.PLAY);
            } else {
                //rightsStatus = mDrmClient.checkRightsStatus(uri, OmaDrmStore.Action.PLAY);
            }
        } catch (IllegalArgumentException e) {
            MusicLogUtils.e(TAG, "getDrmRightsStatus throw IllegalArgumentException " + e);
        }
        MusicLogUtils.d(TAG, "getDrmRightsStatus: rightsStatus=" + rightsStatus);
        return rightsStatus;
    }

    private Cursor getTrackCursor(TrackListAdapter.TrackQueryHandler queryhandler, String filter,
            boolean async) {

        if (queryhandler == null) {
            throw new IllegalArgumentException();
        }

        Cursor ret = null;
        mSortOrder = "title_pinyin_key";// MediaStore.Audio.Media.TITLE_PINYIN_KEY;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.TITLE + " != ''");
        if (MusicUtils.sService != null) {
            ret = new NowPlayingCursor(MusicUtils.sService, mCursorCols);
        }
        if (ret != null && async) {
            init(ret, false);
        }
        return ret;
    }

    private class NowPlayingCursor extends AbstractCursor {
        public NowPlayingCursor(IMediaPlaybackService service, String[] cols)
        {
            mCols = cols;
            mService = service;
            makeNowPlayingCursor();
        }

        private void makeNowPlayingCursor() {
            if (mCurrentPlaylistCursor != null) {
                mCurrentPlaylistCursor.close();
            }
            // / @}
            mCurrentPlaylistCursor = null;
            try {
                mNowPlaying = mService.getQueue();
            } catch (RemoteException ex) {
                mNowPlaying = new long[0];
            }
            mSize = mNowPlaying.length;
            if (mSize == 0) {
                return;
            }

            StringBuilder where = new StringBuilder();
            where.append(MediaStore.Audio.Media._ID + " IN (");
            for (int i = 0; i < mSize; i++) {
                where.append(mNowPlaying[i]);
                if (i < mSize - 1) {
                    where.append(",");
                }
            }
            where.append(")");

            mCurrentPlaylistCursor = MusicUtils.query(MediaPlaybackActivity.this,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mCols, where.toString(), null, MediaStore.Audio.Media._ID);

            if (mCurrentPlaylistCursor == null) {
                mSize = 0;
                return;
            }

            int size = mCurrentPlaylistCursor.getCount();
            mCursorIdxs = new long[size];
            mCurrentPlaylistCursor.moveToFirst();
            int colidx = mCurrentPlaylistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            for (int i = 0; i < size; i++) {
                mCursorIdxs[i] = mCurrentPlaylistCursor.getLong(colidx);
                mCurrentPlaylistCursor.moveToNext();
            }
            mCurrentPlaylistCursor.moveToFirst();
            mCurPos = -1;

            try {
                int removed = 0;
                for (int i = mNowPlaying.length - 1; i >= 0; i--) {
                    long trackid = mNowPlaying[i];
                    int crsridx = Arrays.binarySearch(mCursorIdxs, trackid);
                    if (crsridx < 0) {
                        removed += mService.removeTrack(trackid);
                    }
                }
                if (removed > 0) {
                    mNowPlaying = mService.getQueue();
                    mSize = mNowPlaying.length;
                    if (mSize == 0) {
                        mCursorIdxs = null;
                        return;
                    }
                }
            } catch (RemoteException ex) {
                mNowPlaying = new long[0];
            }
        }

        @Override
        public int getCount()
        {
            return mSize;
        }

        @Override
        public boolean onMove(int oldPosition, int newPosition)
        {
            if (oldPosition == newPosition)
                return true;

            if (mNowPlaying == null || mCursorIdxs == null || newPosition >= mNowPlaying.length) {
                return false;
            }

            long newid = mNowPlaying[newPosition];
            int crsridx = Arrays.binarySearch(mCursorIdxs, newid);
            mCurrentPlaylistCursor.moveToPosition(crsridx);
            mCurPos = newPosition;

            return true;
        }

        public boolean removeItem(int which)
        {
            try {
                if (mService.removeTracks(which, which) == 0) {
                    return false;
                }
                int i = (int) which;
                mSize--;
                while (i < mSize) {
                    mNowPlaying[i] = mNowPlaying[i + 1];
                    i++;
                }
                onMove(-1, (int) mCurPos);
            } catch (RemoteException ex) {
            }
            return true;
        }

        public void moveItem(int from, int to) {
            try {
                mService.moveQueueItem(from, to);
                mNowPlaying = mService.getQueue();
                onMove(-1, mCurPos);
            } catch (RemoteException ex) {
            }
        }

        private void dump() {
            String where = "(";
            for (int i = 0; i < mSize; i++) {
                where += mNowPlaying[i];
                if (i < mSize - 1) {
                    where += ",";
                }
            }
            where += ")";
            MusicLogUtils.i(TAG, where);
        }

        @Override
        public String getString(int column)
        {
            try {
                return mCurrentPlaylistCursor.getString(column);
            } catch (Exception ex) {
                onChange(true);
                return "";
            }
        }

        @Override
        public short getShort(int column)
        {
            return mCurrentPlaylistCursor.getShort(column);
        }

        @Override
        public int getInt(int column)
        {
            try {
                return mCurrentPlaylistCursor.getInt(column);
            } catch (Exception ex) {
                onChange(true);
                return 0;
            }
        }

        @Override
        public long getLong(int column)
        {
            try {
                return mCurrentPlaylistCursor.getLong(column);
            } catch (Exception ex) {
                onChange(true);
                return 0;
            }
        }

        @Override
        public float getFloat(int column)
        {
            return mCurrentPlaylistCursor.getFloat(column);
        }

        @Override
        public double getDouble(int column)
        {
            return mCurrentPlaylistCursor.getDouble(column);
        }

        @Override
        public int getType(int column) {
            return mCurrentPlaylistCursor.getType(column);
        }

        @Override
        public boolean isNull(int column)
        {
            return mCurrentPlaylistCursor.isNull(column);
        }

        @Override
        public String[] getColumnNames()
        {
            return mCols;
        }

        @Override
        public void deactivate()
        {
            if (mCurrentPlaylistCursor != null)
                mCurrentPlaylistCursor.deactivate();
        }

        @Override
        public boolean requery()
        {
            makeNowPlayingCursor();
            return true;
        }

        @Override
        public void close() {
            super.close();
            if (mCurrentPlaylistCursor != null) {
                mCurrentPlaylistCursor.close();
                mCurrentPlaylistCursor = null;
            }
        }

        private String[] mCols;
        private Cursor mCurrentPlaylistCursor;
        private int mSize;
        private long[] mNowPlaying;
        private long[] mCursorIdxs;
        private int mCurPos;
        private IMediaPlaybackService mService;
    }

    private BroadcastReceiver mNowPlayingListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MediaPlaybackService.META_CHANGED)) {
                mPlayList.invalidateViews();
            } else if (intent.getAction().equals(MediaPlaybackService.QUEUE_CHANGED)) {
                if (MusicUtils.sService == null) {
                    return;
                }
                if (mAdapter != null) {
                    Cursor c = new NowPlayingCursor(MusicUtils.sService, mCursorCols);
                    if (c.getCount() == 0) {
                        Log.i("HHJ", "finish 3002 ");
                        finish();
                        return;
                    }
                    mAdapter.changeCursor(c);
                }
            }
        }
    };

    private OnItemClickListener mPlayListListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            if (mTrackCursor.getCount() == 0) {
                return;
            }
            if (checkDrmRightsForPlay(mTrackCursor, position, false)) {
                MusicUtils.playAll(MediaPlaybackActivity.this, mTrackCursor, position);
                try {
                    if (mAdapter != null) {
                        mAdapter.changeCurrentItemStauts(MusicUtils.sService.isPlaying());
                    }
                } catch (RemoteException ex) {
                }
            }
        }
    };

    private DialogInterface.OnClickListener mDrmDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (mTrackCursor instanceof NowPlayingCursor) {
                    if (MusicUtils.sService != null) {
                        try {
                            MusicUtils.sService.setQueuePosition(mCurTrackPos);
                            return;
                        } catch (RemoteException ex) {
                            MusicLogUtils.e(TAG, "RemoteException when setQueuePosition: ", ex);
                        }
                    }
                } else {
                    MusicUtils.playAll(MediaPlaybackActivity.this, mTrackCursor, mCurTrackPos);
                }
            }
        }
    };

    private void unregisterReceiverSafe(BroadcastReceiver receiver) {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        }
    }
}
