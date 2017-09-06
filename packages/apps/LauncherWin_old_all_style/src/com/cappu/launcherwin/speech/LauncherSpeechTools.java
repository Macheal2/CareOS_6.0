package com.cappu.launcherwin.speech;

import com.cappu.launcherwin.basic.BasicKEY;

import android.content.Context;
import android.media.AudioManager;


public class LauncherSpeechTools extends SpeechTools{
    private static final String TAG = "LauncherSpeechTools";

    public LauncherSpeechTools(Context context) {
        super(context);
    }

    public boolean isAudioActive() {
        final AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean statusFlag = (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) ? true: false; 
        
        if(statusFlag == true){
            return false;
        }

        if(BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){
            if(am == null){
                return false;
            }
        }
        
        return true;
    }

    public void startSpeech(String speech,boolean canSpeech){

        if(!canSpeech){
            return;
        }

        if(!isAudioActive()){
            return;
        }else{
            super.startSpeech(speech);
        }

    }

    public void onDestroy() {
        super.stopSpeech();
        super.destory();
    }

    public void stopSpeaking(){
        super.stopSpeech();
    }
}
