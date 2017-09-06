package com.cappu.launcherwin.speech;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.business.speech.PackageUtils;
import com.iflytek.business.speech.SpeechIntent;
import com.iflytek.business.speech.SpeechServiceUtil;
import com.iflytek.business.speech.SpeechServiceUtil.ISpeechInitListener;
import com.iflytek.business.speech.SynthesizerListener;
import com.iflytek.business.speech.TextToSpeech;
import android.os.ServiceManager;//modify by even 20160420
import com.android.internal.telephony.ITelephony;//modify by even 20160420
import android.telephony.TelephonyManager;//modify by even 20160420

public class SpeechTools{
    private static final String TAG = "SpeechTools";

    public static final String SPEAKER_KEY = "speech_tools_key";

    public static final String SPEAKER_DEFAULT = "3";//"xiaoyan";
    public static final String SPEAKER_YUE_YU = "15";//"xiaomei";
    public static final String SPEAKER_SI_CHUAN = "14";//"xiaorong";
    public static final String SPEAKER_DONG_BEI = "11";//"xiaoqian";
    public static final String SPEAKER_HE_NAN = "25";//"xiaokun";
    public static final String SPEAKER_HU_NAN = "24";//"xiaoqiang";
    public static final String SPEAKER_TAIWAN = "22";//"xiaolin";
    
    private static final String SPEECH_TTS_FILE="/sdcard/.Speechcloud/";

    protected SpeechServiceUtil mSpeechSynthesizer = null;
    private SynthesizerListener.Stub mSpeechListener;

    protected Context mContext;
    private String mSpeechStr;
    private boolean isSpeaking = false;

    public static final int START = 0;
    public static final int STOP = 1;
    private int mSpeechState = -1;

    private String mSpeaker;
    
    private Intent mTtsParamsIntent;

    public SpeechTools(Context context) {
        init(context);
    }

    public SpeechTools(Context context, int type) {
        init(context);
    }

    protected void init(Context context){
        mContext = context;
        mSpeaker = getSpeaker();
        mTtsParamsIntent=getTtsIntent();
        Intent serviceIntent = new Intent();
        serviceIntent.putExtra(SpeechIntent.SERVICE_LOG_ENABLE, true);
        mSpeechSynthesizer = new SpeechServiceUtil(context, mInitListener, serviceIntent);
        Uri uri = Settings.Global.getUriFor(SPEAKER_KEY);
        mContext.getContentResolver().registerContentObserver(uri, true, mSpeakerObserver);
    }

    public void startSpeech(){
        if(!TextUtils.isEmpty(mSpeechStr)){
            startSpeech(mSpeechStr);
        }
    }
    
