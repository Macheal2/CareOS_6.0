package com.cappu.launcherwin;


import java.lang.reflect.Method;

import android.app.Application;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.contacts.widget.I99Dialog;
import com.cappu.launcherwin.downloadUI.DownloadCenter;
import com.cappu.launcherwin.downloadUI.celllayout.DownloadCellLayoutMainActivity;
import com.cappu.launcherwin.install.APKInstallTools;
import com.cappu.launcherwin.network.GprsPwdActivity;
import com.cappu.launcherwin.speech.SpeechSetting;
import com.cappu.launcherwin.tools.AppComponentNameReplace;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.CareDialog;
import com.cappu.launcherwin.widget.I99ThemeToast;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.widget.CappuDialogUI;
import com.cappu.launcherwin.widget.TopBar;

//dengying@20150408 wifi open gprs status update begin
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.NetworkInfo.State;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
//dengying@20150408 wifi open gprs status update end
import android.telephony.SubscriptionManager;//zhaogagnzhu

//modify by even hall menu
import android.os.SystemProperties;
//hejianfeng add start
import com.cappu.widget.CareSettingItem;
//hejianfeng add end

public class LauncherSettingActivity extends BasicActivity implements
        CareSettingItem.SettingItemListener, OnClickListener {
    private static final String TAG = "Lcappu_launcher_settingauncherSettingActivity";
    
    private CareSettingItem wifi_layout;
    private CareSettingItem gprs_layout;
    
    private CareSettingItem textsize_layout;
    
    private CareSettingItem theme_layout;
    
    private CareSettingItem wallpaper_layout;
    
    private CareSettingItem common_layout;
    private CareSettingItem voice_layout;
    
    private CareSettingItem workspace_lock_layout;
    
    //added by yzs for low battery warring begin
    private CareSettingItem mLowBattery;
    //added by yzs for low battery warring end

    //dengying speaker begin
    private CareSettingItem speaker_layout;
    //dengying speaker end
    
    //dengying home_key_wake_up begin
    private CareSettingItem home_key_wake_up_layout;
    //dengying home_key_wake_up end    

/* Cappu:liukun on: Tue, 15 Aug 2017 10:11:04 +0800
 * TODO: 在桌面设置里面添加指示灯开关
 */
    private CareSettingItem indicator_light_layout;
// End of Cappu:liukun
    
//zhouhua start
  //  private CareSettingItem bootaudio_setting_layout;
//zhouhua end 
    
   // zhouhua start
    private CareSettingItem about_phone_icon_layout;
 // zhouhua end
    private CareSettingItem android_layout; 
    private CareSettingItem gprs_setting_layout;
    
    private CareSettingItem workspace_switch_layout; 
    
    // added by jiangyan start
    private CareSettingItem clock_style_choice_layout;
    // added by jiangyan end
    private CareSettingItem about_layout;
    
    private ConnectivityManager mConnectivityManager;
    
    private Dialog mSimDialog = null;

    private Handler mHandler = new Handler();
    
    private int mTextSize = 34;;
    
    private PopupWindow mPopupWindowGprs;
    private LinearLayout mGprsDialogView;
    private Button dialog_cancel;
    private Button dialog_confirm;
    
    private AppComponentNameReplace mAppComponentNameReplace;
    
    private SharedPreferences mPrefs;
    private GPRSIntentReceiver mGPRSIntentReceiver;
    private MyHandler myHandler;//dengying@20150408 wifi open gprs status update
    
    /**文字大小dialog*/
    private static final int FONT_SIZE_DIALOG = 1;
  //zhouhua start
    private final String BOOTAUDIO_PROPERTY = "persist.sys.bootaudio";
    private final String SELECT_PROPERTY_ON = "1";
    private final String SELECT_PROPERTY_OFF = "0";
    // zhouhua end
    private CareDialog.Builder mThemeEBuilder;
    private CareDialog mThemeDialog;
    
    private CareDialog.Builder mFontEBuilder;
    private CareDialog mFontDialog;
    
    private LayoutInflater mLayoutInflater;
    
    private View mThemeView;
    private View mModeView;
    private View mFontView;
    
    private CareSettingItem movement_layout; 
    private TelephonyManager mTelephonyManager;//zhaogangzhu
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cappu_launcher_setting);
        
        myHandler = new MyHandler(); //dengying@20150408 wifi open gprs status update
        
        if (mGPRSIntentReceiver == null) {
            mGPRSIntentReceiver = new GPRSIntentReceiver();
            IntentFilter filter = new IntentFilter();  
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
            filter.addAction("android.net.conn.CONNECTIVITY_CANCEL");//dengying@20150408 wifi open gprs status update
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGEING"); 
            registerReceiver(mGPRSIntentReceiver, filter);
        }
        
        mAppComponentNameReplace = new AppComponentNameReplace(this);
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);//zhaogagnzhu add
        init();
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mGPRSIntentReceiver != null) {
            try {
                unregisterReceiver(mGPRSIntentReceiver);
            } catch (Exception e) {
                Log.i("HHJ", "释放注册失败 e:");
            }
        }
	}

    private void init() {
        mGprsDialogView = (LinearLayout) View.inflate(this, R.layout.gprs_dialog, null);
        mGprsDialogView.setOnKeyListener(new android.view.View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindowGprs.isShowing())) {
                    Log.i("HHJ", "129 setOnKeyListener");
                    mPopupWindowGprs.dismiss();  
                    return true;
                }
                return false;
            }
        });
        
        mPopupWindowGprs = new PopupWindow(mGprsDialogView, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        mPopupWindowGprs.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_default));
        mPopupWindowGprs.setFocusable(true);
        mPopupWindowGprs.setAnimationStyle(R.style.menushow);
        mPopupWindowGprs.update();
        mGprsDialogView.setFocusableInTouchMode(true);
        
        
        dialog_cancel = (Button) mGprsDialogView.findViewById(R.id.dialog_cancel);
        dialog_confirm = (Button) mGprsDialogView.findViewById(R.id.dialog_confirm);
        dialog_cancel.setOnClickListener(this);
        dialog_confirm.setOnClickListener(this);
        
        
        wifi_layout = (CareSettingItem) findViewById(R.id.wifi_layout);
        
        gprs_layout = (CareSettingItem) findViewById(R.id.gprs_layout);
        wifi_layout.setItemListener(this);
        gprs_layout.setItemListener(this);
        if(gprsIsOpenMethod("getMobileDataEnabled")){
        	gprs_layout.setSelected(true);
        }else{
        	gprs_layout.setSelected(false);
        }
        
        movement_layout = (CareSettingItem) findViewById(R.id.movement_layout);//dengying@20150117 movement
        
        if(movement_layout != null){
            Log.i("HHJ", "movement_layout not null");
            movement_layout.setItemListener(this);
        }else{
            Log.i("HHJ", "movement_layout is null");
        }
        
        textsize_layout = (CareSettingItem) findViewById(R.id.textsize_layout);
        textsize_layout.setItemListener(this);
        
        theme_layout = (CareSettingItem) findViewById(R.id.theme_switch_layout);;
        theme_layout.setItemListener(this);
        
        wallpaper_layout = (CareSettingItem) findViewById(R.id.wallpaper_layout);;
        wallpaper_layout.setItemListener(this);
        
        common_layout = (CareSettingItem) findViewById(R.id.common_layout);
        voice_layout = (CareSettingItem) findViewById(R.id.voice_layout);
        
        workspace_lock_layout  = (CareSettingItem) findViewById(R.id.workspace_lock_layout);
        setWorkspaceLock();
        if(workspace_lock_layout != null){
            workspace_lock_layout.setItemListener(this);
        }
        // added by jiangyan start
        clock_style_choice_layout = (CareSettingItem) findViewById(R.id.clock_style_choice_layout);
        if (clock_style_choice_layout != null) {
            clock_style_choice_layout.setItemListener(this);
        }
        // added by jiangyan end

        // added by yzs for low Battery warrring begin
        mLowBattery = (CareSettingItem) findViewById(R.id.setting_low_battery);
        mLowBattery.setItemListener(this);
        //added by yzs for low Battery warrring end

        //dengying speaker begin
        speaker_layout  = (CareSettingItem) findViewById(R.id.speaker_layout);
        setSpeaker();
        if(speaker_layout != null){
        	speaker_layout.setItemListener(this);
        }
        //dengying speaker end
        
        //dengying home_key_wake_up begin
        home_key_wake_up_layout  = (CareSettingItem) findViewById(R.id.home_key_wake_up_layout);
        setHomeKeyWakeUp();
        if(home_key_wake_up_layout != null){
        	home_key_wake_up_layout.setItemListener(this);
         }
        //dengying home_key_wake_up end        


