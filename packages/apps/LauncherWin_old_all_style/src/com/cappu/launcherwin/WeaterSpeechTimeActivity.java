package com.cappu.launcherwin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.cappu.launcherwin.basic.BasicActivity;
import com.cappu.launcherwin.basic.BasicKEY;
import com.cappu.launcherwin.calendar.CalendarActivity;
import com.cappu.launcherwin.widget.CareTimePicker;
import com.cappu.launcherwin.widget.CareTimePickerDialog;
import com.cappu.launcherwin.widget.CareTimePickerDialog.OnTimeSetListener;
import com.cappu.launcherwin.widget.TopBar;

import android.R.integer;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeaterSpeechTimeActivity extends BasicActivity implements OnClickListener,OnTimeSetListener {

    TextView tips;
    TextView setting;
    
    SimpleDateFormat HH_time = new SimpleDateFormat("HH:mm");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weater_speech_time_setting_layout);
        init();
    }

    private void init() {
        tips = (TextView) findViewById(R.id.tips);
        setting = (TextView) findViewById(R.id.setting);
        setting.setOnClickListener(this);
        
        Integer[] times = getTime();
        
        String weatherSpeechTime = Settings.Global.getString(getContentResolver(), BasicKEY.LAUNCHER_WEATHER_SPEECH);
        if(weatherSpeechTime == null){
            weatherSpeechTime = "08:00";
        }
        setting.setText(weatherSpeechTime);
    }
    

    public Integer[] getTime(){
        Integer[] timesInt = new Integer[2]; 
        Calendar now = Calendar.getInstance();
        Date date = now.getTime();
        String times = HH_time.format(date);
        String[] time = times.split(":");
        int hour = 0;
        int minute = 0;
        try {
            hour = Integer.parseInt(time[0]);
            minute = Integer.parseInt(time[1]);
        } catch (Exception e) {
        }
        timesInt[0] = hour;
        timesInt[1] = minute;
        
        return timesInt;
        
    }
    @Override
    public void onClick(View v) {
        if(v == mCancel){
            finish();
        }else if(v == setting){
            Integer[] times = getTime();
            CareTimePickerDialog careTimePickerDialog = new CareTimePickerDialog(WeaterSpeechTimeActivity.this, this, times[0], times[1], true);
            careTimePickerDialog.show();
        }
    }
    @Override
    public void onTimeSet(CareTimePicker view, int hourOfDay, int minute) {
        Log.i("HHJ", "hourOfDay:"+hourOfDay+"     minute"+minute);
        
        
        if(minute<10){
            setting.setText(hourOfDay+":0"+minute);
            Settings.Global.putString(getContentResolver(), BasicKEY.LAUNCHER_WEATHER_SPEECH,hourOfDay+":0"+minute);
        }else{
            setting.setText(hourOfDay+":"+minute);
            Settings.Global.putString(getContentResolver(), BasicKEY.LAUNCHER_WEATHER_SPEECH,hourOfDay+":"+minute);
        }
        
    }
    
}
