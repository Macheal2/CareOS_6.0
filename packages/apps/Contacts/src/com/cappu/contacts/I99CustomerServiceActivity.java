package com.cappu.contacts;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;
import com.cappu.theme.ThemeManager;
import android.widget.LinearLayout;

import com.android.contacts.R;

import com.cappu.contacts.util.I99Utils;

public class I99CustomerServiceActivity extends Activity implements View.OnClickListener{

    private View mCallMe;
    private ImageButton mCappuBu, mCareBu;
    private LinearLayout mDialogtitlebg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.i99_customer_service);
        mCallMe = findViewById(R.id.call_me);
        mCappuBu = (ImageButton)findViewById(R.id.cappu_bu);
        mCareBu = (ImageButton)findViewById(R.id.careos_bu);
        mCallMe.setOnClickListener(this);
        mCappuBu.setOnClickListener(this);
        mCareBu.setOnClickListener(this);
	
// yuan tong qin add start
	mDialogtitlebg = (LinearLayout)findViewById(R.id.customer_service_dialog_background);
        
        ThemeManager tm = ThemeManager.getInstance();
        int themeId = tm.getThemeType(this);
        if(themeId == 0){//default
        	mDialogtitlebg.setBackgroundResource(R.drawable.i99_dialog_top_bg);
        }else{
        	mDialogtitlebg.setBackgroundResource(R.drawable.i99_dialog_top_bg_2);
        }
//yuan tong qin add end 
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.call_me:
                String num = getResources().getString(R.string.customer_service_number);
                if(!TextUtils.isEmpty(num)){
                    I99Utils.doCall(I99CustomerServiceActivity.this, num);
                }
                break;
            case R.id.cappu_bu:

                break;
            case R.id.careos_bu:

                break;
        }
    }
}