/* Cappu:liukun on: Tue, 15 Aug 2017 10:12:32 +0800
 * TODO: 在桌面设置里面添加指示灯开关
 */
        indicator_light_layout = (CareSettingItem) findViewById(R.id.indicator_light_layout);
        setIndicatorLight();
        if(indicator_light_layout != null){
            indicator_light_layout.setItemListener(this);
        }
// End of Cappu:liukun
        
       //zhouhua start
       /* bootaudio_setting_layout  = (CareSettingItem) findViewById(R.id.bootaudio_setting_layout);

		if (bootaudio_setting_layout != null) {
			bootaudio_setting_layout.setItemListener(this);
		}

		String on = getBootAudioSystemProperities();
		if (on.equals("1")) {
			updateBootAudioStatus(true);
		} else {
			updateBootAudioStatus(false);
		}*/
        //zhouhua end 
       

        
        if(common_layout != null){
            common_layout.setItemListener(this);
        }
        if(voice_layout != null){
            voice_layout.setItemListener(this);
        }
        
        workspace_switch_layout = (CareSettingItem) findViewById(R.id.workspace_switch_layout);
        if(workspace_switch_layout != null){
            workspace_switch_layout.setItemListener(this);
        }
        
        android_layout = (CareSettingItem) findViewById(R.id.android_layout);
        android_layout.setItemListener(this);
          //zhouhua start
       about_phone_icon_layout= (CareSettingItem) findViewById(R.id.about_phone_layout);
        about_phone_icon_layout.setItemListener(this);
        // zhouhua end
        
        gprs_setting_layout = (CareSettingItem) findViewById(R.id.gprs_setting_layout);
        if(gprs_setting_layout!= null){
            gprs_setting_layout.setItemListener(this);
        }
        
        about_layout = (CareSettingItem) findViewById(R.id.about_layout);
        if(about_layout != null && BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER){
            about_layout.setVisibility(View.GONE);
        }
        
    }
    
    public void setWorkspaceLock(){
        if(Settings.Global.getInt(getContentResolver(), "workspace_lock",getResources().getInteger(R.integer.workspace_lock)) == 0){
            workspace_lock_layout.setSelected(false);
        }else{
        	workspace_lock_layout.setSelected(true);
        }
    }
    
    //dengying home_key_wake_up begin
    public void setHomeKeyWakeUp(){
        if(Settings.Global.getInt(getContentResolver(), "home_key_wake_up",1) == 0){
        	home_key_wake_up_layout.setSelected(false);
        	Settings.Global.putInt(getContentResolver(), "home_key_wake_up", 0);
        }else{
        	home_key_wake_up_layout.setSelected(true);
        	Settings.Global.putInt(getContentResolver(), "home_key_wake_up", 1);
        }
    }
    //dengying home_key_wake_up end


