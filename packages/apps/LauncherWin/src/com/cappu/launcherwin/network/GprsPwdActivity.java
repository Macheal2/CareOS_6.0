
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//added by yzs begin
import com.cappu.launcherwin.basic.BasicKEY;
//added by yzs end

public class GprsPwdActivity extends BasicActivity implements View.OnClickListener {
    
    public static final String ENABLE_DATA_CONN_SECRET_CODE = "enable_data_conn_secretcode";
    
    public static final String DATA_CONN_SECRET_CODE = "data_conn_secretcode";

    ConnectivityManager mConnectivityManager;
    Context mTContext;
    int mTextSize = 34;
    
    RelativeLayout pwd_state_layout;
    ImageButton pwd_state_switch;
    RelativeLayout pwd_set_layout;
    TextView pwd_set_info;
    RelativeLayout pwd_reset_layout;
    
    private PopupWindow mPopupWindowGprs;
    LinearLayout mGprsDialogView;
    Button dialog_cancel;
    Button dialog_confirm;
    
    TextView title;
    TextView titleconfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ruyi_setting_gprs_pwd);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            mTContext = createPackageContext("com.cappu.launcherwin", Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Log.i("HHJ", "NameNotFoundException e:" + e.toString());
        }
        init();
    }

    private void init() {
        pwd_state_layout = (RelativeLayout) findViewById(R.id.pwd_state_layout);
        pwd_state_switch = (ImageButton) findViewById(R.id.pwd_state_switch);
        pwd_set_layout = (RelativeLayout) findViewById(R.id.pwd_set_layout);
        pwd_set_info = (TextView) findViewById(R.id.pwd_set_info);
        pwd_reset_layout = (RelativeLayout) findViewById(R.id.pwd_reset_layout);
        pwd_state_layout.setOnClickListener(this);
        pwd_set_layout.setOnClickListener(this);
        pwd_reset_layout.setOnClickListener(this);
        pwd_state_switch.setOnClickListener(this);
        
        setPwdInfo();
        setSwitch();
        
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
        
        dialog_cancel = (Button) mGprsDialogView.findViewById(R.id.dialog_cancel);
        dialog_confirm = (Button) mGprsDialogView.findViewById(R.id.dialog_confirm);
        title = (TextView) mGprsDialogView.findViewById(R.id.title);
        titleconfirm = (TextView) mGprsDialogView.findViewById(R.id.titleconfirm);
        title.setText(R.string.privacy_reset_password);
        titleconfirm.setText(R.string.privacy_reset_password_message);
        dialog_cancel.setOnClickListener(this);
        dialog_confirm.setOnClickListener(this);
        
        mPopupWindowGprs = new PopupWindow(mGprsDialogView, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        mPopupWindowGprs.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_default));
        mPopupWindowGprs.setFocusable(true);
        mPopupWindowGprs.setAnimationStyle(R.style.menushow);
        mPopupWindowGprs.update();
        mGprsDialogView.setFocusableInTouchMode(true);
    }
    
    public void setPwdInfo(){
        if("9999".equals(getPwd())){
            pwd_set_info.setText(R.string.privacy_set_password);
        }else{
            pwd_set_info.setText(R.string.privacy_modify_password);
        }
    }
    
    public void setSwitch(){
        if(getPwdOffOrOn()){
            pwd_state_switch.setImageResource(R.drawable.setting_switch_on);
        }else{
            pwd_state_switch.setImageResource(R.drawable.setting_switch_off);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCancel) {
            finish();
        }else if(v == pwd_state_layout || v == pwd_state_switch){
            if(getPwdOffOrOn()){
                setPwdOffOrOn(false);
            }else{
                setPwdOffOrOn(true);
            }
            
            setSwitch();
        }else if(v == pwd_set_layout){
            startActivity(new Intent(this, GprsPwdSettingActivity.class));
        }else if(v == pwd_reset_layout){
            mPopupWindowGprs.showAtLocation(mGprsDialogView, Gravity.BOTTOM, 0, 0);
        }else if(v == dialog_confirm){
            setPwd("9999");
            setPwdInfo();
            if(getPwd().equals("9999")){
                Toast.makeText(this, getString(R.string.Success), Toast.LENGTH_LONG).show();
            }
            if(mPopupWindowGprs.isShowing()){
                mPopupWindowGprs.dismiss();
            }
        }else if(v == dialog_cancel){
            if(mPopupWindowGprs.isShowing()){
                mPopupWindowGprs.dismiss();
            }
        }
    }
    
    public void setPwdOffOrOn(boolean state){
	    //dengying@20150408 gprs pwd begin
        /*SharedPreferences sp = getSharedPreferences("gprs_state", 0);
        Editor et = sp.edit();
        et.putBoolean("state", state);
        et.commit();*/
        Settings.Global.putInt(mTContext.getContentResolver(),BasicKEY.ENABLE_DATA_CONN_SECRET_CODE, (state?1:0));//dengying@20140913 dataconn secretcode
    	//dengying@20150408 gprs pwd end
	}
    
    public boolean getPwdOffOrOn(){
	    ///dengying@20150408 gprs pwd begin
        /*SharedPreferences sp = getSharedPreferences("gprs_state", 0);
        boolean state = sp.getBoolean("state", true);
        return state;*/
        
        return (1 == Settings.Global.getInt(mTContext.getContentResolver(),BasicKEY.ENABLE_DATA_CONN_SECRET_CODE,1));
    	//dengying@20150408 gprs pwd end
	}
    
    public String getPwd(){
	    //dengying@20150408 gprs pwd begin
        /*SharedPreferences sp = getSharedPreferences("gprs_pwd", 0);
        String  pwd = sp.getString("pwd", "9999");
        return pwd;*/
    	return Settings.Global.getString(mTContext.getContentResolver(), BasicKEY.DATA_CONN_SECRET_CODE);
    	//dengying@20150408 gprs pwd end
	}
    
    public void setPwd(String pad){
	    //dengying@20150408 gprs pwd begin
        /*SharedPreferences sp = getSharedPreferences("gprs_pwd", 0);
        Editor et = sp.edit();
        et.putString("pwd", pad);
        et.commit();*/
        Settings.Global.putString(mTContext.getContentResolver(), BasicKEY.DATA_CONN_SECRET_CODE,pad);//dengying@20140913 dataconn secretcode
    	//dengying@20150408 gprs pwd end
	}

    @Override
    protected void onResume() {
        super.onResume();
        
        setPwdInfo();
        
/*        try {
            mTContext = createPackageContext("com.cappu.launcherwin",Context.CONTEXT_IGNORE_SECURITY);
            int textSize = Settings.Secure.getInt(mTContext.getContentResolver(), "textSize");
            if(textSize == 60){
                mTextSize = 34;
            }else if(textSize == 50){
                mTextSize = 30;
            }else if(textSize == 42){
                mTextSize = 25;
            }else {
                mTextSize = 34;
            }
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            Log.i("HHJ", "NameNotFoundException e:"+e.toString());
        }
        mTitle.setTextSize(mTextSize);*/
    }

}
