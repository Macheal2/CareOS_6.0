
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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//added by yzs begin
import com.cappu.launcherwin.basic.BasicKEY;
//added by yzs end

public class GprsPwdSettingActivity extends BasicActivity implements View.OnClickListener {
    public static final String ENABLE_DATA_CONN_SECRET_CODE = "enable_data_conn_secretcode";
    
    public static final String DATA_CONN_SECRET_CODE = "data_conn_secretcode";
    ConnectivityManager mConnectivityManager;
    Context mTContext;
    int mTextSize = 34;
    
    EditText pwd;
    ImageView pwd_clear;
    EditText pwd_again;
    ImageView pwd_again_clear;
    
    CheckBox show_pwd;
    Button btn_left;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_change_pwd);

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            mTContext = createPackageContext("com.cappu.launcherwin", Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            Log.i("HHJ", "NameNotFoundException e:" + e.toString());
        }
        init();
    }

    private void init() {
        pwd = (EditText) findViewById(R.id.pwd);
        pwd_clear = (ImageView) findViewById(R.id.pwd_clear);
        pwd_again = (EditText) findViewById(R.id.pwd_again);
        pwd_again_clear = (ImageView) findViewById(R.id.pwd_again_clear);
        
        show_pwd = (CheckBox) findViewById(R.id.show_pwd);
        btn_left = (Button) findViewById(R.id.btn_left);
        show_pwd.setOnClickListener(this);
        btn_left.setOnClickListener(this);
        pwd_clear.setOnClickListener(this);
        pwd_again_clear.setOnClickListener(this);
        
    }
    
    public void ShowPwd(boolean show){
        if(show){
            pwd.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            pwd_again.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }else{
            pwd.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pwd_again.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); 
        }
    }
    

    @Override
    public void onClick(View v) {
        if (v == mCancel) {
            finish();
        }else if (v == show_pwd){
            Log.i("HHJ", "show_pwd:"+show_pwd.isChecked());
            if(show_pwd.isChecked()){
                ShowPwd(true);
            }else{
                ShowPwd(false);
            }
        }else if(v == pwd_clear){
            pwd.setText(null);
        }else if(v == pwd_again_clear){
            pwd_again.setText(null);
        }else if(v == btn_left){
            Log.i("HHJ", "pwd.getText().toString():"+pwd.getText().toString()+"    pwd_again.getText().toString():"+pwd_again.getText().toString());
            if(pwd.getText().toString().equals(getPwd())){
                setPwd(pwd_again.getText().toString());
                finish();
            }else{
                Toast.makeText(this, getString(R.string.privacy_change_pwd_error), Toast.LENGTH_LONG).show();
            }
        }
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
    
    public String getPwd(){
	    //dengying@20150408 gprs pwd begin
        /*SharedPreferences sp = getSharedPreferences("gprs_pwd", 0);
        String  pwd = sp.getString("pwd", "9999");
        return pwd;*/
    	return Settings.Global.getString(mTContext.getContentResolver(), BasicKEY.DATA_CONN_SECRET_CODE);
    	//dengying@20150408 gprs pwd end
	}
    
    @Override
    protected void onResume() {
        super.onResume();
        
        try {
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
        mTitle.setTextSize(mTextSize);
    }

}
