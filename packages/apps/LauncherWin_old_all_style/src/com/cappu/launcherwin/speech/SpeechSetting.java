
package com.cappu.launcherwin.speech;

import com.cappu.launcherwin.Launcher;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.widget.CappuDialogUI;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

// add by y.haiyang for local_speech (start)
import android.content.ComponentName;
// add by y.haiyang for local_speech (end)
//hejianfeng add start
import com.cappu.widget.CareSettingItem;
//hejianfeng add end

public class SpeechSetting extends BasicActivity implements CareSettingItem.SettingItemListener ,View.OnClickListener {

    ConnectivityManager mConnectivityManager;

    int mTextSize;

    CareSettingItem contacts_layout;
    CareSettingItem mms_layout;
    CareSettingItem launcher_layout;
    CareSettingItem weather_layout;
    CareSettingItem dialpad_layout;

    //added by yzs for time speech begin
    CareSettingItem mTimeSpeech;
    //added by yzs for time speech end

    // add by y.haiyang local speech(start)
    RelativeLayout mLocalSpeech;
    // add by y.haiyang local speech(end)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_setting);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mTextSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
        init();
    }

    private void init() {
        
        contacts_layout = (CareSettingItem) findViewById(R.id.contacts_layout);
        mms_layout = (CareSettingItem) findViewById(R.id.mms_layout);
        launcher_layout = (CareSettingItem) findViewById(R.id.launcher_layout);
        weather_layout = (CareSettingItem) findViewById(R.id.weather_layout);
        dialpad_layout = (CareSettingItem) findViewById(R.id.dialpad_layout);

        // add by y.haiyang for local settings (start)
        mLocalSpeech = (RelativeLayout)findViewById(R.id.local_speech);
        mLocalSpeech.setOnClickListener(this);
        // add by y.haiyang for local settings (end)
        contacts_layout.setItemListener(this);
        mms_layout.setItemListener(this);
        launcher_layout.setItemListener(this);
        weather_layout.setItemListener(this);
        dialpad_layout.setItemListener(this);
        
        //added by yzs for time speech begin 
        mTimeSpeech = (CareSettingItem) findViewById(R.id.talktime_layout);
        mTimeSpeech.setItemListener(this);       
        setTimeStatusSwitch();

        if(isZh()){            
            mTimeSpeech.setVisibility(View.VISIBLE);
        }else{
            setTimeSpeech(false);
            mTimeSpeech.setVisibility(View.GONE);
        }
        //added by yzs for time speech end
        
        setContactsStatusSwitch();
        setMmsStatusSwitch();
        setLauncherStatusSwitch();
        setDialpadStatusSwitch();
        setWeatherStatusSwitch();
        
    }

    private boolean isZh() {
        java.util.Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }
    @Override
    public void onItemClick(CareSettingItem v) {
    	if(v == launcher_layout){
            if(getLauncherStatus()){
                setLauncherStats(false);
            }else{
                setLauncherStats(true);
            }
            setLauncherStatusSwitch();
        }else if(v == contacts_layout){
            if(getContactsStatus()){
                setContactsStatsSwitch(false);
            }else{
                setContactsStatsSwitch(true);
            }
            
            setContactsStatusSwitch();
        }else if(v == mms_layout ){
            if(getMmsStatus()){
                setMmsStatus(false);
            }else{
                setMmsStatus(true);
            }
            
            setMmsStatusSwitch();
        }else if(v == weather_layout){
            if(getWeatherStatus()){
                setWeatherStats(false);
            }else{
                setWeatherStats(true);
            }
            setWeatherStatusSwitch();
        }else if(v == dialpad_layout){
            if(getDialpadStatus()){
                setDialpadStatus(false);
            }else{
                setDialpadStatus(true);
            }
            setDialpadStatusSwitch();
        }

        //added by yzs for time speech begin
        else if(v == mTimeSpeech){
            if(getTimeSpeech()){
                setTimeSpeech(false);
            }else{
                setTimeSpeech(true);
            }
            setTimeStatusSwitch();
        }
        //added by yzs for time speech end
    }
    @Override
    public void onClick(View v) {

        // add by y.haiyang for local speech (start)
        if(v == mLocalSpeech){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            ComponentName cn = new ComponentName("com.magcomm.speech", "com.magcomm.speech.Settings");
            intent.setComponent(cn);
            startActivity(intent);
        }else if (v == mCancel) {
            finish();
        }
        // add by y.haiyang for local speech (end)
        else{
            CappuDialogUI dialog = new CappuDialogUI(this, CappuDialogUI.DIALOG_STYLE_TWO_BUTTONS);
            dialog.setTitle(R.string.i99_dialog_confirm_title);
            dialog.setMessage(R.string.function_is_not_supported_tip);
            dialog.setPositiveButton(R.string.i99_dialog_right, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    try {
                        Intent it = new Intent();
                        it.setData(Uri.parse("http://www.cappu.com/"));
                        it.setAction(Intent.ACTION_VIEW);
                        startActivity(it);
                    } catch (Exception e) {
                        Toast.makeText(SpeechSetting.this, getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
                    }
                }
            });
            dialog.show();
        }
    }
    

    //added by yzs for time speech begin
    public void setTimeSpeech(boolean state){
        Settings.Global.putInt(getContentResolver(),"talking_clock", state ? 1 : 0);
    }

    private boolean getTimeSpeech(){
        return Settings.Global.getInt(getContentResolver(), "talking_clock",0) == 1 ? true : false;
    }

    public void setTimeStatusSwitch(){
        if(getTimeSpeech()){
        	mTimeSpeech.setSelected(true);
        }else{
        	mTimeSpeech.setSelected(false);
        }
    }
    //added by yzs for time speech end

    public void setDialpadStatus(boolean state){
        Settings.Global.putInt(getContentResolver(), "dialpad_speech_status", state?1:0);
    }
    
    public boolean getDialpadStatus(){
        return Settings.Global.getInt(getContentResolver(), "dialpad_speech_status", getResources().getInteger(R.integer.mms_speech_status)) == 1?true:false;
    }
    
    public void setDialpadStatusSwitch(){
        if(getDialpadStatus()){
        	dialpad_layout.setSelected(true);
        }else{
        	dialpad_layout.setSelected(false);
        }
    }
    
    /*短信*/
    public void setMmsStatus(boolean state){
        Settings.Global.putInt(getContentResolver(), "mms_speech_status", state?1:0);
    }
    
    public boolean getMmsStatus(){
        return Settings.Global.getInt(getContentResolver(), "mms_speech_status", getResources().getInteger(R.integer.mms_speech_status)) == 1?true:false;
    }
    
    public void setMmsStatusSwitch(){
        if(getMmsStatus()){
        	mms_layout.setSelected(true);
        }else{
        	mms_layout.setSelected(false);
        }
    }
    
    public void setWeatherStats(boolean state){
        Settings.Global.putInt(getContentResolver(), BasicKEY.WEATHER_SPEECH_STATUS, state?1:0);
    }
    
    public void setLauncherStats(boolean state){
        Settings.Global.putInt(getContentResolver(), "launcher_speech_status", state?1:0);
    }
    
    public void setLauncherStatusSwitch(){
        if(getLauncherStatus()){
        	launcher_layout.setSelected(true);
        }else{
        	launcher_layout.setSelected(false);
        }
    }
    
    public void setWeatherStatusSwitch(){
        if(getWeatherStatus()){
        	weather_layout.setSelected(true);
        }else{
        	weather_layout.setSelected(false);
        }
    }
    
    public boolean getWeatherStatus(){
        return Settings.Global.getInt(getContentResolver(), BasicKEY.WEATHER_SPEECH_STATUS, getResources().getInteger(R.integer.weather_speech_status)) == 1?true:false;
    }
    
    public boolean getLauncherStatus(){
        return Settings.Global.getInt(getContentResolver(), "launcher_speech_status", getResources().getInteger(R.integer.launcher_speech_status)) == 1?true:false;
    }
    
    public void setContactsStatusSwitch(){
        if(getContactsStatus()){
        	contacts_layout.setSelected(true);
        }else{
        	contacts_layout.setSelected(false);
        }
    }
    
    public void setContactsStatsSwitch(boolean state){
        Settings.Global.putInt(getContentResolver(), "contacts_speech_status", state?1:0);
    }
    
    public boolean getContactsStatus(){
        return Settings.Global.getInt(getContentResolver(), "contacts_speech_status", getResources().getInteger(R.integer.contacts_speech_status)) == 1?true:false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTitle.setTextSize(mTextSize);
    }

}
