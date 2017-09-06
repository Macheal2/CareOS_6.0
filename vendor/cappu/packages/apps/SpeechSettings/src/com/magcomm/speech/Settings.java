package com.magcomm.speech;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import com.cappu.app.CareDialog;//modify by even
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import com.cappu.widget.TopBar;//modify by even

import android.content.Context;

import com.magcomm.speech.common.SpeechTools;

public class Settings extends Activity implements OnItemClickListener {

    private static final String TAG = "SettingsSpeech";

    private static final String[] SPEAKER = {
        SpeechTools.SPEAKER_DEFAULT,
        SpeechTools.SPEAKER_YUE_YU,
        SpeechTools.SPEAKER_DONG_BEI,
        SpeechTools.SPEAKER_HE_NAN,
        SpeechTools.SPEAKER_HU_NAN,
        SpeechTools.SPEAKER_SI_CHUAN,
        SpeechTools.SPEAKER_TAIWAN,
    };

    private static final int[] DESCRIBE = {
        R.string.pu_tong_hua,
        R.string.yue_yu,
        R.string.dong_bei_hua,
        R.string.he_nan_hua,
        R.string.hu_nan_hua,
        R.string.si_chuan_hua,
        R.string.tai_wan_hua,
    };

    private List<Speaker> mData = new ArrayList<Speaker>();

    private TopBar mTopBar;
    private ListView mListView;
    private SpeechTools mSpeechTools;
    private SpeakerAdapter mSpeakerAdapter;

    private Speaker mSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings);

        init();
        mTopBar = (TopBar)findViewById(R.id.topbar);
        mTopBar.setOnTopBarListener(mTopBarListener);

        mListView = (ListView)findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        mSpeakerAdapter = new SpeakerAdapter();
        mListView.setAdapter(mSpeakerAdapter);
    }

    private void init(){
        mSpeechTools = new SpeechTools(Settings.this);
        final String spk = mSpeechTools.getSpeaker();
        mData.clear();
        for (int i=0 ;i< SPEAKER.length ; i++){
            Speaker speaker = new Speaker(SPEAKER[i],DESCRIBE[i],false);

            if(TextUtils.equals(spk, SPEAKER[i])){
                speaker.setState(true);
                mSpeaker = speaker;
            }
            mData.add(speaker);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

        String logo = getString(R.string.logo);
        Speaker speaker = mData.get(position);

        if(!TextUtils.equals(mSpeaker.speaker, speaker.speaker)){
            mSpeechTools.setSpeaker(speaker.speaker);
            mSpeaker.state = false;
            speaker.state = true;
            mSpeakerAdapter.notifyDataSetChanged();

            mSpeaker = speaker;
        }

        Log.i("1104","select speaker = " + speaker.speaker);
        mSpeechTools.startSpeech(logo);
    }


    private class SpeakerAdapter extends BaseAdapter {

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
            Speaker speaker = mData.get(position);
            if(view == null){
                LayoutInflater inflater = LayoutInflater.from(Settings.this);
                view = inflater.inflate(R.layout.item, null);
                holder = new ViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.state = (CheckBox) view.findViewById(R.id.state);
                view.setTag(holder);
            }else{
                holder = (ViewHolder)view.getTag();
            }

            holder.name.setText(speaker.describe);
            holder.state.setChecked(speaker.state);

            if(position == 0){
                view.setBackgroundResource(R.drawable.item_top_bg);
            }else if(position == mData.size() -1){
                view.setBackgroundResource(R.drawable.item_bottom_bg);
            }else{
                view.setBackgroundResource(R.drawable.item_middle_bg);
            }

            return view;
        }

        class ViewHolder {
            TextView name ;
            CheckBox state;
        }
    }


    private class Speaker {
        String speaker;
        int describe;
        boolean state;

        public Speaker(String speaker, int describe, boolean state){
            this.speaker = speaker;
            this.describe = describe;
            this.state = state;
        }

        public void setState(boolean state){
            this.state = state;
        }
    }

    private TopBar.onTopBarListener mTopBarListener = new TopBar.onTopBarListener(){

        public void onLeftClick(View v){
            String sp = mSpeechTools.getSpeaker();
            if(!TextUtils.equals(sp, mSpeaker.speaker)){
                showConfirmDialog();
            }else{
                Settings.this.finish();
            }
        }
        public void onRightClick(View v){
            SpeechTools.setSpeaker(Settings.this, mSpeaker.speaker);
            Settings.this.finish();
        }
        public void onTitleClick(View v){

        }
    };


    private void showConfirmDialog() {
        CareDialog.Builder builder = new CareDialog.Builder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.this.finish();
                        }
                    })
                .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SpeechTools.setSpeaker(Settings.this, mSpeaker.speaker);
                            Settings.this.finish();
                        }
                    });
        builder.create().show();
    }


    @Override
    protected void onDestroy() {
        mSpeechTools.destory();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(mSpeechTools.isSpeaking()){
            mSpeechTools.stopSpeech();
        }
        super.onPause();
    }
}
