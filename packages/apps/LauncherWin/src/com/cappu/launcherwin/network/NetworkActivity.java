package com.cappu.launcherwin.network;


import com.cappu.launcherwin.R;
import com.cappu.launcherwin.R.id;
import com.cappu.launcherwin.R.layout;
import com.cappu.launcherwin.R.string;
import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NetworkActivity extends BasicActivity implements View.OnClickListener {
    
    RelativeLayout netwokSetLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        /*requestWindowFeature(Window.FEATURE_NO_TITLE); */
        setContentView(R.layout.network_main_on);
        
        init();
    }
    
    public void init(){
        netwokSetLayout = (RelativeLayout) findViewById(R.id.netwokSetLayout);
        netwokSetLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == mCancel){
            finish();
        }if(v == netwokSetLayout){
            Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
            startActivity(intent);
        }
    }


}
