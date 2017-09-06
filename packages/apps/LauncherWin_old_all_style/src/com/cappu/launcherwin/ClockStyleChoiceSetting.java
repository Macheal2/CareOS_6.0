package com.cappu.launcherwin;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.widget.TopBar;

/**
 * Author:Jiangyan Data:2017/8/2 Function:added for "clock style choice"
 * **/

public class ClockStyleChoiceSetting extends BasicActivity implements
        OnClickListener {

    private Button mAnalogBtn;
    private Button mDigitalBtn;
    public static Boolean isDigitalUsed = true;
    private LinearLayout mEnable;
    private ImageButton mStateButton;

    public static final String TAG = "ClockStyleChoiceSetting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock_style_choice_layout);
        initView();

    }

    private void initView() {
        // TODO Auto-generated method stub

        mAnalogBtn = (Button) findViewById(R.id.analog_clock_btn);
        mDigitalBtn = (Button) findViewById(R.id.digital_clock_btn);
        mEnable = (LinearLayout) findViewById(R.id.clock_enable);
        mStateButton = (ImageButton) findViewById(R.id.clock_state_switch);

        mAnalogBtn.setOnClickListener(this);
        mDigitalBtn.setOnClickListener(this);
        mEnable.setOnClickListener(this);
        mStateButton.setOnClickListener(this);

        boolean enableStatus = Settings.Global.getInt(getContentResolver(),
                "hall_setting", 1) == 1;

        if (enableStatus) {
            mStateButton.setBackgroundResource(R.drawable.setting_switch_on);
            if (isDigitalUsed) {
                Settings.Global.putInt(getContentResolver(),
                        "hall_ui_display", 99);
                setDigitalShow();
                isDigitalUsed = true;
            } else {
                Settings.Global.putInt(getContentResolver(),
                        "hall_ui_display", 1);
                setAnalogShow();
                isDigitalUsed = false;
            }

            clickable = true;
        } else {
            mStateButton.setBackgroundResource(R.drawable.setting_switch_off);

            setUnusedShow(isDigitalUsed);
            clickable = false;
        }

    }

    private boolean clickable = true;

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mCancel) {
            finish();
        }
        switch (v.getId()) {
        case R.id.analog_clock_btn:
            if (!clickable) {
                Toast.makeText(this, "未开启皮套时钟...", Toast.LENGTH_SHORT).show();
            } else {
                Settings.Global.putInt(getContentResolver(),
                        "hall_ui_display", 1);
                if (!isDigitalUsed) {
                    Toast.makeText(this, "模拟时钟正在使用...", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    setAnalogShow();
                }
                isDigitalUsed = false;
            }
            break;
        case R.id.digital_clock_btn:
            if (!clickable) {
                Toast.makeText(this, "未开启皮套时钟...", Toast.LENGTH_SHORT).show();
            } else {
                Settings.Global.putInt(getContentResolver(),
                        "hall_ui_display", 99);
                if (isDigitalUsed) {
                    Toast.makeText(this, "数字时钟正在使用...", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    setDigitalShow();
                }
                isDigitalUsed = true;
            }
            break;
        case R.id.clock_enable:
        case R.id.clock_state_switch:
            boolean enable = Settings.Global.getInt(getContentResolver(),
                    "hall_setting", 1) == 1;
           
            if (enable) {
                Settings.Global.putInt(getContentResolver(), "hall_setting", 0);

                mStateButton
                        .setBackgroundResource(R.drawable.setting_switch_off);
                setUnusedShow(isDigitalUsed);
                clickable = false;
            } else {
                Settings.Global.putInt(getContentResolver(), "hall_setting", 1);

                mStateButton
                        .setBackgroundResource(R.drawable.setting_switch_on);

                setDigitalShow();
                clickable = true;
                isDigitalUsed = true;
            }

            break;
        default:
            break;
        }
    }

    private void setDigitalShow() {
        mDigitalBtn.setBackground(getResources().getDrawable(
                R.drawable.clock_text_selected));
        mAnalogBtn.setBackground(getResources().getDrawable(
                R.drawable.clock_text_unselected));
    }

    private void setAnalogShow() {
        mAnalogBtn.setBackground(getResources().getDrawable(
                R.drawable.clock_text_selected));
        mDigitalBtn.setBackground(getResources().getDrawable(
                R.drawable.clock_text_unselected));
    }

    private void setUnusedShow(boolean isDigitalUsed) {
        if (isDigitalUsed) {
            mDigitalBtn.setBackground(getResources().getDrawable(
                    R.drawable.clock_text_selected_unused));
            mAnalogBtn.setBackground(getResources().getDrawable(
                    R.drawable.clock_text_unselected_unused));
        } else {
            mDigitalBtn.setBackground(getResources().getDrawable(
                    R.drawable.clock_text_unselected_unused));
            mAnalogBtn.setBackground(getResources().getDrawable(
                    R.drawable.clock_text_selected_unused));
        }
    }

}
