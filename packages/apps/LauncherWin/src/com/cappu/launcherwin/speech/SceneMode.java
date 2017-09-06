
package com.cappu.launcherwin.speech;

import com.cappu.launcherwin.R;
//import com.cappu.launcherwin.tools.KookSharedPreferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

public class SceneMode extends BroadcastReceiver {

    ConnectivityManager mConnectivityManager;

    //Context mTContext;
    
    Context mContext;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Log.i("HHJ", " audio :" +audio.getRingerMode());
        switch (audio.getRingerMode()) {
            /**声音模式*/
            case AudioManager.RINGER_MODE_NORMAL:
                //Log.i("HHJ", " SceneMode intent 声音模式:");
                
                if(getRingeMode() == 0){
                    Log.i("HHJ", "getRingeMode is 0");
                    setRingeMode(1);
                }else{
                    break;
                }
                
                Log.i("HHJ", " SceneMode intent getDialpadMode():"+getDialpadMode()+"    getDialpadStatus():"+getDialpadStatus());
                if(getDialpadMode() == 1){
                    setDialpadMode(0);
                    if(!getDialpadStatus()){//当置为静音后应该是false；如果说getDialpadStatus() 获取是true 说明人为去设置过.那么不需要去理会了
                        setDialpadStatus(true);
                    }
                }
                
                if(getLauncherMode() == 1){
                    setLauncherMode(0);
                    if(!getLauncherStatus()){
                        setLauncherStats(true);
                    }
                }
                
                if(getContactsMode() == 1){
                    setContactsMode(0);
                    if(!getContactsStatus()){
                        setContactsStatsSwitch(true);
                    }
                }
                
                if(getMmsMode() == 1){
                    setMmsMode(0);
                    if(!getMmsStatus()){
                        setMmsStatus(true);
                    }
                }
                
                break;
                /**静音模式  0 */
            case AudioManager.RINGER_MODE_SILENT:
                //Log.i("HHJ", " SceneMode intent 静音模式:");
              /**震动模式    1   */
            case AudioManager.RINGER_MODE_VIBRATE:
               // Log.i("HHJ", " SceneMode intent 震动模式:");
                
                if(getRingeMode() == 1){
                    Log.i("HHJ", "getRingeMode is 1");
                    setRingeMode(0);
                }else{
                    break;
                }
                
                
                if(getDialpadStatus()){
                    setDialpadStatus(false);
                    setDialpadMode(1);
                }else{
                    setDialpadMode(0);
                }
                
                Log.i("HHJ", " getLauncherStatus():"+getLauncherStatus());
                if(getLauncherStatus()){
                    setLauncherStats(false);
                    setLauncherMode(1);
                }else{
                    setLauncherMode(0);
                }
                
                if(getContactsStatus()){
                    setContactsStatsSwitch(false);
                    setContactsMode(1);
                }else{
                    setContactsMode(0);
                }
                
                if(getMmsStatus()){
                    setMmsStatus(false);
                    setMmsMode(1);
                }else{
                    setMmsMode(0);
                }
                
                break;
        }

    }
    
    public void setRingeMode(int mode){
        Settings.Global.putInt(mContext.getContentResolver(), "ring_mode",mode);
        
    }
    
    public int getRingeMode(){
        return Settings.Global.getInt(mContext.getContentResolver(), "ring_mode",1);
        
    }
    
    
    public void setDialpadStatus(boolean state){
        Settings.Global.putInt(mContext.getContentResolver(), "dialpad_speech_status",state?1:0);
        
    }
    
    public void setDialpadMode(int mode){
        Settings.Global.putInt(mContext.getContentResolver(), "dialpad_speech_mode",mode);
        
    }
    
    public int getDialpadMode(){
        return Settings.Global.getInt(mContext.getContentResolver(), "dialpad_speech_mode",0);
    }
    
    public boolean getDialpadStatus(){
        return Settings.Global.getInt(mContext.getContentResolver(), "dialpad_speech_status",0) == 1?true:false;
    }
    
    
    /*短信*/
    public void setMmsStatus(boolean state){
        Settings.Global.putInt(mContext.getContentResolver(), "mms_speech_status",state?1:0);
        
    }
    public void setMmsMode(int mode){
        Settings.Global.putInt(mContext.getContentResolver(), "mms_speech_mode",mode);
    }
    
    public int getMmsMode(){
        return Settings.Global.getInt(mContext.getContentResolver(), "mms_speech_mode",0);
    }
    
    public boolean getMmsStatus(){
        return Settings.Global.getInt(mContext.getContentResolver(), "mms_speech_status",0)== 1?true:false;
    }
    
    
    public void setLauncherStats(boolean state){
        Settings.Global.putInt(mContext.getContentResolver(), "launcher_speech_status", state?1:0);
    }
    
    public void setLauncherMode(int mode){
        Settings.Global.putInt(mContext.getContentResolver(), "launcher_speech_mode",mode);
        
    }
    
    public int getLauncherMode(){
        return Settings.Global.getInt(mContext.getContentResolver(), "launcher_speech_mode",0);
        
    }
    
    
    public boolean getLauncherStatus(){
        return Settings.Global.getInt(mContext.getContentResolver(), "launcher_speech_status",0) == 1?true:false;
        
    }
    
    
    public void setContactsStatsSwitch(boolean state){
        Settings.Global.putInt(mContext.getContentResolver(), "contacts_speech_status",state?1:0);
    }
    
    public void setContactsMode(int mode){
        Settings.Global.putInt(mContext.getContentResolver(), "contacts_speech_mode",mode);
        
    }
    
    public int getContactsMode(){
        return Settings.Global.getInt(mContext.getContentResolver(), "contacts_speech_mode",0);
    }
    
    public boolean getContactsStatus(){
        return Settings.Global.getInt(mContext.getContentResolver(), "contacts_speech_status",0) == 1?true:false;
    }
}