/* Cappu:liukun on: Tue, 15 Aug 2017 13:36:26 +0800
 * TODO:在桌面设置里面添加指示灯开关 
 */
    public void setIndicatorLight(){
        if(Settings.Global.getInt(getContentResolver(), "indicator_light_layout",1) == 0){
            indicator_light_layout.setSelected(false);
            Settings.Global.putInt(getContentResolver(), "indicator_light_layout", 0);
        }else{
            indicator_light_layout.setSelected(true);
            Settings.Global.putInt(getContentResolver(), "indicator_light_layout", 1);
        }
    }
// End of Cappu:liukun
    
    //dengying speaker begin
    public void setSpeaker(){
        if(Settings.Global.getInt(getContentResolver(), "speaker",1) == 0){
        	speaker_layout.setSelected(false);
        	MyFile.CloseSpeaker(this);
        }else{
        	speaker_layout.setSelected(true);
        	MyFile.OpenSpeaker(this);
        }
    }
    //dengying speaker end
    
    //dengying@20150609 begin
  /*  public void updateBootAudioStatus(boolean isOpen){
        if(isOpen){
            bootaudio_setting_layout.setSelected(true);
        }else{
        	bootaudio_setting_layout.setSelected(false);
        }
    }*/
    //dengying@20150609 end 
    
    @Override
    protected void onResume() {
        super.onResume();
        
        int textSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
        if(textSize == getResources().getDimensionPixelSize(R.dimen.l_text_size)){
        	textsize_layout.setSubContent(R.string.font_level_small);
        }else if(textSize == getResources().getDimensionPixelSize(R.dimen.xl_text_size)){
        	textsize_layout.setSubContent(R.string.font_level_big);
        }else if(textSize == getResources().getDimensionPixelSize(R.dimen.xxl_text_size)
        		||textSize == getResources().getDimensionPixelSize(R.dimen.china_xxl_text_size)){
        	textsize_layout.setSubContent(R.string.font_level_big_most);
        }else{
        	textsize_layout.setSubContent(R.string.font_level_big);
        }
        mTitle.setTextSize(mTextSize);
        
        int themeType = Settings.Global.getInt(getContentResolver(), ThemeManager.ACTION_THEME_KEY, ThemeManager.THEME_DEFUALT);
        if(themeType == ThemeManager.THEME_COLORFUL){
        	theme_layout.setSubContent(R.string.theme_xuan_cai);
        }else if(themeType == ThemeManager.THEME_CHINESESTYLE){
        	theme_layout.setSubContent(R.string.theme_select_chinesestyle);
        }
        //hejianfeng add start for 3x3
        else if(themeType==ThemeManager.THEME_NINE_GRIDS){
        	theme_layout.setSubContent(R.string.theme_select_nine_grids);
        }
        if(themeType==ThemeManager.THEME_NINE_GRIDS){
        	wallpaper_layout.setVisibility(View.VISIBLE);
        }else{
        	wallpaper_layout.setVisibility(View.GONE);
        }
        //hejianfeng add end
        
    }

    @Override
    public void onItemClick(CareSettingItem v) {
		if (v == wifi_layout) {
			Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
			startActivity(intent);
		} else if (v == gprs_layout) {
			if (!isSimOk()) {
				Log.v(TAG, "onItemClick,jeff isSimOk()");
				showSimDialog();
				gprs_layout.setSelected(false);
				return;
			}

			if (gprsIsOpenMethod("getMobileDataEnabled")) {
				gprs_layout.setSelected(false);
				if (mTelephonyManager != null) {
					Intent intent = new Intent("android.net.conn.SETDATA_OFF");// zhaogangzhu@20150929
					this.sendBroadcast(intent);
				}
			} else {
				gprs_layout.setSelected(false);// dengying@20150408 wifi open
												// gprs status update
				if (mTelephonyManager != null) {
					Intent intent = new Intent("android.net.conn.SETDATA_ON");// zhaogangzhu@20150929
					this.sendBroadcast(intent);
				}
			}
		} else if (v == textsize_layout) {
			mFontDialog = (CareDialog) onCreateDialog(FONT_SIZE_DIALOG);
			mFontDialog.show();
		} else if (v == movement_layout) {// dengying@20150117 movement
			try {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(
						"com.cappu.careos.parent",
						"com.cappu.careos.parent.SettingActivity"));// wangyang
				startActivity(intent);
			} catch (Exception e) {
				ActivityNotFind();
			}

		} else if (v == theme_layout) {

			Intent intent = new Intent(this, ThemeSelectActivity.class);
			startActivity(intent);
		} else if (v == wallpaper_layout) {
			startActivity(new Intent(this, ThemeGridActivity.class));
		} else if (v == common_layout) {
			Intent intent = new Intent(this, RingtoneActivity.class);
			startActivity(intent);
			Toast.makeText(this,
					getString(R.string.i99_dialog_confirm_set_ring),
					Toast.LENGTH_LONG).show(); // zhouhua
		} else if (v == voice_layout) {
			try {
				Intent intent = new Intent(this, SpeechSetting.class);
				startActivity(intent);
			} catch (Exception e) {
				// TODO: handle exception
				Log.i("HHJ", "Exception e:" + e.toString());
				Toast.makeText(this, getString(R.string.iflytek_tips),
						Toast.LENGTH_LONG).show();
			}

		} else if (v == workspace_lock_layout) {
			if (Settings.Global.getInt(getContentResolver(), "workspace_lock",
					getResources().getInteger(R.integer.workspace_lock)) == 0) {
				Settings.Global.putInt(getContentResolver(), "workspace_lock",
						1);
				I99ThemeToast.toast(this,
						getString(R.string.workspase_lock_info), "l",
						Color.parseColor("#FFFFFF"));
			} else {
				Settings.Global.putInt(getContentResolver(), "workspace_lock",
						0);
			}
			setWorkspaceLock();
        }// added by jiangyan start
        else if (v == clock_style_choice_layout) {
            Intent intent = new Intent(this, ClockStyleChoiceSetting.class);
            startActivity(intent);
        }// added by jiangyan end
         // added by yzs for low battery warring begin
		else if (v == mLowBattery) {
			Intent intent = new Intent(this, LowBatterySetting.class);
			startActivity(intent);
		}
		// added by yzs for low battery warring end
		else if (v == speaker_layout) {// dengying speaker
			if (Settings.Global.getInt(getContentResolver(), "speaker", 1) == 0) {
				Settings.Global.putInt(getContentResolver(), "speaker", 1);
			} else {
				Settings.Global.putInt(getContentResolver(), "speaker", 0);
			}
			setSpeaker();
		} else if (v == home_key_wake_up_layout) {// dengying home_key_wake_up
			if (Settings.Global.getInt(getContentResolver(),
					"home_key_wake_up", 1) == 0) {
				Settings.Global.putInt(getContentResolver(),
						"home_key_wake_up", 1);
			} else {
				Settings.Global.putInt(getContentResolver(),
						"home_key_wake_up", 0);
			}
			setHomeKeyWakeUp();
		}


