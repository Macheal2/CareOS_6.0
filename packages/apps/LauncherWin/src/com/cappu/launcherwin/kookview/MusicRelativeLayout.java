package com.cappu.launcherwin.kookview;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.BubbleView.OnChildViewClick;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.contacts.widget.I99Dialog;
import com.cappu.launcherwin.kookview.music.MusicService;
import com.cappu.launcherwin.kookview.music.MusicService.MusicBinder;
import com.cappu.launcherwin.widget.LauncherLog;

public class MusicRelativeLayout extends RelativeLayout implements OnChildViewClick,OnClickListener{
    private String TAG = "MusicRelativeLayout";
    
    public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    public static final String META_CHANGED = "com.android.music.metachanged";
    public static final String QUEUE_CHANGED = "com.android.music.queuechanged";
    public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";
    public static final String PLAYBACK_COMPLETE = "com.android.music.playbackcomplete";
    public static final String QUIT_PLAYBACK = "com.android.music.quitplayback";
    public static final String ATTACH_AUX_AUDIO_EFFECT = "com.android.music.attachauxaudioeffect";
    public static final String DETACH_AUX_AUDIO_EFFECT = "com.android.music.detachauxaudioeffect";
    
    public static final String CAPPU_NET_MUSIC = "com.cappu.music.change";

    ImageView mPrev;
    ImageView mPlay;
    ImageView mNext;
    TextView mTitle;
    TextView mArtist;
    private ImageView imgNeedle;
    boolean isPlaying;
    ImageView turntable_album;
    private Matrix mMatrix;
    boolean turntableRun = false;
    AlbumRunnable mAlbumRunnable;
    private Context mContext;
    
    /**控制layout*/
    RelativeLayout music_option_relativelayout;
    /**圆盘layout*/
    RelativeLayout music_relativelayout;
    
    com.cappu.music.IMediaPlaybackService mIMediaPlaybackService;
    
    //private MusicBinder mMusicBinder;
    
