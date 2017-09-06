package com.cappu.launcherwin;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;

import com.cappu.util.Constant;
import com.cappu.widget.CareSettingItem;

public class WorkapaceSettingActivity extends BasicActivity implements View.OnClickListener ,CareSettingItem.SettingItemListener{
    private int mTextSize = 34;
    
    private CareSettingItem workspace_tuoch_layout;
    private CareSettingItem back_tuoch_layout;
    private CareSettingItem negative_screen;
    private CareSettingItem mian_screen_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_tuoch_switch);
        init();
    }

    private void init() {
    	workspace_tuoch_layout = (CareSettingItem) findViewById(R.id.workspace_tuoch_layout);;
    	workspace_tuoch_layout.setItemListener(this);
    	back_tuoch_layout = (CareSettingItem) findViewById(R.id.back_tuoch_layout);;
    	back_tuoch_layout.setItemListener(this);
    	negative_screen = (CareSettingItem) findViewById(R.id.negative_screen);;
    	negative_screen.setItemListener(this);
    	mian_screen_manager = (CareSettingItem) findViewById(R.id.mian_screen_manager);;
    	mian_screen_manager.setItemListener(this);
    	
        if(ThemeManager.getInstance().getCurrentThemeType()!=ThemeManager.THEME_NINE_GRIDS){
        	negative_screen.setVisibility(View.GONE);
        	mian_screen_manager.setVisibility(View.GONE);
        	back_tuoch_layout.setItemStyle(Constant.STYLE_BOTTOM);
        }
        workspace_tuoch_layout.setSelected(Settings.Global.getInt(getContentResolver(), "workspace_tuoch",getResources().getInteger(R.integer.workspace_tuoch))==1);
        back_tuoch_layout.setSelected(Settings.Global.getInt(getContentResolver(), "back_tuoch",getResources().getInteger(R.integer.back_tuoch))==1);
        negative_screen.setSelected(Settings.Global.getInt(getContentResolver(), "add_contacts_screen",0)==1);
    }
    @Override
    public void onItemClick(CareSettingItem v) {
    	if(v == back_tuoch_layout){
    		Settings.Global.putInt(getContentResolver(), "back_tuoch",v.getSelected()?1:0);
        }else if(v == workspace_tuoch_layout){
        	Settings.Global.putInt(getContentResolver(), "workspace_tuoch",v.getSelected()?1:0);
        }else if(v == mian_screen_manager){
            startActivity(new Intent(this, ScreenManagerActivity.class));
        }else if(v == negative_screen){
        	Settings.Global.putInt(getContentResolver(), "add_contacts_screen",v.getSelected()?1:0);
        }
    }
    @Override
    public void onClick(View v) {
        if (v == mCancel) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            int textSize = Settings.Secure.getInt(this.getContentResolver(), "textSize");
            if (textSize == 60) {
                mTextSize = 34;
            } else if (textSize == 50) {
                mTextSize = 30;
            } else if (textSize == 42) {
                mTextSize = 25;
            } else {
                mTextSize = 34;
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        mTitle.setTextSize(mTextSize);
    }

}