    //modify by even 20160420		  
    /**
     * 返回电话状态
     * 
     * CALL_STATE_IDLE 无任何状态时 
     * CALL_STATE_OFFHOOK 接起电话时
     * CALL_STATE_RINGING 电话进来时 
     */
    private boolean phoneIsInUse() {
        boolean phoneInUse = false;
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            tm.getCallState();
            if(tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                phoneInUse = false;
            }else{
                phoneInUse = true;
            }             
        }
        return phoneInUse;
    }
    //end

    public void startSpeech(String speechstr){
    	Log.i(TAG,"startSpeech");
        final AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean statusFlag = (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) ? true: false; 

        if(statusFlag){
            return;
        }

        //modify by even 20160420
        //start
        if(phoneIsInUse())
        {
            return;
        }
        //end

          synchronized (this) {
        	Log.i(TAG,"startSpeech ,jeff mSpeaker="+mSpeaker);
        	mTtsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ROLE_CN, mSpeaker);
            mSpeechSynthesizer.speak(speechstr, mTtsParamsIntent);
            mSpeechState = START;
            isSpeaking = true;
        }
    }

    public void stopSpeech(){
        synchronized (this) {
            Log.i(TAG,"stopSpeech");
            mSpeechState = STOP;
            isSpeaking = false;
            mSpeechSynthesizer.stopSpeak();
        }
    }

    public int getSpeechState(){
        return mSpeechState;
    }

    public boolean isSpeaking(){
        return isSpeaking;
    }

    public void destory(){
        mContext.getContentResolver().unregisterContentObserver(mSpeakerObserver);
        synchronized (this) {
        	mSpeechSynthesizer.destroy();
        }
    }
    private Intent getTtsIntent(){
    	Intent ttsParamsIntent = new Intent();
		ttsParamsIntent.putExtra(TextToSpeech.KEY_PARAM_ENGINE_TYPE, TextToSpeech.TTS_ENGINE_LOCAL);
		ttsParamsIntent.putExtra(TextToSpeech. KEY_PARAM_PCM_LOG, true);
		return ttsParamsIntent;
    }


    /**
     * if need  we show set listener for call back
     * no set just use default
     */
    public void setSpeechListener(SynthesizerListener.Stub listener){
        mSpeechListener = listener;
    }

    public void setSpeechContent(String speech){
        mSpeechStr = speech;
    }


    /**
     * 初期化监听。
     */
     private ISpeechInitListener mInitListener = new ISpeechInitListener() {

     	@Override
		public void onSpeechInit(int arg0) {
			// TODO Auto-generated method stub
	        Intent ttsIntent = new Intent();
	        ttsIntent.putExtra(TextToSpeech.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
	   
//	        ttsIntent.putExtra(SpeechIntent.ARG_RES_FILE, SpeechIntent.RES_FROM_ASSETS);
	        ttsIntent.putExtra(SpeechIntent.ARG_RES_TYPE, SpeechIntent.RES_SPECIFIED);
	        ttsIntent.putExtra(SpeechIntent.ARG_RES_FILE, SPEECH_TTS_FILE);
	        ttsIntent.putExtra(TextToSpeech.KEY_PARAM_PCM_LOG, true);
	        if(mSpeechListener == null){
                mSpeechListener = mDefaultSpeechListener;
            }
	        mSpeechSynthesizer.initSynthesizerEngine(mSpeechListener, ttsIntent);
			Log.d("TAG","onSpeechInit");
		}

		@Override
		public void onSpeechUninit() {
			// TODO Auto-generated method stub
			Log.d("TAG","onSpeechUninit");
		}
     };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener.Stub mDefaultSpeechListener = new SynthesizerListener.Stub() {
        
        @Override
		public void onProgressCallBack(int arg0) throws RemoteException {
			Log.d(TAG, "onProgressCallBack");
		}
		
		@Override
		public void onPlayCompletedCallBack(int arg0) throws RemoteException {
//			mService.stopSpeak();
			Log.d(TAG, "onPlayCompletedCallBack");
		}
		
		@Override
		public void onPlayBeginCallBack() throws RemoteException {
			Log.d(TAG, "onPlayBeginCallBack");
		}
		
		@Override
		public void onInterruptedCallback() throws RemoteException {
			Log.d(TAG, "onInterruptedCallback");
		}
		
		@Override
		public void onInit(int arg0) throws RemoteException {
			Log.d(TAG, "onInit");
		}



		@Override
		public void onSpeakPaused() throws RemoteException {
			// TODO Auto-generated method stub
			Log.d(TAG, "onSpeakPaused");
		}

		@Override
		public void onSpeakResumed() throws RemoteException {
			// TODO Auto-generated method stub
			Log.d(TAG, "onSpeakResumed");
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3)
				throws RemoteException {
			Log.d(TAG, "onEvent");
			
		}
    };


    public String getSpeaker(){
        String speaker = Settings.Global.getString(mContext.getContentResolver(), SPEAKER_KEY);
        if(TextUtils.isEmpty(speaker)){
            return SPEAKER_DEFAULT;
        }
        return speaker;
    }

    public void setSpeaker(String speaker){
        mSpeaker = speaker;
    }
    
    public static void setSpeaker(Context context, String speaker){
        Settings.Global.putString(context.getContentResolver(), SPEAKER_KEY, speaker);
    }


    ContentObserver mSpeakerObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange){
            Log.i(TAG,"mSpeakerObserver onChange =" + selfChange);
            Log.i(TAG,"mSpeakerObserver speaker =" + getSpeaker());
            mSpeaker = getSpeaker();
        }
    };
    
    public void setSpeakerListener(SpeakerListener speakerListener){
        this.mSpeakerListener = speakerListener;
    }
    
    SpeakerListener mSpeakerListener;
    public interface SpeakerListener{
        /**播放完成*/
        public void onCompleted();
        /**播放暂停*/
        public void onSpeakPaused();
    }

}