/* Cappu:liukun on: Tue, 15 Aug 2017 13:29:07 +0800
 * TODO: 在桌面设置里面添加指示灯开关 
 */
                else if (v == indicator_light_layout) {
                    if (Settings.Global.getInt(getContentResolver(), "indicator_light_layout" , 1) == 0){
                        Settings.Global.putInt(getContentResolver(), "indicator_light_layout" , 1);
                    } else {
                        Settings.Global.putInt(getContentResolver(), "indicator_light_layout" , 0);
                    }
                    setIndicatorLight();
                }
// End of Cappu:liukun

		// dengying@20150609 begin
		/*else if (v == bootaudio_setting_layout) {
			String on = getBootAudioSystemProperities();
			Log.i("BootaudioActivity", "getBootAudioSystemProperities  on:"
					+ on);
			Intent intent = new Intent("com.android.cappu.bootaudio");
			if (on.equals("1")) {
				updateBootAudioStatus(false);
				intent.putExtra("isOpen", false);
				startActivity(intent);
			} else {
				updateBootAudioStatus(true);
				intent.putExtra("isOpen", true);
				startActivity(intent);
			}
		}*/// dengying@20150609 end
		else if (v == android_layout) {
			try {
				Intent intent = new Intent(Settings.ACTION_SETTINGS); // ,"com.android.setting.Settings"
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(this, getString(R.string.activity_not_found),
						Toast.LENGTH_LONG).show();
			}
		} else if (v == gprs_setting_layout) {
			if (BasicKEY.LAUNCHER_VERSION == BasicKEY.CAPPU_LAUNCHER) {
				Intent intent = new Intent(this, GprsPwdActivity.class);
				startActivity(intent);
			} else {
				ActivityNotFind();
			}

		} else if (v == workspace_switch_layout) {
			Intent intent = new Intent(this, WorkapaceSettingActivity.class);
			startActivity(intent);
		}// by zhouhua 20161124
		else if (v == about_phone_icon_layout) {
			Intent intent =  new Intent(this, About.class);//new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);

			startActivity(intent);
		} else if (v == about_layout) {
			Intent intent = new Intent(this, AboutCareOS.class);
			startActivity(intent);
		}
    }
    @Override
    public void onClick(View v) {
		if (v == dialog_cancel) {
			if (mPopupWindowGprs != null && mPopupWindowGprs.isShowing()) {
				mPopupWindowGprs.dismiss();
			}
		} else if (v == dialog_confirm) {
			if (mTelephonyManager != null) {
				Intent intent = new Intent("android.net.conn.SETDATA_ON");// zhaogangzhu@20150929
				this.sendBroadcast(intent);
			}
			if (mPopupWindowGprs != null && mPopupWindowGprs.isShowing()) {
				mPopupWindowGprs.dismiss();
			}
		}else if(v==mCancel){
			finish();
		}
    }
    public void ActivityNotFind() {
        final I99Dialog delete = new I99Dialog(this);
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
    
    //modify by even hall menu
    //start
    private String getBootAudioSystemProperities(){
            return SystemProperties.get(BOOTAUDIO_PROPERTY,"1");
    }
    //end

    protected Dialog onCreateDialog(int id) {
        
        if(mLayoutInflater == null){
            mLayoutInflater = LayoutInflater.from(this);
        }

        switch (id) {
        case FONT_SIZE_DIALOG:
			mFontEBuilder = new CareDialog.Builder(this);
			int textSize = Settings.Global.getInt(getContentResolver(),
					"textSize",
					getResources().getDimensionPixelSize(R.dimen.xl_text_size));
			mFontView = mLayoutInflater.inflate(R.layout.font_choose, null);
			RelativeLayout small_layout = (RelativeLayout) mFontView
					.findViewById(R.id.small_layout);
			small_layout.setOnClickListener(mFontClickListener);
			CheckBox small_check = (CheckBox) mFontView
					.findViewById(R.id.small_check);
			small_check.setOnClickListener(mFontClickListener);

			RelativeLayout big_layout = (RelativeLayout) mFontView
					.findViewById(R.id.big_layout);
			big_layout.setOnClickListener(mFontClickListener);
			CheckBox big_check = (CheckBox) mFontView
					.findViewById(R.id.big_check);
			big_check.setOnClickListener(mFontClickListener);

			RelativeLayout big_more_layout = (RelativeLayout) mFontView
					.findViewById(R.id.big_more_layout);
			big_more_layout.setOnClickListener(mFontClickListener);
			CheckBox big_more_check = (CheckBox) mFontView
					.findViewById(R.id.big_more_check);
			big_more_check.setOnClickListener(mFontClickListener);
			mFontEBuilder.setTitle(getString(R.string.textsize_title));
			mFontEBuilder.setView(mFontView);
			if (textSize == getResources().getDimensionPixelSize(
					R.dimen.xl_text_size)) {
				big_check.setChecked(true);
				big_more_check.setChecked(false);
				small_check.setChecked(false);
			} else if (textSize == getResources().getDimensionPixelSize(
					R.dimen.xxl_text_size) || textSize == getResources().getDimensionPixelSize(
							R.dimen.china_xxl_text_size)) {
				big_check.setChecked(false);
				big_more_check.setChecked(true);
				small_check.setChecked(false);
			} else if (textSize == getResources().getDimensionPixelSize(
					R.dimen.l_text_size)) {
				big_check.setChecked(false);
				big_more_check.setChecked(false);
				small_check.setChecked(true);
			}
            return mFontEBuilder.create();
        default:
            return null;
        }
    }
    
    private OnClickListener mFontClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int ts = -1;
            switch (v.getId()) {
            case R.id.small_layout:
            case R.id.small_check:
                ts = getResources().getDimensionPixelSize(R.dimen.l_text_size);
                break;
            case R.id.big_layout:
            case R.id.big_check:
                ts = getResources().getDimensionPixelSize(R.dimen.xl_text_size);
                break;
            case R.id.big_more_layout:
            case R.id.big_more_check:
            	if(ThemeManager.getInstance().getCurrentThemeType()==ThemeManager.THEME_NINE_GRIDS){
            		ts = getResources().getDimensionPixelSize(R.dimen.china_xxl_text_size);
            	}else{
            		ts = getResources().getDimensionPixelSize(R.dimen.xxl_text_size);
            	}
                break;
            default:
                break;
            }
            LauncherLog.v(TAG, "mFontClickListener,jeff ts="+ts);
            Settings.Global.putInt(getContentResolver(), "textSize", ts);
            sendBroadcast(new Intent("com.cappu.bubbleview.refresh"));
			if (ts == getResources().getDimensionPixelSize(R.dimen.l_text_size)) {
				textsize_layout.setSubContent(R.string.font_level_small);
			} else if (ts == getResources().getDimensionPixelSize(
					R.dimen.xl_text_size)) {
				textsize_layout.setSubContent(R.string.font_level_big);
			} else if (ts == getResources().getDimensionPixelSize(
					R.dimen.xxl_text_size)
					|| ts == getResources().getDimensionPixelSize(
							R.dimen.china_xxl_text_size)) {
				textsize_layout.setSubContent(R.string.font_level_big_most);
			} else {
				textsize_layout.setSubContent(R.string.font_level_big);
			}
            mFontDialog.dismiss();
            
        }
    };
    
    private void showSimDialog() {
        if (mSimDialog == null) {
            mSimDialog = new Dialog(this);
            mSimDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSimDialog.setContentView(R.layout.sim_dialog);
            mSimDialog.setCancelable(false);

        }
        mSimDialog.show();
        new Thread() {
            @Override
            public void run() {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    Log.i("HHJ", " InterruptedException  e: "+e.toString());
                }
                mHandler.post(new Runnable() {
                    public void run() {
                        mSimDialog.dismiss();
                    }
                });
            }
        }.start();
    }

    /**检测GPRS是否打开*/
    private boolean gprsIsOpenMethod(String methodName) {
        Class cmClass = mConnectivityManager.getClass();
        Class[] argClasses = null;
        Object[] argObject = null;

        Boolean isOpen = false;
        try {
            Method method = cmClass.getMethod(methodName, argClasses);

            isOpen = (Boolean) method.invoke(mConnectivityManager, argObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOpen;
    }
    /**获取手机号*/
    public String getPhoneNumber(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
        String imei = tm.getDeviceId(); 
        String tel = tm.getLine1Number(); 
        return tel;
    }
    
    //dengying@20150408 wifi open gprs status update
    public boolean isSimOk(){
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        return (TelephonyManager.SIM_STATE_READY == tm.getSimState());
    }    
    
    private class MyHandler extends Handler  
    {   
        @Override  
        public void handleMessage(Message msg) {  
            // TODO Auto-generated method stub  
            super.handleMessage(msg);  
            if(msg.what==1) {  
		    	updateDataConnStatus();
            }  
        }  
    }
    
    private class MyThread implements Runnable{
    	@Override
        public void run(){
		    try{
		    	Thread.sleep(5000);

                //发送消息到handler
                Message mMsg = new Message();
                mMsg.what=1;
                myHandler.sendMessage(mMsg);
	 	    }catch(Exception ex){
	 	    	ex.printStackTrace();
		    }
        }
    }

    private void updateDataConnStatus(){
    	boolean isGprs = gprsIsOpenMethod("getMobileDataEnabled");
        if(isGprs && isSimOk()){
        	gprs_layout.setSelected(true);
        }else{
        	gprs_layout.setSelected(false);
        }
    }    
    
    public class GPRSIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
	        // add by y.haiyang for remove check dialog (start)
			String action = intent.getAction();
			boolean wifiConn = mConnectivityManager.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
			boolean isGprs = gprsIsOpenMethod("getMobileDataEnabled");

			Log.i("nian", "action = " + action);
			Log.i("dengyingDataConn", "LauncherSettingActivity action = "
					+ action + " wifiConn=" + wifiConn + " isGprs=" + isGprs);
			if ("android.net.conn.CONNECTIVITY_CANCEL".equals(action)) {
				gprs_layout.setSelected(false);
				gprs_layout.setEnabled(true);
			} else if ("android.net.conn.CONNECTIVITY_CHANGEING".equals(action)) {
				if (isSimOk()) {
					gprs_layout.setSelected(true);
				}

				if (!wifiConn) {
					gprs_layout.setEnabled(false);
				} else {
					new Thread(new MyThread()).start();
				}
			} else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
				updateDataConnStatus();
				gprs_layout.setEnabled(true);
			}
		}
    }    
    //dengying@20150408 wifi open gprs status update end
    
    /* 当设置铃声之后的回调函数 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1:
                try {
                    // 得到我们选择的铃声
                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    setRingtonePrefs(pickedUri.toString());
                    // 将我们选择的铃声设置成为默认
                    if (pickedUri != null) {
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, pickedUri);
                    }
                } catch (Exception e) {
                }
                break;

            case 2:
                try {
                    // 得到我们选择的铃声
                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    // 将我们选择的铃声设置成为默认
                    if (pickedUri != null) {
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, pickedUri);
                    }
                } catch (Exception e) {
                }
                break;
            case 3:
                try {
                    // 得到我们选择的铃声
                    Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    // 将我们选择的铃声设置成为默认
                    if (pickedUri != null) {
                        RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, pickedUri);
                    }
                } catch (Exception e) {
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    public void getSharedPreferences(Context context){
        if (mPrefs == null) {
            mPrefs = context.getSharedPreferences("ringtone", 0);
        }
    }
    
    public void setRingtonePrefs(String value) {
        getSharedPreferences(this);
        mPrefs.edit().putString("sounds", value).commit();
    }

    public String getRingtonePrefs() {
        getSharedPreferences(this);
        return mPrefs.getString("sounds",null);
    }
    
    public Uri getDefaultRingtoneUri(){ 
        return RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE); 
    } 

    @Override
    public void onThemeChanged(int theme) {
    	LauncherLog.v(TAG, "onThemeChanged,jeff theme="+theme);
    }
    
}
