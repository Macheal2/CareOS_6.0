package com.cappu.launcherwin.kookview.music;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.cappu.launcherwin.R;
import com.cappu.launcherwin.kookview.MusicRelativeLayout;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service {
    private MediaPlayer mMediaPlayer;
    private MusicBinder mMusicBinder;
    
    private int mPlayPos = 0;
    private Cursor mCursor;
    
    private Toast mToast;
    private AudioManager mAudioManager;
    
    private static final int TRACK_ENDED = 1;
    private static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FOCUSCHANGE = 4;
    private static final int FADEDOWN = 5;
    private static final int FADEUP = 6;
    private boolean mPausedByTransientLossOfFocus = false;
    private float mCurrentVolume = 1.0f;

    public class MusicBinder extends Binder {
        public void play(){
            MusicService.this.play();
        }
        
        public void pause(){
            MusicService.this.pause();
        }
        
        public void next(){
            MusicService.this.next();
        }
        
        public void prev(){
            MusicService.this.prev();
        }
        
        public boolean isPlaying(){
            return MusicService.this.isPlaying();
        }
        
        void startPlay(String uri) {
            MusicService.this.startPlay(uri);
        }
        
        void stopPlay() {
            MusicService.this.stopPlay();
        }
        
        int getPlayTotal(){
            return MusicService.this.getPlayTotal();
        }
        
        int getPlayCurrentPosition(){
            return MusicService.this.getPlayCurrentPosition();
        }
        
        /**是否循环播放*/
        boolean getIsLooping(){
            return MusicService.this.getIsLooping();
        }
    }
    
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };
    
    private Handler mMediaplayerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    /// M: change volume step from 0.01 to 0.05
                    mCurrentVolume += .05f;
                    if (mCurrentVolume < 1.0f) {
                        /// M: speed up step from 10 to 50
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 50);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    setVolume(mCurrentVolume);
                    break;
                case FOCUSCHANGE:
                     switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            //MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            //MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                            mMediaplayerHandler.removeMessages(FADEUP);
                            mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            //MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            //MusicLogUtils.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                            if(!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                play();
                            } else {
                                mMediaplayerHandler.removeMessages(FADEDOWN);
                                mMediaplayerHandler.sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };
    
    MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (mMediaPlayer != null) {
                next();
            }
        }
    };
    
    void startPlay(String uri) {
        try {
            if(TextUtils.isEmpty(uri)){
                return;
            }
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(uri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            
            Intent i = new Intent(MusicRelativeLayout.CAPPU_NET_MUSIC);
            i.putExtra("artist", getArtistName());
            i.putExtra("album",getAlbumName());
            i.putExtra("track", getTrackName());
            i.putExtra("playing", isPlaying());
            sendBroadcast(i);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    void stopPlay(){
        mMediaPlayer.stop();
    }
    
    
    /**获取播放总时间*/
    int getPlayTotal(){
        return mMediaPlayer.getDuration();
    }
    
    /**获取播放到的时间*/
    int getPlayCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }
    
    /**是否循环播放*/
    boolean getIsLooping(){
        return mMediaPlayer.isLooping();
    }
    
    /**是否正在播放*/
    boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }
    
    void pause(){
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }
    
    void next(){
        mPlayPos ++;
        if(mPlayPos >= mCursor.getCount()){
            mPlayPos = 0;
        }
        startPlay(getUri(mPlayPos));
    }
    
    void prev(){
        mPlayPos --;
        if(mPlayPos <= 0){
            mPlayPos = mCursor.getCount() - 1;
        }
        startPlay(getUri(mPlayPos));
    }
    
    void play(){
        if (AudioManager.AUDIOFOCUS_REQUEST_FAILED == mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
            showToast(getString(R.string.audiofocus_request_failed_message));
            return;
        }
        startPlay(getUri(mPlayPos));
    }
    
    public String getArtistName() {
        synchronized(this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
    
    public String getUri(int position) {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }else{
                try {
                    mCursor.moveToPosition(position);
                    return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                } catch (Exception e) {
                    return null;
                }
                
            }
            
        }
    }
    
    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        }
    }
    
    private void showToast(CharSequence toastText) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
        }
        mToast.setText(toastText);
        mToast.show();
    }

    
    /**获取歌曲列表*/
    public Cursor getList(Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        /*String[] playList;
        if(cursor!=null){
            playList = new String[cursor.getCount()];
        }else{
            playList = new String[0];
        }
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String uri = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            playList[i] = uri;
            i++;
        }*/
        return cursor;
    }
    
    public void setVolume(float vol) {
        mMediaPlayer.setVolume(vol, vol);
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(vol, vol);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(listener);
        mMusicBinder = new MusicBinder();
        mCursor = getList(this);
        mPlayPos = 0;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }
}
