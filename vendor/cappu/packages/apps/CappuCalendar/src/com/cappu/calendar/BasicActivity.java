package com.cappu.calendar;

import com.cappu.calendar.R;
import com.cappu.calendar.theme.ModeManager;
import com.cappu.calendar.theme.ModeManager.OnModeChangedListener;
import com.cappu.calendar.theme.ThemeManager;
import com.cappu.calendar.theme.ThemeManager.OnThemeChangedListener;
import com.cappu.calendar.theme.TopBar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class BasicActivity extends Activity implements /*OnThemeChangedListener,*/OnClickListener/*,OnModeChangedListener*/ {

    public ThemeManager mThemeManager;
    public ModeManager mModeManager;
    
    public ImageButton mCancel;
    public TextView mTitle;
    public ImageButton mOption;
    public static final String CALENDAR_MENU_SP = "menu_SharedPreferences";
    public static final String CALENDAR_MENU_SP_ACT = "menu_key_sp_activity";
    
    LinearLayout mMenuView;
    RadioGroup mRadioGroup;
    RadioButton mRedioMoonButton;
    RadioButton mRedioDetailButton;
    private PopupWindow mPopupWindowMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window window = this.getWindow();
        window.setStatusBarColor(0xffc50d22);
//        mThemeManager = ThemeManager.getInstance();
//        mModeManager = ModeManager.getInstance();
//        this.setTheme(mThemeManager.getThemeId());
        
        /* menu */
        mMenuView = (LinearLayout)View.inflate(this, R.layout.menu_layout, null);
        mRadioGroup = (RadioGroup) mMenuView.findViewById(R.id.menu_radio);
        mRedioMoonButton  = (RadioButton) mMenuView.findViewById(R.id.menu_redio_moon);
        mRedioDetailButton = (RadioButton) mMenuView.findViewById(R.id.menu_redio_detail);
        mPopupWindowMenu = new PopupWindow(mMenuView, LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
        mPopupWindowMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_popup_bg));
        mPopupWindowMenu.setFocusable(true);
        mPopupWindowMenu.setAnimationStyle(R.style.menushow);
        mPopupWindowMenu.update();
        mMenuView.setFocusableInTouchMode(true);
        mMenuView.setOnKeyListener(new android.view.View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopupWindowMenu.isShowing())) {
                    mPopupWindowMenu.dismiss();  
                    return true;
                }
                return false;
            }
        });
        //单选框
        mRadioGroup = (RadioGroup)mMenuView.findViewById(R.id.menu_radio);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if (checkedId == R.id.menu_redio_detail) {
                    mRedioMoonButton.setText(getResources().getString(R.string.menu_moon));
                    mRedioDetailButton.setText(getResources().getString(R.string.menu_detail)+getResources().getString(R.string.menu_default));
                    SharedPreferences.Editor editor = getSharedPreferences(CALENDAR_MENU_SP, getBaseContext().MODE_WORLD_WRITEABLE).edit();
                    editor.putInt(CALENDAR_MENU_SP_ACT, 0);
                    editor.commit();
                }else if (checkedId == R.id.menu_redio_moon){
                    mRedioMoonButton.setText(getResources().getString(R.string.menu_moon)+getResources().getString(R.string.menu_default));
                    mRedioDetailButton.setText(getResources().getString(R.string.menu_detail));
                    SharedPreferences.Editor editor = getSharedPreferences(CALENDAR_MENU_SP, getBaseContext().MODE_WORLD_WRITEABLE).edit();
                    editor.putInt(CALENDAR_MENU_SP_ACT, 1);
                    editor.commit();
                }
            }
        });
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        //Log.i(TAG, "onMenuOpened");
        if (mPopupWindowMenu != null) {
            mPopupWindowMenu.showAtLocation(mMenuView, Gravity.BOTTOM, 0, 0);
            mRadioGroup.setVisibility(View.VISIBLE);
        }
        return true;
    }
    
    public void initTopBar(){
        mCancel = (ImageButton) findViewById(TopBar.LEFT_ID);
        mTitle = (TextView) findViewById(TopBar.TOP_TITLE_ID);
        mOption = (ImageButton) findViewById(TopBar.RIGHT_ID);
        mCancel.setOnClickListener(this);
        mOption.setOnClickListener(this);
    }
    

    @Override
    protected void onResume() {
        super.onResume();
//        mThemeManager.setThemeChangedListener(this);
//        mModeManager.setModeChangedListener(this);
        SharedPreferences sp = getSharedPreferences(CALENDAR_MENU_SP, getBaseContext().MODE_WORLD_WRITEABLE);
        int intent = sp.getInt(CALENDAR_MENU_SP_ACT, -1);
        if (intent == 0){
            mRadioGroup.check(R.id.menu_redio_detail);//menu_redio_moon
        }else if (intent == 1) {
            mRadioGroup.check(R.id.menu_redio_moon);//menu_redio_detail
        }else{
            mRadioGroup.check(R.id.menu_redio_detail);//menu_redio_moon
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

//    @Override
//    public void onThemeChanged(int theme) {
//        recreate();
//    }


    @Override
    public void onClick(View arg0) {
        
    }

//    @Override
//    public void onModeChanged(int modeType) {
//        // TODO Auto-generated method stub
//        
//    }

}