    private BroadcastReceiver mMusicBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            Log.i(TAG, "广播接收到   intent :"+intent.getAction());
            if("com.cappu.launcher.pagechange".equals(intent.getAction())){
                int page = intent.getIntExtra("page", -1);
                if(page == 2){
                    Message msg = new Message();
                    msg.what = 0;
                    mMediaplayerHandler.sendMessage(msg);
                }
            }else{
                Message msg = new Message();
                msg.obj = intent;
                msg.what = 1;
                mMediaplayerHandler.sendMessage(msg);
                //Log.i(TAG, "广播接收到   intent  artist:"+artist+"  album:"+album+"  track:"+track+"   playing:"+playing);
            }
            
            
        }
    };
    
    private IMediaPlaybackServiceConnection mIMediaPlaybackServiceConnection = new IMediaPlaybackServiceConnection();  
    private final class IMediaPlaybackServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "链接到系统music 成功");
            try {
                mIMediaPlaybackService = com.cappu.music.IMediaPlaybackService.Stub.asInterface(service);
            } catch (Exception e) {
                mIMediaPlaybackService = null;
                //mMusicBinder = null;
            }
            
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIMediaPlaybackService = null;
            //mMusicBinder = null;
        }

    }
    
    BroadcastReceiver mBroadcastReceiver;
    public MusicRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        Log.i(TAG, "MusicRelativeLayout init");
        //bindService(context);
    }

    @Override
    public void onClick(Context c) {
    	LauncherLog.v(TAG, "onClick,jeff");
        //bindService(c);
        Intent intent =  new Intent();
        //intent.setComponent(new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity"));
        intent.setComponent(new ComponentName("com.cappu.music", "com.cappu.music.MusicBrowserActivity"));
        try {
            c.startActivity(intent);
        } catch (Exception e) {
            final I99Dialog delete = new I99Dialog(c);
            delete.setTitle(R.string.i99_contact_function_warning);
            delete.setMessage(R.string.i99_contact_function_under);
            delete.setPositiveButton(R.string.i99_ok, new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    delete.dismiss();
                }
            });
            delete.show();
        }
    }
    private boolean mBindTag=false;
    private void bindService(Context context) {
        try{
			Log.i(TAG, "MusicRelativeLayout bindService");
			if (mIMediaPlaybackService == null && !mBindTag) {
				Intent service;
				service = new Intent("com.cappu.music.MediaPlaybackService");
				service.setPackage("com.cappu.music");
				boolean success = context.bindService(service,
						mIMediaPlaybackServiceConnection,
						Context.BIND_AUTO_CREATE);
				if (success) {
					mBindTag=true;
					IntentFilter commandFilter = new IntentFilter();
					commandFilter.addAction(PAUSE_ACTION);
					commandFilter.addAction(PREVIOUS_ACTION);
					commandFilter.addAction(NEXT_ACTION);
					commandFilter.addAction(PLAYSTATE_CHANGED);
					commandFilter.addAction(META_CHANGED);
					commandFilter.addAction(QUEUE_CHANGED);
					commandFilter.addAction("com.cappu.launcher.pagechange");
					commandFilter.addAction(PLAYBACK_COMPLETE);
					commandFilter.addAction(QUIT_PLAYBACK);
					commandFilter.addAction(ATTACH_AUX_AUDIO_EFFECT);
					commandFilter.addAction(DETACH_AUX_AUDIO_EFFECT);
					Intent intent = context.registerReceiver(
							mMusicBroadcastReceiver, commandFilter);
				}
			}
        }catch(Exception e){
            Log.i(TAG, "MusicRelativeLayout bindService:"+e.toString());
        }
        
    }
    
    private boolean isMyServiceRunning(Context context,String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    protected void onFinishInflate() {
        bindService(getContext());
        mPrev = (ImageView)findViewById(R.id.control_prev);
        mPlay = (ImageView)findViewById(R.id.control_play);
        mNext = (ImageView)findViewById(R.id.control_next);
        mTitle = (TextView)findViewById(R.id.title);
        mArtist = (TextView)findViewById(R.id.artist);
        //hejianfeng add start
        imgNeedle=(ImageView) findViewById(R.id.music_play_needle);
        //hejianfeng add end
        turntable_album = (ImageView) findViewById(R.id.turntable_album);
        if(ThemeManager.getInstance().getCurrentThemeType(getContext()) == ThemeManager.THEME_CHINESESTYLE){
            turntable_album.setVisibility(View.INVISIBLE);
        }else{
            turntable_album.setVisibility(View.VISIBLE); 
        }
        music_relativelayout = (RelativeLayout) findViewById(R.id.music_relativelayout);
        music_option_relativelayout = (RelativeLayout) findViewById(R.id.music_option_relativelayout);
        
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) music_option_relativelayout.getLayoutParams();
        
        if(ThemeManager.getInstance().getCurrentThemeType(mContext) == ThemeManager.THEME_CHINESESTYLE){ //add by wangyang 2016.9.19
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        
        if(turntable_album.getDrawable() != null){
            layoutParams.height = turntable_album.getDrawable().getIntrinsicHeight();
            music_option_relativelayout.setLayoutParams(layoutParams);
        }
        
        mPrev.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mNext.setOnClickListener(this);
        
        if (mMatrix == null) {
            mMatrix = new Matrix();
        } else {
            mMatrix.reset();
        }
        mAlbumRunnable = new AlbumRunnable();
    };
    
    private class AlbumRunnable implements Runnable {
        @Override
        public void run() {
            rotateDialer(1);
            turntable_album.post(this);
        }
    }
    private void rotateDialer(float degrees) {
        mMatrix.postRotate(degrees,turntable_album.getWidth() / 2, turntable_album.getHeight() / 2);
        turntable_album.setImageMatrix(mMatrix);
    }
    public void startRotate(){
        try {
            if(!turntableRun && mIMediaPlaybackService != null && mIMediaPlaybackService.isPlaying()){
                turntable_album.post(mAlbumRunnable);
                turntableRun = true;
            }
        } catch (Exception e) {
            Log.i(TAG, "196 e:"+e.toString());
        }
        
    }
    
    public void stopRotate(){
        turntableRun = false;
        mMatrix.reset();
        turntable_album.setImageMatrix(mMatrix);
        turntable_album.removeCallbacks(mAlbumRunnable);
    }
    
    private Handler mMediaplayerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	//hejianfeng add start
        	int textsize=Settings.Global.getInt(mContext.getContentResolver(),
    				"textSize", mContext.getResources()
    						.getDimensionPixelSize(R.dimen.xl_text_size));
            mArtist.setTextSize(textsize);
            mTitle.setTextSize(textsize);
            //hejianfeng add start
            switch (msg.what) {
            case 1:
                Intent intent;
                try {
                    intent = (Intent) msg.obj;
                } catch (Exception e) {
                    intent = null;
                }
                if(intent == null){
                    stopRotate();
                    if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                		mPlay.setImageResource(R.drawable.music_widget_control_play_btn_nine);
                    }else{
                    	mPlay.setImageResource(R.drawable.music_widget_control_play_btn);
                    }
                    return;
                }
                String artist = intent.getStringExtra("artist");//艺术家
                String album = intent.getStringExtra("album");//专辑
                String track = intent.getStringExtra("track");//歌曲名
                isPlaying = intent.getBooleanExtra("playing",false);//是否播放
                //hejianfeng add start
                if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                	if(!TextUtils.isEmpty(artist)){
                		mArtist.setText(artist+"-");
                	}else{
                		mArtist.setText(mContext.getString(R.string.app_name_music));
                	}
                }else{
                	mArtist.setText(artist);
                }
                //hejianfeng add end
                mTitle.setText(track);
                stopRotate();
                if(isPlaying){
                    startRotate();
                    if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                    	mPlay.setImageResource(R.drawable.music_widget_control_pause_btn_nine);
                    }else{
                    	mPlay.setImageResource(R.drawable.music_widget_control_pause_btn);
                    }
                }else{
                	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                		mPlay.setImageResource(R.drawable.music_widget_control_play_btn_nine);
                    }else{
                    	mPlay.setImageResource(R.drawable.music_widget_control_play_btn);
                    }
                }
                break;

            case 0:
                mMatrix.reset();
                turntable_album.setImageMatrix(mMatrix);
                break;
            default:
                break;
            }
            
        }
    };
    
    long curreetTime = -1;
    
    @Override
    public void onClick(View view) {
        Log.i(TAG, "MusicRelativeLayout onClick turntable_album:"+turntable_album.getWidth());
        //非线上版本用系统music
            try {
                if(System.currentTimeMillis() - curreetTime < 300 ){
                    Toast.makeText(getContext(), "操作速度太快,请休息下", Toast.LENGTH_SHORT).show();
                    curreetTime = System.currentTimeMillis();
                    return;
                }
			curreetTime = System.currentTimeMillis();

			if (mIMediaPlaybackService != null) {
				if (view == mPrev) {
					mIMediaPlaybackService.prev();
				} else if (view == mPlay) {
					Log.i(TAG,
							"music isplaying   mIMediaPlaybackService.isPlaying() :"
									+ mIMediaPlaybackService.isPlaying());
					if (mIMediaPlaybackService.isPlaying()) {
						mIMediaPlaybackService.pause();
					} else {
						mIMediaPlaybackService.play();
					}
				} else if (view == mNext) {
					mIMediaPlaybackService.next();
				}

				if (mIMediaPlaybackService.isPlaying()) {
					startRotate();
					 if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
	                    	mPlay.setImageResource(R.drawable.music_widget_control_pause_btn_nine);
	                    }else{
	                    	mPlay.setImageResource(R.drawable.music_widget_control_pause_btn);
	                    }
				} else {
					stopRotate();
					if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
                		mPlay.setImageResource(R.drawable.music_widget_control_play_btn_nine);
                    }else{
                    	mPlay.setImageResource(R.drawable.music_widget_control_play_btn);
                    }
				}
			} else {
				Log.i(TAG, "music 播放出错  mIMediaPlaybackService is null");
				bindService(getContext());
			}
            } catch (RemoteException e) {
                Log.i(TAG, "music 播放出错 "+e.toString());
            }
        
    }
    
    @Override
    protected void onDetachedFromWindow() {
        //Log.i(TAG, "music layout view 销毁掉 ");
    	//hejianfeng add start
    	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
    		return ;
    	}
    	//hejianfeng add end
		if (mIMediaPlaybackServiceConnection != null
				&& mBindTag ) {
			LauncherLog.v(TAG, "onDetachedFromWindow,jeff unbindService");
			getContext().unbindService(mIMediaPlaybackServiceConnection);
			mIMediaPlaybackService = null;
			mBindTag = false;
		}
        if(mMusicBroadcastReceiver != null){
            try {
                getContext().unregisterReceiver(mMusicBroadcastReceiver);
            } catch (Exception e) {
                Log.i(TAG, "music layout view 销毁掉 "+e.toString());
            }
        }
        
        super.onDetachedFromWindow();
    }
    

}

