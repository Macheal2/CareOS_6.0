package com.android.mms.ui;

import android.app.Activity;
import android.os.Bundle;

import com.android.mms.R;
import com.cappu.widget.TopBar;
import com.mediatek.setting.GeneralPreferenceActivity;
import com.mediatek.setting.MmsPreferenceActivity;
import com.mediatek.setting.NotificationPreferenceActivity;
import com.mediatek.setting.SmsPreferenceActivity;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import android.content.Intent;

/**
 * Created by lenovo on 15-5-15.
 */
public class CareMmsSettings extends Activity implements OnClickListener{
    private TopBar mTopBar;
    private LinearLayout mCareSmsSettings;
    private LinearLayout mCareMmsSettings;
    private LinearLayout mCareNotifactionSettings;
    private LinearLayout mCareGeneralSettings;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_mms_settings);
        
        mTopBar = (TopBar) findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(new TopBar.onTopBarListener(){
            @Override
            public void onLeftClick(View v){
                CareMmsSettings.this.finish();
            }
    
            @Override
            public void onRightClick(View v){
             
            }

            @Override
            public void onTitleClick(View v){

            }    
        });

        mCareSmsSettings = (LinearLayout) findViewById(R.id.sms_settings);
        mCareSmsSettings.setOnClickListener(this);
    
        mCareMmsSettings = (LinearLayout) findViewById(R.id.mms_settings);
        mCareMmsSettings.setOnClickListener(this);

        mCareNotifactionSettings = (LinearLayout) findViewById(R.id.notification_settings);
        mCareNotifactionSettings.setOnClickListener(this);

        mCareGeneralSettings = (LinearLayout) findViewById(R.id.general_settings);
        mCareGeneralSettings.setOnClickListener(this);
    }  

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.sms_settings:
                Intent smsPreferenceIntent = new Intent(CareMmsSettings.this, SmsPreferenceActivity.class);
            	startActivity(smsPreferenceIntent);
            break;
            case R.id.mms_settings:
                Intent mmsPreferenceIntent = new Intent(CareMmsSettings.this, MmsPreferenceActivity.class);
            	startActivity(mmsPreferenceIntent);	
            break;
            case R.id.notification_settings:
                Intent notificationPreferenceIntent = new Intent(CareMmsSettings.this,NotificationPreferenceActivity.class);
            	startActivity(notificationPreferenceIntent);
            break;
            case R.id.general_settings:
                Intent generalPreferenceIntent = new Intent(CareMmsSettings.this, GeneralPreferenceActivity.class);
            	startActivity(generalPreferenceIntent);
            break;
        }
    } 
}
