package com.cappu.launcherwin;

import java.util.ArrayList;
import java.util.List;

import com.cappu.launcherwin.R;
import android.app.Activity;
import com.cappu.app.CareDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.view.Window;
import android.widget.LinearLayout;
import android.util.Log;
import com.cappu.widget.TopBar;

import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.Ringtone;

public class LowBatterySetting extends Activity implements OnClickListener, OnItemClickListener{
    private static final String TAG = "LowBatterySetting";
    private TopBar mTopBar;
    private LinearLayout mEnable;
    private ListView mList;
    private ImageButton mStateButton;
    private BatteryWarringAdapter mAdapter;
    private Status mPathStatus;
    private Context mContext;

    private Ringtone mRingtone ;

    private static final String[] SOURCES = {
        "/system/media/audio/ui/LowBattery.ogg",
        "/system/media/audio/ui/lowbattery01.ogg",
        "/system/media/audio/ui/lowbattery02.ogg",
        //"/system/media/audio/ui/lowbattery03.ogg",
        "/system/media/audio/ui/lowbattery04.ogg",
        "/system/media/audio/ui/lowbattery05.ogg",
        //"/system/media/audio/ui/lowbattery06.ogg",
    };
    
    private static final int[] DESCRIBE = {
        R.string.low_battery_default,
        R.string.low_battery_one,
        R.string.low_battery_two,
        //R.string.low_battery_three,
        R.string.low_battery_four,
        R.string.low_battery_five,
        //R.string.low_battery_six,
    };

    private List<Status> mData = new ArrayList<Status>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = LowBatterySetting.this;

        setContentView(R.layout.low_battery_layout);
        init();
        mTopBar = (TopBar)findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(mTopBarListener);   
    }

    private void init(){
        final String pathSource = Settings.Global.getString(getContentResolver(),Settings.Global.LOW_BATTERY_SOUND);
        mData.clear();
        for (int i=0 ;i< DESCRIBE.length ; i++){
            Status status = new Status(SOURCES[i], DESCRIBE[i],false);
            if(TextUtils.equals(pathSource, SOURCES[i])){
                status.setSelected(true);
                mPathStatus = status;
            }
            mData.add(status);
        }

        mEnable = (LinearLayout) findViewById(R.id.lowbattery_enable);
        mEnable.setOnClickListener(this);
        mStateButton = (ImageButton) findViewById(R.id.lowbattery_state_switch);
        mStateButton.setOnClickListener(this);

        final boolean enableStatus = Settings.Global.getInt(getContentResolver(), Settings.Global.POWER_SOUNDS_ENABLED, 1) == 1;

        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(this);
        mAdapter = new BatteryWarringAdapter();
        mList.setAdapter(mAdapter);

        if(enableStatus){
            mStateButton.setBackgroundResource(R.drawable.setting_switch_on);
            mList.setAlpha(1.0f);
            clickable = true;
        }else{
            mStateButton.setBackgroundResource(R.drawable.setting_switch_off);
            mList.setAlpha(0.5f);  
            clickable = false;   
        }

        mRingtone = new Ringtone(mContext, false);
        Log.i(TAG, " init is called and mRingtone = " + mRingtone);
    }

    private TopBar.onTopBarListener mTopBarListener = new TopBar.onTopBarListener(){

        public void onLeftClick(View v){
            final String pathSource = Settings.Global.getString(getContentResolver(),Settings.Global.LOW_BATTERY_SOUND);
            if(!TextUtils.equals(pathSource, mPathStatus.path)){
                showConfirmDialog();
            }else{
                LowBatterySetting.this.finish();
            }
        }
        public void onRightClick(View v){
            Settings.Global.putString(getContentResolver(),Settings.Global.LOW_BATTERY_SOUND, mPathStatus.path);
            LowBatterySetting.this.finish();
        }
        public void onTitleClick(View v){

        }
    };

    private void showConfirmDialog() {
        CareDialog.Builder builder = new CareDialog.Builder(this)
                .setTitle(R.string.i99_dialog_confirm_title)
                .setMessage(R.string.confirm_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            LowBatterySetting.this.finish();
                        }
                    })
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {                          
                            Settings.Global.putString(getContentResolver(),Settings.Global.LOW_BATTERY_SOUND, mPathStatus.path);
                            LowBatterySetting.this.finish();
                        }
                    });
        builder.create().show();
    }

    private boolean clickable = true;

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.lowbattery_enable:
            case R.id.lowbattery_state_switch:
                //Log.i(TAG, "onClick is called and enable = " + mEnableStatus);
                boolean enable = Settings.Global.getInt(getContentResolver(), Settings.Global.POWER_SOUNDS_ENABLED, 1) == 1;
                if (enable){
                    Settings.Global.putInt(getContentResolver(), Settings.Global.POWER_SOUNDS_ENABLED, 0);
                    mStateButton.setBackgroundResource(R.drawable.setting_switch_off);                    
                    mList.setAlpha(0.5f);
                    clickable = false;
                }else{
                    Settings.Global.putInt(getContentResolver(), Settings.Global.POWER_SOUNDS_ENABLED, 1);
                    mStateButton.setBackgroundResource(R.drawable.setting_switch_on);
                    mList.setAlpha(1.0f);  
                    clickable = true;          
                }  
            mAdapter.notifyDataSetChanged();            
            break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Status status = mData.get(position);

        if(!TextUtils.equals(mPathStatus.path, status.path)){
            mPathStatus.selected = false;
            status.selected = true;     
            //mPathStatus.path = status.path;
            mAdapter.notifyDataSetChanged();
 
            mPathStatus = status;    
        }
        if (mPathStatus.path != null) {
            final Uri soundUri = Uri.parse("file://" + mPathStatus.path);
            if (soundUri != null) {
                mRingtone.setUri(soundUri);
                if (mRingtone != null) {
                    mRingtone.setStreamType(AudioManager.STREAM_SYSTEM);
                    mRingtone.play();           
                } else {
                    Log.d(TAG, "playSounds: failed to load ringtone from uri: " + soundUri);
                }
            }   
        }
    }

    private class BatteryWarringAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {

            ViewHolder holder = null ;
            Status status = mData.get(position);
            if(view == null){
                LayoutInflater inflater = LayoutInflater.from(LowBatterySetting.this);
                view = inflater.inflate(R.layout.item, null);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.state = (CheckBox) view.findViewById(R.id.state);
                view.setTag(holder);
            }else{
                holder = (ViewHolder)view.getTag();
            }

            holder.name.setText(status.desc);
            holder.state.setChecked(status.selected);

            view.setClickable(!clickable);

            if(position == 0){
                view.setBackgroundResource(R.drawable.bg_selector_top);
            }else if(position == mData.size() -1){
                view.setBackgroundResource(R.drawable.bg_selector_bottom);
            }else{
                view.setBackgroundResource(R.drawable.bg_selector_middle);
            }

            return view;
        }

        class ViewHolder {
            TextView name ;
            CheckBox state;
        }
    }

    private class Status {
        String path;
        int desc;
        boolean selected;

        public Status(String path, int describe, boolean select){
            this.path = path;
            this.desc = describe;
            this.selected = select;
        }

        public void setSelected(boolean select){
            this.selected = select;
        }
    }
}
